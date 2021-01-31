package app.ix.utils.pojopatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import com.flipkart.zjsonpatch.JsonPatch;

import ix.core.controllers.EntityFactory;
import ix.core.models.Author;
import ix.core.models.Keyword;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.RunOnly;
import ix.ginas.models.EmbeddedKeywordList;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Parameter;
import ix.ginas.models.v1.Property;
import ix.test.SubstanceJsonUtil;
import ix.utils.pojopatch.PojoDiff;
import ix.utils.pojopatch.PojoPatch;

/**
 * Created by katzelda on 3/7/16.
 */

@RunWith(Parameterized.class)
public class PojoDiffTest{

    
    private List<UUID> uuids = new ArrayList<>();
    ObjectMapper mapper = EntityFactory.EntityMapper.JSON_DIFF_ENTITY_MAPPER();
    
    
    
    public static enum PatchType{
    	MUTATE_DIRECT,
    	MUTATE_USING_DIFF,
    	APPLY_DIFF_TO_CLONE,
    	RETURN_EXPECTED;
    	
    	public <T> T applyChange(T o1, T o2) throws Exception{
    		switch(this){
			case MUTATE_DIRECT:
				{
					PojoPatch<T> patch = PojoDiff.getDiff(o1, o2);
					patch.apply(o1);
					return o1;
				}
			case MUTATE_USING_DIFF:
				{
					EntityWrapper<T> ew1=EntityWrapper.of(o1);
		    		EntityWrapper<T> ew2=EntityWrapper.of(o2);
		    		JsonNode oldOne = ew1.toFullJsonNode();
		    		JsonNode newOne = ew2.toFullJsonNode();
		    		JsonNode jp= JsonDiff.asJson(oldOne, newOne);
		    		JsonNode modifiedOne= JsonPatch.apply(jp, oldOne);
		    		T tnew=ew1.getEntityInfo().fromJsonNode(modifiedOne);
		    		
		    		PojoPatch<T> patch = PojoDiff.getDiff(o1,tnew);
			    	patch.apply(o1);
			    	return o1;
				}
				
			case RETURN_EXPECTED:
				{
					return o2;
				}
			case APPLY_DIFF_TO_CLONE:
				{
					PojoPatch<T> patch = PojoDiff.getDiff(o1, o2);
					T clone=EntityWrapper.of(o1).getClone();
					patch.apply(clone);
					return clone;
				}
			default:
				throw new IllegalArgumentException("Uknown option:" + this);
    	
    	}
    	}
    }
    
    @Parameterized.Parameters(name = "{0}")
	public static List<Object[]> params(){
		List<Object[]> list = new ArrayList<>();
		for(PatchType type: PatchType.values()){
			list.add(new Object[]{type+" serailize neither",type,false,false});
			list.add(new Object[]{type+" serailize both",type,true,true});
			list.add(new Object[]{type+" serailize first",type,true,false});
			list.add(new Object[]{type+" serailize second",type,false,true});
		}
		
		return list;
	}
	
	boolean serializefirst=false;
	boolean serializesecond=false;

	public PojoDiffTest(String name, PatchType pt, boolean serializefirst, boolean serializesecond){
		this.ptype=pt;
		this.serializefirst=serializefirst;
		this.serializesecond=serializesecond;
		
	}
    
    public PatchType ptype=PatchType.MUTATE_DIRECT;
    public <T> T getChanged(T o1, T o2) throws Exception{
    	if(serializefirst){
    		EntityWrapper<T> ew=EntityWrapper.of(o1);
    		o1=ew.getEntityInfo().fromJsonNode(ew.toFullJsonNode());
    	}
    	if(serializesecond){
    		EntityWrapper<T> ew=EntityWrapper.of(o2);
    		o2=ew.getEntityInfo().fromJsonNode(ew.toFullJsonNode());
    	}
    	return ptype.applyChange(o1, o2);
         
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

        old=getChanged(old, old);

        Author expected = new Author();
        expected.id = 12345L;

        assertEquals(expected.getName(), old.getName());
        assertEquals(expected.id, old.id);
    }

