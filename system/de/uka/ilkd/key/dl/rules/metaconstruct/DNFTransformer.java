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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.QuantifierType;
import de.uka.ilkd.key.dl.formulatools.ReplaceVisitor;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.And;
import de.uka.ilkd.key.dl.model.Biimplies;
import de.uka.ilkd.key.dl.model.CompoundFormula;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.DLStatementBlock;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Dot;
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
import de.uka.ilkd.key.dl.model.Variable;
import de.uka.ilkd.key.dl.model.VariableDeclaration;
import de.uka.ilkd.key.dl.model.VariableType;
import de.uka.ilkd.key.dl.model.impl.TermFactoryImpl;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * @author jdq
 * 
 */
public class DNFTransformer extends AbstractDLMetaOperator {

	private static class Pair {
		QuantifierType type;
		List<String> decl;
		VariableType sort;

		/**
		 * @param decl
		 * @param type
		 */
		public Pair(List<String> decl, QuantifierType type, VariableType sort) {
			super();
			this.decl = decl;
			this.type = type;
			this.sort = sort;
		}

	}

	private static class Result {
		List<Pair> quantifiers = new LinkedList<Pair>();
		Formula form;
	}

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
	/*@Override*/
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
	/*@Override*/
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
			// TODO: check if there is a disjunction in the formula
			Formula form = null;
			for (Formula f : forms) {
				if (form == null) {
					form = f;
				} else {
					form = tf.createAnd(form, f);
				}
			}
			Result r = createPrenexForm(form, services.getNamespaces(), tf);
			Formula result = r.form;
			// transform formula result to DNF
			result = createDNF(r, tf);

			s = tf.createDiffSystem(Collections.singletonList(result));
			// add all old annotations to the new diff system
			s.setDLAnnotations(system.getDLAnnotations());
			JavaBlock res = JavaBlock.createJavaBlock(new DLStatementBlock(s));
			return de.uka.ilkd.key.logic.TermFactory.DEFAULT.createTerm(
					modality, new Term[] { post },
					new ImmutableArray[0], res);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * @param result
	 * @param tf
	 * @return
	 */
	private Formula createDNF(Result r, TermFactory tf) {
		// TODO: preserve the order of the subformulas (insert in place)
		Formula result = r.form;
		// split or
		Set<Set<Formula>> or = new LinkedHashSet<Set<Formula>>();
		or.add(new LinkedHashSet<Formula>());
		or.iterator().next().add(result);
		LinkedHashSet<Set<Formula>> newOr = new LinkedHashSet<Set<Formula>>();
		boolean changed = true;
		while (changed) {
			changed = false;
			for (Set<Formula> and : or) {
				Set<Formula> newAnd = new LinkedHashSet<Formula>(and);
				for (Formula f : and) {
					boolean checkDot = checkDot(f);
					if (checkDot && f instanceof And) {
						newAnd.remove(f);
						newAnd.add((Formula) ((And) f).getChildAt(0));
						newAnd.add((Formula) ((And) f).getChildAt(1));
						changed = true;
					} else if (checkDot && f instanceof Or) {
						// TODO: maybe cache forms, as we perform the same
						// actions on it, as on newAnd
						newAnd.remove(f);
						Set<Formula> forms = new LinkedHashSet<Formula>(newAnd);
						newOr.add(forms);
						// now forms and and are the same and both known
						// elements of "newOr"
						forms.add((Formula) ((Or) f).getChildAt(0));
						newAnd.add((Formula) ((Or) f).getChildAt(1));
						changed = true;
					}
				}
				newOr.add(newAnd);
			}
			or = newOr;
			newOr = new LinkedHashSet<Set<Formula>>();
		}
		result = null;
		Collections.reverse(r.quantifiers);

		for (Set<Formula> and : or) {
			Formula con = null;
			Set<String> vars = new HashSet<String>();
			for (Formula f : and) {
				vars.addAll(getVariables(f));
				if (con == null) {
					con = f;
				} else {
					con = tf.createAnd(con, f);
				}
			}
			// add quantifiers before each conjuncted element
			for (Pair p : r.quantifiers) {
				if (p.type == QuantifierType.FORALL) {
					// we have to stop if we see an universal quantifier... this
					// could only be added to the complete formula
					break;
				}
				List<String> varDecls = new ArrayList<String>();
				for (String s : p.decl) {
					if (vars.contains(s)) {
						varDecls.add(s);
					}
				}
				if (!varDecls.isEmpty()) {
					VariableDeclaration decl = tf.createVariableDeclaration(
							p.sort, varDecls, false, false);
					switch (p.type) {
					case FORALL:
						throw new IllegalStateException(
								"Universal quantifers cannot be splitted by disjunctions");
					case EXISTS:
						con = tf.createExists(decl, con);
						break;
					default:
						throw new IllegalStateException(
								"Unknown Quantifier type: " + p.type);
					}
				}
			}
			if (result == null) {
				result = con;
			} else {
				result = tf.createOr(result, con);
			}
		}
		// add the rest of the quantifiers in front of the complete formula
		boolean forAllFound = false;
		outerloop: for (Pair p : r.quantifiers) {
			if (!forAllFound && p.type == QuantifierType.FORALL) {
				forAllFound = true;
			} else if (!forAllFound) {
				continue outerloop;
			}
			VariableDeclaration decl = tf.createVariableDeclaration(p.sort,
					p.decl, false, false);
			swi: switch (p.type) {
			case FORALL:
				result = tf.createForall(decl, result);
				break swi;
			case EXISTS:
				result = tf.createExists(decl, result);
				break swi;
			default:
				throw new IllegalStateException("Unknown Quantifier type: "
						+ p.type);
			}
		}
		return result;
	}

