/**
 * File created 01.02.2007
 */
package de.uka.ilkd.key.dl.rules;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.PairOfTermAndQuantifierType;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.QuantifierType;
import de.uka.ilkd.key.dl.formulatools.ContainsSkolemSymbolVisitor;
import de.uka.ilkd.key.dl.formulatools.SkolemSymbolWithMostParametersVisitor;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.ListOfGoal;
import de.uka.ilkd.key.proof.RuleFilter;
import de.uka.ilkd.key.rule.BuiltInRule;
import de.uka.ilkd.key.rule.Rule;
import de.uka.ilkd.key.rule.RuleApp;

/**
 * The EliminateQuantifierRule is a built-in rule for eleminating quantifiers
 * that are in skolemised form. It is applied to skolem symbol and does a reduce
 * call to the implication of all relevant, thus containing the given skolem
 * symbol, formulas in the current sequence.
 * 
 * @author jdq
 * @since 01.02.2007
 * 
 */
public class EliminateQuantifierRule extends RuleOperatingOnWholeSequence
        implements BuiltInRule, RuleFilter, UnknownProgressRule {

    /**
     * @param formulaContainsSearchSymbolDefault
     */
    public EliminateQuantifierRule() {
        super(false);
    }

    public static final EliminateQuantifierRule INSTANCE = new EliminateQuantifierRule();

    private Term search;

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.BuiltInRule#isApplicable(de.uka.ilkd.key.proof.Goal,
     *      de.uka.ilkd.key.logic.PosInOccurrence,
     *      de.uka.ilkd.key.logic.Constraint)
     */
    public boolean isApplicable(Goal goal, PosInOccurrence pio,
            Constraint userConstraint) {
        if (pio != null) {
            // return (pio.subTerm().op() instanceof RigidFunction)
            // && ((RigidFunction) pio.subTerm().op()).isSkolem();
            return ContainsSkolemSymbolVisitor.containsSkolemSymbolAndIsFO(pio
                    .subTerm());
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#apply(de.uka.ilkd.key.proof.Goal,
     *      de.uka.ilkd.key.java.Services, de.uka.ilkd.key.rule.RuleApp)
     */
    @Override
    public synchronized ListOfGoal apply(Goal goal, Services services,
            RuleApp ruleApp) {
        search = SkolemSymbolWithMostParametersVisitor
                .getSkolemSymbolWithMostParameters(ruleApp.posInOccurrence()
                        .subTerm());
        return super.apply(goal, services, ruleApp);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#displayName()
     */
    public String displayName() {
        return "Eliminate Quantifier";
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#name()
     */
    public Name name() {
        return new Name("EliminateQuantifier");
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

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.proof.RuleFilter#filter(de.uka.ilkd.key.rule.Rule)
     */
    public boolean filter(Rule rule) {
        return rule instanceof EliminateQuantifierRule;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.RuleOperatingOnWholeSequence#performQuery(de.uka.ilkd.key.logic.Term,
     *      de.uka.ilkd.key.dl.IMathSolver)
     */
    @Override
    protected Term performQuery(Term term) throws RemoteException {
        List<String> variables = getVariables();
        if (variables.isEmpty()) {
            variables.add(search.op().name().toString());
        }

        return MathSolverManager.getCurrentQuantifierEliminator().reduce(
                term,
                variables,
                Collections.singletonList(new PairOfTermAndQuantifierType(
                        search, QuantifierType.FORALL)));

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.RuleOperatingOnWholeSequence#performSearch(de.uka.ilkd.key.logic.Term)
     */
    @Override
    protected void performSearch(Term visited) {
        if (visited.op() == search.op()) {
            addFormula = true;
        }
    }

}
