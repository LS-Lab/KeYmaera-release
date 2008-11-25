package de.uka.ilkd.key.dl.formulatools.collector.filter;

import de.uka.ilkd.key.dl.formulatools.collector.FoundItem;

public class FilterNotOperatorName extends FilterDecorator {

	private String operatorName;
	
	public FilterNotOperatorName( String operatorName, IFilter decoratedFilter ) {
		super(decoratedFilter);
		this.operatorName = operatorName;
	}
	
	/*@Override*/
	public RemoveItem isValid(FoundItem val) {
		if( val.getName().equals(this.operatorName)) 
			return RemoveItem.REMOVE;
		return getDecoratedFilterResult(val);
	}

}
