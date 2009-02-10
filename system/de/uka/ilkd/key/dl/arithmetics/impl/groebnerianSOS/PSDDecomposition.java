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

import de.uka.ilkd.key.dl.arithmetics.impl.orbital.OrbitalSimplifier;
import orbital.math.Arithmetic;
import orbital.math.Matrix;
import orbital.math.Values;
import orbital.math.functional.Operations;

/**
 * Class for decomposing a symmetric positive semi-definite matrix
 * <code>M</code> into a product <code>T^t D T</code>, where <code>T</code> is
 * an upper triangular matrix and <code>D</code> is a diagonal matrix that only
 * has non-negative entries
 */
public class PSDDecomposition {

    public final Matrix T;
    public final Matrix D;
    
    /**
     * @param M has to be symmetric
     */
    public PSDDecomposition(Matrix M) throws NotPSDException {
        assert (M.isSymmetric());        
        final int width = M.dimensions()[0];
        final Arithmetic zero = Values.getDefault().ZERO();
        final Arithmetic one = Values.getDefault().ONE();
        
        // first clone the input matrix; we only work on the upper half of the
        // matrix
        final Matrix inp = Values.getDefault().newInstance(1, width);
        inp.insertRows(M);
        inp.removeRow(0);
        
        // resulting matrices
        final Matrix T = Values.getDefault().ZERO(width, width);
        final Matrix D = Values.getDefault().ZERO(width, width);

        try {
            for (int i = 0; i < width; ++i) {
                System.out.print('.');
//                System.out.println(inp);
//                System.out.println();
                final Arithmetic diagElem = inp.get(i, i);

                if (Operations.less.apply(diagElem, zero))
                    throw new NotPSDException("diagonal element is " +
                                              OrbitalSimplifier.toDouble(diagElem));

                if (diagElem.isZero()) {
                    for (int j = i + 1; j < width; ++j)
                        if (!inp.get(i, j).isZero())
                            throw new NotPSDException("matrix element is " +
                                                      OrbitalSimplifier.toDouble(inp.get(i, j)));
                } else {
                    D.set(i, i, diagElem);
                    T.set(i, i, one);
                    for (int j = i + 1; j < width; ++j) {
                        final Arithmetic a_i_j = inp.get(i, j);
                        final Arithmetic newCoeff = a_i_j.divide(diagElem);

                        T.set(i, j, newCoeff);

                        if (!newCoeff.isZero()) {
                            for (int k = i + 1; k <= j; ++k) {
                                final Arithmetic summand = newCoeff
                                        .multiply(inp.get(i, k));
                                inp.set(k, j, inp.get(k, j).subtract(summand));
                            }
                        }
                    }
                }
            }
        } finally {
            System.out.println();
        }
        
        this.T = T;
        this.D = D;
        
        assert (T.transpose().multiply(D).multiply(T).equals(M));
    }
    
    public static final class NotPSDException extends Exception {
        public NotPSDException(String msg) {
            super("Matrix is not positive semi-definite: " + msg);
        }
    }
    
}
