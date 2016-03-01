package util.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * Created by katzelda on 2/25/16.
 */
public class JsonUtilTest {
    ObjectMapper mapper = new ObjectMapper();


    @Test
    public void buildSimpleKeyValuesStrings(){
        JsonNode a = new JsonUtil.JsonBuilder().add("name", "myname2")
                .add("type", "type2")
                .toJson();

        assertEquals("myname2", a.get("name").textValue());
        assertEquals("type2", a.get("type").textValue());
    }

    @Test
    public void missingFieldReturnsNullNode(){
        JsonNode a = new JsonUtil.JsonBuilder().add("name", "myname2")
                .add("type", "type2")
                .toJson();

        assertNull(a.get("does not exist"));
    }
    @Test
    public void buildSimpleKeyValuesIntegers(){
        JsonNode a = new JsonUtil.JsonBuilder().add("name", "myname2")
                .add("num", 5)
                .toJson();

        assertEquals("myname2", a.get("name").textValue());
        assertEquals(5, a.get("num").asInt());
    }

    @Test
    public void arrayStringSingleElement(){
        String[] array = {"foo"};
        JsonNode a = new JsonUtil.JsonBuilder().add("array", array)
                            .toJson();
        assertTrue(a.get("array").isArray());
        assertEquals(1, a.get("array").size());
        assertEquals("foo", a.get("array").get(0).textValue());
    }

    @Test
    public void arrayIntegerSingleElement(){
        int[] array = {7};
        JsonNode a = new JsonUtil.JsonBuilder().add("array", array)
                .toJson();
        assertTrue(a.get("array").isArray());
        assertEquals(1, a.get("array").size());
        assertEquals(7, a.get("array").get(0).intValue());
    }

    @Test
    public void arrayMultipleIntegerElements(){
        int[] array = {7, 9};
        JsonNode a = new JsonUtil.JsonBuilder().add("array", array)
                .toJson();
        assertTrue(a.get("array").isArray());
        assertEquals(2, a.get("array").size());
        assertEquals(7, a.get("array").get(0).intValue());
        assertEquals(9, a.get("array").get(1).intValue());
    }

    @Test
    public void arrayStringMultipleElements(){
        String[] array = {"foo", "bar"};
        JsonNode a = new JsonUtil.JsonBuilder().add("array", array)
                .toJson();
        assertTrue(a.get("array").isArray());
        assertEquals(2, a.get("array").size());
        assertEquals("foo", a.get("array").get(0).textValue());
        assertEquals("bar", a.get("array").get(1).textValue());
    }

    @Test
    public void arrayEmptyElements(){
        JsonNode a = new JsonUtil.JsonBuilder().add("array", new String[0])
                .toJson();
        assertTrue(a.get("array").isArray());
        assertEquals(0, a.get("array").size());

    }


    @Test
    public void nested(){
        /*
        "names": [{
		"uuid": "26c09e1c-b1f4-4777-b67d-c405900a0f92",
		"created": 1456419675628,
		"createdBy": "AUTO_IMPORTER",
		"lastEdited": 1456419676224,
		"lastEditedBy": "AUTO_IMPORTER",
		"deprecated": false,
		"name": "ASPIRIN CALCIUM",
		"type": "of",
		"nameOrgs": [],
		"preferred": true,
		"displayName": true,
		"domains": [],
		"languages": ["en"],
		"nameJurisdiction": [],
		"references": ["8b804897-28d3-40c0-a44a-a3a5bece4662"],
		"access": [],
		"_self": "http://localhost:9001/ginas/app/api/v1/names(26c09e1c-b1f4-4777-b67d-c405900a0f92)?view=full"
	},
         */
        JsonNode a = new JsonUtil.JsonBuilder()
                .add("names", new JsonUtil.JsonBuilder()
                                    .add("uuid","26c09e1c-b1f4-4777-b67d-c405900a0f92" )
                                    .add("displayName", true))
                .add("type", "type2")
                .toJson();


        assertEquals("26c09e1c-b1f4-4777-b67d-c405900a0f92", a.get("names").get("uuid").textValue());
    }
    private void print(JsonNode n){
        print(n, 0);
    }
    private void print(JsonNode n, int depth){
        Iterator <Map.Entry<String, JsonNode>> iter = n.fields();
       char[] array = new char[depth*4];
        Arrays.fill(array, ' ');
        String indentation = new String(array);
        while(iter.hasNext()){
            Map.Entry<String, JsonNode> element = iter.next();
            System.out.println(indentation + element.getKey());
            print(element.getValue(), depth+1);

        }
    }

