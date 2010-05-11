/**
 * 
 */
package de.uka.ilkd.key.dl.formulatools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

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
import de.uka.ilkd.key.dl.model.Unequals;
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

	/**
	 * Represents structural information about terms: (maximal) degree of
	 * variables, term depth and variable occurrence information.
	 */
	public static class TermInformations {

		private class Info {
			int degree = 0;

			public Info(int i) {
				degree = i;
			}
		}

		private int degree = 0;
		private int depth;

		private Set<Term> variables = new LinkedHashSet<Term>();

		private Term t;

		public TermInformations(Term t) {
			this.t = t;
			this.depth = t.depth();
			degree = collectInformations(t).degree;
		}

		private Info collectInformations(Term current) {
			if (current.op() == Op.ALL || current.op() == Op.EX) {
				Info collectInformations = collectInformations(current.sub(0));
				// degree of the children + 1
				return new Info(collectInformations.degree + 1);
			} else if (current.op() == Op.AND
					|| current.op() == Op.OR
					|| current.op() == Op.IMP
					|| current.op() == Op.EQV
					|| current.op() == Op.NOT
					|| current.op() == RealLDT.getFunctionFor(Greater.class)
					|| current.op() == RealLDT
							.getFunctionFor(GreaterEquals.class)
					|| current.op() == RealLDT.getFunctionFor(LessEquals.class)
					|| current.op() == RealLDT.getFunctionFor(Less.class)
					|| current.op() == RealLDT.getFunctionFor(Unequals.class)
					|| current.op() instanceof Equality) {
				// get maximum degree of the children
				List<Info> infos = new ArrayList<Info>();
				for (int i = 0; i < current.arity(); i++) {
					infos.add(collectInformations(current.sub(i)));
				}
				Info res = new Info(0);
				for (Info i : infos) {
					if (i.degree > res.degree) {
						res.degree = i.degree;
					}
				}
				return res;
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
						Info res = new Info(0);
						for (Info i : infos) {
							if (i.degree > res.degree) {
								res.degree = i.degree;
							}
						}
						return res;
					} else if (current.op() == RealLDT
							.getFunctionFor(Mult.class)) {
						// get sum of degrees of the children
						Info res = new Info(0);
						for (int i = 0; i < current.arity(); i++) {
							res.degree += collectInformations(current.sub(i)).degree;
						}
						return res;
					} else if (current.op() == RealLDT
							.getFunctionFor(Div.class)) {
						// for x/y return maximal degree of the children
						List<Info> infos = new ArrayList<Info>();
						for (int i = 0; i < current.arity(); i++) {
							infos.add(collectInformations(current.sub(i)));
						}
						Info res = new Info(0);
						for (Info i : infos) {
							if (i.degree > res.degree) {
								res.degree = i.degree;
							}
						}
						return res;
					} else if (current.op() == RealLDT
							.getFunctionFor(Exp.class)) {
						Info res = new Info(0);
						try {
							res.degree += collectInformations(current.sub(0)).degree
									* Math.abs(Integer.parseInt(current.sub(1)
											.toString()));
						} catch (NumberFormatException noint) {
							// trouble case because it may not be decidable
							res.degree += collectInformations(current.sub(0)).degree
									* collectInformations(current.sub(1)).degree
									+ 10;
						}
						return res;
					} else {
						RigidFunction rf = (RigidFunction) current.op();
						// test for skolem function
						System.out.println(rf.name());//XXX
						if (rf.isSkolem()
								|| rf.name().toString().endsWith("$sk")) {
							variables.add(current);
							Info i = new Info(0);
							i.degree = 1;
							return i;
						}
					}
				} else if (current.arity() == 1) {
					Info res = new Info(0);
					if (current.op() == RealLDT.getFunctionFor(MinusSign.class)) {
						res.degree = collectInformations(current.sub(0)).degree;
						return res;
					}
				} else if (current.arity() == 0) {
					Info i = new Info(0);
					RigidFunction rf = (RigidFunction) current.op();
					// test for skolem function
					if (rf.isSkolem()
							|| rf.name().toString().endsWith("$sk")) {
						variables.add(current);
						i.degree = 1;
						return i;
					}
					try {
						Integer.parseInt(rf.name().toString());
						i.degree = 0;
						return i;
					} catch (NumberFormatException e) {
						variables.add(current);
						i.degree = 1;
						return i;
					}
				}
			} else if (current.op() instanceof ProgramVariable
					|| current.op() instanceof Metavariable
					|| current.op() instanceof LogicVariable) {
				Info i = new Info(0);
				variables.add(current);
				i.degree = 1;
				return i;
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
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		/*@Override*/
		public String toString() {
			return "(" + t + ", " + degree + ", " + depth + ")";
		}
	}

	/**
	 * Compute an ordered set of given formulas with respect to structural
	 * information
	 * 
	 * @param terms
	 * @return
	 */
	public static Queue<Term> getOrder(Set<Term> terms,
			final Set<Term> currentVariables) {
		Set<TermInformations> infos = new LinkedHashSet<TermInformations>();
		TreeMap<String, Integer> variables = new TreeMap<String, Integer>();
		// collect term information and recency information
		for (Term t : terms) {
			TermInformations termInformations = new TermInformations(t);
			infos.add(termInformations);
			// collect information for recency order
			for (Term var : termInformations.getVariables()) {
				String s = var.op().name().toString();
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
					if(!variables.containsKey(s)) {
						variables.put(s, 0);
					}
				}
			}
		}

		// lexicographical order of comparators in the following list
		final List<Comparator<TermInformations>> comparators = new ArrayList<Comparator<TermInformations>>();
		// recency order, i.e., how new the symbols are
		comparators.add(new RecencyOrder(variables));
		// degree order: smaller degrees first
		comparators.add(new Comparator<TermInformations>() {

			/*@Override*/
			public int compare(TermInformations o1, TermInformations o2) {
				return o1.degree - o2.degree;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Object#toString()
			 */
			/*@Override*/
			public String toString() {
				return "DegreeComparator";
			}
		});

		comparators.add(new Comparator<TermInformations>() {

			/*@Override*/
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

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Object#toString()
			 */
			/*@Override*/
			public String toString() {
				return "VariableCountComparator";
			}
		});

		// term depth order: shallow terms first
		comparators.add(new Comparator<TermInformations>() {

			/*@Override*/
			public int compare(TermInformations o1, TermInformations o2) {
				return o1.depth - o2.depth;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Object#toString()
			 */
			/*@Override*/
			public String toString() {
				return "DepthComparator";
			}

		});

		// lexicographical order of the above suborders plus alphabetical order
		// for breaking ties
		Comparator<TermInformations> mainComparator = new Comparator<TermInformations>() {

			/*@Override*/
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

		List<TermInformations> ordered = new ArrayList<TermInformations>(infos);

		// XXX
		List<TermInformations> test = new ArrayList<TermInformations>(infos);
		
		Collections.sort(test, comparators.get(0));
		System.out.println("Recency: " + test);//XXX
		test.clear();
		test.addAll(infos);
		Collections.sort(test, comparators.get(1));
		System.out.println("Degree: " + test);//XXX
		
		Collections.sort(test, comparators.get(2));
		System.out.println("Depth: " + test);//XXX
		
		// XXX
		Queue<Term> result = new LinkedList<Term>();
		// while we got terms we havent added to our result, we need to reorder
		// all informations left and take the first one

		while (!ordered.isEmpty()) {
			// quick re-sort
			Collections.sort(ordered, mainComparator);
			// get minimum
			TermInformations first = ordered.remove(0);

//			System.out.println(first);
//			int j = 0;
//			System.out.println("\n" + comparators.get(j).toString());// XXX
//			int compare = comparators.get(j).compare(first, ordered.get(0));
//			while (compare == 0 && j + 1 < comparators.size()) {
//				j++;
//				Comparator<TermInformations> comparator = comparators.get(j);
//				System.out.println("\n" + comparator.toString());// XXX
//				compare = comparators.get(j).compare(first, ordered.get(0));
//			}
//			if(compare == 0) {
//				System.out.println("String compare");
//			}
			
			// implicitly changes comparator, so re-sort before next use in loop
			currentVariables.addAll(first.getVariables());
			
			result.add(first.getT());
		}

		return result;
	}
}
