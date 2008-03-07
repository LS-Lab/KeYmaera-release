/***************************************************************************
 *   Copyright (C) 2007-2008 by Jan-David Quesel                                *
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

import de.uka.ilkd.key.dl.arithmetics.impl.csdp.Blockmat.blockmatrix;
import de.uka.ilkd.key.dl.arithmetics.impl.csdp.Blockmat.constraintmatrix;

/**
 * @author jdq
 * 
 */
public class CSDP {
	static {
		System.loadLibrary("csdp.so");
	}

	/**
	 * Input Parameters:
	 * 
	 * @param n
	 *            gives the dimension of the X, C, and Z matrices.
	 * @param k
	 *            gives the number of constraints.
	 * @param blockmatrixC
	 *            gives the C matrix and implicitly defines the block structure
	 *            of the block diagonal matrices.
	 * @param a
	 *            gives the right hand side vector a.
	 * @param constraints
	 *            specifies the problem constraints.
	 * @param constant_offset
	 *            This scalar is added to the primal and dual objective values.
	 * 
	 * 
	 * @param blockmatrixpX
	 *            On input, this parameter gives the initial primal solution X.
	 *            On output, it gives the optimal primal solution X
	 * @param py
	 *            On input, this parameter gives the initial dual solution y. On
	 *            output, it gives the optimal dual solution y
	 * @param blockmatrixpZ
	 *            On input, this parameter gives the initial dual solution Z. On
	 *            output, it gives the optimal dual solution Z
	 * @param ppobj
	 *            gives the optimal primal objective value
	 * @param pdobj
	 *            gives the optimal dual objective value
	 * @return
	 *            <ul>
	 *            <li> 0: Success. Problem is solvable
	 *            <li> 1: Success. The problem is primal infeasible.
	 *            <li> 2: Success. The problem is dual infeasible.
	 *            <li> 3: Partial Success: A solution has been found, but full
	 *            accuracy was not achieved. One or more of primal
	 *            infeasibility, dual infeasibility, or relative duality gap are
	 *            larger than their tolerances, but by a factor of less than
	 *            1000.
	 *            <li> 4: Failure. Maximum iterations reached.
	 *            <li> 5: Failure. Stuck at edge of primal feasibility.
	 *            <li> 6: Failure. Stuck at edge of dual infeasibility.
	 *            <li> 7: Failure. Lack of progress.
	 *            <li> 8: Failure. X, Z, or O was singular.
	 *            <li> 9: Failure. Detected NaN or Inf values.
	 *            </ul>
	 */
	private static native int easy_sdp(int n, int k, int[][] blockmatrixC,
			double[] a, int[][] constraints, double constant_offset,
			int[][] blockmatrixpX, double[][] py, int[][] blockmatrixpZ,
			double[] ppobj, double[] pdobj);

	/**
	 * Input Parameters:
	 * 
	 * @param n
	 *            gives the dimension of the X, C, and Z matrices.
	 * @param k
	 *            gives the number of constraints.
	 * @param blockmatrixC
	 *            gives the C matrix and implicitly defines the block structure
	 *            of the block diagonal matrices.
	 * @param a
	 *            gives the right hand side vector a.
	 * @param constraints
	 *            specifies the problem constraints.
	 * @param constant_offset
	 *            This scalar is added to the primal and dual objective values.
	 * 
	 * 
	 * @param blockmatrixpX
	 *            On input, this parameter gives the initial primal solution X.
	 *            On output, it gives the optimal primal solution X
	 * @param py
	 *            On input, this parameter gives the initial dual solution y. On
	 *            output, it gives the optimal dual solution y
	 * @param blockmatrixpZ
	 *            On input, this parameter gives the initial dual solution Z. On
	 *            output, it gives the optimal dual solution Z
	 * @param ppobj
	 *            gives the optimal primal objective value
	 * @param pdobj
	 *            gives the optimal dual objective value
	 * @return
	 *            <ul>
	 *            <li> 0: Success. Problem is solvable
	 *            <li> 1: Success. The problem is primal infeasible.
	 *            <li> 2: Success. The problem is dual infeasible.
	 *            <li> 3: Partial Success: A solution has been found, but full
	 *            accuracy was not achieved. One or more of primal
	 *            infeasibility, dual infeasibility, or relative duality gap are
	 *            larger than their tolerances, but by a factor of less than
	 *            1000.
	 *            <li> 4: Failure. Maximum iterations reached.
	 *            <li> 5: Failure. Stuck at edge of primal feasibility.
	 *            <li> 6: Failure. Stuck at edge of dual infeasibility.
	 *            <li> 7: Failure. Lack of progress.
	 *            <li> 8: Failure. X, Z, or O was singular.
	 *            <li> 9: Failure. Detected NaN or Inf values.
	 *            </ul>
	 */
	public static int easy_sdpJava(int n, int k, blockmatrix C, double[] a,
			constraintmatrix constraints, double constant_offset,
			blockmatrix pX, double[][] py, blockmatrix pZ, double[] ppobj,
			double[] pdobj) {
		return easy_sdp(0, 0, null, null, null, 0, null, null, null, null, null);
	}
}
