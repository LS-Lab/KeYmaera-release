package de.uka.ilkd.key.dl.formulatools.collector;

import java.util.HashSet;
import java.util.Set;

import de.uka.ilkd.key.dl.formulatools.collector.filter.*;
import de.uka.ilkd.key.logic.Term;

/**
 * Has a set of variables and variableterms with the opportunity to filter.
 * 
 * @author Timo Michelsen
 * 
 */
public class FilterVariableSet extends HashSet<FoundItem> {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance of the FilterVariableSet-Class.
	 */
	public FilterVariableSet() {
		super();
	}

	/**
	 * Gets the variableterms of this set.
	 * 
	 * @return Set of Variableterms
	 */
	public Set<Term> getVariableTerms() {
		HashSet<Term> terms = new HashSet<Term>();

		for (FoundItem f : this) {
			terms.add(f.getTerm());
		}

		return terms;
	}

	/**
	 * Gets the variablenames of this set.
	 * 
	 * @return Set of variablenames
	 */
	public Set<String> getVariables() {
		HashSet<String> variables = new HashSet<String>();

		for (FoundItem f : this) {
			variables.add(f.getName());
		}

		return variables;
	}

	/**
	 * Filters the set of variables with the given filter.
	 * 
	 * @param filter
	 * @return Set of filtered variables
	 */
	public FilterVariableSet filter(IFilter filter) {
		
		FilterVariableSet newSet = new FilterVariableSet();
		
		// Mark removes
		for (FoundItem f : this) {
			if (filter.isValid(f) == RemoveItem.NOT_REMOVE) {
				newSet.add(f);
			}
		}
		
		return newSet;
	}
	
	/**
	 * Finds the first Term, which passes the filter
	 * 
	 * @param filter
	 * @return
	 */
	public FoundItem filterFirst( IFilter filter ) {
		
		for( FoundItem f: this) {
			if( filter.isValid(f) == RemoveItem.NOT_REMOVE )
				return f;
		}
		return null;
	}

	/**
	 * Filters the set of variables with the given filter
	 * in an inverse way.
	 * 
	 * @param filter
	 * @return Set of filtered variables (inversed)
	 */
	public FilterVariableSet filterInverse(IFilter filter) {
		
		FilterVariableSet newSet = new FilterVariableSet();
		
		// Mark removes
		for (FoundItem f : this) {
			if (filter.isValid(f) == RemoveItem.REMOVE) {
				newSet.add(f);
			}
		}
		
		return newSet;	
	}
}
