/*******************************************************************************
 * Copyright (c) 2009 Timo Michelsen, Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Timo Michelsen   - initial API and implementation
 *     Jan-David Quesel - implementation 
 ******************************************************************************/
package de.uka.ilkd.key.dl.arithmetics.impl.qepcad;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import scala.Tuple2;
import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.OrbitalSimplifier;
import de.uka.ilkd.key.dl.arithmetics.impl.qepcad.ProgramCommunicator.Stopper;
import de.uka.ilkd.key.dl.formulatools.Prenex;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.Quantifier;

/**
 * Implements the QuantifierElimintor with an external program called qebcad.
 * 
 * source: http://www.cs.usna.edu/~qepcad/B/QEPCAD.html
 * 
 * @author Timo Michelsen
 * 
 */
public class QepCad implements IQuantifierEliminator {

	private Stopper stopper = new Stopper();

	public QepCad(Node n) {
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
		Tuple2<Term, List<QuantifiableVariable>> result = Prenex.transform(form, nss);

		ArrayList<QuantifiableVariable> vars  = new ArrayList<QuantifiableVariable>();
		vars.addAll(result._2);
		Term in = removeUnusedQuantifiers(result._1, vars);
		System.out.println("Variables are: " + vars);
		
		QepCadInput input = Term2QepCadConverter.convert(in, vars);
		
		if (input.getVariableList().equals("()")) {
			if (OrbitalSimplifier.testForSimpleTautology(String2TermConverter
					.convert(input.getFormula(), nss))) {
				return TermBuilder.DF.tt();
			} else {
				return TermBuilder.DF.ff();
			}
		}

		System.out.println("PRENEX : Formula send to QEPCAD: "
				+ input.getFormula());
		if (input.getFormula().equals("[ TRUE ].")) {
			return TermBuilder.DF.tt();
		} else if (input.getFormula().equals("[ FALSE ].")) {
			return TermBuilder.DF.ff();
		}
		String res = ProgramCommunicator.start(input, stopper);
		// System.out.println("QEPCAD : Result                : " + res);

		Term parsedTerm = String2TermConverter.convert(res, nss);
		// System.out.println("PARSER : Result: " +
		// Term2QepCadConverter.convert(parsedTerm).getFormula()); // DEBUG

		return parsedTerm;
	}
	
	public Term removeUnusedQuantifiers(Term t, List<QuantifiableVariable> vars) {
	    if(t.op() instanceof Quantifier) {
	        ImmutableArray<QuantifiableVariable> varsBoundHere = t.varsBoundHere(0);
	        Term sub = removeUnusedQuantifiers(t.sub(0), vars);
	        for(int i = varsBoundHere.size() - 1; i >= 0; i--) {
	            QuantifiableVariable v = varsBoundHere.get(i);
	            if(sub.freeVars().contains(v)) {
	                System.out.println("Variable " + v + " occurs in " + sub);
	                if(t.op() == Quantifier.ALL) {
	                    sub = TermBuilder.DF.all(v, sub);
	                } else if(t.op() == Quantifier.EX) {
	                    sub = TermBuilder.DF.ex(v, sub);
	                } else {
	                    throw new IllegalArgumentException("Don't know how to handle quantifier in " + t);
	                }
	            } else {
	                System.out.println("Removing " + v);
	                vars.remove(v);
	                System.out.println("New List " + vars);
	            }
	        }
	        return sub;
	    }
	    System.out.println("No quantifier in " + t);
	    return t;
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
		return "QepCad";
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
		return Options.INSTANCE.getQepcadBinary().exists()
				&& Options.INSTANCE.getSaclibPath().exists();
	}

}