    @Test
    public void embeddedKeywordListAddToBeginningAndEndShouldBeEquivalent() throws Exception {

    	EmbeddedKeywordList oldList = new EmbeddedKeywordList();
    	oldList.add(new Keyword("KEY","VALUE1"));
    	oldList.add(new Keyword("KEY","VALUE2"));

    	EmbeddedKeywordList newList = new EmbeddedKeywordList();
    	newList.add(new Keyword("KEY","VALUE0"));
    	newList.add(new Keyword("KEY","VALUE1"));
    	newList.add(new Keyword("KEY","VALUE2"));
    	newList.add(new Keyword("KEY","VALUE3"));


    	oldList=getChanged(oldList, newList);

    	assertEquals(oldList.size(), newList.size());

    	for(int i=0;i<oldList.size();i++){
    		assertEquals(oldList.get(i), newList.get(i));
    	}

    }

    @Test
    public void embeddedReferenceUUIDsToBeginningAndEndShouldWork() throws Exception {
    	Name oldName = new Name();
    	UUID onID = oldName.getOrGenerateUUID();
    	oldName.addReference("00000000-0000-0000-0000-000000000001");


    	Name newName = new Name();
    	newName.setUuid(onID);
    	newName.addReference("00000000-0000-0000-0000-000000000000");
    	newName.addReference("00000000-0000-0000-0000-000000000001");
    	newName.addReference("00000000-0000-0000-0000-000000000002");

    	oldName=getChanged(oldName, newName);

    	assertEquals(oldName.uuid, newName.uuid);

    	assertEquals(newName.getReferences().size(),oldName.getReferences().size());

    	assertTrue(oldName.getReferences().containsAll(newName.getReferences()));
    	assertTrue(newName.getReferences().containsAll(oldName.getReferences()));

    }

    @Test
    public void embeddedLanguagesToBeginningAndEndShouldWork() throws Exception {
    	Name oldName = new Name();
    	UUID onID = oldName.getOrGenerateUUID();
    	oldName.addLanguage("en");


    	Name newName = new Name();
    	newName.setUuid(onID);
    	newName.addLanguage("es");
    	newName.addLanguage("fr");
    	newName.addLanguage("en");

    	oldName=getChanged(oldName, newName);

    	assertEquals(oldName.uuid, newName.uuid);

    	assertEquals(newName.languages.size(),oldName.languages.size());

    	assertTrue(oldName.languages.containsAll(newName.languages));
    	assertTrue(newName.languages.containsAll(oldName.languages));

    }

    @Test
    public void setField() throws Exception {
        Author old = new Author();
        old.id = 12345L;

        assertNull(old.lastname);

        Author update = new Author();
        update.id = 12345L;
        update.lastname = "Jones";

        old=getChanged(old, update);

        assertEquals(update.lastname, old.lastname);

        jsonMatches(update, old);

    }


    private void jsonMatches(Object expected, Object actual){
    	JsonNode js1=mapper.valueToTree(expected);
    	JsonNode js2=mapper.valueToTree(actual);

    	try{
            SubstanceJsonUtil.assertEquals(js1, js2, Comparator.comparing(Objects::toString));
    		//assertEquals(js1,js2);
    	}catch(Throwable e){
    	    System.out.println("js1 class = " + js1.getClass());
            System.out.println("js2 class = " + js2.getClass());


    		System.out.println(JsonDiff.asJson(js1, js2));
    		throw e;
    	}
    }

    @Test
    public void nulloutField() throws Exception {
        Author old = new Author();

        old.id = 12345L;
        old.lastname = "Jones";


        Author update = new Author();
        update.id = 12345L;


        old=getChanged(old, update);

        assertNull(old.lastname);

        jsonMatches(update, old);

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

        prop=getChanged(prop, update);
        assertEquals(updatedParams, prop.getParameters());

        jsonMatches(update, prop);

    }
    @Test
    public void add3ToList() throws Exception {

        List<Parameter> originalParams = new ArrayList<>();

        
        Property prop = new Property();

        prop.setParameters(originalParams);

        List<Parameter> updatedParams = new ArrayList<>();
       
        Parameter p1 = new Parameter();
        p1.setName( "foo");
        updatedParams.add(p1);
        Parameter p2 = new Parameter();
        p2.setName( "foobar");
        updatedParams.add(p2);
        Parameter p3 = new Parameter();
        p3.setName( "foobarfoo");
        updatedParams.add(p3);
        
        updatedParams.stream().forEach(p->p.getOrGenerateUUID());

        Property update = new Property();

        update.setParameters(updatedParams);

        prop=getChanged(prop, update);
        
        assertSameContents(updatedParams,prop.getParameters());
        
        jsonMatches(update, prop);

    }
    
