//This file is part of KeY - Integrated Deductive Software Design
//Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                  Universitaet Koblenz-Landau, Germany
//                  Chalmers University of Technology, Sweden
//
//The KeY system is protected by the GNU General Public License. 
//See LICENSE.TXT for details.
//
//
package de.uka.ilkd.key.strategy.quantifierHeuristics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.recoderext.ImplicitFieldAdder;
import de.uka.ilkd.key.logic.IteratorOfTerm;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.ldt.IntegerLDT;
import de.uka.ilkd.key.logic.op.ArrayOfQuantifiableVariable;
import de.uka.ilkd.key.logic.op.AttributeOp;
import de.uka.ilkd.key.logic.op.IUpdateOperator;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.SetAsListOfQuantifiableVariable;
import de.uka.ilkd.key.logic.op.SetOfQuantifiableVariable;
import de.uka.ilkd.key.util.LRUCache;

/**
 * This classe is used to select and store <code>Trigger</code>s 
 * for a quantified formula in Prenex CNF(PCNF).
 */
class TriggersSet {

    /**a <code>HashMap</code> from <code>Term</code> to 
     * <code>TriggersSet</code> uses to cache all created TriggersSets*/
    private final static Map cache = new LRUCache(1000);
    /** Quantified formula of PCNF*/
    private final Term allTerm;
    /**all <code>Trigger</code>s  for <code>allTerm</code>*/
    private SetOfTrigger allTriggers = SetAsListOfTrigger.EMPTY_SET;
    /**a <code>HashMap</code> from <code>Term</code> to <code>Trigger</code> 
     * which stores different subterms of <code>allTerm</code> 
     * with its according trigger */
    private final Map termToTrigger = new HashMap();
    /**all universal variables of <code>allTerm</code>*/
    private final SetOfQuantifiableVariable uniQuantifiedVariables;
    /**
     * Replacement of the bound variables in <code>allTerm</code> with
     * metavariables and constants
     */
    private final Substitution replacementWithMVs;

    private TriggersSet(Term allTerm, Services services) {
        this.allTerm = allTerm;
        replacementWithMVs = ReplacerOfQuanVariablesWithMetavariables.createSubstitutionForVars(allTerm);
        uniQuantifiedVariables = getAllUQS(allTerm);
        initTriggers(services);
    }

    static TriggersSet create(Term allTerm, Services services) {
        TriggersSet trs = (TriggersSet) cache.get(allTerm);
        if (trs == null) {
            // add check whether it is in PCNF
            trs = new TriggersSet(allTerm, services);
            cache.put(allTerm, trs);
        }
        return trs;
    }

    /**
     * @param allterm
     * @return return all univesal variables of <code>allterm</code>
     */
    private SetOfQuantifiableVariable getAllUQS(Term allterm) {
        final Operator op = allterm.op();
        if (op == Op.ALL) {
            QuantifiableVariable v =
                    allterm.varsBoundHere(0).getQuantifiableVariable(0);
            return getAllUQS(allterm.sub(0)).add(v);
        }
        if (op == Op.EX) {
            return getAllUQS(allterm.sub(0));
        }
        return SetAsListOfQuantifiableVariable.EMPTY_SET;
    }

    /**
     * initial all <code>Trigger</code>s by finding triggers in every clauses
     */
    private void initTriggers(Services services) {
        final QuantifiableVariable var =
                allTerm.varsBoundHere(0).getQuantifiableVariable(0);
        final IteratorOfTerm it =
                TriggerUtils.iteratorByOperator(TriggerUtils.discardQuantifiers(allTerm),
                Op.AND);
        while (it.hasNext()) {
            final Term clause = it.next();
            // a trigger should contain the first variable of allTerm
            if (clause.freeVars().contains(var)) {
                ClauseTrigger ct = new ClauseTrigger(clause);
                ct.createTriggers(services);
            }
        }
    }

