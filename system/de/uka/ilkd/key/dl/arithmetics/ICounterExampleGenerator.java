/**
 * 
 */
package de.uka.ilkd.key.dl.arithmetics;

import java.rmi.RemoteException;

import de.uka.ilkd.key.logic.Term;

/**
 * TODO jdq documentation since Aug 17, 2007
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
     * 
     */
    public abstract String findInstance(Term form) throws RemoteException;

}