    private void assertSameContents(Collection<?> a, Collection<?> b){
        assertEquals(new HashSet<>(a), new HashSet<>(b));
    }

    static class Foo{
        List<String> innerList;
        @JsonCreator
        public Foo(@JsonProperty("innerList") List<String> innerList){
            this.innerList = innerList;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Foo foo = (Foo) o;

            return innerList != null ? innerList.equals(foo.innerList) : foo.innerList == null;
        }

        @Override
        public int hashCode() {
            return innerList != null ? innerList.hashCode() : 0;
        }
    }
    @Test
    public void EmbedListTest() throws Exception{
    	
        Foo s = new Foo(Arrays.asList("A, B"));
        Foo s2 = new Foo(Arrays.asList("A, B"));
    	
    	s=getChanged(s, s2);

    	jsonMatches(s, s2);
    	
    	
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

        prop=getChanged(prop, update);


        List<Parameter> updatedParams = prop.getParameters();
        assertEquals(1, updatedParams.size());
        //the Pojo diff sometimes changes the fields of the first value
        //and deletes the 2nd instead of the simplier deleting the first
        //so just make sure the only element in the list has the correct name
        //can't do reference check
        assertEquals(p2.getName(), updatedParams.get(0).getName());


        jsonMatches(update, prop);

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

        prop=getChanged(prop, update);

        assertTrue(prop.getParameters().isEmpty());

        jsonMatches(update, prop);

    }
    @Test
    public void switchOrderSimple() throws Exception {
	        List<Parameter> originalParams = new ArrayList<>();
	        Supplier<Parameter> p1 = ()->{
	        	Parameter p=new Parameter();
		        p.setName("foo");
		        return p;
	        };
	
	        Supplier<Parameter> p2 = ()->{
	        	Parameter p=new Parameter();
		        p.setName("bar");
		        return p;
	        };
	
	        originalParams.add(p1.get());
	        originalParams.add(p2.get());
	
	        Property prop = new Property();
	        
	        prop.setParameters(originalParams);
	
	        Property update = new Property();
	
	        List<Parameter> newParams=new ArrayList<Parameter>();
	        
	        newParams.add(p2.get());
	        newParams.add(p1.get());
	        
	        update.setParameters(newParams);
	        prop=getChanged(prop, update);
	        assertTrue(prop.getParameters().size()==2);
	        jsonMatches(update, prop);
    }
    @Test
    public void switchOrderIdsSimple() throws Exception {
	        List<Parameter> originalParams = new ArrayList<>();
	        Parameter p1 = new Parameter();
	        p1.setName("foo");
	        p1.getOrGenerateUUID();
	
	        Parameter p2 = new Parameter();
	        p2.setName("bar");
	        p2.getOrGenerateUUID();
	
	        originalParams.add(p1);
	        originalParams.add(p2);
	
	        Property prop = new Property();
	
	        prop.setParameters(originalParams);
	
	
	        Property update = new Property();
	
	        List<Parameter> newParams=new ArrayList<Parameter>();
	        newParams.add(p2);
	        newParams.add(p1);
	        update.setParameters(newParams);
	        prop=getChanged(prop, update);
	        assertTrue(prop.getParameters().size()==2);
	        assertEquals(update,prop);
    }
    public static class MapContainer{
    	public Map<String,String> fstring;
    	
