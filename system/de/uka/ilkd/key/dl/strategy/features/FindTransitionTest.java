/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel, André Platzer                 *
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
package de.uka.ilkd.key.dl.strategy.features;

import java.rmi.RemoteException;
import java.util.Collections;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.formulatools.TermTools;
import de.uka.ilkd.key.dl.rules.FindTransitionRule;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

public class FindTransitionTest implements Feature {

    private static final long TIMEOUT = 2000;

    public static final Feature INSTANCE = new FindTransitionTest();

    public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
        //@todo cache stuff
        if (!FindTransitionRule.INSTANCE.isApplicable(goal, pos, null)) {
            return LongRuleAppCost.ZERO_COST;
        }
        if (goal.sequent().succedent().size() > 1) {
            //@todo we only gives a counterexample when succedent is otherwise empty
            //@todo should always produce counterexample but only stop on it if succedent otherwise empty
            return LongRuleAppCost.create(1);
        }
        Term antecedent = TermTools.createJunctorTermNAry(TermBuilder.DF.tt(),
                Op.AND, goal.sequent().antecedent().iterator(),
                Collections.EMPTY_SET, true);

        TestThread thread = new TestThread(antecedent, pos.subTerm(), goal.proof().getServices());
        thread.start();
        try {
            thread.join(2*TIMEOUT);
        } catch (InterruptedException e) {
            System.out.println("Interrupted FindTransitionTest");
        }
        if (thread.result == Result.CE_FOUND) {
            return TopRuleAppCost.INSTANCE;
        }
        if (thread.isAlive()) {
            try {
                MathSolverManager.getCurrentCounterExampleGenerator()
                        .abortCalculation();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return LongRuleAppCost.ZERO_COST;
    }
    private enum Result {
        CE_FOUND, NO_RESULT_IN_TIME, UNKNOWN, NO_COUNTER_EXAMPLE_AVAILABLE
    }

    private static class TestThread extends Thread {

        private Term term;
        private Term modalFormula;

        private Result result;

        private String string;
        
        private Services services;

        public TestThread(Term term, Term modalFormula, Services services) {
            this.term = term;
            this.modalFormula = modalFormula;
        }

        /*@Override*/
        public void run() {
            result = Result.UNKNOWN;
            try {
                string = MathSolverManager.getCurrentCounterExampleGenerator()
                        .findTransition(term, modalFormula, TIMEOUT, services);
                if (string.equals("")) {
                    result = Result.NO_COUNTER_EXAMPLE_AVAILABLE;
                } else if(string.trim().equalsIgnoreCase("$Aborted")) {
                    result = Result.NO_RESULT_IN_TIME;
                } else {
                    System.out.println("CEX Transition");//XXX 
                    System.out.println("CEX Transition:\n" + string + "\nfor " + modalFormula);//XXX 
                    result = Result.CE_FOUND;
                }
            } catch (RemoteException e) {
                result = Result.NO_RESULT_IN_TIME;
            } catch (SolverException e) {
                result = Result.NO_RESULT_IN_TIME;
            } catch (AssertionError e) {
                result = Result.UNKNOWN;
                e.printStackTrace();
            }
        }

        public Result getResult() {
            return result;
        }
    }
}
