package de.uka.ilkd.key.dl.formulatools.collector.filter;

import de.uka.ilkd.key.dl.formulatools.collector.FoundItem;

public class FilterArity extends FilterDecorator {

	private int arity;
	
	public FilterArity(int arity, IFilter decoratedFilter) {
		super(decoratedFilter);
		this.arity = arity;
	}

	/*@Override*/
	public RemoveItem isValid(FoundItem val) {
		if( val.getTerm().arity() == this.arity ) {
			return getDecoratedFilterResult(val);
		}
		return RemoveItem.REMOVE;
	}

}
