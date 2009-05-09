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

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import orbital.math.AlgebraicAlgorithms;
import orbital.math.Arithmetic;
import orbital.math.Matrix;
import orbital.math.Polynomial;
import orbital.math.Values;
import orbital.math.Vector;
import orbital.util.KeyValuePair;
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.OrbitalSimplifier;

/**
 * Class to represent polynomials of the form
 * <code>t1 * X^i1 + t2 * X^i2 + ...</code>, where each <code>tj</code> is a
 * linear combination of parameters/variables. The representation of the
 * coefficients is sparse because we can expect a rather large number of
 * parameters.
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

	/**
	 * Mapping from the exponents <code>ij</code> to the coefficient term
	 * <code>tj</code>
	 */
	private final Map<Arithmetic, CoefficientTerm> polyTerms =
	    new TreeMap<Arithmetic, CoefficientTerm>(AlgebraicAlgorithms.DEGREE_REVERSE_LEXICOGRAPHIC);

	private void addTerm(Arithmetic exp, int variable, Arithmetic coeff) {
	    if (coeff.isZero())
	        return;
	    
	    // check whether we already have an entry for this variable
	    boolean present = false;
            CoefficientTerm term = polyTerms.get(exp);
	    while (term != null && !present) {
	        if (term.variable == variable)
	            present = true;
	        term = term.next;
	    }
	    
	    if (present) {
	        // we must update this entry
	        CoefficientTerm oldTerm = polyTerms.get(exp);
	        CoefficientTerm newTerm = null;
	    
	        while (oldTerm != null) {
	            if (oldTerm.variable == variable)
	                newTerm = new CoefficientTerm (oldTerm.coefficient.add(coeff),
	                                               variable, newTerm);
	            else
                        newTerm = new CoefficientTerm (oldTerm.coefficient,
                                                       oldTerm.variable, newTerm);
	            
	            oldTerm = oldTerm.next;
	        }
	        
	        polyTerms.put(exp, newTerm);
            } else {
                // we can just add a new entry
                final CoefficientTerm newTerm =
                    new CoefficientTerm(coeff, variable, polyTerms.get(exp));
                polyTerms.put(exp, newTerm);
            }
	}
	
	/**
	 * Add the polynomial <code>variable * p</code> to this object
	 */
	public void addTerms(Polynomial p, int variable) {
		if (p.isZero())
			return;

		final Iterator<KeyValuePair> monomialIt = p.monomials();
		while (monomialIt.hasNext()) {
			KeyValuePair nextMono = monomialIt.next();
			final Arithmetic v = (Arithmetic) nextMono.getKey();
			final Arithmetic coeff = (Arithmetic) nextMono.getValue();

			addTerm(v, variable, coeff);
		}
	}

	/**
	 * Generate a system of linear constraints (over the parameters in the
	 * polynomial coefficients) that describes that all polynomial coefficients
	 * are zero. It is assumed that the parameter indexes denote positions in a
	 * symmetric matrix:
	 * 
	 * <pre>
	 *    /  0  1  3  ....
	 *    |  1  2  4
	 *    |  3  4  5  ....
	 *    |  .......
	 * </pre>
	 * 
	 * The constraint generated for the constant term of the polynomial is
	 * always put in the first place of the resulting array.
	 */
	public double[] coefficientComparison(int matrixSize) {
	    return toDoubleArray(exactCoefficientComparison(matrixSize, new BitSet ()));
	}
	
	public static double[] toDoubleArray(Matrix m) {
	    final int height = m.dimensions()[0];
            final int width = m.dimensions()[1];
            final double[] res = new double [height * width];
            for (int i = 0; i < height; ++i)
                for (int j = 0; j < width; ++j)
                    res[i*width + j] = OrbitalSimplifier.toDouble(m.get(i, j));
            return res;
	}

	/**
	 * Generate a system of linear constraints (over the parameters in the
	 * polynomial coefficients) that describes that all polynomial coefficients
	 * are zero. It is assumed that the parameter indexes denote positions in a
	 * symmetric matrix Q (of size <code>matrixSize</code>):
	 * 
	 * <pre>
	 *    /  0  1  3  ....
	 *    |  1  2  4
	 *    |  3  4  5  ....
	 *    |  .......
	 * </pre>
	 * 
	 * In order to represent the constraints, this matrix is linearised to a
	 * vector <code>Q' = ( 0  1  3  ...  1  2  4  ...  3  4  5 .... ) ^t</code>.
	 * Each constraint is then a (row) vector of the same
	 * length, and the system of all constraints is a matrix with all the row
	 * vectors.
	 * 
	 * The constraint generated for the constant term of the polynomial is
	 * always put in the first row of the matrix.
	 * 
	 * Using the parameter <code>removedRows</code>, it is possible to
	 * specify that certain rows/columns of the Q-matrix are supposed to be
	 * suppressed
	 */
	public Matrix exactCoefficientComparison(int matrixSize,
	                                         BitSet removedRows) {
	    final int smallMatrixSize = matrixSize - removedRows.cardinality();
	    final int matrixLength = smallMatrixSize * smallMatrixSize;
		final Matrix res =
		    Values.getDefault().ZERO(polyTerms.size(), matrixLength);

		// mapping from rows/columns of the complete Q-matrix to the
		// rows/columns of the reduced Q-matrix
		final int[] conversionMapping = new int [matrixSize];
		for (int i = 0, j = 0; i < matrixSize; ++i)
		    if (removedRows.get(i)) {
		        conversionMapping[i] = -1;
		    } else {
		        conversionMapping[i] = j;
		        j = j + 1;
		    }
		
		int row = 1;
		for (Entry<Arithmetic, CoefficientTerm> entry : polyTerms.entrySet()) {
			if (entry.getKey().isZero()) {
				// the constant term is always put in the first row
				exactCopy2Array(entry.getValue(), res, smallMatrixSize,
				                conversionMapping, 0);
			} else {
				assert (row < polyTerms.size()) : "It was (wrongly) assumed that polynomials always have a constant term";
				exactCopy2Array(entry.getValue(), res, smallMatrixSize,
				                conversionMapping, row);
				row = row + 1;
			}
		}

		return res;
	}

        public Matrix exactCoefficientComparisonBlock(int[] blockSizes,
                                                      Collection<Arithmetic> monomials) {
            final int matrixLength = blockOffset(blockSizes.length, blockSizes);
            final Matrix res =
                Values.getDefault().ZERO(polyTerms.size(), matrixLength);

            int row = 0;
            for (Arithmetic mono : monomials) {
                final CoefficientTerm term = polyTerms.get(mono);
                exactCopy2BlockArray(term, res, blockSizes, row);
                row = row + 1;
            }
            
            return res;
        }

	private void exactCopy2Array(CoefficientTerm term, Matrix m,
			             int matrixSize,
			             int[] conversionMapping,
			             int mRow) {
	    while (term != null) {
	        final int col = column(term.variable);
	        final int row = row(term.variable, col);

	        final int ncol = conversionMapping[col];
                final int nrow = conversionMapping[row];
	        
	        if (ncol >= 0 && nrow >= 0) {
	            final Arithmetic val;
	            if (ncol == nrow)
	                val = term.coefficient;
	            else
	                // because such parameters occur twice in the matrix, the
	                // coefficients have to be divided by 2
	                val = term.coefficient.divide(Values.getDefault().valueOf(2));

	            m.set(mRow, nrow * matrixSize + ncol, val);
	            m.set(mRow, ncol * matrixSize + nrow, val);
	        }

	        term = term.next;
	    }
	}

	/**
	 * Similar as <code>exactCopy2Array</code>, but for block matrices
	 */
        private void exactCopy2BlockArray(CoefficientTerm term, Matrix m,
                                          int[] blockSizes,
                                          int mRow) {
            while (term != null) {
                final BlockMatrixPos pos = new BlockMatrixPos(term.variable, blockSizes);
                
                final Arithmetic val;
                if (pos.column == pos.row)
                    val = term.coefficient;
                else
                    // because such parameters occur twice in the matrix, the
                    // coefficients have to be divided by 2
                    val = term.coefficient.divide(Values.getDefault().valueOf(2));

                final int offset = blockOffset(pos.block, blockSizes);
                
                m.set(mRow, offset + pos.row * blockSizes[pos.block] + pos.column, val);
                m.set(mRow, offset + pos.column * blockSizes[pos.block] + pos.row, val);

                term = term.next;
            }
        }

	// Compute the column and row number, given a variable index (within the
	// upper half of the matrix)

	private static int column(int variable) {
		int col = 0;
		int maxVar = 0;
		while (maxVar < variable) {
			col = col + 1;
			maxVar = maxVar + col + 1;
		}
		return col;
	}

	private static int row(int variable, int column) {
		return variable - (column * (column + 1)) / 2;
	}

	private static class BlockMatrixPos {
	    public final int block;
            public final int row;
	    public final int column;

	    public BlockMatrixPos(int variable, int[] blockSizes) {
	        int offset = 0;
	        for (int i = 0; true; ++i) {
	            final int blockSize = blockSizes[i];
                    final int blockLength = blockSize * (blockSize + 1) / 2;
	            if (offset + blockLength > variable) {
	                // we found the right block
	                block = i;
	                column = column(variable - offset);
	                row = row(variable - offset, column);
	                return;
	            }
	            offset = offset + blockLength;
	        }
	    }
	}
	
	private int blockOffset(int blk, int blockSizes[]) {
	    int res = 0;

	    for (int i = 0; i < blk; ++i)
	      res += blockSizes[i] * blockSizes[i];

	    return res;
	}

	public int size() {
		return polyTerms.size();
	}

	public String toString() {
		final StringBuffer res = new StringBuffer();
		for (Entry<Arithmetic, CoefficientTerm> entry : polyTerms.entrySet()) {
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

	/**
	 * @param nf
	 * @return
	 */
	public SparsePolynomial multiply(Polynomial nf) {
            final SparsePolynomial result = new SparsePolynomial();
            
    		final Iterator<KeyValuePair> monomialIt = nf.monomials();
    		while (monomialIt.hasNext()) {
    			KeyValuePair nextMono = monomialIt.next();
    			final Arithmetic monom = (Arithmetic) nextMono.getKey();
    			final Arithmetic coefficient = (Arithmetic) nextMono.getValue();
                for (Arithmetic c : polyTerms.keySet()) {
                    Arithmetic newMonom = monom.add(c);
				
                    CoefficientTerm coefficientTerm = polyTerms.get(c);
                    while (coefficientTerm != null) {
                        final Arithmetic newCoefficient =
                            coefficientTerm.coefficient.multiply(coefficient);
                        result.addTerm(newMonom, coefficientTerm.variable, newCoefficient);
                        coefficientTerm = coefficientTerm.next;
                    }
		}
            }
            
//		System.out.println("multiplying " + this + " * " + nf + " = " + result);// XXX
            return result;
	}

	public SparsePolynomial add(SparsePolynomial s) {
		SparsePolynomial result = new SparsePolynomial();
		for (Arithmetic m : s.polyTerms.keySet()) {
			CoefficientTerm thisCoTerms = polyTerms.get(m);
			CoefficientTerm sCoTerms = s.polyTerms.get(m);

			if (thisCoTerms == null) {
				result.polyTerms.put(m, sCoTerms);
			} else {
				Map<Integer, Arithmetic> thisCoTermMap = new HashMap<Integer, Arithmetic>();
				Map<Integer, Arithmetic> sCoTermMap = new HashMap<Integer, Arithmetic>();
				CoefficientTerm next = thisCoTerms;
				while (next != null) {
					thisCoTermMap.put(next.variable, next.coefficient);
					next = next.next;
				}
				next = sCoTerms;
				while (next != null) {
					sCoTermMap.put(next.variable, next.coefficient);
					next = next.next;
				}
				Map<Integer, Arithmetic> resultCoTermMap = new HashMap<Integer, Arithmetic>();
				for (Integer i : thisCoTermMap.keySet()) {
					Arithmetic coefficient = thisCoTermMap.get(i);
					if (sCoTermMap.containsKey(i)) {
						coefficient = coefficient.add(sCoTermMap.get(i));
					}

					resultCoTermMap.put(i, coefficient);
				}
				for (Integer i : sCoTermMap.keySet()) {
					if (!resultCoTermMap.containsKey(i)) {
						resultCoTermMap.put(i, sCoTermMap.get(i));
					}
				}

				// convert the resultCoTermMap to CoefficientTerms again
				for (Integer i : resultCoTermMap.keySet()) {
					result.polyTerms.put(m, new CoefficientTerm(resultCoTermMap
							.get(i), i, result.polyTerms.get(m)));
				}
			}
		}

		// add the remaining terms
		for (Arithmetic m : polyTerms.keySet()) {
			if (!result.polyTerms.containsKey(m)) {
				result.polyTerms.put(m, polyTerms.get(m));
			}
		}
//		System.out.println("adding " + this + " + " + s + " = " + result);// XXX
		return result;
	}

	/**
	 * @return
	 */
	public Set<Arithmetic> getMonomials() {
		return polyTerms.keySet();
	}
}
