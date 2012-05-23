/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel                                *
 *   quesel@informatik.uni-oldenburg.de                                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
/**
 * File created 20.02.2007
 */
package de.uka.ilkd.key.dl.strategy.features;

import java.util.HashMap;
import java.util.Iterator;
import java.util.WeakHashMap;

import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Node;
import de.uka.ilkd.key.rule.Rule;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * This feature ensures that a rule is only applied once per branch.
 * 
 * @author jdq
 * @since 20.02.2007
 * 
 */
public class OnlyOncePerBranchFeature implements Feature {

	public static final OnlyOncePerBranchFeature INSTANCE = new OnlyOncePerBranchFeature();
	
	//private WeakHashMap<RuleApp, Rule> applied = new WeakHashMap<RuleApp, Rule>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule.RuleApp,
	 *      de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.proof.Goal)
	 */
	public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
		//if(applied.containsKey(app)) {
		//	return TopRuleAppCost.INSTANCE;

		//}
		//applied.put(app, app.rule());
		

	    if(DLOptionBean.INSTANCE.isReduceOnFreshBranch()) {
	        Node n = goal.node();
	        do {
	            if (n.getAppliedRuleApp() != null && n.getAppliedRuleApp().rule() == app.rule()) {
	                 return TopRuleAppCost.INSTANCE;
	            }
	            n = n.parent();
	        } while(n != null && n.childrenCount() <= 1);    
	    } else {
            Iterator<RuleApp> it = goal.appliedRuleApps().iterator();
            while (it.hasNext()) {
                RuleApp next = it.next();
                if (next.rule() == app.rule()) {
                    return TopRuleAppCost.INSTANCE;
                }

            }
	    }
	    
		return LongRuleAppCost.ZERO_COST;
	}
}
