package ix.core.controllers;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import ix.core.models.Payload;
import ix.core.models.Structure;
import ix.core.NamedResource;


@NamedResource(name="structures",
               type=Structure.class,
               description="Resource for handling chemical structures")
public class StructureFactory extends EntityFactory {
    
    public static final Model.Finder<UUID, Structure> finder = 
        new Model.Finder(UUID.class, Structure.class);
    
    public static Structure getStructure (UUID id) {
    	Structure s=getTempStructure(id.toString());
    	if(s!=null)return s;
        return getEntity (id, finder);
    }

    public static Structure getStructure (String id) {
    	Structure s=getTempStructure(id);
    	if(s!=null)return s;
        return getEntity (toUUID (id), finder);
    }
    
    public static Result count () { return count (finder); }
    
    public static Integer getCount () {
        try {
            return getCount (finder);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static Result get (UUID id, String select) {
        return get (id, select, finder);
    }

    public static Result field (UUID id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        return create (Structure.class, finder);
    }

    public static Result delete (UUID id) {
        return delete (id, finder);
    }

    public static Result edits (UUID id) {
        return edits (id, Structure.class);
    }

    public static Result update (UUID id, String field) {
        return update (id, field, Structure.class, finder);
    }

    public static Result updateEntity () {
        return EntityFactory.updateEntity(Structure.class);
    }
    

    
    public static void saveTempStructure(Structure s){
    	if(s.id==null)s.id=UUID.randomUUID();
    	play.cache.Cache.set(s.id.toString(), s);
    }
    
    public static Structure getTempStructure(String uuid){
    	return (Structure)play.cache.Cache.get(uuid);
    }
}
