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
/**
 * File created 28.02.2007
 */
package de.uka.ilkd.key.dl.model.impl;

import java.io.IOException;
import java.util.List;

import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.dl.model.Variable;
import de.uka.ilkd.key.dl.model.VariableDeclaration;
import de.uka.ilkd.key.dl.model.VariableType;
import de.uka.ilkd.key.java.NameAbstractionTable;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.ReuseableProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.SourceElement;
import de.uka.ilkd.key.java.reference.ExecutionContext;

/**
 * This class is the implementation of the representation of variable
 * declarations in dL. {@link VariableDeclaration}
 * 
 * @author jdq
 * @since 28.02.2007
 * 
 */
public class VariableDeclarationImpl extends DLNonTerminalProgramElementImpl
        implements VariableDeclaration {

    /**
     * @param variable
     */
    public VariableDeclarationImpl(VariableType type, List<? extends DLProgramElement> variables) {
        addChild(type);
        for (DLProgramElement variable : variables) {
            addChild(variable);
        }
    }

    /**
     * @param variableType
     * @param sv
     */
    public VariableDeclarationImpl(VariableType variableType, ProgramVariable sv) {
        addChild(variableType);
        addChild(sv);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.model.impl.DLNonTerminalProgramElementImpl#prettyPrint(de.uka.ilkd.key.java.PrettyPrinter)
     */
    /*@Override*/
    public void prettyPrint(PrettyPrinter arg0) throws IOException {
        arg0.printDLVariableDeclaration(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.VariableDeclaration#getType()
     */
    public VariableType getType() {
        return (VariableType) getChildAt(0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.model.impl.DLNonTerminalProgramElementImpl#equalsModRenaming(de.uka.ilkd.key.java.SourceElement,
     *      de.uka.ilkd.key.java.NameAbstractionTable)
     */
    /*@Override*/
    public boolean equalsModRenaming(SourceElement se, NameAbstractionTable nat) {
        if (se instanceof VariableDeclaration) {
            VariableDeclaration decl = (VariableDeclaration) se;
            if(getChildCount() != decl.getChildCount()) {
                return false;
            }
            for (int i = 0; i < getChildCount(); i++) {
                if (!decl.getChildAt(i).equalsModRenaming(getChildAt(i), nat)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.ReuseableProgramElement#reuseSignature(de.uka.ilkd.key.java.Services,
     *      de.uka.ilkd.key.java.reference.ExecutionContext)
     */
    public String reuseSignature(Services services, ExecutionContext ec) {
        /*StringBuilder builder = new StringBuilder();
        String space ="";
        for(ProgramElement pe: this) {
            if(pe instanceof ReuseableProgramElement) {
                builder.append(space + ((ReuseableProgramElement)pe).reuseSignature(services, ec));
                space = " ";
            }
        }
        return builder.toString();*/
        // all variable declarations are essentially equivalent and the program effect doesn't really depend on it
        return "VariableDeclaration";
    }

}
