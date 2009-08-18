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

import de.uka.ilkd.key.dl.rules.metaconstruct.MonomialReals;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.TacletApp;
import de.uka.ilkd.key.strategy.feature.BinaryTacletAppFeature;
import de.uka.ilkd.key.strategy.feature.Feature;
import de.uka.ilkd.key.strategy.termProjection.ProjectionToTerm;


/**
 * Return zero iff the monomial <code>dividendSV</code> can be made smaller
 * (in the polynomial reduction ordering) by adding or subtracting
 * <code>divisorSV</code>
 */
public abstract class ReducibleMonomialsFeature extends BinaryTacletAppFeature {
    private final ProjectionToTerm dividend, divisor;

    private ReducibleMonomialsFeature(ProjectionToTerm dividend,
                                      ProjectionToTerm divisor) {
        this.dividend = dividend;
        this.divisor = divisor;
    }

    public static Feature createDivides(ProjectionToTerm dividend,
                                        ProjectionToTerm divisor) {
        return new ReducibleMonomialsFeature ( dividend, divisor ) {
            protected boolean checkReducibility(MonomialReals mDividend,
                                                MonomialReals mDivisor) {
                return mDivisor.divides ( mDividend );
            }            
        };
    }

    protected boolean filter(TacletApp app, PosInOccurrence pos, Goal goal) {        
        final Term dividendT = dividend.toTerm ( app, pos, goal );
        final Term divisorT = divisor.toTerm ( app, pos, goal );
        
        final MonomialReals mDividend = MonomialReals.create ( dividendT );
        final MonomialReals mDivisor = MonomialReals.create ( divisorT );
        
        return checkReducibility ( mDividend, mDivisor );
    }

    protected abstract boolean checkReducibility(MonomialReals mDividend,
                                                 MonomialReals mDivisor);
}
