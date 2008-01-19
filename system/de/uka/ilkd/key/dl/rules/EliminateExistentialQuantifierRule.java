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
 * File created 01.02.2007
 */
package de.uka.ilkd.key.dl.rules;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.PairOfTermAndQuantifierType;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.QuantifierType;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;
import de.uka.ilkd.key.dl.formulatools.ContainsMetaVariableVisitor;
import de.uka.ilkd.key.dl.formulatools.SkolemfunctionTracker;
import de.uka.ilkd.key.dl.formulatools.TermRewriter;
import de.uka.ilkd.key.dl.formulatools.TermTools;
import de.uka.ilkd.key.dl.formulatools.ContainsMetaVariableVisitor.Result;
import de.uka.ilkd.key.dl.formulatools.TermRewriter.Match;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.logic.IteratorOfConstrainedFormula;
import de.uka.ilkd.key.logic.IteratorOfTerm;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.PosInTerm;
import de.uka.ilkd.key.logic.SequentChangeInfo;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Junctor;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.IteratorOfGoal;
import de.uka.ilkd.key.proof.ListOfGoal;
import de.uka.ilkd.key.rule.BuiltInRule;
import de.uka.ilkd.key.rule.Rule;
import de.uka.ilkd.key.rule.RuleApp;

/**
 * This class serves a rule to eliminate existential quantifiers
 * 
 * @author jdq
 * @since 27.08.2007
 * 
 */
