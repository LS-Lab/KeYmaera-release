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

/**
 * Represents a found item in the AllCollector-Class.
 * 
 * @author Timo Michelsen
 *
 */
public class FoundItem {
	
	private String name;
	private Term term;
	
	/**
	 * Creates a new instance of the FoundItem-Class with the
	 * given name and term. 
	 * 
	 * @param name Name of the item
	 * @param term Term of the item
	 */
	public FoundItem(String name, Term term ) {
		this.name = name;
		this.term = term;
	}
	
	/**
	 * Returns the name of this item.
	 * 
	 * @return Name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Return the associated term of this item
	 * 
	 * @return Term
	 */
	public Term getTerm() {
		return this.term;
	}
}
