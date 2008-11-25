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
