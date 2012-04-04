/*******************************************************************************
 * Copyright (c) 2009 Timo Michelsen, Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Timo Michelsen   - initial API and implementation
 *     Jan-David Quesel - implementation 
 ******************************************************************************/
package de.uka.ilkd.key.dl.arithmetics.impl.qepcad;

/**
 * Represents the input for the qepcad-program
 * 
 * @author Timo Michelsen
 */
public class QepCadInput {
	
	private String description = "[]";
	private String variableList = "";
	private int freeVariableNum = 0;
	private String formula = "";
	
	/**
	 * Standardconstructor
	 */
	public QepCadInput() {		
	}
	
	/**
	 * Constructor. Applies the given Parameters.
	 * 
	 * @param desc Description of the formula
	 * @param varList List of variables in the formula
	 * @param freeVarNum Number of free variables
	 * @param formula Formula
	 */
	public QepCadInput( String desc, String varList, int freeVarNum, String formula ) {
		setDescription( desc );
		setVariableList( varList );
		setFreeVariableNum( freeVarNum );
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
	 * Gets the number of free variables
	 * 
	 * @return Number of free variables
	 */
	public int getFreeVariableNum() {
		return this.freeVariableNum;
	}
	
	/**
	 * Sets the number of variables. Negative
	 * numbers are not allowed.
	 * 
	 * @param val New number of variables
	 */
	public void setFreeVariableNum( int val ) {
		if( val < 0) 
			val = 0;
		
		this.freeVariableNum = val;
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
