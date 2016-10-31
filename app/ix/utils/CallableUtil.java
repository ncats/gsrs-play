package ix.utils;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import ix.utils.CallableUtil.TypedCallable;


public class CallableUtil {



	private static class DeferredThrower<E extends Throwable>{
		private E t;
		public void thenThrow(E t){
			this.t=t;
		}
		public <T> T tryCall(Callable<T> sup){
			try{
				return sup.call();
			}catch(Throwable t){
				thenThrow((E)t);
				return null;
			}
		}
		public void throwIfNecessary() throws E{
			if(t!=null){
				throw t;
			}
		}

	}
	
	//Cache of lookups for converting signatures to types
	private static Map<String,Class<?>> retTypes = new ConcurrentHashMap<>();

	/**
	 * Attempts to extract the return type of the {@link ix.utils.CallableUtil.TypedCallable} if it is 
	 * a lambda expression, using reflection. This is only possible because {@link ix.utils.CallableUtil.TypedCallable}
	 * implements {@link java.io.Serializable}. Even still, there is no guarantee that this will
	 * return anything more than {@link java.lang.Object} as its class, if the type is not runtime
	 * accessible.
	 * 
	 * @param tc
	 * @return
	 * @throws Exception
	 */
	private static Class<?> getLambdaReturnType(Object tc) throws Exception{
		SerializedLambda sl = getSerializedLambda(tc);
		
		String sig=sl.getImplMethodSignature();
		DeferredThrower<Exception> dt= new DeferredThrower<>();
		Class<?> ret= retTypes.computeIfAbsent(sig, s->dt.tryCall(()->{
				return Class.forName(sig.substring(sig.indexOf(")")).substring(2).replace('/', '.').replace(";", ""));
			}));
		dt.throwIfNecessary();
		return ret;

	}

	private static final String WRITE_REPLACE = "writeReplace";
	private static SerializedLambda getSerializedLambda(Object function) throws Exception {
		Objects.requireNonNull(function);
		
		for (Class<?> clazz = function.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
			try {
				Method replaceMethod = clazz.getDeclaredMethod(WRITE_REPLACE);
				replaceMethod.setAccessible(true);
				Object serializedForm = replaceMethod.invoke(function);

				if (serializedForm instanceof SerializedLambda) {
					return (SerializedLambda) serializedForm;
				}
			}catch (NoSuchMethodError e) {
				// fall through the loop and try the next class
			}catch (Throwable t) {
				throw new RuntimeException("Error while extracting serialized lambda", t);
			}
		}
		throw new Exception("writeReplace method not found");
	}
	
	
	private static Class<?> findTypeArguments(Type t) {
        if (t instanceof ParameterizedType) {
            Type[] typeArgs = ((ParameterizedType) t).getActualTypeArguments();
            return (Class<?>) typeArgs[0];
        } else if (t instanceof Class) {
            Class<?> c = (Class<?>) t;
            return findTypeArguments(c.getGenericSuperclass());
        } else{
        	return Object.class;
        }
    }

	/**
	 * Special form of a callable, which also returns the return type without
	 * actually calling the value generator.
	 * 
	 * <p>
	 * <b>Usage Note</b> that the default method {@link #getType()} is not guaranteed 
	 * to return the correct returning Type for the callable. It will fail if the 
	 * implementation of TypedCallable is not discoverable at runtime (due to type 
	 * erasure). In such cases, it will return <code>Object.class</code>.
	 * </p>
	 *
	 * <p>
	 * For example:
	 * </p>
	 * 
	 * <p>
	 * <pre>
	 * <code>
	 * TypedCallable tc = (()->"Test");
	 * tc.getType(); //will be Object.class
	 * 
	 * TypedCallable&lt;String&gt; tc2 = (()->"Test");
	 * tc2.getType(); //will be String.class
	 * 
	 * </code>
	 * </pre>
	 * </p>
	 *
	 * <p>
	 * In general, if &lt;T&gt; is explicitly typed in a subtype or implementation
	 * of {@link ix.utils.CallableUtil.TypedCallable} then the default method will 
	 * return that explicit type &lt;T&gt;. It will also work with lambdas, where 
	 * it returns the declared type (or the implicitly declared type). Care must be 
	 * taken that the implicitly declared type for a lambda is the expected type. 
	 * </p>
	 * 
	 * 

	 * 
	 * <p>
	 * To ensure that the type returned is correct, it is recommended that the static
	 * factory method {@link TypedCallable#of(Callable, Class)} is used for deferred callables,
	 * or {@link TypedCallable#of(Object)} for a simple wrapper around an object.
	 * </p>
	 * 
	 * 
	 * 
	 * @author peryeata
	 *
	 * @param <T>
	 */
	@FunctionalInterface
	public static interface TypedCallable<T> extends java.io.Serializable{
		public T call() throws Exception;
		
		//public Class<?> getType();
		
		default Class<?> getType(){
			
			Supplier<Class<?>> annonymousClassTypeGetter = ()->{
				try{
					Class<?> cls= (Class<?>) this.getClass().getDeclaredMethod("call").getReturnType();
					if(cls==Object.class){
						//interesting
					}
					return cls;
				}catch(Exception e2){
					try{
						return findTypeArguments(TypedCallable.this.getClass());
					}catch(Exception e3){
						return Object.class; //Fallback
					}
				}
			};
			if(this.getClass().isAnonymousClass() || !this.getClass().isSynthetic()){
				return annonymousClassTypeGetter.get();
			}
			try{
				return (Class<?>) getLambdaReturnType(this);
			}catch(Exception e){
				return annonymousClassTypeGetter.get();
			}
		}
		
		/**
		 * Creates a TypedCallable which will return the explicit given type
		 * to calls for {@link #getType()}. This is the preferred mechanism to
		 * generate a TypedCallable, as it will ensure that the return type is
		 * discoverable.
		 * @param call
		 * @param type
		 * @return
		 */
		public static <T> TypedCallable<T> of(final Callable<T> call,final Class<T> type){
			return new TypedCallable<T>(){
				private static final long serialVersionUID = 1L;
				@Override
				public T call() throws Exception {
					return call.call();
				}
				@Override
				public Class<?> getType() {
					return type;
				}
			};
		}
		@SuppressWarnings("unchecked")
		public static <T> TypedCallable<T> of(T t){
			return of(()->t, (Class<T>)t.getClass());
		}
	}
}
