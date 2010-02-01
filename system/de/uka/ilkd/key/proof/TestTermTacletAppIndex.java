// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

/** tests the TacletIndex class.*/
package de.uka.ilkd.key.proof;


import java.util.Iterator;

import junit.framework.TestCase;
import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.collection.ImmutableSLList;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.*;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.rule.NoPosTacletApp;
import de.uka.ilkd.key.rule.Taclet;
import de.uka.ilkd.key.rule.TacletForTests;


public class TestTermTacletAppIndex extends TestCase{   

    NoPosTacletApp ruleRewriteNonH1H2; 
    NoPosTacletApp ruleNoFindNonH1H2H3;
    NoPosTacletApp ruleAntecH1;
    NoPosTacletApp ruleSucc;
    NoPosTacletApp ruleMisMatch;
    NoPosTacletApp notfreeconflict;
    NoPosTacletApp remove_f;
    NoPosTacletApp remove_ff;
    NoPosTacletApp remove_zero;
    Metavariable x;
        
    public TestTermTacletAppIndex(String name) {
	super(name);
    }

    private Taclet taclet(String name) {
	return TacletForTests.getTaclet(name).taclet();
    }
    
    public void setUp() {
        TacletForTests.parse(System.getProperty("key.home")+
                java.io.File.separator+"system"+java.io.File.separator+
        "de/uka/ilkd/key/proof/ruleForTestTacletIndex.taclet");

        ruleRewriteNonH1H2 = NoPosTacletApp.createNoPosTacletApp(taclet("rewrite_noninteractive_h1_h2"));
        ruleNoFindNonH1H2H3 = NoPosTacletApp.createNoPosTacletApp(taclet("nofind_noninteractive_h1_h2_h3"));
        ruleAntecH1 = NoPosTacletApp.createNoPosTacletApp(taclet("rule_antec_h1"));
        ruleSucc = NoPosTacletApp.createNoPosTacletApp(taclet("rule_succ"));
        ruleMisMatch = NoPosTacletApp.createNoPosTacletApp(taclet ("antec_mismatch"));
        notfreeconflict = NoPosTacletApp.createNoPosTacletApp(taclet("not_free_conflict"));
        remove_f = NoPosTacletApp.createNoPosTacletApp(taclet("remove_f"));
        remove_ff = NoPosTacletApp.createNoPosTacletApp(taclet("remove_ff"));
        remove_zero = NoPosTacletApp.createNoPosTacletApp(taclet("remove_zero"));

        x = new Metavariable (new Name ("X"), 
                TacletForTests.sortLookup("nat"));
        TacletForTests.getVariables().add(x);
    }
    
    public void tearDown() {
        x = null;
        ruleRewriteNonH1H2 = null;
        ruleNoFindNonH1H2H3 = null;
        ruleAntecH1 = null;
        ruleSucc = null;
        ruleMisMatch = null;
        notfreeconflict = null;
        remove_f = null;
        remove_ff = null;
        remove_zero = null;       
        realCache = null;
	noCache = null;	
    }


    private TermTacletAppIndexCacheSet realCache =
        new TermTacletAppIndexCacheSet ();

    private TermTacletAppIndexCacheSet noCache =
        new TermTacletAppIndexCacheSet () {
            public ITermTacletAppIndexCache getAntecCache() {
                return getNoCache ();
            }
            public ITermTacletAppIndexCache getSuccCache() {
                return getNoCache ();
            }
    };
    

    public void testIndex0 () {
        doTestIndex0(noCache);
    }

    public void testIndex0WithCache () {
        for ( int i = 0; i != 3; ++i )
            doTestIndex0 ( realCache );
    }

