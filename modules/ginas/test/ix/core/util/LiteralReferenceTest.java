package ix.core.util;

import static org.junit.Assert.*;

import org.junit.Test;

import ix.utils.LiteralReference;

public class LiteralReferenceTest {
	
	public static class MockThing{
		int i=0;
		MockThing(int i){
			this.i=i;
		}
		@Override
		public boolean equals(Object o){
			if(!(o instanceof MockThing)){
				return false;
			}
			return ((MockThing)o).i==i;
		}
		public static MockThing of(int i){
			return new MockThing(i);
		}
	}
	

	@Test
	public void sameLiteralReferenceIsEqualToItself(){
		LiteralReference<MockThing> lr = LiteralReference.of(MockThing.of(2));
		
		assertEquals(lr,lr);
	}
	@Test
	public void sameLiteralDifferentLiteralReferencesAreEqualToEachOther(){
		MockThing tst=MockThing.of(20);
		LiteralReference<MockThing> lr1 = LiteralReference.of(tst);
		LiteralReference<MockThing> lr2 = LiteralReference.of(tst);
		assertEquals(lr1.hashCode(),lr2.hashCode());
		assertEquals(lr1,lr2);
	}
	@Test
	public void differentInstancesOfEquivalentObjectsStillDifferent(){
		
		LiteralReference<MockThing> lr1 = LiteralReference.of(MockThing.of(5));
		LiteralReference<MockThing> lr2 = LiteralReference.of(MockThing.of(5));
		
		assertNotEquals(lr1,lr2);
	}
	@Test
	public void differentInstancesOfEquivalentObjectsStillSameWhenFetched(){
		
		LiteralReference<MockThing> lr1 = LiteralReference.of(MockThing.of(5));
		LiteralReference<MockThing> lr2 = LiteralReference.of(MockThing.of(5));
		
		assertEquals(lr1.get(),lr2.get());
	}
}
