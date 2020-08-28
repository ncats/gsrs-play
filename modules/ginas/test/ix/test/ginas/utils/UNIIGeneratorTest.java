package ix.test.ginas.utils;

import ix.AbstractGinasServerTest;
import ix.ginas.utils.GinasUtils;
import ix.ginas.utils.UNIIGenerator;
import org.junit.Test;

import static ix.test.util.TestUtil.addUniiCheckDigit;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by katzelda on 5/30/17.
 */
public class UNIIGeneratorTest extends AbstractGinasServerTest{

    UNIIGenerator sut = new UNIIGenerator();



    @Test
    public void testUniiWith4ConsecutiveLetters(){
        assertFalse(sut.allowID(addUniiCheckDigit("ABCD12A81") ));

        assertFalse(sut.allowID(addUniiCheckDigit("7ABCD2A81") ));
        assertFalse(sut.allowID(addUniiCheckDigit("7LN2ABCD2") ));
        assertFalse(sut.allowID(addUniiCheckDigit("7LN2X7ABCD") ));
    }

    @Test
    public void onesAndIsNotOKAnymore(){
        assertFalse(sut.isValidId(addUniiCheckDigit("7LNIX2A82"))); //that's a one not an I
        assertTrue(GinasUtils.isUnii(addUniiCheckDigit("7LNIX2A82"))); //if it's a legacy UNII it should still be OK

        assertFalse(sut.isValidId(addUniiCheckDigit("7LNIX2A82"))); //that's an I not an one
        assertTrue(GinasUtils.isUnii(addUniiCheckDigit("7LNIX2A82"))); //if it's a legacy UNII it should still be OK
    }

    @Test
    public void zerosAndOsNotOKAnymore(){
        assertFalse(sut.isValidId(addUniiCheckDigit("7LN2X0A80"))); //that's a zero not an O
        assertTrue(GinasUtils.isUnii(addUniiCheckDigit("7LN2X0A80"))); //if it's a legacy UNII it should still be OK

        assertFalse(sut.isValidId(addUniiCheckDigit("7LN2XOA8O"))); //that's an O not an zero
        assertTrue(GinasUtils.isUnii(addUniiCheckDigit("7LN2XOA8O"))); //if it's a legacy UNII it should still be OK
    }

    @Test
    public void invalidUniiWrongCheckDigit(){
        assertFalse(GinasUtils.isUnii("7LN1X0A80Q"));
        assertFalse(sut.isValidId("7LN1X0A80Q")); //that's a one not an L

    }

    @Test
    public void invalidUniiWithLeetDirtyWord(){
        assertFalse(sut.allowID("75h1tA80V"));
    }

    @Test
    public void invalidUniiWithtDirtyWord(){
        assertFalse(sut.allowID("7SHITA80V"));
    }

    @Test
    public void invalidUniiWithtMonth(){
        for(String m : Arrays.asList("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")){
            String unii = addUniiCheckDigit(m+"272222");
            assertFalse(sut.allowID(unii ));
        }

    }

    @Test
    public void numberConfusedWithSciNotation(){
        //make all numbers with a check digit
        //this should be 2234567E29
        String unii = addUniiCheckDigit("2234567E2");
        assertTrue(Character.isDigit(unii.charAt(unii.length() -1)));
        assertTrue(GinasUtils.isUnii(unii));
        assertFalse(sut.allowID(unii ));

    }


}
