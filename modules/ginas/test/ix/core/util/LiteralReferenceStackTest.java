package ix.core.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import ix.test.AbstractGinasTest;
import ix.utils.LinkedReferenceSet;

public class LiteralReferenceStackTest extends AbstractGinasTest{
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
	public void pushingLiteralReferenceOnStackWillMakeItContainLiteralReference(){
		LinkedReferenceSet<MockThing> refSet=new LinkedReferenceSet<>();
		
		MockThing m1 = MockThing.of(1);
		MockThing m2 = MockThing.of(1);
		AtomicBoolean called = new AtomicBoolean(false);
		refSet.pushAndPopWith(m1, ()->{
			assertTrue(refSet.contains(m1));
			assertFalse(refSet.contains(m2));
			called.set(true);
		});
		
		assertTrue(called.get());
		
		
	}
}
