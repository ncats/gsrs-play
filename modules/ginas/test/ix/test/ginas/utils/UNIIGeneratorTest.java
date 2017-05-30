package ix.test.ginas.utils;

import ix.AbstractGinasServerTest;
import ix.ginas.utils.UNIIGenerator;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by katzelda on 5/30/17.
 */
public class UNIIGeneratorTest extends AbstractGinasServerTest{

    UNIIGenerator sut = new UNIIGenerator();

    @Test
    public void testUniiWith4ConsecutiveLetters(){
        assertFalse(sut.allowID("ABCD10A80V"));

        assertFalse(sut.allowID("7ABCD0A80V"));
        assertFalse(sut.allowID("7LN1ABCD0V"));
        assertFalse(sut.allowID("7LN1X0ABCD"));
    }

    @Test
    public void validUnii(){
        assertTrue(sut.allowID("7LN1X0A80V")); //that's a one not an L
    }

    @Test
    public void invalidUniiWithLeetDirtyWord(){
        assertFalse(sut.allowID("75h1tA80V"));
    }

    @Test
    public void invalidUniiWithtDirtyWord(){
        assertFalse(sut.allowID("7SHITA80V"));
    }
}
