/**
 * 
 */
package de.uka.ilkd.key.dl.arithmetics;

import java.rmi.RemoteException;
import java.util.Set;

import de.uka.ilkd.key.logic.Term;

/**
 * TODO jdq documentation since Aug 17, 2007
 * 
 * @author jdq
 * @since Aug 17, 2007
 * 
 */
public interface ISimplifier extends IMathSolver {
    /**
     * Simplifies the given term if its a mathematical expression
     * 
     */
    public abstract Term simplify(Term form) throws RemoteException;

    /**
     * Simplifies the given term if its a mathematical expression
     * 
     */
    public abstract Term simplify(Term form, Set<Term> assumptions)
            throws RemoteException;

    /**
     * Fully simplifies the given term if its a mathematical expression
     * 
     */
    public abstract Term fullSimplify(Term form) throws RemoteException;

}
