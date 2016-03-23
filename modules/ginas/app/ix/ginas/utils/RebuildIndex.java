package ix.ginas.utils;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.utils.EntityUtils;

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
		try {
			UPDATE_MESSAGE = "Preprocessing ...";
			Collection<Class<?>> classes = getEntityClasses(models.split(","));

			long start = System.currentTimeMillis();
			EntityPersistAdapter.setUpdatingIndex(true);
			for (Class<?> eclass : classes) {

				Class idClass = Long.class;
				Field idf = EntityUtils.getIdFieldForClass(eclass);
				if (idf != null) {
					idClass = idf.getType();
				}
				//System.out.println(eclass + "\t" + idClass);
				Model.Finder finder = new Model.Finder(idClass, eclass);
				int page = 0;
				int pageSize = 10;
				int rcount = finder.findRowCount();
				UPDATE_MESSAGE = "Fetching first " + pageSize + " of " + rcount + " records in " + (System.currentTimeMillis() - start) + "ms";
				long totalTimeSerializing=0;
				while (true) {
					Query q = finder.query();
					q.setFirstRow(pageSize * page)
							.setMaxRows(pageSize);
					System.out.println("This is the raw sql:" + q.getGeneratedSql());
					List l = q.findList();
					EntityMapper em = EntityMapper.FULL_ENTITY_MAPPER();
					
					for (Object o : l) {
						long serialTime=System.currentTimeMillis();
						try {
							String v = em.valueToTree(o).toString();
						} catch (Exception e) {
							e.printStackTrace();
							Logger.info("Error serializing entity:" + o);
						}
						serialTime=System.currentTimeMillis()-serialTime;
						totalTimeSerializing+=serialTime;
						
					}
					long timesofar=(System.currentTimeMillis() - start);
					double serialFraction = totalTimeSerializing/(timesofar+0.0);
					
					UPDATE_MESSAGE += "\nRecords Processed:" + (page + 1) * pageSize + " of " + rcount + " in " +timesofar + "ms (" +totalTimeSerializing + "ms serializing, " +serialFraction + ")";
					if (l.isEmpty() || (page + 1) * pageSize > rcount) break;
					page++;
				}
				page = 0;
				pageSize = 10;
			}
			UPDATE_MESSAGE += "\n\nComplete.\nTotal Time:" + (System.currentTimeMillis() - start) + "ms";
		}catch(Exception e){
			e.printStackTrace();
			UPDATE_MESSAGE = e.getMessage();
		}finally {
			EntityPersistAdapter.setUpdatingIndex(false);
		}
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