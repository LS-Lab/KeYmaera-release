/**
 * File created 25.01.2007
 */
package de.uka.ilkd.key.dl.arithmetics;

import java.rmi.RemoteException;

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
}
