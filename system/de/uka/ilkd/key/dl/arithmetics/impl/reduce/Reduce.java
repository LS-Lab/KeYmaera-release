package de.uka.ilkd.key.dl.arithmetics.impl.reduce;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;

/**
 * Implements the QuantifierElimintor with an external program called
 * reduce/redlog.
 * 
 * source: http://www.cs.usna.edu/~qepcad/B/QEPCAD.html
 * 
 * @author jdq Jan-David Quesel
 * 
 */
public class Reduce implements IQuantifierEliminator {

	public Reduce(Node n) {
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

		System.out.println("START  : Reduce called");
		String input = Term2ReduceConverter.convert(form);
		ProcessBuilder pb = new ProcessBuilder(Options.INSTANCE
				.getReduceBinary().getAbsolutePath());
		Process process;
		try {
			process = pb.start();

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					process.getOutputStream()));
			File tmp = File.createTempFile("keymaera-reduce", ".txt");
			String generateInput = generateInput(input, tmp);
			System.out.println("Query is " + generateInput);
			out.write(generateInput);
			out.flush();
			while (process.getInputStream().available() > 0) {
				System.out.print((char) process.getInputStream().read());
			}
			System.out.println();
			try {
				process.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			FileReader f = new FileReader(tmp);

			String res = "";
			int next;
			while ((next = f.read()) != -1) {
				res += (char) next;
			}
			tmp.delete();
			System.out.println("Output of redlog is " + res);// XXX
			Term parsedTerm = String2TermConverter.convert(res, nss);
			return parsedTerm;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println("QEPCAD : Result                : " + res);
		//

		// return parsedTerm;
		return null;
	}

	private String generateInput(String input, File tmp) {
		return "load_package redlog; off rlverbose; rlset R; "
				+ "redlog_phi := " + input + ";" + "off nat; out \""
				+ tmp.getAbsolutePath() + "\"; rlqe redlog_phi; shut \""
				+ tmp.getAbsolutePath() + "\"; quit;\n";
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
	}

	public long getCachedAnswerCount() throws RemoteException {
		return 0;
	}

	public String getName() {
		return "Reduce";
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
		return Options.INSTANCE.getReduceBinary().exists()
				&& Options.INSTANCE.getReduceBinary().isFile();
	}

}
