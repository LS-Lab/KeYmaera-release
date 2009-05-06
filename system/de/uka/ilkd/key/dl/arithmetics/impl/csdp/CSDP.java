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
     * @param blockSizes
     *            array containing the sizes of the individual blocks of 
     *            the X, C, and Z matrices.
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
    protected static native int easySDP(int[] blockSizes, int k, double[] blockmatrixC,
            double[] a, double[] constraints, double constant_offset,
            double[] blockmatrixpX, double[] py, double[] blockmatrixpZ,
            double[] ppobj, double[] pdobj);

    @SuppressWarnings("unused")
    private static native int test2(int n, int k, double[] blockmatrixC,
            double[] a, double[] constraints, double constant_offset,
            double[] blockmatrixpX, double[] py, double[] blockmatrixpZ,
            double[] ppobj, double[] pdobj);

    private static native int test();
    
    public static void main(String[] args) {
        System.loadLibrary("lapack");
        System.loadLibrary("blas");
        System.loadLibrary("m");
        System.loadLibrary("csdp");
        // System.loadLibrary("sdp");

        test();

        testBigBlock();
        testManyBlocks();
    }

    private static void testBigBlock() {
        int n = 7;

        double[] constraints =
            new double[] { 3, 1, 0, 0, 0, 0, 0, 0, 3, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0,

                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 1, 0, 0,
                0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 1 };
        double[] C = new double[] { 2, 1, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0,
                0, 0, 3, 0, 1, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 1, 0, 3, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        double[] a = new double[] { 1, 2 };
        double[] X = new double[C.length],
                 y = new double[a.length], Z = new double[C.length],
                 pobj = new double[n], dobj = new double[a.length];

        easySDP(new int[] { n }, 2, C, a, constraints, 0, X, y, Z, pobj, dobj);
        System.out.println("X: " + Arrays.toString(X));// XXX
        System.out.println("y: " + Arrays.toString(y));// XXX
        System.out.println("Z: " + Arrays.toString(Z));// XXX
        System.out.println("pobj: " + Arrays.toString(pobj));// XXX
        System.out.println("dobj: " + Arrays.toString(dobj));// XXX
    }

    private static void testManyBlocks() {
        int[] blockSizes = new int[] { 2, 3, 1, 1 };
        double[] constraints =
            new double[] {
                /***** Constraint 1 */
                // block 1
                3, 1, 0, 3,
                // block 2
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                // block 3
                1,
                // block 4
                0,
                /***** Constraint 2 */
                // block 1
                0, 0, 0, 0,
                // block 2
                3, 0, 1, 0, 4, 0, 0, 0, 5,
                // block 3
                0,
                // block 4
                1};
        double[] C =
            new double[] {
                //block 1
                2, 1, 1, 2,
                // block 2
                3, 0, 1, 0, 2, 0, 1, 0, 3,
                // block 3
                0,
                // block 4
                0};
        double[] a = new double[] { 1, 2 };
        double[] X = new double[C.length],
                 y = new double[a.length], Z = new double[C.length],
                 pobj = new double[7],
                 dobj = new double[a.length];

        easySDP(blockSizes, 2, C, a, constraints, 0, X, y, Z, pobj, dobj);
        System.out.println("X: " + Arrays.toString(X));// XXX
        System.out.println("y: " + Arrays.toString(y));// XXX
        System.out.println("Z: " + Arrays.toString(Z));// XXX
        System.out.println("pobj: " + Arrays.toString(pobj));// XXX
        System.out.println("dobj: " + Arrays.toString(dobj));// XXX
    }

}
