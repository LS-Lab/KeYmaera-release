package de.uka.ilkd.key.dl.formulatools.collector.filter;

import de.uka.ilkd.key.dl.formulatools.collector.FoundItem;
import de.uka.ilkd.key.logic.op.Modality;

public class FilterModality extends FilterDecorator {

	public FilterModality(IFilter decoratedFilter) {
		super(decoratedFilter);
	}

	/*@Override*/
	public RemoveItem isValid(FoundItem val) {
		if( val.getTerm().op() instanceof Modality ) {
			return getDecoratedFilterResult(val);
		}
		return RemoveItem.REMOVE;
	}

}
