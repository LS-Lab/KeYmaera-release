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

import java.util.Arrays;

import orbital.math.Arithmetic;
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
        assert (eqCoefficients.dimensions()[1] == approxSolution.length) : "Dimensions dont fit: " + eqCoefficients.dimensions()[1] + " != " + approxSolution.length;
        
        this.eqCoefficients = eqCoefficients;
        this.eqHeteros = eqHeteros;

        System.out.println("Precision of approx solution: " +
                           eqCoefficients.multiply(Values.getDefault().valueOf(approxSolution)));
        
        final ApproxSolutionEntry[] solEntries =
            new ApproxSolutionEntry [approxSolution.length];
        for (int i = 0; i < approxSolution.length; ++i)
            solEntries[i] = new ApproxSolutionEntry (approxSolution[i], i, eps);
        
        Arrays.sort(solEntries);
        
        final Matrix permCoefficients =
            (Matrix)ValuesImpl.getDefault().newInstance(eqCoefficients.dimensions()[0],
                                                        eqCoefficients.dimensions()[1] + 1);
        for (int i = 0; i < solEntries.length; ++i)
            permCoefficients.setColumn(i, eqCoefficients.getColumn(solEntries[i].variable));
        permCoefficients.setColumn(eqCoefficients.dimensions()[1], eqHeteros);
        
        // solve the equations using the Gaussian algorithm
        echelon(permCoefficients);
        
        // read off the solution of the equations
        final Vector permSolution = solveEchelonMatrix(permCoefficients, solEntries);
        
        // undo the permutation of the solution entries
        final Vector exactSolution = Values.getDefault().ZERO(permSolution.dimension());
        for (int i = 0; i < solEntries.length; ++i)
            exactSolution.set(solEntries[i].variable, permSolution.get(i));
        
        this.exactSolution = exactSolution;
    }

    
    /**
     * Read of a solution from a matrix in row echelon form
     */
    private static Vector solveEchelonMatrix(Matrix m,
                                             ApproxSolutionEntry[] approxSolutions) {
        final int height = m.dimensions()[0];
        final int width = m.dimensions()[1];

        assert (approxSolutions.length == width - 1);

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
                // we "patch" the solution using the approximate solution
                res.set(col, approxSolutions[col].value.asFraction());
                col = col - 1;
            }

            Arithmetic val = m.get(row, width - 1);
            for (int j = col + 1; j < width - 1; ++j)
                val = val.subtract(res.get(j).multiply(m.get(row, j)));
            val = val.divide(m.get(row, col));
            
            res.set(col, val);
            
            col = col - 1;
            row = row - 1;
        }
        
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

            final Arithmetic pivot = m.get(row, col);

            // simplify the other rows using the row with the pivot element
            i = i + 1;
            while (i < height) {
                if (!m.get(i, col).isZero()) {
                    final Arithmetic factor = m.get(i, col).divide(pivot);

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