    @Test
    public void sameJSonHasNoChanges() throws IOException {

        JsonNode a = new JsonUtil.JsonBuilder().add("name", "myname2")
                                        .add("type", "type2")
                                        .toJson();

        Changes changes = JsonUtil.getDestructiveChanges(a, a);

        assertTrue(changes.isEmpty());
    }


    @Test
    public void singleRemoval() throws IOException {

        JsonNode a =  new JsonUtil.JsonBuilder().add("name", "myname2")
                                        .add("type", "type2")
                                        .toJson();
        JsonNode b =  new JsonUtil.JsonBuilder()
                                .add("type", "type2")
                                .toJson();



        Changes changes = JsonUtil.getDestructiveChanges(a, b);

        Map<String, Change> expected = new HashMap<>();

        JsonNode name = a.path("name");
        //value is full path so has leading slash for root
        expected.put("/name", new Change("/name", name.asText(), null, Change.ChangeType.REMOVED));

        assertEquals(new Changes(expected), changes);
    }

    @Test
    public void singleAddition() throws IOException {

        JsonNode a =  new JsonUtil.JsonBuilder().add("name", "myname2")
                .add("type", "type2")
                .toJson();
        JsonNode b =  new JsonUtil.JsonBuilder()
                .add("type", "type2")
                .toJson();



        Changes changes = JsonUtil.getDestructiveChanges(b, a);

        Map<String, Change> expected = new HashMap<>();

        String name = a.path("name").asText();
        //value is full path so has leading slash for root
        expected.put("/name", new Change("/name",null, name, Change.ChangeType.ADDED));

        assertEquals(new Changes(expected), changes);
    }

    @Test
    public void filteredOutChange(){
        JsonNode a =  new JsonUtil.JsonBuilder().add("name", "myname2")
                .add("type", "type2")
                .toJson();
        JsonNode b =  new JsonUtil.JsonBuilder()
                .add("type", "type2")
                .toJson();

        ChangeFilter filter = ChangeFilters.keyMatches("name");

        Changes changes = JsonUtil.getDestructiveChanges(b, a, filter);

        assertTrue(changes.isEmpty());
    }



    @Test
    public void getChanges() throws IOException {

        // TP:Sorry for direct file access. It seems the resource as stream
        // pieces are coming up as either empty or null.
        // The direct file path reference pieces are used elsewhere for the same
        // reason. Be nice to clean up, but this passes the tests.
        File b4f = new File("test/util/json/OLD_JSON.js");
        File aft = new File("test/util/json/New_JSON.js");
        try (InputStream inBefore = new FileInputStream(b4f);
             InputStream inAfter = new FileInputStream(aft);
        ) {


            JsonNode before = mapper.readTree(inBefore);
            JsonNode after = mapper.readTree(inAfter);

            ChangeFilter createdFilter = ChangeFilters.keyMatches("created");
            ChangeFilter lastEditedFilter = ChangeFilters.keyMatches("lastEdited");
            ChangeFilter selfFilter = ChangeFilters.keyMatches("_self");
            ChangeFilter uuidFilter = ChangeFilters.keyMatches("uuid");
            ChangeFilter blankOrNullFilter = ChangeFilters.nullOrBlankValues();

            Changes changes = JsonUtil.getDestructiveChanges(before, after,
                    createdFilter, lastEditedFilter, selfFilter, blankOrNullFilter,uuidFilter,
                    ChangeFilters.filterOutType(Change.ChangeType.REPLACED));

            Changes expected = new ChangesBuilder(before, after)

                    .removed("/names/6/references/1")

                    .added("/names/0/uuid")
                    .added("/names/1/uuid")
                    .added("/names/2/uuid")
                    .added("/names/3/uuid")
                    .added("/names/4/uuid")
                    .added("/names/5/uuid")
                    .added("/names/6/uuid")

                    .build();




            assertChangesEqual(expected, changes);
        }
    }



    private void assertChangesEqual(Changes expected, Changes actual){

        if(!expected.equals(actual)){
            throw new AssertionError(expected.diff(actual));

        }
    }






}
