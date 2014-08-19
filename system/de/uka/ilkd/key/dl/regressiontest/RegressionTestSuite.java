package de.uka.ilkd.key.dl.regressiontest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.uka.ilkd.key.dl.regressiontest.issues.issue0001.Issue0001Test;
import de.uka.ilkd.key.dl.regressiontest.proofexamples.AutomatedProvableTest;
import de.uka.ilkd.key.dl.regressiontest.proofexamples.LicsTutorialTest;
import de.uka.ilkd.key.dl.regressiontest.proofexamples.NotProvableTest;

/**
 * Regression test suite.
 * @author smitsch
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	NotProvableTest.class,
	AutomatedProvableTest.class,
	LicsTutorialTest.class,
	Issue0001Test.class
})
public class RegressionTestSuite {
	// the class remains empty,
	// used only as a holder for the above annotations
}
