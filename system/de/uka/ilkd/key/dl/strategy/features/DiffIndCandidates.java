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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.wolfram.jlink.Expr;

import orbital.util.Setops;

import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.NamedElement;
import de.uka.ilkd.key.dl.model.ProgramVariable;
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
import de.uka.ilkd.key.logic.op.CastFunctionSymbol;
import de.uka.ilkd.key.logic.op.Junctor;
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
        //l.add(post);    // consider diffind itself als diffstrengthening 
        l.addAll(indCandidates(update, system, goal.sequent(), services));
        l.remove(TermBuilder.DF.tt());
        System.out.println("CANDIDATES .....\n" + l);
        return genericToOld(new ArrayList<Term>(l)).iterator();
    }

    private List<Term> indCandidates(Update update, DiffSystem system, Sequent seq, Services services) {
        final Map<ProgramVariable, LinkedHashSet<ProgramVariable>> dep = DependencyStateGenerator
                .generateDependencyMap(system).getDependencies();
        for (Map.Entry<ProgramVariable, LinkedHashSet<ProgramVariable>> s : dep.entrySet()) {
            if (!s.getValue().contains(s.getKey())) {
               // System.out.println("WARNING: " + "dependency of x'=5 should be reflexive. Hence " + s.getKey() + " contained in " + s.getValue());
                s.getValue().add(s.getKey());
                assert s.getValue().contains(s.getKey()) : "dependency of x'=5 should be reflexive. Hence " + s.getKey() + " contained in " + s.getValue();
            }
        }
        final Map<ProgramVariable, LinkedHashSet<ProgramVariable>> tdep = DependencyStateGenerator
                .createTransitiveClosure(dep);
        for (Map.Entry<ProgramVariable, LinkedHashSet<ProgramVariable>> s : tdep.entrySet()) {
            if (!s.getValue().contains(s.getKey())) {
                //System.out.println("WARNING: " + "transitive dependency of x'=5 should be reflexive. Hence " + s.getKey() + " contained in " + s.getValue());
                s.getValue().add(s.getKey());
                assert s.getValue().contains(s.getKey()) : "transitive dependency of x'=5 should be reflexive. Hence " + s.getKey() + " contained in " + s.getValue();
            }
        }
        // modified variables MV(system)
        Set<ProgramVariable>  modifieds = new LinkedHashSet<ProgramVariable>();
        for (ProgramVariable s : tdep.keySet()) {
            modifieds.add(s);
        }
        modifieds = Collections.unmodifiableSet(modifieds);
        // free variables FV(system)
        Set<ProgramVariable>  frees = new LinkedHashSet<ProgramVariable>();
        for (LinkedHashSet<ProgramVariable> s : tdep.values()) {
            //@todo frees.addAll(modifieds) as well?
            frees.addAll(s);
        }
        if (!frees.containsAll(modifieds)) {
            System.out.println( "WARNING: dependency of x'=5 should be reflexive. Hence modifieds " + modifieds + " contained in frees " + frees);
            frees.addAll(modifieds);
            assert frees.containsAll(modifieds) : "dependency of x'=5 should be reflexive. Hence modifieds " + modifieds + " contained in frees " + frees;
        }
        frees = Collections.unmodifiableSet(frees);

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
        
        final Set<Term> possibles = getMatchingCandidates(update, system, seq, services);
        System.out.println("POSSIBLES:  ....\n" + possibles);
        
        List<Term> result = new LinkedList<Term>();
        PriorityQueue<de.uka.ilkd.key.dl.model.ProgramVariable> depOrder = new PriorityQueue<de.uka.ilkd.key.dl.model.ProgramVariable>(
                tdep.size() + 1, dependencyComparator);
        depOrder.addAll(tdep.keySet());
        while (!depOrder.isEmpty()) {
            // min is the minimal element, i.e. the element which depends on the
            // least number of variables
            final ProgramVariable min = depOrder.poll();
            // cluster of variables that min depends on, transitively
            final Set<ProgramVariable> cluster = tdep.get(min);
            assert cluster != null : "transitive closure should contain all information for "
                    + min;
//            assert depOrder.containsAll(cluster) : "choosing minimum " + min + " from " + dep
//                    + " entails that remaining " + depOrder
//                    + " still contains all dependent vars from its cluster " + cluster;
            // find formulas that only refer to cluster
            Set<Term> matches = selectMatchingCandidates(
                    possibles,
                    system, cluster, modifieds, frees);
            System.out.println("    GENERATORS: for " + min + " cluster " + cluster + " are " + matches);
            //@todo all nonempty subsets
            if (!matches.isEmpty()) {
                result.add(TermBuilder.DF.and(genericToOld(matches)));
            }
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
    private Set<Term> getMatchingCandidates(Update update, DiffSystem system, Sequent seq, Services services) {
        //@todo need to conside possible generation renamings by update
        Set<Term> invariant = splitConjuncts(system.getInvariant());
        //System.out.println("  INVARIANT " + invariant + " of " + system.getInvariant() + " of " + system);
        Set<Term> matches = new LinkedHashSet<Term>();
        ArrayOfAssignmentPair asss = update.getAllAssignmentPairs();
        for (int i = 0; i < asss.size(); i++) {
            AssignmentPair ass = asss.getAssignmentPair(i);
            Term x = ass.locationAsTerm();
            assert x.arity()==0 : "only works for atomic locations";
            x = TermBuilder.DF.var((de.uka.ilkd.key.logic.op.ProgramVariable)services.getNamespaces().programVariables().lookup(ass.location().name()));
            Term t = ass.value();
            //System.out.println(x + "@" + x.getClass());
            //@todo if x occurs in t then can't do that without alpha-renaming stuff
            Term equation = TermBuilder.DF.equals(x, t);
            if (!invariant.contains(equation)) {
                //System.out.println("\tnew " + equation + " for " + invariant);
                boolean found = false;
                for (Term inv : invariant) {
                    if (inv.toString().equals(equation.toString())) {
                        //System.out.println(" WARNING: identical printout with different representation " + equation + " and " + inv);
                        //@xxx string comparison is a hack
                        found = true;
                    }
                }
                if (!found)
                    matches.add(equation);
            }
        }
        if (true)
            return matches;
        //@todo respect different update levels
        for (IteratorOfConstrainedFormula i = seq.antecedent().iterator(); i
                .hasNext();) {
            ConstrainedFormula cf = i.next();
            if (!invariant.contains(cf.formula())) {
                matches.add(cf.formula());
            }
        }
        return matches;
    }

    /**
     * Splits a formula along all its conjunctions into a set of its conjuncts.
     * @param form
     * @return
     */
    private Set<Term> splitConjuncts(Term form) {
        Set<Term> conjuncts = new LinkedHashSet<Term>();
        if (form.op() == Junctor.AND) {
            for (int i = 0; i < form.arity(); i++) {
                conjuncts.addAll(splitConjuncts(form.sub(i)));
            }
        } else {
            conjuncts.add(form);
        }
        return conjuncts;
    }
    
    private Set<Term> selectMatchingCandidates(Set<Term> candidates, DiffSystem system, Set<ProgramVariable> myvars,
            Set<ProgramVariable>  mymodifieds, Set<ProgramVariable>  myfrees) {
        Set<Name> vars = projectNames2(myvars); 
        Set<Name> modifieds = projectNames2(mymodifieds); 
        Set<Name> frees = projectNames2(myfrees); 
        //@todo need to conside possible generation renamings by update
        Term invariant = system.getInvariant();
        Set<Term> matches = new LinkedHashSet<Term>();
        for (Term fml : candidates) {
            final Set<Name> occurrences =
                Collections.unmodifiableSet(projectNames(projectProgramVariables(FOVariableNumberCollector.getVariables(fml))));
            if (!vars.containsAll(Setops.intersection(occurrences,frees))) {
                //System.out.println("    skip " + fml + " as " + occurrences + " not in " + vars + " ");
                // variables with more dependencies
                continue;
            }
            if (Setops.intersection(occurrences,modifieds).isEmpty()) {
                //System.out.println("    skip " + fml + " as no change. Changes: " + modifieds + " disjoint from occurrences " + occurrences);
                // trivially invariant as nothing changes
                continue;
            } else {
                // FV(fml)\cap MV(system) != EMPTY
                matches.add(fml);
            }
        }
        return matches;
    }
    
    /**
     * projection to programvariables
     * @param s
     * @return
     */
    private static Set<de.uka.ilkd.key.logic.op.ProgramVariable> projectProgramVariables(Set<Operator> s) {
        Set<de.uka.ilkd.key.logic.op.ProgramVariable> r = new LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable>();
        for (Operator o : s) {
            if (o instanceof de.uka.ilkd.key.logic.op.ProgramVariable) {
                r.add((de.uka.ilkd.key.logic.op.ProgramVariable)o);
            }
        }
        return r;
    }
    /**
     * projects set of named things to the set of its respective names.
     * @param s
     * @return
     */
    private static Set<Name> projectNames(Set<? extends Named> s) {
        Set<Name> r = new LinkedHashSet<Name>();
        for (Named n : s) {
            r.add(n.name());
        }
        return r;
    }
    //@todo refactor to get rid of this duplication
    private static Set<Name> projectNames2(Set<? extends NamedElement> s) {
        Set<Name> r = new LinkedHashSet<Name>();
        for (NamedElement n : s) {
            r.add(n.getElementName());
        }
        return r;
    }
    
    private static ListOfTerm genericToOld(Collection<Term> c) {
        ListOfTerm r = SLListOfTerm.EMPTY_LIST;
        for (Term s : c) {
            r = r.append(s);
        }
        assert r.size() == c.size();
        return r;
    }
    private static List<Term> oldToGeneric(ListOfTerm c) {
        List<Term> r = new java.util.ArrayList<Term>(c.size());
        for (IteratorOfTerm i = c.iterator(); i.hasNext(); ) {
            r.add(i.next());
        }
        assert r.size() == c.size();
        return r;
    }
}
