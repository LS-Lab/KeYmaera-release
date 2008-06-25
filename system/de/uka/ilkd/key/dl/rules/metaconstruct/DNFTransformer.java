/***************************************************************************
 *   Copyright (C) 2007 by Jan-David Quesel                                *
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

import java.util.ArrayList;
import java.util.List;

import de.uka.ilkd.key.dl.model.And;
import de.uka.ilkd.key.dl.model.Biimplies;
import de.uka.ilkd.key.dl.model.CompoundFormula;
import de.uka.ilkd.key.dl.model.DLStatementBlock;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Equals;
import de.uka.ilkd.key.dl.model.Exists;
import de.uka.ilkd.key.dl.model.Expression;
import de.uka.ilkd.key.dl.model.Forall;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.Greater;
import de.uka.ilkd.key.dl.model.GreaterEquals;
import de.uka.ilkd.key.dl.model.Implies;
import de.uka.ilkd.key.dl.model.Less;
import de.uka.ilkd.key.dl.model.LessEquals;
import de.uka.ilkd.key.dl.model.Not;
import de.uka.ilkd.key.dl.model.Or;
import de.uka.ilkd.key.dl.model.Predicate;
import de.uka.ilkd.key.dl.model.PredicateTerm;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.Unequals;
import de.uka.ilkd.key.dl.model.VariableDeclaration;
import de.uka.ilkd.key.dl.model.impl.TermFactoryImpl;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.ArrayOfQuantifiableVariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * @author jdq
 * 
 */
public class DNFTransformer extends AbstractDLMetaOperator {

