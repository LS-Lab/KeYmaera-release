/*******************************************************************************
 * Copyright (c) 2009 Timo Michelsen.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Timo Michelsen - initial API and implementation
 ******************************************************************************/
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
