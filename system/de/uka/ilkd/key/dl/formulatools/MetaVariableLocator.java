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
 * 
 */
package de.uka.ilkd.key.dl.formulatools;

import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.MetaVariable;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.SetAsListOfMetavariable;
import de.uka.ilkd.key.logic.op.SetOfMetavariable;

/**
 * TODO jdq documentation since Aug 28, 2007
 * 
 * @author jdq
 * @since Aug 28, 2007
 * 
 */
public class MetaVariableLocator extends Visitor {

    public static final MetaVariableLocator INSTANCE = new MetaVariableLocator();

    private SetOfMetavariable result;

    /**
     * TODO jdq documentation since Aug 28, 2007
     * 
     * @param dominantTerm
     * @return
     */
    public synchronized SetOfMetavariable find(Term dominantTerm) {
        result = SetAsListOfMetavariable.EMPTY_SET;
        dominantTerm.execPreOrder(this);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
     */
    @Override
    public void visit(Term visited) {
        if (visited.op() instanceof Modality) {
            result = result
                    .union(findInsideModality((ProgramElement) ((StatementBlock) visited
                            .javaBlock().program()).getChildAt(0)));
        }

    }

    /**
     * TODO jdq documentation since Aug 28, 2007
     * 
     * @param programElement
     * @return
     */
    private SetOfMetavariable findInsideModality(ProgramElement programElement) {
        SetOfMetavariable result = SetAsListOfMetavariable.EMPTY_SET;
        if (programElement instanceof DLNonTerminalProgramElement) {
            DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) programElement;
            for (ProgramElement p : dlnpe) {
                result = result.union(findInsideModality(p));
            }
        } else if (programElement instanceof MetaVariable) {
            MetaVariable var = (MetaVariable) programElement;
            result = result.add((Metavariable) Main.getInstance().mediator()
                    .getServices().getNamespaces().variables().lookup(
                            var.getElementName()));
        }
        return result;
    }

}
