package de.uka.ilkd.key.dl.formulatools.collector.filter;

import de.uka.ilkd.key.dl.formulatools.collector.FoundItem;
import de.uka.ilkd.key.logic.op.Function;

public class FilterZeroArityFunction extends FilterDecorator {

	public FilterZeroArityFunction(IFilter decoratedFilter) {
		super(decoratedFilter);
	}

	@Override
	public RemoveItem isValid(FoundItem val) {
		if( val.getTerm().op() instanceof Function && val.getTerm().op().arity() == 0 )
			return getDecoratedFilterResult(val);
		return RemoveItem.REMOVE;
	}

}
