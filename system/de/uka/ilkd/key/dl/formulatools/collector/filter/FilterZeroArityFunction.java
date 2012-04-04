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
import de.uka.ilkd.key.logic.op.Function;

public class FilterZeroArityFunction extends FilterDecorator {

	public FilterZeroArityFunction(IFilter decoratedFilter) {
		super(decoratedFilter);
	}

	/*@Override*/
	public RemoveItem isValid(FoundItem val) {
		if( val.getTerm().op() instanceof Function && val.getTerm().op().arity() == 0 )
			return getDecoratedFilterResult(val);
		return RemoveItem.REMOVE;
	}

}
