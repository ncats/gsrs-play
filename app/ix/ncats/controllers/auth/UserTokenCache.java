package ix.ncats.controllers.auth;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import ix.core.controllers.UserProfileFactory;
import ix.core.models.UserProfile;
import ix.core.util.CachedSupplier;
import ix.core.util.StreamUtil;
import ix.utils.Util;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

public class UserTokenCache{
	   static CachedSupplier<CacheManager> manager=CachedSupplier.of(()->CacheManager.getInstance());
	   private Cache tokenCache=null;
	   private ConcurrentHashMap<String,UserProfile> tokenCacheUserProfile=new ConcurrentHashMap<String,UserProfile>();
	   private long lastCacheUpdate=-1;
	   
	   public UserTokenCache(){
	    	
	    	//always hold onto the tokens for twice the time required
	    	long tres=Util.getTimeResolutionMS()*2;
	    	
	    	int maxElements=99999;
	        
	        CacheManager manager = UserTokenCache.manager.get();
	        
	        
	        CacheConfiguration config =
	            new CacheConfiguration (Authentication.CACHE_NAME, maxElements)
	            	.timeToLiveSeconds(tres/1000);
	        
	        tokenCache = new Cache (config);
	        manager.removeCache(Authentication.CACHE_NAME);
	        manager.addCache(tokenCache);

	        tokenCache.setSampledStatisticsEnabled(true);
	        
	        
	        manager.removeCache(Authentication.CACHE_NAME_UP);
	        
	        lastCacheUpdate=-1;
	}

	private void updateIfNeeded() {
		if (Util.getCanonicalCacheTimeStamp() != lastCacheUpdate) {
			updateUserProfileTokenCache();
		}
	}

	public void updateUserCache(UserProfile up) {
		try{
			up.getRoles();
		//EntityWrapper.of(up).toFullJson();
		
		}catch(Exception e){
			
		}
		tokenCache.put(new Element(up.getComputedToken(), up.getIdentifier()));
		tokenCacheUserProfile.put(up.getIdentifier(), up);
	}

	public UserProfile getUserProfileFromToken(String token) {
		updateIfNeeded();
		Element e=tokenCache.get(token);
		if(e==null){
			return null;
		}
		return (UserProfile)tokenCacheUserProfile.get(e.getObjectValue());
	}
	
	
	public UserProfile computeUserIfAbsent(String username, Function<String,UserProfile> fetcher){
		UserProfile up= tokenCacheUserProfile.get(username);
		if(up==null){
			up= fetcher.apply(username);
			if(up!=null){
				updateUserCache(up);
			}
			return up;
		}
		return up;
	}
	
	private void updateUserProfileTokenCache(){
    	try(Stream<UserProfile> stream=UserProfileFactory.userStream()){
    		stream.forEach(up->{
    			updateUserCache(up);
    		});
	    	lastCacheUpdate=Util.getCanonicalCacheTimeStamp();
    	}catch(Exception e){
    		e.printStackTrace();
    		throw e;
    	}
    }	
}