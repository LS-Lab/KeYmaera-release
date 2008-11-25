package de.uka.ilkd.key.dl.formulatools.collector;

import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;

/**
 * Visitor-Extension, which collects all variables, functions, etc.
 * 
 * @author Timo Michelsen
 *
 */
public class AllCollector extends Visitor {

	/**
	 * List of all found items
	 */
	private FilterVariableSet foundVariables = new FilterVariableSet();
	
	/*@Override*/
	public void visit(Term visited) {
		this.foundVariables.add( new FoundItem(visited.op().name().toString(), visited) );
	}
	
	/**
	 * Returns the list of found items after collecting through
	 * a term.
	 * 
	 * @return List of found items.
	 */
	public FilterVariableSet getItemSet() {
		return this.foundVariables;
	}
	
	/**
	 * Static Method to return a list of found items after collecting
	 * throgh a given term.
	 * 
	 * @param term Term
	 * @return List of found items.
	 */
	public static FilterVariableSet getItemSet( Term term ) {
		AllCollector collector = new AllCollector();
		term.execPreOrder(collector);
		return collector.getItemSet();
	}
}
