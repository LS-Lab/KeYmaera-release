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

@SuppressWarnings("unchecked")
public class FilterNotOperatorType extends FilterDecorator {

	private Class classID;
	
	public FilterNotOperatorType(Class classID, IFilter decoratedFilter ) {
		super( decoratedFilter );
		this.classID = classID;
	}
	/*@Override*/
	public RemoveItem isValid(FoundItem val) {
		
		if( classID.isInstance(val.getTerm().op()) )
			return RemoveItem.REMOVE;

		return getDecoratedFilterResult(val);
	}
	
}