    /**
     * 
     * @param trigger
     *            a <code>Term</code>
     * @param qvs
     *            all universal variables of <code>trigger</code>
     * @param isUnify
     *            true if <code>trigger</code>contains existential variable
     * @param isElement
     *            true if the <code>Trigger</code> to be created is taken as a
     *            element of multi-trigger
     * @return a <code>Trigger</code> with <code>trigger</code> as its term
     */
    private Trigger createUniTrigger(Term trigger,
            SetOfQuantifiableVariable qvs,
            boolean isUnify, boolean isElement) {
        Trigger t = (Trigger) termToTrigger.get(trigger);
        if (t == null) {
            t = new UniTrigger(trigger, qvs, isUnify, isElement, this);
            termToTrigger.put(trigger, t);
        }
        return t;
    }

    /**
     * 
     * @param trs
     * @param clause
     *            a <code>Term</code> of clause form
     * @param qvs
     *            all universal varaibles of all <code>clause</code>
     * @return
     */
    private Trigger createMultiTrigger(SetOfTrigger trs, Term clause,
            SetOfQuantifiableVariable qvs) {
        return new MultiTrigger(trs, qvs, clause);
    }

    /**
     * this class is used to find <code>Trigger</code>s in a clause. And it
     * will try to find triggers from every literals in this clause. Every
     * substerm of literal that satify the conditions:(1)it should not be a
     * variable, (2) it doesn't contain propersitional connectives, (3) it is
     * not in loop, (4) it should contains all universal variables in the clause
     * and the first variable of <code>allTerm</code> (5) it doesn't contain
     * subtrigger, will be selected as an Uni-trigger. If a literal does not
     * contain all universal variables in clause, a set of subterms of this
     * literal will be selected as Multi-trigger's elements which are actually
     * uni-triggers except that condition (2) will be changedand into that it
     * contains all universal variables in the literal in. Afterwards, a set of
     * multi-triggers will be constructed by combining thoes elements so that
     * all variables in clause should be include by some of them.
     */
    private class ClauseTrigger {

        final Term clause;
        /**all unversal variables of <code>clause</code>*/
        final SetOfQuantifiableVariable selfUQVS;
        /**elements which are uni-trigges and will be used to construct
         *several multi-triggers for <code>clause</code> */
        private SetOfTrigger elementsOfMultiTrigger =
                SetAsListOfTrigger.EMPTY_SET;

        public ClauseTrigger(Term clause) {
            this.clause = clause;
            selfUQVS = TriggerUtils.intersect(uniQuantifiedVariables, 
                    clause.freeVars());

        }

        /**
         *Searching uni-triggers and elements of multi-triggers in every
         *literal in this <code>clause</code> and add those uni-triggers
         *to the goal trigger set. At last construct multi-triggers from
         * those elements. 
         */
        public void createTriggers(Services services) {
            final IteratorOfTerm it =
                    TriggerUtils.iteratorByOperator(clause, Op.OR);
            while (it.hasNext()) {
                final Term oriTerm = it.next();
                final Iterator it2 = expandIfThenElse(oriTerm).iterator();
                while (it2.hasNext()) {
                    Term t = (Term)it2.next();
                    if (t.op() == Op.NOT) {
                        t = t.sub(0);
                    }
                    recAddTriggers(t, services);
                }
            }
            setMultiTriggers(elementsOfMultiTrigger.toArray(), 0);
        }

        /**
         * @param term    one atom at the begining
         * @param litQVS  all universal variables of <code>term</code>
         * @return true   if find any trigger from <code>term</code>
         */
        private boolean recAddTriggers(Term term, Services services) {
            if (!mightContainTriggers(term)) {
                return false;
            }

            final SetOfQuantifiableVariable uniVarsInTerm =
                    TriggerUtils.intersect(term.freeVars(), selfUQVS);

            boolean foundSubtriggers = false;
            for (int i = 0; i < term.arity(); i++) {
                final Term subTerm = term.sub(i);
                final boolean found = recAddTriggers(subTerm, services);

                if (found && uniVarsInTerm.subset(subTerm.freeVars())) {
                    foundSubtriggers = true;
                }
            }

            if (!foundSubtriggers) {
                addUniTrigger(term, services);
                return true;
            }

            return foundSubtriggers;
        }

