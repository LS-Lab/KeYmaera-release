/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel                                *
 *   quesel@informatik.uni-oldenburg.de                                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
/*
 * AssignImpl.java 1.00 Mo Jan 15 09:44:05 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.Assign;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.Expression;
import de.uka.ilkd.key.dl.model.FunctionTerm;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.java.NameAbstractionTable;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.SourceElement;
import de.uka.ilkd.key.java.declaration.VariableSpecification;
import de.uka.ilkd.key.java.reference.ExecutionContext;
import de.uka.ilkd.key.logic.Named;

/**
 * The implementation of an assignment ({@link Assign})
 * 
 * @version 1.00
 * @author jdq
 */
public class AssignImpl extends DLNonTerminalProgramElementImpl implements
        Assign {

    /**
     * Creates a new Assign statement with a given variable and a given value.
     * 
     * @param var
     *                the variable to assign
     * @param value
     *                the value to assign to the variable
     */
    public AssignImpl(ProgramVariable var, Expression value) {
        addChild(var);
        addChild(value);
    }
    
    public AssignImpl(FunctionTerm var, Expression value) {
        addChild(var);
        addChild(value);
    }
 
    public AssignImpl(ProgramElement var, ProgramElement value) {
        addChild(var);
        addChild(value);
    }

    /**
     * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#getSymbol()
     *      getSymbol
     */
    public String getSymbol() {
        return ":=";
    }

    /**
     * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#printInfix()
     *      printInfix
     */
    public boolean printInfix() {
        return true;
    }

    //@TODO check implementation!
    /*public boolean equalsModRenaming(SourceElement se, 
    		NameAbstractionTable nat) {
    	if (!(se instanceof Assign)) {
    		return false;
    	}		 
    	final Assign b = (Assign)se;
    	System.out.println("checking");
    	if (getChildAt(0) instanceof ProgramVariable && b.getChildAt(0) instanceof ProgramVariable) {
        	//@todo check that same types and return false if different
    	} else {
    		//@TODO check for FunctionTerm cases
    		return false;
    	}
    	System.out.println("checking still");
    	nat.add(getChildAt(0), b.getChildAt(0));	
    	if (b.getChildCount()!=getChildCount()) {
    		return false;
    	}	
    	for (int i = 0, cc = getChildCount(); i<cc; i++) {
    		if (!getChildAt(i).equalsModRenaming
    				(b.getChildAt(i), nat)) {
    			return false;
    		}
    	}
    	System.out.println("checking ok");
    	return true;	
    }*/

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.ReuseableProgramElement#reuseSignature(de.uka.ilkd.key.java.Services,
     *      de.uka.ilkd.key.java.reference.ExecutionContext)
     */
    public String reuseSignature(Services services, ExecutionContext ec) {
        // TODO Auto-generated method stub
        StringBuilder result = new StringBuilder();
        result.append(getSymbol() + "(");
        for (int i = 0; i < getChildCount(); i++) {
            result.append(((DLProgramElement) getChildAt(i))
                    .reuseSignature(services, ec)
                    + ", ");
        }
        result.append(")");
        return result.toString();
    }
}
