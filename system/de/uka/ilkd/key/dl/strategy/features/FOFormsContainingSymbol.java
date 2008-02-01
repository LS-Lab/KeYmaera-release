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

import de.uka.ilkd.key.dl.formulatools.SkolemSymbolWithMostParametersVisitor;
import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.IteratorOfConstrainedFormula;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * This feature checks whether a certain symbol is contained in the current
 * formula. It also ensures that the formula is in first order form.
 * 
 * @author jdq
 * @since 20.02.2007
 * 
 */
public class FOFormsContainingSymbol extends Visitor implements Feature {

    public static final FOFormsContainingSymbol INSTANCE = new FOFormsContainingSymbol();

    private boolean formulaContainsSearchSymbol;

    private boolean formulaIsFO;

    private Term search;

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule.RuleApp,
     *      de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.proof.Goal)
     */
    public synchronized RuleAppCost compute(RuleApp app, PosInOccurrence pos,
            Goal goal) {
        // search = (RigidFunction) app.posInOccurrence().subTerm().op();

        search = SkolemSymbolWithMostParametersVisitor
                .getSkolemSymbolWithMostParameters(app.posInOccurrence()
                        .subTerm());
        IteratorOfConstrainedFormula it = goal.sequent().iterator();
        while (it.hasNext()) {
            ConstrainedFormula f = it.next();
            formulaContainsSearchSymbol = false;
            formulaIsFO = true;
            f.formula().execPostOrder(this);
            if (formulaContainsSearchSymbol && !formulaIsFO) {
                return TopRuleAppCost.INSTANCE;
            }
        }
        return LongRuleAppCost.ZERO_COST;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
     */
    @Override
    public void visit(Term visited) {
        if (visited.op() == search.op()) {
            formulaContainsSearchSymbol = true;
        }
        if (visited.op() instanceof Modality) {
            formulaIsFO = false;
        }

    }

}
