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
 * DotImpl.java 1.00 Mo Jan 15 09:52:04 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.FunctionTerm;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.reference.ExecutionContext;

import java.io.IOException;

/**
 * Implementation of {@link Dot}
 * 
 * @version 1.00
 * @author jdq
 */
public class DotImpl extends DLNonTerminalProgramElementImpl implements Dot {

    private int order;

    /**
     * Sets order to 0. As Dot represents at least one '.
     * 
     * @param var
     *                the dotted variable
     */
    public DotImpl(ProgramVariable var) {
        this(1, var);
    }
    
    public DotImpl(int order, ProgramVariable var) {
        this.order = order;
        addChild(var);
    }
    
    public DotImpl(int order, FunctionTerm var) {
        this.order = order;
        addChild(var);
    }

    /**
     * @param convert
     * @param order2
     */
    public DotImpl(DLProgramElement convert, int order) {
        this.order = order;
        addChild(convert);
    }

    /**
     * Increments the dotcount
     */
    public void increment() {
        order++;
    }

    /**
     * @see de.uka.ilkd.key.dl.model.Dot#getOrder() getOrder
     */
    public int getOrder() {
        return order;
    }

    /**
     * @see de.uka.ilkd.key.dl.model.impl.DLNonTerminalProgramElementImpl#prettyPrint(de.uka.ilkd.key.java.PrettyPrinter)
     *      prettyPrint
     */
    public void prettyPrint(PrettyPrinter arg0) throws IOException {
        arg0.printDot(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.ReuseableProgramElement#reuseSignature(de.uka.ilkd.key.java.Services,
     *      de.uka.ilkd.key.java.reference.ExecutionContext)
     */
    public String reuseSignature(Services services, ExecutionContext ec) {
        return ((DLProgramElement) getChildAt(0)).reuseSignature(services, ec)
                + getClass().getName() + "[" + order + "]";
    }
}
