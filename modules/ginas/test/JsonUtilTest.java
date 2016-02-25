import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import play.libs.Json;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import static org.junit.Assert.*;
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
    public void sameJSonHasNoChanges() throws IOException {

        JsonNode a = new JsonUtil.JsonBuilder().add("name", "myname2")
                                        .add("type", "type2")
                                        .toJson();
                //mapper.readTree("\"name\":\"myname2\", \"type\":\"type2\" }");

        Map<String, JsonUtil.Change> changes = JsonUtil.getDestructiveChanges(a, a);

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



        Map<String, JsonUtil.Change> changes = JsonUtil.getDestructiveChanges(a, b);

        Map<String, JsonUtil.Change> expected = new HashMap<>();

        JsonNode name = a.path("name");
        //value is full path so has leading slash for root
        expected.put("/name", new JsonUtil.Change(name, JsonUtil.ChangeType.REMOVED));

        assertEquals(expected, changes);
    }

    @Test
    public void singleAddition() throws IOException {

        JsonNode a =  new JsonUtil.JsonBuilder().add("name", "myname2")
                .add("type", "type2")
                .toJson();
        JsonNode b =  new JsonUtil.JsonBuilder()
                .add("type", "type2")
                .toJson();



        Map<String, JsonUtil.Change> changes = JsonUtil.getDestructiveChanges(b, a);

        Map<String, JsonUtil.Change> expected = new HashMap<>();

        JsonNode name = a.path("name");
        //value is full path so has leading slash for root
        expected.put("/name", new JsonUtil.Change(name, JsonUtil.ChangeType.ADDED));

        assertEquals(expected, changes);
    }

    private static void assertThatNonDestructive(JsonNode before, JsonNode after){
        SubstancePostUpdateTest.assertThatNonDestructive(before, after);
    }






}
