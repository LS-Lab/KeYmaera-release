/***************************************************************************
 *   Copyright (C) 2007 by Jan-David Quesel                                *
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

import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnableToConvertInputException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.LogicVariable;

/**
 * An {@link IODESolver} is a special {@link IMathSolver} that handles the
 * solving of differential equations. The implementations can be accessed using
 * the {@link MathSolverManager}.
 * 
 * @author jdq
 * @author ap
 * @since Aug 17, 2007
 * 
 */
public interface IODESolver extends IMathSolver {

    /**
     * This class is used to encapsulate the results produced by the ODESolver.
     * 
     * @author jdq
     * @since Feb 1, 2008
     * 
     */
    public static class ODESolverResult {
        private Term invariantExpression;

        private Term postCondition;

        public ODESolverResult(Term invariant, Term post) {
            this.invariantExpression = invariant;
            this.postCondition = post;
        }

        /**
         * @return the invariantExpression
         */
        public Term getInvariantExpression() {
            return invariantExpression;
        }

        /**
         * @param invariantExpression
         *                the invariantExpression to set
         */
        public void setInvariantExpression(Term invariantExpression) {
            this.invariantExpression = invariantExpression;
        }

        /**
         * @return the postCondition
         */
        public Term getPostCondition() {
            return postCondition;
        }

        /**
         * @param postCondition
         *                the postCondition to set
         */
        public void setPostCondition(Term postCondition) {
            this.postCondition = postCondition;
        }
    }

    /**
     * Solves the given differential equation system
     * 
     * @param form
     *                the system to solve
     * @param t
     *                the logical variable used as time
     * @param ts
     * @param phi
     *                the formula to be updated by the solutions of the
     *                differential equations
     * @param nss
     *                the current namespace sets
     * @return a Term containing an update for the solutions of the differential
     *         equations on the term phi
     * @throws RemoteException
     *                 if there is any problem
     */
    public abstract ODESolverResult odeSolve(DiffSystem form, LogicVariable t,
            LogicVariable ts, Term phi, Services services)
            throws RemoteException, SolverException;

    /**
     * DiffInd for the given differential equation system.
     * 
     * @param form
     *                the differential equation system
     * @param post
     *                the formula to be sustained.
     * @param nss
     *                the current namespace sets
     * @throws RemoteException
     *                 if there is any problem
     */
    public abstract Term diffInd(DiffSystem form, Term post, Services services)
            throws RemoteException, SolverException;

    /**
     * DiffFin for the given differential equation system.
     * 
     * @param form
     *                the differential equation system
     * @param post
     *                the formula to be attained.
     * @param ep
     *                progress
     * @param nss
     *                the current namespace sets
     * @throws RemoteException
     *                 if there is any problem
     * @throws UnsolveableException
     * @throws ConnectionProblemException
     * @throws ServerStatusProblemException
     * @throws UnableToConvertInputException
     */
    public abstract Term diffFin(DiffSystem form, Term post, Term ep, Services services)
            throws RemoteException, SolverException;
}
