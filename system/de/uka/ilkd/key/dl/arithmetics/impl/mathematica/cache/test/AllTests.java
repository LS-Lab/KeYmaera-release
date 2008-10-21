package de.uka.ilkd.key.dl.arithmetics.impl.mathematica.cache.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Central class to invoke all other test-cases
 * 
 * @author Timo Michelsen
 */
public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for de.uka.ilkd.key.dl.arithmetics.impl.mathematica.cache.test");
        //$JUnit-BEGIN$
        suite.addTestSuite(ExprRenamerTest.class);
        suite.addTestSuite(RenameTableTest.class);
        suite.addTestSuite(CacherTest.class);
        //$JUnit-END$
        return suite;
    }

}
