/***************************************************************************
 *   Copyright (C) 2008 by Jan David Quesel                                *
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
package de.uka.ilkd.key.dl.arithmetics.impl.hollight;

import java.rmi.RemoteException;
import java.util.List;

import org.w3c.dom.Node;

import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.impl.qepcad.PrenexGenerator;
import de.uka.ilkd.key.dl.arithmetics.impl.qepcad.PrenexGenerator.PrenexGeneratorResult;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;

/**
 * @author jdq TODO Documentation since 29.04.2009
 */
public class HOLLight implements IQuantifierEliminator {

	private ProgramCommunicator.Stopper stopper = new ProgramCommunicator.Stopper();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator#reduce(de.uka.ilkd
	 * .key.logic.Term, de.uka.ilkd.key.logic.NamespaceSet)
	 */
	@Override
	public Term reduce(Term form, NamespaceSet nss) throws RemoteException,
			SolverException {
		// TODO: HOL Light does not need the prenex form, but we need the
		// universal closure...
		PrenexGeneratorResult result = PrenexGenerator.transform(form, nss);

		switch (Options.INSTANCE.getMethod()) {
		case ProofProducing:
			String convert = Term2HOLLightConverter.convert(result.getTerm(),
					result.getVariables());
			System.out.println("time REAL_QELIM_CONV`" + convert + "`;;");
			String res = ProgramCommunicator.start(convert,
					stopper);
			if (res.replaceAll(" ", "").replaceAll("\n", "").contains("<=>T")) {
				return TermBuilder.DF.tt();
			}
			break;
		case Harrison:
			convert = Term2HarrisonConverter.convert(result.getTerm(),
					result.getVariables());
			System.out.println("time real_qelim <<" + convert + ">>;;");
			res = ProgramCommunicator.start(convert,
					stopper);
			if (res.contains("fol formula = <<true>>")) {
				return TermBuilder.DF.tt();
			}
			break;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator#reduce(de.uka.ilkd
	 * .key.logic.Term, de.uka.ilkd.key.logic.NamespaceSet, long)
	 */
	@Override
	public Term reduce(Term form, NamespaceSet nss, long timeout)
			throws RemoteException, SolverException {
		return reduce(form, nss);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator#reduce(de.uka.ilkd
	 * .key.logic.Term, java.util.List, java.util.List,
	 * de.uka.ilkd.key.logic.NamespaceSet)
	 */
	@Override
	public Term reduce(Term form, List<String> names,
			List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss)
			throws RemoteException, SolverException {
		return reduce(form, nss);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator#reduce(de.uka.ilkd
	 * .key.logic.Term, java.util.List, java.util.List,
	 * de.uka.ilkd.key.logic.NamespaceSet, long)
	 */
	@Override
	public Term reduce(Term form, List<String> names,
			List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss,
			long timeout) throws RemoteException, SolverException {
		return reduce(form, nss);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator#reduce(de.uka.ilkd
	 * .key.logic.Term, java.util.List, de.uka.ilkd.key.logic.NamespaceSet)
	 */
	@Override
	public Term reduce(Term query,
			List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss)
			throws RemoteException, SolverException {
		return reduce(query, nss);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator#reduce(de.uka.ilkd
	 * .key.logic.Term, java.util.List, de.uka.ilkd.key.logic.NamespaceSet,
	 * long)
	 */
	@Override
	public Term reduce(Term query,
			List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss,
			long timeout) throws RemoteException, SolverException {
		return reduce(query, nss);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#abortCalculation()
	 */
	@Override
	public void abortCalculation() throws RemoteException {
		stopper.stop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getCachedAnswerCount()
	 */
	@Override
	public long getCachedAnswerCount() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getName()
	 */
	@Override
	public String getName() {
		return "HOL Light";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getQueryCount()
	 */
	@Override
	public long getQueryCount() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getTimeStatistics()
	 */
	@Override
	public String getTimeStatistics() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getTotalCalculationTime()
	 */
	@Override
	public long getTotalCalculationTime() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getTotalMemory()
	 */
	@Override
	public long getTotalMemory() throws RemoteException,
			ServerStatusProblemException, ConnectionProblemException {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#isConfigured()
	 */
	@Override
	public boolean isConfigured() {
		return Options.INSTANCE.getOcamlPath().isFile()
				&& Options.INSTANCE.getHollightPath().exists();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#resetAbortState()
	 */
	@Override
	public void resetAbortState() throws RemoteException {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 */
	public HOLLight(Node n) {
		// TODO Auto-generated constructor stub
	}
}
