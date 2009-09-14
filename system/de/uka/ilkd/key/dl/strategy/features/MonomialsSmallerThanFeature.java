// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//


package de.uka.ilkd.key.dl.strategy.features;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.Mult;
import de.uka.ilkd.key.dl.model.Plus;
import de.uka.ilkd.key.dl.strategy.termfeature.QuasiRealLiteralFeature;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.TacletApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;
import de.uka.ilkd.key.strategy.termProjection.ProjectionToTerm;
import de.uka.ilkd.key.strategy.termfeature.BinarySumTermFeature;
import de.uka.ilkd.key.strategy.termfeature.ConstTermFeature;
import de.uka.ilkd.key.strategy.termfeature.OperatorTF;
import de.uka.ilkd.key.strategy.termfeature.SubTermFeature;
import de.uka.ilkd.key.strategy.termfeature.TermFeature;


/**
 * Feature that returns zero iff each monomial of one polynomial is smaller than
 * all monomials of a second polynomial
 */
public class MonomialsSmallerThanFeature extends AbstractMonomialSmallerThanFeature {

    private final TermFeature  hasCoeff;

    private final ProjectionToTerm left, right;
    private final Function mul, add;


    private MonomialsSmallerThanFeature(ProjectionToTerm left, ProjectionToTerm right) {
        this.left = left;
        this.right = right;
        this.add = RealLDT.getFunctionFor(Plus.class);
        this.mul = RealLDT.getFunctionFor(Mult.class);
        
        hasCoeff = createHasCoeffTermFeature ();
    }
    
    static TermFeature createHasCoeffTermFeature() {
        return
            BinarySumTermFeature.createSum (
                  OperatorTF.create ( RealLDT.getFunctionFor(Mult.class) ),
                  SubTermFeature.create ( new TermFeature[] {
                        ConstTermFeature.createConst ( LongRuleAppCost.ZERO_COST ),
                        QuasiRealLiteralFeature.ANY } ) );
    }

    public static Feature create(ProjectionToTerm left, ProjectionToTerm right) {
        return new MonomialsSmallerThanFeature ( left, right );
    }
    
    protected boolean filter(TacletApp app, PosInOccurrence pos, Goal goal) {
        final MonomialCollector m1 = new MonomialCollector ();
        m1.collect ( left.toTerm ( app, pos, goal ) );
        final MonomialCollector m2 = new MonomialCollector ();
        m2.collect ( right.toTerm ( app, pos, goal ) );

        return lessThan ( m1.getResult(), m2.getResult() );
    }

    /**
     * this overwrites the method of <code>SmallerThanFeature</code>
     */
    protected boolean lessThan(Term t1, Term t2) {

        // here, the ordering is graded concerning multiplication on integers
        final int t1Deg = degree ( t1 );
        final int t2Deg = degree ( t2 );
        if ( t1Deg < t2Deg ) return true;
        if ( t1Deg > t2Deg ) return false;

        if ( t1Deg != 0 ) {
            final ImmutableList<Term> atoms1 = collectAtoms ( t1 );
            final ImmutableList<Term> atoms2 = collectAtoms ( t2 );

            if ( atoms1.size () < atoms2.size () ) return false;
            if ( atoms1.size () > atoms2.size () ) return true;
        }
        
        return super.lessThan ( t1, t2 );
    }

    
    
    /**
     * @return the degree of a monomial (number of terms that are connected
     *         multiplicatively) -1. To ensure that also cases like
     *         <tt>f(a*b)=a*b</tt> are handled properly, we simply count the
     *         total number of multiplication operators in the term.
     */
    private int degree(Term t) {
        int res = 0;
        
        if ( t.op () == mul
             && !QuasiRealLiteralFeature.isLiteral(t.sub ( 0 ))
             && !QuasiRealLiteralFeature.isLiteral(t.sub ( 1 )) )
            ++res;

        for ( int i = 0; i != t.arity (); ++i )
            res += degree ( t.sub ( i ) );

        return res;
    }

    private class MonomialCollector extends Collector {
        protected void collect(Term te) {
            if ( te.op () == add ) {
                collect ( te.sub ( 0 ) );
                collect ( te.sub ( 1 ) );
            } else if ( QuasiRealLiteralFeature.isLiteral(te) ) {
              // nothing  
            } else {
                addTerm ( stripOffLiteral ( te ) );
            }
        }

        private Term stripOffLiteral(Term te) {
            if ( ! ( hasCoeff.compute ( te ) instanceof TopRuleAppCost ) )
                // we leave out literals/coefficients on the right, because we
                // do not want to compare these literals
                return te.sub ( 0 );
            return te;
        }
    }
}
