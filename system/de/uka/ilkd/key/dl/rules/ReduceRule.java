/**
 * File created 01.02.2007
 */
package de.uka.ilkd.key.dl.rules;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.PairOfTermAndQuantifierType;
import de.uka.ilkd.key.dl.formulatools.SkolemfunctionTracker;
import de.uka.ilkd.key.dl.formulatools.TermRewriter;
import de.uka.ilkd.key.dl.formulatools.TermRewriter.Match;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.strategy.features.FOSequence;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.ArrayOfQuantifiableVariable;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.Quantifier;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.ListOfGoal;
import de.uka.ilkd.key.proof.RuleFilter;
import de.uka.ilkd.key.rule.BuiltInRule;
import de.uka.ilkd.key.rule.Rule;
import de.uka.ilkd.key.rule.RuleApp;

/**
 * The ReduceRule is a Built-In Rule that is applied to a whole sequence. It is
 * used by the DLProfile. It calls Reduce on the reconstructed implication.
 * 
 * @author jdq
 * @since 01.02.2007
 * @see de.uka.ilkd.key.proof.DLProfile
 */
public class ReduceRule extends RuleOperatingOnWholeSequence implements
        BuiltInRule, RuleFilter, UnknownProgressRule {

    private Set<Term> skolemSymbols;

    private HashSet<String> quantifiedVariables;

    /**
     * @param formulaContainsSearchSymbolDefault
     */
    public ReduceRule() {
        super(true);
    }

    public static final ReduceRule INSTANCE = new ReduceRule();

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.BuiltInRule#isApplicable(de.uka.ilkd.key.proof.Goal,
     *      de.uka.ilkd.key.logic.PosInOccurrence,
     *      de.uka.ilkd.key.logic.Constraint)
     */
    public boolean isApplicable(Goal goal, PosInOccurrence pio,
            Constraint userConstraint) {
        return MathSolverManager.isQuantifierEliminatorSet()
                && ((DLOptionBean.INSTANCE.isSimplifyBeforeReduce() || DLOptionBean.INSTANCE
                        .isSimplifyAfterReduce()) ? MathSolverManager
                        .isSimplifierSet() : true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#displayName()
     */
    public String displayName() {
        return "Eliminate Universal Quantifiers";
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#name()
     */
    public Name name() {
        return new Name("Eliminate Universal Quantifiers");
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.proof.RuleFilter#filter(de.uka.ilkd.key.rule.Rule)
     */
    public boolean filter(Rule rule) {
        return rule instanceof ReduceRule;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return displayName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.RuleOperatingOnWholeSequence#apply(de.uka.ilkd.key.proof.Goal,
     *      de.uka.ilkd.key.java.Services, de.uka.ilkd.key.rule.RuleApp)
     */
    @Override
    public synchronized ListOfGoal apply(Goal goal, Services services,
            RuleApp ruleApp) {
        quantifiedVariables = new HashSet<String>();
        skolemSymbols = new HashSet<Term>();
        return super.apply(goal, services, ruleApp);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.RuleOperatingOnWholeSequence#visit(de.uka.ilkd.key.logic.Term)
     */
    @Override
    public void visit(Term visited) {
        if (visited.op() instanceof RigidFunction) {
            RigidFunction sk = (RigidFunction) visited.op();
            if (sk.isSkolem()) {
                skolemSymbols.add(visited);
            }
        } else if (visited.op() instanceof Quantifier) {
            ArrayOfQuantifiableVariable varsBoundHere = visited
                    .varsBoundHere(0);
            for (int i = 0; i < varsBoundHere.size(); i++) {
                quantifiedVariables.add(varsBoundHere
                        .getQuantifiableVariable(i).name().toString());
            }
        }
        super.visit(visited);
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
        List<Term> skolem = new LinkedList<Term>();

        skolem.addAll(SkolemfunctionTracker.INSTANCE
                .getOrderedList(skolemSymbols));
        if (variables.isEmpty()) {
            // TODO: check if we need to sort the quantified variables
            variables.addAll(quantifiedVariables);
            // variables.addAll(skolem);
        }

        if (DLOptionBean.INSTANCE.isReaddQuantifiers()) {
            // To reduce the formula we should really reintroduce the
            // quantifier,
            // as it leads to a tremendous performance gain
            // List<PairOfTermAndQuantifierType> quantifiers = new
            // LinkedList<PairOfTermAndQuantifierType>();
            // for (Term t : skolem) {
            // quantifiers.add(new PairOfTermAndQuantifierType(t,
            // QuantifierType.FORALL));
            // }
            // term = MathSolverManager.getCurrentQuantifierEliminator().reduce(
            // term, variables, quantifiers);
            Set<Match> matches = new HashSet<Match>();
            List<LogicVariable> vars = new ArrayList<LogicVariable>();
            for (Term sk : skolem) {
                LogicVariable logicVariable = new LogicVariable(new Name(sk
                        .op().name()
                        + "$skolem"), sk.op().sort(new Term[0]));
                vars.add(logicVariable);
                matches.add(new Match((RigidFunction) sk.op(), TermBuilder.DF
                        .var(logicVariable)));
                variables.add(logicVariable.name().toString());
            }
            term = TermRewriter.replace(term, matches);
            for (QuantifiableVariable v : vars) {
                term = TermBuilder.DF.all(v, term);
            }
        } else {
            for (Term sk : skolem) {
                if (sk.arity() == 0) {
                    variables.add(sk.op().name().toString());
                } else {
                    throw new IllegalStateException(
                            "Dont know what to do if not readding quantifiers and "
                                    + "trying to a skolem symbol with parameters to "
                                    + "the variable list");
                }
            }
        }
        if (DLOptionBean.INSTANCE.isSimplifyBeforeReduce()) {
            term = MathSolverManager.getCurrentSimplifier().simplify(term);
        }
        term = MathSolverManager.getCurrentQuantifierEliminator().reduce(term,
                variables, new LinkedList<PairOfTermAndQuantifierType>());
        if (DLOptionBean.INSTANCE.isSimplifyAfterReduce()) {
            term = MathSolverManager.getCurrentSimplifier().simplify(term);
        }
        return term;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.RuleOperatingOnWholeSequence#performSearch(de.uka.ilkd.key.logic.Term)
     */
    @Override
    protected void performSearch(Term visited) {
        if (!FOSequence.isFOOperator(visited.op())) {
            addFormula = false;
        }
    }

}
