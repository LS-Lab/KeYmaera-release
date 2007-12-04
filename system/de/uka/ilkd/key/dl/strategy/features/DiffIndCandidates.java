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
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.dl.transitionmodel.DependencyStateGenerator;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.IteratorOfConstrainedFormula;
import de.uka.ilkd.key.logic.IteratorOfTerm;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.ListOfTerm;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.SLListOfTerm;
import de.uka.ilkd.key.logic.Sequent;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.CastFunctionSymbol;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.QuanUpdateOperator;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.sort.AbstractSort;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.rule.updatesimplifier.ArrayOfAssignmentPair;
import de.uka.ilkd.key.rule.updatesimplifier.AssignmentPair;
import de.uka.ilkd.key.rule.updatesimplifier.Update;
import de.uka.ilkd.key.strategy.termgenerator.TermGenerator;
import de.uka.ilkd.key.util.Debug;

/**
 * DiffInd candidates.
 * 
 * @author ap
 */
public class DiffIndCandidates implements TermGenerator {

    public final static TermGenerator INSTANCE = new DiffIndCandidates();

    private final TermBuilder tb = TermBuilder.DF;

    private DiffIndCandidates() {
    }

    public IteratorOfTerm generate(RuleApp app, PosInOccurrence pos, Goal goal) {
        System.out.println("generating for " + app.rule().name());
        Term term = pos.subTerm();
        final Update update = Update.createUpdate(term);
        // unbox from update prefix
        if (term.op() instanceof QuanUpdateOperator) {
            term = ((QuanUpdateOperator) term.op()).target(term);
            if (term.op() instanceof QuanUpdateOperator)
                throw new AssertionError("assume nested updates have been merged");
        }
        if (!(term.op() instanceof Modality
                && term.javaBlock() != null
                && term.javaBlock() != JavaBlock.EMPTY_JAVABLOCK
                && term.javaBlock().program() instanceof StatementBlock)) {
            throw new IllegalArgumentException("inapplicable to " + pos);
        }
        final DiffSystem system = (DiffSystem) ((StatementBlock) term
                .javaBlock().program()).getChildAt(0);
        final Term invariant = system.getInvariant();
        final Term post = term.sub(0);
        final Services services = goal.proof().getServices();

        Set<Term> l = new LinkedHashSet();
        l.add(post);
        l.addAll(indCandidates(update, system, goal.sequent()));
        System.out.println("CANDIDATES .....\n" + l);
        return genericToOld(new ArrayList<Term>(l)).iterator();
    }

    private List<Term> indCandidates(Update update, DiffSystem system, Sequent seq) {
        final Map<ProgramVariable, LinkedHashSet<ProgramVariable>> dep = DependencyStateGenerator
                .generateDependencyMap(system).getDependencies();
        final Map<ProgramVariable, LinkedHashSet<ProgramVariable>> tdep = DependencyStateGenerator
                .createTransitiveClosure(dep);
        // modified variables MV(system)
        final Set<ProgramVariable>  modifieds = new LinkedHashSet<ProgramVariable>();
        for (ProgramVariable s : tdep.keySet()) {
            modifieds.add(s);
        }
        // free variables FV(system)
        final Set<ProgramVariable>  frees = new LinkedHashSet<ProgramVariable>();
        for (LinkedHashSet<ProgramVariable> s : tdep.values()) {
            //@todo frees.addAll(modifieds) as well?
            frees.addAll(s);
        }
        if (!frees.containsAll(modifieds)) {
            System.out.println( "WARNING: dependency of x'=5 should be reflexive. Hence modifieds " + modifieds + " contained in frees " + frees);
            frees.addAll(modifieds);
            assert frees.containsAll(modifieds) : "dependency of x'=5 should be reflexive. Hence modifieds " + modifieds + " contained in frees " + frees;
        }

        // compare variables according to number of dependencies
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
        
        List<Term> result = new LinkedList<Term>();
        PriorityQueue<de.uka.ilkd.key.dl.model.ProgramVariable> depOrder = new PriorityQueue<de.uka.ilkd.key.dl.model.ProgramVariable>(
                tdep.size() + 1, dependencyComparator);
        depOrder.addAll(tdep.keySet());
        while (!depOrder.isEmpty()) {
            // min is the minimal element, i.e. the element which depends on the
            // least number of variables
            ProgramVariable min = depOrder.poll();
            // cluster of variables that min depends on, transitively
            Set<ProgramVariable> cluster = tdep.get(min);
            assert cluster != null : "transitive closure should contain all information for "
                    + min;
            assert depOrder.containsAll(cluster) : "choosing minimum " + dep
                    + " yields that depOrder " + depOrder
                    + " still contains all dependent vars " + cluster;
            // find formulas that only refer to cluster
            Set<Term> matches = selectMatchingCandidates(
                    getMatchingCandidates(update, system, seq),
                    system, cluster, modifieds, frees);
            //@todo all nonempty subsets
            Term candidate = TermBuilder.DF.and(genericToOld(matches));
            result.add(candidate);
            depOrder.removeAll(cluster);
        }
        return result;
    }

    /**
     * Get all possibly matching formulas, regardless of their form.
     * @param seq
     * @param system
     * @return
     */
    private Set<Term> getMatchingCandidates(Update update, DiffSystem system, Sequent seq) {
        //@todo need to conside possible generation renamings by update
        Term invariant = system.getInvariant();
        Set<Term> matches = new LinkedHashSet<Term>();
        ArrayOfAssignmentPair asss = update.getAllAssignmentPairs();
        for (int i = 0; i < asss.size(); i++) {
            AssignmentPair ass = asss.getAssignmentPair(i);
            Term x = ass.locationAsTerm();
            Term t = ass.value();
            //@todo if x occurs in t then can't do that without alpha-renaming stuff
            matches.add(TermBuilder.DF.equals(x, t));
        }
        if (true)
            return matches;
        for (IteratorOfConstrainedFormula i = seq.antecedent().iterator(); i
                .hasNext();) {
            ConstrainedFormula cf = i.next();
            matches.add(cf.formula());
        }
        return matches;
    }
    private Set<Term> selectMatchingCandidates(Set<Term> candidates, DiffSystem system, Set<ProgramVariable> vars,
            Set<ProgramVariable>  modifieds, Set<ProgramVariable>  frees) {
        //@todo need to conside possible generation renamings by update
        Term invariant = system.getInvariant();
        Set<Term> matches = new LinkedHashSet<Term>();
        for (Term fml : candidates) {
            //@todo if (invariant.contains(fml)) continue;
            Set<Operator> occurrences = FOVariableNumberCollector.getVariables(fml);
            occurrences.retainAll(frees);
            if (!vars.containsAll(occurrences)) {
                // variables with more dependencies
                continue;
            }
            occurrences.retainAll(modifieds);
            if (occurrences.isEmpty()) {
                // trivially invariant as nothing changes
                continue;
            } else {
                // FV(fml)\cap MV(system) != EMPTY
                matches.add(fml);
            }
        }
        return matches;
    }
    
    private static ListOfTerm genericToOld(Collection<Term> c) {
        ListOfTerm r = SLListOfTerm.EMPTY_LIST;
        for (Term s : c) {
            r = r.append(s);
        }
        return r;
    }
    private static List<Term> oldToGeneric(ListOfTerm c) {
        List<Term> r = new java.util.ArrayList<Term>(c.size());
        for (IteratorOfTerm i = c.iterator(); i.hasNext(); ) {
            r.add(i.next());
        }
        return r;
    }
}
