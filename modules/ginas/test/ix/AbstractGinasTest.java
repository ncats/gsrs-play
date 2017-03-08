package ix;

import ix.core.util.ExpectFailureChecker;
import ix.core.util.RunOnlyTestRunner;
import ix.test.util.BrowserErrorPrinter;
import org.junit.Rule;

import ix.test.util.TestNamePrinter;
import org.junit.runner.RunWith;
import org.junit.rules.ExpectedException;

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
	public BrowserErrorPrinter failingHttpStatusPrinter = new BrowserErrorPrinter();
	@Rule
	public TestNamePrinter printer = new TestNamePrinter();

	@Rule
	public ExpectFailureChecker expectedToFailChecker = new ExpectFailureChecker();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

}
