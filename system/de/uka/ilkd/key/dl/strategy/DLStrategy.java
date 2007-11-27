/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel, André Platzer                 *
 *   quesel@informatik.uni-oldenburg.de                                    *
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

package de.uka.ilkd.key.dl.strategy;

import java.util.Map;
import java.util.WeakHashMap;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.options.DLOptionBean.ApplyRules;
import de.uka.ilkd.key.dl.rules.DebugRule;
import de.uka.ilkd.key.dl.rules.EliminateQuantifierRule;
import de.uka.ilkd.key.dl.rules.EliminateQuantifierRuleWithContext;
import de.uka.ilkd.key.dl.rules.FindInstanceRule;
import de.uka.ilkd.key.dl.rules.ReduceRule;
import de.uka.ilkd.key.dl.rules.VisualizationRule;
import de.uka.ilkd.key.dl.strategy.features.FOFormula;
import de.uka.ilkd.key.dl.strategy.features.FOSequence;
import de.uka.ilkd.key.dl.strategy.features.FindInstanceTest;
import de.uka.ilkd.key.dl.strategy.features.HypotheticalProvabilityFeature;
import de.uka.ilkd.key.dl.strategy.features.KeYBeyondFO;
import de.uka.ilkd.key.dl.strategy.features.LoopInvariantRuleDispatchFeature;
import de.uka.ilkd.key.dl.strategy.features.OnlyOncePerBranchFeature;
import de.uka.ilkd.key.dl.strategy.features.ReduceFeature;
import de.uka.ilkd.key.dl.strategy.features.SimplifyFeature;
import de.uka.ilkd.key.dl.strategy.features.SwitchFeature;
import de.uka.ilkd.key.dl.strategy.features.TimeoutTestApplicationFeature;
import de.uka.ilkd.key.dl.strategy.features.SwitchFeature.Case;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Node;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.AbstractFeatureStrategy;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.Strategy;
import de.uka.ilkd.key.strategy.StrategyFactory;
import de.uka.ilkd.key.strategy.StrategyProperties;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.AgeFeature;
import de.uka.ilkd.key.strategy.feature.AutomatedRuleFeature;
import de.uka.ilkd.key.strategy.feature.CheckApplyEqFeature;
import de.uka.ilkd.key.strategy.feature.ConditionalFeature;
import de.uka.ilkd.key.strategy.feature.ConstraintStrengthenFeature;
import de.uka.ilkd.key.strategy.feature.ConstraintStrengthenFeatureUC;
import de.uka.ilkd.key.strategy.feature.ContainsQuantifierFeature;
import de.uka.ilkd.key.strategy.feature.CountMaxDPathFeature;
import de.uka.ilkd.key.strategy.feature.CountPosDPathFeature;
import de.uka.ilkd.key.strategy.feature.EqNonDuplicateAppFeature;
import de.uka.ilkd.key.strategy.feature.Feature;
import de.uka.ilkd.key.strategy.feature.FormulaAddedByRuleFeature;
import de.uka.ilkd.key.strategy.feature.LeftmostNegAtomFeature;
import de.uka.ilkd.key.strategy.feature.MatchedIfFeature;
import de.uka.ilkd.key.strategy.feature.NonDuplicateAppFeature;
import de.uka.ilkd.key.strategy.feature.NotBelowQuantifierFeature;
import de.uka.ilkd.key.strategy.feature.NotWithinMVFeature;
import de.uka.ilkd.key.strategy.feature.PurePosDPathFeature;
import de.uka.ilkd.key.strategy.feature.RuleSetDispatchFeature;
import de.uka.ilkd.key.strategy.feature.ScaleFeature;
import de.uka.ilkd.key.strategy.feature.SimplifyBetaCandidateFeature;
import de.uka.ilkd.key.strategy.feature.SimplifyReplaceKnownCandidateFeature;
import de.uka.ilkd.key.strategy.feature.SumFeature;
import de.uka.ilkd.key.strategy.feature.TermSmallerThanFeature;
import de.uka.ilkd.key.strategy.termProjection.AssumptionProjection;
import de.uka.ilkd.key.strategy.termProjection.TermBuffer;

/**
 * @author jdq
 * @author ap
 */
public class DLStrategy extends AbstractFeatureStrategy {

    private final Feature completeF;

    private final Feature approvalF;

    private enum FirstOrder {
        NOT_FO, FO;
    }

    private enum CounterExample {
        NO_CE, CE;
    }

    private Map<Node, FirstOrder> foCache = new WeakHashMap<Node, FirstOrder>();

    private Map<Node, CounterExample> ceCache = new WeakHashMap<Node, CounterExample>();
    
    private boolean stopOnFirstCE;

    private boolean blockAllRules = false;

