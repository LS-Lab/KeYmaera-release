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

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import com.wolfram.jlink.Expr;

import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;

/**
 * The IKernelLinkWrapper is the interface specification for the remote
 * Mathematica server.
 * 
 * @author jdq
 * @since 25.01.2007
 * 
 */
public interface IKernelLinkWrapper extends Remote, Serializable {

    public static class ExprAndMessages implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = -7180022676045195743L;

        public Expr expression;

        public Expr messages;

        /**
         * @param result
         * @param msg
         */
        public ExprAndMessages(Expr result, Expr msg) {
            this.expression = result;
            this.messages = msg;
        }

    }

    /**
     * Evaluates the given expr
     * 
     * @param expr
     *                the expr to evaluate
     * @return an object containing the result from Mathematica and possible
     *         messages
     * @throws RemoteException
     *                 if there was any problem, this exception may have nested
     *                 ones.
     * @throws ConnectionProblemException 
     * @throws ServerStatusProblemException 
     * @throws UnsolveableException 
     */
    public ExprAndMessages evaluate(Expr expr) throws RemoteException, ServerStatusProblemException, ConnectionProblemException, UnsolveableException;
    public ExprAndMessages evaluate(Expr expr, long timeout) throws RemoteException, ServerStatusProblemException, ConnectionProblemException, UnsolveableException;
    public ExprAndMessages evaluate(Expr expr, long timeout, long memoryconstraint) throws RemoteException, ServerStatusProblemException, ConnectionProblemException, UnsolveableException;
    public ExprAndMessages evaluate(Expr expr, long timeout, boolean allowCache) throws RemoteException, ServerStatusProblemException, ConnectionProblemException, UnsolveableException;
    
    public Expr nativeEvaluate(String expr) throws ServerStatusProblemException, RemoteException;
    /**
     * Interrupts the current calculation
     * 
     * @throws RemoteException
     *                 if there was an error
     */
    public void interruptCalculation() throws RemoteException;

    /**
     * Returns an integer indicating the current server status.
     * 
     * @throws RemoteException
     * 
     */
    public int getStatus() throws RemoteException;

    public Map<Expr, ExprAndMessages> getCache() throws RemoteException;

    /**
     * @param cache
     */
    public void addToCache(Map<Expr, ExprAndMessages> cache)
            throws RemoteException;

    public void clearCache() throws RemoteException;

    /**
     * Get the number of queries to the server
     * 
     * @return the number of queries to the server
     * @throws RemoteException
     */
    public long getCallCount() throws RemoteException;

    /**
     * Get the number of cached answers that were returned since the server was
     * started
     * 
     * @return the number of cached answers that were returned since the server
     *         was started
     * @throws RemoteException
     */
    public long getCachedAnswers() throws RemoteException;

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
     * Get the maximum number of bytes used while started.
     * @return
     * @throws RemoteException
     * @throws ServerStatusProblemException
     * @throws ConnectionProblemException
     */
    public long getTotalMemory() throws RemoteException, ServerStatusProblemException, ConnectionProblemException;

    
    /**
     * Reset the abort state.
     * 
     * @throws RemoteException
     * 
     */
    public void resetAbortState() throws RemoteException;

}
