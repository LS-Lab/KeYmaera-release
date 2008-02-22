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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.uka.ilkd.key.logic.IteratorOfConstrainedFormula;
import de.uka.ilkd.key.logic.Term;

/**
 * @author jdq
 * 
 */
public class VariableOrderCreator {

	public static interface TermOrder extends Comparator<Term> {
	}

	public static class VariableOrder implements TermOrder {
		List<String> vars;

		/**
		 * 
		 */
		public VariableOrder(List<String> vars) {
			this.vars = vars;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Term o1, Term o2) {
			int[] one = rate(VariableCollector.getVariableTerms(o1));
			int[] two = rate(VariableCollector.getVariableTerms(o2));
			Term newer = null;
			for (int i = 0; i < one.length; i++) {
				if (one[i] != -1) {
					if (two[i] != -1) {
						int r = one[i] - two[i];
						if (r > 0) {
							if (newer == null) {
								newer = o1;
							} else {
								if (newer == o2) {
									return 0;
								}
							}
						} else if (r < 0) {
							if (newer == null) {
								newer = o2;
							} else {
								if (newer == o1) {
									return 0;
								}
							}
						}
					} else {
						if (newer == null) {
							newer = o1;
						} else {
							if (newer == o2) {
								return 0;
							}
						}
					}
				} else {
					if (two[i] != -1) {
						if (newer == null) {
							newer = o2;
						} else {
							if (newer == o1) {
								return 0;
							}
						}
					}
				}
			}
			return 0;
		}

		/**
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
				int firstUscore = -1;
				for (int i = 0; i < s.length(); i++) {
					if (s.charAt(i) == '_') {
						underScores++;
						if (firstUscore == -1) {
							firstUscore = i;
						}
					}
				}
				if (firstUscore != -1) {
					s = s.substring(0, firstUscore);
				}
				// add smallest numbers
				int indexOf = vars.indexOf(s);
				if (vector[indexOf] == -1 || underScores < vector[indexOf]) {
					vector[indexOf] = underScores;
				}
			}
			return vector;
		}

	}

	public static VariableOrder getVariableOrder(
			IteratorOfConstrainedFormula iterator) {
		TreeSet<String> variables = new TreeSet<String>();

		while (iterator.hasNext()) {
			Set<String> variableTerms = VariableCollector.getVariables(iterator
					.next().formula());
			for (String s : variableTerms) {
				int underScores = 0;
				int firstUscore = -1;
				for (int i = 0; i < s.length(); i++) {
					if (s.charAt(i) == '_') {
						underScores++;
						if (firstUscore == -1) {
							firstUscore = i;
						}
					}
				}
				if (firstUscore != -1) {
					s = s.substring(0, firstUscore);
				}
				variables.add(s);
			}
		}
		return new VariableOrder(new ArrayList<String>(variables));
	}

}