        private Set expandIfThenElse(Term t) {
            final Set[] possibleSubs = new Set[t.arity()];
            boolean changed = false;
            for (int i = 0; i != t.arity(); ++i) {
                final Term oriSub = t.sub(i);
                possibleSubs[i] = expandIfThenElse(oriSub);
                changed = changed || possibleSubs[i].size() != 1 || possibleSubs[i].iterator().next() != oriSub;
            }

            final Set res = new HashSet();
            if (t.op() == Op.IF_THEN_ELSE) {
                res.addAll(possibleSubs[1]);
                res.addAll(possibleSubs[2]);
            }

            if (!changed) {
                res.add(t);
                return res;
            }

            final Term[] chosenSubs = new Term[t.arity()];
            final ArrayOfQuantifiableVariable[] boundVars =
                    new ArrayOfQuantifiableVariable[t.arity()];
            for (int i = 0; i != t.arity(); ++i) {
                boundVars[i] = t.varsBoundHere(i);
            }

            res.addAll(combineSubterms(t, possibleSubs, chosenSubs,
                    boundVars, 0));
            return res;
        }

        private Set combineSubterms(Term oriTerm,
                Set/*of Term*/[] possibleSubs,
                Term[] chosenSubs,
                ArrayOfQuantifiableVariable[] boundVars,
                int i) {
            final HashSet set = new HashSet();
            if (i >= possibleSubs.length) {
                final Term res =
                        TermFactory.DEFAULT.createTerm(oriTerm.op(),
                        chosenSubs,
                        boundVars,
                        oriTerm.javaBlock());


                set.add(res);
                return set;
            }


            final Iterator it = possibleSubs[i].iterator();
            while (it.hasNext()) {
                chosenSubs[i] = (Term)it.next();
                set.addAll(combineSubterms(oriTerm, possibleSubs,
                        chosenSubs, boundVars,
                        i + 1));
            }
            return set;
        }

        /**
         * Check whether a given term (or a subterm of the term) might be a
         * trigger candidate
         */
        private boolean mightContainTriggers(Term term) {
            if (term.freeVars().size() == 0) {
                return false;
            }
            final Operator op = term.op();
            if (op instanceof Modality || op instanceof IUpdateOperator || op instanceof QuantifiableVariable) {
                return false;
            }
            if (!UniTrigger.passedLoopTest(term, allTerm)) {
                return false;
            }
            return true;
        }

        /**
         * Further criteria for triggers. This is just a HACK, there should be
         * a more general framework for characterising acceptable triggers
         */
        private boolean isAcceptableTrigger(Term term, Services services) {
            final Operator op = term.op();

            // we do not want to match on expressions a.<created>
            if (op instanceof AttributeOp) {
                final AttributeOp attrOp = (AttributeOp) op;                
                if (attrOp.attribute().name().toString().endsWith(ImplicitFieldAdder.IMPLICIT_CREATED)) {
                    return false;
                }
            }

            final IntegerLDT integerLDT = services.getTypeConverter().getIntegerLDT();
            // matching on equations and inequalities does not seem to have any
            // positive effect for the time being
            if (op == Op.EQUALS || 
                    op == integerLDT.getLessOrEquals() || 
                    op == integerLDT.getGreaterOrEquals()) {
                return false;
            }

            /*
            if ( op == Op.EQUALS ) {
            // we do not want to match on equations t = null
            if ( term.sub ( 0 ).sort () == Sort.NULL
            || term.sub ( 1 ).sort () == Sort.NULL ) return false;
            // we do not want to match on equations t = TRUE
            if ( "TRUE".equals ( term.sub ( 0 ).op ().name ().toString () )
            || "TRUE".equals ( term.sub ( 1 ).op ().name ().toString () ) )
            return false;
            }
             */

            return true;
        }

