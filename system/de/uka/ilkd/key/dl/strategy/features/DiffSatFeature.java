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

import de.uka.ilkd.key.dl.model.DLStatementBlock;
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
import de.uka.ilkd.key.logic.op.SchemaVariable;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Node;
import de.uka.ilkd.key.proof.TacletFilter;
import de.uka.ilkd.key.rule.ListOfTacletApp;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.rule.Taclet;
import de.uka.ilkd.key.rule.TacletApp;
import de.uka.ilkd.key.rule.inst.IllegalInstantiationException;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;
import de.uka.ilkd.key.strategy.termProjection.ProjectionToTerm;

/**
 * DiffSat strategy.
 * 
 * @author ap
 */
public class DiffSatFeature implements Feature {

    private static final int MAX_STEPS = HypotheticalProvabilityFeature.MAX_HYPOTHETICAL_RULE_APPLICATIONS;

    private Map<Node, Long> branchingNodesAlreadyTested = new WeakHashMap<Node, Long>();

    /**
     * Remembers diffinds for the given [D]F modality.
     * diffAugCache.get(D).get(A) remembers whether A is diffind for [D] 
     */
    private Map<DiffSystem, Map<Term,RuleAppCost>> diffIndCache = new WeakHashMap<DiffSystem, Map<Term,RuleAppCost>>();

    //public static final DiffSatFeature INSTANCE = new DiffSatFeature();

    /**
     * the default initial timeout, -1 means use
     * DLOptionBean.INSTANCE.getInitialTimeout()
     */
    private final long initialTimeout;

    private final ProjectionToTerm value;
    
    /**
     * @param timeout
     *                the default overall (initial) timeout for the hypothetic
     *                proof
     */
    public DiffSatFeature(long timeout, ProjectionToTerm value) {
        this.initialTimeout = timeout;
        this.value = value;
    }

    public DiffSatFeature(ProjectionToTerm value) {
        this(-1, value);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule.RuleApp,
     *      de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.proof.Goal)
     */
    public RuleAppCost compute(RuleApp myapp, PosInOccurrence pos, Goal goal) {
        System.out.println("check sat " + " for " + myapp.rule().name() + " on " + pos);
        TacletApp app = (TacletApp) myapp;
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

        return diffSat(app, pos, goal, timeout);
    }

