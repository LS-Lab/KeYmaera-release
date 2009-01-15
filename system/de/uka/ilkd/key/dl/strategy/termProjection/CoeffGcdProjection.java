// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//


package de.uka.ilkd.key.dl.strategy.termProjection;

import orbital.math.Arithmetic;
import orbital.math.functional.Operations;
import orbital.moon.math.ValuesImpl;
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.OrbitalSimplifier;
import de.uka.ilkd.key.dl.rules.metaconstruct.MonomialReals;
import de.uka.ilkd.key.dl.rules.metaconstruct.PolynomialReals;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.termProjection.ProjectionToTerm;

/**
 * Given a monomial and a polynomial, this projection computes the gcd of all
 * numerical coefficients. The constant term of the polynomial is ignored. The
 * result is guaranteed to be non-negative.
 */
public class CoeffGcdProjection implements ProjectionToTerm {

    private final ProjectionToTerm monomialLeft;
    private final ProjectionToTerm polynomialRight;
    
    private CoeffGcdProjection(ProjectionToTerm monomialLeft,
                               ProjectionToTerm polynomialRight) {
        this.monomialLeft = monomialLeft;
        this.polynomialRight = polynomialRight;
    }

    public static ProjectionToTerm create(ProjectionToTerm monomialLeft,
                                          ProjectionToTerm polynomialRight) {
        return new CoeffGcdProjection ( monomialLeft, polynomialRight );
    }

    public Term toTerm(RuleApp app, PosInOccurrence pos, Goal goal) {
        final Term monoT = monomialLeft.toTerm ( app, pos, goal );
        final Term polyT = polynomialRight.toTerm ( app, pos, goal );

        final MonomialReals mono = MonomialReals.create ( monoT );
        final PolynomialReals poly = PolynomialReals.create ( polyT );

        Arithmetic gcd = MonomialReals.gcd(mono.getCoefficient (), poly.coeffGcd ());
        
        if (Operations.greater.apply(mono.getCoefficient(),
                                     ValuesImpl.getDefault().ZERO())
            !=
            Operations.greater.apply(gcd, ValuesImpl.getDefault().ZERO()))
            gcd = gcd.minus();
            
        return OrbitalSimplifier.arithmetic2Term ( gcd );
    }
}