	/**
	 * @param name
	 * @param arity
	 */
	public DNFTransformer() {
		super(new Name("#DLDNF"), 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#sort(de.uka.ilkd.key.logic.Term[])
	 */
	@Override
	public Sort sort(Term[] term) {
		return Sort.FORMULA;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.rules.metaconstruct.AbstractDLMetaOperator#calculate(de.uka.ilkd.key.logic.Term,
	 *      de.uka.ilkd.key.rule.inst.SVInstantiations,
	 *      de.uka.ilkd.key.java.Services)
	 */
	@Override
	public Term calculate(Term term, SVInstantiations svInst, Services services) {
		DiffSystem system = (DiffSystem) ((StatementBlock) term.sub(0)
				.javaBlock().program()).getChildAt(0);
		Term post = term.sub(0).sub(0);
		Modality modality = (Modality) term.sub(0).op();
		try {
			TermFactory tf = TermFactory.getTermFactory(TermFactoryImpl.class,
					services.getNamespaces());
			List<Formula> forms = new ArrayList<Formula>();
			for (ProgramElement p : system) {
				forms.add(createNegationNormalform((Formula) p, false, tf));
			}
			DiffSystem s = tf.createDiffSystem(forms);
			JavaBlock res = JavaBlock.createJavaBlock(new DLStatementBlock(s));
			return de.uka.ilkd.key.logic.TermFactory.DEFAULT.createTerm(
					modality, new Term[] { post },
					new ArrayOfQuantifiableVariable[0], res);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	private Formula createNegationNormalform(Formula t, boolean negated,
			TermFactory tf) {
		if (negated) {
			if (t instanceof Forall) {
				Forall f = (Forall) t;
				return tf.createExists((VariableDeclaration) f.getChildAt(0),
						createNegationNormalform((Formula) ((Forall) t)
								.getChildAt(1), true, tf));
			} else if (t instanceof Exists) {
				Exists f = (Exists) t;
				return tf.createForall((VariableDeclaration) f.getChildAt(0),
						createNegationNormalform((Formula) ((Exists) t)
								.getChildAt(1), true, tf));
			} else if (t instanceof And) {
				return tf.createOr(createNegationNormalform((Formula) ((And) t)
						.getChildAt(0), true, tf), createNegationNormalform(
						(Formula) ((And) t).getChildAt(1), true, tf));
			} else if (t instanceof Or) {
				return tf.createAnd(createNegationNormalform((Formula) ((Or) t)
						.getChildAt(0), true, tf), createNegationNormalform(
						(Formula) ((Or) t).getChildAt(1), true, tf));
			} else if (t instanceof Implies) {
				return tf.createAnd(createNegationNormalform(
						(Formula) ((Implies) t).getChildAt(0), false, tf),
						createNegationNormalform((Formula) ((Implies) t)
								.getChildAt(1), true, tf));
			} else if (t instanceof Biimplies) {
				return tf.createOr(tf.createAnd(createNegationNormalform(
						(Formula) ((Biimplies) t).getChildAt(0), false, tf),
						createNegationNormalform((Formula) ((Biimplies) t)
								.getChildAt(1), true, tf)), tf.createAnd(
						createNegationNormalform((Formula) ((Biimplies) t)
								.getChildAt(0), true, tf),
						createNegationNormalform((Formula) ((Biimplies) t)
								.getChildAt(1), false, tf)));
			} else if (t instanceof Not) {
				return createNegationNormalform((Formula) ((Not) t)
						.getChildAt(0), false, tf);
			} else if (t instanceof PredicateTerm) {
				List<Expression> expr = new ArrayList<Expression>();
				for (int i = 1; i < ((PredicateTerm) t).getChildCount(); i++) {
					expr.add((Expression) t);
				}
				return tf.createPredicateTerm(negate(
						(Predicate) ((PredicateTerm) t).getChildAt(0), tf),
						expr);
			}
		} else {
			if (t instanceof Forall) {
				Forall f = (Forall) t;
				return tf.createForall((VariableDeclaration) f.getChildAt(0),
						createNegationNormalform((Formula) ((Forall) t)
								.getChildAt(1), false, tf));
			} else if (t instanceof Exists) {
				Exists f = (Exists) t;
				return tf.createExists((VariableDeclaration) f.getChildAt(0),
						createNegationNormalform((Formula) ((Exists) t)
								.getChildAt(1), false, tf));
			} else if (t instanceof And) {
				return tf.createAnd(createNegationNormalform((Formula) ((And) t)
						.getChildAt(0), false, tf), createNegationNormalform(
						(Formula) ((And) t).getChildAt(1), false, tf));
			} else if (t instanceof Or) {
				return tf.createOr(createNegationNormalform((Formula) ((Or) t)
						.getChildAt(0), false, tf), createNegationNormalform(
						(Formula) ((Or) t).getChildAt(1), false, tf));
			} else if (t instanceof Implies) {
				return tf.createOr(createNegationNormalform(
						(Formula) ((Implies) t).getChildAt(0), true, tf),
						createNegationNormalform((Formula) ((Implies) t)
								.getChildAt(1), false, tf));
			} else if (t instanceof Biimplies) {
				return tf.createAnd(tf.createAnd(createNegationNormalform(
						(Formula) ((Biimplies) t).getChildAt(0), false, tf),
						createNegationNormalform((Formula) ((Biimplies) t)
								.getChildAt(1), false, tf)), tf.createOr(
						createNegationNormalform((Formula) ((Biimplies) t)
								.getChildAt(0), true, tf),
						createNegationNormalform((Formula) ((Biimplies) t)
								.getChildAt(1), true, tf)));
			} else if (t instanceof Not) {
				return createNegationNormalform((Formula) ((Not) t)
						.getChildAt(0), true, tf);
			} else if (t instanceof PredicateTerm) {
				return t;
			}
		}
		throw new IllegalArgumentException(
				"Could not create negation normal form for " + t);
	}

	/**
	 * @param childAt
	 * @return
	 */
	private Predicate negate(Predicate childAt, TermFactory tf) {
		if (childAt instanceof Equals) {
			return tf.createUnequals();
		} else if (childAt instanceof Unequals) {
			return tf.createEquals();
		} else if (childAt instanceof Less) {
			return tf.createGreaterEquals();
		} else if (childAt instanceof LessEquals) {
			return tf.createGreater();
		} else if (childAt instanceof GreaterEquals) {
			return tf.createLess();
		} else if (childAt instanceof Greater) {
			return tf.createLessEquals();
		}
		throw new IllegalArgumentException("Dont know how to negate " + childAt
				+ " of type " + childAt.getClass());
	}

}
