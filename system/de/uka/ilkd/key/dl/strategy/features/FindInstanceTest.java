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
package de.uka.ilkd.key.dl.strategy.features;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.PosInTerm;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Goal.GoalStatus;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

public class FindInstanceTest implements Feature {

    private static final long TIMEOUT = 2000;

    public static final Feature INSTANCE = new FindInstanceTest();

    public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
    	if(!MathSolverManager.isCounterExampleGeneratorSet()) {
    		return LongRuleAppCost.ZERO_COST;
    	}
        Iterator<ConstrainedFormula> it = goal.sequent().antecedent()
                .iterator();
        Term resultTerm = TermBuilder.DF.tt();
        Map<Term, List<PosInOccurrence>> changes = iterate(goal, it,
                resultTerm, true);
        resultTerm = changes.keySet().iterator().next();
        it = goal.sequent().succedent().iterator();
        Map<Term, List<PosInOccurrence>> changes2 = iterate(goal, it,
                TermBuilder.DF.ff(), false);
        resultTerm = TermBuilder.DF.and(resultTerm, TermBuilder.DF.not(changes2
                .keySet().iterator().next()));

        TestThread thread = new TestThread(resultTerm, goal.proof().getServices());
        thread.start();
        try {
            thread.join(2*TIMEOUT);
        } catch (InterruptedException e) {
            System.out.println("Interrupted FindInstanceTest");
        }
        if (thread.result == Result.CE_FOUND) {
        	if(resultTerm.metaVars().size() == 0) {
        		// we do not count the CE if there are metavariables
        		goal.setStatus(GoalStatus.COUNTER_EXAMPLE);
        	}
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

    /**
     * Iterates over the given formulas and constructs the conjunction or
     * disjunction of all first order formulas in the sequence.
     * 
     * @param result
     *                the current goal
     * @param it
     *                the iterator used to access the formulas
     * @param resultTerm
     *                the term built so far
     * @param and
     *                if true this function returns the conjunction, otherwise
     *                the disjunction is returned
     * @return the conjunction or disjunction of all first order formulas in the
     *         sequence.
     */
    private Map<Term, List<PosInOccurrence>> iterate(Goal result,
            Iterator<ConstrainedFormula> it, Term resultTerm, boolean and) {
        List<PosInOccurrence> changes = new ArrayList<PosInOccurrence>();
        while (it.hasNext()) {
            ConstrainedFormula f = it.next();
            changes.add(new PosInOccurrence(f, PosInTerm.TOP_LEVEL, false));
            if (and) {
                resultTerm = TermBuilder.DF.and(resultTerm, f.formula());
            } else {
                resultTerm = TermBuilder.DF.or(resultTerm, f.formula());
            }
        }
        HashMap<Term, List<PosInOccurrence>> res = new HashMap<Term, List<PosInOccurrence>>();
        res.put(resultTerm, changes);
        return res;
    }

    private enum Result {
        CE_FOUND, NO_RESULT_IN_TIME, UNKNOWN, NO_COUNTER_EXAMPLE_AVAILABLE
    }

    private static class TestThread extends Thread {

        private Term term;

        private Result result;

        private String string;

        private Services services;

        public TestThread(Term term, Services services) {
            this.term = term;
            this.services = services;
        }

        /*@Override*/
        public void run() {
            result = Result.UNKNOWN;
            try {
                string = MathSolverManager.getCurrentCounterExampleGenerator()
                        .findInstance(term, TIMEOUT, services);
                if (string.equals("")) {
                    result = Result.NO_COUNTER_EXAMPLE_AVAILABLE;
                } else if(string.trim().equalsIgnoreCase("$Aborted")) {
                    result = Result.NO_RESULT_IN_TIME;
                } else {
                    System.out.println("CEX Counterexample");//XXX 
                    //System.out.println("CEX Counterexample:\n" + string + "\nfor " + term);//XXX 
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
