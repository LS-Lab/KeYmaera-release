package de.uka.ilkd.key.dl.regressiontest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.uka.ilkd.key.dl.regressiontest.issues.issue0001.Issue0001Test;

/**
 * Regression test suite.
 * @author smitsch
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
  Issue0001Test.class
})
public class RegressionTestSuite {
	// the class remains empty,
	// used only as a holder for the above annotations
}
