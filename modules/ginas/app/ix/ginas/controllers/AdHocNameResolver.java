package ix.ginas.controllers;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import ix.core.util.CachedSupplier;
import ix.core.util.ConfigHelper;
import play.Logger;


public class AdHocNameResolver {
	
	private static CachedSupplier<Map<String,String>> omap = CachedSupplier.of(()->{
		String fname = ConfigHelper.getOrDefault("ix.ginas.mapping.synonymsFile", "");
	
		Map<String,String> lookup = new HashMap<String,String>();
		
		if(fname!=null && !fname.equals("")){
			try{
			Files.lines(new File(fname).toPath())
			     .forEach(l->{
			    	 String[] tabs= l.split("\t");
			    	 
			    	 if(tabs.length > 1){
			    		 lookup.putIfAbsent(tabs[0], tabs[1]);
			    	 }
			     });
			
			}catch(Exception e){
				Logger.error("Trouble reading synonym file", e);
			}
			     
		}
		return lookup;
	});

	/**
	 * Uses an ad-hoc lookup file to resolve legacy or arbitrary synonyms
	 * @param key
	 * @return
	 */
	public static Optional<String> getAdaptedRecordKey(String key){
		return Optional.ofNullable(omap.get().get(key));
	}
	
	
}
