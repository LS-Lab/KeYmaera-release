/*******************************************************************************
 * Copyright (c) 2012 Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jan-David Quesel - initial API and implementation
 ******************************************************************************/
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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import de.uka.ilkd.key.dl.formulatools.collector.AllCollector;
import de.uka.ilkd.key.dl.formulatools.collector.FilterVariableSet;
import de.uka.ilkd.key.dl.formulatools.collector.FoundItem;
import de.uka.ilkd.key.dl.formulatools.collector.filter.FilterModality;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.Assign;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.RandomAssign;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.ProgramElementName;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.VariableNamer;
import de.uka.ilkd.key.logic.op.AbstractMetaOperator;
import de.uka.ilkd.key.logic.op.LocationVariable;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.logic.op.SVSubstitute;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.MatchConditions;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * Creates an anonymising update for a modifies clause.
 */
public class DLIntroNewAnonUpdateOp extends AbstractMetaOperator {

    public DLIntroNewAnonUpdateOp() {
        super(new Name("#dlintroNewAnonUpdate"), 2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.MetaOperator#calculate(de.uka.ilkd.key.logic.Term,
     *      de.uka.ilkd.key.rule.inst.SVInstantiations,
     *      de.uka.ilkd.key.java.Services)
     */
    public Term calculate(Term term, SVInstantiations svInst, Services services) {
        Term post = term.sub(1);
        
        // Find first modality
        FilterVariableSet set = AllCollector.getItemSet(term.sub(0));
        FoundItem item = set.filterFirst(new FilterModality(null));

        // for (String name : ProgramVariableCollector.INSTANCE.getProgramVariables(term.sub(0))) {
        //    ProgramVariable var = searchFreeVar(services, name);
        //    post = TermFactory.DEFAULT.createUpdateTerm(TermBuilder.DF
        //            .var((ProgramVariable) services.getNamespaces().lookup(
        //                    new Name(name))), TermBuilder.DF.var(var), post);
        // }
        for (String name : getProgramVariables(item.getTerm())) {
        	
            ProgramVariable var = searchFreeVar(services, name);
            post = TermFactory.DEFAULT.createUpdateTerm(TermBuilder.DF
                    .var((ProgramVariable) services.getNamespaces().lookup(
                            new Name(name))), TermBuilder.DF.var(var), post);
        }
        
        return post;

    }

    /**
     * Search a new variable name for the given location variable
     * TODO jdq maybe we should use the {@link VariableNamer}
     * 
     * @param services the services to access the namespaces
     * @param loc the name of the previous variable
     * @return a new programvariable with a fresh name
     */
    private ProgramVariable searchFreeVar(Services services, String loc) {
        int i = 0;
        String newName = services.getNamespaces().getUniqueName(loc);
        LocationVariable locationVariable = new LocationVariable(
                new ProgramElementName(newName), RealLDT.getRealSort());
        services.getNamespaces().programVariables().add(locationVariable);
        return locationVariable;
    }
    
    // added by Timo Michelsen
    private Set<String> getProgramVariables( Term term ) {
    	Set<String> names = new LinkedHashSet<String>();
    	
    	DLProgramElement childAt = (DLProgramElement) ((StatementBlock) term.javaBlock().program()).getChildAt(0);
        names.addAll(getProgramVariables(childAt));
        
        ArrayList<String> inv = new ArrayList<String>(names);
        names.clear();
        for(int i = inv.size() - 1; i >= 0; i--) {
            names.add(inv.get(i));
        }
        assert names.size() == inv.size();

        return names;
    }
    
    // added by Timo Michelsen
    private Collection<String> getProgramVariables(ProgramElement form) {
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        if (form instanceof Dot) {
            Dot dot = (Dot) form;
            if (dot.getChildAt(0) instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
                de.uka.ilkd.key.dl.model.ProgramVariable pv = (de.uka.ilkd.key.dl.model.ProgramVariable) dot
                        .getChildAt(0);
                result.add(pv.getElementName().toString());
            }
        } else if (form instanceof RandomAssign) {
            RandomAssign dot = (RandomAssign) form;
            if (dot.getChildAt(0) instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
                de.uka.ilkd.key.dl.model.ProgramVariable pv = (de.uka.ilkd.key.dl.model.ProgramVariable) dot
                        .getChildAt(0);

                result.add(pv.getElementName().toString());
            }
        } else if (form instanceof Assign) {
            Assign assign = (Assign) form;
            if (assign.getChildAt(0) instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
                de.uka.ilkd.key.dl.model.ProgramVariable pv = (de.uka.ilkd.key.dl.model.ProgramVariable) assign
                        .getChildAt(0);
                result.add(pv.getElementName().toString());
            }
        } else if (form instanceof DLNonTerminalProgramElement) {
            DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) form;
            for (ProgramElement p : dlnpe) {
                result.addAll(getProgramVariables(p));
            }
        }

        return result;
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
