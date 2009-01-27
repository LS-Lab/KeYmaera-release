// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//


package de.uka.ilkd.key.dl.strategy.features;

import de.uka.ilkd.key.dl.strategy.termfeature.QuasiRealLiteralFeature;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.TacletApp;
import de.uka.ilkd.key.strategy.feature.Feature;
import de.uka.ilkd.key.strategy.termProjection.ProjectionToTerm;


/**
 * Feature that returns zero iff each variable/atom of one monomial is smaller
 * than all variables of a second monomial
 */
public class AtomsSmallerThanFeature extends AbstractMonomialSmallerThanFeature {

    private final ProjectionToTerm left, right;

    private AtomsSmallerThanFeature(ProjectionToTerm left, ProjectionToTerm right) {
        this.left = left;
        this.right = right;
    }


    public static Feature create(ProjectionToTerm left, ProjectionToTerm right) {
        return new AtomsSmallerThanFeature ( left, right );
    }

    protected boolean filter(TacletApp app, PosInOccurrence pos, Goal goal) {
        return lessThan ( collectAtoms ( left.toTerm ( app, pos, goal ) ),
                          collectAtoms ( right.toTerm ( app, pos, goal ) ) );
    }

    /**
     * this overwrites the method of <code>SmallerThanFeature</code>
     */
    protected boolean lessThan(Term t1, Term t2) {
        if ( QuasiRealLiteralFeature.isLiteral(t1) ) {
            if ( !QuasiRealLiteralFeature.isLiteral(t2) ) return true;
        } else {
            if ( QuasiRealLiteralFeature.isLiteral(t2) ) return false;
        }
        
        return super.lessThan ( t1, t2 );
    }
}
