package ix.test;

import org.junit.Test;

import ix.AbstractGinasTest;

import java.util.Arrays;

import static org.junit.Assert.*;
/**
 * Created by katzelda on 5/27/16.
 */
public class ArgParserTest extends AbstractGinasTest {

    @Test
    public void singleArgumentNoWhitespace(){
        assertEquals(Arrays.asList("foo"), ArgParser.parseArgs("foo"));
    }

    @Test
    public void twoParamsNoQuotes(){
        assertEquals(Arrays.asList("foo", "bar"), ArgParser.parseArgs("foo bar"));
    }
    @Test
    public void threeParamsNoQuotes(){
        assertEquals(Arrays.asList("foo", "bar", "baz"), ArgParser.parseArgs("foo bar baz"));
    }

    @Test
    public void singleArgumentQuoted(){
        assertEquals(Arrays.asList("foo"), ArgParser.parseArgs("\"foo\""));
    }
    @Test
    public void twoWordArgumentQuoted(){
        assertEquals(Arrays.asList("foo bar"), ArgParser.parseArgs("\"foo bar\""));
    }
    @Test
    public void jvmproperty(){
        assertEquals(Arrays.asList("-Dtestconfig=ginas.conf"), ArgParser.parseArgs("-Dtestconfig=ginas.conf"));
    }
}
