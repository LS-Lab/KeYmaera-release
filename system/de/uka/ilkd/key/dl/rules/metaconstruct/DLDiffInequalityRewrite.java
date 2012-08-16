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
package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.And;
import de.uka.ilkd.key.dl.model.Biimplies;
import de.uka.ilkd.key.dl.model.CompoundFormula;
import de.uka.ilkd.key.dl.model.Constant;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.DLStatementBlock;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.Equals;
import de.uka.ilkd.key.dl.model.Exists;
import de.uka.ilkd.key.dl.model.Expression;
import de.uka.ilkd.key.dl.model.Forall;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.FunctionTerm;
import de.uka.ilkd.key.dl.model.Implies;
import de.uka.ilkd.key.dl.model.LogicalVariable;
import de.uka.ilkd.key.dl.model.MetaVariable;
import de.uka.ilkd.key.dl.model.Not;
import de.uka.ilkd.key.dl.model.Or;
import de.uka.ilkd.key.dl.model.Predicate;
import de.uka.ilkd.key.dl.model.PredicateTerm;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.Variable;
import de.uka.ilkd.key.dl.model.VariableDeclaration;
import de.uka.ilkd.key.dl.model.impl.TermFactoryImpl;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * @author jdq
 */
public class DLDiffInequalityRewrite extends AbstractDLMetaOperator {

	public static final Name NAME = new Name("#DLDiffInequalityRewrite");

