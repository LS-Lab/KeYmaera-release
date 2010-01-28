package de.uka.ilkd.key.dl.arithmetics.impl.ch;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;


import de.uka.ilkd.key.dl.arithmetics.impl.ch.Options.CHMode;
import de.uka.ilkd.key.dl.arithmetics.impl.ch.cohenhormander.*;

import org.w3c.dom.Node;

import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.PairOfTermAndQuantifierType;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.FailedComputationException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.IncompleteEvaluationException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.OrbitalSimplifier;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.Op;


/* All of the boilerplate code here was copied from the qepcad wrapper.
 */

public class CohenHormander implements IQuantifierEliminator {

	private Thread workerThread;
	
	
	
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
	public Term reduce(final Term form, List<String> names,
			List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss,
			long timeout) throws RemoteException, SolverException {

		
		//System.out.println("here's my NamespaceSet:");
		//System.out.println(nss);
		
		//PrenexGeneratorResult result = PrenexGenerator.transform(form, nss);

		
		CHFormula fm = Term2CHConverter.convert(form,nss);

		workerThread = Thread.currentThread();

		System.out.println("here is what we are passing to quantifier elimination:");
		P.print_fol_formula().apply(fm);
		System.out.println();
		
		CHFormula fm1;
		
		int mode = 1;
		if(Options.INSTANCE.getEliminatorMode() == CHMode.DNF){
			mode = 2;
		}
		
		try{
			CV.start();
			fm1 = AM.real_elim_try_universal_closure(fm,mode);
		}catch(CHAbort e){
			System.out.println("caught aborted qelim");
			throw new IncompleteEvaluationException("Quantifier elimination aborted!");
		}
		

		System.out.println("here is the result of quantifier elimination:");
		P.print_fol_formula().apply(fm1);
		System.out.println();
		
		CHFormula fm2 = AM.elim_fractional_literals(fm1);
		//System.out.println(fm2);
		Term res = CH2TermConverter.convertFormula(fm2, nss);

/*
		if(fm1 instanceof True){
			res = TermBuilder.DF.tt();
		} else if (fm1 instanceof False){
			res = TermBuilder.DF.ff();
		} else{
		    //@todo backtranslation rather than saying don't know.
		    //return null;
		    //throw new FailedComputationException("CohenHormander gave a complicated formula back which we have to translate " + fm1);
			res = form;
		}
	*/		
		
		
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
			CV.stop();
		System.out.println("Stop signal sent!");
		//workerThread.interrupt();
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
