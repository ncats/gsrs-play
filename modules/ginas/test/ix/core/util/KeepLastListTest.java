package ix.core.util;

import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
public class KeepLastListTest {

    @Test
    public void emptyList(){
        KeepLastList<String> sut = new KeepLastList<>(5);
        assertTrue(sut.isEmpty());
    }

    @Test
    public void addAFew(){
        KeepLastList<String> sut = new KeepLastList<>(5);

        sut.add("one");
        sut.add("two");
        sut.add("three");

        assertEquals(Arrays.asList("one", "two", "three"), sut);
    }

    @Test
    public void addMaxAmount(){
        KeepLastList<String> sut = new KeepLastList<>(5);

        sut.add("one");
        sut.add("two");
        sut.add("three");
        sut.add("four");
        sut.add("five");


        assertEquals(Arrays.asList("one", "two", "three", "four", "five"), sut);
    }
    @Test
    public void overMaxShouldLoseHeadOfList(){
        KeepLastList<String> sut = new KeepLastList<>(5);

        sut.add("one");
        sut.add("two");
        sut.add("three");
        sut.add("four");
        sut.add("five");

        sut.add("six");
        sut.add("seven");


        assertEquals(Arrays.asList("three", "four", "five", "six", "seven"), sut);
    }

    @Test
    public void stream(){
        KeepLastList<String> sut = new KeepLastList<>(5);

        sut.add("one");
        sut.add("two");
        sut.add("three");
        sut.add("four");
        sut.add("five");

        sut.add("six");
        sut.add("seven");


        assertEquals(Arrays.asList("three", "four", "five", "six", "seven"), sut.stream().collect(Collectors.toList()));
    }

    @Test
    public void iterator(){
        KeepLastList<String> sut = new KeepLastList<>(5);

        sut.add("one");
        sut.add("two");
        sut.add("three");
        sut.add("four");
        sut.add("five");

        sut.add("six");
        sut.add("seven");


        Iterator<String> iter = sut.iterator();

        Iterator<String> expected = Arrays.asList("three", "four", "five", "six", "seven").iterator();
        while(expected.hasNext()) {
            assertTrue(iter.hasNext());
            assertEquals(expected.next(), iter.next());
        }
        assertFalse(iter.hasNext());



    }

    @Test
    public void listIterator(){
        KeepLastList<String> sut = new KeepLastList<>(5);

        sut.add("one");
        sut.add("two");
        sut.add("three");
        sut.add("four");
        sut.add("five");

        sut.add("six");
        sut.add("seven");

        ListIterator<String> iter = sut.listIterator();

        assertEquals("three", iter.next());
        assertEquals("four", iter.next());
        assertEquals("five", iter.next());
        assertEquals("five", iter.previous());
        assertEquals("four", iter.previous());
        assertEquals("three", iter.previous());
    }

    @Test
    public void listIteratorSet(){
        KeepLastList<String> sut = new KeepLastList<>(5);

        sut.add("one");
        sut.add("two");
        sut.add("three");
        sut.add("four");
        sut.add("five");

        sut.add("six");
        sut.add("seven");

        ListIterator<String> iter = sut.listIterator();

        assertEquals("three", iter.next());
        assertEquals("four", iter.next());
        assertEquals("five", iter.next());
        assertEquals("five", iter.previous());
        assertEquals("four", iter.previous());
        assertEquals("three", iter.previous());
        iter.set("newValue");
        assertEquals(Arrays.asList("newValue", "four", "five", "six", "seven"), sut);

    }
    @Test
    public void listIteratorAdd(){
        KeepLastList<String> sut = new KeepLastList<>(5);

        sut.add("one");
        sut.add("two");
        sut.add("three");
        sut.add("four");
        sut.add("five");

        sut.add("six");
        sut.add("seven");

        ListIterator<String> iter = sut.listIterator();

        assertEquals("three", iter.next());
        assertEquals("four", iter.next());
        assertEquals("five", iter.next());

        iter.add("newValue");
        assertEquals("newValue", iter.previous());
        assertEquals("newValue", iter.next());
        assertEquals("six", iter.next());
        assertEquals(Arrays.asList("four", "five", "newValue", "six", "seven"), sut);

    }
}