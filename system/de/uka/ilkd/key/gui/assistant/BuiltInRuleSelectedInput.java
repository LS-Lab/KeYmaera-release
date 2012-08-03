// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2004 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
package de.uka.ilkd.key.gui.assistant;

import de.uka.ilkd.key.rule.Rule;
import de.uka.ilkd.key.rule.RuleApp;

/**
 * A rule event has happend. The corresponding rule application object
 * is encapsulated by the input object and can be evaluated by the
 * proof assistant AI.
 */
public class BuiltInRuleSelectedInput implements AIInput {

    /** the rule application to be evaluated */
    private final String rule;

    public BuiltInRuleSelectedInput(String rule) {
        this.rule = rule;
    }
    
    /**
     * @return the rule
     */
    public String getRule() {
        return rule;
    }

    /** 
     * returns the AI input identifier 
     */
    public int getInputID() {
	return BUILT_IN_RULE_SELECTED_EVENT;
    }

    /** toString */
    public String toString() {
	return "RuleEvent: "+getInputID()+
	    "\n for Rule:"+getRule();
    }

    

}
