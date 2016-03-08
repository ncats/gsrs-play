package app.ix.utils.pojopatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ix.core.models.Author;
import ix.ginas.models.v1.Parameter;
import ix.ginas.models.v1.Property;
import ix.utils.pojopatch.PojoDiff;
import ix.utils.pojopatch.PojoPatch;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Created by katzelda on 3/7/16.
 */
public class TestPojoDiff {

    ObjectMapper mapper = new ObjectMapper();

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
        p1.name = "foo";
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
        p1.name = "foo";

        Parameter p2 = new Parameter();
        p2.name = "bar";

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
        assertEquals(p2.name, updatedParams.get(0).name);


        JsonMatches(update, prop);

    }

    @Test
    public void clearList() throws Exception {

        List<Parameter> originalParams = new ArrayList<>();
        Parameter p1 = new Parameter();
        p1.name = "foo";

        Parameter p2 = new Parameter();
        p2.name = "bar";

        originalParams.add(p1);
        originalParams.add(p2);

        Property prop = new Property();

        prop.setParameters(originalParams);


        Property update = new Property();

        update.setParameters(new ArrayList<Parameter>());

        PojoPatch<Property> patch = PojoDiff.getDiff(prop, update);

        patch.apply(prop);

        assertTrue(prop.getParameters().isEmpty());

    }
}
