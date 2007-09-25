/**
 * File created 05.04.2007
 */
package de.uka.ilkd.key.logic.sort;

import de.uka.ilkd.key.logic.Term;

/**
 * A sort that is a placeholder for another sort. Used to avoid nasty meta
 * operators for rules that drag formulas from the program to the logic level.
 * 
 * @author jdq
 * @since 05.04.2007
 * 
 */
public interface PlaceHolderSort {

	/**
	 * Returns the sort this sort is a placeholder for
	 * 
	 * @return the sort this sort is a placeholder for
	 */
	public Sort getRealSort();

	/**
	 * Dertermines the real sort if the variable would be created with toplevel
	 * operator term
	 * 
	 * @param term
	 *            the toplevel operator
	 * @return the real sort if the variable would be created with toplevel
	 *         operator term
	 */
	public Sort getRealSort(Term[] term);
}
