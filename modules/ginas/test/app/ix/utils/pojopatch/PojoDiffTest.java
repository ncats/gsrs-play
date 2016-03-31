package app.ix.utils.pojopatch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.controllers.EntityFactory;
import ix.core.models.Author;
import ix.core.models.Keyword;
import ix.ginas.models.v1.Parameter;
import ix.ginas.models.v1.Property;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;
import ix.utils.pojopatch.PojoDiff;
import ix.utils.pojopatch.PojoPatch;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

/**
 * Created by katzelda on 3/7/16.
 */
public class PojoDiffTest {

    private List<UUID> uuids = new ArrayList<>();
    private int uuidIndex=0;
    ObjectMapper mapper = new ObjectMapper();

    private UUID getNextUUID(){
        if(uuidIndex > uuids.size()){
            UUID uuid = UUID.randomUUID();
            uuids.add(uuid);
            uuidIndex++;
            return uuid;
        }
        return uuids.get(uuidIndex++);
    }
    
   

    private String getUUID(int index){
        if(index == uuids.size()){
            UUID uuid = UUID.randomUUID();
            uuids.add(uuid);
            return uuid.toString();
        }
        return uuids.get(index).toString();
    }
    
    
    
    @Test
    public void sameObjectShouldNotHaveChanges() throws Exception {
        //we'll use Author since that's simple and it's an entity

        Author old = new Author();

        old.id = 12345L;

        PojoPatch patch = PojoDiff.getDiff(old, old);

        patch.apply(old);

        Author expected = new Author();
        expected.id = 12345L;

        assertEquals(expected.getName(), old.getName());
        assertEquals(expected.id, old.id);
    }

    @Test
    public void setField() throws Exception {
        Author old = new Author();

        old.id = 12345L;

        assertNull(old.lastname);

        Author update = new Author();
        update.id = 12345L;

        update.lastname = "Jones";

        PojoPatch patch = PojoDiff.getDiff(old, update);

        patch.apply(old);

        assertEquals(update.lastname, old.lastname);

        JsonMatches(update, old);

    }


    private void JsonMatches(Object expected, Object actual){
        assertEquals(mapper.valueToTree(expected), mapper.valueToTree(actual));
    }

    @Test
    public void nulloutField() throws Exception {
        Author old = new Author();

        old.id = 12345L;
        old.lastname = "Jones";


        Author update = new Author();
        update.id = 12345L;


        PojoPatch patch = PojoDiff.getDiff(old, update);

        patch.apply(old);

        assertNull(old.lastname);

        JsonMatches(update, old);

    }


    @Test
    public void addToList() throws Exception {

        List<Parameter> originalParams = new ArrayList<>();

        Property prop = new Property();

        prop.setParameters(originalParams);

        List<Parameter> updatedParams = new ArrayList<>();
        Parameter p1 = new Parameter();
        p1.setName( "foo");
        updatedParams.add(p1);

        Property update = new Property();

        update.setParameters(updatedParams);

        PojoPatch<Property> patch = PojoDiff.getDiff(prop, update);

        patch.apply(prop);

        assertEquals(updatedParams, prop.getParameters());

        JsonMatches(update, prop);

    }

    @Test
    public void removeFromList() throws Exception {

        List<Parameter> originalParams = new ArrayList<>();
        Parameter p1 = new Parameter();
        p1.setName("foo");

        Parameter p2 = new Parameter();
        p2.setName( "bar");

        originalParams.add(p1);
        originalParams.add(p2);

        Property prop = new Property();

        prop.setParameters(originalParams);


        List<Parameter> newParams = new ArrayList<>();
        newParams.add(p2);

        Property update = new Property();

        update.setParameters(newParams);

        PojoPatch<Property> patch = PojoDiff.getDiff(prop, update);

        patch.apply(prop);


        List<Parameter> updatedParams = prop.getParameters();
        assertEquals(1, updatedParams.size());
        //the Pojo diff sometimes changes the fields of the first value
        //and deletes the 2nd instead of the simplier deleting the first
        //so just make sure the only element in the list has the correct name
        //can't do reference check
        assertEquals(p2.getName(), updatedParams.get(0).getName());


        JsonMatches(update, prop);

    }

