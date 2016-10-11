package ix.core.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import ix.AbstractGinasTest;
import ix.utils.Util;

public class ComparatorMakerTest extends AbstractGinasTest{

	@Test
	public void suppliedOrderOfElementsShouldBeSortedAccordingly(){
		List<String> mylist = new ArrayList<>();
		for(int i=0;i<100;i++){
			mylist.add("Test string" + i);
		}
		Collections.shuffle(mylist);
		List<String> prefOrder = new ArrayList<>(mylist);
		Comparator<String> comp=Util.comparitor(prefOrder.stream());
		Collections.shuffle(mylist);
		assertNotEquals(prefOrder,mylist);
		mylist.sort(comp);
		assertEquals(prefOrder,mylist);
	}
	
	@Test
	public void suppliedOrderOfElementsWithNamerShouldBeSortedAccordingly(){
		List<MyClass> mylist = new ArrayList<>();
		for(int i=0;i<100;i++){
			mylist.add(new MyClass("Test string" + i));
		}
		Collections.shuffle(mylist);
		List<MyClass> prefOrder = new ArrayList<>(mylist);
		Comparator<MyClass> comp=Util.comparitor((m)->m.getId(),prefOrder.stream().map(m->m.getId()));
		Collections.shuffle(mylist);
		assertNotEquals(prefOrder,mylist);
		mylist.sort(comp);
		assertEquals(prefOrder,mylist);
	}
	
	public static class MyClass{
		public String id;
		public MyClass(String id){
			this.id=id;
		}
		public String getId(){
			return id;
		}
	}
}
