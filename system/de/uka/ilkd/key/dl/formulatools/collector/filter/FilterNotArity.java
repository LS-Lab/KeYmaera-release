package de.uka.ilkd.key.dl.formulatools.collector.filter;

import de.uka.ilkd.key.dl.formulatools.collector.FoundItem;

public class FilterNotArity extends FilterDecorator {

	private int arity;
	
	public FilterNotArity( int arity, IFilter decoratedFilter ) {
		super(decoratedFilter);
		this.arity = arity;
	}
	
	/*@Override*/
	public RemoveItem isValid(FoundItem val) {
		if( val.getTerm().op().arity() == this.arity)
			return RemoveItem.REMOVE;
		return getDecoratedFilterResult(val);
	}
	
}
