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

import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermOrdering;
import de.uka.ilkd.key.logic.ldt.IntegerLDT;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.TacletApp;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.termfeature.TermFeature;
import de.uka.ilkd.key.util.Debug;

/**
 * Feature that returns zero iff a literal coefficient *1 on the left side of an
 * equation is considered unnecessary. This is the case if the left side is
 * bigger than the right side even after eliminating the coefficient.
 * (Otherwise we have to keep the coefficient, because <code>commute_eq</code>
 * would commute the equation and the strategy probably would loop)
 */
public class LiteralCoeffEqLeftUnnecessaryFeature extends BinaryTacletAppFeature {
    private final TermOrdering termOrdering = OrderingFeature.createTermOrdering ();
    private final TermFeature  hasCoeff;

    private LiteralCoeffEqLeftUnnecessaryFeature(IntegerLDT numbers) {
        hasCoeff =
            MonomialsSmallerThanFeature.createHasCoeffTermFeature ( numbers );
    }

    public static Feature create(IntegerLDT numbers) {
        return new LiteralCoeffEqLeftUnnecessaryFeature ( numbers );
    }
    
    protected boolean filter(TacletApp app, PosInOccurrence pos, Goal goal) {
        assert pos != null : "Feature is only applicable to rules with find";
        Debug.assertTrue ( pos.getIndex() == 0,
                           "Feature is only applicable if focus is left term "
                           + "of an equation" );
        final Term eq = pos.up().subTerm();
        Debug.assertTrue ( eq.op() == Op.EQUALS,
                           "Feature is only applicable if focus is left term "
                           + "of an equation" );
        
        final Term leftMonomial = stripOffLiteral ( pos.subTerm () );
        final Term rightPolynomial = eq.sub ( 1 );
        
        return termOrdering.compare ( rightPolynomial, leftMonomial ) < 0;
    }
    
    private Term stripOffLiteral(Term te) {
        if ( ! ( hasCoeff.compute ( te ) instanceof TopRuleAppCost ) )
            return te.sub ( 0 );
        return te;
    }
}
