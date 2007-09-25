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

import de.uka.ilkd.key.dl.formulatools.ProgramVariableCollector;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.java.Services;
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
    public Term calculate(Term term, SVInstantiations svInst, Services services) {
        Term post = term.sub(1);

        for (String name : ProgramVariableCollector.INSTANCE
                .getProgramVariables(term.sub(0))) {
            LogicVariable var = searchFreeVar(services, name);
            post = TermBuilder.DF.all(var, TermFactory.DEFAULT
                    .createUpdateTerm(TermBuilder.DF.var((ProgramVariable) services.getNamespaces().lookup(
                                    new Name(name))), TermBuilder.DF.var(var),
                            post));
        }
        return post;

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
        } while (services.getNamespaces().variables().lookup(new Name(newName)) != null || services.getNamespaces().programVariables().lookup(new Name(newName)) != null);
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
