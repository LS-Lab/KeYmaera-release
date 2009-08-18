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

import orbital.math.Arithmetic;
import de.uka.ilkd.key.dl.rules.metaconstruct.PolynomialReals;
import de.uka.ilkd.key.dl.strategy.termfeature.QuasiRealLiteralFeature;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.TacletApp;
import de.uka.ilkd.key.strategy.feature.BinaryTacletAppFeature;
import de.uka.ilkd.key.strategy.feature.Feature;
import de.uka.ilkd.key.strategy.termProjection.ProjectionToTerm;

/**
 * Return zero only if the value of one (left) polynomial always will be (less
 * or equal) or (less) than the value of a second (right) polynomial.
 * Both polynomials can optionally be multiplied with some constant before
 * comparison.
 */
public abstract class PolynomialValuesCmpFeature extends BinaryTacletAppFeature {

    private final ProjectionToTerm left, right, leftCoeff, rightCoeff;
    
    protected PolynomialValuesCmpFeature(ProjectionToTerm left,
                                         ProjectionToTerm right,
                                         ProjectionToTerm leftCoeff,
                                         ProjectionToTerm rightCoeff) {
        this.left = left;
        this.right = right;
        this.leftCoeff = leftCoeff;
        this.rightCoeff = rightCoeff;
    }

    public static Feature lt(ProjectionToTerm left, ProjectionToTerm right) {
        return lt ( left, right, null, null );
    }
    
    public static Feature lt(ProjectionToTerm left,
                             ProjectionToTerm right,
                             ProjectionToTerm leftCoeff,
                             ProjectionToTerm rightCoeff) {
        return new PolynomialValuesCmpFeature ( left,
                                                right,
                                                leftCoeff,
                                                rightCoeff ) {
            protected boolean compare(PolynomialReals leftPoly, PolynomialReals rightPoly) {
                return leftPoly.valueLess ( rightPoly );                
            }
        };
    }
    
    public static Feature leq(ProjectionToTerm left, ProjectionToTerm right) {
        return leq ( left, right, null, null );
    }
    
    public static Feature leq(ProjectionToTerm left,
                              ProjectionToTerm right,
                              ProjectionToTerm leftCoeff,
                              ProjectionToTerm rightCoeff) {
        return new PolynomialValuesCmpFeature ( left,
                                                right,
                                                leftCoeff,
                                                rightCoeff ) {
            protected boolean compare(PolynomialReals leftPoly, PolynomialReals rightPoly) {
                return leftPoly.valueLeq ( rightPoly );
            }
        };
    }
    
    public static Feature eq(ProjectionToTerm left, ProjectionToTerm right) {
        return eq ( left, right, null, null );
    }
    
    public static Feature eq(ProjectionToTerm left,
                             ProjectionToTerm right,
                             ProjectionToTerm leftCoeff,
                             ProjectionToTerm rightCoeff) {
        return new PolynomialValuesCmpFeature ( left,
                                                right,
                                                leftCoeff,
                                                rightCoeff ) {
            protected boolean compare(PolynomialReals leftPoly, PolynomialReals rightPoly) {
                return leftPoly.valueEq ( rightPoly );
            }
        };
    }
    
    protected boolean filter(TacletApp app, PosInOccurrence pos, Goal goal) {
        return compare ( getPolynomial ( left, leftCoeff, app, pos, goal ),
                         getPolynomial ( right, rightCoeff, app, pos, goal ) );
    }

    protected abstract boolean compare(PolynomialReals leftPoly, PolynomialReals rightPoly);
    
    private PolynomialReals getPolynomial(ProjectionToTerm polyProj,
	                                  ProjectionToTerm coeffProj,
                                          TacletApp app,
                                          PosInOccurrence pos,
                                          Goal goal) {
        final PolynomialReals poly =
            PolynomialReals.create ( polyProj.toTerm ( app, pos, goal ) );

        if (coeffProj == null) return poly;
        final Term coeffT = coeffProj.toTerm ( app, pos, goal );
        if ( coeffT == null ) return poly;
        
        final Arithmetic coeff = QuasiRealLiteralFeature.literal2Arithmetic(coeffT);
        return poly.multiply ( coeff );
    }
}
