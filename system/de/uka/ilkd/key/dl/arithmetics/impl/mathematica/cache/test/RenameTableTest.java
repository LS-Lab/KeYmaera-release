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

import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.cache.RenameTable;
import junit.framework.TestCase;

/**
 * Tests the functionality of the RenameTable-Class.
 * 
 * @author Timo Michelsen
 */
public class RenameTableTest extends TestCase {

    public void test_reverse() {
        RenameTable table = new RenameTable();
        
        table.put("x", "y");
        table.put("a", "b");
        
        assertEquals( table.get("x"), "y");
        assertEquals( table.get("a"), "b");
        
        table.reverse();
        
        assertEquals( table.get("y"), "x");
        assertEquals( table.get("b"), "a");
        
        table.reverse();
        
        assertEquals( table.get("x"), "y");
        assertEquals( table.get("a"), "b");
    }
    
}
