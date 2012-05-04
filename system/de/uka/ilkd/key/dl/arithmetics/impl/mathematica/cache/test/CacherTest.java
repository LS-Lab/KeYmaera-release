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

import com.wolfram.jlink.Expr;

import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.Term2ExprConverter;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.IKernelLinkWrapper.ExprAndMessages;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.cache.Cacher;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.cache.ICacher;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.logic.sort.Sort;
import junit.framework.TestCase;

/**
 * Test the functionality of the cacher-class
 * 
 * @author Timo Michelsen
 */
public class CacherTest extends TestCase {
    
    // Term-Builder
    TermBuilder tb = TermBuilder.DF;

    // Logic variables
    LogicVariable x = new LogicVariable(new Name("x"), SortR.R);
    LogicVariable y = new LogicVariable(new Name("y"), SortR.R);
    LogicVariable a = new LogicVariable(new Name("a"), SortR.R);
    LogicVariable x2 = new LogicVariable(new Name("x2"), SortR.R);
    LogicVariable y2 = new LogicVariable(new Name("y2"), SortR.R);
    LogicVariable a2 = new LogicVariable(new Name("a2"), SortR.R);
    
    // Function
    RigidFunction gt = new RigidFunction(new Name("gt"), SortR.FORMULA, new Sort[] { SortR.R, SortR.R });

    // Objects to test with
    Term term = null;
    Term term2 = null;
    ICacher cacher = null;
    Expr expr = null;
    Expr expr2 = null;
    ExprAndMessages eam = null;
    
    /*
     * (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() {
        term = tb.all(x, tb.ex(y, tb.and(
                tb.func(gt, tb.var(y), tb.var(x)), tb.func(gt, tb.var(a), tb
                                .var(y)))));
        
        term2 = tb.all(x2, tb.ex(y2, tb.and(
                tb.func(gt, tb.var(y2), tb.var(x2)), tb.func(gt, tb.var(a2), tb
                                .var(y2)))));
        
        expr = Term2ExprConverter.convert2Expr(term);
        expr2 = Term2ExprConverter.convert2Expr(term2);

        eam = new ExprAndMessages(expr,expr);
        cacher = new Cacher();
    }
    
    /**
     * Tests the contains()-method
     *
     */
    public void test_contains() {             
        assertEquals( cacher.contains(expr), false );       
        cacher.put(expr, eam);       
        assertEquals( cacher.contains(expr), true );
    }
    
    /**
     * Tests the contains()-method and the special
     * cache-functionality. Indirect tests of renaming
     * and reverting of the internal RenamingTable.
     */
    public void test_contains2() {            
        assertEquals( cacher.contains(expr), false );
        assertEquals( cacher.contains(expr2), false );
        
        cacher.put( expr, eam );
        
        assertEquals( cacher.contains(expr), true );
        assertEquals( cacher.contains(expr2), true ); 
    }
    
    /**
     * Tests the get()-method.
     *
     */
    public void test_get() {
        assertEquals( cacher.contains(expr), false );
        assertEquals( cacher.contains(expr2), false );
        
        cacher.put( expr, eam );      
        ExprAndMessages result = cacher.get(expr2);
        
        assertEquals( result.expression.toString(), expr2.toString());
    }
}
