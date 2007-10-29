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
 * File created 07.02.2007
 */
package de.uka.ilkd.key.dl.formulatools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.uka.ilkd.key.dl.model.Assign;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.RandomAssign;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Modality;

/**
 * The ProgramVariableDeclaratorVisitor is used to insert declarations of
 * program variables into the namespaces.
 * 
 * @author jdq
 * @since 07.02.2007
 * 
 */
public class ProgramVariableCollector extends Visitor {

    public static final ProgramVariableCollector INSTANCE = new ProgramVariableCollector();

    private Set<String> names;
    
    private boolean found;

    /**
     * Collects all program variables that are changed in the first modality of
     * the given term
     * 
     * @param term
     * @return an ordered set containing the names of the variables is returned.
     *         the set has to be ordered thus its robust against load/save of
     *         proofs
     */
    public synchronized Set<String> getProgramVariables(Term term) {
        names = new LinkedHashSet<String>();
        found = false;
        term.execPreOrder(this);
        ArrayList<String> inv = new ArrayList<String>(names);
        names.clear();
        for(int i = inv.size() - 1; i >= 0; i--) {
            names.add(inv.get(i));
        }
        assert names.size() == inv.size();
        return names;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
     */
    @Override
    public void visit(Term visited) {
        if (!found && visited.op() instanceof Modality) {
            DLProgramElement childAt = (DLProgramElement) ((StatementBlock) visited
                    .javaBlock().program()).getChildAt(0);
            names.addAll(getProgramVariables(childAt));
            found = true;
        }
    }

    /**
     * @return
     */
    private Collection<String> getProgramVariables(ProgramElement form) {
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        if (form instanceof Dot) {
            Dot dot = (Dot) form;
            if (dot.getChildAt(0) instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
                de.uka.ilkd.key.dl.model.ProgramVariable pv = (de.uka.ilkd.key.dl.model.ProgramVariable) dot
                        .getChildAt(0);
                result.add(pv.getElementName().toString());
            }
        } else if (form instanceof RandomAssign) {
            RandomAssign dot = (RandomAssign) form;
            if (dot.getChildAt(0) instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
                de.uka.ilkd.key.dl.model.ProgramVariable pv = (de.uka.ilkd.key.dl.model.ProgramVariable) dot
                        .getChildAt(0);

                result.add(pv.getElementName().toString());
            }
        } else if (form instanceof Assign) {
            Assign assign = (Assign) form;
            if (assign.getChildAt(0) instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
                de.uka.ilkd.key.dl.model.ProgramVariable pv = (de.uka.ilkd.key.dl.model.ProgramVariable) assign
                        .getChildAt(0);
                result.add(pv.getElementName().toString());
            }
        } else if (form instanceof DLNonTerminalProgramElement) {
            DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) form;
            for (ProgramElement p : dlnpe) {
                result.addAll(getProgramVariables(p));
            }
        }

        return result;
    }
}
