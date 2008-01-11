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
 * StarImpl.java 1.00 Mo Jan 15 09:07:06 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.Star;
import de.uka.ilkd.key.java.PrettyPrinter;
import java.io.IOException;

/**
 * Implementation of {@link Star}.
 * 
 * @version 1.00
 * @author jdq
 */
public class StarImpl extends CompoundDLProgramImpl implements Star {

    /**
     * Creates a new repetition operator
     * 
     * @param p
     *                the formula to repeat
     */
    public StarImpl(DLProgram p) {
        addChild(p);
    }

    /**
     * @see de.uka.ilkd.key.dl.model.impl.CompoundDLProgramImpl#prettyPrint(de.uka.ilkd.key.java.PrettyPrinter)
     *      prettyPrint
     */
    public void prettyPrint(PrettyPrinter arg0) throws IOException {
        arg0.printStar(this);
    }

    /**
     * @return the invariant
     */
    public Formula getInvariant() {
        return getDLAnnotation("invariant").get(0);
    }

}
