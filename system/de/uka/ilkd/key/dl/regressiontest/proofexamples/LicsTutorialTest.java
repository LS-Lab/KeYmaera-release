package de.uka.ilkd.key.dl.regressiontest.proofexamples;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uka.ilkd.key.dl.regressiontest.AbstractProofRegressionTest;

/**
 * Regression test for LICS tutorials.
 * 
 * @author smitsch
 */
@RunWith(Parameterized.class)
public class LicsTutorialTest extends AbstractProofRegressionTest {
	
	/**
	 * Provides the test parameters (one file at a time).
	 * @return The files under test.
	 */
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { 
				{ "proofExamples/hybrid/tutorial/lics1-continuous-forward.key.mathematica.proof", 0 },
//				{ "proofExamples/hybrid/tutorial/lics1-continuous-forward.key.redlog.proof" },
//				{ "proofExamples/hybrid/tutorial/lics1-continuous-forward.key.smt.proof" },
				{ "proofExamples/hybrid/tutorial/lics2-hybrid-forward.key.mathematica.proof", 0 },
//				{ "proofExamples/hybrid/tutorial/lics2-hybrid-forward.key.redlog.proof" },
//				{ "proofExamples/hybrid/tutorial/lics2-hybrid-forward.key.smt.proof" },
				{ "proofExamples/hybrid/tutorial/lics3a-event-forward.key.mathematica.proof", 0 },
				{ "proofExamples/hybrid/tutorial/lics4a-time-safe.key.mathematica.proof", 0 },
				{ "proofExamples/hybrid/tutorial/lics4c-time-safe-relative.key.mathematica.proof", 0 },
				{ "proofExamples/hybrid/tutorial/lics5-controllability-equivalence.key.mathematica.proof", 0 },
				{ "proofExamples/hybrid/tutorial/lics6-MPC-acceleration-equivalence.key.mathematica.proof", 0 },
				{ "proofExamples/hybrid/tutorial/lics7-MPC.key.proof", 1 }
		});
	}
	
	/**
	 * Initializes a new test with the specified file under test.
	 * @param fileName The file under test.
	 */
	public LicsTutorialTest(String fileName, int expected) {
		super(fileName, expected);
	}

}
