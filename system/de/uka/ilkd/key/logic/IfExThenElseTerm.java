// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.logic;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.logic.op.IfExThenElse;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;


/**
 *
 */
class IfExThenElseTerm extends Term {
  
    private final ImmutableArray<Term> subTerm;

    /** depth of the term */
    private final int depth;

    private final ImmutableArray<QuantifiableVariable> exVariables;
    
    public IfExThenElseTerm (IfExThenElse op,
                             Term[] subs,
                             ImmutableArray<QuantifiableVariable> exVariables) {
        super ( op, op.sort ( subs ) );

        this.exVariables = exVariables;
        this.subTerm = new ImmutableArray<Term> ( subs );
        
        int max_depth = -1;
        for (Term sub : subs) {
            if (sub.depth() > max_depth) {
                max_depth = sub.depth();
            }
        }
        depth = max_depth + 1;
    }
 
    /* (non-Javadoc)
     * @see de.uka.ilkd.key.logic.Term#varsBoundHere(int)
     */
    public ImmutableArray<QuantifiableVariable> varsBoundHere (int n) {
        if ( n == 0 || n == 1 ) return exVariables;
        return new ImmutableArray<QuantifiableVariable> ();
    }    

    /* (non-Javadoc)
     * @see de.uka.ilkd.key.logic.Term#arity()
     */
    public int arity () {
        return subTerm.size();
    }
    
    /* (non-Javadoc)
     * @see de.uka.ilkd.key.logic.Term#depth()
     */
    public int depth () {
        return depth;
    }
    
    /* (non-Javadoc)
     * @see de.uka.ilkd.key.logic.Term#sub(int)
     */
    public Term sub (int nr) {
        return subTerm.get ( nr );
    }
}
