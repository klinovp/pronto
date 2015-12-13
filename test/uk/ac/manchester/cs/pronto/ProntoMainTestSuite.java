/**
 *
 */

package uk.ac.manchester.cs.pronto;

import uk.ac.manchester.cs.pronto.index.SimpleIndexSetImplTest;
import uk.ac.manchester.cs.pronto.lp.CCSetAnalyzerTest;
import uk.ac.manchester.cs.pronto.zpartition.ZPartitionerCGImplTest;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Pavel Klinov
 *
 *         Collection of the most essential test cases
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
		                    SimpleIndexSetImplTest.class,
		                    CCSetAnalyzerTest.class,
		                    PSATSolverImplTest.class,
		                    ZPartitionerCGImplTest.class,
		                    HSOptimizedLexReasonerTest.class
})
public class ProntoMainTestSuite {

	public static final String BASE = "test_data/";

	public static final String URI_PREFIX = "file:test_prefix#";

	public static void main(String args[]) {
		JUnitCore.main(ProntoMainTestSuite.class.getName());
	}

}
