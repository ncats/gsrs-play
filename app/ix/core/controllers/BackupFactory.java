package ix.core.controllers;

import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.NamedResource;
import ix.core.models.BackupEntity;
import ix.core.models.BaseModel;
import ix.core.util.EntityUtils.Key;
import ix.core.util.Java8Util;
import play.Logger;
import play.db.ebean.Model;
import play.mvc.Result;

@NamedResource(name="backups",
type=BackupEntity.class,
description="Backup of serialized form of entities")
public class BackupFactory extends EntityFactory {
    public static final Model.Finder<Long, BackupEntity> finder = 
        new Model.Finder(Long.class, BackupEntity.class);

    public static Result get (long id) {
        try {
            BackupEntity backup = finder.byId(id);
            if (backup != null) {
                ObjectMapper mapper = getEntityMapper ();
                return Java8Util.ok (mapper.valueToTree(backup));
            }
            return notFound ("Bad request: "+request().uri());
        }
        catch (Exception ex) {
            Logger.trace("Can't retrieve backup "+id, ex);
        }
        
        return internalServerError
            ("Unable to fullfil request: "+request().uri());
    }
    
    public static Optional<BackupEntity> getByRefId(String refid){

    	BackupEntity be = finder.where().eq("refid", refid).findUnique();
    	return Optional.ofNullable(be);
    }
    
    

    public static BackupEntity getByKey(Key k){
    	if(k.getIdNative() instanceof UUID ||
    			k.getIdNative() instanceof String){
    		return getByRefId(k.getIdString()).orElse(null); //TODO: this part is inconsistent
			//because the UUIDs considered unique
			//globally, but other IDs are not 
			//considered globally unique
    	}else if(k.getIdNative() instanceof Long || k.getIdNative() instanceof Integer ){
    		return getByRefId(k.getKind() + ":" + k.getIdString()).orElse(null);
    	}
    	
    	return null;
    }
  
    
    public static Integer getCount () {
        try {
            return getCount (finder);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Result field (Long id, String field) {
        try {
            
        	BackupEntity backup = finder.byId(id);
            if (backup != null) {
                return field (backup, field);
            }
            return notFound ("Bad request: "+request().uri());
        }
        catch (Exception ex) {
            Logger.trace("Can't retrieve backup "+id, ex);
        }

        return internalServerError
            ("Unable to fullfil request: "+request().uri());
    }
    
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }
    

    
    
    
    

}
