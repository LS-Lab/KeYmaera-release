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
package de.uka.ilkd.key.dl.strategy.features;

import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;
import de.uka.ilkd.key.strategy.termProjection.ProjectionToTerm;

/**
 * Checks whether a term projection gives a first-order formula
 * @author ap
 *
 */
public class FirstOrderFeature implements Feature {

    private final ProjectionToTerm value;

    public FirstOrderFeature(ProjectionToTerm value) {
        super();
        this.value = value;
    }

    /*@Override*/
    public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
        return FOSequence.INSTANCE.isFOFormula(value.toTerm(app, pos, goal))
                ? LongRuleAppCost.ZERO_COST
                : TopRuleAppCost.INSTANCE;
    }

}
