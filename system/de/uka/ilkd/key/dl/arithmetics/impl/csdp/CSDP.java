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

import java.util.Arrays;
import java.util.Random;

/**
 * @author jdq
 * 
 */
public class CSDP {
	static {
		System.loadLibrary("csdp");
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
	 * @return <ul>
	 *         <li>0: Success. Problem is solvable
	 *         <li>1: Success. The problem is primal infeasible.
	 *         <li>2: Success. The problem is dual infeasible.
	 *         <li>3: Partial Success: A solution has been found, but full
	 *         accuracy was not achieved. One or more of primal infeasibility,
	 *         dual infeasibility, or relative duality gap are larger than their
	 *         tolerances, but by a factor of less than 1000.
	 *         <li>4: Failure. Maximum iterations reached.
	 *         <li>5: Failure. Stuck at edge of primal feasibility.
	 *         <li>6: Failure. Stuck at edge of dual infeasibility.
	 *         <li>7: Failure. Lack of progress.
	 *         <li>8: Failure. X, Z, or O was singular.
	 *         <li>9: Failure. Detected NaN or Inf values.
	 *         </ul>
	 */
	private static native int easySDP(int n, int k, double[] blockmatrixC,
			double[] a, double[] constraints, double constant_offset,
			double[] blockmatrixpX, double[] py, double[] blockmatrixpZ,
			double[] ppobj, double[] pdobj);

	@SuppressWarnings("unused")
	private static native int test2(int n, int k, double[] blockmatrixC,
			double[] a, double[] constraints, double constant_offset,
			double[] blockmatrixpX, double[] py, double[] blockmatrixpZ,
			double[] ppobj, double[] pdobj);

	private static native int test();

	public static boolean sdp(int n, int k, double[] a, double[] constraints) {
		double[] X = new double[n * n], y = new double[a.length], Z = new double[n
				* n], pobj = new double[n], dobj = new double[a.length];
//		System.out.println("n is: " + n);// XXX
//		System.out.println("a is: " + Arrays.toString(a));// XXX
//		System.out.println("constraints is: " + Arrays.toString(constraints));// XXX
		int result = easySDP(n, k, new double[n * n], a, constraints, 0, X, y,
				Z, pobj, dobj);
		// TODO: check that X is really an PSD matrix and that it yields a
		// solution when using exact arithmetics

		// check can either be checked by cholesky decomposition or by harrissions algorithm 
		
		
		// TODO: guess fractions for those doubles, as they may be caused by
		// rounding errors
		return result == 0;
	}

        public static int sdp(int n, int k, double[] a, double[] constraints, double[] solution) {
            assert (solution.length == n*n);
            double[] y = new double[a.length], Z = new double[n
                            * n], pobj = new double[n], dobj = new double[a.length];
            final double[] C = new double[n * n];
            
            // make sure that the constraints are given as upper triangular
            // matrices
            final double[] inpConstraints = constraints.clone();
            for (int i = 0; i < n; ++i)
                for (int j = 0; j < i; ++j)
                    for (int l = 0; l < k; ++l)
                        inpConstraints[l*n*n + i*n + j] = 0.0;
            
            Arrays.fill(solution, 0.1);
            Arrays.fill(y, 0.1);
            Arrays.fill(Z, 0.1);

//            fillRandomly(solution);
//            fillRandomly(y);
//            fillRandomly(Z);
//            fillRandomly(C);
//            Arrays.fill(C, -1.0); // try to find solutions with small values
            
//          System.out.println("n is: " + n);// XXX
//          System.out.println("a is: " + Arrays.toString(a));// XXX
//          System.out.println("constraints is: " + Arrays.toString(constraints));// XXX
            return easySDP(n, k, C, a, inpConstraints, 0, solution, y, Z, pobj, dobj);
        }

        public static int robustSdp(int n, int k, double[] a, double[] constraints,
                                    double[] solution) {
            int res = sdp(n, k, a, constraints, solution);
            
            if (res != 0 && res != 3)
                return res;
            
            // otherwise, we found a solution and try to make it more robust
            
            // traces of the constraints
            final double[] traces = new double[k];
            Arrays.fill(traces, 0.0);
            for (int l = 0; l < k; ++l)
                for (int i = 0; i < n; ++i)
                    traces[l] = traces[l] + constraints[l*n*n + i*(n+1)];
            
            double lambda = 1.0;
            final double[] inpA = new double[k];
            
            while (lambda > 0.000000001) {
                System.out.println("Robustification: Trying lambda = " + lambda);
                
                for (int l = 0; l < k; ++l)
                    inpA[l] = a[l] - lambda*traces[l];
                final int res2 = sdp(n, k, inpA, constraints, solution);
                
                if (res2 != 0 && res2 != 3) {
                    lambda = lambda / 2.0;
                    continue;
                }
                
                // otherwise we have found a robust solution, but need to
                // correct it
                System.out.println("Robustification succeeded with lambda = " + lambda);
                
                for (int i = 0; i < n; ++i)
                    solution[i*(n+1)] = solution[i*(n+1)] + lambda;
                
                return res2;
            }
            
            // no robust solution could be found
            System.out.println("Robustification failed");
            return sdp(n, k, a, constraints, solution);
        }
            
        private static final Random random = new Random ();
        
        private static void fillRandomly(double[] ar) {
            for (int i = 0; i < ar.length; ++i)
                ar[i] = random.nextDouble() - 0.5;
        }
        
	public static void main(String[] args) {
		System.loadLibrary("lapack");
		System.loadLibrary("blas");
		System.loadLibrary("m");
		System.loadLibrary("csdp");
		// System.loadLibrary("sdp");

		test();

		int n = 7;

		double[] constraints = new double[] { 3, 1, 0, 0, 0, 0, 0, 1, 3, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0,

				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 1, 0, 0,
				0, 0, 0, 4, 0, 0, 0, 0, 0, 1, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 1 };
		double[] C = new double[] { 2, 1, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0,
				0, 0, 3, 0, 1, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 1, 0, 3, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		double[] a = new double[] { 1, 2 };
		double[] X = new double[C.length], y = new double[a.length], Z = new double[C.length], pobj = new double[n], dobj = new double[a.length];

		easySDP(7, 2, convertToFortranForm(C, 7), a, constraints, 0, X, y, Z,
				pobj, dobj);
		System.out.println("X: " + Arrays.toString(X));// XXX
		System.out.println("y: " + Arrays.toString(y));// XXX
		System.out.println("Z: " + Arrays.toString(Z));// XXX
		System.out.println("pobj: " + Arrays.toString(pobj));// XXX
		System.out.println("dobj: " + Arrays.toString(dobj));// XXX
	}

	private static double[] convertToFortranForm(double[] array, int dim) {
		double[][] tmp = new double[dim][dim];
		double[] result = new double[array.length];
		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				tmp[i][j] = array[j + i * dim];
				System.out.println("Setting pos (" + i + ", " + j + ")"
						+ " to value " + array[j + i * dim]);// XXX
			}
		}

		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				result[j + dim * i] = tmp[j][i];
			}
		}
		return result;
	}

}