    	public boolean equals(Object o){
    		if(o!=null && o instanceof MapContainer){
    			if(fstring==null){
    				if(((MapContainer)o).fstring==null){
    					return true;
    				}
    			}else{
    				return ((MapContainer)o).fstring.equals(this.fstring);
    			}
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
    	mc2=getChanged(mc2, mc1);
    	assertEquals(mc1,mc2);
    }
    
    @Test
    public void removePropertiesFromMap() throws Exception {
    	MapContainer mc1=new MapContainer();
    	mc1.addProperty("key1", "value1");
    	mc1.addProperty("key2", "value2");
    	MapContainer mc2=new MapContainer();
    	mc1=getChanged(mc1, mc2);
    	assertEquals(mc2,mc1);
    }
    
    @Test
    public void addNewToListWithIDSimple() throws Exception {
	        List<Parameter> originalParams = new ArrayList<>();
	        Parameter p1 = new Parameter();
	        p1.setName("foo");
	        p1.getOrGenerateUUID();
	
	        Parameter p2 = new Parameter();
	        p2.setName("bar");
	        p2.getOrGenerateUUID();
	
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
	        prop=getChanged(prop, update);
	        assertTrue(prop.getParameters().size()==3);
	        jsonMatches(update, prop);
	        
    }
    @Test
    public void addNewToListWithoutIDSimple() throws Exception {
	        List<Parameter> originalParams = new ArrayList<>();
	        Parameter p1 = new Parameter();
	        p1.setName("foo");
	        p1.getOrGenerateUUID();
	
	        Parameter p2 = new Parameter();
	        p2.setName("bar");
	        p2.getOrGenerateUUID();
	
	        originalParams.add(p1);
	        originalParams.add(p2);
	
	        Property prop = new Property();

	        prop.setParameters(originalParams);
	
	
	        Property update = new Property();

			update.setUuid(prop.getOrGenerateUUID());

	        List<Parameter> newParams=new ArrayList<>();
	        Parameter p3 = new Parameter();
	        p3.setName("foobar");
	        newParams.add(p1);
	        newParams.add(p2);
	        newParams.add(p3);
	        update.setParameters(newParams);
	        prop=getChanged(prop, update);
	        assertTrue(prop.getParameters().size()==3);
	        assertEquals(update,prop);
	        
    }
    @Test
    public void addNewToFrontOfListWithoutIDSimple() throws Exception {
        List<Parameter> originalParams = new ArrayList<>();
        Parameter p1 = new Parameter();
        p1.setName("foo");
        p1.getOrGenerateUUID();

        Parameter p2 = new Parameter();
        p2.setName("bar");
        p2.getOrGenerateUUID();

        originalParams.add(p1);
        originalParams.add(p2);

        Property prop = new Property();

        prop.setParameters(originalParams);


        Property update = new Property();

        List<Parameter> newParams=new ArrayList<Parameter>();
        Parameter p3 = new Parameter();
        p3.setName("foobar");
        newParams.add(p3);
        newParams.add(p1);
        newParams.add(p2);

        update.setParameters(newParams);
        prop=getChanged(prop, update);
        assertTrue(prop.getParameters().size()==3);
        assertEquals(update,prop);

    }

    @Test
    public void addMultipleNewToFrontOfListWithoutIDSimple() throws Exception {
        List<Parameter> originalParams = new ArrayList<>();
        Parameter p1 = new Parameter();
        p1.setName("foo");
        p1.getOrGenerateUUID();

        Parameter p2 = new Parameter();
        p2.setName("bar");
        p2.getOrGenerateUUID();

        originalParams.add(p1);
        originalParams.add(p2);

        Property prop = new Property();

        prop.setParameters(originalParams);


        Property update = new Property();

        List<Parameter> newParams=new ArrayList<Parameter>();
        Parameter p3 = new Parameter();
        p3.setName("foobar");

        Parameter p4 = new Parameter();
        p4.setName("asdf");
        Parameter p5 = new Parameter();
        p5.setName("p5");


        newParams.add(p3);
        newParams.add(p4);
        newParams.add(p5);

        newParams.add(p1);
        newParams.add(p2);

        update.setParameters(newParams);
        prop=getChanged(prop, update);
        assertEquals(5, prop.getParameters().size());
        assertEquals(update,prop);

    }

    @Test
    public void addToSet() throws Exception{
        Property old = new Property();

        old.addReference(getUUID(0));

        Property newProp = new Property();

        newProp.addReference(getUUID(0));
        newProp.addReference(getUUID(1));

        old=getChanged(old, newProp);
        jsonMatches(newProp, old);

    }
    @Test
    public void changeOrderAndPropertyOfIdentifiedObjects() throws Exception{
        Property oldp = new Property();
        List<Parameter> params = new ArrayList<Parameter>();
        UUID uuid1=UUID.randomUUID();
        UUID uuid2=UUID.randomUUID();
        UUID uuid3=UUID.randomUUID();
        
        Parameter p1 = new Parameter();
        p1.setUuid(uuid1);
        Parameter p2 = new Parameter();
        p2.setUuid(uuid2);
        Parameter p3 = new Parameter();
        p3.setUuid(uuid3);
        
        params.add(p1);
        params.add(p2);
        params.add(p3);
        
        oldp.setParameters(params);
        
        Property newp = new Property();
        List<Parameter> paramsnew = new ArrayList<Parameter>();
       
        
        Parameter p1b = new Parameter();
        p1b.setUuid(uuid1);
        Parameter p2b = new Parameter();
        p2b.setUuid(uuid2);
        Parameter p3b = new Parameter();
        p3b.setUuid(uuid3);
        
        p3b.addReference("NONSENSE");
        paramsnew.add(p3b);
        paramsnew.add(p2b);
        paramsnew.add(p1b);
        newp.setParameters(paramsnew);
        


        oldp=getChanged(oldp, newp);
        
        jsonMatches(newp, oldp);

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


        old=getChanged(old, newProp);

        jsonMatches(newProp, old);

    }

    @Test
    public void removeFromSet() throws Exception{
        Property old = new Property();

        old.addReference(getUUID(0));
        old.addReference(getUUID(1));

        Property newProp = new Property();

        newProp.addReference(getUUID(1));

        old=getChanged(old, newProp);

        jsonMatches(newProp, old);

    }

    @Test
    public void elementsInSetReordered() throws Exception{
        
    	Property old = new Property();
        old.addReference(getUUID(0));
        old.addReference(getUUID(1));
        old.addReference(getUUID(2));
        old.addReference(getUUID(3));
        old.addReference(getUUID(4));
        old.addReference(getUUID(5));

        Property newProp = new Property();
        
        newProp.addReference(getUUID(5));
        newProp.addReference(getUUID(4));
        newProp.addReference(getUUID(3));
        newProp.addReference(getUUID(2));
        newProp.addReference(getUUID(1));
        newProp.addReference(getUUID(0));

        old=getChanged(old, newProp);
        
        jsonMatches(newProp, old);

    }

    @Test
    public void removeMultipleFromSet() throws Exception{
    	int totalInSet =10;
    	
    	for(int i=1;i<totalInSet;i++){
    		for(int j=0;j<i;j++){
		        Property old = new Property();
		        for(int k=0;k<i;k++){
		        	old.addReference(getUUID(k));
		        }
		
		        Property newProp = new Property();
		        
		        newProp.addReference(getUUID(j));
		
		        old=getChanged(old, newProp);
		
		        jsonMatches(newProp, old);
    		}
    	}
    }
    
    @Test
    public void removeOneFromSet() throws Exception{
    	int totalInSet =10;
    	
    	for(int i=1;i<totalInSet;i++){
    		for(int j=0;j<i;j++){
		        Property old = new Property();
		        Property newProp = new Property();
		        
		        for(int k=0;k<i;k++){
		        	old.addReference(getUUID(k));
		        	if(k!=j){
		        		newProp.addReference(getUUID(k));
		        	}
		        }
		
		        old=getChanged(old, newProp);
		
		        jsonMatches(newProp, old);
    		}
    	}
    }
    
    
    @Test
    public void testRepetitivelyApplyAddingPatchAdds() throws Exception{
    	String adding="test";
    	List<String> mylist1 = new ArrayList<String>();
    	mylist1.add(adding);
    	List<String> mylist2 = new ArrayList<String>();
    	List<String> expected = new ArrayList<String>();
    	
    	PojoPatch<List<String>> pp=PojoDiff.getDiff(mylist2, mylist1);
    	
    	for(int i=0;i<10;i++){
    		pp.apply(mylist2);
    		expected.add(adding);
    		assertEquals(expected,mylist2);
    	}
    }
    
    @Test
    public void testSortingPatchWorks() throws Exception{
    	
    	List<String> mylist1 = new ArrayList<String>();
    	
    	
    	for(int i=0;i<100;i++){
    		mylist1.add("Testing:" + i);
    	}
    	List<String> mylist2 = new ArrayList<String>(mylist1);
    	Collections.shuffle(mylist1);
    	mylist1=getChanged(mylist1, mylist2);
    	assertEquals(mylist2,mylist1);
    		
    }

}
