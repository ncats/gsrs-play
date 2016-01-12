package ix.ginas.utils;
import ix.core.adapters.EntityPersistAdapter;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.reflections.Reflections;

import play.Logger;
import play.db.ebean.Model;

import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RebuildIndex  {
	public static String UPDATE_MESSAGE = "";
	
	public static String getUpdateMessage(){
		return UPDATE_MESSAGE;
	}
	
    public static void updateLuceneIndex(String models) throws Exception{
    	UPDATE_MESSAGE="Preprocessing ...";
    	Collection<Class<?>> classes = getEntityClasses(models.split(","));
        
    	long start=System.currentTimeMillis();
        EntityPersistAdapter.setUpdatingIndex(true);
        for(Class<?> eclass: classes){
        	Class idClass=Long.class;
        	for(Field f:eclass.getFields()){
        		if(f.isAnnotationPresent(Id.class)){
        			idClass=f.getType();
        		}
        	}
        	//System.out.println(eclass + "\t" + idClass);
        	Model.Finder finder = new Model.Finder(idClass, eclass);
        	int page=0;
        	int pageSize=10;
        	int rcount=finder.findRowCount(); 
        	while(true){
	        	Query q=finder.query();
	        	q.setFirstRow(pageSize*page)
	            .setMaxRows(pageSize);
	        	List l=q.findList();
	        	ObjectMapper om = new ObjectMapper();
	        	for(Object o:l){
	        		try{
	        			String v=om.valueToTree(o).toString();
	        		}catch(Exception e){
	        			Logger.info("Error serializing entity:" + o);
	        		}
	        	}
	        	UPDATE_MESSAGE="Records Processed:" + (page+1)*pageSize + " of " + rcount +  " in " + (System.currentTimeMillis()-start) + "ms";
	        	if(l.isEmpty() || (page+1)*pageSize > rcount)break;
	        	page++;
        	}
        	page=0;
        	pageSize=10;
        }
        EntityPersistAdapter.setUpdatingIndex(false);
        UPDATE_MESSAGE="Complete.\nTotal Time:" + (System.currentTimeMillis()-start) + "ms";
       
    }
    public static Set<Class<?>> getEntityClasses (String[] models) throws Exception {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        for(String load: models) {
            load = load.trim();
            if (load.endsWith(".*")) {
                Reflections reflections = new Reflections
                    (load.substring(0, load.length()-2));
                Set<Class<?>> resources =
                    reflections.getTypesAnnotatedWith(Entity.class);
                for (Class<?> c : resources) {
                	classes.add((Class<? extends Entity>) c);
                }
            }
            else {
            	Reflections reflections = new Reflections
                        (load.substring(0, load.lastIndexOf(".")));
            	Set<Class<?>> resources =
                        reflections.getTypesAnnotatedWith(Entity.class);
                    for (Class<?> c : resources) {
                    	if(c.getName().equalsIgnoreCase(load))
                    		classes.add(c);
                    }
            }
        }
        return classes;
        
    }
}