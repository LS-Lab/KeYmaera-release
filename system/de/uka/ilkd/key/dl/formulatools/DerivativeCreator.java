/**
 * *****************************************************************************
 * Copyright (c) 2012 Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 *     Jan-David Quesel - initial API and implementation
 * ****************************************************************************
 */
package de.uka.ilkd.key.dl.formulatools;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.tree.CommonTree;

import recoder.java.expression.operator.BinaryAndAssignment;
import scala.actors.threadpool.Arrays;

import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.And;
import de.uka.ilkd.key.dl.model.Biimplies;
import de.uka.ilkd.key.dl.model.CompoundFormula;
import de.uka.ilkd.key.dl.model.Constant;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.ElementaryDLProgram;
import de.uka.ilkd.key.dl.model.Equals;
import de.uka.ilkd.key.dl.model.Expression;
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
import de.uka.ilkd.key.dl.model.impl.ExistsImpl;
import de.uka.ilkd.key.dl.model.impl.VariableDeclarationImpl;
import de.uka.ilkd.key.dl.model.impl.VariableImpl;
import de.uka.ilkd.key.dl.model.impl.VariableTypeImpl;
import de.uka.ilkd.key.java.Comment;
import de.uka.ilkd.key.java.NonTerminalProgramElement;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.expression.Operator;
import de.uka.ilkd.key.java.expression.operator.BinaryOperator;
import de.uka.ilkd.key.java.expression.operator.Divide;
import de.uka.ilkd.key.java.expression.operator.Minus;
import de.uka.ilkd.key.java.expression.operator.Plus;
import de.uka.ilkd.key.java.expression.operator.Times;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;

public class DerivativeCreator {

	/**
	 * The function calculates the derivative (induction) of a term based on the
	 * derivivates given by @sys
	 * 
	 * note that the @sys must not contain any evolution domain
	 */
	public static final Term diffInd(DiffSystem sys, Term post, Services s) {
		// neue Methode
		System.out.println("Test");
		sys = getAdjustedSystem(sys, s);
		HashMap<String, Term> replacements = new HashMap<String, Term>();
		collectDiffReplacements(sys, replacements, s);
		System.out.println("Replacements are: " + replacements);
		Term createNFF = NegationNormalForm.apply(post);
		System.out.println("Formula: " + post);
		System.out.println("NFF: " + createNFF);
		return Derive.apply(createNFF, replacements, null);
	}

	/**
	 * The function calculates the derivative (diffFin) of a term based on the
	 * derivivates given by @sys
	 * 
	 * note that the @sys must not contain any evolution domain
	 */
	public static final Term diffFin(DiffSystem sys, Term post, Term epsilon, Services s) {
		sys = getAdjustedSystem(sys, s);
		HashMap<String, Term> replacements = new HashMap<String, Term>();
		collectDiffReplacements(sys, replacements, s);
		System.out.println("Replacements are: " + replacements);
		Term createNFF = NegationNormalForm.apply(post);
		System.out.println("Formula: " + post);
		System.out.println("NFF: " + createNFF);
		return Derive.apply(createNFF, replacements, epsilon);
	}

