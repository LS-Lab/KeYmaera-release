// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//


package de.uka.ilkd.key.strategy.feature.instantiator;

import java.util.Iterator;

import de.uka.ilkd.key.collection.ImmutableSLList;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * Feature to make the most recent <code>RuleApp</code> stored by the
 * <code>BackTrackingManager</code> accessible to other features (the features
 * in <code>body</code>). The result of the feature is the result of
 * <code>body</code>.
 */
public class OpenRuleAppCP implements Feature {

    private final BackTrackingManager manager;
    private final Feature body;

    private RuleApp currentRuleApp = null;

    private OpenRuleAppCP(Feature body, BackTrackingManager manager) {
        this.body = body;
        this.manager = manager;
    }

    public static Feature create(Feature body, BackTrackingManager manager) {
        return new OpenRuleAppCP (body, manager);
    }
    
    public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
        currentRuleApp = app;
        
        manager.passChoicePoint(cp, this);
        
        return body.compute(currentRuleApp, pos, goal);
    }

    private final ChoicePoint cp = new ChoicePoint () {
        public Iterator<CPBranch> getBranches(final RuleApp oldApp) {
            final CPBranch branch = new CPBranch () {
                public void choose() {
                    currentRuleApp = oldApp;
                }
                public RuleApp getRuleAppForBranch() {
                    return oldApp;
                }
            };
            
            final ImmutableSLList<CPBranch> nil = ImmutableSLList.nil();
			return nil.prepend ( branch ).iterator();
        }        
    };
}
