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
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * Feature for storing and restoring <code>RuleApp</code>s, supposed to be used
 * together with the class <code>StoreRuleAppCP</code>. The side-effect of
 * the feature is to restore the rule app that is currently stored, the result
 * is always zero.
 */
public class RuleAppBuffer implements Feature {

    private BackTrackingManager manager = null;
    private RuleApp storedRuleApp = null;
    
    public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
        manager.passChoicePoint ( cp, this );
        return LongRuleAppCost.ZERO_COST;        
    }

    public RuleApp getContent() {
        return storedRuleApp;
    }

    public BackTrackingManager getBackTrackingManager() {
        return manager;
    }
    
    public void setContent(RuleApp storedRuleApp, BackTrackingManager manager) {
        this.manager = manager;
        this.storedRuleApp = storedRuleApp;
    }
    
    private final ChoicePoint cp = new ChoicePoint () {
    	public Iterator<CPBranch> getBranches(final RuleApp oldApp) {
            final CPBranch branch = new CPBranch () {
                private final RuleApp app = storedRuleApp;
                public void choose() {}
                public RuleApp getRuleAppForBranch() { return app; }
            };
            
            final ImmutableSLList<CPBranch> nil = ImmutableSLList.nil();
			return nil.prepend ( branch ).iterator();
        }        
    };

}