    private void doTestIndex0(TermTacletAppIndexCacheSet cache) {
        Services serv = TacletForTests.services ();

        TacletIndex ruleIdx = new TacletIndex ();
        ruleIdx.add ( remove_f );
        ruleIdx.add ( remove_zero );

        Term term = TacletForTests.parseTerm ( "f(f(f(zero)))=one" );
        ConstrainedFormula cfma = new ConstrainedFormula ( term );

        PosInOccurrence pio = new PosInOccurrence ( cfma, PosInTerm.TOP_LEVEL,
                                                    false );

        TermTacletAppIndex termIdx =
            TermTacletAppIndex.create ( pio, serv, Constraint.BOTTOM, ruleIdx,
                                        NullNewRuleListener.INSTANCE,
                                        TacletFilter.TRUE, cache );

        checkTermIndex ( pio, termIdx );

        // this should not alter the index, as the formula actually
        // did not change
        termIdx = termIdx.update ( pio.down ( 0 ), serv, Constraint.BOTTOM,
                                   ruleIdx, NullNewRuleListener.INSTANCE,
                                   cache );

        checkTermIndex ( pio, termIdx );

        // now a real change
        Term term2 = TacletForTests.parseTerm ( "f(f(zero))=one" );
        ConstrainedFormula cfma2 = new ConstrainedFormula ( term2 );
        PosInOccurrence pio2 = new PosInOccurrence ( cfma2,
                                                     PosInTerm.TOP_LEVEL, false );

        termIdx = termIdx.update ( pio2.down ( 0 ).down ( 0 ).down ( 0 ), serv,
                                   Constraint.BOTTOM, ruleIdx,
                                   NullNewRuleListener.INSTANCE, cache );
        checkTermIndex2 ( pio2, termIdx );

        // add a new taclet to the index
        ruleIdx.add ( remove_ff );
        SetRuleFilter filter = new SetRuleFilter ();
        filter.addRuleToSet ( ruleIdx.lookup ( remove_ff.taclet().name () ).rule () );
        termIdx = termIdx.addTaclets ( filter, pio2, serv, Constraint.BOTTOM,
                                       ruleIdx, NullNewRuleListener.INSTANCE );
        checkTermIndex3 ( pio2, termIdx );
    }

    public void testIndexWithMV() {
        doTestIndexWithMV ( noCache );
    }

    public void testIndexWithMVWithCache() {
        for ( int i = 0; i != 3; ++i )
            doTestIndexWithMV ( realCache );
    }

    private void doTestIndexWithMV(TermTacletAppIndexCacheSet cache) {
        Services serv = TacletForTests.services ();

        TacletIndex ruleIdx = new TacletIndex ();
        ruleIdx.add ( remove_f );
        ruleIdx.add ( remove_zero );

        Term t1 = TacletForTests.parseTerm ( "f(zero)" );
        Term t2 = TacletForTests.parseTerm ( "X" );

        Constraint c = Constraint.BOTTOM.unify ( t1, t2, null );
        assertTrue ( c.isSatisfiable () );
        Term term = TacletForTests.parseTerm ( "f(f(X))=one" );
        ConstrainedFormula cfma = new ConstrainedFormula ( term, c );
        // System.out.println(cfma);

        PosInOccurrence pio = new PosInOccurrence ( cfma, PosInTerm.TOP_LEVEL,
                                                    false );

        TermTacletAppIndex termIdx =
            TermTacletAppIndex.create ( pio, serv, Constraint.BOTTOM, ruleIdx,
                                        NullNewRuleListener.INSTANCE,
                                        TacletFilter.TRUE, cache );

        checkTermIndex ( pio, termIdx );

        // this should not alter the index, as the formula actually
        // did not change
        termIdx = termIdx.update ( pio.down ( 0 ), serv, Constraint.BOTTOM,
                                   ruleIdx, NullNewRuleListener.INSTANCE,
                                   cache );

        checkTermIndex ( pio, termIdx );

        // now a real change
        Term term2 = TacletForTests.parseTerm ( "f(f(zero))=one" );
        ConstrainedFormula cfma2 = new ConstrainedFormula ( term2, c );
        PosInOccurrence pio2 = new PosInOccurrence ( cfma2,
                                                     PosInTerm.TOP_LEVEL, false );

        termIdx = termIdx.update ( pio2.down ( 0 ).down ( 0 ).down ( 0 ), serv,
                                   Constraint.BOTTOM, ruleIdx,
                                   NullNewRuleListener.INSTANCE, cache );
        checkTermIndex2 ( pio2, termIdx );

        // add a new taclet to the index
        ruleIdx.add ( remove_ff );
        SetRuleFilter filter = new SetRuleFilter ();
        filter.addRuleToSet ( ruleIdx.lookup ( remove_ff.taclet().name () ).rule () );
        termIdx = termIdx.addTaclets ( filter, pio2, serv, Constraint.BOTTOM,
                                       ruleIdx, NullNewRuleListener.INSTANCE );
        checkTermIndex3 ( pio2, termIdx );
    }

