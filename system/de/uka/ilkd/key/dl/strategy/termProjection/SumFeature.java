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

import java.util.Iterator;

import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * A feature that computes the sum of the values of a feature term when a given
 * variable ranges over a sequence of terms
 */
public class SumFeature<T> implements Feature {
    
    private final Buffer<T> var;
    private final Generator<T> generator;
    private final Feature body;

    /**
     * @param var
     *            <code>TermBuffer</code> in which the terms are going to
     *            be stored
     * @param generator
     *            the terms that are to be iterated over
     * @param body
     *            a feature that is supposed to be evaluated repeatedly for the
     *            possible values of <code>var</code>
     */
    public static <G> Feature create(Buffer<G> var,
                                 Generator<G> generator,
                                 Feature body) {
        return new SumFeature<G> ( var, generator, body );
    }

    private SumFeature (Buffer<T> var,
                                   Generator<T> generator,
                                   Feature body) {
        this.var = var;
        this.generator = generator;
        this.body = body;
    }

    
    public RuleAppCost compute (RuleApp app, PosInOccurrence pos, Goal goal) {        
        final T outerVarContent = var.getContent ();
        
        final Iterator<T> it = generator.generate ( app, pos, goal );
        RuleAppCost res = LongRuleAppCost.ZERO_COST;
        while ( it.hasNext () && ! ( res instanceof TopRuleAppCost ) ) {
            var.setContent ( it.next () );
            res = res.add ( body.compute ( app, pos, goal ) );
        }
        
        var.setContent ( outerVarContent );
        return res;
    }
}
