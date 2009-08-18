/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel, Andre Platzer                 *
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
package de.uka.ilkd.key.dl.formulatools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import de.uka.ilkd.key.dl.formulatools.collector.AllCollector;
import de.uka.ilkd.key.dl.formulatools.collector.filter.FilterVariableCollector;
import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.Term;

/**
 * @author jdq
 * 
 */
public class VariableOrderCreator {

	public static interface TermOrder extends Comparator<Term> {
	}

	public static class VariableOrder implements TermOrder {
		private List<String> vars;
		private TreeMap<String, Integer> map;

		/**
		 * 
		 */
		public VariableOrder(TreeMap<String, Integer> vars) {
			this.vars = new ArrayList<String>(vars.keySet());
			this.map = vars;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		/*@Override*/
		public int compare(Term o1, Term o2) {

			Set<Term> terms1 = AllCollector.getItemSet(o1).filter(
					new FilterVariableCollector()).getVariableTerms();
			Set<Term> terms2 = AllCollector.getItemSet(o1).filter(
					new FilterVariableCollector()).getVariableTerms();

			int[] one = rate(terms1);
			int[] two = rate(terms2);
			Term newer = null;
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
							.substring(s.indexOf('_') + 1));
					underScores++;
					s = s.substring(0, s.indexOf('_'));
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

	public static VariableOrder getVariableOrder(
			Iterator<ConstrainedFormula> iterator) {
		TreeMap<String, Integer> variables = new TreeMap<String, Integer>();

		while (iterator.hasNext()) {
			Set<String> variableTerms = AllCollector.getItemSet(
					iterator.next().formula()).filter(
					new FilterVariableCollector(null)).getVariables();
			for (String s : variableTerms) {
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
		return new VariableOrder(variables);
	}

}
