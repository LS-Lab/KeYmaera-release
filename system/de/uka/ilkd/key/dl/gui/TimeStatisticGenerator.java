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
package de.uka.ilkd.key.dl.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import de.uka.ilkd.key.dl.arithmetics.IMathSolver;
import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.gui.AutoModeListener;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.proof.ProofEvent;

/**
 * Class used for the time statistic generation
 * 
 * @author jdq
 * 
 */
public class TimeStatisticGenerator implements AutoModeListener {

	private static class SolverStatistics {
		public long accumulatedTime;
		public long solverTime;
		public long totalMemory;
		public long cachedAnswers;
		public long queries;
		public long startTime;
		public boolean started;
		public boolean valid = true;
	}
	
	private static final StatDialog statDialog = new StatDialog();

	public static final TimeStatisticGenerator INSTANCE = new TimeStatisticGenerator();
	
	private Proof currentProof;
	
	private Map<Proof, SolverStatistics> statistics = new HashMap<Proof, SolverStatistics>();
	
	private TimeStatisticGenerator() {
		
	}

	public static void hookTimeStatisticGenerator(JMenu menu) {
		JMenuItem item = new JMenuItem("Open statistic generator");
		item.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				statDialog.setVisible(true);
			}

		});
		menu.add(item);
	}

	static class StatDialog extends JFrame {
		/**
         * 
         */
		private static final long serialVersionUID = -185617352313612850L;

		private final JLabel label = new JLabel("Time: 0");

		public StatDialog() {
			add(label);
			pack();
		}
	}

	public void autoModeStarted(ProofEvent e) {
		if (currentProof == e.getSource()) {
			SolverStatistics s = statistics.get(e.getSource());
			if (!s.started) {
				s.started = true;
				s.startTime = System.currentTimeMillis();
			}
		}
	}

	public void autoModeStopped(ProofEvent e) {
		if (currentProof == e.getSource()) {
			final SolverStatistics s = statistics.get(e.getSource());
			if (s != null && s.started) {
				s.accumulatedTime += System.currentTimeMillis() - s.startTime;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						statDialog.label.setText("Time: " + s.accumulatedTime);
						statDialog.pack();
					}
				});
				s.started = false;
			}
		}
	}

	public long getTime(Proof p) {
		return statistics.get(p).accumulatedTime;
	}

	/**
	 * Queries the statistics from the underlying solver.
	 * @return
	 * @throws RemoteException
	 */
	public String getSolverTimes() throws RemoteException {
		if (MathSolverManager.isQuantifierEliminatorSet()) {
			IMathSolver solver = MathSolverManager
					.getCurrentQuantifierEliminator();
			return solver.getTimeStatistics();
		} else {
			return "na";
		}
	}

	/**
	 * Queries the statistics from the underlying solver.
	 * @return The total calculation time.
	 * @throws RemoteException
	 */
	public long getTotalCalculationTime() throws RemoteException {
		if (MathSolverManager.isQuantifierEliminatorSet()) {
			IMathSolver solver = MathSolverManager
					.getCurrentQuantifierEliminator();
			return solver.getTotalCalculationTime();
		} else {
			return -1;
		}
	}
	
	/**
	 * Returns the total calculation time statistics recorded for the specified proof.
	 * @param p The proof.
	 * @return The total calculation time.
	 */
	public long getTotalCalculationTime(Proof p) {
		return statistics.get(p).solverTime;
	}

	/**
	 * Get the maximum number of bytes used while started, including known information from backends.
	 * 
	 * @return
	 * @throws RemoteException
	 * @throws ServerStatusProblemException
	 * @throws ConnectionProblemException
	 */
	public long getTotalMemory() throws RemoteException {
		if (MathSolverManager.isQuantifierEliminatorSet()) {
			IMathSolver solver = MathSolverManager
					.getCurrentQuantifierEliminator();
			try {
				return solver.getTotalMemory();
			} catch (ServerStatusProblemException e) {
				return -1;
			} catch (ConnectionProblemException e) {
				return -1;
                        } catch (IllegalStateException e) {
                            System.out.println("This is not supposed to happen");
                            e.printStackTrace();
                            return -1000;
			}
		} else {
			return -1;
		}

	}
	
	/**
	 * Returns the total memory consumption recorded for the specified proof.
	 * @param p The proof.
	 * @return The total memory consumption.
	 */
	public long getTotalMemory(Proof p) {
		return statistics.get(p).totalMemory;
	}

	/**
	 * Queries the statistics from the underlying solver.
	 * @return The number of cached answers.
	 */
	public long getCachedAnswers() throws RemoteException {
		if (MathSolverManager.isQuantifierEliminatorSet()) {
			IMathSolver solver = MathSolverManager
					.getCurrentQuantifierEliminator();
			return solver.getCachedAnswerCount();
		} else {
			return -1;
		}
	}
	
	/**
	 * Returns the cached answer statistics recorded for the specified proof.
	 * @param p The proof.
	 * @return The number of cached answers.
	 */
	public long getCachedAnswers(Proof p) {
		return statistics.get(p).cachedAnswers;
	}

	/**
	 * Queries the statistics from the underlying solver.
	 * @return The number of queries.
	 */
	public long getQueries() throws RemoteException {
		if (MathSolverManager.isQuantifierEliminatorSet()) {
			IMathSolver solver = MathSolverManager
					.getCurrentQuantifierEliminator();
			return solver.getQueryCount();
		} else {
			return -1;
		}
	}
	
	/**
	 * Returns the query statistics recorded for the specified proof.
	 * @param p The proof.
	 * @return The number of queries.
	 */
	public long getQueries(Proof p) {
		return statistics.get(p).queries;
	}
	
	/**
	 * Indicates whether valid statistics are available for the specified proof.
	 * @param p The proof.
	 * @return True, if valid statistics are available; false otherwise.
	 */
	public boolean validStatisticsAvailable(Proof p) {
		return statistics.get(p).valid;
	}

	/**
	 * Print time statistics information into the given printer.
	 * @param printer
	 */
    public void print(PrintWriter printer, Proof proof) {
    	if (statistics.get(proof).valid) {
            printer.print(",  " + getTotalCalculationTime(proof) + "," + getTotalMemory(proof));
        } else {
            printer.print(",  N/A,N/A");
        }
    }
    
    /**
     * Switches statistics recording to the specified proof.
     * @param proof The proof to record statistics for.
     */
    public void recordFor(final Proof proof) {
    	// recordFor: we don't want to pollute the IMathSolvers with knowing proofs
    	if (!statistics.containsKey(proof) && proof != null) {
			statistics.put(proof, new SolverStatistics());
		}
    	
    	if (currentProof != null) {
    		updateStatistics(currentProof);
    	}
    	currentProof = proof;
    	
    	resetStatisticsInSolver(proof);
    }
    
    /**
     * Refreshes the statistics of the specified proof.
     * @param proof The proof to refresh.
     */
    public void refreshStatistics(final Proof proof) {
    	if (proof != null) {
    		updateStatistics(proof);
    		resetStatisticsInSolver(null);
    	}
    }

    /**
     * Resets the proof statistics in the solvers.
     * @param proof The proof to set invalid if something goes wrong, null for none.
     */
	private void resetStatisticsInSolver(final Proof proof) {
		if (MathSolverManager.isQuantifierEliminatorSet()) {
			IMathSolver solver = MathSolverManager
					.getCurrentQuantifierEliminator();
			try {
				solver.resetStatistics();
			} catch (RemoteException e) {
				e.printStackTrace();
				// statistics for this proof are broken from now on
				if (proof != null) statistics.get(proof).valid = false;
			}
		}
	}
	
	/**
	 * Updates the statistics for the specified proof.
	 * @param proof The proof to update.
	 */
	private void updateStatistics(final Proof proof) {
		SolverStatistics s = statistics.get(proof);
		try {
			long time = getTotalCalculationTime();
			if (time != -1) s.solverTime += time;
			else s.solverTime = -1;
			long mem = getTotalMemory();
			if (mem != -1) s.totalMemory = Math.max(s.totalMemory, mem);
			else s.totalMemory = -1;
			long answers = getCachedAnswers();
			if (answers != -1) s.cachedAnswers += answers;
			else s.cachedAnswers = -1;
			long queries = getQueries();
			if (queries != -1) s.queries += queries;
			else s.queries = -1;
		} catch (RemoteException e) {
			e.printStackTrace();
			s.valid = false;
		}
	}
}
