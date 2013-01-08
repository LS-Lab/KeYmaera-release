/***************************************************************************
 *   Copyright (C) 2007 by Andre Platzer                                   *
 *   @informatik.uni-oldenburg.de                                        *
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

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.dl.arithmetics.exceptions.FailedComputationException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.And;
import de.uka.ilkd.key.dl.model.Biimplies;
import de.uka.ilkd.key.dl.model.CompoundFormula;
import de.uka.ilkd.key.dl.model.Constant;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLStatementBlock;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Div;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.Equals;
import de.uka.ilkd.key.dl.model.Exists;
import de.uka.ilkd.key.dl.model.Exp;
import de.uka.ilkd.key.dl.model.Expression;
import de.uka.ilkd.key.dl.model.Forall;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.Function;
import de.uka.ilkd.key.dl.model.FunctionTerm;
import de.uka.ilkd.key.dl.model.Implies;
import de.uka.ilkd.key.dl.model.LogicalVariable;
import de.uka.ilkd.key.dl.model.MetaVariable;
import de.uka.ilkd.key.dl.model.Minus;
import de.uka.ilkd.key.dl.model.MinusSign;
import de.uka.ilkd.key.dl.model.Mult;
import de.uka.ilkd.key.dl.model.Not;
import de.uka.ilkd.key.dl.model.Or;
import de.uka.ilkd.key.dl.model.Plus;
import de.uka.ilkd.key.dl.model.Predicate;
import de.uka.ilkd.key.dl.model.PredicateTerm;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.Variable;
import de.uka.ilkd.key.dl.model.VariableDeclaration;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * @author miroel
 */
public class DiffNormalize extends AbstractDLMetaOperator {

	public static final Name NAME = new Name("#DiffNormalize");
	private static final String DERIVATIVE_VARIABLE_PREFIX = "d";

	public DiffNormalize() {
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
		try {
			return diffNormalize(term.sub(0), services);
		} catch (UnsolveableException e) {
			throw new IllegalStateException(e.getMessage(), e);
		} catch (FailedComputationException e) {
			throw new IllegalStateException(e.getMessage(), e);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw (InternalError) new InternalError(e.getMessage()).initCause(e);
		}
	}

	@SuppressWarnings("unchecked")
	private static Term diffNormalize(Term term, Services services) throws SolverException {
		final DiffSystem system = (DiffSystem) ((StatementBlock) term.javaBlock().program()).getChildAt(0);
		Term post = term.sub(0);
		if (term.op() == Modality.BOX || term.op() == Modality.TOUT) {
			try {
				StringWriter writer = new StringWriter();
				DiffSystem sys = system;
				sys.prettyPrint(new PrettyPrinter(writer));
				sys = getNormalizedSystem(sys, services);
				sys.setDLAnnotations(system.getDLAnnotations());
				return de.uka.ilkd.key.logic.TermFactory.DEFAULT.createTerm(term.op(), new Term[] { post }, new ImmutableArray[0], JavaBlock.createJavaBlock(new DLStatementBlock(sys)));
				// } catch (SolverException e) {
				// throw e;
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw (InternalError) new InternalError(e.getMessage()).initCause(e);
			}
		} else {
			throw new IllegalStateException("Unknown modality " + term.op());
		}
	}

