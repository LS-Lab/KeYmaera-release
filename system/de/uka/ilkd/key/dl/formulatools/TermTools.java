/**
 * 
 */
package de.uka.ilkd.key.dl.formulatools;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.collection.ImmutableSLList;
import de.uka.ilkd.key.dl.formulatools.TermRewriter.Match;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.NonRigidFunction;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Named;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.Junctor;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.Quantifier;
import de.uka.ilkd.key.logic.op.RigidFunction;

/**
 * @author andre
 * 
 */
public class TermTools {
	/**
	 * Explicit n-ary-fied version of {@link
	 * de.uka.ilkd.logic.TermFactory#createJunctorTerm(Junctor,Term[])}.
	 * 
	 * @see orbital.logic.functor.Functionals#foldRight
	 * @internal almost identical to
	 * @see #createJunctorTermNAry(Term,Junctor,IteratorOfTerm)
	 */
	public static final Term createJunctorTermNAry(Term c, Junctor op,
			Iterator<ConstrainedFormula> i, Set<Term> skip, boolean constrainedFormulaIt) {
		Term construct = c;
		while (i.hasNext()) {
			ConstrainedFormula f = i.next();
			Term t = f.formula();
			if (!skip.contains(t)) {
				// ignore tautological constraints, since they do not contribute
				// to
				// the specification
				// but report others
				if (!f.constraint().isBottom())
					throw new IllegalArgumentException(
							"there is a non-tautological constraint on " + f
									+ ". lower constraints, first");
				construct = TermFactory.DEFAULT.createJunctorTermAndSimplify(
						op, construct, t);
			}
		}
		return construct;
	}

	/**
	 * Explicit n-ary-fied version of {@link
	 * de.uka.ilkd.logic.TermFactory#createJunctorTerm(Junctor,Term[])}.
	 * 
	 * @see orbital.logic.functor.Functionals#foldRight
	 * @internal almost identical to
	 * @see #createJunctorTermNAry(Term,Junctor,IteratorOfTerm)
	 */
	public static final Term createJunctorTermNAry(Term c, Junctor op,
			Iterator<Term> i, Set<Term> skip) {
		Term construct = c;
		while (i.hasNext()) {
			Term t = i.next();
			if (!skip.contains(t)) {
				construct = TermFactory.DEFAULT.createJunctorTermAndSimplify(
						op, construct, t);
			}
		}
		return construct;
	}

	/**
	 * Splits a formula along all its conjunctions into the set of its
	 * conjuncts.
	 * 
	 * @param form
	 * @return
	 */
	public static Set<Term> splitConjuncts(Term form) {
		Set<Term> conjuncts = new LinkedHashSet<Term>();
		if (form.op() == Junctor.AND) {
			for (int i = 0; i < form.arity(); i++) {
				conjuncts.addAll(splitConjuncts(form.sub(i)));
			}
		} else {
			conjuncts.add(form);
		}
		return conjuncts;
	}

	/**
	 * Checks whether
	 * 
	 * @param set
	 * @param formula
	 * @return
	 */
	private static boolean containsAll(Set<Term> set, Set<Term> formula) {
		all: for (Term fml : formula) {
			if (set.contains(fml)) {
				continue;
			}
			for (Term b : set) {
				if (b.toString().equals(fml.toString())) {
					// @todo assert namespaces.unique
					System.out
							.println(" WARNING: identical printout with different representation "
									+ b + " and " + fml);
					// findDelta(b, fml);
					// FIXME string comparison is an ugly hack
					continue all;
				}
			}
			return false;
		}
		return true;
	}

	// debugging helper
	private static void findDelta(Term b, Term fml) {
		if (b.equals(fml)) {
			return;
		}
		if (b.arity() != fml.arity()) {
			System.out.println("arity difference " + b + " : " + b.arity()
					+ "\t" + fml + " : " + fml.arity());
		}
		if (!b.op().equals(fml.op())) {
			System.out.println("operator difference " + b + " : " + b.op()
					+ "@" + b.op().getClass() + "\t" + fml + " : " + fml.op()
					+ "@" + fml.op().getClass());
			System.out.println();
		}
		for (int i = 0; i < b.arity(); i++) {
			findDelta(b.sub(i), fml.sub(i));
		}
	}