public class EliminateExistentialQuantifierRule implements BuiltInRule,
        UnknownProgressRule, IrrevocableRule {

    /**
     * TODO jdq documentation since Aug 27, 2007
     * 
     * @author jdq
     * @since Aug 27, 2007
     * 
     */
    private final class CloseRuleApp implements RuleApp {
        public boolean complete() {
            return true;
        }

        public Constraint constraint() {
            return Constraint.BOTTOM;
        }

        public ListOfGoal execute(Goal goal, Services services) {
            return goal.split(0);
        }

        public PosInOccurrence posInOccurrence() {
            // TODO Auto-generated method stub
            return null;
        }

        public Rule rule() {
            return new Rule() {

                public ListOfGoal apply(Goal goal, Services services,
                        RuleApp ruleApp) {
                    // TODO Auto-generated method stub
                    return null;
                }

                public String displayName() {
                    return "Closed by"
                            + EliminateExistentialQuantifierRule.this
                                    .displayName();
                }

                public Name name() {
                    return new Name("Closed by "
                            + EliminateExistentialQuantifierRule.this.name());
                }

            };
        }
    }

    public static final BuiltInRule INSTANCE = new EliminateExistentialQuantifierRule();

    private boolean unsolvable;

    /**
     * 
     */
    public EliminateExistentialQuantifierRule() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#apply(de.uka.ilkd.key.proof.Goal,
     *      de.uka.ilkd.key.java.Services, de.uka.ilkd.key.rule.RuleApp)
     */
    public synchronized ListOfGoal apply(Goal goal, Services services,
            RuleApp ruleApp) {
        Operator op = ruleApp.posInOccurrence().subTerm().op();
        if (!(op instanceof Metavariable)) {
            throw new IllegalArgumentException(
                    "This rule can only be applied to Metavariables. Found: "
                            + op + "[" + op.getClass() + "]");
        }
        List<Metavariable> variables = new ArrayList<Metavariable>();
        if (ruleApp instanceof ReduceRuleApp) {
            for (String varName : ((ReduceRuleApp) ruleApp).getVariables()) {
                variables.add((Metavariable) services.getNamespaces()
                        .variables().lookup(new Name(varName)));
            }
        }
        if (variables.isEmpty()) {
            variables.add((Metavariable) op);
        }

        // search the variable on all branches

        Set<Goal> goals = new HashSet<Goal>();
        ListOfGoal openGoals = goal.proof().openGoals();
        IteratorOfGoal goalIt = openGoals.iterator();
        while (goalIt.hasNext()) {
            Goal curGoal = goalIt.next();
            IteratorOfConstrainedFormula it = curGoal.sequent().iterator();
            Result result = Result.DOES_NOT_CONTAIN_VAR;
            while (it.hasNext()) {
                ConstrainedFormula next = it.next();
                Result res = ContainsMetaVariableVisitor
                        .containsMetaVariableAndIsFO(variables, next.formula());
                if ((result == Result.CONTAINS_VAR)
                        && (res == Result.CONTAINS_VAR_BUT_CANNOT_APPLY || res == Result.DOES_NOT_CONTAIN_VAR_AND_IS_NOT_FO)) {
                    result = Result.CONTAINS_VAR_BUT_CANNOT_APPLY;
                    break;
                } else if (result == Result.DOES_NOT_CONTAIN_VAR_AND_IS_NOT_FO) {
                    if (res == Result.CONTAINS_VAR) {
                        result = Result.CONTAINS_VAR_BUT_CANNOT_APPLY;
                    }
                } else if (res == Result.CONTAINS_VAR) {
                    result = res;
                } else if (res == Result.CONTAINS_VAR_BUT_CANNOT_APPLY) {
                    result = res;
                    break;
                }
            }
            if (result == Result.CONTAINS_VAR_BUT_CANNOT_APPLY) {
                selectGoalAndShowDialog(curGoal);
                throw new IllegalStateException("All branches that contain "
                        + Arrays.toString(variables.toArray())
                        + " have to be first order");
            } else if (result == Result.CONTAINS_VAR) {
                goals.add(curGoal);
            }
        }

        // construct a conjunction of goal formulas containing implications for
        // sequents
        Term query = TermBuilder.DF.tt();
        Set<Term> skolemSymbols = new HashSet<Term>();
        Set<Term> commonAnte = new HashSet<Term>();
        Set<Term> commonSucc = new HashSet<Term>();
        List<Goal> goalList = new ArrayList<Goal>(goals);
        IteratorOfConstrainedFormula iterator = goalList.get(0).sequent()
                .antecedent().iterator();
        while (iterator.hasNext()) {
            commonAnte.add(iterator.next().formula());
        }
        iterator = goalList.get(0).sequent().succedent().iterator();
        while (iterator.hasNext()) {
            commonSucc.add(iterator.next().formula());
        }
        for (int i = 1; i < goals.size(); i++) {
            if (!commonAnte.isEmpty()) {
                IteratorOfConstrainedFormula it = goalList.get(i).sequent()
                        .antecedent().iterator();

                while (it.hasNext()) {
                    Term formula = it.next().formula();
                    if (!commonAnte.contains(formula)) {
                        commonAnte.remove(formula);
                    }
                }
                if (!commonSucc.isEmpty()) {
                    it = goalList.get(i).sequent().succedent().iterator();
                    while (it.hasNext()) {
                        Term formula = it.next().formula();
                        if (!commonSucc.contains(formula)) {
                            commonSucc.remove(formula);
                        }
                    }
                }
            }
        }
        Term ante = TermBuilder.DF.tt();
        Set<Match> commonMatches = new HashSet<Match>();
        Map<Term, LogicVariable> commonVars = new HashMap<Term, LogicVariable>();
        for (Term t : commonAnte) {
            ante = TermFactory.DEFAULT.createJunctorTermAndSimplify(Op.AND,
                    ante, t);
            final Set<Term> sk = new HashSet<Term>();
            t.execPreOrder(new Visitor() {

                @Override
                public void visit(Term visited) {
                    if (visited.op() instanceof RigidFunction) {
                        RigidFunction f = (RigidFunction) visited.op();
                        if (f.isSkolem()) {
                            sk.add(visited);
                        }
                    }
                }

            });
            List<Term> orderedList = SkolemfunctionTracker.INSTANCE
                    .getOrderedList(sk);
            for (Term s : orderedList) {
                if (s.arity() > 0) {
                    LogicVariable logicVariable = new LogicVariable(new Name(s
                            .op().name()
                            + "$sk"), s.op().sort(new Term[0]));
                    commonVars.put(s, logicVariable);
                    commonMatches.add(new Match((RigidFunction) s.op(),
                            TermBuilder.DF.var(logicVariable)));
                    sk.remove(s);
                }
            }
            skolemSymbols.addAll(sk);
        }
        Term succ = TermBuilder.DF.ff();
        for (Term t : commonSucc) {
            succ = TermFactory.DEFAULT.createJunctorTermAndSimplify(Op.OR,
                    succ, t);
            final Set<Term> sk = new HashSet<Term>();
            t.execPreOrder(new Visitor() {

                @Override
                public void visit(Term visited) {
                    if (visited.op() instanceof RigidFunction) {
                        RigidFunction f = (RigidFunction) visited.op();
                        if (f.isSkolem()) {
                            sk.add(visited);
                        }
                    }
                }

            });
            List<Term> orderedList = SkolemfunctionTracker.INSTANCE
                    .getOrderedList(sk);
            for (Term s : orderedList) {
                if (s.arity() > 0) {
                    LogicVariable logicVariable = new LogicVariable(new Name(s
                            .op().name()
                            + "$sk"), s.op().sort(new Term[0]));
                    commonVars.put(s, logicVariable);
                    commonMatches.add(new Match((RigidFunction) s.op(),
                            TermBuilder.DF.var(logicVariable)));
                    sk.remove(s);
                }
            }
            skolemSymbols.addAll(sk);
        }
        for (Goal g : goals) {
            Set<Term> findSkolemSymbols = findSkolemSymbols(g.sequent()
                    .iterator());

            Term antecendent = TermTools.createJunctorTermNAry(TermBuilder.DF.tt(),
                    Op.AND, g.sequent().antecedent().iterator(), commonAnte);
            Term succendent = TermTools.createJunctorTermNAry(TermBuilder.DF.ff(), Op.OR,
                    g.sequent().succedent().iterator(), commonSucc);
            Set<Match> matches = new HashSet<Match>();
            List<LogicVariable> vars = new ArrayList<LogicVariable>();
            Term imp = TermBuilder.DF.imp(antecendent, succendent);
            List<Term> orderedList = SkolemfunctionTracker.INSTANCE
                    .getOrderedList(findSkolemSymbols);
            for (Term sk : orderedList) {
                if (sk.arity() > 0) {
                    LogicVariable logicVariable = new LogicVariable(new Name(sk
                            .op().name()
                            + "$sk"), sk.op().sort(new Term[0]));
                    vars.add(logicVariable);
                    matches.add(new Match((RigidFunction) sk.op(),
                            TermBuilder.DF.var(logicVariable)));
                    findSkolemSymbols.remove(sk);
                }
            }
            if (!matches.isEmpty()) {
                imp = TermRewriter.replace(imp, matches);
                for (QuantifiableVariable v : vars) {
                    imp = TermBuilder.DF.all(v, imp);
                }
            }

            skolemSymbols.addAll(findSkolemSymbols);
            query = TermBuilder.DF.and(query, imp);
        }

        query = TermBuilder.DF.imp(ante, TermBuilder.DF.or(query, succ));
        if(!commonMatches.isEmpty()) {
            query = TermRewriter.replace(query, commonMatches);
        }
        for (Term sk : SkolemfunctionTracker.INSTANCE.getOrderedList(commonVars
                .keySet())) {
            query = TermBuilder.DF.all(commonVars.get(sk), query);
            //TODO: check if we can avoid adding these variables to the namespace...
            services.getNamespaces().variables().add(commonVars.get(sk));
        }
        List<PairOfTermAndQuantifierType> quantifiers = new LinkedList<PairOfTermAndQuantifierType>();
        for (Metavariable var : variables) {
            quantifiers.add(new PairOfTermAndQuantifierType(TermBuilder.DF
                    .func(var), QuantifierType.EXISTS));
        }
        for (Term t : SkolemfunctionTracker.INSTANCE
                .getOrderedList(skolemSymbols)) {
            quantifiers.add(new PairOfTermAndQuantifierType(t,
                    QuantifierType.FORALL));
        }

        try {
            if (DLOptionBean.INSTANCE.isSimplifyBeforeReduce()) {
                query = MathSolverManager.getCurrentSimplifier()
                        .simplify(query, services.getNamespaces());
            }
            Term resultTerm = MathSolverManager
                    .getCurrentQuantifierEliminator()
                    .reduce(query, quantifiers, services.getNamespaces());
            if (DLOptionBean.INSTANCE.isSimplifyAfterReduce()) {
                resultTerm = MathSolverManager.getCurrentSimplifier().simplify(
                        resultTerm, services.getNamespaces());
            }
            // if there is a result: close all goals beside this one... and
            // progress
            // here
            for (Goal g : goals) {
                if (g != goal) {
                    g.apply(new CloseRuleApp());
                }
            }
            if (resultTerm.equals(TermBuilder.DF.tt())) {
                return goal.split(0);
            }
            ListOfGoal result = goal.split(1);
            removeAllFormulas(result, goal.sequent().succedent().iterator(),
                    false);
            removeAllFormulas(result, goal.sequent().antecedent().iterator(),
                    true);
            SequentChangeInfo info = result.head().sequent().addFormula(
                    new ConstrainedFormula(resultTerm), false, true);
            result.head().setSequent(info);
            return result;
        } catch (RemoteException e) {
            throw new IllegalStateException("Cannot eliminate variable "
                    + Arrays.toString(variables.toArray()), e);
        } catch (UnsolveableException e) {
            unsolvable = true;
            throw new IllegalStateException("Cannot eliminate variable "
                    + Arrays.toString(variables.toArray()), e);
        } catch (SolverException e) {
            throw new IllegalStateException("Cannot eliminate variable "
                    + Arrays.toString(variables.toArray()), e);
        }
    }

    /**
     * TODO jdq documentation since Aug 31, 2007
     * 
     * @param iterator
     * @return
     */
    private Set<Term> findSkolemSymbols(IteratorOfConstrainedFormula iterator) {
        final Set<Term> result = new HashSet<Term>();
        while (iterator.hasNext()) {
            iterator.next().formula().execPreOrder(new Visitor() {

                @Override
                public void visit(Term visited) {
                    if (visited.op() instanceof RigidFunction) {
                        RigidFunction f = (RigidFunction) visited.op();
                        if (f.isSkolem()) {
                            result.add(visited);
                        }
                    }
                }

            });
        }
        return result;
    }

    /**
     * TODO jdq documentation since Aug 29, 2007
     * 
     * @param curGoal
     */
    private void selectGoalAndShowDialog(Goal curGoal) {
        Main.getInstance().mediator().goalChosen(curGoal);
        JOptionPane
                .showMessageDialog(Main.getInstance(),
                        "There was a goal that does not match the condition for applying this rule");
    }

    /**
     * TODO jdq documentation since Aug 27, 2007
     * 
     * @param result
     * @param iterator
     * @param ante
     */
    private void removeAllFormulas(ListOfGoal result,
            IteratorOfConstrainedFormula iterator, boolean ante) {
        while (iterator.hasNext()) {
            ConstrainedFormula next = iterator.next();
            SequentChangeInfo removeFormula = result
                    .head()
                    .sequent()
                    .removeFormula(
                            new PosInOccurrence(next, PosInTerm.TOP_LEVEL, ante));
            result.head().setSequent(removeFormula);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.BuiltInRule#isApplicable(de.uka.ilkd.key.proof.Goal,
     *      de.uka.ilkd.key.logic.PosInOccurrence,
     *      de.uka.ilkd.key.logic.Constraint)
     */
    public boolean isApplicable(Goal goal, PosInOccurrence pio,
            Constraint userConstraint) {
        if (MathSolverManager.isQuantifierEliminatorSet()
                && ((DLOptionBean.INSTANCE.isSimplifyBeforeReduce() || DLOptionBean.INSTANCE
                        .isSimplifyAfterReduce()) ? MathSolverManager
                        .isSimplifierSet() : true)) {
            return pio != null && pio.subTerm() != null
                    && pio.subTerm().op() instanceof Metavariable;
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#displayName()
     */
    public String displayName() {
        return "Eliminate Existential Quantifier";
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#name()
     */
    public Name name() {
        return new Name("Eliminate Existential Quantifier");
    }

    @Override
    public String toString() {
        return displayName();
    }

 
    /**
     * @return the unsolvable
     */
    public boolean isUnsolvable() {
        return unsolvable;
    }

}