    private void checkAtPos(PosInOccurrence pio,
			    TermTacletAppIndex termIdx,
			    ImmutableList<Taclet> list) {
        checkTacletList(termIdx.getTacletAppAt(pio,
                                               TacletFilter.TRUE),
                        list);
    }

    private PosInOccurrence down ( PosInOccurrence pio, int i ) {
	return handleDisplayConstraint(pio.down(i),
	                               pio.constrainedFormula().constraint());
    }

    private void checkTermIndex(PosInOccurrence pio,
                                TermTacletAppIndex termIdx) {
        ImmutableList<Taclet> listA = ImmutableSLList.<Taclet>nil();
        ImmutableList<Taclet> listB = listA.prepend(remove_f.taclet());
        ImmutableList<Taclet> listC = listA.prepend(remove_zero.taclet());
        
        checkAtPos(pio, termIdx, listA);
        checkAtPos(down(pio, 0), termIdx, listB);
        checkAtPos(down(down(pio, 0), 0), termIdx, listB);
        checkAtPos(down(down(down(pio, 0), 0), 0), termIdx, listB);
        checkAtPos(down(down(down(down(pio, 0), 0), 0), 0), termIdx, listC);
        checkAtPos(down(pio, 1), termIdx, listA);
    }

    private void checkTermIndex2(PosInOccurrence pio,
				 TermTacletAppIndex termIdx) {
	ImmutableList<Taclet> listA = ImmutableSLList.<Taclet>nil();
	ImmutableList<Taclet> listB = listA.prepend(remove_f.taclet());
	ImmutableList<Taclet> listC = listA.prepend(remove_zero.taclet());

	checkAtPos(pio, termIdx, listA);
	checkAtPos(down(pio, 0), termIdx, listB);
	checkAtPos(down(down(pio, 0), 0), termIdx, listB);
	checkAtPos(down(down(down(pio, 0), 0), 0), termIdx, listC);
	checkAtPos(down(pio, 1), termIdx, listA);
    }

    private void checkTermIndex3(PosInOccurrence pio,
				 TermTacletAppIndex termIdx) {
	ImmutableList<Taclet> listA = ImmutableSLList.<Taclet>nil();
	ImmutableList<Taclet> listB = listA.prepend(remove_f.taclet());
	ImmutableList<Taclet> listC = listA.prepend(remove_zero.taclet());
	ImmutableList<Taclet> listD = listB.prepend(remove_ff.taclet());

	checkAtPos(pio, termIdx, listA);
	checkAtPos(down(pio, 0), termIdx, listD);
	checkAtPos(down(down(pio, 0), 0), termIdx, listB);
	checkAtPos(down(down(down(pio, 0), 0), 0), termIdx, listC);
	checkAtPos(down(pio, 1), termIdx, listA);
    }


    private void checkTacletList ( ImmutableList<NoPosTacletApp> p_toCheck,
				   ImmutableList<Taclet>         p_template ) {
	assertTrue ( p_toCheck.size () == p_template.size () );
        for (NoPosTacletApp aP_toCheck : p_toCheck) assertTrue(p_template.contains(aP_toCheck.taclet()));
    }

    /**
     * Check whether the given term is a metavariable, and replace it
     * with a concrete term provided that such a term is determined by
     * the user constraint
     * @return A <code>PosInOccurrence</code> object in which
     * eventually the metavariable has been replaced with a term as
     * given by the user constraint. In any case the object points to
     * the same position of a term as the <code>pos</code> parameter
     */
    private static PosInOccurrence handleDisplayConstraint
	( PosInOccurrence pos, Constraint displayConstraint ) {

	Term term = pos.subTerm ();

	if ( term.op () instanceof Metavariable ) {
	    if ( pos.termBelowMetavariable () == null ) {
		Term metaTerm = displayConstraint
		    .getInstantiation ( (Metavariable)term.op () );
		if ( metaTerm.op () != term.op () )
		    return pos.setTermBelowMetavariable ( metaTerm );
	    }
	}

	return pos;
    }
    
}
