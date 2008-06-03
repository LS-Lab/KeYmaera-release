package de.uka.ilkd.key.dl.arithmetics.impl.qepcad;

import java.rmi.RemoteException;
import java.util.List;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;

/**
 * Implements the QuantifierElimintor with an external program
 * called qebcad.
 * 
 * source: http://www.cs.usna.edu/~qepcad/B/QEPCAD.html
 * 
 * @author Timo Michelsen
 *
 */
public class QepCad implements IQuantifierEliminator {

	public Term reduce(Term form, NamespaceSet nss) throws RemoteException, SolverException {
		
		return null;
	}

	public Term reduce(Term form, NamespaceSet nss, long timeout) throws RemoteException, SolverException {
		
		return null;
	}

	public Term reduce(Term form, List<String> names, List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss) throws RemoteException, SolverException {
		
		return null;
	}

	public Term reduce(Term form, List<String> names, List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss, long timeout) throws RemoteException, SolverException {
		
		return null;
	}

	public Term reduce(Term query, List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss) throws RemoteException, SolverException {
		
		return null;
	}

	public Term reduce(Term query, List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss, long timeout) throws RemoteException, SolverException {
		
		return null;
	}

	public void abortCalculation() throws RemoteException {
	}

	public long getCachedAnwserCount() throws RemoteException {
		return 0;
	}

	public String getName() {
		return null;
	}

	public long getQueryCount() throws RemoteException {
		return 0;
	}

	public String getTimeStatistics() throws RemoteException {
		return null;
	}

	public long getTotalCalculationTime() throws RemoteException {
		return 0;
	}

	public long getTotalMemory() throws RemoteException, ServerStatusProblemException, ConnectionProblemException {
		return 0;
	}

	public void resetAbortState() throws RemoteException {
	}

}
