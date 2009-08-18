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

import orbital.math.Arithmetic;
import orbital.math.functional.Operations;
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
import de.uka.ilkd.key.util.LRUCache;

/**
 * Class for analysing and modifying polynomial expressions over the integers
 */
public class PolynomialReals {

    private final Arithmetic constantPart;
    private final ImmutableList<MonomialReals> parts;

    private static final ImmutableList<MonomialReals> nil = ImmutableSLList.nil();
    
    private PolynomialReals(ImmutableList<MonomialReals> parts, Arithmetic constantPart) {
        this.parts = parts;
        this.constantPart = constantPart;
    }
    
    private static final LRUCache<Term, PolynomialReals> polynomialCache = 
        new LRUCache<Term, PolynomialReals> ( 2000 );

    public final static PolynomialReals ZERO =
        new PolynomialReals ( nil,
        	              ValuesImpl.getDefault().ZERO());    
    public final static PolynomialReals ONE =
        new PolynomialReals ( nil,
        	              ValuesImpl.getDefault().ONE() );    

    public static PolynomialReals create(Term polyTerm) {
        PolynomialReals res = polynomialCache.get ( polyTerm );
        if ( res == null ) {
            res = createHelp ( polyTerm );
            polynomialCache.put ( polyTerm, res );
        }
        return res;
    }

    private static PolynomialReals createHelp(Term polynomial) {
        final Analyser a = new Analyser ();
        a.analyse ( polynomial );
        return new PolynomialReals ( a.parts, a.constantPart );
    }

    public PolynomialReals multiply(Arithmetic c) {
        if ( c.isZero () )
            return ZERO;
        ImmutableList<MonomialReals> newParts = ImmutableSLList.nil();
        final Iterator<MonomialReals> it = parts.iterator ();
        while ( it.hasNext () )
            newParts = newParts.prepend ( it.next ().multiply ( c ) );

        return new PolynomialReals ( newParts, constantPart.multiply ( c ) );
    }

    public PolynomialReals multiply(MonomialReals m) {
        if ( m.getCoefficient ().isZero () )
            return ZERO;
        
        ImmutableList<MonomialReals> newParts = ImmutableSLList.nil();
        final Iterator<MonomialReals> it = parts.iterator ();
        while ( it.hasNext () )
            newParts = newParts.prepend ( it.next ().multiply ( m ) );

        if ( m.getParts ().isEmpty () )
            return new PolynomialReals ( newParts,
                                    constantPart.multiply ( m.getCoefficient () ) );
        
        newParts = addPart ( newParts, m.multiply ( constantPart ) );
        return new PolynomialReals ( newParts, ValuesImpl.getDefault().ZERO() );
    }

    public PolynomialReals add(Arithmetic c) {
        return new PolynomialReals ( parts, constantPart.add ( c ) );
    }
    
    public PolynomialReals sub(PolynomialReals p) {
        final Arithmetic newConst =
            getConstantTerm ().subtract ( p.getConstantTerm () );
        ImmutableList<MonomialReals> newParts = parts;
        final Iterator<MonomialReals> it = p.getParts ().iterator ();
        while ( it.hasNext () )
            newParts = addPart ( newParts, it.next ().multiply
        	                         ( ValuesImpl.getDefault().MINUS_ONE() ) );
        return new PolynomialReals ( newParts, newConst );
    }
    
    public PolynomialReals add(MonomialReals m) {
        if ( m.getParts ().isEmpty () )
            return new PolynomialReals ( parts,
                                    constantPart.add ( m.getCoefficient () ) );

        return new PolynomialReals ( addPart ( parts, m ), constantPart );
    }
    
    public PolynomialReals add(PolynomialReals p) {
        final Arithmetic newConst =
            getConstantTerm ().add ( p.getConstantTerm () );
        ImmutableList<MonomialReals> newParts = parts;
        final Iterator<MonomialReals> it = p.getParts ().iterator ();
        while ( it.hasNext () )
            newParts = addPart ( newParts, it.next () );
        return new PolynomialReals ( newParts, newConst );
    }

    /**
     * @return the greatest common divisor of the coefficients of the monomials
     *         of this polynomial (referring to {@link MonomialReals.gcd}).
     *         The constant part of the polynomial is not taken into account. If
     *         there are no monomials (apart from the constant term), the result
     *         is <code>ZERO</code>
     */
    public Arithmetic coeffGcd() {
        Arithmetic res = ValuesImpl.getDefault().ZERO();
        final Iterator<MonomialReals> it = parts.iterator ();
        while ( it.hasNext () )
            res = MonomialReals.gcd ( res, it.next ().getCoefficient () );
        return res;
    }
    
    /**
     * @return <code>true</code> if the value of <code>this</code> will
     *         always be less than the value of <code>p</code>
     *         (i.e., same monomials, but the constant part is less or equal)
     */
    public boolean valueLess(PolynomialReals p) {
        if ( !sameParts ( p ) ) return false;
        return Operations.less.apply ( constantPart, p.constantPart );
    }

    /**
     * @return <code>true</code> if the value of <code>this</code> will
     *         always be equal to the value of <code>p</code>
     *         (i.e., same monomials and same constant part)
     */
    public boolean valueEq(PolynomialReals p) {
        if ( !sameParts ( p ) ) return false;
        return Operations.equal.apply ( constantPart, p.constantPart );
    }

