/***************************************************************************
 *   Copyright (C) 2007 by André Platzer                                   *
 *   @informatik.uni-oldenburg.de                                          *
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import orbital.util.Setops;
import de.uka.ilkd.key.dl.formulatools.Prog2LogicConverter;
import de.uka.ilkd.key.dl.formulatools.ReplacementSubst;
import de.uka.ilkd.key.dl.formulatools.TermTools;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.NamedElement;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.dl.model.Star;
import de.uka.ilkd.key.dl.transitionmodel.DependencyStateGenerator;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.IteratorOfConstrainedFormula;
import de.uka.ilkd.key.logic.IteratorOfTerm;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.ListOfTerm;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Named;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.SLListOfTerm;
import de.uka.ilkd.key.logic.Sequent;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Junctor;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.QuanUpdateOperator;
import de.uka.ilkd.key.pp.LogicPrinter;
import de.uka.ilkd.key.pp.NotationInfo;
import de.uka.ilkd.key.pp.ProgramPrinter;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.rule.updatesimplifier.ArrayOfAssignmentPair;
import de.uka.ilkd.key.rule.updatesimplifier.AssignmentPair;
import de.uka.ilkd.key.rule.updatesimplifier.Update;
import de.uka.ilkd.key.strategy.termgenerator.TermGenerator;

/**
 * DiffInd candidates.
 * 
 * @author ap
 */
public class DiffIndCandidates implements TermGenerator {

    public final static TermGenerator INSTANCE = new DiffIndCandidates();

    private static final TermBuilder tb = TermBuilder.DF;

    private DiffIndCandidates() {
    }

    public IteratorOfTerm generate(RuleApp app, PosInOccurrence pos, Goal goal) {
        Term term = pos.subTerm();
        // unbox from update prefix
        while (term.op() instanceof QuanUpdateOperator) {
            term = ((QuanUpdateOperator) term.op()).target(term);
        }
        if (!(term.op() instanceof Modality && term.javaBlock() != null
                && term.javaBlock() != JavaBlock.EMPTY_JAVABLOCK && term
                .javaBlock().program() instanceof StatementBlock)) {
            throw new IllegalArgumentException("inapplicable to " + pos);
        }
        final DLProgram program = (DLProgram) ((StatementBlock) term
                .javaBlock().program()).getChildAt(0);
        Term currentInvariant;
        if (program instanceof DiffSystem) {
            currentInvariant = ((DiffSystem) program).getInvariant();
        } else if (program instanceof Star) {
            currentInvariant = tb.tt();
        } else {
            throw new IllegalArgumentException("Don't know how to handle "
                    + program);
        }
        final Services services = goal.proof().getServices();

        Set<Term> l = new LinkedHashSet<Term>();
        // we do not need post itself as candidate because diffind or strategy
        // can handle this.
        // we only consider sophisticated choices
        // l.add(post); // consider diffind itself als diffstrengthening
        l
                .addAll(indCandidates(goal.sequent(), pos, currentInvariant,
                        services));
        System.out.println("DiffInd CANDIDATES");
        for (Term c : l) {
            try {
                final LogicPrinter lp = new LogicPrinter(new ProgramPrinter(
                        null), NotationInfo.createInstance(), services);
                lp.printTerm(c);
                System.out.print("...  " + lp.toString());
            } catch (Exception ignore) {
                System.out.println("......  " + c.toString());
                ignore.printStackTrace();
            }
        }
        return TermTools.genericToOld(new ArrayList<Term>(l)).iterator();
    }

