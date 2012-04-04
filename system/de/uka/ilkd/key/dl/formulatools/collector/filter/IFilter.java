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
