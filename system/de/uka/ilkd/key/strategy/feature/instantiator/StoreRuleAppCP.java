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
 * Feature that stores the most recent <code>RuleApp</code> of the
 * <code>BackTrackingManager</code> in a <code>RuleAppBuffer</code>.
 * The buffer can afterwards (within the term <code>body</code>) be used to
 * access the <code>RuleApp</code>. The result of the feature is the result of
 * <code>body</code>.
 */
public class StoreRuleAppCP implements Feature {

    private final BackTrackingManager manager;
    private final RuleAppBuffer buffer;
    private final Feature body;

    private StoreRuleAppCP(RuleAppBuffer buffer,
                           BackTrackingManager manager,
                           Feature body) {
        this.manager = manager;
        this.buffer = buffer;
        this.body = body;
    }

    public static Feature create(RuleAppBuffer buffer,
                                 BackTrackingManager manager,
                                 Feature body) {
        return new StoreRuleAppCP(buffer, manager, body);
    }

    public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
        final RuleApp outerVarContent = buffer.getContent();
        final BackTrackingManager outerManager = buffer.getBackTrackingManager();

        // ensure that the buffer contains something, in case the current
        // feature evaluation run has been "cancelled" (see
        // <code>BackTrackingManager.cancelChoicePoint</code>)
        buffer.setContent(app, manager);
        
        manager.passChoicePoint(cp, this);
        
        final RuleAppCost res = body.compute(app, pos, goal);

        buffer.setContent(outerVarContent, outerManager);
        return res;
    }

    private final ChoicePoint cp = new ChoicePoint () {
    	public Iterator<CPBranch> getBranches(final RuleApp oldApp) {
            final CPBranch branch = new CPBranch () {
                public void choose() {
                    buffer.setContent(oldApp, manager);                    
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
