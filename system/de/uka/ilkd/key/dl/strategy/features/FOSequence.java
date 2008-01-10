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

import de.uka.ilkd.key.logic.IteratorOfConstrainedFormula;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Sequent;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.IUpdateOperator;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.SubstOp;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * Checks whether formulas are first-order formulas, respectively sequents purely consist
 * of first-order formulas. 
 * 
 * @author jdq
 * @since 20.02.2007
 * @todo undo this slowish synchronization stuff after thinking about it properly
 */
public class FOSequence extends Visitor implements Feature {

    public static final FOSequence INSTANCE = new FOSequence();

    private boolean notFO = false;

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule.RuleApp,
     *      de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.proof.Goal)
     */
    public synchronized RuleAppCost compute(RuleApp app, PosInOccurrence pos,
            Goal goal) {
        if (isFOSequent(goal.sequent())) {
            return LongRuleAppCost.ZERO_COST;
        } else {
            return TopRuleAppCost.INSTANCE;
        }
    }

    public synchronized boolean isFOSequent(Sequent seq) {
        IteratorOfConstrainedFormula it = seq.iterator();
        while (it.hasNext()) {
            notFO = false;
            it.next().formula().execPreOrder(this);
            if (notFO) {
                return false;
            }
        }
        return true;
    }
    
    public synchronized boolean isFOFormulas(IteratorOfConstrainedFormula formulas) {
        IteratorOfConstrainedFormula it = formulas;
        while (it.hasNext()) {
            notFO = false;
            it.next().formula().execPreOrder(this);
            if (notFO) {
                return false;
            }
        }
        return true;
    }

    public synchronized boolean isFOFormula(Term t) {
        notFO = false;
        t.execPreOrder(this);
        return !notFO;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
     */
    @Override
    public void visit(Term visited) {
        if (!isFOOperator(visited.op())) {
            notFO = true;
        }
    }

    /**
     * This function tests whether the given operator is a common first-order
     * logical operator.
     * 
     * @param op
     *            the operator to test
     * @return true, if the operator is an FO operator
     */
    public static boolean isFOOperator(Operator op) {
        if (op instanceof Modality || op instanceof SubstOp
                || op instanceof IUpdateOperator) {
            return false;
        }
        return true;
    }
}
