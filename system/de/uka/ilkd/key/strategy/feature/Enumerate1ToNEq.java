// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//


package de.uka.ilkd.key.strategy.feature;

import java.util.Iterator;

import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;

/**
 * Just an example ... to be removed!
 * 
 * Very stupid term generator that generates a list of equations of the form
 * <tt>n = n</tt>.
 */
public class Enumerate1ToNEq implements TermGenerator {

    private final int n;
    
    private final TermBuilder tb = TermBuilder.DF;

    public Enumerate1ToNEq(int n) {
        this.n = n;
    }

    public Iterator<Term> generate(RuleApp app,
                                   PosInOccurrence pos,
                                   final Goal goal) {
        return new Iterator<Term> () {
            private int i = 0;
            public boolean hasNext() {
                return i <= n;
            }
            public Term next() {
                final Term num = tb.zTerm ( goal.proof ().getServices (),
                                            Integer.toString ( i ) );
                ++i;
                return tb.equals ( num, num );
            }
			/*@Override*/
			public void remove() {
				// TODO Auto-generated method stub
				
			}            
        };
    }

}
