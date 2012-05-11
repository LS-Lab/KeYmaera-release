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
package de.uka.ilkd.key.dl.arithmetics;

import java.rmi.RemoteException;
import java.util.List;

import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Term;

/**
 * A {@link ICounterExampleGenerator} is used to check a term for
 * unsatisfiability. The implementations can be accessed using the
 * {@link MathSolverManager}.
 * 
 * @author jdq
 * @since Aug 17, 2007
 * 
 */
public interface ICounterExampleGenerator extends IMathSolver {

    /**
     * Tries to find an instantiation for the given term if its a mathematical
     * expression
     * 
     * @throws RemoteException
     * @throws UnsolveableException
     * @throws ConnectionProblemException
     * @throws ServerStatusProblemException
     * 
     */
    public abstract String findInstance(Term form, long timeout, Services s)
            throws RemoteException, SolverException;

    /**
     * Tries to find a transition within the program of modalForm from a state
     * satisfying initial to a state satisfying postcondition of modalForm.
     * 
     * @param initial
     *                first-order formula characterising initial state
     * @param modalForm
     *                Modal formula
     *                <p>
     *                F characterising transition system p and reachability
     *                question F. Possibly,
     *                <p>
     *                F is prefixed with an update giving {U}
     *                <p>
     *                F.
     * @return true if successfully found a transition from initial via U and p
     *         to F, or false for unknown.
     * @throws RemoteException
     * @throws UnsolveableException
     * @throws ConnectionProblemException
     * @throws ServerStatusProblemException
     * 
     */
    String findTransition(Term initial, Term modalForm, long timeout, Services services)
            throws RemoteException, SolverException;

	public abstract List<String> findMultiInstance(Term form, int ninst, long timeout)
	            throws RemoteException, SolverException;

	public abstract List<String> findMultiNumInstance(Term form, int ninst, long timeout)
	            throws RemoteException, SolverException;

}
