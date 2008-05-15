package de.uka.ilkd.key.dl.formulatools.collector.filter;

import de.uka.ilkd.key.dl.formulatools.collector.FoundItem;

public class FilterNotInArityRange extends FilterDecorator {

	private int minArity;
	private int maxArity;
	
	public FilterNotInArityRange(  int minArity, int maxArity, IFilter decoratedFilter ) {
		super(decoratedFilter);
		this.minArity = minArity;
		this.maxArity = maxArity;
	}
	
	@Override
	public RemoveItem isValid(FoundItem val) {
		if( val.getTerm().op().arity() >= this.minArity && val.getTerm().op().arity() <= this.maxArity )
			return RemoveItem.REMOVE;
		return getDecoratedFilterResult(val);
	}

}
