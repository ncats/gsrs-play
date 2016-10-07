package ix;

import org.junit.Rule;

import ix.test.util.TestNamePrinter;

/**
 * Abstract test class for doing common operations
 * found in all other tests for GSRS. Extending this class
 * will produce additional output for what tests are being 
 * run.
 * @author peryeata
 *
 */
public abstract class AbstractGinasTest {
	@Rule
	public TestNamePrinter printer = new TestNamePrinter();
	
}
