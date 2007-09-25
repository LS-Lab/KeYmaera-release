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

import java.util.HashMap;
import java.util.Map;

import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PIOPathIterator;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;

/**
 */
public class CountSymbolsFeature implements Feature {
    
    private final Map nameToCost = new HashMap ();
    
    private CountSymbolsFeature () {}
    
    public static Feature create () {
        return new CountSymbolsFeature ();
    }
    
    public void setCost(Name name, RuleAppCost cost) {
        nameToCost.put ( name, cost );
    }
    
    public RuleAppCost compute ( RuleApp app, PosInOccurrence pos, Goal goal ) {
        assert pos != null : "Feature is only applicable to rules with find";

        RuleAppCost res = LongRuleAppCost.ZERO_COST;
        final PIOPathIterator it = pos.iterator ();

        while ( it.next () != -1 && ! ( res instanceof TopRuleAppCost ) ) {
            final Term t = it.getSubTerm ();
            final Operator op = t.op ();

            final RuleAppCost cost = (RuleAppCost)nameToCost.get ( op.name () );
            if ( cost != null ) res = res.add ( cost );
        }
        
        return res;
    }
    
}
