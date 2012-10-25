/***************************************************************************
 *   Copyright (C) 2007 by Andre Platzer                                   *
 *   @informatik.uni-oldenburg.de                                          *
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

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
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.DLStatementBlock;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Div;
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
	private Term diffNormalize(Term term, Services services) throws SolverException {
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

	// TODO text nachbessern
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
	private DiffSystem getNormalizedSystem(DiffSystem sys, Services s) {
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

	private ProgramElement replaceDottedVariables(ProgramElement childAt, HashMap<Variable, LogicalVariable> renaming, TermFactory tf) {
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
			result = normalizePredicateTerm((PredicateTerm) childAt, renaming, tf);
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

	// private boolean containsDots(DLProgramElement pred) {
	// if (pred instanceof Dot) {
	// return true;
	// } else if (pred instanceof DLNonTerminalProgramElement) {
	// DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) pred;
	// for (ProgramElement p : dlnpe) {
	// if (containsDots((DLProgramElement) p)) {
	// return true;
	// }
	// }
	// return false;
	// }
	// return false;
	// }

	private Dot getSomeDot(Expression exp) {
		if (exp instanceof Dot) {
			return (Dot) exp;
		} else if (exp instanceof FunctionTerm) {
			FunctionTerm fTerm = (FunctionTerm) exp;
			Dot d = null;
			for (int i = 1; i < fTerm.getChildCount(); i++) {
				d = getSomeDot((Expression) fTerm.getChildAt(i));
				if (d != null) {
					return d;
				}
			}
		}
		return null;
	}

	private boolean containsDot(Expression exp, Dot d) {
		if (exp instanceof Dot && ((Dot) exp).getChildAt(0).equals(d.getChildAt(0))) {
			return true;
		} else if (exp instanceof FunctionTerm) {
			FunctionTerm fTerm = (FunctionTerm) exp;
			for (int i = 1; i < fTerm.getChildCount(); i++) {
				if (containsDot((Expression)((FunctionTerm) exp).getChildAt(i), d)) {
					return true;
				}
			}
			return false;
		}
		return false;
	}

	private PredicateTerm normalizePredicateTerm(PredicateTerm pTerm, HashMap<Variable, LogicalVariable> renaming, TermFactory tf) {
		if (pTerm.getChildAt(0) instanceof Equals) {
			Dot d = getSomeDot((Expression) pTerm.getChildAt(1));
			if (d != null) {
				PredicateTerm result = transformEquation((Expression) pTerm.getChildAt(1), (Expression) pTerm.getChildAt(2), d, tf);
				List<Expression> children = new ArrayList<Expression>();
				if(result.getChildAt(1) instanceof Dot) {
					children.add((Expression) result.getChildAt(1));
				} else {
					children.add((Expression) replaceDottedVariables(result.getChildAt(1), renaming, tf));
				}
				children.add((Expression) replaceDottedVariables(result.getChildAt(2), renaming, tf));
				return tf.createPredicateTerm((Predicate) pTerm.getChildAt(0), children);
			} else {
				d = getSomeDot((Expression) pTerm.getChildAt(2));
				if (d != null) {
					PredicateTerm result = transformEquation((Expression) pTerm.getChildAt(2), (Expression) pTerm.getChildAt(1), d, tf);
					List<Expression> children = new ArrayList<Expression>();
					if(result.getChildAt(1) instanceof Dot) {
						children.add((Expression) result.getChildAt(1));
					} else {
						children.add((Expression) replaceDottedVariables(result.getChildAt(1), renaming, tf));
					}
					children.add((Expression) replaceDottedVariables(result.getChildAt(2), renaming, tf));
					return tf.createPredicateTerm((Predicate) pTerm.getChildAt(0), children);
				}
			}
			return pTerm;
		} else {
			List<Expression> children = new ArrayList<Expression>();
			children.add((Expression) replaceDottedVariables(pTerm.getChildAt(1), renaming, tf));
			children.add((Expression) replaceDottedVariables(pTerm.getChildAt(2), renaming, tf));
			return tf.createPredicateTerm((Predicate) pTerm.getChildAt(0), children);
		}
	}
		
	// Assumes that exp1 contains d!
	private PredicateTerm transformEquation(Expression exp1, Expression exp2, Dot d, TermFactory tf) {
		if (exp1 instanceof Dot) {
			if (((Dot) exp1).getChildAt(0).equals(d.getChildAt(0))) {
				List<Expression> children = new ArrayList<Expression>();
				children.add(exp1);
				children.add(exp2);
				return tf.createPredicateTerm(tf.createEquals(), children);
			} else {
				throw new IllegalArgumentException("Expected: "+d+", but found: "+exp1);
			}
		} else if (exp1 instanceof FunctionTerm) {
			FunctionTerm fTerm = (FunctionTerm) exp1;
			if(fTerm.getChildAt(0) instanceof MinusSign) {
				return transformEquation((Expression)fTerm.getChildAt(1), tf.createMinusSign(exp2), d, tf);
			} else {
				Expression dottedExpression = null, other = null;
				boolean inverse;
				if (containsDot((Expression) fTerm.getChildAt(1), d)) {
					dottedExpression = (Expression) fTerm.getChildAt(1);
					other = (Expression) fTerm.getChildAt(2);
					inverse = false;
				} else {
					dottedExpression = (Expression) fTerm.getChildAt(2);
					other = (Expression) fTerm.getChildAt(1);
					inverse = true;
				}

				if (fTerm.getChildAt(0) instanceof Plus) {
					exp2 = tf.createMinus(exp2, other);
					return transformEquation(dottedExpression, exp2, d, tf);
				} else if (fTerm.getChildAt(0) instanceof Minus) {
					
					if(inverse) {
						exp2 = tf.createMinusSign(tf.createMinus(exp2, other));
					} else {
						exp2 = tf.createPlus(exp2, other);
					}
					
					return transformEquation(dottedExpression, exp2, d, tf);
				} else if (fTerm.getChildAt(0) instanceof Mult) {
					if((other instanceof Constant && !(((Constant) other).getValue().equals(BigDecimal.ZERO))) ) {
						return transformEquation(dottedExpression, tf.createDiv(exp2, other), d, tf);
					}
				} else if (fTerm.getChildAt(0) instanceof Div) {
					
					if(inverse) {
						dottedExpression = tf.createMult(dottedExpression, exp2);
						exp2 = other;
					} else {
						exp2 = tf.createMult(exp2, other);
					}
					
					return transformEquation(dottedExpression, exp2, d, tf);
				} else {
					throw new IllegalArgumentException(exp1+" contains unsupported Operator: "+fTerm.getChildAt(0).getClass());
				}
				List<Expression> children = new ArrayList<Expression>();
				children.add(exp1);
				children.add(exp2);
				return tf.createPredicateTerm(tf.createEquals(), children);
			}
		} else {
			throw new IllegalArgumentException("Not supported sub-class of expression: "+exp1.getClass());
		}
	}
}
