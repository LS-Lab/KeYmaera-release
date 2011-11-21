package de.uka.ilkd.key.dl.arithmetics.impl.smt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import de.uka.ilkd.key.dl.arithmetics.ICounterExampleGenerator;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.impl.qepcad.ProgramCommunicator.Stopper;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;

/**
 * Implements the QuantifierElimintor with an external program using SMT LIB.
 * 
 * @author jdq
 * 
 */
public class SMT implements IQuantifierEliminator, ICounterExampleGenerator {

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
				new ArrayList<QuantifiableVariable>(), nss);
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
		String smtIn = input.getVariableList() + "\n";
		smtIn += "(assert (not " + input.getFormula() + "))\n";
		String start = "Start sat check:";
		smtIn += "(echo \"" + start + "\")\n(check-sat)\n";
		File inputFile;
		System.out.println("SMT Input: \n" + smtIn);
		try {
			inputFile = File.createTempFile("keymaerasmt", ".smt2");
			inputFile.deleteOnExit();
			FileWriter in = new FileWriter(inputFile);
			in.write(smtIn);
			in.flush();
			in.close();
			Process process = Runtime.getRuntime().exec(
					Options.INSTANCE.getZ3Binary().getAbsolutePath() + " "
							+ inputFile.getAbsolutePath());
			inputFile.delete();
			BufferedReader b = new BufferedReader(new InputStreamReader(process.getInputStream()));
			boolean checkSat = false;
			String line = null;
			while((line = b.readLine()) != null) {
				System.out.println("Read: " + line);
				if(line.indexOf(start) != -1) {
					checkSat = true;
				} else if(checkSat) {
					if(line.indexOf("unsat") != -1) {
						return TermBuilder.DF.tt();
					} else if(line.indexOf("sat") != -1 || line.indexOf("unknown") != -1) {
						return TermBuilder.DF.ff();
					}
				} else {
					System.out.println("Ignoring line: " + line);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.dl.arithmetics.ICounterExampleGenerator#findInstance(de.uka.ilkd.key.logic.Term, long)
	 */
	@Override
	public String findInstance(Term form, long timeout) throws RemoteException,
			SolverException {
		NamespaceSet nss = null; // FIXME retrieve namespace set
		nss = Main.getInstance().mediator().namespaces();
		SMTInput input = Term2SMTConverter.convert(form,
				new ArrayList<QuantifiableVariable>(), nss);
		String smtIn = input.getVariableList() + "\n";
		smtIn += "(assert " + input.getFormula() + ")\n";
		String start = "Start sat check:";
		String model = "Start model:";
		smtIn += "(echo \"" + start + "\")\n(check-sat)\n";
		smtIn += "(echo \"" + model + "\")\n(get-model)\n";
		File inputFile;
		try {
			inputFile = File.createTempFile("keymaerasmt", ".smt2");
			inputFile.deleteOnExit();
			FileWriter in = new FileWriter(inputFile);
			in.write(smtIn);
			in.flush();
			in.close();
			Process process = Runtime.getRuntime().exec(
					Options.INSTANCE.getZ3Binary().getAbsolutePath() + " "
							+ inputFile.getAbsolutePath() + " MODEL=true");
			inputFile.delete();
			BufferedReader b = new BufferedReader(new InputStreamReader(process.getInputStream()));
			boolean checkSat = false;
			boolean sat = false;
			boolean readModel = false;
			String result = "";
			String line = null;
			while((line = b.readLine()) != null) {
				System.out.println("Read: " + line);
				if(line.indexOf(start) != -1) {
					checkSat = true;
				} else if(checkSat) {
					if(line.indexOf("unsat") != -1 || line.indexOf("unknown") != -1) {
						return "";
					} else if(line.indexOf("sat") != -1) {
						sat = true;
					}
					checkSat = false;
				} else if(sat && line.indexOf(model) != -1) {
					readModel = true;
				} else if(sat && readModel) {
					result += line;
				} else {
					System.out.println("Ignoring line: " + line);
				}
			}
			return result;
//			System.out.println("SMT Input: \n" + input.getVariableList() + "\n"
//					+ "(assert " + input.getFormula() + ")" + "\n"
//					+ "(check-sat)");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.dl.arithmetics.ICounterExampleGenerator#findTransition(de.uka.ilkd.key.logic.Term, de.uka.ilkd.key.logic.Term, long, de.uka.ilkd.key.java.Services)
	 */
	@Override
	public String findTransition(Term initial, Term modalForm, long timeout,
			Services services) throws RemoteException, SolverException {
		throw new UnsupportedOperationException("Transition counter examples are currently not supported with Z3.");
	}

}
