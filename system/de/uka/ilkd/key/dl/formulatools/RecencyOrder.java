/**
 * 
 */
package de.uka.ilkd.key.dl.formulatools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import de.uka.ilkd.key.dl.formulatools.LexicographicalOrder.TermInformations;
import de.uka.ilkd.key.logic.Term;

public class RecencyOrder implements Comparator<TermInformations> {
	private List<String> vars;
	private TreeMap<String, Integer> map;

	/**
	 * 
	 */
	public RecencyOrder(TreeMap<String, Integer> vars) {
		this.vars = new ArrayList<String>(vars.keySet());
		System.out.println(vars);
		this.map = vars;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	/*@Override*/
	public int compare(TermInformations o1, TermInformations o2) {

		Set<Term> terms1 = o1.getVariables();
		Set<Term> terms2 = o2.getVariables();

		int[] one = rate(terms1);
		int[] two = rate(terms2);
		TermInformations newer = null;
		for (int i = 0; i < one.length; i++) {
			assert (one[i] >= -1 && two[i] >= -1);
			if (one[i] != -1) {
				if (two[i] != -1) {
					int r = one[i] - two[i];
					if (r > 0) {
						if (newer == null) {
							newer = o1;
						} else {
							if (newer == o2) {
								return minComp(one, two);
							}
						}
					} else if (r < 0) {
						if (newer == null) {
							newer = o2;
						} else {
							if (newer == o1) {
								return minComp(one, two);
							}
						}
					}
				} else {
					if (newer == null) {
						newer = o1;
					} else {
						if (newer == o2) {
							return minComp(one, two);
						}
					}
				}
			} else {
				if (two[i] != -1) {
					if (newer == null) {
						newer = o2;
					} else {
						if (newer == o1) {
							return minComp(one, two);
						}
					}
				}
			}
		}
		return (newer == o1) ? 1 : -1;
	}

	/**
	 * In conflicting cases, the term with a variable closest to the current
	 * generation is considered greater.
	 * 
	 * @param one
	 * @param two
	 * @return
	 */
	private int minComp(int[] one, int[] two) {
		int minOne = 0;
		int minTwo = 0;
		for (int i = 0; i < one.length; i++) {
			Integer integer = map.get(vars.get(i));
			int cur1 = integer - one[i];
			int cur2 = integer - two[i];
			if (cur1 < minOne) {
				minOne = cur1;
			}
			if (cur2 < minTwo) {
				minTwo = cur2;
			}
		}
		return (minOne > minTwo) ? 1 : (minTwo > minOne) ? -1 : 0;
	}

	/**
	 * Construct a vector that contains the oldest occurrences of all
	 * variables
	 * 
	 * @param variableTerms
	 * @return
	 */
	private int[] rate(Set<Term> variableTerms) {
		int[] vector = new int[vars.size()];
		for (int i = 0; i < vector.length; i++) {
			vector[i] = -1;
		}
		for (Term var : variableTerms) {
			String s = var.op().name().toString();
			int underScores = 0;
			if (s.contains("_")) {
				underScores = Integer.parseInt(s
						.substring(s.lastIndexOf('_') + 1));
				underScores++;
				s = s.substring(0, s.lastIndexOf('_'));
			}
			// add smallest (oldest) numbers
			int indexOf = vars.indexOf(s);
			if (vector[indexOf] == -1 || underScores < vector[indexOf]) {
				vector[indexOf] = underScores;
			}
		}
		return vector;
	}

}