    protected DLStrategy(Proof p_proof) {
        this(p_proof, false);
    }
    protected DLStrategy(Proof p_proof, boolean stopOnFirstCE) {
        super(p_proof);
        this.stopOnFirstCE=stopOnFirstCE;

        final RuleSetDispatchFeature d = RuleSetDispatchFeature.create();

        bindRuleSet(d, "closure", -9000);
        bindRuleSet(d, "concrete", -10000);
        bindRuleSet(d, "alpha", -7000);
        bindRuleSet(d, "delta", -6000);
        bindRuleSet(d, "pull_out_quantifier", 5000);

        bindRuleSet(d, "beta",
                add(longConst(-2500), ifZero(
                        SimplifyBetaCandidateFeature.INSTANCE, SumFeature
                                .createSum(new Feature[] {
                                        longConst(-1070),
                                        ifZero(PurePosDPathFeature.INSTANCE,
                                                longConst(-200)),
                                        // ifZero (
                                        // ContainsQuantifierFeature.INSTANCE,
                                        // longConst ( -2000 ) ),
                                        ScaleFeature.createScaled(
                                                CountPosDPathFeature.INSTANCE,
                                                -3.0),
                                        ScaleFeature.createScaled(
                                                CountMaxDPathFeature.INSTANCE,
                                                10.0) }))));

        final Feature introducedByGammaF = FormulaAddedByRuleFeature
                .create(getFilterFor(new String[] { "gamma",
                        "gamma_destructive" }));
        bindRuleSet(d, "test_gen", inftyConst());

        if (DLOptionBean.INSTANCE.getApplyGammaRules() == ApplyRules.ALWAYS) {
            bindRuleSet(d, "gamma", ifZero(introducedByGammaF, longConst(0),
                    add(
                            ifZero(NonDuplicateAppFeature.INSTANCE,
                                    longConst(-200)), longConst(-3250))));

            bindRuleSet(d, "gamma_destructive", ifZero(introducedByGammaF,
                    longConst(-5000)));
        } else if (DLOptionBean.INSTANCE.getApplyGammaRules() == ApplyRules.NEVER) {
            bindRuleSet(d, "gamma", inftyConst());

            bindRuleSet(d, "gamma_destructive", inftyConst());
        } else if (DLOptionBean.INSTANCE.getApplyGammaRules() == ApplyRules.ONLY_TO_MODALITIES) {
            bindRuleSet(d, "gamma", ifZero(FOFormula.INSTANCE, inftyConst(),
                    ifZero(introducedByGammaF, longConst(0), add(ifZero(
                            NonDuplicateAppFeature.INSTANCE, longConst(-200)),
                            longConst(-3250)))));

            bindRuleSet(d, "gamma_destructive", ifZero(FOFormula.INSTANCE,
                    inftyConst(), ifZero(introducedByGammaF, longConst(-5000))));
        } else {
            throw new IllegalStateException("For gamma rules the state "
                    + DLOptionBean.INSTANCE.getApplyGammaRules()
                    + " is not supported yet.");
        }

        bindRuleSet(d, "replace_known", SumFeature
                .createSum(new Feature[] {
                        SimplifyReplaceKnownCandidateFeature.INSTANCE,
                        ifZero(ConstraintStrengthenFeature.INSTANCE, add(
                                ifZero(SimplifyBetaCandidateFeature.INSTANCE,
                                        inftyConst()),
                                NotBelowQuantifierFeature.INSTANCE,
                                LeftmostNegAtomFeature.INSTANCE),
                                longConst(-800)),
                        longConst(-4000),
                        ScaleFeature.createScaled(
                                CountMaxDPathFeature.INSTANCE, 10.0) }));

        final TermBuffer equation = new TermBuffer();
        bindRuleSet(d, "apply_equations", add(ifZero(MatchedIfFeature.INSTANCE,
                add(CheckApplyEqFeature.INSTANCE, let(equation,
                        AssumptionProjection.create(0), TermSmallerThanFeature
                                .create(sub(equation, 1), sub(equation, 0))))),
                ifZero(ConstraintStrengthenFeature.INSTANCE, add(ifZero(
                        SimplifyBetaCandidateFeature.INSTANCE, inftyConst()),
                        NotBelowQuantifierFeature.INSTANCE, ifZero(
                                ContainsQuantifierFeature.INSTANCE,
                                inftyConst()))), longConst(-4000)));

        bindRuleSet(d, "order_terms", add(TermSmallerThanFeature.create(
                instOf("commEqLeft"), instOf("commEqRight")), longConst(-8000)));

        bindRuleSet(d, "simplify_literals", ifZero(
                ConstraintStrengthenFeature.INSTANCE, longConst(-4000),
                longConst(-8000)));

        bindRuleSet(d, "try_apply_subst", add(
                EqNonDuplicateAppFeature.INSTANCE, longConst(-10000)));

        // delete cast
        bindRuleSet(d, "cast_deletion", ifZero(
                implicitCastNecessary(instOf("castedTerm")), longConst(-5000),
                inftyConst()));

        // disallow simplification of polynomials and inequations here
        // (these rules need guidance that is not present in this strategy)
        bindRuleSet(d, "polySimp_expand", inftyConst());
        bindRuleSet(d, "polySimp_directEquations", inftyConst());
        bindRuleSet(d, "polySimp_saturate", inftyConst());
        bindRuleSet(d, "polySimp_applyEq", inftyConst());
        bindRuleSet(d, "polySimp_applyEqRigid", inftyConst());
        bindRuleSet(d, "inEqSimp_expand", inftyConst());
        bindRuleSet(d, "inEqSimp_directInEquations", inftyConst());
        bindRuleSet(d, "inEqSimp_saturate", inftyConst());
        bindRuleSet(d, "inEqSimp_propagation", inftyConst());
        bindRuleSet(d, "inEqSimp_special_nonLin", inftyConst());
        bindRuleSet(d, "inEqSimp_nonLin", inftyConst());

        bindRuleSet(d, "system_invariant", inftyConst());
        bindRuleSet(d, "query_normalize", inftyConst());

        
        bindRuleSet(d, "loop_invariant", LoopInvariantRuleDispatchFeature.INSTANCE);
        
        bindRuleSet(d, "mathematica_reduce", add(ifZero(ReduceFeature.INSTANCE,
                longConst(4999), inftyConst()), (MathSolverManager
                .isSimplifierSet()) ? longConst(0) : inftyConst()));

        bindRuleSet(d, "mathematica_simplify", add(SimplifyFeature.INSTANCE,
                (MathSolverManager.isSimplifierSet()) ? longConst(0)
                        : inftyConst()));

        bindRuleSet(d, "invariant_weaken", new SwitchFeature(
                HypotheticalProvabilityFeature.INSTANCE,
                new Case(longConst(0), longConst(-2000)),
                new Case(longConst(1), longConst(1000000)),
                new Case(inftyConst(), inftyConst())));

        bindRuleSet(d, "invariant_diff", new SwitchFeature(
                HypotheticalProvabilityFeature.INSTANCE,
                new Case(longConst(0), longConst(-1000)),
                new Case(longConst(1), longConst(10000000)),
                new Case(inftyConst(), inftyConst())));


        if (DLOptionBean.INSTANCE.isNormalizeEquations()) {
            bindRuleSet(d, "inequation_normalization", -2000);
        } else {
            bindRuleSet(d, "inequation_normalization", inftyConst());
        }

        // final Feature simplifierF = ifZero(selectSimplifier(-10000),
        // EqNonDuplicateAppFeature.INSTANCE);
        final Feature simplifierF = selectSimplifier(-10000);

        final Feature duplicateF = ifZero(NonDuplicateAppFeature.INSTANCE,
                longConst(0), inftyConst());
        Feature reduceSequence = null;

        if (DLOptionBean.INSTANCE.isCallReduce()) {
            if (DLOptionBean.INSTANCE.isUseTimeoutStrategy()) {
                /*
                 * basic idea of the following statement: - check for options -
                 * if applying timeoutstrategy: -- try to reduce -- if
                 * successful --- rate the result (cheap for good rating,
                 * unafordable elsewise) -- if not successful in time --- try to
                 * find a counter example (if none is found high costs,
                 * unafordable otherwise)
                 */
                reduceSequence = ConditionalFeature.createConditional(
                        ReduceRule.INSTANCE,
                        ifZero(FOSequence.INSTANCE, new SwitchFeature(
                                TimeoutTestApplicationFeature.INSTANCE,
                                new Case(longConst(0), longConst(-20000)),
                                new Case(longConst(1), longConst(20000)),
                                new Case(inftyConst(), inftyConst())),
                                inftyConst()));
            } else {
                reduceSequence = ConditionalFeature.createConditional(
                        ReduceRule.INSTANCE, add(
                                OnlyOncePerBranchFeature.INSTANCE,
                                KeYBeyondFO.INSTANCE, FOSequence.INSTANCE));
            }
        } else {
            reduceSequence = ConditionalFeature.createConditional(
                    ReduceRule.INSTANCE, inftyConst());
        }

        final Feature eliminateQuantifier = ConditionalFeature
                .createConditional(EliminateQuantifierRule.INSTANCE,
                        inftyConst());
        // final Feature eliminateQuantifier = ConditionalFeature
        // .createConditional(EliminateQuantifierRule.INSTANCE, add(
        // longConst(5000), FOFormsContainingSymbol.INSTANCE));

        final Feature debugRule = ConditionalFeature.createConditional(
                DebugRule.INSTANCE, inftyConst());
        final Feature visualRule = ConditionalFeature.createConditional(
                VisualizationRule.INSTANCE, inftyConst());
        final Feature contextElimRule = ConditionalFeature.createConditional(
                EliminateQuantifierRuleWithContext.INSTANCE, inftyConst());

        final Feature findInstanceRule = ConditionalFeature.createConditional(
                FindInstanceRule.INSTANCE, inftyConst());

        final Feature ifMatchedF = ifZero(MatchedIfFeature.INSTANCE,
                longConst(+1));

        final Feature noQuantifierInstantition = ifZero(
                ConstraintStrengthenFeatureUC.create(p_proof), inftyConst());

        completeF = SumFeature
                .createSum(new Feature[] { AutomatedRuleFeature.INSTANCE,
                        NotWithinMVFeature.INSTANCE, simplifierF, duplicateF,
                        ifMatchedF, d, AgeFeature.INSTANCE, reduceSequence,
                        debugRule, visualRule, contextElimRule, eliminateQuantifier,
                        findInstanceRule, noQuantifierInstantition });

        approvalF = duplicateF;
    }

