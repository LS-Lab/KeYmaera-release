/***************************************************************************
 *   Copyright (C) 2009 by Philipp Ruemmer                                 *
 *   philipp@chalmers.se                                                   *
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

package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.rmi.RemoteException;

import de.uka.ilkd.key.dl.arithmetics.ISimplifier;
import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * Evaluate (sub)terms that only consist of literals and arithmetic operators.
 * This is currently directly delegated to the <code>OrbitalSimplifier</code>,
 * relying on the assumption that this simplifier does precise calculations.
 * 
 * @author philipp
 */
public class Evaluate extends AbstractDLMetaOperator {

    public static final Name NAME = new Name("#evaluate");

    public Evaluate() {
	super(NAME, 1);
    }

    public Term calculate(Term term, SVInstantiations svInst, Services services) {
	final Term toBeEvaled = term.sub(0);
	// the orbital simplifier is hardwired as it is sufficient for the evaluation of arithmetic expressions without variables and always present
	final ISimplifier simplifier = MathSolverManager.getSimplifier("Orbital");
	
	try {
	    return simplifier.simplify(toBeEvaled, services.getNamespaces());
	} catch (IllegalArgumentException e) {
	    // for instance symbolic variables
	    return toBeEvaled;
	} catch (ArithmeticException e) {
	    // for instance division by zero
	    return toBeEvaled;
	} catch (RemoteException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return toBeEvaled;
	} catch (SolverException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return toBeEvaled;
	}
    }
    
}
