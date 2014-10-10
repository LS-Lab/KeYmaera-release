package de.uka.ilkd.key.dl.regressiontest.issues.issue0021;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uka.ilkd.key.dl.regressiontest.AbstractProofRegressionTest;

/**
 * Regression test for issue #0021.
 * Issue: DRI Conjunction rule
 * 
 * @author smitsch
 */
@RunWith(Parameterized.class)
public class Issue0021Test extends AbstractProofRegressionTest {
	
	/**
	 * Provides the test parameters (one file at a time).
	 * @return The files under test.
	 */
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { 
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0021/driconj.key.proof", 0 } 
		});
	}
	
	/**
	 * Initializes a new test with the specified file under test.
	 * @param fileName The file under test.
	 */
	public Issue0021Test(String fileName, int expected) {
		super(fileName, expected);
	}

}
