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
 * Implements the Decorator-Pattern for filters.
 * Every Filter must an subclass of this class.
 * 
 * @author Timo Michelsen
 *
 */
public abstract class FilterDecorator implements IFilter {
	
	/**
	 * Decorated Filter
	 */
	private IFilter decoratedFilter;
	
	/**
	 * Creates an instance of the FilterDecorator-Class with the
	 * given Filter to decorate. Null is allowed.
	 * 
	 * @param decoratedFilter Filter to decorate. Null is allowed.
	 */
	public FilterDecorator( IFilter decoratedFilter ) {
		this.decoratedFilter = decoratedFilter;
	}

	/**
	 * Returns the decorated filter.
	 * 
	 * @return Decorated filter.
	 */
	public IFilter getDecoratedFilter() {
		return this.decoratedFilter;
	}
	
	/**
	 * Returns the result of the decorated Filter, if exists.
	 * If no decorated filter is specified, NOT_REMOVE will be returned.
	 * 
	 * @param f Item to check with the decorated Filter
	 * @return see summary
	 */
	public RemoveItem getDecoratedFilterResult( FoundItem f ) {
		if( this.decoratedFilter == null )
			return RemoveItem.NOT_REMOVE;
		else
			return this.decoratedFilter.isValid(f);
	}
}
