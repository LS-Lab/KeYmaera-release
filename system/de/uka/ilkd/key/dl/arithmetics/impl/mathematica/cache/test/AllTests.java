/*******************************************************************************
 * Copyright (c) 2012 Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jan-David Quesel - initial API and implementation
 ******************************************************************************/
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
