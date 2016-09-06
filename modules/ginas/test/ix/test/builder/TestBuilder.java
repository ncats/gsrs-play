package ix.test.builder;

import ix.ginas.models.v1.Substance;
import org.junit.Test;

import static org.junit.Assert.*;
/**
 * Created by katzelda on 9/6/16.
 */
public class TestBuilder {

    @Test
    public void setName(){
        Substance substance = new SubstanceBuilder()
                                    .addName("foo")
                                    .build();

        assertEquals("foo", substance.getName());
    }
}
