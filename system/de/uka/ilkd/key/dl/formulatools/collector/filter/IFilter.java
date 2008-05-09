package de.uka.ilkd.key.dl.formulatools.collector.filter;

import de.uka.ilkd.key.dl.formulatools.collector.FoundItem;

/**
 * Interface for filters to implement.
 * Part of the Decorator-Pattern.
 * 
 * @author Timo Michelsen
 *
 */
public interface IFilter {
	
	/**
	 * Checks if a FoundItem-Object has a specific property 
	 * 
	 * @param val Item to check
	 * @return Item has passed the check, or not
	 */
	RemoveItem isValid( FoundItem val );
}
