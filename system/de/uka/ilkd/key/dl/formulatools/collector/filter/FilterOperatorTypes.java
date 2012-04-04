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

import java.util.LinkedHashSet;

import de.uka.ilkd.key.dl.formulatools.collector.FoundItem;

@SuppressWarnings("unchecked")
public class FilterOperatorTypes extends FilterDecorator {

	private LinkedHashSet<Class> classes = null;
	
	public FilterOperatorTypes( LinkedHashSet<Class> classes,  IFilter decoratedFilter ) {
		super(decoratedFilter);
		this.classes = classes;
	}

	/*@Override*/
	public RemoveItem isValid(FoundItem val) {
		for( Class c : this.classes ) {
			if( c.isInstance(val.getTerm().op())) {
				return getDecoratedFilterResult(val);
			}
		}
		return RemoveItem.REMOVE;	
	}	


}
