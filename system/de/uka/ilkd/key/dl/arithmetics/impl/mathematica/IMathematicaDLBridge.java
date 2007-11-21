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
 * File created 25.01.2007
 */
package de.uka.ilkd.key.dl.arithmetics.impl.mathematica;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import de.uka.ilkd.key.dl.arithmetics.IODESolver.ODESolverResult;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.PairOfTermAndQuantifierType;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnableToConvertInputException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.LogicVariable;

/**
 * The IMathematicaDLBridge is the interface specifying the interface between
 * KeY and Mathematica.
 * 
 * @author jdq
 * @since 25.01.2007
 * 
 */
public interface IMathematicaDLBridge extends Remote {

    public String IDENTITY = "MethematicaDLBridge";

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
     * @throws UnableToConvertInputException 
     * @throws UnsolveableException 
     * @throws ConnectionProblemException 
     * @throws ServerStatusProblemException 
     */
    public ODESolverResult odeSolve(DiffSystem form, LogicVariable t,
            LogicVariable ts, Term phi, NamespaceSet nss)
            throws RemoteException, UnableToConvertInputException, ServerStatusProblemException, ConnectionProblemException, UnsolveableException;

    public Term diffInd(DiffSystem form, Term post, NamespaceSet nss)
            throws RemoteException, UnableToConvertInputException, ServerStatusProblemException, ConnectionProblemException, UnsolveableException;
    public Term diffFin(DiffSystem form, Term post, NamespaceSet nss)
            throws RemoteException, UnableToConvertInputException, ServerStatusProblemException, ConnectionProblemException, UnsolveableException;
    
    /**
     * Simplifies the given Term if possible
     * 
     * @param form
     *                the term to simplify
     * @param assumptions
     *                the assumptions used for simplification
     * @return the simplifyed term, this may be the same as the input.
     * @throws UnableToConvertInputException 
     * @throws UnsolveableException 
     * @throws ConnectionProblemException 
     * @throws ServerStatusProblemException 
     */
    public Term simplify(Term form, Set<Term> assumptions)
            throws RemoteException, UnableToConvertInputException, ServerStatusProblemException, ConnectionProblemException, UnsolveableException;

    /**
     * Simplifies the given Term if possible (using FullSimplify)
     * 
     * @param form
     *                the term to simplify
     * @param assumptions
     *                the assumptions used for simplification
     * @return the simplifyed term, this may be the same as the input.
     * @throws UnableToConvertInputException 
     * @throws UnsolveableException 
     * @throws ConnectionProblemException 
     * @throws ServerStatusProblemException 
     */
    public Term fullSimplify(Term form) throws RemoteException, UnableToConvertInputException, ServerStatusProblemException, ConnectionProblemException, UnsolveableException;

    /**
     * @param form
     * @return
     * @throws RemoteException
     * @throws UnsolveableException 
     * @throws ConnectionProblemException 
     * @throws ServerStatusProblemException 
     */
    public String findInstance(Term form) throws RemoteException, ServerStatusProblemException, ConnectionProblemException, UnsolveableException;

    /**
     * Abort the current calculation. Or the next one started.
     */
    public void abortCalculation() throws RemoteException;

    /**
     * Get the statistics about calculation time
     * 
     * @return the statistics about calculation time
     * @throws RemoteException
     */
    public String getTimeStatistics() throws RemoteException;

    /**
     * Get the total time the server was calculating while started
     * 
     * @return the total time the server was calculating while started
     * @throws RemoteException
     */
    public long getTotalCalculationTime() throws RemoteException;

    /**
     * Get the number of cached answers that were returned since the server was
     * started
     * 
     * @return the number of cached answers that were returned since the server
     *         was started
     * @throws RemoteException
     */
    public long getCachedAnwserCount() throws RemoteException;

    /**
     * Get the number of queries to the server
     * 
     * @return the number of queries to the server
     * @throws RemoteException
     */
    public long getQueryCount() throws RemoteException;

    /**
     * Reset the abort state.
     * 
     * @throws RemoteException
     * 
     */
    public void resetAbortState() throws RemoteException;

    /**
     * Calls Mathematica Reduce on the given Term.
     * 
     * @param form
     *                the Term to reduce
     * @param quantifiedSymbols
     *                the symbols that were quantified
     * @param type
     *                the type of the quantifier to reintroduce before reduction
     * @return the reduced Term. (may be the same as the input)
     * @throws RemoteException
     * @throws UnsolveableException 
     * @throws ConnectionProblemException 
     * @throws ServerStatusProblemException 
     * @throws UnableToConvertInputException 
     */
    public Term reduce(Term form, List<String> additionalReduce,
            List<PairOfTermAndQuantifierType> quantifiers)
            throws RemoteException, ServerStatusProblemException, ConnectionProblemException, UnsolveableException, UnableToConvertInputException;
}
