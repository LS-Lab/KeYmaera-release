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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

import de.uka.ilkd.key.dl.arithmetics.IODESolver.ODESolverResult;
import de.uka.ilkd.key.dl.arithmetics.IODESolver.ODESolverUpdate;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.PairOfTermAndQuantifierType;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnableToConvertInputException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;
import de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker.PolynomialClassification;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.LogicVariable;

/**
 * The IMathematicaDLBridge is the interface specifying the interface between
 * KeY and Mathematica.
 * 
 * @author jdq
 * @since 25.01.2007
 * 
 */
public interface IMathematicaDLBridge extends Remote {

    public String IDENTITY = "MethematicaDLBridge";

    /**
     * Solves the given differential equation system
     * 
     * @param form
     *                the system to solve
     * @param t
     *                the logical variable used as time
     * @param ts
     * @param phi
     *                the formula to be updated by the solutions of the
     *                differential equations
     * @param services
     *                the current namespace sets
     * @return a Term containing an update for the solutions of the differential
     *         equations on the term phi
     * @throws RemoteException
     *                 if there is any problem
     * @throws UnableToConvertInputException
     * @throws UnsolveableException
     * @throws ConnectionProblemException
     * @throws ServerStatusProblemException
     */
    public ODESolverResult odeSolve(DiffSystem form, LogicVariable t,
			LogicVariable ts, Term phi, Services services)
			throws RemoteException, SolverException;

	public Term[] pdeSolve(DiffSystem form, LogicVariable t, Services services) throws RemoteException, SolverException;

	public List<ODESolverUpdate> odeUpdate(DiffSystem form, LogicVariable t,
			Services services, long timeout) throws RemoteException,
			SolverException;

    public Term diffInd(DiffSystem form, Term post, Services services)
            throws RemoteException, SolverException;

    public Term diffFin(DiffSystem form, Term post, Term ep, Services services)
            throws RemoteException, SolverException;

    /**
     * Simplifies the given Term if possible
     * 
     * @param form
     *                the term to simplify
     * @param assumptions
     *                the assumptions used for simplification
     * @return the simplifyed term, this may be the same as the input.
     * @throws UnableToConvertInputException
     * @throws UnsolveableException
     * @throws ConnectionProblemException
     * @throws ServerStatusProblemException
     */
    public Term simplify(Term form, Set<Term> assumptions, NamespaceSet nss)
            throws RemoteException, SolverException;
    /**
     * Computes parity decomposition of the formula to make sure that all atoms
     * are square-free.
     * 
     * @param form
     *                the term to simplify
     * @param assumptions
     *                the assumptions used for simplification
     * @return the simplified term, this may be the same as the input.
     * @throws RemoteException
     * @throws SolverException
     */
    public Term parityNF(Term form, NamespaceSet nss)
            throws RemoteException, SolverException;

    
    /**
     * Computes the boundary of a closed semi-algebraic set if it is defined by
     * a square-free polynomial.
     * 
     * @param form
     * @return the set boundary if the boundary is defined by a square-free 
     * polynomial, or the set itself otherwise.
     * @throws RemoteException
     * @throws SolverException
     */
    public Term getBoundary(Term form, NamespaceSet nss)
            throws RemoteException, SolverException;
    
    /**
     * Computes the condition ensuring the gradient vector of the closed 
     * invariant candidate if it is defined by a square-free polynomial.
     * 
     * @param form
     * @return condition ensuring non-zero gradient
     * polynomial, or the set itself otherwise.
     * @throws RemoteException
     * @throws SolverException
     */
    public  Term nonZeroGrad(Term form, ArrayList<String> vars, NamespaceSet nss)
            throws RemoteException, SolverException;
    
    /**
     * @author s0805753@sms.ed.ac.uk
     * 
     * Computes a conjunctive description of a quantifier-free formula in which
     * all predicate symbols are '<=', if such a description is possible.
     * 
     * N.B. equations '==' are <b>not</b> converted to '<='.
     * @throws RemoteException
     * @throws SolverException
     */
    public Term toLessEqualConjunct(Term form, NamespaceSet nss)
            throws RemoteException, SolverException;
    
    /**
     * @author s0805753@sms.ed.ac.uk
     * 
     * Checks if the formula is a conjunction of atoms where 
     * all predicate symbols are '<='.
     * 
     * N.B. equations '==' are <b>not</b> converted to '<='.
     * 
     */
    public abstract boolean isLessEqualConjunct(Term form, NamespaceSet nss)
            throws RemoteException, SolverException;
    
    /**
     * Simplifies the given Term if possible (using FullSimplify)
     * 
     * @param form
     *                the term to simplify
     * @return the simplifyed term, this may be the same as the input.
     * @throws UnableToConvertInputException
     * @throws UnsolveableException
     * @throws ConnectionProblemException
     * @throws ServerStatusProblemException
     */
    public Term fullSimplify(Term form, NamespaceSet nss) throws RemoteException, SolverException;

