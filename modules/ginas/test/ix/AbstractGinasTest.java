package ix;

import ix.core.util.ExpectFailureChecker;
import ix.core.util.RunOnlyTestRunner;
import org.junit.Rule;

import ix.test.util.TestNamePrinter;
import org.junit.runner.RunWith;

/**
 * Abstract test class for doing common operations
 * found in all other tests for GSRS. Extending this class
 * will produce additional output for what tests are being 
 * run.
 * @author peryeata
 *
 */
@RunWith(RunOnlyTestRunner.class)
public abstract class AbstractGinasTest {
	@Rule
	public TestNamePrinter printer = new TestNamePrinter();

	@Rule
	public ExpectFailureChecker expectedToFailChecker = new ExpectFailureChecker();
	
}
