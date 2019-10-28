package ix.core.controllers;

import java.util.Objects;
import java.util.UUID;

import ix.core.NamedResource;
import ix.core.UserFetcher;
import ix.core.chem.ChemCleaner;
import ix.core.chem.StructureProcessorTask;
import ix.core.models.Structure;
import ix.core.plugins.IxCache;
import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.utils.UUIDUtil;
import play.Logger;
import play.db.ebean.Model;
import play.mvc.Result;


@NamedResource(name="structures",
               type=Structure.class,
               description="Resource for handling chemical structures")
public class StructureFactory extends EntityFactory {

    static final Logger.ALogger AccessLogger = Logger.of("access");
    
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


    public static Result edits (UUID id) {
        return edits (id, Structure.class);
    }
    
    public static void saveTempStructure(Structure s){
        if(s.id==null)s.id=UUID.randomUUID();
        
        AccessLogger.info("{} {} {} {} \"{}\"", 
                UserFetcher.getActingUser(true).username, 
                "unknown", 
                "unknown",
                "structure search:" + s.id,
                s.molfile.trim().replace("\n", "\\n").replace("\r", ""));
        //IxCache.setTemp(s.id.toString(), EntityWrapper.of(s).toFullJson());
        IxCache.setTemp(s.id.toString(), EntityWrapper.of(s).toInternalJson());
    }
    
    public static Structure getTempStructure(String uuid){
        String jsn = (String)IxCache.getTemp(uuid);
        if(jsn==null)return null;
        try{
            return EntityUtils.getEntityInfoFor(Structure.class).fromJson(jsn);
        }catch(Exception e){
            Logger.error("Error deserializing structure", e);
            return null;
        }    
    }
    
    public static Structure getStructureFrom(String str, boolean store) {
        Objects.requireNonNull(str);
        if (UUIDUtil.isUUID(str)) {
            Structure s = StructureFactory.getStructure(str);
            if (s != null) {
                return s;
            }
        }
        try {
            Structure struc = new Structure();
            StructureProcessorTask.Builder taskBuilder = new StructureProcessorTask.Builder()
                                                                    .mol(str)
                                                                    .structure(struc)
                                                                    .query(true)
                                                                    .standardize(false);
            
            //If it doesn't work once, try after removing SGROUPS
            try{
                struc = taskBuilder.build().instrument();
            }catch(Exception e){
                Logger.error("Structure Exception", e);
                struc = taskBuilder.mol(ChemCleaner.removeSGroups(str))
                                   .build()
                                   .instrument();
            }
            
            if(store){
                StructureFactory.saveTempStructure(struc);
            }
            return struc;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Can not parse structure from:" + str, e);
            //throw new IllegalStateException("Can not parse structure from:" + str);
        }
    }
}
