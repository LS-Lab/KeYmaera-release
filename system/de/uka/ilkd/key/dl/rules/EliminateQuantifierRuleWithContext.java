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
 * File created 27.03.2007
 */
package de.uka.ilkd.key.dl.rules;

import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.rule.Rule;

/**
 * This rule eliminates the given skolem symbol using all formulas contained in
 * the sequent.
 * 
 * @author jdq
 * @since 27.03.2007
 * 
 */
public class EliminateQuantifierRuleWithContext extends EliminateQuantifierRule {

    public static final EliminateQuantifierRuleWithContext INSTANCE = new EliminateQuantifierRuleWithContext();

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#displayName()
     */
    /*@Override*/
    public String displayName() {
        return "Eliminate Quantifier With Context";
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#name()
     */
    /*@Override*/
    public Name name() {
        return new Name("EliminateQuantifierWithContext");
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.RuleOperatingOnWholeSequence#performSearch(de.uka.ilkd.key.logic.Term)
     */
    /*@Override*/
    protected void performSearch(Term visited) {
        // As want the whole context, always set addFormula to true
        addFormula = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.proof.RuleFilter#filter(de.uka.ilkd.key.rule.Rule)
     */
    /*@Override*/
    public boolean filter(Rule rule) {
        return rule instanceof EliminateQuantifierRuleWithContext;
    }

}
