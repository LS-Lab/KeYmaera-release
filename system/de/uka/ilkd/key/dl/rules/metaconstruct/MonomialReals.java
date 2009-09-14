// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.util.Iterator;

import orbital.math.AlgebraicAlgorithms;
import orbital.math.Arithmetic;
import orbital.math.Integer;
import orbital.math.Rational;
import orbital.moon.math.ValuesImpl;
import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.collection.ImmutableSLList;
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.OrbitalSimplifier;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.strategy.termfeature.QuasiRealLiteralFeature;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.TermSymbol;
import de.uka.ilkd.key.util.Debug;
import de.uka.ilkd.key.util.LRUCache;

/**
 * Class for analysing and modifying monomial expressions over the reals
 */
public class MonomialReals {
    
    private final ImmutableList<Term> parts;
    private final Arithmetic coefficient;
    
    private MonomialReals(final ImmutableList<Term> parts, final Arithmetic coefficient) {
        this.parts = parts;
        this.coefficient = coefficient;
    }
    
    private static final ImmutableList<Term> nil = ImmutableSLList.nil();
    
    private static final LRUCache<Term, MonomialReals> monomialCache = 
        new LRUCache<Term, MonomialReals> ( 2000 );
    
    public static final MonomialReals ONE =
        new MonomialReals ( nil, ValuesImpl.getDefault().ONE() );
    
    public static MonomialReals create(Term monoTerm) {
        MonomialReals res = monomialCache.get ( monoTerm );
        if ( res == null ) {
            res = createHelp ( monoTerm );
            monomialCache.put ( monoTerm, res );
        }
        return res;
    }

    private static MonomialReals createHelp(Term monomial) {
        final Analyser a = new Analyser ();
        a.analyse ( monomial );
        return new MonomialReals ( a.parts, a.coeff );
    }
    
    public MonomialReals setCoefficient(Arithmetic c) {
        return new MonomialReals ( parts, c );
    }
    
    public MonomialReals multiply(Arithmetic c) {
        return new MonomialReals ( parts, coefficient.multiply ( c ) );
    }
    
    public MonomialReals multiply(MonomialReals m) {
        return new MonomialReals ( parts.prepend ( m.parts ),
                              coefficient.multiply ( m.coefficient ) );
    }
    
    public MonomialReals addToCoefficient(Arithmetic c) {
        return new MonomialReals ( parts, coefficient.add ( c ) );
    }
    
    /**
     * @return true iff the monomial <code>this</code> divides the monomial
     *         <code>m</code>
     */
    public boolean divides (MonomialReals m) {
        if ( m.coefficient.isZero() ) return true;
        if ( this.coefficient.isZero() ) return false;
        
        return difference ( this.parts, m.parts ).isEmpty ();
    }
    
    /**
     * @return true iff the variables/parts of <code>this</code> subsume the
     *         variables of <code>m</code>, i.e., if each variable that
     *         occurs in <code>m</code> occurs in the same or a higher power
     *         in <code>this</code>
     */
    public boolean variablesSubsume(MonomialReals m) {
        return this.parts.size () >= m.parts.size ()
               && difference ( m.parts, this.parts ).isEmpty ();
    }
    
    public boolean variablesEqual(MonomialReals m) {
        return this.parts.size () == m.parts.size ()
               && this.variablesSubsume ( m );
    }
    
    public boolean variablesDisjoint(MonomialReals m) {
        return difference ( m.parts, this.parts ).size () == m.parts.size ();
    }
    
    /**
     * @return the result of dividing the monomial <code>m</code> by the
     *         monomial <code>this</code>
     */
    public MonomialReals reduce(MonomialReals m) {
        if ( m.coefficient.isZero() || this.coefficient.isZero() )
            return new MonomialReals ( nil,
                                       ValuesImpl.getDefault().ZERO() );
        
        return new MonomialReals ( difference ( m.parts, this.parts ),
                                   m.coefficient.divide(this.coefficient) );
    }
    
    /**
     * @return the result of dividing the least common reducible (LCR) of
     *         monomial <code>m</code> and <code>this</code> by the monomial
     *         <code>this</code>
     */
    public MonomialReals divideLCR(MonomialReals m) {
        Debug.assertFalse ( coefficient.isZero() );
        Debug.assertFalse ( m.coefficient.isZero() );
        
        final ImmutableList<Term> newParts = difference ( m.parts, this.parts );

        final Arithmetic gcd;
        if (coefficient instanceof orbital.math.Integer &&
            m.coefficient instanceof orbital.math.Integer) {
            // try to avoid the introduction of fractions if both coefficients
            // are integer
            gcd = AlgebraicAlgorithms.gcd((orbital.math.Integer)coefficient,
                                          (orbital.math.Integer)m.coefficient);
        } else {
            gcd = ValuesImpl.getDefault().ONE();
        }
        
        return new MonomialReals ( newParts, m.coefficient.divide ( gcd ) );
    }

