package ix.core.controllers;

import java.util.Objects;
import java.util.UUID;

import ix.core.UserFetcher;
import ix.core.plugins.IxCache;
import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.ginas.models.v1.Subunit;
import ix.utils.UUIDUtil;
import play.Logger;
import play.db.ebean.Model;
import play.mvc.Result;

//TODO: Decide if this should be a named Resource directly
//@NamedResource(name="sequences",
//               type=Subunit.class,
//               description="Resource for handling protein and nucleic acid sequences")
public class SequenceFactory extends EntityFactory {
    static final Logger.ALogger AccessLogger = Logger.of("access");
    
    public static final Model.Finder<UUID, Subunit> finder =
            new Model.Finder(UUID.class, Subunit.class);
    
    public static Subunit getSubunit (UUID id) {
        Subunit s=getTempSequence(id.toString());
    	if(s!=null)return s;
        return getEntity (id, finder);
    }

    public static Subunit getSequence (String id) {
        Subunit s=getTempSequence(id);
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

    public static Result edits (UUID id) {
        return edits (id, Subunit.class);
    }

    
    public static void saveTempSequence(Subunit s){
    	if(s.uuid==null)s.uuid=UUID.randomUUID();
        AccessLogger.info("{} {} {} {} \"{}\"", 
        		UserFetcher.getActingUser(true).username, 
        		"unknown", 
        		"unknown",
        		"sequence search:" + s.uuid,
        		s.sequence.trim().replace("\n", "\\n").replace("\r", ""));
    	IxCache.setTemp(s.uuid.toString(), EntityWrapper.of(s).toFullJson());
    }
    
    public static Subunit getTempSequence(String uuid){
    	String jsn = (String)IxCache.getTemp(uuid);
    	if(jsn==null)return null;
    	try{
    		return EntityUtils.getEntityInfoFor(Subunit.class).fromJson(jsn);
    	}catch(Exception e){
    		Logger.error("Error deserializing sequence", e);
    		return null;
    	}
    	
    }
    
    public static Subunit getStructureFrom(String str, boolean store) {
        Objects.requireNonNull(str);
        if (UUIDUtil.isUUID(str)) {
            Subunit s = SequenceFactory.getSequence(str);
            if (s != null) {
                return s;
            }
        }
        try {
            Subunit su = new Subunit();
            su.sequence=str;
            su.subunitIndex=1;
            su.getOrGenerateUUID();
            if(store){
                SequenceFactory.saveTempSequence(su);
            }
            return su;
        } catch (Exception e) {
            throw new IllegalStateException("Can not parse sequence from:" + str);
        }
    }
}
