/***************************************************************************
 *   Copyright (C) 2007 by Andre Platzer                                   *
 *   @informatik.uni-oldenburg.de                                          *
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
/**
 * 
 */
package de.uka.ilkd.key.dl.strategy.features;

import java.util.Map;
import java.util.WeakHashMap;

import de.uka.ilkd.key.dl.formulatools.TermTools;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.impl.QuantifiedImpl;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.rules.UnknownProgressRule;
import de.uka.ilkd.key.dl.rules.metaconstruct.DLUniversalClosureOp;
import de.uka.ilkd.key.dl.strategy.RuleAppCostTimeout;
import de.uka.ilkd.key.dl.strategy.features.HypotheticalProvabilityFeature.HypotheticalProvability;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Sequent;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.QuanUpdateOperator;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Node;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * Weakening part of DiffSat strategy.
 * 
 * @author ap
 */
public class DiffWeakenFeature implements Feature {

    private static final int MAX_STEPS = HypotheticalProvabilityFeature.MAX_HYPOTHETICAL_RULE_APPLICATIONS;

    private Map<Node, Long> branchingNodesAlreadyTested = new WeakHashMap<Node, Long>();

    /**
     * Remembers whether diffweaken works for the given [D&H]F modality
     * and surrounding stuff.
     * diffWeakenCache.get(D&H).get(A==>B,{U}(H->F))
     * remembers whether F is diffweakable for A==>B,{U}[D&H]F
     * @note The cached formula is UNSOUND because of missing universal closures.
     *  This is intentionally so, because DLUniversalClosure otherwise introduces new variables rendering caching useless.
     */
    private Map<DiffSystem, Map<Sequent,RuleAppCost>> diffWeakenCache = new WeakHashMap<DiffSystem, Map<Sequent,RuleAppCost>>();

    public static final DiffWeakenFeature INSTANCE = new DiffWeakenFeature();

    /**
     * the default initial timeout, -1 means use
     * DLOptionBean.INSTANCE.getInitialTimeout()
     */
    private final long initialTimeout;

    /**
     * @param timeout
     *                the default overall (initial) timeout for the hypothetic
     *                proof
     */
    public DiffWeakenFeature(long timeout) {
        this.initialTimeout = timeout;
    }

    public DiffWeakenFeature() {
        this(-1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule.RuleApp,
     *      de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.proof.Goal)
     */
    public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
        Node firstNodeAfterBranch = getFirstNodeAfterBranch(goal.node());
        // if (branchingNodesAlreadyTested.containsKey(firstNodeAfterBranch)) {
        // if (resultCache.containsKey(firstNodeAfterBranch)) {
        // return resultCache.get(firstNodeAfterBranch);
        // }
        // return TopRuleAppCost.INSTANCE;
        // } else {
        Long timeout = getLastTimeout(firstNodeAfterBranch);
        if (timeout == null) {
            timeout = initialTimeout >= 0 ? initialTimeout
                    : DLOptionBean.INSTANCE.getDiffSatTimeout();
        } else {
            final int a = DLOptionBean.INSTANCE
                    .getQuadraticTimeoutIncreaseFactor();
            final int b = DLOptionBean.INSTANCE
                    .getLinearTimeoutIncreaseFactor();
            final int c = DLOptionBean.INSTANCE
                    .getConstantTimeoutIncreaseFactor();
            timeout = a * timeout * timeout + b * timeout + c;
        }
        // branchingNodesAlreadyTested.put(firstNodeAfterBranch, timeout);

        RuleAppCost r = diffWeaken(pos, goal, timeout * 1000);
//        System.out.println("weak " + r + " for " + app.rule().name());
        return r;
    }

