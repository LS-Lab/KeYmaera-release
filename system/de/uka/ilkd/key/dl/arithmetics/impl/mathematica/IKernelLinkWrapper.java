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
     */
    public ExprAndMessages evaluate(Expr expr) throws RemoteException;

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

    /**
     * Returns a list of log messages
     */
    public List<String> getLogList() throws RemoteException;

    public Map<Expr, ExprAndMessages> getCache() throws RemoteException;

    /**
     * @param cache
     */
    public void addToCache(Map<Expr, ExprAndMessages> cache)
            throws RemoteException;

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
    public long getCachedAnwsers() throws RemoteException;

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
     * Reset the abort state.
     * 
     * @throws RemoteException
     * 
     */
    public void resetAbortState() throws RemoteException;

}
