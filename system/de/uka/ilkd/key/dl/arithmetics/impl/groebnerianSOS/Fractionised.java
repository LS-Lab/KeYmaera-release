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
import orbital.math.Integer;
import orbital.math.MathUtilities;
import orbital.moon.math.ValuesImpl;

/**
 * Class for converting floating-point numbers (doubles) to fractions by
 * approximation up to a certain precision using the Stern-Brocot construction
 * (see Knuth et al, Concrete Mathematics)
 */
public class Fractionised {
    
    public final double fp;
    
    public final Integer numerator;
    public final Integer denominator;
    
    public Fractionised(double fp, double eps) {
	this.fp = fp;
	
	// ensure that fp is non-negative
	final boolean sign;
	if (fp >= 0) {
	    sign = true;
	} else {
	    sign = false;
	    fp = -fp;
	}

	// "binary search" to find the EPS-near rational with the smallest
	// numerator and denominator
	Integer leftNum = ValuesImpl.getDefault().valueOf((int)Math.floor(fp));
	Integer leftDenom = ValuesImpl.getDefault().ONE();
	Integer rightNum = ValuesImpl.getDefault().ONE();
	Integer rightDenom = ValuesImpl.getDefault().ZERO();
	Integer finalNum = null;
	Integer finalDenom = null;
	
	if (compare(fp, leftNum, leftDenom, eps) == 0) {
	    finalNum = leftNum;
	    finalDenom = leftDenom;
	} else {
	    int i = 0;
	    while (true) {
		final Integer midNum = leftNum.add(rightNum);
		final Integer midDenom = leftDenom.add(rightDenom);
	    
		final int c = compare(fp, midNum, midDenom, eps);
		if (c < 0) {
		    rightNum = midNum;
		    rightDenom = midDenom;
		} else if (c > 0) {
		    leftNum = midNum;
		    leftDenom = midDenom;
		} else {
		    finalNum = midNum;
		    finalDenom = midDenom;
		    break;
		}
		
		if (i > 1000) {
		    // this is not going to work, bail out
                    finalNum = midNum;
                    finalDenom = midDenom;
                    break;
		}
		
		i = i + 1;
	    }
	}
	
	if (sign)
	    numerator = finalNum;
	else
	    numerator = (Integer)finalNum.minus();
	denominator = finalDenom;
    }
    
    /**
     * @return a negative number if <code>decimal</code> is smaller than
     *         <code>exactNum/exactDenom</code>, 0 if the difference between
     *         them is at most <code>EPS</code>, and a positive number if
     *         <code>decimal</code> is bigger
     */
    private static int compare(double decimal,
	                       Integer exactNum, Integer exactDenom,
	                       double eps) {
	if (exactDenom.isZero())
	    // infinity is bigger than anything else
	    return -1;
	
	final double exact = OrbitalSimplifier.toDouble(exactNum) /
	                     OrbitalSimplifier.toDouble(exactDenom);
	
	if (decimal < exact) {
	    final double diff = exact - decimal;
	    if (diff <= eps)
		return 0;
	    return -1;
	} else {
	    final double diff = decimal - exact;
	    if (diff <= eps)
		return 0;
	    return 1;
	}
    }
    
    public Arithmetic asFraction() {
	return ValuesImpl.getDefault().rational(numerator, denominator);
    }

    public String toString() {
	return "" + fp + " ~ " + numerator + "/" + denominator;
    }
    
}
