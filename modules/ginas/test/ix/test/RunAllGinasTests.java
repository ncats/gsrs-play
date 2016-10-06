package ix.test;

import org.junit.runner.RunWith;

/**
 * Example suite
 */
//@RunWith(FindAllTestsRunner.class)
@RunWith(GinasTestSuite.class)
@GinasTestSuite.Config(fullExcludePattern = {"ix\\.ntd\\..+"})
public class RunAllGinasTests {


}