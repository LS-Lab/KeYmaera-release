package de.uka.ilkd.key.dl.formulatools.collector.filter;

import de.uka.ilkd.key.dl.formulatools.collector.FoundItem;

public class FilterNotNumber extends FilterDecorator {
	
	public FilterNotNumber( IFilter decoratedFilter ) {
		super(decoratedFilter);
	}

	@Override
	public RemoveItem isValid(FoundItem val) {
		try {
			Double.parseDouble(val.getTerm().op().name().toString());
			return RemoveItem.REMOVE;
		}
		catch( Exception ex) {
			return getDecoratedFilterResult(val);
		}
	}

}
