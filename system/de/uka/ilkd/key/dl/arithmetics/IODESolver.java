/**
 * 
 */
package de.uka.ilkd.key.dl.arithmetics;

import java.rmi.RemoteException;

import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.LogicVariable;

/**
 * TODO jdq documentation since Aug 17, 2007
 * 
 * @author jdq
 * @since Aug 17, 2007
 * 
 */
public interface IODESolver extends IMathSolver {
    
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
         * @param invariantExpression the invariantExpression to set
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
         * @param postCondition the postCondition to set
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
            LogicVariable ts, Term phi, NamespaceSet nss)
            throws RemoteException;

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
    public abstract Term diffInd(DiffSystem form, Term post, NamespaceSet nss)
            throws RemoteException;
}
