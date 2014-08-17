package de.uka.ilkd.key.dl.regressiontest.issues.issue0001;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uka.ilkd.key.dl.regressiontest.AbstractProofRegressionTest;

/**
 * Regression test for issue #0001.
 * Issue: QE with universal closure and saved reducevariables does not reload correctly
 * 
 * @author smitsch
 */
@RunWith(Parameterized.class)
public class Issue0001Test extends AbstractProofRegressionTest {
	
	/**
	 * Provides the test parameters (one file at a time).
	 * @return The files under test.
	 */
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { 
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0001/nodelay_204.key.proof", 0 } 
		});
	}
	
	/**
	 * Initializes a new test with the specified file under test.
	 * @param fileName The file under test.
	 */
	public Issue0001Test(String fileName, int expected) {
		super(fileName, expected);
	}

}
