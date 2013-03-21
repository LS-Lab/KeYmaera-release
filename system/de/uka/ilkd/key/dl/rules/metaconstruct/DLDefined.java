/***************************************************************************
 *   Copyright (C) 2012 by Jan David Quesel                                *
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
package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.Constant;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.DLStatementBlock;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Div;
import de.uka.ilkd.key.dl.model.Exp;
import de.uka.ilkd.key.dl.model.Expression;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.FunctionTerm;
import de.uka.ilkd.key.dl.model.Or;
import de.uka.ilkd.key.dl.model.PredicateTerm;
import de.uka.ilkd.key.dl.model.Quest;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.impl.TermFactoryImpl;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.parser.NumberCache;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * This meta-operator is used to check that a given expression does not have any
 * zero denominators
 * 
 * @author jdq
 */
public class DLDefined extends AbstractDLMetaOperator {

    public static final Name NAME = new Name("#defined");

    public DLDefined() {
        super(NAME, 1);
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.uka.ilkd.key.logic.op.AbstractMetaOperator#calculate(de.uka.ilkd.key
     * .logic.Term, de.uka.ilkd.key.rule.inst.SVInstantiations,
     * de.uka.ilkd.key.java.Services)
     */
    public Term calculate(Term term, SVInstantiations svInst, Services services) {
        Expression expr = (Expression) ((PredicateTerm) ((Quest) ((StatementBlock) term.sub(0)
                .javaBlock().program()).getChildAt(0)).getChildAt(0)).getChildAt(1);
        try {
            TermFactory tf = TermFactory.getTermFactory(
                    DLOptionBean.INSTANCE.getTermFactoryClass(),
                    services.getNamespaces());
            List<Formula> denominators = findDenominators(tf, expr);
            Formula result = null;
            // create big conjunction
            for(Formula f: denominators) {
                if(result == null) {
                    result = f;
                } else {
                    result = tf.createAnd(result, f);
                }
            }
            if(result == null) {
                // we have no denominators
                return TermBuilder.DF.tt();
            } else {
				System.out.println("term was: " + term);
				System.out.println("output is: " + result);
                return services.getTypeConverter().convertToLogicElement(result);
            }
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Cannot create termfactory", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot create termfactory", e);
        } catch (InstantiationException e) {
            throw new IllegalStateException("Cannot create termfactory", e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Cannot create termfactory", e);
        }
    }

    /** 
     * Construct the set/list of all formulas t!=0 for which a division by t occurs in expr.
     * Divisions include proper /t or things like t^-4.
     * @param expr
     * @return
     */
    private List<Formula> findDenominators(TermFactory tf, Expression expr) {
        List<Formula> result = new ArrayList<Formula>();
        if (expr instanceof FunctionTerm) {
            FunctionTerm ft = (FunctionTerm) expr;
            if (ft.getChildAt(0) instanceof Div) {
                Constant zero = tf.createConstant(new BigDecimal(0));
                // add the denominator
                List<Expression> children = new ArrayList<Expression>();
                children.add((Expression) ft.getChildAt(2));
                children.add(zero);
                result.add(tf.createPredicateTerm(tf.createUnequals(), children));
            } else if (ft.getChildAt(0) instanceof Exp) {
                // if the exponent is negative the base has to be non-zero
                Constant zero = tf.createConstant(new BigDecimal(0));
                List<Expression> children = new ArrayList<Expression>();
                children.add((Expression) ft.getChildAt(2));
                children.add(zero);
                List<Expression> children2 = new ArrayList<Expression>();
                children2.add((Expression) ft.getChildAt(1));
                children2.add(zero);
                result.add(tf.createImpl(
                        tf.createPredicateTerm(tf.createLess(), children),
                        tf.createPredicateTerm(tf.createUnequals(), children2)));
            }
            // recursively add more denominators
            for(int i = 1; i < ft.getChildCount(); i++) {
                result.addAll(findDenominators(tf, (Expression) ft.getChildAt(i)));
            }
        }
        return result;
    }
}
