package de.uka.ilkd.key.dl.regressiontest.issues.issue0028;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uka.ilkd.key.dl.regressiontest.AbstractProofRegressionTest;

/**
 * Regression test for issue #0028.
 * Issue: Order of quantified variables in calls to Reduce
 * 
 * @author smitsch
 */
@RunWith(Parameterized.class)
public class Issue0028Test extends AbstractProofRegressionTest {
	
	/**
	 * Provides the test parameters (one file at a time).
	 * @return The files under test.
	 */
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { 
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0028/vars_order_UnitTest", 0 },
				{ "proofExamples/hybrid/dev/issues_keymaera/issue_0028/QE_var_order_UnitTest", 0 }
		});
	}
	
	/**
	 * Initializes a new test with the specified file under test.
	 * @param fileName The file under test.
	 */
	public Issue0028Test(String fileName, int expected) {
		super(fileName, expected);
	}
	
	/**
	 * The test method. Called once per entry in the static
	 * data() parameter provider. Overridden to provide a different 
	 * timeout parameter.
	 * @throws Exception If the KeYmaera child process throws an exception 
	 */
	@Test(timeout=60000)
	@Override
	public void test() throws Exception {
		super.test();
	}

}
