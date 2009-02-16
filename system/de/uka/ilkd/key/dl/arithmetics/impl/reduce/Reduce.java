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
import de.uka.ilkd.key.dl.arithmetics.impl.reduce.Options.ReduceSwitch;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;

/**
 * Implements the QuantifierElimintor with an external program called
 * reduce/redlog.
 * 
 * source: http://www.algebra.fim.uni-passau.de/~redlog/
 * http://reduce-algebra.sourceforge.net/
 * 
 * @author jdq Jan-David Quesel
 * 
 */
public class Reduce implements IQuantifierEliminator {

	public Reduce(Node n) {
		// TODO: n contains configuration from the XML files
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
			process.destroy();
			return parsedTerm;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String generateInput(String input, File tmp) {
		// TODO: use all those options
		String result = "load_package redlog; off rlverbose; rlset R; ";

		if (Options.INSTANCE.getRlanuexgcdnormalize() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlanuexgcdnormalize().name()
					.toLowerCase()
					+ " rlanuexgcdnormalize; ";
		}
		if (Options.INSTANCE.getRlanuexpsremseq() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlanuexpsremseq().name()
					.toLowerCase()
					+ " rlanuexpsremseq; ";
		}
		if (Options.INSTANCE.getRlanuexsgnopt() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlanuexsgnopt().name().toLowerCase()
					+ " rlanuexsgnopt; ";
		}
		if (Options.INSTANCE.getRlcadaproj() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlcadaproj().name().toLowerCase()
					+ " rlcadaproj; ";
		}
		if (Options.INSTANCE.getRlcadaprojalways() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlcadaprojalways().name()
					.toLowerCase()
					+ " rlcadaprojalways; ";
		}
		if (Options.INSTANCE.getRlcadbaseonly() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlcadbaseonly().name().toLowerCase()
					+ " rlcadbaseonly; ";
		}
		if (Options.INSTANCE.getRlcadextonly() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlcadextonly().name().toLowerCase()
					+ " rlcadextonly; ";
		}
		if (Options.INSTANCE.getRlcadfac() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlcadfac().name().toLowerCase()
					+ " rlcadfac; ";
		}
		if (Options.INSTANCE.getRlcadfulldimonly() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlcadfulldimonly().name()
					.toLowerCase()
					+ " rlcadfulldimonly; ";
		}
		if (Options.INSTANCE.getRlcadhongproj() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlcadhongproj().name().toLowerCase()
					+ " rlcadhongproj; ";
		}
		if (Options.INSTANCE.getRlcadisoallroots() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlcadisoallroots().name()
					.toLowerCase()
					+ " rlcadisoallroots; ";
		}
		if (Options.INSTANCE.getRlcadpartial() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlcadpartial().name().toLowerCase()
					+ " rlcadpartial; ";
		}
		if (Options.INSTANCE.getRlcadpbfvs() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlcadpbfvs().name().toLowerCase()
					+ " rlcadpbfvs; ";
		}
		if (Options.INSTANCE.getRlcadprojonly() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlcadprojonly().name().toLowerCase()
					+ " rlcadprojonly; ";
		}
		if (Options.INSTANCE.getRlcadrawformula() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlcadrawformula().name()
					.toLowerCase()
					+ " rlcadrawformula; ";
		}
		if (Options.INSTANCE.getRlcadte() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlcadte().name().toLowerCase()
					+ " rlcadte; ";
		}
		if (Options.INSTANCE.getRlcadtrimtree() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlcadtrimtree().name().toLowerCase()
					+ " rlcadtrimtree; ";
		}
		if (Options.INSTANCE.getRlqedfs() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlqedfs().name().toLowerCase()
					+ " rlqedfs; ";
		}
		if (Options.INSTANCE.getRlqeheu() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlqeheu().name().toLowerCase()
					+ " rlqeheu; ";
		}
		if (Options.INSTANCE.getRlqepnf() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlqepnf().name().toLowerCase()
					+ " rlqepnf; ";
		}
		if (Options.INSTANCE.getRlqeqsc() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlqeqsc().name().toLowerCase()
					+ " rlqeqsc; ";
		}
		if (Options.INSTANCE.getRlqesqsc() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlqesqsc().name().toLowerCase()
					+ " rlqesqsc; ";
		}
		if (Options.INSTANCE.getRlsimpl() != ReduceSwitch.DEFAULT) {
			result += Options.INSTANCE.getRlsimpl().name().toLowerCase()
					+ " rlsimpl; ";
		}

		return result + "redlog_phi := "
				+ (Options.INSTANCE.isRlall() ? "rlall(" : "(") + input + ");"
				+ "off nat; out \"" + tmp.getAbsolutePath() + "\"; "
				+ Options.INSTANCE.getQeMethod().getMethod()
				+ " redlog_phi; shut \"" + tmp.getAbsolutePath()
				+ "\"; quit;\n";
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
