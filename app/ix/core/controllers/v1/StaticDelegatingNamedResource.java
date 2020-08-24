package ix.core.controllers.v1;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import ix.core.NamedResource;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.search.SearchFactory;
import ix.core.models.Role;
import ix.core.util.CachedSupplier;
import play.Logger;
import play.libs.F;
import play.mvc.Result;

/**
 * Implementation of {@link InstantiatedNamedResource} which wraps around an {@link EntityFactory}
 * class, delegating to the static methods found. This is currently implemented by overriding 
 * the fallback {@link InstantiatedNamedResource#operate(ix.core.controllers.v1.InstantiatedNamedResource.Operation)}
 * method, which is what the default interface uses when a specific operation method is not
 * implemented.
 * 
 * <p>
 * 
 * <b>Note:</b> Use of this class is discouraged. It only serves as stand-in between
 * the old static way of doing things, and a new, more Object-Oriented approach. 
 * 
 * </p>
 * @author peryeata
 * 
 *
 * @param <I> The type of ID
 * @param <V> The type of entity
 */
public class StaticDelegatingNamedResource<I,V> implements InstantiatedNamedResource<I,V>{

	@Override
    public boolean isAccessible(List<Role> roles) {
	    if(adminOnly){
	        return roles.contains(Role.Admin);
	    }else{
	        return InstantiatedNamedResource.super.isAccessible(roles); 
	    }
    }

    private ConcurrentHashMap<Operation, Function<Operation, Result>> resultList = new ConcurrentHashMap<>();
	
	private Class<? extends EntityFactory> ef;
	private NamedResource nr;
	private Class<V> entityType;
	private boolean adminOnly;
	
	private Function<String, Optional<I>> resolver=null;
	
	public StaticDelegatingNamedResource(Class<? extends EntityFactory> factory, Class<I> idType, Class<V> resource){
		this.ef=factory;
		this.nr=factory.getAnnotation(NamedResource.class);
		this.entityType=resource;
		this.adminOnly=nr.adminOnly();
		
		
		Arrays.stream(Operations.values())
			.map(o->o.op().withIdClass(idType))
			.forEach(op->{
				try{
					Method m = ef.getMethod(op.getOperationName(), op.asSigniture());
					resultList.put(op, (realOperation)->{
						Object[] raw= realOperation.asRawArguments();
						return CachedSupplier.ofCallable(()->{
							Object ret = m.invoke(null, raw);
							if(ret instanceof F.Promise){
								//TODO improve this timeout!!
								return ((F.Promise<Result>)ret).get(1, TimeUnit.HOURS);
							}
							return (Result)ret;
						}).get();
					});
				}catch(Exception e){
				    Logger.info("Not found operation:" +op.getOperationName() + " in " + factory.getName());
				}
			});
		
		
		if(nr.allowSearch()){
    		resultList.computeIfAbsent(Operations.SEARCH_OPERATION.op(), op->{
    			return (opp)->{
    				@SuppressWarnings("rawtypes")
    				List<Argument> args=opp.getArguments();
    				return SearchFactory.searchREST(
    						nr.searchRequestBuilderClass(),
    						resource,
    						(String)args.get(0).getValue(),
    						(int)args.get(1).getValue(),
    						(int)args.get(2).getValue(),
    						(int)args.get(3).getValue())
    						.get(1, TimeUnit.HOURS);
    			};
    		});
		}
		
		
		try{
			Method m = ef.getMethod("resolveID", String.class);
			this.resolver = (res)->{
				try{
					return (Optional<I>)m.invoke(null, res);
				}catch(Exception e){
					Logger.info("Error resolving ID:'" + res + "' from " + factory.getName());
					return Optional.empty();
				}
			};
		}catch(Exception e){
		    Logger.info("No resolveID operation for resource:" + factory.getName());
		}
		
		
		
	}
	
	@Override
	public Result operate(Operation op) {
		return resultList
				.getOrDefault(op,(o)->InstantiatedNamedResource.super.operate(o))
				.apply(op);
	}

	@Override
	public Optional<I> resolveID(String synonym) {
		try{
			if(resolver!=null){
				Optional<I> id1=resolver.apply(synonym);
				if(id1.isPresent()){
					return id1;
				}
			}
		}catch(Exception e){
			 Logger.info("Error resolving ID for :" + this.getName());
		}
		return InstantiatedNamedResource.super.resolveID(synonym);
	}

	@Override
	public String getName() {
		return nr.name();
	}

	@Override
	public String getDescription() {
		return nr.description();
	}

	@Override
	public Class<V> getTypeKind() {
		return entityType;
	}

	@Override
	public Set<ix.core.controllers.v1.InstantiatedNamedResource.Operation> getSupportedOperations() {
		return resultList.keySet();
	}

}
