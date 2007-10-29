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
 * File created 29.03.2007
 */
package de.uka.ilkd.key.dl.rules;

import java.util.List;

import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.rule.BuiltInRule;
import de.uka.ilkd.key.rule.BuiltInRuleApp;

/**
 * Special RuleApp for the DL builtin rules. This rule app can store a list of
 * variables that should be reduced.
 * 
 * @author jdq
 * @since 29.03.2007
 * 
 */
public class ReduceRuleApp extends BuiltInRuleApp {

    private List<String> variables;

    /**
     * @param builtInRule
     * @param pio
     * @param userConstraint
     */
    public ReduceRuleApp(BuiltInRule builtInRule, PosInOccurrence pio,
            Constraint userConstraint, List<String> variables) {
        super(builtInRule, pio, userConstraint);
        this.variables = variables;
    }

    /**
     * @return the variables
     */
    public List<String> getVariables() {
        return variables;
    }

    /**
     * @param variables
     *                the variables to set
     */
    public void setVariables(List<String> variables) {
        this.variables = variables;
    }

}
