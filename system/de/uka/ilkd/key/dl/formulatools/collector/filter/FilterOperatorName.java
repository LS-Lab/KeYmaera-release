package de.uka.ilkd.key.dl.formulatools.collector.filter;

import de.uka.ilkd.key.dl.formulatools.collector.FoundItem;

public class FilterOperatorName extends FilterDecorator {

	private String operatorName;
	
	public FilterOperatorName( String opName, IFilter decoratedFilter ) {
		super(decoratedFilter);
		this.operatorName = opName;
	}
	
	/*@Override*/
	public RemoveItem isValid(FoundItem val) {
		if( val.getName().equals(this.operatorName)) {
			return getDecoratedFilterResult(val);
		}
		return RemoveItem.REMOVE;
	}

}
