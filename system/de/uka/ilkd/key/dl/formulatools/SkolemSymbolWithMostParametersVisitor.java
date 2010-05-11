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

import java.util.LinkedHashSet;

import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.RigidFunction;

/**
 * Visitor that returns the skolem symbol that has the highest parameter count.
 * 
 * @author jdq
 * @since 20.02.2007
 * 
 */
public class SkolemSymbolWithMostParametersVisitor extends Visitor {

    private static final SkolemSymbolWithMostParametersVisitor INSTANCE = new SkolemSymbolWithMostParametersVisitor();

    private static LinkedHashSet<Term> skolemTerms;

    private SkolemSymbolWithMostParametersVisitor() {
    }

    public synchronized static Term getSkolemSymbolWithMostParameters(
            Term form) {
        skolemTerms = new LinkedHashSet<Term>();
        form.execPreOrder(INSTANCE);
        Term result = null;
        for (Term term : skolemTerms) {
            if (result == null) {
                result = term;
            } else {
                if (result.arity() > term.arity()) {
                    result = term;
                }
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
     */
    /*@Override*/
    public void visit(Term visited) {
        if ((visited.op() instanceof RigidFunction)
                && ((RigidFunction) visited.op()).isSkolem()) {
            skolemTerms.add(visited);
        }
    }
}
