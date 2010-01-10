package de.uka.ilkd.key.dl.arithmetics.impl.ch;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

//import cohenhormander.*;


import org.w3c.dom.Node;

import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.PairOfTermAndQuantifierType;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.impl.ch.PrenexGenerator.PrenexGeneratorResult;
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.OrbitalSimplifier;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.Op;


/* All of the boilerplate code here was copied from the qepcad wrapper.
 */

public class CohenHormander implements IQuantifierEliminator {

	//private Stopper stopper = new Stopper();
	
	public CohenHormander(Node n) {
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

		// System.out.println("START  : Reduce called");
		PrenexGeneratorResult result = PrenexGenerator.transform(form, nss);

		
		cohenhormander.Formula fm = Term2CHConverter.convert(result.getTerm(),
				result.getVariables());

			
		System.out.println("here is what we are passing to quantifier elimination:");
		cohenhormander.P.print_fol_formula().apply(fm);
		System.out.println();
		cohenhormander.Formula fm1 = cohenhormander.AM.real_elim(fm); 
		System.out.println("here is the result of quantifier elimination:");
		cohenhormander.P.print_fol_formula().apply(fm1);
		System.out.println();
		
		Term res;


		if(fm1 instanceof cohenhormander.True){
			res = TermBuilder.DF.tt();
		} else if (fm1 instanceof cohenhormander.False){
			res = TermBuilder.DF.ff();
		} else{
		    res = form;
		}
			
		
		
		return res;
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
	    // TODO	
		//stopper.stop();
	}

	public long getCachedAnswerCount() throws RemoteException {
		return 0;
	}

	public String getName() {
		return "CohenHormander";
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