	/**
	 * @param f
	 * @return
	 */
	private Collection<? extends String> getVariables(DLProgramElement f) {
		Set<String> vars = new HashSet<String>();
		if (f instanceof DLNonTerminalProgramElement) {
			for (ProgramElement p : (DLNonTerminalProgramElement) f) {
				vars.addAll(getVariables((DLProgramElement) p));
			}
		} else {
			if (f instanceof Variable) {
				vars.add(((Variable) f).getElementName().toString());
			}
		}
		return vars;
	}

	/**
	 * @param forms
	 * @param namespaces
	 * @return
	 */
	private Result createPrenexForm(Formula t, NamespaceSet namespaces,
			TermFactory tf) {
		Result r = new Result();
		if (!checkDot(t)) {
			r.form = t;
			return r;
		}
		if (t instanceof Forall || t instanceof Exists) {
			CompoundFormula f = (CompoundFormula) t;
			VariableDeclaration decl = (VariableDeclaration) f.getChildAt(0);
			List<String> quantifiers = new ArrayList<String>();
			HashMap<QuantifiableVariable, Term> map = new HashMap<QuantifiableVariable, Term>();
			for (int i = 1; i < decl.getChildCount(); i++) {
				String string = ((Variable) decl.getChildAt(i)).getElementName().toString();
				Name n = new Name(namespaces.getUniqueName(string, true));
				LogicVariable sym = new LogicVariable(n, RealLDT.getRealSort());
				namespaces.variables().add(sym);
				map.put(new LogicVariable(new Name(string), RealLDT
						.getRealSort()), TermBuilder.DF.var(sym));
				quantifiers.add(n.toString());
			}
			r.form = (Formula) ReplaceVisitor.convert(f.getChildAt(1), map, tf);
			if (t instanceof Forall) {
				r.quantifiers.add(new Pair(quantifiers, QuantifierType.FORALL,
						decl.getType()));
			} else {
				r.quantifiers.add(new Pair(quantifiers, QuantifierType.EXISTS,
						decl.getType()));
			}
			Result prenexForm = createPrenexForm(r.form, namespaces, tf);
			r.quantifiers.addAll(prenexForm.quantifiers);
			r.form = prenexForm.form;
		} else if (t instanceof And) {
			Result one = createPrenexForm((Formula) ((And) t).getChildAt(0),
					namespaces, tf);
			Result two = createPrenexForm((Formula) ((And) t).getChildAt(1),
					namespaces, tf);
			r.quantifiers.addAll(one.quantifiers);
			r.quantifiers.addAll(two.quantifiers);
			r.form = tf.createAnd(one.form, two.form);
		} else if (t instanceof Or) {
			Result one = createPrenexForm((Formula) ((Or) t).getChildAt(0),
					namespaces, tf);
			Result two = createPrenexForm((Formula) ((Or) t).getChildAt(1),
					namespaces, tf);
			r.quantifiers.addAll(one.quantifiers);
			r.quantifiers.addAll(two.quantifiers);
			r.form = tf.createOr(one.form, two.form);
		} else if (t instanceof Implies) {
			Result one = createPrenexForm(
					(Formula) ((Implies) t).getChildAt(0), namespaces, tf);
			Result two = createPrenexForm(
					(Formula) ((Implies) t).getChildAt(1), namespaces, tf);
			r.quantifiers.addAll(one.quantifiers);
			r.quantifiers.addAll(two.quantifiers);
			r.form = tf.createOr(tf.createNot(one.form), two.form);

		} else if (t instanceof Biimplies) {
			Result one = createPrenexForm((Formula) ((Biimplies) t)
					.getChildAt(0), namespaces, tf);
			Result two = createPrenexForm((Formula) ((Biimplies) t)
					.getChildAt(1), namespaces, tf);
			r.quantifiers.addAll(one.quantifiers);
			r.quantifiers.addAll(two.quantifiers);
			r.form = tf.createAnd(
					tf.createOr(tf.createNot(one.form), two.form), tf.createOr(
							one.form, tf.createNot(two.form)));
		} else if (t instanceof Not) {
			Result pf = createPrenexForm((Formula) ((Not) t).getChildAt(0),
					namespaces, tf);
			r.form = tf.createNot(pf.form);
			r.quantifiers.addAll(pf.quantifiers);
		} else if (t instanceof PredicateTerm) {
			r.form = t;
		}
		return r;
	}

	private Formula createNegationNormalform(Formula t, boolean negated,
			TermFactory tf) {
		if (!checkDot(t)) {
			if (negated) {
				return tf.createNot(t);
			} else {
				return t;
			}
		}
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
					expr.add((Expression) ((PredicateTerm) t).getChildAt(i));
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
				return tf.createAnd(createNegationNormalform(
						(Formula) ((And) t).getChildAt(0), false, tf),
						createNegationNormalform((Formula) ((And) t)
								.getChildAt(1), false, tf));
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
	 * @param t
	 * @return
	 */
	private boolean checkDot(ProgramElement t) {
		if (t instanceof Dot) {
			return true;
		}
		if (t instanceof DLNonTerminalProgramElement) {
			for (ProgramElement p : (DLNonTerminalProgramElement) t) {
				if (checkDot(p)) {
					return true;
				}
			}
		}
		return false;
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
