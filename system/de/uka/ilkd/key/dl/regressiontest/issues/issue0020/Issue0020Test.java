package de.uka.ilkd.key.dl.regressiontest.issues.issue0020;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uka.ilkd.key.dl.regressiontest.AbstractProofRegressionTest;

/**
 * Regression test for issue #0020.
 * Issue: DRI on formulas without \assumes(f=g ==>)
 * 
 * @author smitsch
 */
@RunWith(Parameterized.class)
public class Issue0020Test extends AbstractProofRegressionTest {
	
	/**
	 * Provides the test parameters (one file at a time).
	 * @return The files under test.
	 */
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { 
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0020/dribis.key.proof", 0 } 
		});
	}
	
	/**
	 * Initializes a new test with the specified file under test.
	 * @param fileName The file under test.
	 */
	public Issue0020Test(String fileName, int expected) {
		super(fileName, expected);
	}

}
