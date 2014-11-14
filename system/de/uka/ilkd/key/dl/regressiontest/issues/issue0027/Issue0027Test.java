package de.uka.ilkd.key.dl.regressiontest.issues.issue0027;

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
public class Issue0027Test extends AbstractProofRegressionTest {
	
	/**
	 * Provides the test parameters (one file at a time).
	 * @return The files under test.
	 */
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { 
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0027/DlwQ.key", 0 },
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0027/DlwQ.key.proof", 1 }
		});
	}
	
	/**
	 * Initializes a new test with the specified file under test.
	 * @param fileName The file under test.
	 */
	public Issue0027Test(String fileName, int expected) {
		super(fileName, expected);
	}

}
