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

import java.util.HashSet;
import java.util.Set;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * Metaoperator for wrapping the full simplify call of Mathematica
 * 
 * @author jdq
 * @since 30.01.2007
 * 
 */
public class FullSimplify extends AbstractDLMetaOperator {

    public static final Name NAME = new Name("#fullsimplify");

    /**
     * 
     */
    public FullSimplify() {
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
        Set<Term> assumptions = new HashSet<Term>();
        if (term.arity() > 1) {
            throw new IllegalArgumentException("Illegal number of arguments " + term.arity());
        }
        try {
            return MathSolverManager.getCurrentSimplifier().fullSimplify(
                    term.sub(0), services.getNamespaces());
        } catch (Exception e) {
            e.printStackTrace();
            if(e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
        }
        return term.sub(0);
    }

}