    /**
     * Determine diffind candidates for the formula at the given position in the
     * given sequent. Find candidates relative to the given current invariant,
     * which is already known to be invariant.
     * 
     * @param seq
     * @param pos
     * @param currentInvariant
     * @param services
     * @return
     */
    protected List<Term> indCandidates(Sequent seq, PosInOccurrence pos,
            Term currentInvariant, Services services) {
        Term term = pos.subTerm();
        final Update update = Update.createUpdate(term);
        // unbox from update prefix
        if (term.op() instanceof QuanUpdateOperator) {
            term = ((QuanUpdateOperator) term.op()).target(term);
            if (term.op() instanceof QuanUpdateOperator)
                throw new AssertionError(
                        "assume that nested updates have been merged");
        }
        if (!(term.op() instanceof Modality && term.javaBlock() != null
                && term.javaBlock() != JavaBlock.EMPTY_JAVABLOCK && term
                .javaBlock().program() instanceof StatementBlock)) {
            throw new IllegalArgumentException("inapplicable to " + pos);
        }
        final DLProgram program = (DLProgram) ((StatementBlock) term
                .javaBlock().program()).getChildAt(0);
        // compute dependency relation
        final Map<ProgramVariable, LinkedHashSet<ProgramVariable>> dep = computeDependencies(program);
        final Map<ProgramVariable, LinkedHashSet<ProgramVariable>> tdep = computeTranstitiveDependencies(dep);
        final Set<ProgramVariable> modifieds = getModifiedVariables(tdep);
        Set<ProgramVariable> frees = getFreeVariables(tdep);
        if (!frees.containsAll(modifieds)) {
            // System.out.println( "WARNING: dependency of x'=5 should be
            // reflexive. Hence modifieds " + modifieds + " contained in frees "
            // + frees);
            // frees.addAll(modifieds);
            assert frees.containsAll(modifieds) : ("dependencies should be reflexive. Hence modified variables "
                    + modifieds + " should be contained in free variables " + frees + " for " + program);
        }
        frees = Collections.unmodifiableSet(frees);

        // find candidates
        final Set<Term> possibles = getMatchingCandidates(update,
                currentInvariant, seq, services, modifieds);
        System.out.println("DiffInd POSSIBLES:  ....\n" + possibles);

        Set<Set<Term>> resultConjuncts = new LinkedHashSet<Set<Term>>();
        // compare variables according to number of dependencies
        PriorityQueue<de.uka.ilkd.key.dl.model.ProgramVariable> depOrder = new PriorityQueue<de.uka.ilkd.key.dl.model.ProgramVariable>(
                tdep.size() + 1, dependencyComparator(tdep));
        depOrder.addAll(tdep.keySet());
        while (!depOrder.isEmpty()) {
            // min is the minimal element, i.e. the element which depends on the
            // least number of variables
            final ProgramVariable min = depOrder.poll();
            // cluster of variables that min depends on, transitively
            final Set<ProgramVariable> cluster = tdep.get(min);
            assert cluster != null : "transitive closure should contain all information for "
                    + min;
            // assert depOrder.containsAll(cluster) : "choosing minimum " + min
            // + " from " + dep
            // + " entails that remaining " + depOrder
            // + " still contains all dependent vars from its cluster " +
            // cluster;
            // find formulas that only refer to cluster
            Set<Term> matches = selectMatchingCandidates(possibles, cluster,
                    modifieds, frees);
            System.out.println("    GENERATORS: for " + min + " cluster "
                    + cluster + " are " + matches);
            if (!matches.isEmpty()) {
                // add all nonempty subsets
                Set<Set<Term>> subsets = Setops.powerset(matches);
                subsets.remove(Collections.EMPTY_SET);
                resultConjuncts.addAll(subsets);
            }
            depOrder.removeAll(cluster);
        }
        // order by size (number of conjuncts), ascending
        List<Set<Term>> orderedResultConjuncts = new ArrayList<Set<Term>>(
                resultConjuncts);
        Collections.sort(orderedResultConjuncts, sizeComparator);
        List<Term> result = new LinkedList<Term>();
        for (Set<Term> s : orderedResultConjuncts) {
            result.add(tb.and(TermTools.genericToOld(s)));
        }
        // remove trivial candidates
        result.remove(tb.ff());
        result.remove(tb.tt());
        return result;
    }

    private static final Comparator<Set<Term>> sizeComparator = new Comparator<Set<Term>>() {
        @Override
        public int compare(Set<Term> arg0, Set<Term> arg1) {
            return arg0.size() - arg1.size();
        }
    };

    //

