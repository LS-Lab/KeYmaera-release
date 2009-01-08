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

import orbital.math.Arithmetic;
import orbital.moon.math.ValuesImpl;

import de.uka.ilkd.key.dl.arithmetics.impl.orbital.OrbitalSimplifier;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.strategy.termfeature.QuasiRealLiteralFeature;
import de.uka.ilkd.key.logic.*;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.TermSymbol;
import de.uka.ilkd.key.util.LRUCache;

/**
 * Class for analysing and modifying monomial expressions over the reals
 */
public class MonomialReals {
    
    private final ListOfTerm parts;
    private final Arithmetic coefficient;
    
    private MonomialReals(final ListOfTerm parts, final Arithmetic coefficient) {
        this.parts = parts;
        this.coefficient = coefficient;
    }
    
    private static final LRUCache<Term, MonomialReals> monomialCache = 
        new LRUCache<Term, MonomialReals> ( 2000 );
    
    public static final MonomialReals ONE =
        new MonomialReals ( SLListOfTerm.EMPTY_LIST, ValuesImpl.getDefault().ONE() );
    
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
            return new MonomialReals ( SLListOfTerm.EMPTY_LIST,
                                       ValuesImpl.getDefault().ZERO() );
        
        return new MonomialReals ( difference ( m.parts, this.parts ),
                                   m.coefficient.divide(this.coefficient) );
    }
    
    
    public Term toTerm () {
        final TermSymbol mul =
            RealLDT.getFunctionFor(de.uka.ilkd.key.dl.model.Mult.class);
        Term res = null;
        
        final IteratorOfTerm it = parts.iterator ();
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
        
        final IteratorOfTerm it = parts.iterator ();
        while ( it.hasNext () )
            res.append ( " * " + it.next () );

        return res.toString ();
    }
    
    private static class Analyser {
        public Arithmetic coeff = ValuesImpl.getDefault().ONE();
        public ListOfTerm parts = SLListOfTerm.EMPTY_LIST;
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
        final IteratorOfTerm it = parts.iterator ();
        while ( it.hasNext () )
            res += it.next ().hashCode ();
        return res;
    }
    
    /**
     * @return the list of all terms that occur in <code>a</code> but not in
     *         <code>b</code>. multiplicity is treated as well here, so this
     *         is really difference of multisets
     */
    private static ListOfTerm difference(ListOfTerm a, ListOfTerm b) {
        ListOfTerm res = a;
        final IteratorOfTerm it = b.iterator ();
        while ( it.hasNext () && !res.isEmpty () )
            res = res.removeFirst ( it.next () );
        return res;
    }

    public Arithmetic getCoefficient() {
        return coefficient;
    }

    public ListOfTerm getParts() {
        return parts;
    }

    public boolean variablesAreCoprime(MonomialReals m) {
        return difference ( parts, m.parts ).equals ( parts );
    }
    
}
