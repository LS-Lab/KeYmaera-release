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
import java.util.*;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.PairOfTermAndQuantifierType;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.formulatools.SkolemfunctionTracker;
import de.uka.ilkd.key.dl.formulatools.TermRewriter;
import de.uka.ilkd.key.dl.formulatools.TermRewriter.Match;
import de.uka.ilkd.key.dl.formulatools.collector.AllCollector;
import de.uka.ilkd.key.dl.formulatools.collector.FilterVariableSet;
import de.uka.ilkd.key.dl.formulatools.collector.filter.FilterNotArity;
import de.uka.ilkd.key.dl.formulatools.collector.filter.FilterNotNumber;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.strategy.features.FOSequence;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.*;
import de.uka.ilkd.key.logic.op.*;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.RuleFilter;
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
        RuleFilter, UnknownProgressRule {

    private Set<Term> skolemSymbols;

    private LinkedHashSet<String> quantifiedVariables;

    private final Set<Term> TRIVIALS;

    /**
     * @param formulaContainsSearchSymbolDefault
     */
    public ReduceRule() {
        super(true);
        TRIVIALS = new LinkedHashSet<Term>(2);
        TRIVIALS.add(TermBuilder.DF.tt());
        TRIVIALS.add(TermBuilder.DF.ff());
    }

    public static final ReduceRule INSTANCE = new ReduceRule();

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.uka.ilkd.key.rule.BuiltInRule#isApplicable(de.uka.ilkd.key.proof.Goal,
     * de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.logic.Constraint)
     */
    public boolean isApplicable(Goal goal, Constraint userConstraint) {
        return MathSolverManager.isQuantifierEliminatorSet()
                && ((DLOptionBean.INSTANCE.isSimplifyBeforeReduce() || DLOptionBean.INSTANCE
                        .isSimplifyAfterReduce()) ? MathSolverManager
                        .isSimplifierSet() : true);
    }

    public boolean isApplicable(Goal goal, PosInOccurrence pio,
            Constraint userConstraint) {
        return isApplicable(goal, userConstraint);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#displayName()
     */
    public String displayName() {
        return "Quantifier Elimination";
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
    /* @Override */
    public String toString() {
        return displayName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.uka.ilkd.key.dl.RuleOperatingOnWholeSequence#apply(de.uka.ilkd.key
     * .proof.Goal, de.uka.ilkd.key.java.Services, de.uka.ilkd.key.rule.RuleApp)
     */
    /* @Override */
    public synchronized ImmutableList<Goal> apply(Goal goal, Services services,
            RuleApp ruleApp) {
        quantifiedVariables = new LinkedHashSet<String>();
        skolemSymbols = new LinkedHashSet<Term>();
        return super.apply(goal, services, ruleApp);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.uka.ilkd.key.dl.RuleOperatingOnWholeSequence#visit(de.uka.ilkd.key
     * .logic.Term)
     */
    /* @Override */
    public void visit(Term visited) {
        if (visited.op() instanceof RigidFunction) {
            RigidFunction sk = (RigidFunction) visited.op();
            if (sk.isSkolem()) {
                skolemSymbols.add(visited);
            }
        } else if (visited.op() instanceof Quantifier) {
            ImmutableArray<QuantifiableVariable> varsBoundHere = visited
                    .varsBoundHere(0);
            for (int i = 0; i < varsBoundHere.size(); i++) {
                quantifiedVariables.add(varsBoundHere.get(i).name().toString());
            }
        }
        super.visit(visited);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.uka.ilkd.key.dl.RuleOperatingOnWholeSequence#performQuery(de.uka.ilkd
     * .key.logic.Term, de.uka.ilkd.key.dl.IMathSolver)
     */
    /* @Override */
    protected Term performQuery(Term term, long timeout)
            throws RemoteException, SolverException {

        final List<String> variables = getVariables();
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
            Set<Match> matches = new LinkedHashSet<Match>();
            List<LogicVariable> vars = new ArrayList<LogicVariable>();
            for (Term sk : skolem) {
                LogicVariable logicVariable = new LogicVariable(new Name(sk
                        .op().name() + "$sk"), sk.op().sort(new Term[0]));
                vars.add(logicVariable);
                matches.add(new Match((RigidFunction) sk.op(), TermBuilder.DF
                        .var(logicVariable)));
                variables.add(logicVariable.name().toString());
            }
            if(DLOptionBean.INSTANCE.isUniversalClosureOnQE()) { // universal closure
                final HashSet<Term> candidates = new HashSet<Term>();
                term.execPreOrder(new Visitor() {
                    @Override
                    public void visit(Term sk) {
                        if(sk.arity() == 0) {
                            if (sk.op() instanceof RigidFunction && !variables.contains(sk.op().name()) && !variables.contains(sk.op().name() + "$sk")) {
                                RigidFunction fn = (RigidFunction) sk.op();
                                if(!fn.isMathFunction()) {
                                    try {
                                        Double.parseDouble(fn.name().toString());
                                    } catch(NumberFormatException e) {
                                        candidates.add(sk);
                                    }
                                }
                            } else if (sk.op() instanceof ProgramVariable) {
                                candidates.add(sk);
                            }
                        }
                    }
                });
                for(Term sk: candidates) {
                    LogicVariable logicVariable = new LogicVariable(new Name(sk
                            .op().name() + "$closure"), sk.op().sort(new Term[0]));
                    if(!variables.contains(logicVariable.name().toString())) {
                        vars.add(logicVariable);
                        if(sk.op() instanceof RigidFunction) {
                            matches.add(new Match((RigidFunction) sk.op(), TermBuilder.DF
                                    .var(logicVariable)));
                        } else if(sk.op() instanceof ProgramVariable) {
                            matches.add(new Match((ProgramVariable) sk.op(), TermBuilder.DF
                                    .var(logicVariable)));
                        } else {
                            throw new IllegalStateException("Found strange object to quantify " + sk + " in " + candidates);
                        }
                        variables.add(logicVariable.name().toString());
                    }
                }
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
                                    + "trying to add a skolem symbol with parameters to "
                                    + "the variable list");
                }
            }
        }
        if (DLOptionBean.INSTANCE.isSimplifyBeforeReduce()) {
            term = MathSolverManager.getCurrentSimplifier().simplify(term,
                    getServices().getNamespaces());
        }
        if (TRIVIALS.contains(term)) {
            return term;
        }
        term = MathSolverManager.getCurrentQuantifierEliminator().reduce(term,
                variables, new LinkedList<PairOfTermAndQuantifierType>(),
                getServices().getNamespaces(), timeout);
        if (TRIVIALS.contains(term)) {
            return term;
        }
        if (DLOptionBean.INSTANCE.isSimplifyAfterReduce()) {
            term = MathSolverManager.getCurrentSimplifier().simplify(term,
                    getServices().getNamespaces());
        }
        return term;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.uka.ilkd.key.dl.RuleOperatingOnWholeSequence#performSearch(de.uka.
     * ilkd.key.logic.Term)
     */
    /* @Override */
    protected void performSearch(Term visited) {
        if (!FOSequence.isFOOperator(visited.op())
                || FOSequence.isFunctionWithDifferentSort(visited,
                        RealLDT.getRealSort())) {
            addFormula = false;
        }
    }

}
