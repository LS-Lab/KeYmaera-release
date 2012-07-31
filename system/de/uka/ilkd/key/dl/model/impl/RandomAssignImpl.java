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

import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.FunctionTerm;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.dl.model.RandomAssign;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.reference.ExecutionContext;

/**
 * This class is the implementation of the representation of random assignments
 * in dL. {@link RandomAssign}
 * 
 * @author jdq
 * @since 28.02.2007
 * 
 */
public class RandomAssignImpl extends DLNonTerminalProgramElementImpl implements
        RandomAssign {

    /**
     * @param variable
     */
    public RandomAssignImpl(ProgramVariable variable) {
        addChild(variable);
    }
    
    public RandomAssignImpl(FunctionTerm variable) {
        addChild(variable);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.model.impl.DLNonTerminalProgramElementImpl#prettyPrint(de.uka.ilkd.key.java.PrettyPrinter)
     */
    /*@Override*/
    public void prettyPrint(PrettyPrinter arg0) throws IOException {
        arg0.printRandomAssign(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.ReuseableProgramElement#reuseSignature(de.uka.ilkd.key.java.Services,
     *      de.uka.ilkd.key.java.reference.ExecutionContext)
     */
    public String reuseSignature(Services services, ExecutionContext ec) {
        return "("
                + ((DLProgramElement) getChildAt(0)).reuseSignature(services,
                        ec) + ") := *";
    }

}
