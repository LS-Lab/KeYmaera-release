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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import orbital.math.Arithmetic;
import orbital.math.Polynomial;
import orbital.math.Vector;

/**
 * Class to represent polynomials of the form <code>t1 * X^i1 + t2 * X^i2 + ...</code>,
 * where each <code>tj</code> is a linear combination of
 * parameters/variables. The representation of the coefficients is sparse
 * because we can expect a rather large number of parameters.
 */
public class SparsePolynomial {

    private static class CoefficientTerm {
        public final Arithmetic coefficient;
        public final int variable;
        
        public final CoefficientTerm next;

        public CoefficientTerm(Arithmetic coefficient, int variable,
                               CoefficientTerm next) {
            super();
            this.coefficient = coefficient;
            this.variable = variable;
            this.next = next;
        }
    }
    
    /** Mapping from the exponents <code>ij</code> to the coefficient
     *  term <code>tj</code> */
    private final Map<Vector, CoefficientTerm> polyTerms =
        new HashMap<Vector, CoefficientTerm> ();
    
    /**
     * Add the polynomial <code>variable * p</code> to this object
     */
    public void addTerms(Polynomial p, int variable) {
        if (p.isZero())
            return;
        
        final Iterator<Vector> expIt = p.indices();
        final Iterator<Arithmetic> coeffIt = p.iterator();
        while (expIt.hasNext()) {
            final Vector exponent = expIt.next();
            final Arithmetic coeff = coeffIt.next();
            
            if (coeff.isZero())
                continue;
            
            final CoefficientTerm newCoeffTerm =
                new CoefficientTerm(coeff, variable, polyTerms.get(exponent));
            polyTerms.put(exponent, newCoeffTerm);
        }
    }

    /**
     * Generate a system of linear constraints (over the parameters in the
     * polynomial coefficients) that describes that all polynomial coefficients
     * are zero. It is assumed that the parameter indexes denote positions in
     * a symmetric matrix:
     * 
     *    /  0  1  3  ....
     *    |  1  2  4
     *    |  3  4  5  ....
     *    |  .......
     *    
     * The constraint generated for the constant term of the polynomial is
     * always put in the first place of the resulting array.
     */
    public double[] coefficientComparison(int matrixSize) {
        final int matrixLength = matrixSize * matrixSize;
        final double[] res = new double[matrixLength * polyTerms.size()];
        Arrays.fill(res, 0.0);
        
        int currentOffset = matrixLength;
        for (Entry<Vector, CoefficientTerm> entry : polyTerms.entrySet()) {
            if (entry.getKey().isZero()) {
                // the constant term is always output first
                copy2Array(entry.getValue(), res, matrixSize, 0);
            } else {
                assert (currentOffset < res.length) :
                    "It was (wrongly) assumed that polynomials always have a constant term";
                copy2Array(entry.getValue(), res, matrixSize, currentOffset);
                currentOffset = currentOffset + matrixLength;
            }
        }
        
        return res;
    }

    private void copy2Array(CoefficientTerm term, final double[] coeffArray,
                            int matrixSize, int currentOffset) {
        while (term != null) {
            assert (term.variable >= 0 && term.variable < matrixSize * matrixSize);
            
            // HACK
            final double val = Double.parseDouble(term.coefficient.toString());
            final int col = column(term.variable);
            final int row = row(term.variable, col);
            
            coeffArray[currentOffset + row * matrixSize + col] = val;
            coeffArray[currentOffset + col * matrixSize + row] = val;
            
            term = term.next;
        }
    }
    
    // Compute the column and row number, given a variable index (within the
    // upper half of the matrix)
    
    private int column(int variable) {
        int col = 0;
        int maxVar = 0;
        while (maxVar < variable) {
            col = col + 1;
            maxVar = maxVar + col + 1;
        }
        return col;
    }
    
    private int row(int variable, int column) {
        return variable - (column * (column + 1)) / 2;
    }
    
    public int size() {
        return polyTerms.size();
    }
    
    public String toString() {
        final StringBuffer res = new StringBuffer ();
        for (Entry<Vector, CoefficientTerm> entry : polyTerms.entrySet()) {
            res.append(entry.getKey());
            res.append(": ");
            CoefficientTerm term = entry.getValue();
            while (term != null) {
                res.append("" + term.coefficient + "*q" + term.variable);
                if (term.next != null)
                    res.append(" + ");
                term = term.next;
            }
            res.append("\n");
        }
        return res.toString();
    }
}