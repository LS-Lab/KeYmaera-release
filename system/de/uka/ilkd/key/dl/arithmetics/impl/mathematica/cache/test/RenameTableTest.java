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
