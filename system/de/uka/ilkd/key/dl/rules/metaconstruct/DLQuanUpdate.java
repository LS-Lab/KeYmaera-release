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

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.dl.formulatools.Prog2LogicConverter;
import de.uka.ilkd.key.dl.formulatools.ReplaceVisitor;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.Assign;
import de.uka.ilkd.key.dl.model.Expression;
import de.uka.ilkd.key.dl.model.NamedElement;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.dl.model.Quantified;
import de.uka.ilkd.key.dl.model.RandomAssign;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.VariableDeclaration;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Named;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.UpdateFactory;
import de.uka.ilkd.key.logic.op.LocationVariable;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;
import de.uka.ilkd.key.rule.updatesimplifier.UpdateSimplifierTermFactory;

/**
 * {@link DLQuanUpdate} represents a random assignment to a variable, i.e. an
 * arbitrary real number is assigned to the variable.
 * 
 * @author jdq
 * @since 28.02.2007
 * 
 */
public class DLQuanUpdate extends AbstractDLMetaOperator {

    /**
     * 
     */
    private static final Name NAME = new Name("#quanupdate");

    /**
     * @param name
     * @param arity
     */
    public DLQuanUpdate() {
        super(NAME, 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.uka.ilkd.key.logic.op.AbstractMetaOperator#calculate(de.uka.ilkd.key
     * .logic.Term, de.uka.ilkd.key.rule.inst.SVInstantiations,
     * de.uka.ilkd.key.java.Services)
     */
    public Term calculate(Term term, SVInstantiations svInst, Services services) {
        Term result = term.sub(0);
        LogicVariable var = null;
        VariableDeclaration decl = (VariableDeclaration) ((Quantified) ((StatementBlock) result
                .javaBlock().program()).getChildAt(0)).getChildAt(0);
        Assign a = (Assign) ((Quantified) ((StatementBlock) result.javaBlock()
                .program()).getChildAt(0)).getChildAt(1);
        Expression left = (Expression) a.getChildAt(0);
        Expression right = (Expression) a.getChildAt(1);
        Name name2 = ((NamedElement) decl.getChildAt(1))
                .getElementName();
        String prefix = name2.toString();
        name2 = new Name(services.getNamespaces().getUniqueName(prefix));
        Sort sort = (Sort) services.getNamespaces()
                .sorts().lookup(decl.getType().getElementName());
        var = new LogicVariable(name2, sort);
        services.getNamespaces().variables().add(var);
        if(!name2.toString().equals(prefix)) {
            // substitute the name in the assignment
            TermFactory tf;
            try {
                tf = TermFactory.getTermFactory(
                        DLOptionBean.INSTANCE.getTermFactoryClass(),
                        services.getNamespaces());
            Map<QuantifiableVariable, Term> singletonMap = Collections.singletonMap(
                        (QuantifiableVariable) new LogicVariable(new Name(prefix),
                                sort), TermBuilder.DF.var(var));
            left = (Expression) ReplaceVisitor.convert(left, singletonMap, tf);
            right = (Expression) ReplaceVisitor.convert(right, singletonMap, tf);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException("Cannot create termfactory!", e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Cannot create termfactory!", e);
            } catch (InstantiationException e) {
                throw new IllegalStateException("Cannot create termfactory!", e);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Cannot create termfactory!", e);
            }
        }
        if (result.op() == Modality.BOX || result.op() == Modality.TOUT) {
            ImmutableArray<QuantifiableVariable> vars[] = new ImmutableArray[1];
            vars[0] = new ImmutableArray<QuantifiableVariable>(var);
            return TermBuilder.DF.tf()
                    .createQuanUpdateTerm(vars,
                            new Term[] { TermBuilder.DF.tt() },
                            new Term[] { Prog2LogicConverter.convert(left,
                                    services) },
                            new Term[] { Prog2LogicConverter.convert(right,
                                    services) }, result.sub(0));
//        } else if (result.op() == Modality.DIA) { // TODO: add
//            // Modality.Finally
//            return TermBuilder.DF.ex(
//                    var,
//                    TermBuilder.DF.tf().createUpdateTerm(
//                            TermBuilder.DF.var(lookup),
//                            TermBuilder.DF.var(var), result.sub(0)));
        } else {
            throw new IllegalStateException("Unknown modality type: "
                    + result.op());
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.uka.ilkd.key.logic.op.AbstractMetaOperator#sort(de.uka.ilkd.key.logic
     * .Term[])
     */
    /* @Override */
    public Sort sort(Term[] term) {
        return Sort.FORMULA;
    }
}
