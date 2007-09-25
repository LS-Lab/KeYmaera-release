package de.uka.ilkd.key.dl.strategy.features;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.IteratorOfConstrainedFormula;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.PosInTerm;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

public class FindInstanceTest implements Feature {

    private static final long TIMEOUT = 2000;

    public static final Feature INSTANCE = new FindInstanceTest();

    public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
        IteratorOfConstrainedFormula it = goal.sequent().antecedent()
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

        TestThread thread = new TestThread(resultTerm);
        thread.start();
        try {
            thread.join(TIMEOUT);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
            IteratorOfConstrainedFormula it, Term resultTerm, boolean and) {
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

    private class TestThread extends Thread {

        private Term term;

        private Result result;

        private String string;

        public TestThread(Term term) {
            this.term = term;
        }

        @Override
        public void run() {
            result = Result.UNKNOWN;
            try {
                string = MathSolverManager.getCurrentCounterExampleGenerator()
                        .findInstance(term);
                if (string.equals("")) {
                    result = Result.NO_COUNTER_EXAMPLE_AVAILABLE;
                } else if(string.trim().equalsIgnoreCase("$Aborted")) {
                    result = Result.NO_RESULT_IN_TIME;
                } else {
                    System.out.println("CE: " + string);//XXX 
                    result = Result.CE_FOUND;
                }
            } catch (RemoteException e) {
                result = Result.NO_RESULT_IN_TIME;
            }
        }

        public Result getResult() {
            return result;
        }
    }
}