    /**
     * Get all possibly matching formulas, regardless of their actual form.
     * 
     * @param update
     *                the update characterising the state at which to determine
     *                candidates.
     * @param currentInvariant
     *                the current known invariant, which holds but is not yet
     *                strong enough to imply post.
     * @param seq
     *                the sequent for which we want to find candidates.
     * @return
     */
    private Set<Term> getMatchingCandidates(Update update,
            Term currentInvariant, Sequent seq, Services services,
            Set<ProgramVariable> modifieds) {
        // @todo need to conside possible generation renamings by update
        final ReplacementSubst revert = revertStateChange(update, services,
                modifieds);
        System.out.println("REVERT " + revert);
        Set<Term> invariant = TermTools.splitConjuncts(currentInvariant);
        // System.out.println(" INVARIANT " + invariant + " of " +
        // system.getInvariant() + " of " + system);

        Set<Term> matches = new LinkedHashSet<Term>();
        ArrayOfAssignmentPair asss = update.getAllAssignmentPairs();
        for (int i = 0; i < asss.size(); i++) {
            AssignmentPair ass = asss.getAssignmentPair(i);
            Term xhp = ass.locationAsTerm();
            assert xhp.arity() == 0 : "only works for atomic locations";
            // @todo assert namespaces.unique
            final Term x = tb
                    .var((de.uka.ilkd.key.logic.op.ProgramVariable) services
                            .getNamespaces().programVariables().lookup(
                                    ass.location().name()));
            Term t = ass.value();
            // System.out.println(x + "@" + x.getClass());
            // turn single update into equation
            Term revertedt = revert.apply(t);
            if (TermTools.occursIn(x, revertedt)) {
                // if x occurs in t then can't do that without alpha-renaming stuff
                continue;
            }
            Term equation = tb.equals(x, revertedt);
            assert equation.op() instanceof de.uka.ilkd.key.logic.op.Equality : "different equalities shouldn't be mixed up: "
                    + " "
                    + x
                    + " equaling "
                    + revertedt
                    + " gives "
                    + equation
                    + " with operator " + equation.op();
            if (!TermTools.subsumes(invariant, equation)) {
                matches.add(equation);
            }
        }
        // @todo respect different update levels
        for (IteratorOfConstrainedFormula i = seq.antecedent().iterator(); i
                .hasNext();) {
            final ConstrainedFormula cf = i.next();
            Term fml = cf.formula();
            // @todo if fml contains both a key and a value of
            // revert.getReplacements then skip
            fml = revert.apply(fml);
            if (FOSequence.INSTANCE.isFOFormula(fml)
                    && !TermTools.subsumes(invariant, fml)) {
                matches.add(fml);
            }
        }
        return matches;
    }

    /**
     * Determines the update state reversals of update.
     */
    private ReplacementSubst revertStateChange(Update update,
            Services services, Set<ProgramVariable> modifieds) {
        Map<Term, Term> undos = new HashMap<Term, Term>();
        ArrayOfAssignmentPair asss = update.getAllAssignmentPairs();
        Set<de.uka.ilkd.key.logic.op.ProgramVariable> modifieds2 = Prog2LogicConverter
                .getCorresponding(modifieds, services);
        for (int i = 0; i < asss.size(); i++) {
            AssignmentPair ass = asss.getAssignmentPair(i);
            Term x = ass.locationAsTerm();
            assert x.arity() == 0 : "only works for atomic locations";
            // @todo assert namespaces.unique
            de.uka.ilkd.key.logic.op.ProgramVariable xvar = (de.uka.ilkd.key.logic.op.ProgramVariable) services
                    .getNamespaces().programVariables().lookup(
                            ass.location().name());
            if (!modifieds2.contains(xvar))
                continue;
            x = tb.var(xvar);
            Term t = ass.value();
            if (t.arity() > 0)
                continue;
            undos.put(t, x);
        }
        return ReplacementSubst.create(undos);
    }

    /**
     * Select those candidates that have a promising form.
     * 
     * @param seq
     * @return
     */
    private Set<Term> selectMatchingCandidates(Set<Term> candidates,
            Set<ProgramVariable> myvars, Set<ProgramVariable> mymodifieds,
            Set<ProgramVariable> myfrees) {
        Set<Name> vars = projectNames2(myvars);
        Set<Name> modifieds = projectNames2(mymodifieds);
        Set<Name> frees = projectNames2(myfrees);
        // @todo need to conside possible generation renamings by update
        Set<Term> matches = new LinkedHashSet<Term>();
        for (Term fml : candidates) {
            final Set<Name> occurrences = Collections
                    .unmodifiableSet(TermTools.projectNames(TermTools.projectProgramVariables(FOVariableNumberCollector
                            .getVariables(fml))));
            if (!vars.containsAll(Setops.intersection(occurrences, frees))) {
                // System.out.println(" skip " + fml + " as " + occurrences + "
                // not in " + vars + " ");
                // variables with more dependencies
                continue;
            }
            if (Setops.intersection(occurrences, modifieds).isEmpty()) {
                // System.out.println(" skip " + fml + " as no change. Changes:
                // " + modifieds + " disjoint from occurrences " + occurrences);
                // trivially invariant as nothing changes
                continue;
            } else {
                // FV(fml)\cap MV(system) != EMPTY
                matches.add(fml);
            }
        }
        return matches;
    }

