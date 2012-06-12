/***************************************************************************
 *   Copyright (C) 2012 by Jan-David Quesel                                *
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
 * AndImpl.java 1.00 Mo Jan 15 09:24:48 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import java.io.IOException;

import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.Diamond;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.java.PrettyPrinter;

/**
 * The implementation of the box modality
 * 
 * @version 1.00
 * @author jdq
 */
public class DiamondImpl extends CompoundFormulaImpl implements Diamond {
    /**
     * Creates the conjunction of the two formulas
     */
    public DiamondImpl(DLProgram program, Formula post) {
        addChild(program);
        addChild(post);
    }

    /**
     * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#getSymbol()
     *      getSymbol
     */
    public String getSymbol() {
        return "<>";
    }

    /**
     * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#printInfix()
     *      printInfix
     */
    public boolean printInfix() {
        return false;
    }
    
    /* (non-Javadoc)
     * @see de.uka.ilkd.key.dl.model.impl.DLNonTerminalProgramElementImpl#prettyPrint(de.uka.ilkd.key.java.PrettyPrinter)
     */
    @Override
    public void prettyPrint(PrettyPrinter arg0) throws IOException {
        arg0.printDiamond(this);
    }
}
