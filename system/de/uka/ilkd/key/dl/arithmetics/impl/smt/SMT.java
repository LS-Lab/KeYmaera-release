package de.uka.ilkd.key.dl.arithmetics.impl.smt;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.impl.qepcad.ProgramCommunicator.Stopper;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;

/**
 * Implements the QuantifierElimintor with an external program using SMT LIB.
 * 
 * @author jdq
 * 
 */
public class SMT implements IQuantifierEliminator {

	private Stopper stopper = new Stopper();

	public SMT(Node n) {
		// TODO: n beinhaltet Konfigurationseinstellungen in XML-Format
	}

	public Term reduce(Term form, NamespaceSet nss) throws RemoteException,
			SolverException {
		return reduce(form, new ArrayList<String>(),
				new ArrayList<PairOfTermAndQuantifierType>(), nss, -1);
	}

	public Term reduce(Term form, NamespaceSet nss, long timeout)
			throws RemoteException, SolverException {
		return reduce(form, new ArrayList<String>(),
				new ArrayList<PairOfTermAndQuantifierType>(), nss, timeout);
	}

	public Term reduce(Term form, List<String> names,
			List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss)
			throws RemoteException, SolverException {
		return reduce(form, names, quantifiers, nss, -1);
	}

	// Main-implementation
	public Term reduce(Term form, List<String> names,
			List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss,
			long timeout) throws RemoteException, SolverException {

		SMTInput input = Term2SMTConverter.convert(form,
				new ArrayList<QuantifiableVariable>());
//		if (input.getVariableList().equals("()")) {
//			if (OrbitalSimplifier.testForSimpleTautology(String2TermConverter
//					.convert(input.getFormula(), nss))) {
//				return TermBuilder.DF.tt();
//			} else {
//				return TermBuilder.DF.ff();
//			}
//		}

//		System.out.println("PRENEX : Formula send to QEPCAD: "
//				+ input.getFormula());
//		if (input.getFormula().equals("[ TRUE ].")) {
//			return TermBuilder.DF.tt();
//		} else if (input.getFormula().equals("[ FALSE ].")) {
//			return TermBuilder.DF.ff();
//		}
//		String res = ProgramCommunicator.start(input, stopper);
		// System.out.println("QEPCAD : Result                : " + res);

//		Term parsedTerm = String2TermConverter.convert(res, nss);
		// System.out.println("PARSER : Result: " +
		// Term2QepCadConverter.convert(parsedTerm).getFormula()); // DEBUG
		System.out.println("SMT Input: \n" + input.getVariableList() + "\n" + "(assert (not " + input.getFormula() + "))" + "\n" + "(check-sat)");
		return null;
	}

	public Term reduce(Term query,
			List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss)
			throws RemoteException, SolverException {
		return reduce(query, new ArrayList<String>(), quantifiers, nss, -1);
	}

	public Term reduce(Term query,
			List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss,
			long timeout) throws RemoteException, SolverException {
		return reduce(query, new ArrayList<String>(), quantifiers, nss, timeout);
	}

	public void abortCalculation() throws RemoteException {
		stopper.stop();
	}

	public long getCachedAnswerCount() throws RemoteException {
		return 0;
	}

	public String getName() {
		return "SMT";
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

	public long getTotalMemory() throws RemoteException,
			ServerStatusProblemException, ConnectionProblemException {
		return 0;
	}

	public void resetAbortState() throws RemoteException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#isConfigured()
	 */
	/* @Override */
	public boolean isConfigured() {
		return true;
	}

}