	/**
	 * Normalizes System so that it only contains one Formula, consisting only
	 * of Conjunctions and Quantifiers. All Differential Equations are of the
	 * form: y'=dy , where y is a Variable and dy is a Term that does not
	 * contain any derivatives
	 * 
	 * @param sys
	 *            Ordinary Differential System to be normalized
	 * @param s
	 *            Services
	 * @return Normalized System
	 */
	private static DiffSystem getNormalizedSystem(DiffSystem sys, Services s) {
		try {
			TermFactory tf = TermFactory.getTermFactory(TermFactory.class, s.getNamespaces());
			LinkedHashMap<Variable, LogicalVariable> renaming = new LinkedHashMap<Variable, LogicalVariable>();
			List<Formula> formulas = new ArrayList<Formula>();
			for (int childAt = 0; childAt < sys.getChildCount(); childAt++) {
				formulas.add((Formula) replaceDottedVariables(sys.getChildAt(childAt), renaming, tf));
			}
			Formula f = null;

			if (formulas.size() > 0) {
				f = formulas.remove(0);
				while (formulas.size() > 0) {
					f = tf.createAnd(f, formulas.remove(0));
				}
			}
			if (renaming.keySet().size() > 0) {
				for (Variable v : renaming.keySet()) {
					List<Expression> l = new ArrayList<Expression>();
					l.add(tf.createDot(v, 1));
					l.add(renaming.get(v));
					f = tf.createAnd(f, tf.createPredicateTerm(tf.createEquals(), l));
				}

				List<Variable> vars = new ArrayList<Variable>();
				vars.addAll(renaming.values());
				f = tf.createExists(tf.createVariableDeclaration(RealLDT.getRealSort(), vars), f);
			}

			formulas.add(f);

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
	/**
	 * Transforms a Programelement so that all Equations only contain up to one Dotted Variable.
	 * Multiple Dotted Variables in one Equation get replaced by ordinary Variables and Quantifier and additional Equations are added.
	 * 
	 * @param childAt
	 * @param renaming map for replacements of dotted variables
	 * @param tf
	 * @return programelement where all equations only contain up to one dotted variable
	 */
	private static ProgramElement replaceDottedVariables(final ProgramElement childAt, HashMap<Variable, LogicalVariable> renaming, final TermFactory tf) {
		ProgramElement result = null;
		if (childAt instanceof Dot) {
			if (!renaming.containsKey(((Dot) childAt).getChildAt(0))) {
				Name n = new Name(tf.getNamespaces().getUniqueName(DERIVATIVE_VARIABLE_PREFIX + ((Variable) ((Dot) childAt).getChildAt(0)).getElementName().toString(), true));
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
			PredicateTerm pTerm = (PredicateTerm) childAt;
			if(isNormalized(pTerm)) {
				result = childAt;
			} else if(isNormalizeable(pTerm)) {
				HashSet<Dot> dots = new HashSet<Dot>();
				Expression left = (Expression) pTerm.getChildAt(1);
				Expression right = (Expression) pTerm.getChildAt(2);
				collectDots(left, dots);
				collectDots(right, dots);
				result = transformEqualsWithOneDot(left, right, dots.iterator().next(), tf);
			} else {
				List<Expression> children = new ArrayList<Expression>();
				for (int i = 1; i < pTerm.getChildCount(); i++) {
					children.add((Expression) replaceDottedVariables(pTerm.getChildAt(i), renaming, tf));
				}
				result = tf.createPredicateTerm((Predicate) pTerm.getChildAt(0), children);
			}
		} else if (childAt instanceof FunctionTerm) {
			FunctionTerm p = (FunctionTerm) childAt;
			Function pred = (de.uka.ilkd.key.dl.model.Function) replaceDottedVariables(p.getChildAt(0), renaming, tf);
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
		} else if (childAt instanceof Exists) {
			Exists quan = (Exists) childAt;
			Formula f = (Formula) replaceDottedVariables(quan.getChildAt(1), renaming, tf);
			result = tf.createExists((VariableDeclaration) quan.getChildAt(0), f);
		} else if (childAt instanceof Forall) {
			Forall quan = (Forall) childAt;
			Formula f = (Formula) replaceDottedVariables(quan.getChildAt(quan.getChildCount() - 1), renaming, tf);
			result = tf.createForall((VariableDeclaration) quan.getChildAt(0), f);
		}
		if (result == null) {
			throw new IllegalArgumentException("Dont know how to convert: " + childAt + " " + childAt.getClass());
		}
		return result;
	}

	/**
	 * Fills the Set with all different Dots found in the Expression
	 * 
	 * @param exp Expression to be searched for Dots
	 * @param dots Set to be filled with Dots
	 */
	private static void collectDots(final Expression exp, Set<Dot> dots) {
		if (exp instanceof Dot) {
			dots.add((Dot) exp);
		} else if (exp instanceof FunctionTerm) {
			FunctionTerm fTerm = (FunctionTerm) exp;
			for (int i = 1; i < fTerm.getChildCount(); i++) {
				collectDots((Expression) fTerm.getChildAt(i), dots);
			}
		}
	}
	
	/**
	 * Checks whether the Expression has the specified Dot d.
	 * 
	 * @param exp Expression to be checked for Dot d
	 * @param d The Dot
	 * @return Expression exp contains the Dot d
	 */
	private static boolean containsDot(final Expression exp, final Dot d) {
		if (exp instanceof Dot && ((Dot) exp).getChildAt(0).equals(d.getChildAt(0))) {
			return true;
		} else if (exp instanceof FunctionTerm) {
			FunctionTerm fTerm = (FunctionTerm) exp;
			for (int i = 1; i < fTerm.getChildCount(); i++) {
				if (containsDot((Expression) ((FunctionTerm) exp).getChildAt(i), d)) {
					return true;
				}
			}
			return false;
		}
		return false;
	}
	
	/**
	 * Checks whether the ProgramElement has at least one Dot.
	 * 
	 * @param p
	 * @return
	 */
	private static boolean containsDots(ProgramElement p) {
		if (p instanceof Dot) {
			return true;
		} else if (p instanceof DLNonTerminalProgramElement) {
			for (ProgramElement s : (DLNonTerminalProgramElement) p) {
				if (containsDots(s)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks whether the PredicateTerm can be normalized
	 * 
	 * @param pTerm A Predicate Term
	 * @param tf A Termfactory
	 * @return checks whether the predicate can be normalized
	 */
	private static boolean isNormalizeable(final PredicateTerm pTerm) {
		HashSet<Dot> dots = new HashSet<Dot>();
		return pTerm.getChildAt(0) instanceof Equals && isNormalizeable((Expression) pTerm.getChildAt(1), dots) && isNormalizeable((Expression) pTerm.getChildAt(2), dots) && dots.size() == 1;
	}

	private static boolean isNormalizeable(final Expression exp, Set<Dot> dots) {
		if(exp instanceof Dot) {
			dots.add((Dot)exp);
			if(dots.size() > 1) {
				return false;
			}
		} else if(exp instanceof FunctionTerm) {
			FunctionTerm fTerm = (FunctionTerm) exp;
			boolean result = true;
			for(int i = 1; i < fTerm.getChildCount(); i++) {
				result &= isNormalizeable((Expression) fTerm.getChildAt(i), dots);
			}
			if(dots.size() > 1) {
				return false;
			} else if(dots.size() == 1) {
				Dot d = dots.iterator().next();
				if(fTerm.getChildAt(0) instanceof Mult) {
					if(containsDot((Expression) fTerm.getChildAt(1), d) && containsDot((Expression) fTerm.getChildAt(2), d)) {
						return false;
					}
				} else if(fTerm.getChildAt(0) instanceof Div) {
					if(containsDot((Expression) fTerm.getChildAt(2), d)) {
						return false;
					}
				} else if(!(fTerm.getChildAt(0) instanceof MinusSign || fTerm.getChildAt(0) instanceof Plus || fTerm.getChildAt(0) instanceof Minus) && containsDot(fTerm, d)) {
					return false;
				}
			}
			return result;
		}
		return true;
	}
	
	/**
	 * Transforms an Equation with only one Dot. 
	 * The Equation is normalized to the Form a*d'+f = b*d'+g, where d' is the Dotted Variable.
	 * Then this Formula is the result (a-b != 0 & d'= (g-f)/(a-b)) | (a-b=0 & g-f=0)
	 * 
	 * @param exp1
	 * @param exp2
	 * @param d
	 * @param tf
	 * @return
	 */
	private static Formula transformEqualsWithOneDot(final Expression exp1, final Expression exp2, final Dot d, final TermFactory tf) {
		Expression flatLeft = flatExpression(exp1, tf);
		Expression a = getSumOfFactorsOfDot(flatLeft, d, tf);
		Expression f = getSumOfRest(flatLeft, d, tf);

		Expression flatRight = flatExpression(exp2, tf);
		Expression b = getSumOfFactorsOfDot(flatRight, d, tf);
		Expression g = getSumOfRest(flatRight, d, tf);
		//XXX
		Expression aMinusB = simplifyPolynom(flatExpression(tf.createMinus(a, b), tf), tf);
		System.out.println("A-B = " + flatExpression(tf.createMinus(a, b), tf));
		Expression gMinusF = simplifyPolynom(flatExpression(tf.createMinus(g, f), tf), tf);
		System.out.println("G-F = " + flatExpression(tf.createMinus(g, f), tf));

		System.out.println(flatLeft);
		System.out.println("A =" + a + "F= " + f);
		System.out.println(flatRight);
		System.out.println("B =" + b + "G= " + g);
		
		final Constant ZERO = tf.createConstant(BigDecimal.ZERO);
		return tf.createOr(tf.createAnd(tf.createPredicateTerm(tf.createUnequals(), aMinusB, ZERO), tf.createPredicateTerm(tf.createEquals(), d, tf.createDiv(gMinusF, aMinusB))),
				tf.createAnd(tf.createPredicateTerm(tf.createEquals(), aMinusB, ZERO), tf.createPredicateTerm(tf.createEquals(), gMinusF, ZERO)));
	}

	// to be applied on polynoms without dots only
	private static Expression simplifyPolynom(Expression exp, TermFactory tf) {
		List<Expression> summands = new ArrayList<Expression>();
		collectSummandsSorted(exp, summands, tf);
		//XXX
		System.out.println("Summands" + Arrays.toString(summands.toArray(new Expression[0])));
		Constant constantSummand = tf.createConstant(BigDecimal.ZERO);
		int index = summands.size() - 1;
		while (index >= 0 && summands.get(index) instanceof Constant) {
			constantSummand = tf.createConstant(constantSummand.getValue().add(((Constant) summands.get(index)).getValue()));
			index--;
		}
		Expression result;
		if(constantSummand.getValue().equals(BigDecimal.ZERO) && index >= 0) { 
			result = summands.get(index);
			index--;
		} else {
			result = constantSummand;
		}
		while (index >= 0) {
			result = tf.createPlus(summands.get(index), result);
			index--;
		}
		return result;
	}

	private static void collectSummandsSorted(Expression exp, List<Expression> summands, TermFactory tf) {
		if (exp instanceof FunctionTerm) {
			FunctionTerm fTerm = (FunctionTerm) exp;
			if (fTerm.getChildAt(0) instanceof MinusSign) {
				collectSummandsSorted(tf.createMult(tf.createConstant(BigDecimal.valueOf(-1)), (Expression) fTerm.getChildAt(1)), summands, tf);
			} else if (fTerm.getChildAt(0) instanceof Mult) {
				Expression monom = simplifyMonom(fTerm, tf);
				if (monom instanceof Constant) {
					summands.add(summands.size(), monom);
				} else {
					summands.add(0, monom);
				}
			} else if (fTerm.getChildAt(0) instanceof Plus) {
				collectSummandsSorted((Expression) fTerm.getChildAt(1), summands, tf);
				collectSummandsSorted((Expression) fTerm.getChildAt(2), summands, tf);
			} else if (fTerm.getChildAt(0) instanceof Minus) {
				collectSummandsSorted((Expression) fTerm.getChildAt(1), summands, tf);
				collectSummandsSorted(tf.createMinusSign((Expression) fTerm.getChildAt(2)), summands, tf);
			} else if (fTerm.getChildAt(0) instanceof Exp) {
				collectSummandsSorted(tf.createMult(tf.createConstant(BigDecimal.ONE), fTerm), summands, tf);
			} else {
				throw new IllegalArgumentException("Expected a polynom, but found " + exp);
			}
		} else if (exp instanceof Constant) {
			summands.add(summands.size(), exp);
		} else if (exp instanceof Variable) {
			summands.add(0, exp);
		} else {
			throw new IllegalArgumentException("Expected a polynom, but found " + exp);
		}
	}
	
	/**
	 * Under the assumption that the Expression is a Monom without Dots, it sums up all Constants to one leading Constant
	 * multiplied by the variables.
	 * 
	 * @param exp
	 * @param tf
	 * @return
	 */
	// to be applied on monoms without dots only
	private static Expression simplifyMonom(final Expression exp, final TermFactory tf) {
		List<Expression> factors = new ArrayList<Expression>();
		collectFactorsSorted(exp, factors);
		int index = 0;
		Constant constantFactor = tf.createConstant(BigDecimal.ONE);
		while (index < factors.size() && factors.get(index) instanceof Constant) {
			constantFactor = tf.createConstant(constantFactor.getValue().multiply(((Constant) factors.get(index)).getValue()));
			index++;
		}
		Expression result = constantFactor;
		while (index < factors.size()) {
			result = tf.createMult(result, factors.get(index));
			index++;
		}
		return result;
	}

	// to be applied on monoms without dots only
	private static void collectFactorsSorted(final Expression exp, final List<Expression> factors) {
		if (exp instanceof FunctionTerm) {
			FunctionTerm fTerm = (FunctionTerm) exp;
			if (fTerm.getChildAt(0) instanceof Mult) {
				collectFactorsSorted((Expression) fTerm.getChildAt(1), factors);
				collectFactorsSorted((Expression) fTerm.getChildAt(2), factors);
			} else if (fTerm.getChildAt(0) instanceof Exp) {
				for (int i = factors.size() - 1; i >= 0; i--) {
					factors.add(factors.size(), fTerm);
				}
			} else {
				throw new IllegalArgumentException("Expected a monom, but found " + exp);
			}
		} else if (exp instanceof Constant) {
			int position = 0;
			for (int i = factors.size() - 1; i >= 0; i--) {
				if (factors.get(i) instanceof Exp && (((Constant) ((FunctionTerm) factors.get(i)).getChildAt(2)).getValue().compareTo(BigDecimal.ONE) < 0)) {
					position = i;
					break;
				}
			}
			factors.add(position, exp);
		} else if (exp instanceof Variable) {
			factors.add(factors.size(), exp);
		} else {
			throw new IllegalArgumentException("Expected a monom, but found " + exp);
		}
	}

	private static Expression flatExpression(final Expression exp, final TermFactory tf) {
		if (exp instanceof FunctionTerm) {
			FunctionTerm fTerm = (FunctionTerm) exp;
			if (fTerm.getChildAt(0) instanceof MinusSign) {
				return flatExpression(tf.createMult(tf.createConstant(BigDecimal.valueOf(-1)), (Expression) fTerm.getChildAt(1)), tf);
			}
			Expression left = flatExpression((Expression) fTerm.getChildAt(1), tf);
			Expression right = flatExpression((Expression) fTerm.getChildAt(2), tf);
			if (fTerm.getChildAt(0) instanceof Plus) {
				return tf.createPlus(left, right);
			} else if (fTerm.getChildAt(0) instanceof Minus) {
				return flatExpression(tf.createPlus(left, tf.createMult(tf.createConstant(BigDecimal.valueOf(-1)), right)), tf);
			} else if (fTerm.getChildAt(0) instanceof Mult) {
				if (left instanceof FunctionTerm) {
					FunctionTerm leftTerm = (FunctionTerm) left;
					if (leftTerm.getChildAt(0) instanceof Plus) {
						return flatExpression(tf.createPlus(tf.createMult((Expression) leftTerm.getChildAt(1), right), tf.createMult((Expression) leftTerm.getChildAt(2), right)), tf);
					} else if (leftTerm.getChildAt(0) instanceof Minus) {
						return flatExpression(tf.createMinus(tf.createMult((Expression) leftTerm.getChildAt(1), right), tf.createMult((Expression) leftTerm.getChildAt(2), right)), tf);
					}
				}
				if (right instanceof FunctionTerm) {
					FunctionTerm rightTerm = (FunctionTerm) right;
					if (rightTerm.getChildAt(0) instanceof Plus) {
						return flatExpression(tf.createPlus(tf.createMult(left, (Expression) rightTerm.getChildAt(1)), tf.createMult(left, (Expression) rightTerm.getChildAt(2))), tf);
					} else if (rightTerm.getChildAt(0) instanceof Minus) {
						return flatExpression(tf.createMinus(tf.createMult(left, (Expression) rightTerm.getChildAt(1)), tf.createMult(left, (Expression) rightTerm.getChildAt(2))), tf);
					}
				}
				return tf.createMult(left, right);
			} else if (fTerm.getChildAt(0) instanceof Div) {
				return flatExpression(tf.createMult(left, tf.createExp(right, tf.createConstant(BigDecimal.valueOf(-1)))), tf);
			}
		}
		return exp;
	}

	private static Expression getSumOfFactorsOfDot(final Expression polynom, final Dot d, final TermFactory tf) {
		if (polynom instanceof FunctionTerm) {
			FunctionTerm fTerm = (FunctionTerm) polynom;
			if (fTerm.getChildAt(0) instanceof Plus) {
				Expression left = getSumOfFactorsOfDot((Expression) fTerm.getChildAt(1), d, tf);
				Expression right = getSumOfFactorsOfDot((Expression) fTerm.getChildAt(2), d, tf);
				return tf.createPlus(left, right);
			} else if (fTerm.getChildAt(0) instanceof Mult) {
				if (containsDot(fTerm, d)) {
					return collectFactors(fTerm, tf);
				} else {
					// skip summands without the dotted variable
					return tf.createConstant(BigDecimal.ZERO);
				}
			}
		} else if (polynom == d) {
			return tf.createConstant(BigDecimal.ONE);
		} else if (polynom instanceof Variable || polynom instanceof Constant) {
			return tf.createConstant(BigDecimal.ZERO);
		}
		throw new IllegalArgumentException("Expecting polynomial, found " + polynom);

	}

	private static Expression getSumOfRest(final Expression exp, final Dot d, final TermFactory tf) {
		if (exp instanceof FunctionTerm) {
			FunctionTerm fTerm = (FunctionTerm) exp;
			if (fTerm.getChildAt(0) instanceof Plus) {
				Expression left = getSumOfRest((Expression) fTerm.getChildAt(1), d, tf);
				Expression right = getSumOfRest((Expression) fTerm.getChildAt(2), d, tf);
				return tf.createPlus(left, right);
			} else if (fTerm.getChildAt(0) instanceof Mult) {
				if (!containsDot(fTerm, d)) {
					return collectFactors(fTerm, tf);
				}
			}
		} else if (exp instanceof Variable || exp instanceof Constant) {
			return exp;
		}
		return tf.createConstant(BigDecimal.ZERO);
	}

	private static Expression collectFactors(final Expression monom, final TermFactory tf) {
		if (monom instanceof FunctionTerm) {
			FunctionTerm fTerm = (FunctionTerm) monom;
			if (fTerm.getChildAt(0) instanceof Mult) {
				Expression left = collectFactors((Expression) fTerm.getChildAt(1), tf);
				Expression right = collectFactors((Expression) fTerm.getChildAt(2), tf);
				return tf.createMult(left, right);
			} else if(fTerm.getChildAt(0) instanceof Exp) {
				return monom;
			} else {
				throw new IllegalArgumentException("Monoms cannot contain operators other than multiplication! Found: " + monom);
			}
		} else if (monom instanceof Variable || monom instanceof Constant) {
			return monom;
		} else if (monom instanceof Dot) {
			// ignore derivatives
			return tf.createConstant(BigDecimal.ONE);
		}
		throw new IllegalArgumentException("Unknown operator in " + monom);
	}
	
	/**
	 * Checks whether the DiffSystem is normalized, meaning that only Equations may contain Dots 
	 * and each Equation may only contain up to one Dot.
	 * 
	 * @param diffSystem
	 * @return
	 */
	public static boolean isNormalized(DiffSystem diffSystem) {
		for (ProgramElement p : diffSystem) {
			if(!isNormalized(p)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isNormalized(ProgramElement childAt) {
		if (childAt instanceof And || childAt instanceof Or) {
			return (isNormalized(((CompoundFormula) childAt).getChildAt(0)) && isNormalized(((CompoundFormula) childAt).getChildAt(1)));
		} else if (childAt instanceof PredicateTerm) {
			PredicateTerm pt = (PredicateTerm) childAt;
			if (pt.getChildAt(0) instanceof Equals) {
				return (pt.getChildAt(1) instanceof Dot || !containsDots(pt.getChildAt(1))) && !containsDots(pt.getChildAt(2));
			}
		} else if (childAt instanceof Exists || childAt instanceof Forall) {
			return isNormalized(((CompoundFormula) childAt).getChildAt(1));
		}
		return !containsDots(childAt);
	}
}