    public Name name() {
        return new Name("DLStrategy");
    }

    /**
     * Evaluate the cost of a <code>RuleApp</code>.
     * 
     * @return the cost of the rule application expressed as a
     *         <code>RuleAppCost</code> object.
     *         <code>TopRuleAppCost.INSTANCE</code> indicates that the rule
     *         shall not be applied at all (it is discarded by the strategy).
     */
    public RuleAppCost computeCost(RuleApp app, PosInOccurrence pio, Goal goal) {
        if (veto(app, pio, goal)) {
            RuleAppCost compute = completeF.compute(app, pio, goal);
            return compute;
        } else {
            return TopRuleAppCost.INSTANCE;
        }
    }

    /**
     * Re-Evaluate a <code>RuleApp</code>. This method is called immediately
     * before a rule is really applied
     * 
     * @return true iff the rule should be applied, false otherwise
     */
    public boolean isApprovedApp(RuleApp app, PosInOccurrence pio, Goal goal) {
        if (veto(app, pio, goal)) {
            return !(approvalF.compute(app, pio, goal) instanceof TopRuleAppCost);
        } else {
            return false;
        }
    }
    
    public boolean foundCounterexample() {
        return blockAllRules;
    }

    /**
     * TODO jdq documentation since Sep 6, 2007
     * 
     * @param app
     * @param pio
     * @param goal
     */
    private boolean veto(RuleApp app, PosInOccurrence pio, Goal goal) {
        if (blockAllRules) {
            return false;
        }
        if (((foCache.containsKey(goal.node()) && foCache.get(goal.node()) == FirstOrder.FO) || FOSequence.INSTANCE
                .compute(app, pio, goal) == LongRuleAppCost.ZERO_COST)) {
            foCache.put(goal.node(), FirstOrder.FO);
            if (DLOptionBean.INSTANCE.isStopAtFO()) {
                return false;
            }
            if (DLOptionBean.INSTANCE.isUseFindInstanceTest()) {
                CounterExample cached = getFirstInCacheUntilBranch(goal
                        .node(), ceCache);
                if (cached != null) {
                    ceCache.put(goal.node(), cached);
                    return cached != CounterExample.CE;
                } else if (FindInstanceTest.INSTANCE.compute(app, pio, goal) == TopRuleAppCost.INSTANCE) {
                    System.out.println("Found CE");//XXX 
                    ceCache.put(goal.node(), CounterExample.CE);
                    if (stopOnFirstCE) {
                        blockAllRules = true;
                    }
                    return false;
                } else {
                    ceCache.put(goal.node(), CounterExample.NO_CE);
                }
            }
        } else {
            foCache.put(goal.node(), FirstOrder.NOT_FO);
        }
        return true;
    }

    /**
     * @return
     */
    public static <S> S getFirstInCacheUntilBranch(Node node, Map<Node, S> cache) {
        if (cache.containsKey(node)) {
            return cache.get(node);
        }
        if (node.root() || node.parent().root() || node.parent().childrenCount() > 1) {
            return null;
        }
        return getFirstInCacheUntilBranch(node.parent(), cache);
    }

    @Override
    protected RuleAppCost instantiateApp(RuleApp app, PosInOccurrence pio,
            Goal goal) {
        return TopRuleAppCost.INSTANCE;
    }

    public static class Factory extends StrategyFactory {
        public static final Factory INSTANCE = new Factory();
        public Factory() {

        }

        @Override
        public Strategy create(Proof p_proof,
                StrategyProperties strategyProperties) {
            return new DLStrategy(p_proof);
        }
        
        public Strategy create(Proof p_proof,
                StrategyProperties strategyProperties, boolean stopOnFirstCE) {
            return new DLStrategy(p_proof, stopOnFirstCE);
        }

        public Name name() {
            return new Name("DLStrategy");
        }
    }
}
