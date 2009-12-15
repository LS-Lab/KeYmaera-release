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
package de.uka.ilkd.key.dl.arithmetics;

import java.rmi.RemoteException;

import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;

/**
 * The interface that every math solver has to implement.
 * 
 * @author jdq
 * @since 25.01.2007
 * 
 */
public interface IMathSolver {

    /**
     * @return
     */
    public abstract String getName();

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
     * Get the maximum number of bytes used while started.
     * @return
     * @throws RemoteException
     * @throws ServerStatusProblemException
     * @throws ConnectionProblemException
     */
    public long getTotalMemory() throws RemoteException, ServerStatusProblemException, ConnectionProblemException;

    /**
     * Get the number of cached answers that were returned since the server was
     * started
     * 
     * @return the number of cached answers that were returned since the server
     *         was started
     * @throws RemoteException
     */
    public long getCachedAnswerCount() throws RemoteException;

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
     * If the mathsolver is configured by the initial dialog you have to as well implement this check in ...
     * @return true, if the tool is probably configured
     */
    public boolean isConfigured();
}
