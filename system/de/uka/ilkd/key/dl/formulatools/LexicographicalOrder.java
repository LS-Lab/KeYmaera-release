/**
 * 
 */
package de.uka.ilkd.key.dl.formulatools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.uka.ilkd.key.dl.formulatools.collector.AllCollector;
import de.uka.ilkd.key.dl.formulatools.collector.filter.FilterVariableCollector;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.Div;
import de.uka.ilkd.key.dl.model.Exp;
import de.uka.ilkd.key.dl.model.Greater;
import de.uka.ilkd.key.dl.model.GreaterEquals;
import de.uka.ilkd.key.dl.model.Less;
import de.uka.ilkd.key.dl.model.LessEquals;
import de.uka.ilkd.key.dl.model.Minus;
import de.uka.ilkd.key.dl.model.MinusSign;
import de.uka.ilkd.key.dl.model.Mult;
import de.uka.ilkd.key.dl.model.Plus;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Equality;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.logic.op.RigidFunction;

/**
 * @author jdq TODO documentation
 */
public class LexicographicalOrder {

	public static class TermInformations {

		private class Info {
			int degree = 0;
		}

		private int degree = 0;
		private int depth;

		private Set<Term> variables = new HashSet<Term>();

		private Term t;

		public TermInformations(Term t) {
			this.t = t;
			this.depth = t.depth();
			collectInformations(t);
		}

		private Info collectInformations(Term current) {
			if (current.op() == Op.ALL || current.op() == Op.EX) {
				Info collectInformations = collectInformations(current.sub(0));
				// degree of the children + 1
				degree = collectInformations.degree + 1;
			} else if (current.op() == Op.AND
					|| current.op() == Op.OR
					|| current.op() == Op.IMP
					|| current.op() == Op.EQV
					|| current.op() == RealLDT.getFunctionFor(Greater.class)
					|| current.op() == RealLDT
							.getFunctionFor(GreaterEquals.class)
					|| current.op() == RealLDT.getFunctionFor(LessEquals.class)
					|| current.op() == RealLDT.getFunctionFor(Less.class)
					|| current.op() instanceof Equality) {
				// get maximum degree of the children
				List<Info> infos = new ArrayList<Info>();
				for (int i = 0; i < current.arity(); i++) {
					infos.add(collectInformations(current.sub(i)));
				}
				for (Info i : infos) {
					if (i.degree > degree) {
						degree = i.degree;
					}
				}
			} else if (current.op() instanceof RigidFunction) {
				if (current.arity() > 1) {
					if (current.op() == RealLDT.getFunctionFor(Plus.class)
							|| current.op() == RealLDT
									.getFunctionFor(Minus.class)) {
						// get maximum degree of the children
						List<Info> infos = new ArrayList<Info>();
						for (int i = 0; i < current.arity(); i++) {
							infos.add(collectInformations(current.sub(i)));
						}
						for (Info i : infos) {
							if (i.degree > degree) {
								degree = i.degree;
							}
						}
					} else if (current.op() == RealLDT
							.getFunctionFor(Mult.class)) {
						// get sum of degrees of the children
						for (int i = 0; i < current.arity(); i++) {
							degree += collectInformations(current.sub(i)).degree;
						}
					} else if (current.op() == RealLDT
							.getFunctionFor(Div.class)) {
						// for x/y get degree(x) - degree(y)
						degree = collectInformations(current.sub(0)).degree
								- collectInformations(current.sub(1)).degree;
					} else if (current.op() == RealLDT
							.getFunctionFor(Exp.class)) {
						degree += collectInformations(current.sub(0)).degree
								* Integer.parseInt(current.sub(1).toString());
					} else {
						RigidFunction rf = (RigidFunction) current.op();
						if (rf.isSkolem()) {
							variables.add(current);
							Info i = new Info();
							i.degree = 1;
							return i;
						}
					}
				} else if (current.arity() == 1) {
					if (current.op() == RealLDT.getFunctionFor(MinusSign.class)) {
						degree = collectInformations(current.sub(0)).degree;
					}
				} else if (current.arity() == 0) {
					Info i = new Info();
					if (current.op() instanceof ProgramVariable
							|| current.op() instanceof Metavariable
							|| current.op() instanceof LogicVariable) {
						variables.add(current);
						i.degree = 1;
						return i;
					} else if (current.op() instanceof RigidFunction) {
						try {
							Integer.parseInt(current.op().toString());
							i.degree = 0;
							return i;
						} catch (Exception e) {
							variables.add(current);
							i.degree = 1;
							return i;
						}
					}
				}
			}
			throw new IllegalArgumentException("Dont know what to do with "
					+ current.op() + " of class " + current.op().getClass());
		}

