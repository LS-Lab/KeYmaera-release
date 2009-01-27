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

package de.uka.ilkd.key.dl.strategy.termfeature;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import orbital.math.Arithmetic;
import orbital.math.ValueFactory;
import orbital.moon.math.ValuesImpl;

import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.Div;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.termfeature.BinaryTermFeature;
import de.uka.ilkd.key.strategy.termfeature.TermFeature;

/**
 * Term feature that returns zero iff the considered term is a real literal that
 * is fully normalised. Such a term can be an integer literal or a proper
 * irreducible fraction with a positive denominator.
 */
public abstract class QuasiRealLiteralFeature extends BinaryTermFeature {

    public static final TermFeature ANY = new QuasiRealLiteralFeature () {
        protected boolean checkValue(BigInteger num, BigInteger denom) {
            return true;
        }
    };

    public static final TermFeature ZERO = new QuasiRealLiteralFeature () {
        protected boolean checkValue(BigInteger num, BigInteger denom) {
            return num.signum() == 0;
        }
    };

    public static final TermFeature ONE = new QuasiRealLiteralFeature () {
        protected boolean checkValue(BigInteger num, BigInteger denom) {
            return num.equals(BigInteger.ONE) && denom.equals(BigInteger.ONE);
        }
    };

    public static final TermFeature INTEGER = new QuasiRealLiteralFeature () {
        protected boolean checkValue(BigInteger num, BigInteger denom) {
            return denom.equals(BigInteger.ONE);
        }
    };

    public static final TermFeature POSITIVE = new QuasiRealLiteralFeature () {
        protected boolean checkValue(BigInteger num, BigInteger denom) {
            return num.signum() > 0;
        }
    };

    public static final TermFeature NEGATIVE = new QuasiRealLiteralFeature () {
        protected boolean checkValue(BigInteger num, BigInteger denom) {
            return num.signum() < 0;
        }
    };

    public static final TermFeature NON_POSITIVE = new QuasiRealLiteralFeature () {
        protected boolean checkValue(BigInteger num, BigInteger denom) {
            return num.signum() <= 0;
        }
    };

    public static final TermFeature NON_NEGATIVE = new QuasiRealLiteralFeature () {
        protected boolean checkValue(BigInteger num, BigInteger denom) {
            return num.signum() >= 0;
        }
    };

    private QuasiRealLiteralFeature () {}
    
    protected abstract boolean checkValue(BigInteger num, BigInteger denom);
    
    protected boolean filter(Term term) {
	if (term.op() == RealLDT.getFunctionFor(Div.class)) {
	    if (!isIntLiteral(term.sub(0)) || !isIntLiteral(term.sub(1)))
		return false;
	    BigInteger num = new BigInteger (term.sub(0).op().name().toString());
	    BigInteger denom = new BigInteger (term.sub(1).op().name().toString());
	    return num.signum() != 0 &&
	           denom.compareTo(BigInteger.ONE) > 0 &&
	           num.gcd(denom).equals(BigInteger.ONE) &&
	           checkValue(num, denom);
	}
	 
	if (!isIntLiteral(term))
	    return false;
        BigInteger num = new BigInteger (term.op().name().toString());
        return checkValue(num, BigInteger.ONE);
    }
    
    private final static Pattern anyInt = Pattern.compile("0|-?[1-9]\\d*");

    private boolean isIntLiteral(Term term) {
	if (!(term.op() instanceof RigidFunction
		&& ((RigidFunction) term.op()).arity() == 0))
	    return false;
	Matcher m = anyInt.matcher(term.op().name().toString());
	return m.matches();
    }
    
    public static boolean isLiteral(Term term) {
        return !(ANY.compute(term) instanceof TopRuleAppCost);
    }
    
    public static Arithmetic literal2Arithmetic(Term term) {
        // we assume that the term actually denotes a literal
        assert(isLiteral(term));
        ValueFactory f = ValuesImpl.getDefault();
        if (term.op() == RealLDT.getFunctionFor(Div.class)) {
            BigInteger num = new BigInteger (term.sub(0).op().name().toString());
            BigInteger denom = new BigInteger (term.sub(1).op().name().toString());
            return f.rational(f.valueOf(num), f.valueOf(denom));
        }
        return f.valueOf(term.op().name().toString());
    }
}
