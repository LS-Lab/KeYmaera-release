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
 * File created 20.02.2007
 */
package de.uka.ilkd.key.dl.formulatools;

import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.RigidFunction;

/**
 * This class is a visitor implementation that checks if a given term is a first
 * order term and contains skolem symbols.
 * 
 * @author jdq
 * @since 20.02.2007
 * 
 */
public class ContainsSkolemSymbolVisitor extends Visitor {

    private static final ContainsSkolemSymbolVisitor INSTANCE = new ContainsSkolemSymbolVisitor();

    private boolean foundSkolem = false;

    private boolean fo = true;

    private ContainsSkolemSymbolVisitor() {
    }

    public synchronized static boolean containsSkolemSymbolAndIsFO(Term form) {
        INSTANCE.fo = true;
        INSTANCE.foundSkolem = false;
        form.execPreOrder(INSTANCE);
        return INSTANCE.fo && INSTANCE.foundSkolem;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
     */
    /*@Override*/
    public void visit(Term visited) {
        if (fo) {
            if (visited.op() instanceof Modality) {
                fo = false;
            } else if ((visited.op() instanceof RigidFunction)
                    && ((RigidFunction) visited.op()).isSkolem()) {
                foundSkolem = true;
            }
        }
    }
}
