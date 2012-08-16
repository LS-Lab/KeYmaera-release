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
 * File created 28.02.2007
 */
package de.uka.ilkd.key.dl.rules.metaconstruct;

import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.dl.model.RandomAssign;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Named;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.LocationVariable;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * {@link DLRandomAssign} represents a random assignment to a variable, i.e. an
 * arbitrary real number is assigned to the variable.
 * 
 * @author jdq
 * @since 28.02.2007
 * 
 */
public class DLRandomAssign extends AbstractDLMetaOperator {

    /**
     * 
     */
    private static final Name NAME = new Name("#randomass");

    /**
     * @param name
     * @param arity
     */
    public DLRandomAssign() {
        super(NAME, 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#calculate(de.uka.ilkd.key.logic.Term,
     *      de.uka.ilkd.key.rule.inst.SVInstantiations,
     *      de.uka.ilkd.key.java.Services)
     */
    public Term calculate(Term term, SVInstantiations svInst, Services services) {
        Term result = term.sub(0);
        LogicVariable var = null;
        ProgramVariable progVar = (ProgramVariable) ((RandomAssign) ((StatementBlock) result
                .javaBlock().program()).getChildAt(0)).getChildAt(0);
        de.uka.ilkd.key.logic.op.ProgramVariable lookup = (LocationVariable) services.getNamespaces().programVariables().lookup(progVar.getElementName());
        var = new LogicVariable(new Name(services.getNamespaces()
                .getUniqueName(progVar.getElementName().toString())),
                lookup.sort());
        services.getNamespaces().variables().add(var);
        if (result.op() == Modality.BOX || result.op() == Modality.TOUT) {
            return TermBuilder.DF
                    .all(
                            var,
                            TermBuilder.DF
                                    .tf()
                                    .createUpdateTerm(
                                            TermBuilder.DF
                                                    .var(lookup),
                                            TermBuilder.DF.var(var),
                                            result.sub(0)));
        } else if (result.op() == Modality.DIA) { // TODO: add
            // Modality.Finally
            return TermBuilder.DF
                    .ex(
                            var,
                            TermBuilder.DF
                                    .tf()
                                    .createUpdateTerm(
                                            TermBuilder.DF
                                                    .var(lookup),
                                            TermBuilder.DF.var(var),
                                            result.sub(0)));
        } else {
            throw new IllegalStateException("Unknown modality type: "
                    + result.op());
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#sort(de.uka.ilkd.key.logic.Term[])
     */
    /*@Override*/
    public Sort sort(Term[] term) {
        return Sort.FORMULA;
    }
}
