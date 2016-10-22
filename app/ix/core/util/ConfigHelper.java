package ix.core.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import ix.core.Experimental;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.pojopointer.PojoPointer;
import ix.utils.Tuple;
import play.Play;

@Experimental
public class ConfigHelper {
	private final static CachedSupplier<ConcurrentHashMap<String, Object>> configVars= CachedSupplier.of(()->{
		return new ConcurrentHashMap<String,Object>();
	});
	
	public static <T> CachedSupplier<T> supplierOf(String path, T d){
		return CachedSupplier.of(()->{
			return getOrDefault(path, d);
		});
	}
	

	public static int getInt(String path, int def) {
		return getOrDefault(path, def);
	}
	public static boolean getBoolean(String path, boolean def){
		return getOrDefault(path, def);
	}
	public static long getLong(String path, long def){
		return getOrDefault(path, def);
	}
	public static  <T> T getOrDefault(String path, T def){
		Class<?> cls;
		if(def==null){
			cls=Object.class;
		}else{
			cls=def.getClass();
		}
		
		return (T) configVars
						.get()
						.computeIfAbsent(path,p->{
							return resolver.apply(Tuple.of(path,cls), def);
						});
	}
	
	/**
	 * Explicitly set a configuration variable
	 * 
	 * @param path
	 * @param value
	 */
	public static <T> void setConfig(String path, T value){
		configVars.get().put(path, value);
	}
	
	/**
	 * Explicitly sets the configuration fetcher
	 * 
	 * @param resolve
	 */
	public static void setConfigurationResolver(BiFunction<Tuple<String,Class<?>>, Object, Object> resolve){
		resolver=resolve;
	}
	
	private final static BiFunction<Tuple<String,Class<?>>,Object, Object> PLAY_CONFIG_RESOLVER=((f,d)->{
		String key = f.k();
		Class cls = f.v();
		
		if(cls.isAssignableFrom(Long.class)){
			return Play.application().configuration().getLong(key, (Long)d);
		}
		
		Object o= EntityWrapper.of(Play.application().configuration().asMap())
						.at(PojoPointer.fromUriPath(key.replace(".", "/"))) //This is a silly way
						.map(e->(Object)e.getValue())						//... but it works
						.orElse(d);
		
		return o;
		
	});
	public static BiFunction<Tuple<String,Class<?>>,Object, Object> resolver=PLAY_CONFIG_RESOLVER;

}
