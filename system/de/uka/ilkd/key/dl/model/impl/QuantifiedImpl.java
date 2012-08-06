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

import java.io.IOException;

import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.dl.model.Quantified;
import de.uka.ilkd.key.dl.model.Variable;
import de.uka.ilkd.key.dl.model.VariableDeclaration;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.ReuseableProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.reference.ExecutionContext;

/**
 * The implementation of a quantifier in a program ({@link Quantified})
 * 
 * @version 1.00
 * @author jdq
 */
public class QuantifiedImpl extends DLNonTerminalProgramElementImpl implements Quantified {

    /**
     * Creates a new quantified statement with a given variable and a given statement.
     * 
     * @param var
     *                the variable to assign
     * @param value
     *                the value to assign to the variable
     */
    public QuantifiedImpl(VariableDeclaration decl, DLProgram statement) {
        addChild(decl);
        addChild(statement);
    }
    
    public QuantifiedImpl(ProgramVariable decl, DLProgram statement) {
        addChild(decl);
        addChild(statement);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.ReuseableProgramElement#reuseSignature(de.uka.ilkd.key.java.Services,
     *      de.uka.ilkd.key.java.reference.ExecutionContext)
     */
    public String reuseSignature(Services services, ExecutionContext ec) {
        // TODO Auto-generated method stub
        StringBuilder result = new StringBuilder();
        result.append("forall " + ((ReuseableProgramElement) getChildAt(0)).reuseSignature(services, ec) + " (");
        for (int i = 1; i < getChildCount(); i++) {
            result.append(((DLProgramElement) getChildAt(i))
                    .reuseSignature(services, ec)
                    + ", ");
        }
        result.append(")");
        return result.toString();
    }
    
    /* (non-Javadoc)
     * @see de.uka.ilkd.key.dl.model.impl.DLNonTerminalProgramElementImpl#prettyPrint(de.uka.ilkd.key.java.PrettyPrinter)
     */
    @Override
    public void prettyPrint(PrettyPrinter arg0) throws IOException {
        arg0.printQuantified(this);
    }
    
}
