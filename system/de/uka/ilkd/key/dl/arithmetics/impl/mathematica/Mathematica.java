/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel                                *
 *   quesel@informatik.uni-oldenburg.de                                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
/**
 * File created 25.01.2007
 */
package de.uka.ilkd.key.dl.arithmetics.impl.mathematica;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

import de.uka.ilkd.key.dl.arithmetics.ICounterExampleGenerator;
import de.uka.ilkd.key.dl.arithmetics.IGroebnerBasisCalculator;
import de.uka.ilkd.key.dl.arithmetics.IODESolver;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator;
import de.uka.ilkd.key.dl.arithmetics.ISimplifier;
import de.uka.ilkd.key.dl.arithmetics.abort.ServerConsole;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker.PolynomialClassification;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.LogicVariable;

/**
 * MathSolver interface for the Mathematica integration.
 * 
 * @author jdq
 * @since 25.01.2007
 * 
 */
public class Mathematica implements ICounterExampleGenerator, IODESolver,
		IQuantifierEliminator, ISimplifier, IGroebnerBasisCalculator {

	public static final String NAME = "Mathematica";

	private IMathematicaDLBridge bridge;

	private ServerConsole serverConsole;

	public Mathematica(Node node) {
		XPath xpath = XPathFactory.newInstance().newXPath();
		String server = null;
		int port = -1;
		try {
			server = (String) xpath.evaluate("server/ip", node,
					XPathConstants.STRING);
			port = Integer.parseInt((String) xpath.evaluate("server/port",
					node, XPathConstants.STRING));
			if (server == null || port == -1) {
				throw new RuntimeException("XML does not contain a correct"
						+ " server configuration: "
						+ "<server><ip/><port/></server> needed");
			}
			bridge = new MathematicaDLBridge(server, port);

		} catch (XPathExpressionException e) {
			e.printStackTrace(); // XXX
			throw new RuntimeException("Error parsing XML config", e);

		} catch (RemoteException e) {
			e.printStackTrace();// XXX
			throw new RuntimeException("Could not create bridge.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.MathSolver#evaluate(de.uka.ilkd.key.dl.Formula)
	 */
	public ODESolverResult odeSolve(DiffSystem form, LogicVariable t,
			LogicVariable ts, Term phi, Services services)
			throws RemoteException, SolverException {
		return bridge.odeSolve(form, t, ts, phi, services);
	}

    public Term[] pdeSolve(DiffSystem form, LogicVariable t, Services services)
            throws RemoteException, SolverException {
    		return bridge.pdeSolve(form, t, services);
    }

    public Term diffInd(DiffSystem form, Term post, Services services)
			throws RemoteException, SolverException {
		return bridge.diffInd(form, post, services);
	}

	public Term diffFin(DiffSystem form, Term post, Term ep, Services services)
			throws RemoteException, SolverException {
		return bridge.diffFin(form, post, ep, services);
	}

    public List<ODESolverUpdate> odeUpdate(DiffSystem form, LogicVariable t,
                       Services services, long timeout)
                       throws RemoteException, SolverException {
          return bridge.odeUpdate(form, t, services, timeout);
    }

   public List<String> findMultiInstance(Term form, int ninst, long timeout)
				   throws RemoteException, SolverException
   {
		   return bridge.findMultiInstance(form, ninst, timeout);
   }

   public String findNumInstance(Term form, long timeout)
				   throws RemoteException, SolverException {
		   return findMultiNumInstance(form, 1, timeout).get(0);
   }

   public List<String> findMultiNumInstance(Term form, int ninst, long timeout)
				   throws RemoteException, SolverException {
		   return bridge.findMultiNumInstance(form, ninst, timeout);
   }
	/*
	 * Parity decomposition ensures that atomic terms in the formula are 
	 * square-free.
	 */
	public Term parityNF(Term form, NamespaceSet nss) throws RemoteException,
			SolverException {
		return bridge.parityNF(form, nss);
	}
  
	/*
	 * Compute the boundary of the closed invariant candidate, provided that 
	 * it is given by a square-free polynomial (in)equality.
	 */
	public Term getBoundary(Term form, NamespaceSet nss) throws RemoteException,
			SolverException {
		return bridge.getBoundary(form, nss);
	}
	
	/*
	 * Compute the boundary of the closed invariant candidate, provided that 
	 * it is given by a square-free polynomial (in)equality.
	 */
	public Term nonZeroGrad(Term form, ArrayList<String> vars, NamespaceSet nss) throws RemoteException,
			SolverException {
		return bridge.nonZeroGrad(form, vars, nss);
	}
  

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.IMathSolver#getName()
	 */
	public String getName() {
		return NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.IMathSolver#simplify(de.uka.ilkd.key.logic.Term)
	 */
	public Term simplify(Term form, NamespaceSet nss) throws RemoteException,
			SolverException {
		return simplify(form, new HashSet<Term>(), nss);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.IMathSolver#simplify(de.uka.ilkd.key.logic.Term,
	 * java.util.Set)
	 */
	public Term simplify(Term form, Set<Term> assumptions, NamespaceSet nss)
			throws RemoteException, SolverException {

		return bridge.simplify(form, assumptions, nss);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.IMathSolver#fullSimplify(de.uka.ilkd.key.logic.Term)
	 */
	public Term fullSimplify(Term form, NamespaceSet nss)
			throws RemoteException, SolverException {

		return bridge.fullSimplify(form, nss);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.IMathSolver#reduce(de.uka.ilkd.key.logic.Term)
	 */
	public Term reduce(Term form, NamespaceSet nss, long timeout)
			throws RemoteException, SolverException {
		return reduce(form, new ArrayList<PairOfTermAndQuantifierType>(), nss);
	}

	public Term reduce(Term form, NamespaceSet nss) throws RemoteException,
			SolverException {
		return reduce(form, new ArrayList<PairOfTermAndQuantifierType>(), nss,
				-1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.IMathSolver#findInstance(de.uka.ilkd.key.logic.Term)
	 */
	public String findInstance(Term form, long timeout, Services services) throws RemoteException,
			SolverException {
		return bridge.findInstance(form, timeout, services);
	}

	public String findInstance(Term form, Services services) throws RemoteException,
			SolverException {
		return findInstance(form, -1, services);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.IMathSolver#abortCalculation()
	 */
	public void abortCalculation() throws RemoteException {
		bridge.abortCalculation();

	}

	public String getTimeStatistics() throws RemoteException {
		return bridge.getTimeStatistics();
	}

	public long getTotalCalculationTime() throws RemoteException {
		return bridge.getTotalCalculationTime();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.IMathSolver#getCachedAnwserCount()
	 */
	public long getCachedAnswerCount() throws RemoteException {
		return bridge.getCachedAnwserCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.IMathSolver#getQueryCount()
	 */
	public long getQueryCount() throws RemoteException {
		return bridge.getQueryCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.IMathSolver#resetAbortState()
	 */
	public void resetAbortState() throws RemoteException {
		bridge.resetAbortState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator#reduce(de.uka.ilkd
	 * .key.logic.Term, java.util.List)
	 */
	public Term reduce(Term query, List<String> additionalReduce,
			List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss,
			long timeout) throws RemoteException, SolverException {
		return bridge
				.reduce(query, additionalReduce, quantifiers, nss, timeout);
	}

	public Term reduce(Term query, List<String> additionalReduce,
			List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss)
			throws RemoteException, SolverException {
		return reduce(query, additionalReduce, quantifiers, nss, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator#reduce(de.uka.ilkd
	 * .key.logic.Term, java.util.List)
	 */
	public Term reduce(Term form,
			List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss,
			long timeout) throws RemoteException, SolverException {
		return reduce(form, new LinkedList<String>(), quantifiers, nss, timeout);
	}

	public Term reduce(Term form,
			List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss)
			throws RemoteException, SolverException {
		return reduce(form, new LinkedList<String>(), quantifiers, nss, -1);
	}

	/* @Override */
	public String findTransition(Term initial, Term modalForm, long timeout,
			Services services) throws RemoteException, SolverException {
		return bridge.findTransition(initial, modalForm, timeout, services);
	}

	public String findTransition(Term initial, Term modalForm, Services services)
			throws RemoteException, SolverException {
		return bridge.findTransition(initial, modalForm, -1, services);
	}

	/* @Override */
	public long getTotalMemory() throws RemoteException,
			ServerStatusProblemException, ConnectionProblemException {
		return bridge.getTotalMemory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uka.ilkd.key.dl.arithmetics.IGroebnerBasisCalculator#
	 * checkForConstantGroebnerBasis
	 * (de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker
	 * .PolynomialClassification)
	 */
	/* @Override */
	public boolean checkForConstantGroebnerBasis(
			PolynomialClassification<Term> terms, Services services)
			throws RemoteException {
		return bridge.checkForConstantGroebnerBasis(terms, services);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#isConfigured()
	 */
	/* @Override */
	public boolean isConfigured() {
		return Options.INSTANCE.getMathKernel().isFile()
				&& Options.INSTANCE.getMathKernel().exists();
	}
	
	public void toggleServerConsole() {
		((MathematicaDLBridge)bridge).toggleServerConsole();
	}

	@Override
	public Term[] computeGroebnerBasis(Term[] polynomials, Services services)
			throws RemoteException, SolverException {
		return bridge.computeGroebnerBasis(polynomials, services);
	}

	@Override
	public Term polynomialReduce(Term poly, Term[] reductions, Services services)
			throws RemoteException, SolverException {
		return bridge.polynomialReduce(poly, reductions, services);
	}
	
	public Map<String, Double[][]> getPlotData(DiffSystem sys, String t, double minT, double maxT, double sampling, Map<String, Double> initialValues, Services services) throws RemoteException, SolverException {
	    return bridge.getPlotData(sys, t, minT, maxT, sampling, initialValues, services);
	}
}