	/**
	 * Quickly checks whether sup subsumes sub, i.e., all conjuncts of formula
	 * sub are identical to one of the conjuncts of sub.
	 * 
	 * @param set
	 * @param formula
	 * @return
	 */
	public static boolean subsumes(Term sup, Term sub) {
		return subsumes(splitConjuncts(sup), sub);
	}

	public static boolean subsumes(Set<Term> sup, Term sub) {
		return subsumes(sup, splitConjuncts(sub));
	}

	public static boolean subsumes(Set<Term> sup, Set<Term> sub) {
		return containsAll(sup, sub);
	}

	/**
	 * checks whether term x occurs in term t
	 * 
	 * @param x
	 * @param t
	 * @return
	 */
	public static boolean occursIn(final Term x, Term t) {
		final boolean result[] = new boolean[] { false };
		t.execPreOrder(new Visitor() {

			/*@Override*/
			public void visit(Term visited) {
				if (visited.equals(x)) {
					result[0] = true;
				}
			}
		});
		return result[0];
	}

	private static final List<String> builtinList = Arrays.asList(new String[] {
			"add", "sub", "neg", "mul", "div", "exp", "gt", "geq", "equals",
			"neq", "leq", "lt" });

	/**
	 * Get the set of all symbols that occur in a formula or term.
	 */
	public static Set<Operator> getSignature(Term form) {
		Set<Operator> result = new LinkedHashSet<Operator>();
		for (int i = 0; i < form.arity(); i++) {
			result.addAll(getSignature(form.sub(i)));
		}
		if (form.op() == Op.FALSE) {
			return Collections.EMPTY_SET;
		} else if (form.op() == Op.TRUE) {
			return Collections.EMPTY_SET;
		} else if (form.op().name().toString().equals("equals")) {
			return result;
		} else if (form.op() instanceof Function) {
			Function f = (Function) form.op();
			if (builtinList.contains(f.name().toString())) {
				return result;
			} else {
				try {
					// number
					new BigDecimal(form.op().name().toString());
					return result;
				} catch (NumberFormatException e) {
					result.add(f);
					return result;
				}
			}
		} else if (form.op() instanceof LogicVariable
				|| form.op() instanceof de.uka.ilkd.key.logic.op.ProgramVariable
				|| form.op() instanceof Metavariable) {
			result.add(form.op());
			return result;
		} else if (form.op() instanceof Junctor) {
			if (form.op() == Junctor.AND || form.op() == Junctor.OR
					|| form.op() == Junctor.IMP || form.op() == Junctor.NOT) {
				return result;
			}
		} else if (form.op() instanceof Quantifier) {
			// @todo should we remove bound variables?
			return result;
		}
		throw new IllegalArgumentException("Don't know about: " + form
				+ "Operator was: " + form.op());
	}

	/**
	 * Get the set of all symbols that occur in a set of formulas or terms.
	 */
	public static Set<Operator> getSignature(Collection<Term> formulas) {
		Set<Operator> s = new LinkedHashSet<Operator>();
		for (Term t : formulas) {
			s.addAll(getSignature(t));
		}
		return s;
	}

	// more general helpers

	/**
	 * projection to programvariables
	 * 
	 * @param s
	 * @return
	 */
	public static Set<de.uka.ilkd.key.logic.op.ProgramVariable> projectProgramVariables(
			Set<Operator> s) {
		Set<de.uka.ilkd.key.logic.op.ProgramVariable> r = new LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable>();
		for (Operator o : s) {
			if (o instanceof de.uka.ilkd.key.logic.op.ProgramVariable) {
				r.add((de.uka.ilkd.key.logic.op.ProgramVariable) o);
			}
		}
		return r;
	}

