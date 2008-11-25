package de.uka.ilkd.key.dl.formulatools.collector.filter;

import de.uka.ilkd.key.dl.formulatools.collector.FoundItem;

public class FilterOperatorNames extends FilterDecorator {

	private String[] operatorNames;
	
	public FilterOperatorNames(  String[] operators, IFilter decoratedFilter ) {
		super(decoratedFilter);
		this.operatorNames = operators;
	}
	
	/*@Override*/
	public RemoveItem isValid(FoundItem val) {
		for( String op: this.operatorNames ) {
			if( op.equals(val.getName()))
				return getDecoratedFilterResult(val);
		}
		return RemoveItem.REMOVE;
	}

}