    // helper methods

    /**
     * compare variables according to number of dependencies
     */
    private Comparator<de.uka.ilkd.key.dl.model.ProgramVariable> dependencyComparator(
            final Map<ProgramVariable, LinkedHashSet<ProgramVariable>> tdep) {
        Comparator<de.uka.ilkd.key.dl.model.ProgramVariable> dependencyComparator = new Comparator<de.uka.ilkd.key.dl.model.ProgramVariable>() {
            @Override
            public int compare(de.uka.ilkd.key.dl.model.ProgramVariable o1,
                    de.uka.ilkd.key.dl.model.ProgramVariable o2) {
                int size = tdep.get(o1).size();
                int size2 = tdep.get(o2).size();
                if (size == size2) {
                    return o1.getElementName().toString().compareTo(
                            o2.getElementName().toString());
                } else {
                    return size - size2;
                }
            }
        };
        return dependencyComparator;
    }

    /**
     * Determine free variables set from dependency relation
     */
    private Set<ProgramVariable> getFreeVariables(
            final Map<ProgramVariable, LinkedHashSet<ProgramVariable>> tdep) {
        // free variables FV(system)
        Set<ProgramVariable> frees = new LinkedHashSet<ProgramVariable>();
        for (LinkedHashSet<ProgramVariable> s : tdep.values()) {
            // @todo frees.addAll(modifieds) as well?
            frees.addAll(s);
        }
        return frees;
    }

    /**
     * Determine modified variables set from dependency relation
     */
    private Set<ProgramVariable> getModifiedVariables(
            final Map<ProgramVariable, LinkedHashSet<ProgramVariable>> tdep) {
        // modified variables MV(system)
        Set<ProgramVariable> modifieds = new LinkedHashSet<ProgramVariable>();
        for (ProgramVariable s : tdep.keySet()) {
            modifieds.add(s);
        }
        modifieds = Collections.unmodifiableSet(modifieds);
        return modifieds;
    }

    private Map<ProgramVariable, LinkedHashSet<ProgramVariable>> computeTranstitiveDependencies(
            final Map<ProgramVariable, LinkedHashSet<ProgramVariable>> dep) {
        final Map<ProgramVariable, LinkedHashSet<ProgramVariable>> tdep = DependencyStateGenerator
                .createTransitiveClosure(dep);
        for (Map.Entry<ProgramVariable, LinkedHashSet<ProgramVariable>> s : tdep
                .entrySet()) {
            if (!s.getValue().contains(s.getKey())) {
//                System.out.println("WARNING: transitive dependencies are typically reflexive. Hence "
//                        + s.getKey() + " should be contained in " + s.getValue());
            }
        }
        return tdep;
    }

    private Map<ProgramVariable, LinkedHashSet<ProgramVariable>> computeDependencies(
            final DLProgram program) {
        final Map<ProgramVariable, LinkedHashSet<ProgramVariable>> dep = DependencyStateGenerator
                .generateDependencyMap(program).getDependencies();
        for (Map.Entry<ProgramVariable, LinkedHashSet<ProgramVariable>> s : dep
                .entrySet()) {
            if (!s.getValue().contains(s.getKey())) {
                System.out.println("WARNING: transitive dependencies are typically reflexive. Hence "
                        + s.getKey() + " should be contained in " + s.getValue());
            }
        }
        return dep;
    }

    // @todo refactor to get rid of this duplication
    private static Set<Name> projectNames2(Set<? extends NamedElement> s) {
        Set<Name> r = new LinkedHashSet<Name>();
        for (NamedElement n : s) {
            r.add(n.getElementName());
        }
        return r;
    }

}