    public Term toTerm () {
        final TermSymbol mul =
            RealLDT.getFunctionFor(de.uka.ilkd.key.dl.model.Mult.class);
        Term res = null;
        
        final Iterator<Term> it = parts.iterator ();
        if ( it.hasNext () ) {
            res = it.next ();
            while ( it.hasNext () )
                res = TermFactory.DEFAULT.createFunctionTerm ( mul, res,
                                                               it.next () );
        }
        
        final Term cTerm = OrbitalSimplifier.arithmetic2Term(coefficient);
        if ( res == null )
            res = cTerm;
        else if ( !coefficient.isOne() )
            res = TermFactory.DEFAULT.createFunctionTerm ( mul, res, cTerm );
        
        return res;        
    }
    
    public String toString() {
        final StringBuffer res = new StringBuffer ();
        res.append ( coefficient );
        
        final Iterator<Term> it = parts.iterator ();
        while ( it.hasNext () )
            res.append ( " * " + it.next () );

        return res.toString ();
    }
    
    private static class Analyser {
        public Arithmetic coeff = ValuesImpl.getDefault().ONE();
        public ImmutableList<Term> parts = nil;
        private final Operator mul =
            RealLDT.getFunctionFor(de.uka.ilkd.key.dl.model.Mult.class);
        	
        public void analyse(Term monomial) {
            if ( monomial.op () == mul ) {
                analyse ( monomial.sub ( 0 ) );
                analyse ( monomial.sub ( 1 ) );
            } else if ( QuasiRealLiteralFeature.isLiteral(monomial) ) {
                final Arithmetic c = QuasiRealLiteralFeature.literal2Arithmetic(monomial);
                coeff = coeff.multiply ( c );
            } else {
                parts = parts.prepend ( monomial );
            }
        }
    }
    

    public boolean equals(Object o) {
        if ( o == this ) return true;
        
        if ( ! ( o instanceof MonomialReals ) ) return false;

        final MonomialReals m = (MonomialReals)o;

        if ( !coefficient.equals ( m.coefficient ) ) return false;
        if ( parts.size () != m.parts.size () ) return false;
        return difference ( parts, m.parts ).isEmpty ();
    }
    
    public int hashCode() {
        int res = coefficient.hashCode ();
        final Iterator<Term> it = parts.iterator ();
        while ( it.hasNext () )
            res += it.next ().hashCode ();
        return res;
    }
    
    /**
     * @return the list of all terms that occur in <code>a</code> but not in
     *         <code>b</code>. multiplicity is treated as well here, so this
     *         is really difference of multisets
     */
    private static ImmutableList<Term> difference(ImmutableList<Term> a, ImmutableList<Term> b) {
        ImmutableList<Term> res = a;
        final Iterator<Term> it = b.iterator ();
        while ( it.hasNext () && !res.isEmpty () )
            res = res.removeFirst ( it.next () );
        return res;
    }

    public Arithmetic getCoefficient() {
        return coefficient;
    }

    public ImmutableList<Term> getParts() {
        return parts;
    }

    public boolean variablesAreCoprime(MonomialReals m) {
        return difference ( parts, m.parts ).equals ( parts );
    }
    
    /**
     * The gcd of two fractions <code>a1/a2</code>, <code>b1/b2</code>, which
     * we define as <code>gcd(a1, b1) / lcm(a2, b2)</code>.
     */
    public static Arithmetic gcd(Arithmetic a, Arithmetic b) {
        final Integer a1, a2;
        if (a instanceof Integer) {
            a1 = (Integer)a;
            a2 = ValuesImpl.getDefault().ONE();
        } else {
            assert (a instanceof Rational);
            final Rational norm = ((Rational)a).representative();
            a1 = norm.numerator();
            a2 = norm.denominator();
        }

        final Integer b1, b2;
        if (b instanceof Integer) {
            b1 = (Integer)b;
            b2 = ValuesImpl.getDefault().ONE();
        } else {
            assert (b instanceof Rational);
            final Rational norm = ((Rational)b).representative();
            b1 = norm.numerator();
            b2 = norm.denominator();
        }
        
        final Integer num;
        if (a1.isZero())
            num = b1;
        else if (b1.isZero())
            num = a1;
        else
            num = (Integer)AlgebraicAlgorithms.gcd.apply(a1, b1);
        
        final Integer denom = (Integer)AlgebraicAlgorithms.lcm.apply(a2, b2);

        return ValuesImpl.getDefault().rational(num, denom);
    }
}
