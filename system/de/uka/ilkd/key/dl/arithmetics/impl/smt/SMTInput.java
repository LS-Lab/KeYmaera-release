/*******************************************************************************
 * Copyright (c) 2012 Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jan-David Quesel - initial API and implementation
 ******************************************************************************/
package de.uka.ilkd.key.dl.arithmetics.impl.smt;

/**
 * Represents the input for SMT LIB
 * 
 */
public class SMTInput {
	
	private String description = "[]";
	private String variableList = "";
	private int freeVariableNum = 0;
	private String formula = "";
	
	/**
	 * Standardconstructor
	 */
	public SMTInput() {		
	}
	
	/**
	 * Constructor. Applies the given Parameters.
	 * 
	 * @param desc Description of the formula
	 * @param varList List of variables in the formula
	 * @param freeVarNum Number of free variables
	 * @param formula Formula
	 */
	public SMTInput( String desc, String varList, String formula ) {
		setDescription( desc );
		setVariableList( varList );
		setFormula( formula );
	}
	
	/**
	 * Retrieves the description
	 * @return Description
	 */
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * Sets the description
	 * @param desc New description
	 */
	public void setDescription( String desc ) {
		this.description = desc;
	}
	
	/**
	 * Retrieves the variablelist as string
	 * 
	 * @return Variablelist
	 */
	public String getVariableList() {
		return this.variableList;
	}
	
	/**
	 * Sets the list of Variables. Format like "(x,y)"
	 * 
	 * @param varList New list of variables
	 */
	public void setVariableList( String varList ) {
		this.variableList = varList;
	}

	
	/**
	 * Gets the formula
	 * 
	 * @return Formula
	 */
	public String getFormula() {
		return this.formula;
	}
	
	/**
	 * Sets the formula
	 * 
	 * @param formula New formula
	 */
	public void setFormula( String formula ) {
		this.formula = formula;
	}
}
