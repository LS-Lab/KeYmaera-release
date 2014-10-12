package de.uka.ilkd.key.dl.regressiontest.issues.issue0023;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uka.ilkd.key.dl.regressiontest.AbstractProofRegressionTest;

/**
 * Regression test for issue #0023.
 * Issue: Missing heuristics for automated application of special function rules.
 * 
 * @author smitsch
 */
@RunWith(Parameterized.class)
public class Issue0023Test extends AbstractProofRegressionTest {
	
	/**
	 * Provides the test parameters (one file at a time).
	 * @return The files under test.
	 */
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { 
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0023/abs_1.key", 0 },
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0023/abs_2.key", 0 },
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0023/abs_3.key", 0 },
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0023/abs_4.key", 0 },
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0023/abs_5.key", 0 },
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0023/min_1.key", 0 },
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0023/min_2.key", 0 },
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0023/min_3.key", 0 },
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0023/min_4.key", 0 },
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0023/min_5.key", 0 },
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0023/max_1.key", 0 },
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0023/max_2.key", 0 },
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0023/max_3.key", 0 },
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0023/max_4.key", 0 },
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0023/max_5.key", 0 }
		});
	}
	
	/**
	 * Initializes a new test with the specified file under test.
	 * @param fileName The file under test.
	 */
	public Issue0023Test(String fileName, int expected) {
		super(fileName, expected);
	}

}