    @Test
    public void clearList() throws Exception {

        List<Parameter> originalParams = new ArrayList<>();
        Parameter p1 = new Parameter();
        p1.setName("foo");

        Parameter p2 = new Parameter();
        p2.setName("bar");

        originalParams.add(p1);
        originalParams.add(p2);

        Property prop = new Property();

        prop.setParameters(originalParams);


        Property update = new Property();

        update.setParameters(new ArrayList<Parameter>());

        PojoPatch<Property> patch = PojoDiff.getDiff(prop, update);

        patch.apply(prop);

        assertTrue(prop.getParameters().isEmpty());

        JsonMatches(update, prop);

    }
    @Test
    public void switchOrderSimple() throws Exception {
    	try{
	        List<Parameter> originalParams = new ArrayList<>();
	        Parameter p1 = new Parameter();
	        p1.setName("foo");
	
	        Parameter p2 = new Parameter();
	        p2.setName("bar");
	
	        originalParams.add(p1);
	        originalParams.add(p2);
	
	        Property prop = new Property();
	
	        prop.setParameters(originalParams);
	
	
	        Property update = new Property();
	
	        List<Parameter> newParams=new ArrayList<Parameter>();
	        newParams.add(p2);
	        newParams.add(p1);
	        update.setParameters(newParams);
	        PojoPatch<Property> patch = PojoDiff.getDiff(prop, update);
	        patch.apply(prop);
	        assertTrue(prop.getParameters().size()==2);
	        JsonMatches(update, prop);
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
    }
    @Test
    public void switchOrderIdsSimple() throws Exception {
    	try{
	        List<Parameter> originalParams = new ArrayList<>();
	        Parameter p1 = new Parameter();
	        p1.setName("foo");
	        UUID uui1=p1.getOrGenerateUUID();
	
	        Parameter p2 = new Parameter();
	        p2.setName("bar");
	        UUID uui2=p2.getOrGenerateUUID();
	
	        originalParams.add(p1);
	        originalParams.add(p2);
	
	        Property prop = new Property();
	
	        prop.setParameters(originalParams);
	
	
	        Property update = new Property();
	
	        List<Parameter> newParams=new ArrayList<Parameter>();
	        newParams.add(p2);
	        newParams.add(p1);
	        update.setParameters(newParams);
	        PojoPatch<Property> patch = PojoDiff.getDiff(prop, update);
	        patch.apply(prop);
	        assertTrue(prop.getParameters().size()==2);
	        assertEquals(update,prop);
	        
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
    }
    public class MapContainer{
    	public Map<String,String> fstring;
    	
    	public boolean equals(Object o){
    		if(o!=null && o instanceof MapContainer){
    			return ((MapContainer)o).fstring.equals(this.fstring);
    		}
    		return false;
    	}
    	
    	public String toString(){
    		if(fstring==null)return null;
    		return fstring.toString();
    	}
    	public void addProperty(String key, String val){
    		if(fstring==null){
    			fstring=new HashMap<String,String>();
    		}
    		fstring.put(key, val);
    	}
    }
    
    @Test
    public void addPropertiesToMap() throws Exception {
    	MapContainer mc1=new MapContainer();
    	mc1.addProperty("key1", "value1");
    	mc1.addProperty("key2", "value2");
    	MapContainer mc2=new MapContainer();
    	PojoPatch<MapContainer> patch = PojoDiff.getDiff(mc2, mc1);
    	patch.apply(mc2);
    	assertEquals(mc1,mc2);
    }
    
    @Test
    public void removePropertiesToMap() throws Exception {
    	MapContainer mc1=new MapContainer();
    	mc1.addProperty("key1", "value1");
    	mc1.addProperty("key2", "value2");
    	MapContainer mc2=new MapContainer();
    	PojoPatch<MapContainer> patch = PojoDiff.getDiff(mc1, mc2);
    	patch.apply(mc1);
    	assertEquals(mc2,mc1);
    }
    
    @Test
    public void addNewToListWithIDSimple() throws Exception {
    	try{
	        List<Parameter> originalParams = new ArrayList<>();
	        Parameter p1 = new Parameter();
	        p1.setName("foo");
	        UUID uui1=p1.getOrGenerateUUID();
	
	        Parameter p2 = new Parameter();
	        p2.setName("bar");
	        UUID uui2=p2.getOrGenerateUUID();
	
	        originalParams.add(p1);
	        originalParams.add(p2);
	
	        Property prop = new Property();
	
	        prop.setParameters(originalParams);
	
	
	        Property update = new Property();
	
	        List<Parameter> newParams=new ArrayList<Parameter>();
	        Parameter p3 = new Parameter();
	        p3.setName("foobar");
	        p3.getOrGenerateUUID();
	        newParams.add(p1);
	        newParams.add(p2);
	        newParams.add(p3);
	        update.setParameters(newParams);
	        PojoPatch<Property> patch = PojoDiff.getDiff(prop, update);
	        patch.apply(prop);
	        assertTrue(prop.getParameters().size()==3);
	        JsonMatches(update, prop);
	        
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
    }
    @Test
    public void addNewToListWithoutIDSimple() throws Exception {
    	try{
	        List<Parameter> originalParams = new ArrayList<>();
	        Parameter p1 = new Parameter();
	        p1.setName("foo");
	        UUID uui1=p1.getOrGenerateUUID();
	
	        Parameter p2 = new Parameter();
	        p2.setName("bar");
	        UUID uui2=p2.getOrGenerateUUID();
	
	        originalParams.add(p1);
	        originalParams.add(p2);
	
	        Property prop = new Property();
	
	        prop.setParameters(originalParams);
	
	
	        Property update = new Property();
	
	        List<Parameter> newParams=new ArrayList<Parameter>();
	        Parameter p3 = new Parameter();
	        p3.setName("foobar");
	        newParams.add(p1);
	        newParams.add(p2);
	        newParams.add(p3);
	        update.setParameters(newParams);
	        PojoPatch<Property> patch = PojoDiff.getDiff(prop, update);
	        patch.apply(prop);
	        assertTrue(prop.getParameters().size()==3);
	        assertEquals(update,prop);
	        
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
    }

    @Test
    public void addToSet() throws Exception{
        Property old = new Property();

        old.addReference(getUUID(0));

        Property newProp = new Property();

        newProp.addReference(getUUID(0));
        newProp.addReference(getUUID(1));

        PojoPatch<Property> patch = PojoDiff.getDiff(old, newProp);

        patch.apply(old);

        JsonMatches(newProp, old);

    }

    @Test
    public void addToSetThatHasMultiple() throws Exception{
        Property old = new Property();

        old.addReference(getUUID(0));
        old.addReference(getUUID(1));

        Property newProp = new Property();

        newProp.addReference(getUUID(0));
        newProp.addReference(getUUID(1));
        newProp.addReference(getUUID(2));


        PojoPatch<Property> patch = PojoDiff.getDiff(old, newProp);

        patch.apply(old);

        JsonMatches(newProp, old);

    }

    @Test
    public void removeFromSet() throws Exception{
        Property old = new Property();

        old.addReference(getUUID(0));
        old.addReference(getUUID(1));

        Property newProp = new Property();

        newProp.addReference(getUUID(1));


        PojoPatch<Property> patch = PojoDiff.getDiff(old, newProp);

        patch.apply(old);

        JsonMatches(newProp, old);

    }

    @Test
    public void elementsInSetReordered() throws Exception{
        Property old = new Property();

        old.addReference(getUUID(0));
        old.addReference(getUUID(1));

        Property newProp = new Property();

        newProp.addReference(getUUID(1));
        newProp.addReference(getUUID(0));


        PojoPatch<Property> patch = PojoDiff.getDiff(old, newProp);

        patch.apply(old);

        JsonMatches(newProp, old);

    }

    @Test
    public void removeMultipleFromSet() throws Exception{
        Property old = new Property();

        old.addReference(getUUID(0));
        old.addReference(getUUID(1));
        old.addReference(getUUID(2));

        Property newProp = new Property();

        newProp.addReference(getUUID(1));


        PojoPatch<Property> patch = PojoDiff.getDiff(old, newProp);

        patch.apply(old);

        JsonMatches(newProp, old);

    }

}