    /**
     * @param form
     * @param services 
     * @return
     * @throws RemoteException
     * @throws UnsolveableException
     * @throws ConnectionProblemException
     * @throws ServerStatusProblemException
     */
    public String findInstance(Term form, long tmeout, Services services) throws RemoteException,
			SolverException;

    public Map<String, Double> findInstanceD(Term form, long timeout, Services services) throws RemoteException,
            SolverException;

	// finds multiple instances satisfying form
	public List<String> findMultiInstance(Term form, int ninst, long timeout)
			throws RemoteException, SolverException;

	// finds multiple numerical (Real) instances satisfying form
	public List<String> findMultiNumInstance(Term form, int ninst, long timeout)
			throws RemoteException, SolverException;
    

    
    /**
     * Tries to find a transition within the program of modalForm from a state satisfying initial
     * to a state satisfying postcondition of modalForm.
     * 
     * @param initial first-order formula characterising initial state
     * @param modalForm Modal formula <p>F characterising transition system p and reachability question F.
     *  Possibly, <p>F is prefixed with an update giving {U}<p>F.
     * @return true if successfully found a transition from initial via U and p to F,
     *  or false for unknown. 
     * @throws RemoteException
     * @throws UnsolveableException
     * @throws ConnectionProblemException
     * @throws ServerStatusProblemException
     * 
     */
    public String findTransition(Term initial, Term modalForm, long timeout, Services services) throws RemoteException,
            SolverException;

    /**
     * Abort the current calculation. Or the next one started.
     */
    public void abortCalculation() throws RemoteException;

    /**
     * Get the statistics about calculation time
     * 
     * @return the statistics about calculation time
     * @throws RemoteException
     */
    public String getTimeStatistics() throws RemoteException;

    /**
     * Get the total time the server was calculating while started
     * 
     * @return the total time the server was calculating while started
     * @throws RemoteException
     */
    public long getTotalCalculationTime() throws RemoteException;

    /**
     * Get the maximum number of bytes used while started.
     * @return
     * @throws RemoteException
     * @throws ServerStatusProblemException
     * @throws ConnectionProblemException
     */
    public long getTotalMemory() throws RemoteException, ServerStatusProblemException, ConnectionProblemException;

    /**
     * Get the number of cached answers that were returned since the server was
     * started
     * 
     * @return the number of cached answers that were returned since the server
     *         was started
     * @throws RemoteException
     */
    public long getCachedAnwserCount() throws RemoteException;

    /**
     * Get the number of queries to the server
     * 
     * @return the number of queries to the server
     * @throws RemoteException
     */
    public long getQueryCount() throws RemoteException;

    /**
     * Reset the abort state.
     * 
     * @throws RemoteException
     * 
     */
    public void resetAbortState() throws RemoteException;

    /**
     * Calls Mathematica Reduce on the given Term.
     * 
     * @param form
     *                the Term to reduce
     * @param quantifiers
     *                the symbols that were quantified
     * @return the reduced Term. (may be the same as the input)
     * @throws RemoteException
     * @throws UnsolveableException
     * @throws ConnectionProblemException
     * @throws ServerStatusProblemException
     * @throws UnableToConvertInputException
     */
    public Term reduce(Term form, List<String> additionalReduce,
            List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss, long timeout)
            throws RemoteException, SolverException;
    
	/**
	 * @param terms
	 * TODO documentation since Jun 6, 2008
	 */
	boolean checkForConstantGroebnerBasis(PolynomialClassification<Term> terms, Services services) throws RemoteException;

	public Term[] computeGroebnerBasis(Term[] polynomials, Services services) throws RemoteException, SolverException;

	public Term polynomialReduce(Term poly, Term[] reductions, Services services) throws RemoteException, SolverException;
	 
	/**
	 * Generate plot data for the solution of the differential equation system from the given intial values
	 * 
	 * @param sys
	 * @param t
	 * @param minT
	 * @param maxT
	 * @param sampling 
	 * @param initialValues
	 * @return
	 * @throws RemoteException 
	 * @throws SolverException 
	 */
	public Map<String, Double[][]> getPlotData(DiffSystem sys, String t, double minT, double maxT, double sampling, Map<String, Double> initialValues, Services services) throws SolverException, RemoteException;

	public Map<String, Double[][]> getPlotData(Term in, Services services,
			double tendLimi,
			int nUnroLoop, double randMin, double randMax) throws RemoteException, SolverException;

	public Term getVCs(Term form, Term chi, ArrayList<Term> vectorField,
			ArrayList<String> stateVars, NamespaceSet nss) throws RemoteException, SolverException ;

    public abstract Term diffRI(DiffSystem form, Term post, Services services, String op)
            throws RemoteException, SolverException;
}
