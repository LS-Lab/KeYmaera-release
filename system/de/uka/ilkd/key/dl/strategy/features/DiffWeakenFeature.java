/***************************************************************************
 *   Copyright (C) 2007 by André Platzer                                   *
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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.rules.UnknownProgressRule;
import de.uka.ilkd.key.dl.rules.metaconstruct.DLUniversalClosureOp;
import de.uka.ilkd.key.dl.rules.metaconstruct.DiffInd;
import de.uka.ilkd.key.dl.strategy.features.HypotheticalProvabilityFeature.HypotheticalProvability;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.ConstrainedFormula;
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
import de.uka.ilkd.key.proof.TacletFilter;
import de.uka.ilkd.key.rule.ListOfTacletApp;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.rule.Taclet;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * DiffSat strategy.
 * 
 * @author ap
 */
public class DiffWeakenFeature implements Feature {

    private static final int MAX_STEPS = HypotheticalProvabilityFeature.MAX_HYPOTHETICAL_RULE_APPLICATIONS;

    private Map<Node, Long> branchingNodesAlreadyTested = new WeakHashMap<Node, Long>();

    /**
     * Remembers whether diffweaken works for the given [D]F modality.
     */
    private Map<Term, RuleAppCost> diffWeakenCache = new WeakHashMap<Term, RuleAppCost>();

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
                    : DLOptionBean.INSTANCE.getInitialTimeout();
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

        return diffWeaken(pos, goal, timeout);
    }

    /**
     * Determines whether to apply diffweaken rule to the specified position in goal by DiffSat.
     * @param pos
     * @param goal
     * @param timeout
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
        if (diffWeakenCache.containsKey(term)) {
            return diffWeakenCache.get(term);
        }
        final DiffSystem system = (DiffSystem) ((StatementBlock) term
                .javaBlock().program()).getChildAt(0);
        final Term invariant = system.getInvariant();
        final Term post = term.sub(0);
        final Services services = goal.proof().getServices();

        // diffweaken
        RuleApp diffweaken = goal.indexOfTaclets().lookup(new Name("diffweaken")); 
        //= getRuleAppOf(new Name("diffweaken"), pos, goal);
        //@todo optimize first check if post contained in conjunct of invariant
        System.out.println("HYPO: " + diffweaken.rule().name());
        Sequent weakened = DiffSatFeature.changedSequent(pos, goal.sequent(),
                DLUniversalClosureOp.DL_UNIVERSAL_CLOSURE.universalClosure(
                        system, TermBuilder.DF.imp(invariant, post), null, services));
        HypotheticalProvability result = HypotheticalProvabilityFeature
                .provable(goal.proof(), weakened, MAX_STEPS, timeout);
        System.out.println("HYPO: " + diffweaken.rule().name() + " " + result);
        switch (result) {
        case PROVABLE:
            diffWeakenCache.put(term, LongRuleAppCost.ZERO_COST);
            // if weakening provably successful, only use weakening
            return LongRuleAppCost.ZERO_COST;
        case DISPROVABLE:
        case ERROR:
            diffWeakenCache.put(term, TopRuleAppCost.INSTANCE);
            return TopRuleAppCost.INSTANCE;
        case UNKNOWN:
        case TIMEOUT:
            return HypotheticalProvabilityFeature.TIMEOUT_COST;
        default:
            throw new AssertionError("enum known");
        }
    }

    // caching

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