	/**
	 * Collect all program variables which are children of a Dot.
	 * 
	 * @param form
	 *            the current root element.
	 * 
	 * @param map
	 *            the Map used for storing the result
	 */
	public static final void collectDiffReplacements(ProgramElement form, Map<String, Term> map, Services services) {

		if (form instanceof PredicateTerm) {
			PredicateTerm pred = (PredicateTerm) form;
			// we can only handle differential equations of the form x'=f(x,y)
			// here
			if (pred.getChildAt(0) instanceof Equals && pred.getChildAt(1) instanceof Dot) {
				de.uka.ilkd.key.dl.model.ProgramVariable pv = (de.uka.ilkd.key.dl.model.ProgramVariable) ((Dot) pred.getChildAt(1)).getChildAt(0);
				String pvName = pv.getElementName().toString();
				map.put(pvName, Prog2LogicConverter.convert((DLProgramElement) pred.getChildAt(2), services));
			} else {
				if (containsDots(pred)) {
					// could "a'>=b+5" be an equation in the diff-free
					throw new IllegalArgumentException("Don't know how to handle predicate " + pred.getChildAt(0) + " in " + pred);
				} else {
					// ignore evolution domain constraint
					System.out.println("Ignoring " + pred);
				}
			}
		}
		if (form instanceof DLNonTerminalProgramElement) {
			DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) form;
			for (ProgramElement p : dlnpe) {
				collectDiffReplacements(p, map, services);
			}
		}
	}

	/**
	 * @param pred
	 * @return
	 */
	private static boolean containsDots(DLProgramElement pred) {
		if (pred instanceof Dot) {
			return true;
		} else if (pred instanceof DLNonTerminalProgramElement) {
			DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) pred;
			for (ProgramElement p : dlnpe) {
				if (containsDots((DLProgramElement) p)) {
					return true;
				}
			}
			return false;
		}
		return false;
	}

	// .................................

	private static DiffSystem getAdjustedSystem(DiffSystem sys, Services s) {
		try {
			TermFactory tf = TermFactory.getTermFactory(TermFactory.class, s.getNamespaces());
			LinkedHashMap<Variable, LogicalVariable> renaming = new LinkedHashMap<Variable, LogicalVariable>();
			List<Formula> formulas = new ArrayList<Formula>();
			for(int childAt = 0; childAt < sys.getChildCount(); childAt++) {
				formulas.add((Formula)replaceDottedVariables(sys.getChildAt(childAt), renaming, tf));
			}
			System.out.println("#formula :"+formulas.size());
			Formula f = null;
			if(formulas.size() > 0) {
				f = formulas.remove(0);
				while(formulas.size() > 0) {
					f = tf.createAnd(f, formulas.remove(0));
				}
			}
			for(Variable v : renaming.keySet()) {
				List<Expression> l = new ArrayList<Expression>();
				l.add(tf.createDot(v, 1));
				l.add(renaming.get(v));
				
				f = tf.createAnd(f, tf.createPredicateTerm(tf.createEquals(), l));
			}
//			List<Variable> vars = new ArrayList<Variable>();
//			vars.addAll(renaming.values());
//			f = tf.createExists(tf.createVariableDeclaration(RealLDT.getRealSort(), vars), f);
			formulas.add(f);
			System.out.println(tf.createDiffSystem(formulas));
//			System.exit(0);
			return tf.createDiffSystem(formulas);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
    }

//	private static ProgramElement renameDottedVariables(ProgramElement p, TermFactory tf) {
//		LinkedHashMap<Variable, LogicalVariable> renaming = new LinkedHashMap<Variable, LogicalVariable>();
//		ProgramElement pe = getDottedVariableInEq(p, renaming, tf);
//		for (Variable d : renaming.keySet()) {
//			List<Expression> l = new ArrayList<Expression>();
//			l.add(tf.createDot(d, 1));
//			l.add(renaming.get(d));
//			tf.createAnd((Formula) pe, tf.createPredicateTerm(tf.createEquals(), l));
//
//			// pe = new And(tf.createAssign(d,renaming.get(d)), pe);
//		}
//		for (Variable d : renaming.keySet()) {
//			// pe = tf.createExist(tf.createVariableDeclaration(, new
//			// ArrayList<String>renaming.get(d), Boolean.TRUE,
//			// Boolean.FALSE), pe);
//		}
//		return pe;
//	}

	// private static ProgramElement getDottedVariableInEq(ProgramElement p,
	// HashMap<Variable, LogicalVariable> renaming, TermFactory tf) {
	// if (p instanceof Dot) {
	// if(!renaming.containsKey(((Dot) p).getChildAt(0))) { //There is only one
	// new name generated from p
	// Name n = new Name(tf.getNamespaces().getUniqueName(((Variable)((Dot)
	// p).getChildAt(0)).getElementName().toString(), true));
	// LogicalVariable v = tf.createLogicalVariable(n.toString());
	// renaming.put((Variable)((Dot)p).getChildAt(0), v);
	// }
	// return renaming.get(p);
	// } else if (p instanceof PredicateTerm) {
	// assert ((PredicateTerm) p).getChildCount() == 3 :
	// "We assume that there are only binary predicates";
	// ProgramElement child0 = getDottedVariableInEq(((PredicateTerm)
	// p).getChildAt(1), renaming, tf);
	// ProgramElement child1 = getDottedVariableInEq(((PredicateTerm)
	// p).getChildAt(2), renaming, tf);
	// return tf.createPredicateTerm((Predicate)((PredicateTerm)
	// p).getChildAt(0), Arrays.asList(new ProgramElement[]{child0, child1}));
	// } else if (p instanceof DLNonTerminalProgramElement) {
	// for (int i = 0; i < ((DLNonTerminalProgramElement) p).getChildCount();
	// i++) {
	// getDottedVariableInEq(((DLNonTerminalProgramElement) p).getChildAt(i),
	// renaming, tf);
	// }
	// return p;
	// }
	// return p;
	// }

	private static ProgramElement replaceDottedVariables(ProgramElement childAt, HashMap<Variable, LogicalVariable> renaming, TermFactory tf) {
		ProgramElement result = null;
		if (childAt instanceof Dot) {
			if (!renaming.containsKey(((Dot) childAt).getChildAt(0))) { // There
																		// is
																		// only
																		// one
																		// new
																		// name
																		// generated
																		// from
																		// p
				Name n = new Name(tf.getNamespaces().getUniqueName(((Variable) ((Dot) childAt).getChildAt(0)).getElementName().toString(), true));
				LogicalVariable v = tf.createLogicalVariable(n.toString());
				renaming.put((Variable) ((Dot) childAt).getChildAt(0), v);
			}
			result = renaming.get(((Dot) childAt).getChildAt(0));
		} else if (childAt instanceof Implies) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createImpl((Formula) replaceDottedVariables(p.getChildAt(0), renaming, tf), (Formula) replaceDottedVariables(p.getChildAt(1), renaming, tf));
		} else if (childAt instanceof Not) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createNot((Formula) replaceDottedVariables(p.getChildAt(0), renaming, tf));
		} else if (childAt instanceof And) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createAnd((Formula) (Formula) replaceDottedVariables(p.getChildAt(0), renaming, tf), (Formula) (Formula) replaceDottedVariables(p.getChildAt(1), renaming, tf));
		} else if (childAt instanceof Biimplies) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createBiImpl((Formula) replaceDottedVariables(p.getChildAt(0), renaming, tf), (Formula) replaceDottedVariables(p.getChildAt(1), renaming, tf));
		} else if (childAt instanceof Or) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createOr((Formula) replaceDottedVariables(p.getChildAt(0), renaming, tf), (Formula) replaceDottedVariables(p.getChildAt(1), renaming, tf));
		} else if (childAt instanceof PredicateTerm) {
			PredicateTerm p = (PredicateTerm) childAt;
			Predicate pred = (Predicate) replaceDottedVariables(p.getChildAt(0), renaming, tf);
			List<Expression> children = new ArrayList<Expression>();
			for (int i = 1; i < p.getChildCount(); i++) {
				children.add((Expression) replaceDottedVariables(p.getChildAt(i), renaming, tf));
			}
			result = tf.createPredicateTerm(pred, children);
		} else if (childAt instanceof FunctionTerm) {
			FunctionTerm p = (FunctionTerm) childAt;
			de.uka.ilkd.key.dl.model.Function pred = (de.uka.ilkd.key.dl.model.Function) replaceDottedVariables(p.getChildAt(0), renaming, tf);
			List<Expression> children = new ArrayList<Expression>();
			for (int i = 1; i < p.getChildCount(); i++) {
				children.add((Expression) replaceDottedVariables(p.getChildAt(i), renaming, tf));
			}
			result = tf.createFunctionTerm(pred, children);
		} else if (childAt instanceof Predicate) {
			result = (Predicate) childAt;
		} else if (childAt instanceof de.uka.ilkd.key.dl.model.Function) {
			result = (de.uka.ilkd.key.dl.model.Function) childAt;
		} else if (childAt instanceof Constant) {
			result = (Constant) childAt;
		} else if (childAt instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
			de.uka.ilkd.key.dl.model.ProgramVariable pv = (de.uka.ilkd.key.dl.model.ProgramVariable) childAt;
			result = pv;
		} else if (childAt instanceof LogicalVariable) {
			result = (LogicalVariable) childAt;
		} else if (childAt instanceof MetaVariable) {
			result = (MetaVariable) childAt;
		}
		if (result == null) {
			throw new IllegalArgumentException("Dont know how to convert: " + childAt + " " + childAt.getClass());
		}
		return result;
	}
}
