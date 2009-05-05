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
package de.uka.ilkd.key.dl.arithmetics.impl.csdp;

import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

import de.uka.ilkd.key.dl.options.DLOptionBean;

/**
 * @author jdq TODO Documentation since 05.05.2009
 */
public class CSDPInterface {
	private static final double BIG_EPS = 0.00001;

	private static boolean isAlmostNothing(double x) {
		return x < BIG_EPS && x > -BIG_EPS;
	}

	/**
	 * SDP method with a simpler signature, for convenience reasons.
	 * 
	 * <p>
	 * IMPORTANT: <code>constraints</code> has to be a sequence of upper
	 * triangular matrices, otherwise CSDP will do the most stupid things
	 * </p>
	 */
	private static int easiestSDP(int matrixSize, double[] constraints,
			double[] constraintRhs, double[] solution, double[] goal) {
		assert (solution.length == matrixSize * matrixSize && goal.length == matrixSize
				* matrixSize);
		final int constraintNum = constraints.length
				/ (matrixSize * matrixSize);
		final double[] y = new double[constraintRhs.length];
		final double[] Z = emptyGoal(matrixSize);
		final double[] pobj = new double[matrixSize];
		final double[] dobj = new double[constraintRhs.length];

		// not sure whether this helps
		Arrays.fill(y, 0.1);
		Arrays.fill(Z, 0.1);

		if (DLOptionBean.INSTANCE.isCsdpForceInternal()) {
			try {
				System.loadLibrary("csdp");
				return CSDP.easySDP(matrixSize, constraintNum, goal,
						constraintRhs, constraints, 0, solution, y, Z, pobj,
						dobj);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				return CSDPBinaryInterface.easySDP(matrixSize, constraintNum,
						goal, constraintRhs, constraints, 0, solution, y, Z,
						pobj, dobj);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		throw new IllegalStateException("No csdp available");

	}

	public static int sdp(int matrixSize, double[] constraints,
			double[] constraintRhs, double[] solution) {

		Arrays.fill(solution, 0.1);

		return easiestSDP(matrixSize, makeTriangular(constraints, matrixSize),
				constraintRhs, solution, diaGoal(matrixSize));
	}

	private static double[] emptyGoal(int matrixSize) {
		final double[] res = new double[matrixSize * matrixSize];
		Arrays.fill(res, 0.0);
		return res;
	}

	/**
	 * Genererate a diagonal matrix with <code>-1.0</code> entries on the
	 * diagonal
	 */
	private static double[] diaGoal(int matrixSize) {
		final double[] res = new double[matrixSize * matrixSize];
		Arrays.fill(res, 0.0);
		for (int i = 0; i < res.length; i = i + matrixSize + 1)
			res[i] = -1.0;
		return res;
	}

	public static int minimalSdp(int matrixSize, double[] constraints,
			double[] constraintRhs, double[] solution) {
		assert (solution.length == matrixSize * matrixSize);

		final double[] goal = emptyGoal(matrixSize);
		final double[] inpConstraints = makeTriangular(constraints, matrixSize);

		final int res = easiestSDP(matrixSize, inpConstraints, constraintRhs,
				solution, goal);

		if (res == 0 || res == 3) {
			// SUCCESS: try to find a nice solution that contains as small
			// entries as possible
			final SolutionEntry[] solEntryList = sortSolutionEntries(solution,
					matrixSize);
			double lastNorm = maxAbs(solution);

			for (int k = solEntryList.length - 1; k >= 0
					&& !isAlmostNothing(solEntryList[k].value); --k) {
				final int pos = solEntryList[k].row * matrixSize
						+ solEntryList[k].col;

				if (solution[pos] > 0)
					goal[pos] = -1.0;
				else
					goal[pos] = 1.0;

				// check that we still get the same result
				final int res2 = easiestSDP(matrixSize, inpConstraints,
						constraintRhs, solution, goal);
				if (!(res == res2 || res2 == 0)) {
					// go back to the previous solution
					goal[pos] = 0.0;
				} else {
					// check that the norm of the matrix has not gotten bigger
					final double newNorm = maxAbs(solution);
					if (newNorm > lastNorm + 1.0)
						// go back to the previous solution
						goal[pos] = 0.0;
					else
						lastNorm = newNorm;
				}
			}

			// ensure that we get the right solution
			int res2 = -1;
			while (true) {
				res2 = easiestSDP(matrixSize, inpConstraints, constraintRhs,
						solution, goal);
				// should really not happen that this has to be repeated ...
				if (res == res2 || res2 == 0)
					break;
				System.out
						.println("VERY BAD: am not able to restore a solution "
								+ "that was discovered earlier");
			}
		}

		return res;
	}

	private static SolutionEntry[] sortSolutionEntries(double[] solution,
			int matrixSize) {
		final SolutionEntry[] res = new SolutionEntry[matrixSize
				* (matrixSize + 1) / 2];
		int k = 0;
		for (int i = 0; i < matrixSize; ++i)
			for (int j = i; j < matrixSize; ++j)
				res[k++] = new SolutionEntry(i, j, solution[i * matrixSize + j]);
		Arrays.sort(res);
		return res;
	}

	private static SolutionEntry[] sortDiagonalEntries(double[] solution,
			int matrixSize) {
		final SolutionEntry[] res = new SolutionEntry[matrixSize];
		int k = 0;
		for (int i = 0; i < matrixSize; ++i)
			res[k++] = new SolutionEntry(i, i, solution[i * (matrixSize + 1)]);
		Arrays.sort(res);
		return res;
	}

	private static class SolutionEntry implements Comparable<SolutionEntry> {
		public final int row;
		public final int col;
		public final double value;

		public SolutionEntry(int row, int col, double value) {
			this.row = row;
			this.col = col;
			this.value = value;
		}

		public int compareTo(SolutionEntry o) {
			return Double.compare(Math.abs(this.value), Math.abs(o.value));
		}
	}

	public static int solveAndMinimiseSdp(int matrixSize, double[] constraints,
			double[] constraintRhs, double[] solution, BitSet removedRows) {
		assert (solution.length == matrixSize * matrixSize);

		removedRows.clear();
		final double[] inpConstraints = makeTriangular(constraints, matrixSize);

		final int res = easiestSDP(matrixSize, inpConstraints, constraintRhs,
				solution, diaGoal(matrixSize));

		if (res != 0 // && res != 3
		)
			return res;

		// we have found a solution, i.e., the optimisation problem is solvable.
		// we now try to simplify the problem and leave out irrelevant
		// parameters, in the hope that this will make the obtained solution
		// more robust

		System.out.println("Big solution: " + Arrays.toString(solution));

		final int constraintNum = constraints.length
				/ (matrixSize * matrixSize);
		final int matrixLength = matrixSize * matrixSize;

		final double solutionNorm = maxAbs(solution);

		// we try to remove rows/cols with small diagonal entries first
		final SolutionEntry[] diaEntries = sortDiagonalEntries(solution,
				matrixSize);

		final BitSet removableConstraints = new BitSet();
		double[] smallSolution = null;
		for (int l = 0; l < matrixSize
				&& removedRows.cardinality() < matrixSize - 1; ++l) {
			final int rowCol = diaEntries[l].col;

			// try to remove this row/column
			removedRows.set(rowCol);

			System.out.println("Trying to remove: " + removedRows);

			final int newMatrixSize = matrixSize - removedRows.cardinality();
			final int newMatrixLength = newMatrixSize * newMatrixSize;
			final int newConstraintNum = constraintNum
					- removableConstraints.cardinality();
			final double[] newConstraints = new double[newMatrixLength
					* newConstraintNum];
			final double[] newConstraintRhs = new double[newConstraintNum];

			for (int inpK = 0, k = 0; inpK < constraintNum; ++inpK) {
				if (removableConstraints.get(inpK))
					continue;

				newConstraintRhs[k] = constraintRhs[inpK];

				for (int i = 0, newI = 0; i < matrixSize; ++i) {
					if (removedRows.get(i))
						continue;

					for (int j = 0, newJ = 0; j < matrixSize; ++j) {
						if (removedRows.get(j))
							continue;
						newConstraints[k * newMatrixLength + newI
								* newMatrixSize + newJ] = inpConstraints[inpK
								* matrixLength + i * matrixSize + j];
						newJ = newJ + 1;
					}

					newI = newI + 1;
				}

				k = k + 1;
			}

			// check whether the problem is still solvable
			final double[] newSolution = new double[newMatrixLength];
			final int res2 = easiestSDP(newMatrixSize, newConstraints,
					newConstraintRhs, newSolution, diaGoal(newMatrixSize));
			if ((res2 == 0 || res2 == res)
					&& maxAbs(newSolution) < 2.0 * solutionNorm) {
				System.out.println("Still solvable");
				// store the small solution
				smallSolution = newSolution;

				// search for removable constraints (because all entries are
				// 0.0)
				for (int inpK = 0, k = 0; inpK < constraintNum; ++inpK) {
					if (removableConstraints.get(inpK))
						continue;

					if (newConstraintRhs[k] == 0.0) {
						int i = 0;
						for (; i < newMatrixLength
								&& newConstraints[k * newMatrixLength + i] == 0.0; ++i)
							;
						if (i == newMatrixLength)
							removableConstraints.set(inpK);
					}

					k = k + 1;
				}
			} else {
				System.out.println("No longer solvable");
				removedRows.clear(rowCol);
			}
		}

		if (removedRows.isEmpty())
			// removal was not successful
			return res;

		System.out
				.println("Maximum set of cols/rows removable: " + removedRows);
		System.out.println("Small solution: " + Arrays.toString(smallSolution));

		System.arraycopy(smallSolution, 0, solution, 0, smallSolution.length);
		Arrays.fill(solution, smallSolution.length, solution.length, 0.0);

		// reconstruct a complete solution by filling up with zeroes
		/*
		 * final int newMatrixSize = matrixSize - removedRows.cardinality();
		 * final int newMatrixLength = newMatrixSizenewMatrixSize;
		 * 
		 * assert smallSolution != null && smallSolution.length ==
		 * newMatrixLength;
		 * 
		 * for (int i = 0, newI = 0; i < matrixSize; ++i) { if
		 * (removedRows.get(i)) { for (int j = 0; j < matrixSize; ++j)
		 * solution[imatrixSize + j] = 0.0; continue; }
		 * 
		 * for (int j = 0, newJ = 0; j < matrixSize; ++j) { if
		 * (removedRows.get(j)) { solution[imatrixSize + j] = 0.0; continue; }
		 * solution[imatrixSize + j] = smallSolution[newInewMatrixSize + newJ];
		 * newJ = newJ + 1; }
		 * 
		 * newI = newI + 1; }
		 */

		return res;
	}

	private static double maxAbs(double[] ar) {
		double res = -1.0;
		for (int i = 0; i < ar.length; ++i)
			res = Math.max(res, Math.abs(ar[i]));
		return res;
	}

	// make sure that the constraints are given as upper triangular
	// matrices
	private static double[] makeTriangular(double[] constraints, int n) {
		int k = constraints.length / (n * n);
		final double[] inpConstraints = constraints.clone();
		for (int i = 0; i < n; ++i)
			for (int j = 0; j < i; ++j)
				for (int l = 0; l < k; ++l)
					inpConstraints[l * n * n + i * n + j] = 0.0;
		return inpConstraints;
	}

	public static int robustSdp(int n, int k, double[] a, double[] constraints,
			double[] solution) {
		int res = sdp(n, constraints, a, solution);

		if (res != 0 && res != 3)
			return res;

		// otherwise, we found a solution and try to make it more robust

		// traces of the constraints
		final double[] traces = new double[k];
		Arrays.fill(traces, 0.0);
		for (int l = 0; l < k; ++l)
			for (int i = 0; i < n; ++i)
				traces[l] = traces[l] + constraints[l * n * n + i * (n + 1)];

		double lambda = 1.0;
		final double[] inpA = new double[k];

		while (lambda > 0.000000001) {
			System.out.println("Robustification: Trying lambda = " + lambda);

			for (int l = 0; l < k; ++l)
				inpA[l] = a[l] - lambda * traces[l];
			final int res2 = sdp(n, constraints, inpA, solution);

			if (res2 != 0 && res2 != 3) {
				lambda = lambda / 2.0;
				continue;
			}

			// otherwise we have found a robust solution, but need to
			// correct it
			System.out.println("Robustification succeeded with lambda = "
					+ lambda);

			for (int i = 0; i < n; ++i)
				solution[i * (n + 1)] = solution[i * (n + 1)] + lambda;

			return res2;
		}

		// no robust solution could be found
		System.out.println("Robustification failed");
		return sdp(n, constraints, a, solution);
	}

	private static final Random random = new Random();

	private static void fillRandomly(double[] ar) {
		for (int i = 0; i < ar.length; ++i)
			ar[i] = random.nextDouble() - 0.5;
	}
}
