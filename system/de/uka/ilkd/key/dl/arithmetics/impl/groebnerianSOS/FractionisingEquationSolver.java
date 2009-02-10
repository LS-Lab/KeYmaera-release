/***************************************************************************
 *   Copyright (C) 2009 by Philipp Ruemmer                                 *
 *   philipp@chalmers.se                                                   *
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
package de.uka.ilkd.key.dl.arithmetics.impl.groebnerianSOS;

import java.util.ArrayList;
import java.util.List;

import orbital.math.Arithmetic;
import orbital.math.Integer;
import orbital.math.Matrix;
import orbital.math.Values;
import orbital.math.Vector;
import orbital.moon.math.ValuesImpl;
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.OrbitalSimplifier;

/**
 * Class for precisely solving systems of linear equations, given an approximate
 * solution that consists of doubles.
 */
public class FractionisingEquationSolver {

    public final Matrix eqCoefficients;
    public final Vector eqHeteros;
    public final Vector exactSolution;
    
    public FractionisingEquationSolver(Matrix eqCoefficients,
                                       Vector eqHeteros,
                                       double[] approxSolution,
                                       double eps) {
        this.eqCoefficients = eqCoefficients;
        this.eqHeteros = eqHeteros;

        System.out.println("Accuracy of approx solution: " + eqCoefficients.multiply(Values.getDefault().valueOf(approxSolution)));
        
        // represent the approximation solution as fractions, up to the
        // requested precision
        final Vector fractionedApproxSol =
            Values.getDefault().newInstance(approxSolution.length);
        for (int i = 0; i < approxSolution.length; ++i)
            fractionedApproxSol.set(i, new Fractionised (approxSolution[i], eps).asFraction());

        // build the system of equations
        final Matrix system =
            (Matrix)ValuesImpl.getDefault().newInstance(eqCoefficients.dimensions()[0], 1);
        system.insertColumns(0, eqCoefficients);
        system.setColumn(eqCoefficients.dimensions()[1], eqHeteros);

//        System.out.println("System:\n" + system);

        // solve the equations using the Gaussian algorithm
        echelon(system);
        jordan(system);

//        System.out.println("Solved system:\n" + system);

        final Vector particularSol = particularSolution(system);
        final Matrix solutionSpace = homoSolutionSpace(system);
        
//        System.out.println("Particular:\n" + particularSol);
//        System.out.println("Space:\n" + solutionSpace);
        
        // use the least-square-sum method to find the solution that is closest
        // to the approximate solution
        final Matrix solutionSpaceT = solutionSpace.transpose();
        final Matrix leastSquareSystem = solutionSpaceT.multiply(solutionSpace);
        
        final Vector leastSquareLHS =
            solutionSpaceT.multiply(fractionedApproxSol.subtract(particularSol));
        
        // HACK
        leastSquareSystem.insertColumns(Values.getDefault().newInstance(leastSquareLHS.dimension(), 1));
        leastSquareSystem.setColumn(leastSquareSystem.dimensions()[1] - 1,
                                    leastSquareLHS);
        
        echelon(leastSquareSystem);
        
        final Vector perfectOffset =
            solutionSpace.multiply(particularSolution(leastSquareSystem));
        this.exactSolution = particularSol.add(perfectOffset);
    }

    
    /**
     * Read of an arbitrary particular solution from a matrix in echelon form
     * (it is assumed that the last column of the matrix is the RHS of a
     * system of equations)
     */
    private static Vector particularSolution(Matrix m) {
        final int height = m.dimensions()[0];
        final int width = m.dimensions()[1];

        final Vector res = Values.getDefault().ZERO(width - 1);
        
        int row = height - 1;
        int col = width - 2;

        while (row >= 0 && col >= 0) {
            // find the left-most column with a non-zero entry in the current row
            int i = 0;
            while (i < width && m.get(row, i).isZero())
                i = i + 1;

            if (i == width) {
                // trivial row, go to the next one
                row = row - 1;
                continue;
            } else if (i == width - 1) {
                // this system of equations is unsolvable
                throw new IllegalArgumentException
                          ("Tried to solve unsolvable system of equations");
            }
            
            assert (i <= col);
            while (i < col) {
                // these solution components can be chosen arbitrarily, we just
                // take zero
                res.set(col, Values.getDefault().ZERO());
                col = col - 1;
            }
            
            assert (m.get(row, col).isOne());

            Arithmetic val = m.get(row, width - 1);
            for (int j = col + 1; j < width - 1; ++j)
                val = val.subtract(res.get(j).multiply(m.get(row, j)));
            
            res.set(col, val);
            
            col = col - 1;
            row = row - 1;
        }
        
        return res;        
    }
    
    
    /**
     * Read of the space of homogeneous solutions of a system of linear
     * equations that is described by a matrix in echelon form (it is assumed
     * that the last column of the matrix is the RHS of the equations). The
     * columns of the returned matrix are the vectors generating the solution
     * space.
     */
    private static Matrix homoSolutionSpace(Matrix m) {
        final Integer minus_one = Values.getDefault().valueOf(-1);

        final List<java.lang.Integer> zeroRows =
            new ArrayList<java.lang.Integer> ();
        
        final int height = m.dimensions()[0];
        final int width = m.dimensions()[1];

        final Matrix res = Values.getDefault().ZERO(width - 1, 1);
        
        int row = 0;
        int col = 0;
        
        while (row <= height && col < width - 1) {
            while (col < width - 1 && (row == height || m.get(row, col).isZero())) {
                // add a further vector to the solution matrix
                final Matrix solVec = Values.getDefault().ZERO(width - 1, 1);
                
                int vecPos = 0;
                int mPos = 0;
                int zeroRowsPos = 0;
                while (mPos < row) {
                    if (zeroRowsPos < zeroRows.size() &&
                        zeroRows.get(zeroRowsPos).equals(mPos)) {
                        zeroRowsPos = zeroRowsPos + 1;
                    } else {
                        solVec.set(vecPos, 0, m.get(mPos, col));
                        mPos = mPos + 1;
                    }
                    vecPos = vecPos + 1;
                }
                
                vecPos = vecPos + zeroRows.size() - zeroRowsPos;
                
                solVec.set(vecPos, 0, minus_one);
                res.insertColumns(solVec);
                
                zeroRows.add(row);
                col = col + 1;
            }
            
            assert (col == width - 1 || m.get(row, col).isOne());
            
            row = row + 1;
            col = col + 1;
        }
        
        // because Orbital does not like empty matrices, we return a matrix with
        // the zero-vector in case the solution space only contains a single
        // element
        if (res.dimensions()[1] > 1)
            res.removeColumn(0);
        
        return res;
    }
    
    
    /**
     * Turn a matrix into row echelon form
     */
    private static void echelon(Matrix m) {
        final int height = m.dimensions()[0];
        final int width = m.dimensions()[1];
        
        int row = 0;
        int col = 0;

        while (row < height && col < width) {
            // search for the first non-zero element in the current column
            int i = row;
            while (i < height && m.get(i, col).isZero())
                i = i + 1;

            if (i == height) {
                // there is no pivot element in the current column, we try the
                // next one
                col = col + 1;
                continue;
            } else if (i != row) {
                // swap the current row with the row with the pivot element
                for (int j = col; j < width; ++j) {
                    final Arithmetic t = m.get(row, j);
                    m.set(row, j, m.get(i, j));
                    m.set(i, j, t);
                }
            }

            // turn the pivot element of this row into a one
            final Arithmetic pivot = m.get(row, col);
            if (!pivot.isOne()) {
                for (int j = col; j < width; ++j)
                    m.set(row, j, m.get(row, j).divide(pivot));
            }

            // simplify the other rows using the row with the pivot element
            i = i + 1;
            while (i < height) {
                if (!m.get(i, col).isZero()) {
                    final Arithmetic factor = m.get(i, col);

                    for (int j = col; j < width; ++j) {
                        final Arithmetic newEl =
                            m.get(i, j).subtract(m.get(row, j).multiply(factor));
                        m.set(i, j, newEl);
                    }
                }
                
                i = i + 1;
            }
            
            row = row + 1;
            col = col + 1;
        }
    }

    
    /**
     * Clean up a matrix in row echelon form
     * 
     * TODO: better name for the method
     */
    private static void jordan(Matrix m) {
        final int height = m.dimensions()[0];
        final int width = m.dimensions()[1];

        int row = height - 1;

        while (row >= 0) {
            // find the left-most column with a non-zero entry in the current row
            int col = 0;
            while (col < width && m.get(row, col).isZero())
                col = col + 1;

            if (col == width) {
                // trivial row, go to the next one
                row = row - 1;
                continue;
            }
            
            assert (m.get(row, col).isOne());
            
            // simplify this column by subtracting multiplies of this row from
            // other rows
            int i = row - 1;
            while (i >= 0) {
                if (!m.get(i, col).isZero()) {
                    final Arithmetic factor = m.get(i, col);

                    for (int j = col; j < width; ++j) {
                        final Arithmetic newEl =
                            m.get(i, j).subtract(m.get(row, j).multiply(factor));
                        m.set(i, j, newEl);
                    }
                }
                i = i - 1;
            }
            
            row = row - 1;
        }
    }
    
    
    /**
     * Helper class to manage the fractionised components of a floating-point
     * solution returned by the PSD programming procedure
     */
    private static class ApproxSolutionEntry implements Comparable<ApproxSolutionEntry> {
        public final Fractionised value;
        public final int variable;

        public ApproxSolutionEntry(double val, int variable, double eps) {
            this.value = new Fractionised (val, eps);
            this.variable = variable;
            assert (this.value.denominator.compareTo(this.value.denominator.zero()) > 0);
        }

        public int compareTo(ApproxSolutionEntry o) {
            return Double.compare(this.precision(), o.precision());
        }
        
        private double precision() {
            return Math.abs(value.fp) / OrbitalSimplifier.toDouble(value.denominator);
        }

        public String toString() {
            return "" + variable + ": " + value;
        }
    }
}