    public boolean valueUneq(PolynomialReals p) {
        if ( !sameParts ( p ) ) return false;
        return !Operations.equal.apply ( constantPart, p.constantPart );
    }

    public boolean valueEq(Arithmetic c) {
        if ( !parts.isEmpty () ) return false;
        return Operations.equal.apply ( constantPart, c );
    }

    public boolean valueUneq(Arithmetic c) {
        if ( !parts.isEmpty () ) return false;
        return !Operations.equal.apply ( constantPart, c );
    }

    /**
     * @return <code>true</code> if the value of <code>this</code> will
     *         always be less or equal than the value of <code>p</code>
     *         (i.e., same monomials, but the constant part is less or equal)
     */
    public boolean valueLeq(PolynomialReals p) {
        if ( !sameParts ( p ) ) return false;
        return Operations.lessEqual.apply ( constantPart, p.constantPart );
    }

    public boolean valueLess(Arithmetic c) {
        if ( !parts.isEmpty () ) return false;
        return Operations.less.apply ( constantPart, c );
    }

    public boolean valueGeq(Arithmetic c) {
        if ( !parts.isEmpty () ) return false;
        return Operations.greaterEqual.apply ( constantPart, c );
    }

    public boolean sameParts(PolynomialReals p) {
        if ( parts.size () != p.parts.size () ) return false;
        return difference ( parts, p.parts ).isEmpty ();
    }
    
    public Term toTerm () {
        final TermSymbol add = 
            RealLDT.getFunctionFor(de.uka.ilkd.key.dl.model.Plus.class);
        Term res = null;
        
        final Iterator<MonomialReals> it = parts.iterator ();
        if ( it.hasNext () ) {
            res = it.next ().toTerm ();
            while ( it.hasNext () )
                res = TermFactory.DEFAULT.createFunctionTerm
                              ( add, res, it.next ().toTerm () );
        }
        
        final Term cTerm = OrbitalSimplifier.arithmetic2Term(constantPart);
        
        if ( res == null )
            res = cTerm;
        else if ( !constantPart.isZero () )
            res = TermFactory.DEFAULT.createFunctionTerm ( add, cTerm, res );
        
        return res;        
    }
    
    public String toString() {
        final StringBuffer res = new StringBuffer ();
        res.append ( constantPart );
        
        final Iterator<MonomialReals> it = parts.iterator ();
        while ( it.hasNext () )
            res.append ( " + " + it.next () );

        return res.toString ();        
    }
    
    private static class Analyser {
        public Arithmetic constantPart = ValuesImpl.getDefault().ZERO();
        public ImmutableList<MonomialReals> parts = nil;
        private final Operator add =
            RealLDT.getFunctionFor(de.uka.ilkd.key.dl.model.Plus.class);

        public void analyse(Term polynomial) {
            final Operator op = polynomial.op ();
            if ( op == add ) {
                analyse ( polynomial.sub ( 0 ) );
                analyse ( polynomial.sub ( 1 ) );
            } else if ( QuasiRealLiteralFeature.isLiteral(polynomial) ) {
                final Arithmetic c = QuasiRealLiteralFeature.literal2Arithmetic(polynomial);
                constantPart = constantPart.add ( c );
            } else {
                parts = addPart ( parts, MonomialReals.create ( polynomial ) );
            }
        }
    }

    /**
     * @return the list of all monomials that occur in <code>a</code> but not
     *         in <code>b</code>. multiplicity is treated as well here, so
     *         this is really difference of multisets
     */
    private static ImmutableList<MonomialReals> difference(ImmutableList<MonomialReals> a, ImmutableList<MonomialReals> b) {
        ImmutableList<MonomialReals> res = a;
        final Iterator<MonomialReals> it = b.iterator ();
        while ( it.hasNext () && !res.isEmpty () )
            res = res.removeFirst ( it.next () );
        return res;
    }

    private static ImmutableList<MonomialReals> addPart(ImmutableList<MonomialReals> oldParts, MonomialReals m) {
        if ( m.getCoefficient ().isZero() ) return oldParts;
        final ImmutableList<MonomialReals> newParts = addPartHelp ( oldParts, m );
        if ( newParts != null ) return newParts;
        return oldParts.prepend ( m );
    }

    private static ImmutableList<MonomialReals> addPartHelp(ImmutableList<MonomialReals> oldParts, MonomialReals m) {
        if ( oldParts.isEmpty () ) return null;
        final MonomialReals head = oldParts.head ();
        final ImmutableList<MonomialReals> tail = oldParts.tail ();
        if ( head.variablesEqual ( m ) ) {
            final MonomialReals newHead =
                head.addToCoefficient ( m.getCoefficient () );
            if ( newHead.getCoefficient ().isZero() ) return tail;
            return tail.prepend ( newHead );
        }
        final ImmutableList<MonomialReals> res = addPartHelp ( tail, m );
        if ( res == null ) return null;
        return res.prepend ( head );
    }    
    
    public Arithmetic getConstantTerm() {
        return constantPart;
    }

    public ImmutableList<MonomialReals> getParts() {
        return parts;
    }
    
}