    /**
     * Determines whether to apply diffweaken rule to the specified position in goal by DiffSat.
     * @param pos
     * @param goal
     * @param timeout in ms
     */
    public RuleAppCost diffWeaken(PosInOccurrence pos, Goal goal,
            long timeout) {
        Term term = pos.subTerm();
        // unbox from update prefix
        while (term.op() instanceof QuanUpdateOperator) {
            term = ((QuanUpdateOperator) term.op()).target(term);
        }
        if (!(term.op() instanceof Modality
                && term.javaBlock() != null
                && term.javaBlock() != JavaBlock.EMPTY_JAVABLOCK
                && term.javaBlock().program() instanceof StatementBlock)) {
            throw new IllegalArgumentException("inapplicable to " + pos);
        }
        ProgramElement childAt = ((StatementBlock) term
                .javaBlock().program()).getChildAt(0);
        if(childAt instanceof QuantifiedImpl) {
            //TODO: sometimes we could just weaken even with quantifiers...
            return TopRuleAppCost.INSTANCE;
        }
        final DiffSystem system = (DiffSystem) childAt;
        final Term invariant = system.getInvariant(goal.proof().getServices());
        final Term post = term.sub(0);
        final Services services = goal.proof().getServices();

        // diffweaken
        RuleApp diffweaken = goal.indexOfTaclets().lookup(new Name("diffweaken")); 
        //= getRuleAppOf(new Name("diffweaken"), pos, goal);
	
	// diffWeakenCache.get(D&H).get(A==>B,{U}(H->F))
        final Sequent indexing = DiffSatFeature.changedSequent(pos, goal.sequent(),
                       /* SKIP for caching: DLUniversalClosureOp.DL_UNIVERSAL_CLOSURE.universalClosure */
                        TermBuilder.DF.imp(invariant, post), pos.subTerm());
        RuleAppCost cached = RuleAppCostTimeout.superior(timeout, get(system, indexing));
	if (cached != null) {
            return cached;
        } else {
            /*if (get(system, indexing) != null) {
                System.out.println("!!!!\t\tcut time");
            } else if (diffWeakenCache.get(system) != null) {
                System.out.println("!!!!   DELTA " + indexing + " not in " + diffWeakenCache.get(system));
            } else {
                System.out.println("!!!! " + invariant + " implies " + post + "\n" + diffWeakenCache);
            }*/
        }
        // optimize first check if post contained in conjunct of invariant
	if (TermTools.subsumes(invariant, post)) {
            put(system, indexing, LongRuleAppCost.ZERO_COST);
            // if weakening provably successful, only use weakening
            return LongRuleAppCost.ZERO_COST;
	}
        System.out.print("HYPO: " + diffweaken.rule().name() + " "); System.out.flush();
	Term weakenFml = TermBuilder.DF.imp(invariant, post);
        Sequent weakened = DiffSatFeature.changedSequent(pos, goal.sequent(),
                DLUniversalClosureOp.DL_UNIVERSAL_CLOSURE.universalClosure(
                        system, weakenFml, null, services, false),
		pos.subTerm());
        HypotheticalProvability result = HypotheticalProvabilityFeature
                .provable(goal.proof(), weakened, MAX_STEPS, timeout);
        System.out.println(" " + result);
        switch (result) {
        case PROVABLE:
            put(system, indexing, LongRuleAppCost.ZERO_COST);
            // if weakening provably successful, only use weakening
            return LongRuleAppCost.ZERO_COST;
        case DISPROVABLE:
        case ERROR:
            put(system, indexing, TopRuleAppCost.INSTANCE);
            return TopRuleAppCost.INSTANCE;
        case UNKNOWN:
            put(system, indexing, TopRuleAppCost.INSTANCE);
            return TopRuleAppCost.INSTANCE;
        case TIMEOUT:
            put(system, indexing, RuleAppCostTimeout.create(timeout));
            return HypotheticalProvabilityFeature.TIMEOUT_COST;
        default:
            throw new AssertionError("enum known");
        }
    }

    // caching
    private RuleAppCost get(DiffSystem system, Sequent index) {
        Map<Sequent,RuleAppCost> cache = diffWeakenCache.get(system);
        return cache == null ? null : cache.get(index);
    }
    private RuleAppCost put(DiffSystem system, Sequent index, RuleAppCost cost) {
        Map<Sequent,RuleAppCost> cache = diffWeakenCache.get(system);
        if (cache == null)
            cache = new WeakHashMap<Sequent,RuleAppCost>(10);
        RuleAppCost old = cache.put(index, cost);
        diffWeakenCache.put(system, cache);
        return old;
    }

    /**
     * @param node
     * @return
     */
    private Long getLastTimeout(Node node) {
        Long result = null;
        if (node != null) {
            result = branchingNodesAlreadyTested.get(node);
            if (result == null) {
                result = getLastTimeout(node.parent());
            }
        }
        return result;
    }

    /**
     * @return
     */
    public static Node getFirstNodeAfterBranch(Node node) {
        if (node.root()
                || node.parent().root()
                || node.parent().childrenCount() > 1
                || node.parent().getAppliedRuleApp().rule() instanceof UnknownProgressRule) {
            return node;
        }
        return getFirstNodeAfterBranch(node.parent());
    }
}
