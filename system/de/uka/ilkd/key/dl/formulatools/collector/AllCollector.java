/*******************************************************************************
 * Copyright (c) 2009 Timo Michelsen.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Timo Michelsen - initial API and implementation
 ******************************************************************************/
package de.uka.ilkd.key.dl.formulatools.collector;

import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;

/**
 * Visitor-Extension, which collects all variables, functions, etc.
 * 
 * For instance, collect all variables:
 * Set<Term> terms1 = AllCollector.getItemSet(o1).filter( new FilterVariableCollector()).getVariableTerms();
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
