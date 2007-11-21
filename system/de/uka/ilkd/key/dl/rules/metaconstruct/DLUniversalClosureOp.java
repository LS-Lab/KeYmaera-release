// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License.
// See LICENSE.TXT for details.
//
/*
 * Created on 22.12.2004
 */
package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.transitionmodel.DependencyState;
import de.uka.ilkd.key.dl.transitionmodel.DependencyStateGenerator;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.op.AbstractMetaOperator;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.logic.op.SVSubstitute;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.MatchConditions;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * Creates an anonymising update for a modifies clause.
 */
public class DLUniversalClosureOp extends AbstractMetaOperator {

    public DLUniversalClosureOp() {
        super(new Name("#dlUniversalClosure"), 2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.MetaOperator#calculate(de.uka.ilkd.key.logic.Term,
     *      de.uka.ilkd.key.rule.inst.SVInstantiations,
     *      de.uka.ilkd.key.java.Services)
     */
    @SuppressWarnings("unchecked")
    public Term calculate(Term term, SVInstantiations svInst, Services services) {
        Term post = term.sub(1);

        DependencyState depState = DependencyStateGenerator
                .generateDependencyMap((DLProgram) ((StatementBlock) term
                        .sub(0).javaBlock().program()).getChildAt(0));
        Map<de.uka.ilkd.key.dl.model.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable>> generateDependencyMap = depState
                .getDependencies();
        // FileWriter writer;
        // try {
        // writer = new FileWriter("/tmp/depgraph.dot");
        //
        // writer.write("digraph program\n");
        // writer.write("{\n");
        // for (de.uka.ilkd.key.dl.model.ProgramVariable var :
        // generateDependencyMap
        // .keySet()) {
        // writer.write(var.getElementName().toString() + ";\n");
        // }
        // for (de.uka.ilkd.key.dl.model.ProgramVariable var :
        // generateDependencyMap
        // .keySet()) {
        // LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable> deps =
        // generateDependencyMap
        // .get(var);
        // for (de.uka.ilkd.key.dl.model.ProgramVariable dvar : deps) {
        // writer.write(dvar.getElementName().toString() + " -> "
        // + var.getElementName().toString() + ";\n");
        // }
        // }
        //
        // writer.write("}\n");
        // writer.flush();
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        final Map<de.uka.ilkd.key.dl.model.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable>> transitiveClosure = createTransitiveClosure(generateDependencyMap);

        final Map<de.uka.ilkd.key.dl.model.ProgramVariable, Set<de.uka.ilkd.key.dl.model.ProgramVariable>> inverseTransitiveClosure = new HashMap<de.uka.ilkd.key.dl.model.ProgramVariable, Set<de.uka.ilkd.key.dl.model.ProgramVariable>>();
        for (de.uka.ilkd.key.dl.model.ProgramVariable var : generateDependencyMap
                .keySet()) {
            LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable> deps = transitiveClosure
                    .get(var);
            for (de.uka.ilkd.key.dl.model.ProgramVariable v : deps) {
                Set<de.uka.ilkd.key.dl.model.ProgramVariable> set = inverseTransitiveClosure
                        .get(v);
                if (set == null) {
                    inverseTransitiveClosure
                            .put(
                                    v,
                                    new HashSet<de.uka.ilkd.key.dl.model.ProgramVariable>());
                }
                inverseTransitiveClosure.get(v).add(var);
            }
        }
        Comparator<de.uka.ilkd.key.dl.model.ProgramVariable> comparator = new Comparator<de.uka.ilkd.key.dl.model.ProgramVariable>() {

            @Override
            public int compare(de.uka.ilkd.key.dl.model.ProgramVariable o1,
                    de.uka.ilkd.key.dl.model.ProgramVariable o2) {
                int size = inverseTransitiveClosure.get(o1).size();
                int size2 = inverseTransitiveClosure.get(o2).size();
                if (size == size2) {
                    return o1.getElementName().toString().compareTo(
                            o2.getElementName().toString());
                } else {
                    return size2 - size;
                }
            }

        };
        PriorityQueue<de.uka.ilkd.key.dl.model.ProgramVariable> variableOrder = new PriorityQueue<de.uka.ilkd.key.dl.model.ProgramVariable>(
                inverseTransitiveClosure.size()+1, comparator);
        variableOrder.addAll(inverseTransitiveClosure.keySet());
        ArrayList<de.uka.ilkd.key.dl.model.ProgramVariable> programVariables = new ArrayList<de.uka.ilkd.key.dl.model.ProgramVariable>();
        while (!variableOrder.isEmpty()) {
            // max is the maximal element, i.e. the element on which most
            // variables depend
            de.uka.ilkd.key.dl.model.ProgramVariable max = variableOrder.poll();
            int i = 0;
            for (i = 0; i < programVariables.size(); i++) {
                if (transitiveClosure.get(programVariables.get(i)) != null
                        && transitiveClosure.get(programVariables.get(i))
                                .contains(max)) {
                    break;
                }
            }
            programVariables.add(i, max);
            TreeSet<de.uka.ilkd.key.dl.model.ProgramVariable> orderedDeps = new TreeSet<de.uka.ilkd.key.dl.model.ProgramVariable>(
                    new Comparator<de.uka.ilkd.key.dl.model.ProgramVariable>() {

                        @Override
                        public int compare(
                                de.uka.ilkd.key.dl.model.ProgramVariable o1,
                                de.uka.ilkd.key.dl.model.ProgramVariable o2) {
                            LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable> linkedHashSet = transitiveClosure
                                    .get(o1);
                            LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable> linkedHashSet2 = transitiveClosure
                                    .get(o2);

                            if (linkedHashSet.contains(o2)) {
                                return 1;
                            } else if (linkedHashSet2.contains(o1)) {
                                return -1;
                            }
                            // this could cause interleaving
                            Set<de.uka.ilkd.key.dl.model.ProgramVariable> set = inverseTransitiveClosure
                                    .get(o1);
                            Set<de.uka.ilkd.key.dl.model.ProgramVariable> set2 = inverseTransitiveClosure
                                    .get(o2);
                            if (set != null && set2 != null) {
                                int size = set.size();
                                int size2 = set2.size();
                                if (size != size2) {
                                    return size - size2;
                                }
                            }
                            return o1.getElementName().toString().compareTo(
                                    o2.getElementName().toString());

                        }

                    });
            // the elements that reference "max" also include those elements
            // referencing an element that references "max"
            Set<de.uka.ilkd.key.dl.model.ProgramVariable> backwardDeps = inverseTransitiveClosure
                    .get(max);
            orderedDeps.addAll(backwardDeps);

            for (de.uka.ilkd.key.dl.model.ProgramVariable var : orderedDeps) {
                if (!programVariables.contains(var)) {
                    for (i = 0; i < programVariables.size(); i++) {
                        if ((transitiveClosure.get(programVariables.get(i)) != null && transitiveClosure
                                .get(programVariables.get(i)).contains(var))
                                || (inverseTransitiveClosure.get(var) != null && inverseTransitiveClosure
                                        .get(var).contains(
                                                programVariables.get(i)))) {
                            break;
                        }
                    }
                    programVariables.add(i, var);
                    variableOrder.remove(var);
                }
            }
            // find out if need to recreate variableOrder due to isReferencedBy
        }

        // add variables without dependencies
        Set<de.uka.ilkd.key.dl.model.ProgramVariable> freeVars = new TreeSet<de.uka.ilkd.key.dl.model.ProgramVariable>(
                new Comparator<de.uka.ilkd.key.dl.model.ProgramVariable>() {

                    @Override
                    public int compare(
                            de.uka.ilkd.key.dl.model.ProgramVariable o1,
                            de.uka.ilkd.key.dl.model.ProgramVariable o2) {
                        return o1.getElementName().toString().compareTo(
                                o2.getElementName().toString());
                    }

                });

        for (de.uka.ilkd.key.dl.model.ProgramVariable var : transitiveClosure
                .keySet()) {
            if (!programVariables.contains(var)) {
                freeVars.add(var);
            }
        }
        programVariables.addAll(freeVars);

        Collections.reverse(programVariables);

        // Set<String> programVariables = ProgramVariableCollector.INSTANCE
        // .getProgramVariables(term.sub(0));
        for (de.uka.ilkd.key.dl.model.ProgramVariable pvar : programVariables) {
            if (transitiveClosure.keySet().contains(pvar)
                    && !(depState.getWriteBeforeReadList().get(pvar) != null && depState
                            .getWriteBeforeReadList().get(pvar))) {
                String name = pvar.getElementName().toString();
                LogicVariable var = searchFreeVar(services, name);
                post = TermBuilder.DF.all(var, TermFactory.DEFAULT
                        .createUpdateTerm(TermBuilder.DF
                                .var((ProgramVariable) services.getNamespaces()
                                        .lookup(new Name(name))),
                                TermBuilder.DF.var(var), post));
            }
        }
        return post;

    }

    /**
     * TODO jdq documentation since Nov 16, 2007
     * 
     * @param generateDependencyMap
     * @param transitiveClosure
     */
    private Map<de.uka.ilkd.key.dl.model.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable>> createTransitiveClosure(
            Map<de.uka.ilkd.key.dl.model.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable>> generateDependencyMap) {
        final Map<de.uka.ilkd.key.dl.model.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable>> transitiveClosure = new LinkedHashMap<de.uka.ilkd.key.dl.model.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable>>();
        boolean changed = true;
        while (changed) {
            changed = false;
            for (de.uka.ilkd.key.dl.model.ProgramVariable var : generateDependencyMap
                    .keySet()) {
                LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable> clone = transitiveClosure
                        .get(var);
                LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable> deps = transitiveClosure
                        .get(var);
                if (clone == null) {
                    deps = generateDependencyMap.get(var);
                    clone = new LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable>();
                    clone.addAll(deps);

                    transitiveClosure.put(var, clone);
                }
                for (de.uka.ilkd.key.dl.model.ProgramVariable dvar : new HashSet<de.uka.ilkd.key.dl.model.ProgramVariable>(
                        deps)) {
                    LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable> otherDeps = generateDependencyMap
                            .get(dvar);
                    if (otherDeps != null) {
                        changed |= transitiveClosure.get(var).addAll(otherDeps);
                    }
                }
            }
        }
        return transitiveClosure;
    }

    /**
     * TODO jdq documentation since Aug 21, 2007
     * 
     * @param services
     * @param loc
     * @return
     */
    private LogicVariable searchFreeVar(Services services, String loc) {
        int i = 0;
        String newName = null;
        do {
            newName = loc + "_" + i++;
        } while (services.getNamespaces().variables().lookup(new Name(newName)) != null
                || services.getNamespaces().programVariables().lookup(
                        new Name(newName)) != null);
        return new LogicVariable(new Name(newName), RealLDT.getRealSort());
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.Operator#validTopLevel(de.uka.ilkd.key.logic.Term)
     */
    public boolean validTopLevel(Term term) {
        return term.arity() == arity() && term.sub(1).sort() == Sort.FORMULA;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.Operator#sort(de.uka.ilkd.key.logic.Term[])
     */
    public Sort sort(Term[] term) {
        return term[1].sort();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.Operator#isRigid(de.uka.ilkd.key.logic.Term)
     */
    public boolean isRigid(Term term) {
        return false;
    }

    /**
     * (non-Javadoc) by default meta operators do not match anything
     * 
     * @see de.uka.ilkd.key.logic.op.Operator#match(SVSubstitute,
     *      de.uka.ilkd.key.rule.MatchConditions, de.uka.ilkd.key.java.Services)
     */
    public MatchConditions match(SVSubstitute subst, MatchConditions mc,
            Services services) {
        return null;
    }
}
