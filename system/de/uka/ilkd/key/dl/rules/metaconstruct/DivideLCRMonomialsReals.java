//This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//
package de.uka.ilkd.key.dl.rules.metaconstruct;

import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.AbstractMetaOperator;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
* Metaoperator for computing the result of dividing one monomial by another
*/
public class DivideLCRMonomialsReals extends AbstractMetaOperator {

    public DivideLCRMonomialsReals() {
        super ( new Name ( "#divideLCRMonomialsReals" ), 2 );
    }

    /**
     * checks whether the top level structure of the given
     * 
     * @param term the Term is syntactically valid, given the assumption that the top
     *       level operator of the term is the same as this Operator. The
     *       assumption that the top level operator and the term are equal is
     *       NOT checked.
     * @return true iff the top level structure of the Term is valid.
     */
    public boolean validTopLevel(Term term) {
        // a meta operator accepts almost everything
        return term.op () instanceof DivideLCRMonomialsReals
               && term.arity () == arity ();
    }

    /** calculates the resulting term. */
    public Term calculate(Term term, SVInstantiations svInst, Services services) {
        final Term arg1 = term.sub ( 0 );
        final Term arg2 = term.sub ( 1 );

        final MonomialReals m1 = MonomialReals.create ( arg1 );
        final MonomialReals m2 = MonomialReals.create ( arg2 );

        return m2.divideLCR ( m1 ).toTerm ();
    }

}
