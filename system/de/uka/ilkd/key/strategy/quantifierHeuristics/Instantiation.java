// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//This file is part of KeY - Integrated Deductive Software Design
//Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                Universitaet Koblenz-Landau, Germany
//                Chalmers University of Technology, Sweden
//
//The KeY system is protected by the GNU General Public License. 
//See LICENSE.TXT for details.
//
//
package de.uka.ilkd.key.strategy.quantifierHeuristics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.uka.ilkd.key.collection.ImmutableMap;
import de.uka.ilkd.key.collection.DefaultImmutableMap;
import de.uka.ilkd.key.collection.DefaultImmutableSet;
import de.uka.ilkd.key.collection.ImmutableSet;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.Sequent;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.CastFunctionSymbol;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.sort.AbstractSort;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;

class Instantiation {

	private final static TermBuilder tb = TermBuilder.DF;

	/**universally quatifiable variable bound in<code>allTerm</code> */
	private final QuantifiableVariable firstVar;

    private final Term matrix;
    
	/** Literals occurring in the sequent at hand. This is used for branch
     * prediction */
	private ImmutableSet<Term> assumedLiterals = DefaultImmutableSet.<Term>nil();

	/** HashMap from instance(<code>Term</code>) to cost <code>Long</code> */
	private final Map<Term,Long> instancesWithCosts = new HashMap<Term,Long> ();

	/**the <code>TriggersSet</code> of this <code>allTerm</code>*/
	private final TriggersSet triggersSet;
	
	/**Terms bound in every formula on <code>goal</code>*/
	private final ImmutableSet<Term> matchedTerms;

	private Instantiation(Term allterm, Sequent seq, Services services) {
	    firstVar = allterm.varsBoundHere ( 0 ).get ( 0 );
	    matrix = TriggerUtils.discardQuantifiers ( allterm );
	    matchedTerms = sequentToTerms ( seq );
	    triggersSet = TriggersSet.create ( allterm, services );
	    assumedLiterals = initAssertLiterals ( seq );
	    addInstances ( matchedTerms, services );
	}

    
    private static Term lastQuantifiedFormula = null;
    private static Sequent lastSequent = null;
    private static Instantiation lastResult = null;

    static Instantiation create(Term qf, Sequent seq, Services services) {
        if ( qf == lastQuantifiedFormula && seq == lastSequent )
            return lastResult;

        lastQuantifiedFormula = qf;
        lastSequent = seq;
        lastResult = new Instantiation ( qf, seq, services );

        return lastResult;
    }
    
    private static ImmutableSet<Term> sequentToTerms(Sequent seq) {
        ImmutableSet<Term> res = DefaultImmutableSet.<Term>nil();
        for (final ConstrainedFormula cf : seq) {
            res = res.add ( cf.formula () );
        }
        return res;
    }

	
    /**
     * @param terms
     *            on which trigger are doning matching search every
     *            <code>Substitution</code> s by matching
     *            <code>triggers</code> from <code>triggersSet</code> to
     *            <code>terms</code> compute their cost and store the pair of
     *            instance (Term) and cost(Long) in
     *            <code>instancesCostCache</code>
     */
    private void addInstances(ImmutableSet<Term> terms, Services services) {
        for (final Trigger t : triggersSet.getAllTriggers ()) {
            for (final Substitution sub : 
                t.getSubstitutionsFromTerms ( terms, services )) {
                addInstance ( sub, services );
            }
        }        
//        if ( instancesWithCosts.isEmpty () )
            // ensure that there is always at least one instantiation
//            addArbitraryInstance ();
    }

    private void addArbitraryInstance(Services services) {
        ImmutableMap<QuantifiableVariable,Term> varMap =
            DefaultImmutableMap.<QuantifiableVariable,Term>nilMap();

        for (QuantifiableVariable quantifiableVariable : triggersSet.getUniQuantifiedVariables()) {
            final QuantifiableVariable v = quantifiableVariable;
            final Term inst = createArbitraryInstantiation(v, services);
            varMap = varMap.put(v, inst);
        }
        
        addInstance ( new Substitution ( varMap ), services );
    }

    private Term createArbitraryInstantiation(QuantifiableVariable var,
                                              Services services) {
        return tb.func ( ( (AbstractSort)var.sort () ).getCastSymbol (),
                tb.zero(services) );
    }

    private void addInstance(Substitution sub, Services services) {
        final long cost =
            PredictCostProver.computerInstanceCost ( sub, getMatrix(),
                                                     assumedLiterals, services );
        if ( cost != -1 ) addInstance ( sub, cost );
    }

    /**
     * add instance of <code>var</code> in <code>sub</code> with
     * <code>cost</code> to <code>instancesCostCache</code> if this instance
     * is exist, compare thire cost and store the less one.
     * 
     * @param sub
     * @param cost
     */
    private void addInstance(Substitution sub, long cost) {
        final Term inst = sub.getSubstitutedTerm ( firstVar );
        final Long oldCost = instancesWithCosts.get ( inst );
        if ( oldCost == null || oldCost.longValue () >= cost )
            instancesWithCosts.put ( inst, new Long ( cost ) );
    }

    /**
     * @param seq
     * @return all literals in antesequent, and all negation of literal in
     *         succedent
     */
    private ImmutableSet<Term> initAssertLiterals(Sequent seq) {
        ImmutableSet<Term> assertLits = DefaultImmutableSet.<Term>nil();
        for (final ConstrainedFormula cf : seq.antecedent()) {
            final Term atom = cf.formula ();
            final Operator op = atom.op ();
            if ( !( op == Op.ALL || op == Op.EX ) )
                assertLits = assertLits.add ( atom );
        }
        for (final ConstrainedFormula cf : seq.succedent()) {
            final Term atom = cf.formula ();
            final Operator op = atom.op ();
            if ( !( op == Op.ALL || op == Op.EX ) )
                assertLits = assertLits.add ( tb.not ( atom ) );
        }
        return assertLits;
    }
    
    /**
     * Try to find the cost of an instance(inst) according its quantified 
     * formula and current goal. 
     */
    static RuleAppCost computeCost(Term inst, Term form, Sequent seq, Services services) {
        return Instantiation.create ( form, seq, services ).computeCostHelp ( inst );
    }

    private RuleAppCost computeCostHelp(Term inst) {
        Long cost = instancesWithCosts.get ( inst );
        if ( cost == null && ( inst.op () instanceof CastFunctionSymbol ) )
            cost = instancesWithCosts.get ( inst.sub ( 0 ) );

        if ( cost == null ) {
//            if (triggersSet)
            return TopRuleAppCost.INSTANCE;
        }
        if ( cost.longValue () == -1 ) return TopRuleAppCost.INSTANCE;

        return LongRuleAppCost.create ( cost.longValue () );
    }

    /**get all instances from instancesCostCache subsCache*/
    ImmutableSet<Term> getSubstitution() {
        ImmutableSet<Term> res = DefaultImmutableSet.<Term>nil();
        for (final Term inst : instancesWithCosts.keySet ()) {
            res = res.add ( inst );
        }
        return res;
    }

    private Term getMatrix() {
        return matrix;
    }

}
