/**
 * 
 */
package de.uka.ilkd.key.dl.arithmetics;

import java.rmi.RemoteException;
import java.util.List;

import de.uka.ilkd.key.logic.Term;

/**
 * TODO jdq documentation since Aug 17, 2007
 * 
 * @author jdq
 * @since Aug 17, 2007
 * 
 */
public interface IQuantifierEliminator extends IMathSolver {

    /**
     * TODO jdq documentation since Aug 31, 2007
     * 
     * @author jdq
     * @since Aug 31, 2007
     * 
     */
    public class PairOfTermAndQuantifierType {
        /**
         * @param t
         * @param forall
         */
        public PairOfTermAndQuantifierType(Term term, QuantifierType type) {
            this.term = term;
            this.type = type;
        }

        public Term term;

        public QuantifierType type;
    }

    public static enum QuantifierType {
        FORALL, EXISTS;
    }

    /**
     * Reduces the given term if its a mathematical expression
     * 
     */
    public abstract Term reduce(Term form) throws RemoteException;

    // /**
    // * Reduces the given term if its a mathematical expression
    // *
    // */
    // public abstract Term reduce(Term form, List<String> names)
    // throws RemoteException;

    // /**
    // * Reduces the given term if its a mathematical expression
    // *
    // */
    // public abstract Term reduce(Term form, Term quantifedSymbol,
    // QuantifierType type) throws RemoteException;

    /**
     * Reduces the given term if its a mathematical expression
     * 
     */
    public abstract Term reduce(Term form, List<String> names,
            List<PairOfTermAndQuantifierType> quantifiers)
            throws RemoteException;

    /**
     * Reduces the given term if its a mathematical expression
     * 
     * @param query
     * @param orderedList
     * @param singleton
     * @return
     */
    public abstract Term reduce(Term query,
            List<PairOfTermAndQuantifierType> quantifiers)
            throws RemoteException;
}
