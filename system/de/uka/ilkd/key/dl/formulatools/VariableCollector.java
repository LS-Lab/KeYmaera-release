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
 * File created 22.02.2007
 */
package de.uka.ilkd.key.dl.formulatools;

import java.util.HashSet;
import java.util.Set;

import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.ProgramVariable;

/**
 * Visitor implementation that collects the variables occurring in a given term.
 * 
 * @author jdq
 * @since 22.02.2007
 * 
 */
public class VariableCollector extends Visitor {

    private static final VariableCollector INSTANCE = new VariableCollector();

    private static HashSet<String> variables = new HashSet<String>();

    /**
     * Returns all logic, meta and program variables as well as function symbols
     * with arity 0 if they do not represent a number
     * 
     * @param term
     * @return
     */
    public synchronized static Set<String> getVariables(Term term) {
        variables.clear();
        term.execPreOrder(INSTANCE);
        return variables;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
     */
    @Override
    public void visit(Term visited) {
        if (visited.op() instanceof LogicVariable
                || visited.op() instanceof ProgramVariable
                || (visited.op() instanceof Function && visited.arity() == 0)
                || visited.op() instanceof Metavariable) {
            try {
                Double.parseDouble(visited.op().name().toString());
            } catch (Exception e) {
                variables.add(visited.op().name().toString());
            }
        }
    }

}
