package de.uka.ilkd.key.dl.regressiontest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.uka.ilkd.key.dl.regressiontest.issues.issue0001.Issue0001Test;
import de.uka.ilkd.key.dl.regressiontest.issues.issue0021.Issue0021Test;
import de.uka.ilkd.key.dl.regressiontest.issues.issue0023.Issue0023Test;
import de.uka.ilkd.key.dl.regressiontest.issues.issue0027.Issue0027Test;
import de.uka.ilkd.key.dl.regressiontest.issues.issue0028.Issue0028Test;
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
	Issue0001Test.class,
	Issue0021Test.class,
	Issue0023Test.class,
	Issue0027Test.class,
	Issue0028Test.class
})
public class RegressionTestSuite {
	// the class remains empty,
	// used only as a holder for the above annotations
}
