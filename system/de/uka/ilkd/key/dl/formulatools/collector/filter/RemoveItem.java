package de.uka.ilkd.key.dl.formulatools.collector.filter;

/**
 * Enumeration for Filters to indicate, 
 * if an item must be removed or not.
 * 
 * @author Timo Michelsen
 *
 */
public enum RemoveItem {
	
	/**
	 * Indicates, that an item will or must be removed
	 */
	REMOVE,
	
	/**
	 * Indicates, that an item wont be removed
	 */
	NOT_REMOVE;
	
	/**
	 * Converts to Boolean
	 * 
	 * @return true, if NOT_REMOVE is set, false otherwise
	 */
	public boolean toBoolean() {
		return (this != REMOVE);
	}
}