		public int getDegree() {
			return degree;
		}

		public int getDepth() {
			return depth;
		}

		public Set<Term> getVariables() {
			return variables;
		}

		public Term getT() {
			return t;
		}
	}

	public static Queue<Term> getOrder(Set<Term> terms) {
		Queue<Term> result = new LinkedList<Term>();
		Set<TermInformations> infos = new HashSet<TermInformations>();
		TreeMap<String, Integer> variables = new TreeMap<String, Integer>();
		for (Term t : terms) {
			TermInformations termInformations = new TermInformations(t);
			infos.add(termInformations);
			for (Term var : termInformations.getVariables()) {
				String s = var.op().toString();
				if (s.contains("_")) {
					int i = Integer.parseInt(s.substring(s.indexOf('_') + 1));
					i++;
					assert i > 0;
					s = s.substring(0, s.indexOf('_'));
					if (variables.containsKey(s)) {
						// we add the maximal index to the map
						if (variables.get(s) < i) {
							variables.put(s, i);
						}
					} else {
						variables.put(s, i);
					}
				} else {
					variables.put(s, 0);
				}
			}
		}

		final List<Comparator<TermInformations>> comparators = new ArrayList<Comparator<TermInformations>>();
		comparators.add(new RecencyOrder(variables));
		comparators.add(new Comparator<TermInformations>() {

			@Override
			public int compare(TermInformations o1, TermInformations o2) {
				return o1.degree - o2.degree;
			}

		});

		LinkedHashSet<TermInformations> tResult = new LinkedHashSet<TermInformations>();
		final HashSet<Term> currentVariables = new HashSet<Term>();

		comparators.add(new Comparator<TermInformations>() {

			@Override
			public int compare(TermInformations o1, TermInformations o2) {
				int count1 = 0;
				int count2 = 0;
				for (Term var : currentVariables) {
					o1loop: for (Term o1Var : o1.getVariables()) {
						if (o1Var.toString().equals(var.toString())) {
							count1++;
							break o1loop;
						}
					}
					o2loop: for (Term o2Var : o2.getVariables()) {
						if (o2Var.toString().equals(var.toString())) {
							count2++;
							break o2loop;
						}
					}
				}
				return (o1.getVariables().size() - count1)
						- (o2.getVariables().size() - count2);
			}

		});
		
		comparators.add(new Comparator<TermInformations>() {

			@Override
			public int compare(TermInformations o1, TermInformations o2) {
				return o1.depth - o2.depth;
			}
			
		});

		Comparator<TermInformations> mainComparator = new Comparator<TermInformations>() {

			@Override
			public int compare(TermInformations o1, TermInformations o2) {
				for (Comparator<TermInformations> c : comparators) {
					int res = c.compare(o1, o2);
					if (res != 0) {
						return res;
					}
				}
				return o1.toString().compareTo(o2.toString());
			}
		};
		TreeSet<TermInformations> sortedTerms = new TreeSet<TermInformations>(
				mainComparator);

		// while we got terms we havent added to our result, we need to reorder
		// all informations left and take the first one
		while (!infos.isEmpty()) {
			sortedTerms.clear();
			sortedTerms.addAll(infos);
			TermInformations first = sortedTerms.first();

			currentVariables.addAll(first.getVariables());
			tResult.add(first);

			sortedTerms.remove(first);
			// we use sortedTerms as new sorting may be faster
			infos.clear();
			infos.addAll(sortedTerms);
		}
		for (TermInformations i : tResult) {
			result.add(i.getT());
		}

		return result;
	}
}