        /**
         * add a uni-trigger to triggers set or add an element of
         * multi-triggers for this clause
         * @return <code>true</code> if a uni-trigger was added
         */
        private void addUniTrigger(Term term, Services services) {
            if (!isAcceptableTrigger(term, services)) {
                return;
            }
            final boolean isUnify = !term.freeVars().subset(selfUQVS);
            final boolean isElement = !selfUQVS.subset(term.freeVars());
            final SetOfQuantifiableVariable uniVarsInTerm =
                    TriggerUtils.intersect(term.freeVars(), selfUQVS);
            Trigger t = createUniTrigger(term, uniVarsInTerm, isUnify, isElement);
            if (isElement) {
                elementsOfMultiTrigger = elementsOfMultiTrigger.add(t);
            } else {
                allTriggers = allTriggers.add(t);
            }
        }

        /**
         * add a uni-trigger to triggers set or add an element of
         * multi-triggers for this clause
         * @return <code>true</code> if a uni-trigger was added
         */
        private boolean addMultiTrigger(Term term, Services services) {
            if (!isAcceptableTrigger(term, services)) {
                return false;
            }
            final boolean isUnify = !term.freeVars().subset(selfUQVS);
            System.out.println(term);
            final boolean isElement = !selfUQVS.subset(term.freeVars());
            final SetOfQuantifiableVariable uniVarsInTerm =
                    TriggerUtils.intersect(term.freeVars(), selfUQVS);
            Trigger t = createUniTrigger(term, uniVarsInTerm, isUnify, isElement);
            if (isElement) {
                elementsOfMultiTrigger = elementsOfMultiTrigger.add(t);
                return false;
            } else {
                allTriggers = allTriggers.add(t);
                return true;
            }
        }

        /**
         * find all possible combination of <code>ts</code>. Once a
         * combination of elements contains all variables of this clause,
         * it will be used to contruct the multi-trigger which will be 
         * add to triggers set    
         * @param ts elements of multi-triggers at the begining
         * @param i
         * @return
         */
        private Set setMultiTriggers(Trigger[] ts, int i) {
            Set res = new HashSet();
            if (i >= ts.length) {
                return res;
            }
            SetOfTrigger tsi = SetAsListOfTrigger.EMPTY_SET.add(ts[i]);
            res.add(tsi);
            Set nextTriggers = setMultiTriggers(ts, i + 1);
            res.addAll(nextTriggers);
            Iterator it = nextTriggers.iterator();
            while (it.hasNext()) {
                SetOfTrigger next = (SetOfTrigger) it.next();
                next = next.add(ts[i]);
                if (addMultiTrigger(next)) {
                    continue;
                }
                res.add(next);
            }
            return res;
        }

        /**
         * try to construct a multi-trigger by given <code>ts</code>
         * 
         * @param trs
         *            a set of trigger
         * @return true if <code>trs</code> contains all universal varaibles
         *         of this clause, and add the contstructed multi-trigger to
         *         triggers set
         */
        private boolean addMultiTrigger(SetOfTrigger trs) {
            SetOfQuantifiableVariable mulqvs = SetAsListOfQuantifiableVariable.EMPTY_SET;
            IteratorOfTrigger it = trs.iterator();
            while (it.hasNext()) {
                mulqvs = mulqvs.union(it.next().getTriggerTerm().freeVars());
            }
            if (selfUQVS.subset(mulqvs)) {
                Trigger mt = createMultiTrigger(trs, clause, selfUQVS);
                allTriggers = allTriggers.add(mt);
                return true;
            }
            return false;
        }
    }

    public Term getQuantifiedFormula() {
        return allTerm;
    }

    public SetOfTrigger getAllTriggers() {
        return allTriggers;
    }

    public Substitution getReplacementWithMVs() {
        return replacementWithMVs;
    }

    public SetOfQuantifiableVariable getUniQuantifiedVariables() {
        return uniQuantifiedVariables;
    }
}