	public DLDiffInequalityRewrite() {
		super(NAME, 1);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#calculate(de.uka.ilkd.key.logic.Term,
	 *      de.uka.ilkd.key.rule.inst.SVInstantiations,
	 *      de.uka.ilkd.key.java.Services)
	 */
	public Term calculate(Term term, SVInstantiations svInst, Services services) {
		DiffSystem system1 = (DiffSystem) ((StatementBlock) term.sub(0)
				.javaBlock().program()).getChildAt(0);
		Term post = term.sub(0).sub(0);
		TermFactory tf;
		try {
			tf = TermFactory.getTermFactory(TermFactoryImpl.class, services
					.getNamespaces());
			Formula input = null;
			for(ProgramElement f: system1) {
				if(input == null) {
					input = (Formula) f;
				} else {
					input = tf.createAnd(input, (Formula) f);
				}
			}
			
			List<Formula> sys = new ArrayList<Formula>();
			for (Formula f : splitOr(input)) {
				sys.add(rewriteIneq(f, services.getNamespaces(), tf));
			}
			Formula result = null;

			for (Formula f : sys) {
				if (result == null) {
					result = f;
				} else {
					result = tf.createOr(result, f);
				}
			}
			DLProgram program = tf.createDiffSystem(Collections
					.singletonList(result));
			program.setDLAnnotations(system1.getDLAnnotations());
			return de.uka.ilkd.key.logic.TermFactory.DEFAULT.createTerm(term
					.sub(0).op(), new Term[] { post },
					new ImmutableArray[0], JavaBlock
							.createJavaBlock(new DLStatementBlock(program)));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * @param f
	 * @return
	 */
	private Formula rewriteIneq(Formula f, NamespaceSet nss, TermFactory tf) {
		List<DLNonTerminalProgramElement> quantifiers = new ArrayList<DLNonTerminalProgramElement>();
		while (f instanceof Forall || f instanceof Exists) {
			quantifiers.add((DLNonTerminalProgramElement) f);
			f = (Formula) ((DLNonTerminalProgramElement) f).getChildAt(1);
		}
		Dot curDot = null;
		while ((curDot = getDottedVariableInEq(f, false)) != null) {
			Variable oldVar = (Variable) curDot.getChildAt(0);
			String string = "d" + oldVar.getElementName().toString();
			Name n = new Name(nss.getUniqueName(string, true));
			LogicalVariable var = tf.createLogicalVariable(n.toString());
			f = (Formula) convert(f, curDot, var, tf);
			List<Expression> args = new ArrayList<Expression>();
			args.add(curDot);
			args.add(var);
			f = tf
					.createAnd(tf.createPredicateTerm(tf.createEquals(), args),
							f);
			List<Variable> varlist = new ArrayList<Variable>();
			varlist.add(var);
			VariableDeclaration dec = tf.createVariableDeclaration(RealLDT
					.getRealSort(), varlist);
			// we don't add it directly to keep the convert method simple
			quantifiers.add(tf.createExists(dec, f));
		}

		// reintroduce quantifiers
		Collections.reverse(quantifiers);
		for (DLNonTerminalProgramElement p : quantifiers) {
			if (p instanceof Forall) {
				Forall forall = (Forall) p;
				f = tf.createForall((VariableDeclaration) forall.getChildAt(0),
						(Formula) forall.getChildAt(1));
			} else if (p instanceof Exists) {
				Exists ex = (Exists) p;
				f = tf.createExists((VariableDeclaration) ex.getChildAt(0),
						(Formula) ex.getChildAt(1));
			}
		}
		return f;
	}

	/**
	 * @param f
	 * @param curDot
	 * @param var
	 * @param tf
	 * @return
	 */
	private DLProgramElement convert(ProgramElement childAt, Dot curDot,
			LogicalVariable var, TermFactory tf) {
		DLProgramElement result = null;
		if (childAt instanceof Implies) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createImpl((Formula) convert(p.getChildAt(0), curDot,
					var, tf), (Formula) convert(p.getChildAt(1), curDot, var,
					tf));
		} else if (childAt instanceof Not) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createNot((Formula) convert(p.getChildAt(0), curDot,
					var, tf));
		} else if (childAt instanceof And) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createAnd((Formula) convert(p.getChildAt(0), curDot,
					var, tf), (Formula) convert(p.getChildAt(1), curDot, var,
					tf));
		} else if (childAt instanceof Biimplies) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createBiImpl((Formula) convert(p.getChildAt(0), curDot,
					var, tf), (Formula) convert(p.getChildAt(1), curDot, var,
					tf));
		} else if (childAt instanceof Or) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createOr((Formula) convert(p.getChildAt(0), curDot,
					var, tf), (Formula) convert(p.getChildAt(1), curDot, var,
					tf));
		} else if (childAt instanceof PredicateTerm) {
			PredicateTerm p = (PredicateTerm) childAt;
			Predicate pred = (Predicate) convert(p.getChildAt(0), curDot, var,
					tf);
			List<Expression> children = new ArrayList<Expression>();
			for (int i = 1; i < p.getChildCount(); i++) {
				children.add((Expression) convert(p.getChildAt(i), curDot, var,
						tf));
			}
			result = tf.createPredicateTerm(pred, children);
		} else if (childAt instanceof FunctionTerm) {
			FunctionTerm p = (FunctionTerm) childAt;
			de.uka.ilkd.key.dl.model.Function pred = (de.uka.ilkd.key.dl.model.Function) convert(
					p.getChildAt(0), curDot, var, tf);
			List<Expression> children = new ArrayList<Expression>();
			for (int i = 1; i < p.getChildCount(); i++) {
				children.add((Expression) convert(p.getChildAt(i), curDot, var,
						tf));
			}
			result = tf.createFunctionTerm(pred, children);
		} else if (childAt instanceof Predicate) {
			result = (Predicate) childAt;
		} else if (childAt instanceof de.uka.ilkd.key.dl.model.Function) {
			result = (de.uka.ilkd.key.dl.model.Function) childAt;
		} else if (childAt instanceof Constant) {
			result = (Constant) childAt;
		} else if (childAt instanceof Dot) {
			Dot d = (Dot) childAt;
			if (d.getOrder() == curDot.getOrder()
					&& ((Variable) d.getChildAt(0)).getElementName().toString()
							.equals(
									((Variable) curDot.getChildAt(0))
											.getElementName().toString())) {
				result = var;
			} else {
				result = d;
			}
		} else if (childAt instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
			de.uka.ilkd.key.dl.model.ProgramVariable pv = (de.uka.ilkd.key.dl.model.ProgramVariable) childAt;
			result = pv;
		} else if (childAt instanceof LogicalVariable) {
			result = (LogicalVariable) childAt;
		} else if (childAt instanceof MetaVariable) {
			result = (MetaVariable) childAt;
		}
		if (result == null) {
			throw new IllegalArgumentException("Dont know how to convert: "
					+ childAt);
		}
		if (childAt instanceof DLProgramElement) {
			DLProgramElement el = (DLProgramElement) childAt;
			for (String annotation : el.getDLAnnotations().keySet()) {
				final List<Formula> annotations = el
						.getDLAnnotation(annotation);
				List<Formula> newAnnon = new ArrayList<Formula>();

				for (Formula form : annotations) {
					newAnnon.add((Formula) convert(form, curDot, var, tf));
				}

				result.setDLAnnotation(annotation, newAnnon);
			}
		}
		return result;
	}

	/**
	 * @return
	 */
	private Dot getDottedVariableInEq(ProgramElement p, boolean ineq) {
		if (ineq && p instanceof Dot) {
			return (Dot) p;
		} else if (p instanceof PredicateTerm) {
			if (((PredicateTerm) p).getChildAt(0) instanceof Equals) {
				return null;
			} else {
				Dot d = getDottedVariableInEq(
						((PredicateTerm) p).getChildAt(1), true);
				if (d == null) {
					d = getDottedVariableInEq(
							((PredicateTerm) p).getChildAt(2), true);
				}
				return d;
			}
		} else if (p instanceof DLNonTerminalProgramElement) {
			for (int i = 0; i < ((DLNonTerminalProgramElement) p)
					.getChildCount(); i++) {
				Dot d = getDottedVariableInEq(
						((DLNonTerminalProgramElement) p).getChildAt(i), true);
				if (d != null) {
					return d;
				}
			}
		}
		return null;
	}

	/**
	 * @param system1
	 * @param tf
	 * @return
	 */
	private List<Formula> splitOr(Formula d) {
		List<Formula> result = new ArrayList<Formula>();
		if (d instanceof Or) {
			result.addAll(splitOr((Formula) ((Or) d).getChildAt(0)));
			result.addAll(splitOr((Formula) ((Or) d).getChildAt(1)));
		} else {
			result.add(d);
		}
		return result;
	}
}
