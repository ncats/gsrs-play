package ix.core.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import ix.AbstractGinasTest;
import ix.utils.Tuple;
import ix.utils.Tuple.ComparatorNotImplementedException;



public class TupleTest extends AbstractGinasTest{



    @Test
    public void ensureTupleSwapTwiceIsTheSameAsNoSWap(){
        Tuple<String,String> tup = Tuple.of("one","two");

        assertEquals(tup.v(), tup.swap().k());
        assertEquals(tup.k(), tup.swap().v());
        assertEquals(tup.v(), tup.swap().swap().v());
        assertEquals(tup.k(), tup.swap().swap().k());

    }
    @Test
    public void ensureTupleWithKEqualityWorks(){
        Tuple<String,String> tup1 = Tuple.of("one","two").withKEquality();
        Tuple<String,String> tup2 = Tuple.of("one","three").withKEquality();
        Tuple<String,String> tup3 = Tuple.of("two","three").withKEquality();

        assertEquals(tup1, tup2);
        Assert.assertNotEquals(tup1, tup3);
    }

    @Test
    public void ensureTupleWithVEqualityWorks(){
        Tuple<String,String> tup1 = Tuple.of("one","two").swap().withVEquality();
        Tuple<String,String> tup2 = Tuple.of("one","three").swap().withVEquality();
        Tuple<String,String> tup3 = Tuple.of("two","three").swap().withVEquality();

        assertEquals(tup1, tup2);
        Assert.assertNotEquals(tup1, tup3);
    }

    @Test
    public void ensureTuplesSortedByKWithExplicitComparatorWorks(){
        List<Integer> expected = IntStream.range(0,10)
                .mapToObj(i->i)
                .collect(Collectors.toList());
        List<Integer> mod = new ArrayList<Integer>(expected);

        Collections.shuffle(mod);
        List<Integer> got = mod.stream()
                .map(i->Tuple.of(i,"Whatever"))
                .map(t->t.withKSortOrder((i1,i2)->{
                    return i1-i2;
                }))
                .sorted()
                .map(t->t.k())
                .collect(Collectors.toList());

        assertEquals(expected,got);

    }

    @Test
    public void ensureTuplesSortedByKWithExplicitComparatorReverseWorks(){
        List<Integer> expected = IntStream.range(0,10)
                .mapToObj(i->i)
                .collect(Collectors.toList());
        List<Integer> mod = new ArrayList<Integer>(expected);

        Collections.shuffle(mod);
        List<Integer> got = mod.stream()
                .map(i->Tuple.of(i,"Whatever"))
                .map(t->t.withKSortOrder((i1,i2)->{
                    return i2-i1;
                }))
                .sorted()
                .map(t->t.k())
                .collect(Collectors.toList());

        Collections.reverse(expected);

        assertEquals(expected,got);

    }

    @Test
    public void ensureTuplesSortedByKWithComparableMapWorks(){

        String[] myOrder= new String[]{"B","A","C","D","E","F","G","H","I","J"};


        List<Integer> expected = IntStream.range(0,10)
                .mapToObj(i->i)
                .collect(Collectors.toList());
        List<Integer> mod = new ArrayList<Integer>(expected);

        Collections.shuffle(mod);
        List<Integer> got = mod.stream()
                .map(i->Tuple.of(i,"Whatever"))
                .map(t->t.withKSortOrder(k->myOrder[k]))
                .sorted()
                .map(t->t.k())
                .collect(Collectors.toList());

        //swap 1 and 2 based on the set order for strings
        expected.set(0, 1);
        expected.set(1, 0);

        assertEquals(expected,got);

    }

    @Test
    public void ensureTuplesSortedByKWithImplicitComparableWorks(){
        List<Integer> expected = IntStream.range(0,10)
                .mapToObj(i->i)
                .collect(Collectors.toList());
        List<Integer> mod = new ArrayList<Integer>(expected);

        Collections.shuffle(mod);
        List<Integer> got = mod.stream()
                .map(i->Tuple.of(i,"Whatever"))
                .map(t->t.withKSortOrder())
                .sorted()
                .map(t->t.k())
                .collect(Collectors.toList());

        assertEquals(expected,got);

    }

    @Test
    public void ensureTuplesSortedByKWithImplicitComparableoNonComparableThrowsException(){
        List<Integer> expected = IntStream.range(0,10)
                .mapToObj(i->i)
                .collect(Collectors.toList());
        List<Integer> mod = new ArrayList<Integer>(expected);

        class TempClass {
            int i;
            public TempClass(int i){
                this.i=i;

            }
        };


        Collections.shuffle(mod);
        ComparatorNotImplementedException eGot = null;
        try{
            List<TempClass> got = mod.stream()
                    .map(i->Tuple.of(new TempClass(i),"Whatever"))
                    .map(t->t.withKSortOrder())
                    .sorted()
                    .map(t->t.k())
                    .collect(Collectors.toList());
        }catch(ComparatorNotImplementedException e){
            eGot=e;
        }

        Assert.assertNotNull("Should not be able to implicitly sort on non-sortable k-value in tuple", eGot);

    }




}
