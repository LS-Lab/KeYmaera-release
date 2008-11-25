package de.uka.ilkd.key.dl.formulatools.collector.filter;

import java.util.HashSet;

import de.uka.ilkd.key.dl.formulatools.collector.FoundItem;

@SuppressWarnings("unchecked")
public class FilterOperatorTypes extends FilterDecorator {

	private HashSet<Class> classes = null;
	
	public FilterOperatorTypes( HashSet<Class> classes,  IFilter decoratedFilter ) {
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
