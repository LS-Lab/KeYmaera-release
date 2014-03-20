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
 * File created 30.01.2007
 */
package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.util.ArrayList;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.PairOfTermAndQuantifierType;
import de.uka.ilkd.key.dl.arithmetics.impl.reduce.Options;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * Metaoperator for wrapping the reduce call to Mathematica
 * 
 * @author jdq
 * @since 30.01.2007
 * 
 */
public class Reduce extends AbstractDLMetaOperator {

    public static final Name NAME = new Name("#reduce");

    /**
     * 
     */
    public Reduce() {
        super(NAME, 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#calculate(de.uka.ilkd.key.logic.Term,
     *      de.uka.ilkd.key.rule.inst.SVInstantiations,
     *      de.uka.ilkd.key.java.Services)
     */
    public Term calculate(Term term, SVInstantiations svInst, Services services) {
        // for soundness make sure that redlog does not use universal closure
        boolean rlall = Options.INSTANCE.isRlall();
        try {
            Options.INSTANCE.setRlall(false);
            // TODO add timeout
            return MathSolverManager.getCurrentQuantifierEliminator().reduce(
                    term.sub(0), new ArrayList<PairOfTermAndQuantifierType>(),
                    services.getNamespaces());
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            e.printStackTrace();// XXX
            throw new IllegalArgumentException(
                    "Exception occurred while trying to eliminate a quantifier",
                    e);
        } finally {
            Options.INSTANCE.setRlall(rlall);
        }
        // return term.sub(0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#sort(de.uka.ilkd.key.logic.Term[])
     */
    /*@Override*/
    public Sort sort(Term[] term) {
        return Sort.FORMULA;
    }

}
