/**
 * File created 01.02.2007
 */
package de.uka.ilkd.key.dl.rules;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.IteratorOfConstrainedFormula;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.PosInTerm;
import de.uka.ilkd.key.logic.SequentChangeInfo;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.ListOfGoal;
import de.uka.ilkd.key.proof.SLListOfGoal;
import de.uka.ilkd.key.rule.BuiltInRule;
import de.uka.ilkd.key.rule.RuleApp;

/**
 * This class is used as super class for rules operating on a whole sequence.
 * 
 * @author jdq
 * @since 08.03.2007
 * 
 */
public abstract class RuleOperatingOnWholeSequence extends Visitor implements
        BuiltInRule, TestableBuiltInRule {

    protected boolean addFormula;

    private boolean addFormulaDefault;

    private List<String> variables;

    private boolean testMode = false;

    private Term inputFormula;

    private Term resultFormula;

    /**
     * 
     */
    public RuleOperatingOnWholeSequence(boolean addFormulaDef) {
        addFormulaDefault = addFormulaDef;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#apply(de.uka.ilkd.key.proof.Goal,
     *      de.uka.ilkd.key.java.Services, de.uka.ilkd.key.rule.RuleApp)
     */
    public synchronized ListOfGoal apply(Goal goal, Services services,
            RuleApp ruleApp) {
        // reset the testmode instantaniously to ensure that it cannot be
        // enabled accidently
        boolean testModeActive = testMode;
        testMode = false;

        System.out.println("Reduce called in testmode: " + testModeActive);// XXX

        IteratorOfConstrainedFormula it = goal.sequent().antecedent()
                .iterator();
        Term resultTerm = TermBuilder.DF.tt();
        Map<Term, List<PosInOccurrence>> changes = iterate(goal, it,
                resultTerm, true, true);
        resultTerm = changes.keySet().iterator().next();
        it = goal.sequent().succedent().iterator();
        Map<Term, List<PosInOccurrence>> changes2 = iterate(goal, it,
                TermBuilder.DF.ff(), false, false);
        resultTerm = TermBuilder.DF.imp(resultTerm, changes2.keySet()
                .iterator().next());

        if (ruleApp instanceof ReduceRuleApp) {
            variables = ((ReduceRuleApp) ruleApp).getVariables();
        } else {
            variables = new ArrayList<String>();
        }

        inputFormula = resultTerm;
        resultFormula = null;

        try {
            resultTerm = performQuery(resultTerm);
        } catch (RemoteException e) {
            return null;
        }
        resultFormula = resultTerm;

        if (!testModeActive) {
            if (resultTerm.equals(TermBuilder.DF.tt())) {
                return goal.split(0);
            }
            ListOfGoal result = goal.split(1);
            for (PosInOccurrence i : changes.values().iterator().next()) {
                result.head().setSequent(
                        result.head().sequent().removeFormula(i));
            }
            for (PosInOccurrence i : changes2.values().iterator().next()) {
                result.head().setSequent(
                        result.head().sequent().removeFormula(i));
            }
            if (!resultTerm.equals(TermBuilder.DF.ff())) {
                SequentChangeInfo info = result.head().sequent().addFormula(
                        new ConstrainedFormula(resultTerm), false, true);
                result.head().setSequent(info);
            }
            return result;
        }
        return SLListOfGoal.EMPTY_LIST;
    }

    /**
     * @param resultTerm
     * @param solver
     * @return
     * @throws RemoteException
     */
    protected abstract Term performQuery(Term term) throws RemoteException;

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
            IteratorOfConstrainedFormula it, Term resultTerm, boolean and, boolean ante) {
        List<PosInOccurrence> changes = new ArrayList<PosInOccurrence>();
        while (it.hasNext()) {
            ConstrainedFormula f = it.next();
            addFormula = addFormulaDefault;
            f.formula().execPostOrder(this);
            if (addFormula) {
                changes.add(new PosInOccurrence(f, PosInTerm.TOP_LEVEL, ante));
                if (and) {
                    resultTerm = TermBuilder.DF.and(resultTerm, f.formula());
                } else {
                    resultTerm = TermBuilder.DF.or(resultTerm, f.formula());
                }
            }
        }
        HashMap<Term, List<PosInOccurrence>> res = new HashMap<Term, List<PosInOccurrence>>();
        res.put(resultTerm, changes);
        return res;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
     */
    @Override
    public void visit(Term visited) {
        performSearch(visited);
    }

    /**
     * Filters the terms. The result is passed using the addFormula field.
     * 
     * @param visited
     *                the term to test.
     */
    protected abstract void performSearch(Term visited);

    /**
     * @return the variables
     */
    protected List<String> getVariables() {
        return variables;
    }

    public boolean test(Goal goal, Services services, RuleApp app) {
        testMode = true;
        try {
            if (apply(goal, services, app) != null) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public Term getInputFormula() {
        return inputFormula;
    }

    public Term getResultFormula() {
        return resultFormula;
    }

}