	/**
	 * projection to dL programvariables
	 * 
	 * @param s
	 * @return
	 */
	public static Set<de.uka.ilkd.key.logic.op.ProgramVariable> projectDLProgramVariables(
			Set<Operator> s) {
		Set<de.uka.ilkd.key.logic.op.ProgramVariable> r = new LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable>();
		for (Operator o : s) {
			if (o instanceof de.uka.ilkd.key.logic.op.ProgramVariable) {
				r.add((de.uka.ilkd.key.logic.op.ProgramVariable) o);
			}
		}
		return r;
	}

	/**
	 * projects set of named things to the set of its respective names.
	 * 
	 * @param s
	 * @return
	 */
	public static Set<Name> projectNames(Set<? extends Named> s) {
		Set<Name> r = new LinkedHashSet<Name>();
		for (Named n : s) {
			r.add(n.name());
		}
		return r;
	}

	public static ImmutableList<Term> genericToOld(Collection<Term> c) {
		ImmutableList<Term> r = ImmutableSLList.nil();
		for (Term s : c) {
			r = r.append(s);
		}
		assert r.size() == c.size();
		return r;
	}

	public static List<Term> oldToGeneric(ImmutableList<Term> c) {
		List<Term> r = new java.util.ArrayList<Term>(c.size());
		for (Iterator<Term> i = c.iterator(); i.hasNext();) {
			r.add(i.next());
		}
		assert r.size() == c.size();
		return r;
	}

	public static class PairOfTermAndVariableList {
		private Term t;
		private List<String> variables = new ArrayList<String>();

		/**
		 * 
		 */
		public PairOfTermAndVariableList(Term t) {
			this.t = t;
		}

		/**
		 * @return the t
		 */
		public Term getT() {
			return t;
		}

		/**
		 * @return the variables
		 */
		public List<String> getVariables() {
			return variables;
		}
	}

	/**
	 * Quantify all skolem symbols occuring in the formula by replacing them
	 * with <functionname>$sk and adding universal quantifiers in front of the
	 * formula
	 * 
	 * @param t
	 * @return
	 */
	public static PairOfTermAndVariableList quantifyAllSkolemSymbols(Term t) {
		PairOfTermAndVariableList result = new PairOfTermAndVariableList(t);
		List<Term> skolem = new LinkedList<Term>();
		final Set<Term> skolemSym = new LinkedHashSet<Term>();
		result.t.execPreOrder(new Visitor() {

			/*@Override*/
			public void visit(Term visited) {
				if (visited.op() instanceof RigidFunction
						&& ((RigidFunction) visited.op()).isSkolem()) {
					skolemSym.add(visited);
				}
			}

		});
		skolem.addAll(SkolemfunctionTracker.INSTANCE.getOrderedList(skolemSym));

		Set<Match> matches = new LinkedHashSet<Match>();
		List<LogicVariable> vars = new ArrayList<LogicVariable>();
		for (Term sk : skolem) {
			LogicVariable logicVariable = new LogicVariable(new Name(sk.op()
					.name()
					+ "$sk"), sk.op().sort(new Term[0]));
			vars.add(logicVariable);
			matches.add(new Match((RigidFunction) sk.op(), TermBuilder.DF
					.var(logicVariable)));
			result.variables.add(logicVariable.name().toString());
		}
		result.t = TermRewriter.replace(result.t, matches);
		for (QuantifiableVariable v : vars) {
			result.t = TermBuilder.DF.all(v, result.t);
		}
		return result;
	}
	
	public static String program2String(ProgramElement p) {
		StringWriter w = new StringWriter();
		try {
			p.prettyPrint(new PrettyPrinter(w));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return w.toString();
	}
	
	   public static boolean containsNonRigids(ProgramElement element) {
	        if (element instanceof NonRigidFunction) {
	            return true;
	        } else if (element instanceof DLNonTerminalProgramElement) {
	            DLNonTerminalProgramElement ntpl = (DLNonTerminalProgramElement) element;
	            for (int i = 0; i < ntpl.getChildCount(); i++) {
	                if(containsNonRigids(ntpl.getChildAt(i))) {
	                    return true;
	                }
	            }
	        }
	        return false;

	    }
}