    /**
     * Determines if diffind works for the given augmentation according to DiffSat.
     * 
     * @param pos
     * @param goal
     * @param timeout
     */
    public RuleAppCost diffSat(TacletApp app, PosInOccurrence pos, Goal goal,
            long timeout) {
        Term term = pos.subTerm();
        // unbox from update prefix
        while (term.op() instanceof QuanUpdateOperator) {
            term = ((QuanUpdateOperator) term.op()).target(term);
        }
        if (!(term.op() instanceof Modality && term.javaBlock() != null
                && term.javaBlock() != JavaBlock.EMPTY_JAVABLOCK && term
                .javaBlock().program() instanceof StatementBlock)) {
            throw new IllegalArgumentException("inapplicable to " + pos);
        }
        final DiffSystem system = (DiffSystem) ((StatementBlock) term
                .javaBlock().program()).getChildAt(0);
        final Term invariant = system.getInvariant();
        final Term post = term.sub(0);
        final Services services = goal.proof().getServices();

        RuleApp diffind = goal.indexOfTaclets().lookup(new Name("diffind"));
        Term candidate = (Term) app.instantiations().lookupValue(
                new Name("augmented"));
        if (candidate == null) {
            System.out.println("\tno instantiation "
                    + candidate + " for SV 'augmented'");
            if (false) throw new IllegalInstantiationException("Invalid instantiation "
                    + candidate + " for SV 'augmented' in " + app.instantiations());
            candidate = value.toTerm(app, pos, goal);
        }
        if (TopRuleAppCost.INSTANCE.equals(get(system, candidate))) {
            return TopRuleAppCost.INSTANCE;
        }

        // diffind
        System.out.println("HYPO: " + diffind.rule().name() + " initial");
        // diffind:"Invariant Initially Valid"
        Sequent initial = changedSequent(pos, goal.sequent(), TermBuilder.DF.imp(invariant,
                candidate));
        HypotheticalProvability result = HypotheticalProvabilityFeature
                .provable(goal.proof(), initial, MAX_STEPS, timeout);
        System.out.println("HYPO: " + diffind.rule().name() + " initial " + result);
        //@todo cache with goal.sequent()
        switch (result) {
        case PROVABLE:
            break;
        case ERROR:
        case DISPROVABLE:
            // resultCache.put(firstNodeAfterBranch,
            // TopRuleAppCost.INSTANCE);
            return TopRuleAppCost.INSTANCE;
        case UNKNOWN:
        case TIMEOUT:
            // resultCache.put(firstNodeAfterBranch,
            // LongRuleAppCost.create(1));
            return HypotheticalProvabilityFeature.TIMEOUT_COST;
        default:
            throw new AssertionError("enum known");
        }

        // diffind:"ODE Preserves Invariant"
        if (get(system, candidate) != null) {
            return get(system, candidate);
        }
        System.out.println("HYPO: " + diffind.rule().name() + " step");
        Term augTerm = de.uka.ilkd.key.logic.TermFactory.DEFAULT.createProgramTerm(
                    term.op(),
                    term.javaBlock(),
                    candidate);
        Sequent step = changedSequent(pos, goal.sequent(),
                DLUniversalClosureOp.DL_UNIVERSAL_CLOSURE.universalClosure(
                        system,
                        DiffInd.DIFFIND.calculate(augTerm, null, services), null,
                        services));
        result = HypotheticalProvabilityFeature.provable(goal.proof(), step, MAX_STEPS,
                timeout);
        System.out.println("HYPO: " + diffind.rule().name() + " step " + result);
        switch (result) {
        case PROVABLE:
            put(system, candidate, LongRuleAppCost.ZERO_COST);
            return LongRuleAppCost.ZERO_COST;
        case ERROR:
        case DISPROVABLE:
            put(system, candidate, TopRuleAppCost.INSTANCE);
            return TopRuleAppCost.INSTANCE;
        case UNKNOWN:
        case TIMEOUT:
            // resultCache.put(firstNodeAfterBranch,
            // LongRuleAppCost.create(1));
            return HypotheticalProvabilityFeature.TIMEOUT_COST;
        default:
            throw new AssertionError("enum known");
        }
    }

    private RuleAppCost get(DiffSystem system, Term candidate) {
        Map<Term,RuleAppCost> cache = diffIndCache.get(system);
        return cache == null ? null : cache.get(candidate);
    }
    private RuleAppCost put(DiffSystem system, Term candidate, RuleAppCost cost) {
        Map<Term,RuleAppCost> cache = diffIndCache.get(system);
        if (cache == null)
            cache = new WeakHashMap<Term,RuleAppCost>(10);
        RuleAppCost old = cache.put(candidate, cost);
        diffIndCache.put(system, cache);
        return old;
    }

        
    protected static Sequent changedSequent(PosInOccurrence pos, Sequent seq,
            Term fml) {
        return seq.changeFormula(new ConstrainedFormula(fml, pos
                .constrainedFormula().constraint()), pos).sequent();
    }

    private static RuleApp getRuleAppOf(Name tacletname, PosInOccurrence pos,
            Goal goal) {
        // Main.getInstance().mediator().getInteractiveProver().getAppsForName(goal,
        // tacletname.toString(), pos);
        // goal.indexOfTaclets().lookup(name)
        ListOfTacletApp l = goal.ruleAppIndex().getTacletAppAt(
                nameFilter(tacletname), pos, goal.proof().getServices(),
                goal.getClosureConstraint());
        assert l.size() <= 1 : "Names are unique, hence there is at most one taclet app";
        return l.head();
    }

    private static TacletFilter nameFilter(final Name name) {
        return new TacletFilter() {
            protected boolean filter(Taclet taclet) {
                return taclet.name().equals(name);
            }
        };
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
