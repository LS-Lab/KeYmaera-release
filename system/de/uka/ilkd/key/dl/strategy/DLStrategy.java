/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel, Andre Platzer                 *
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
import java.util.Set;
import java.util.WeakHashMap;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.options.DLOptionBean.ApplyRules;
import de.uka.ilkd.key.dl.options.DLOptionBean.CounterexampleTest;
import de.uka.ilkd.key.dl.options.DLOptionBean.DiffSat;
import de.uka.ilkd.key.dl.rules.DebugRule;
import de.uka.ilkd.key.dl.rules.EliminateExistentialQuantifierRule;
import de.uka.ilkd.key.dl.rules.EliminateQuantifierRule;
import de.uka.ilkd.key.dl.rules.EliminateQuantifierRuleWithContext;
import de.uka.ilkd.key.dl.rules.FindInstanceRule;
import de.uka.ilkd.key.dl.rules.FindTransitionRule;
import de.uka.ilkd.key.dl.rules.ReduceRule;
import de.uka.ilkd.key.dl.rules.SumOfSquaresRule;
import de.uka.ilkd.key.dl.rules.VisualizationRule;
import de.uka.ilkd.key.dl.strategy.features.AnnotationList;
import de.uka.ilkd.key.dl.strategy.features.DiffIndCandidates;
import de.uka.ilkd.key.dl.strategy.features.DiffInvariantPresentFeature;
import de.uka.ilkd.key.dl.strategy.features.DiffSatFeature;
import de.uka.ilkd.key.dl.strategy.features.DiffWeakenFeature;
import de.uka.ilkd.key.dl.strategy.features.EliminateExistentialApproveFeature;
import de.uka.ilkd.key.dl.strategy.features.FOFormula;
import de.uka.ilkd.key.dl.strategy.features.FOSequence;
import de.uka.ilkd.key.dl.strategy.features.FindInstanceTest;
import de.uka.ilkd.key.dl.strategy.features.FindTransitionTest;
import de.uka.ilkd.key.dl.strategy.features.HypotheticalProvabilityFeature;
import de.uka.ilkd.key.dl.strategy.features.KeYBeyondFO;
import de.uka.ilkd.key.dl.strategy.features.LoopInvariantRuleDispatchFeature;
import de.uka.ilkd.key.dl.strategy.features.ODESolvableFeature;
import de.uka.ilkd.key.dl.strategy.features.OnlyOncePerBranchFeature;
import de.uka.ilkd.key.dl.strategy.features.PostDiffStrengthFeature;
import de.uka.ilkd.key.dl.strategy.features.ReduceFeature;
import de.uka.ilkd.key.dl.strategy.features.SimplifyFeature;
import de.uka.ilkd.key.dl.strategy.features.SwitchFeature;
import de.uka.ilkd.key.dl.strategy.features.TimeoutTestApplicationFeature;
import de.uka.ilkd.key.dl.strategy.features.SwitchFeature.Case;
import de.uka.ilkd.key.dl.strategy.termProjection.UltimatePostProjection;
import de.uka.ilkd.key.logic.IteratorOfConstrainedFormula;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Metavariable;
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
import de.uka.ilkd.key.strategy.feature.instantiator.RuleAppBuffer;
import de.uka.ilkd.key.strategy.termProjection.AssumptionProjection;
import de.uka.ilkd.key.strategy.termProjection.TermBuffer;

/**
 * Strategy for proving dL formulas with hybrid programs.
 * 
 * @author jdq
 * @author ap
 * @todo allow equality rewriting
 */
public class DLStrategy extends AbstractFeatureStrategy implements
        RealtimeStrategy {

    private final Feature vetoF;

    private final Feature counterexampleF;

    private final Feature completeF;

    private final Feature approvalF;

    private final Feature instantiationF;

    private enum FirstOrder {
        NOT_FO, FO;
    }

    private enum CounterExample {
        NO_CE, CE;
    }

    private Map<Node, FirstOrder> foCache = new WeakHashMap<Node, FirstOrder>();

    private Map<Node, CounterExample> ceCache = new WeakHashMap<Node, CounterExample>();

    private boolean stopOnFirstCE;

    /**
     * whether to block ALL further rule applications.
     */
    private boolean blockAllRules = false;

    private long timeout;

    private boolean counterexample = false;

    protected DLStrategy(Proof p_proof) {
        this(p_proof, false);
    }

    protected DLStrategy(Proof p_proof, boolean stopOnFirstCE) {
        this(p_proof, stopOnFirstCE, -1);
    }

    /**
     * 
     * @param p_proof
     * @param stopOnFirstCE
     *                whether to make a full stop and apply no more rules at all
     *                as soon as the first counterexample occurs. Otherwise,
     *                branches without counterexamples are still worked on even
     *                though the overall proof cannot close.
     * @param timeout
     *                the intended time constraint for this strategy
     */
    protected DLStrategy(Proof p_proof, boolean stopOnFirstCE, long timeout) {
        super(p_proof);
        this.stopOnFirstCE = stopOnFirstCE;
        this.timeout = timeout;

        final RuleSetDispatchFeature d = RuleSetDispatchFeature.create();

        bindRuleSet(d, "closure", -30000);
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

        bindRuleSet(d, "split_gen", ifZero(isAnnotated("generalize"),
                longConst(-2000), inftyConst()));

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

        bindRuleSet(d, "loop_invariant",
                LoopInvariantRuleDispatchFeature.INSTANCE);

        bindRuleSet(d, "mathematica_reduce", add(ifZero(ReduceFeature.INSTANCE,
                longConst(4999), inftyConst()), (MathSolverManager
                .isSimplifierSet()) ? longConst(0) : inftyConst()));

        bindRuleSet(d, "mathematica_simplify", add(SimplifyFeature.INSTANCE,
                (MathSolverManager.isSimplifierSet()) ? longConst(0)
                        : inftyConst()));

        if (DLOptionBean.INSTANCE.isNormalizeEquations()) {
            bindRuleSet(d, "inequation_normalization", -4000);
        } else {
            bindRuleSet(d, "inequation_normalization", inftyConst());
        }

        // final Feature simplifierF = ifZero(selectSimplifier(-10000),
        // EqNonDuplicateAppFeature.INSTANCE);
        final Feature simplifierF = selectSimplifier(-10000);

        final Feature duplicateF = ifZero(NonDuplicateAppFeature.INSTANCE,
                longConst(0), inftyConst());
        Feature reduceSequence = null;

        setupDiffSatStrategy(d);

        if (DLOptionBean.INSTANCE.isCallReduce()) {
            if (DLOptionBean.INSTANCE.isUseTimeoutStrategy()) {
                /*
                 * basic idea of the following statement: - check for options -
                 * if applying timeout strategy: -- try to reduce -- if
                 * successful --- rate the result (cheap for good rating,
                 * unaffordable otherwisewise) -- if not successful in time ---
                 * try to find a counter example (if none is found high costs,
                 * unaffordable if counterexample has been found)
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

        // if the sequent is first order, we can try to apply the rule...
        // the strat should still avoid applying it, if there is another
        // goal containing a relevant MV that is not totally first order
        final Feature eliminateExistentialQuantifier = ConditionalFeature
                .createConditional(EliminateExistentialQuantifierRule.INSTANCE,
                        SumFeature.createSum(new Feature[] { longConst(19000),
                                FOSequence.INSTANCE }));
        // final Feature eliminateQuantifier = ConditionalFeature
        // .createConditional(EliminateQuantifierRule.INSTANCE, add(
        // longConst(5000), FOFormsContainingSymbol.INSTANCE));

        final Feature contextElimRule = ConditionalFeature.createConditional(
                EliminateQuantifierRuleWithContext.INSTANCE, inftyConst());

        final Feature excludeRules = SumFeature.createSum(new Feature[] {
                ConditionalFeature.createConditional(FindInstanceRule.INSTANCE,
                        inftyConst()),
                ConditionalFeature.createConditional(DebugRule.INSTANCE,
                        inftyConst()),
                ConditionalFeature.createConditional(SumOfSquaresRule.INSTANCE,
                        inftyConst()),
                ConditionalFeature.createConditional(
                        VisualizationRule.INSTANCE, inftyConst()),
                ConditionalFeature.createConditional(
                        FindTransitionRule.INSTANCE, inftyConst()) });

        final Feature ifMatchedF = ifZero(MatchedIfFeature.INSTANCE,
                longConst(+1));

        final Feature noQuantifierInstantition = ifZero(
                ConstraintStrengthenFeatureUC.create(p_proof), inftyConst());

        vetoF = duplicateF;

        counterexampleF = FindTransitionTest.INSTANCE;

        completeF = SumFeature.createSum(new Feature[] {
                AutomatedRuleFeature.INSTANCE, NotWithinMVFeature.INSTANCE,
                simplifierF, duplicateF, ifMatchedF, d, AgeFeature.INSTANCE,
                reduceSequence, contextElimRule, eliminateQuantifier,
                excludeRules, noQuantifierInstantition,
                eliminateExistentialQuantifier });

        approvalF = setupApprovalF(p_proof);

        instantiationF = setupInstantiationF(p_proof);
    }

    /**
     * DiffSat strategy.
     * 
     * @author ap
     */
    private void setupDiffSatStrategy(final RuleSetDispatchFeature d) {
        if (DLOptionBean.INSTANCE.getDiffSat() != DiffSat.BLIND) {
            bindRuleSet(d, "diff_solve", ifZero(ODESolvableFeature.INSTANCE,
                    longConst(4000), inftyConst()));
        }
        if (DLOptionBean.INSTANCE.getCounterexampleTest().compareTo(
                CounterexampleTest.TRANSITIONS) >= 0) {
            // @ don't try any diff rules if there is a counterexample
            // transition
            bindRuleSet(d, "diff_rule", FindTransitionTest.INSTANCE
            // @todo report non-first-order counterexamples to
            // foundCounterexample()
            );
        }

        if (DLOptionBean.INSTANCE.getDiffSat().compareTo(DiffSat.SIMPLE) >= 0) {
            bindRuleSet(d, "invariant_weaken", ifZero(isAnnotated("weaken"),
                    longConst(-6000), ifZero(ODESolvableFeature.INSTANCE,
                            inftyConst(), new SwitchFeature(
                                    DiffWeakenFeature.INSTANCE, new Case(
                                            longConst(0), longConst(-6000)),
                                    // reject if it doesn't help, but retry
                                    // costs
                                    new Case(longConst(1), longConst(5000)),
                                    new Case(inftyConst(), inftyConst())))));
            bindRuleSet(
                    d,
                    "invariant_diff",
                    ifZero(
                            isAnnotated("diffind"),
                            longConst(-6000),
                            ifZero(
                                    PostDiffStrengthFeature.INSTANCE,
                                    longConst(-4000),
                                    ifZero(
                                            ODESolvableFeature.INSTANCE,
                                            inftyConst(),
                                            ifZero(
                                                    DiffWeakenFeature.INSTANCE,
                                                    inftyConst(),
                                                    DLOptionBean.INSTANCE
                                                            .getDiffSat()
                                                            .compareTo(
                                                                    DiffSat.DIFF) >= 0 ? // re-evaluate
                                                    // feature
                                                    // at
                                                    // least
                                                    // after
                                                    // diffstrengthen
                                                    // reject if it doesn't
                                                    // help, but
                                                    // retry costs
                                                    longConst(6000)
                                                            : // only directly
                                                                // check
                                                            // diffind
                                                            new SwitchFeature(
                                                                    new HypotheticalProvabilityFeature(
                                                                            DLOptionBean.INSTANCE
                                                                                    .getDiffSatTimeout()),
                                                                    new Case(
                                                                            longConst(0),
                                                                            longConst(-4000)),
                                                                    // reject if
                                                                    // it
                                                                    // doesn't
                                                                    // help, but
                                                                    // retry
                                                                    // costs
                                                                    new Case(
                                                                            longConst(1),
                                                                            longConst(6000)),
                                                                    new Case(
                                                                            inftyConst(),
                                                                            inftyConst())))))));
        } else {
            bindRuleSet(d, "invariant_diff", ifZero(isAnnotated("diffind"),
                    longConst(-6000), ifZero(PostDiffStrengthFeature.INSTANCE,
                            longConst(-4000), inftyConst())));
            bindRuleSet(d, "invariant_weaken", isAnnotated("weaken"));
        }

        if (DLOptionBean.INSTANCE.getDiffSat().compareTo(DiffSat.DIFF) >= 0) {
            bindRuleSet(d, "invariant_strengthen", ifZero(
                    PostDiffStrengthFeature.INSTANCE,
                    // recursively strengthening augmentation validity proofs
                    // seems useless
                    inftyConst(), ifZero(isAnnotated("strengthen"),
                            longConst(-2000), ifZero(
                                    ODESolvableFeature.INSTANCE, inftyConst(),
                                    ifZero(DiffWeakenFeature.INSTANCE,
                                            inftyConst(),
                                            // never instantiate when cheaper
                                            // rule successful
                                            longConst(10000)
                                    // go on try to instantiate,
                                    // except when tabooed
                                    )))));
        } else {
            bindRuleSet(d, "invariant_strengthen", ifZero(
                    PostDiffStrengthFeature.INSTANCE,
                    // recursively strengthening augmentation validity proofs
                    // seems useless
                    inftyConst(), isAnnotated("strengthen")));
        }
    }

    // //////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////
    //
    // Feature terms that handle the instantiation of incomplete taclet
    // applications
    //
    // //////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////

    private RuleSetDispatchFeature setupInstantiationF(Proof p_proof) {
        enableInstantiate();
        final RuleSetDispatchFeature d = RuleSetDispatchFeature.create();
        setupDiffSatInstantiationStrategy(d);
        setupAnnotationInstantiationStrategy(d);
        disableInstantiate();
        return d;
    }

    private void setupAnnotationInstantiationStrategy(RuleSetDispatchFeature d) {
        bindRuleSet(d, "split_gen", ifZero(isAnnotated("generalize"),
                instantiate("gen", annotationOf("generalize", true)),
                inftyConst()));
    }

    /**
     * DiffSat instantiation strategy.
     * 
     * @author ap
     */
    private void setupDiffSatInstantiationStrategy(
            final RuleSetDispatchFeature d) {
        if (DLOptionBean.INSTANCE.getDiffSat().compareTo(DiffSat.DIFF) >= 0) {
            final TermBuffer augInst = new TermBuffer();
            final RuleAppBuffer buffy = new RuleAppBuffer();
            bindRuleSet(d, "invariant_strengthen", ifZero(
                    isAnnotated("strengthen"),
                    // /////instantiate("augment", annotationOf("strengthen",
                    // true)),
                    storeRuleApp(buffy,
                            not(sum(augInst, new AnnotationList("strengthen",
                                    false), add(buffy, instantiate("augment",
                                    augInst),
                                    not(not(new DiffInvariantPresentFeature(
                                            augInst))))))),
                    // no annotation
                    ifZero(storeRuleApp(buffy, not(sum(augInst,
                            DiffIndCandidates.INSTANCE, add(buffy, instantiate(
                                    "augment", augInst),
                                    not(new DiffSatFeature(augInst)))))),
                            longConst(-1000), inftyConst())));
        } else {
            final TermBuffer augInst = new TermBuffer();
            final RuleAppBuffer buffy = new RuleAppBuffer();
            bindRuleSet(d, "invariant_strengthen", ifZero(
                    isAnnotated("strengthen"), storeRuleApp(buffy, not(sum(
                            augInst, new AnnotationList("strengthen", false),
                            add(buffy, instantiate("augment", augInst),
                                    not(not(new DiffInvariantPresentFeature(
                                            augInst))))))), inftyConst()));
        }
        if (DLOptionBean.INSTANCE.getDiffSat().compareTo(DiffSat.AUTO) >= 0) {
            final TermBuffer augInst = new TermBuffer();
            final RuleAppBuffer buffy = new RuleAppBuffer();
            // TODO add result caching
            bindRuleSet(
                    d,
                    "loop_invariant_proposal",
                    ifZero(
                            isAnnotated("invariant"),
                            instantiate("inv", annotationOf("invariant", true)),
                            storeRuleApp(
                                    buffy,
                                    ifZero(
                                            add(
                                                    instantiate(
                                                            "inv",
                                                            new UltimatePostProjection(
                                                                    instOf("post"))),
                                                    openCurrentRuleApp(new HypotheticalProvabilityFeature(
                                                            DLOptionBean.INSTANCE
                                                                    .getLoopSatTimeout()))),
                                            longConst(-2000),
                                            ifZero(
                                                    not(sum(
                                                            augInst,
                                                            DiffIndCandidates.INSTANCE,
                                                            add(
                                                                    buffy,
                                                                    instantiate(
                                                                            "inv",
                                                                            augInst),
                                                                    not(openCurrentRuleApp(new HypotheticalProvabilityFeature(
                                                                            DLOptionBean.INSTANCE
                                                                                    .getLoopSatTimeout())))))),
                                                    longConst(-1000),
                                                    inftyConst() // @todo
                                            // use
                                            // large
                                            // constant
                                            // instead
                                            // to
                                            // try
                                            // at
                                            // least
                                            )))));
        } else {
            bindRuleSet(d, "loop_invariant_proposal", ifZero(
                    isAnnotated("invariant"), instantiate("inv", annotationOf(
                            "invariant", true)), inftyConst()));
        }
        bindRuleSet(d, "loop_variant", ifZero(isAnnotated("variant"),
                // @todo rename n in both instantiations when it already occurs
                // elsewhere
                add(instantiate("inv", annotationOf("variant", true, 0, 2)),
                        instantiate("n", annotationOf("variant", true, 1, 2))),
                inftyConst()));
    }

    // //////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////
    //
    // Feature terms that handle the approval of complete taclet applications
    //
    // //////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////

    private Feature setupApprovalF(Proof p_proof) {
        final RuleSetDispatchFeature d = RuleSetDispatchFeature.create();
        setupDiffSatApprovalStrategy(d);
        return SumFeature.createSum(new Feature[] { d,
                EliminateExistentialApproveFeature.INSTANCE });
    }

    /**
     * DiffSat approval strategy.
     * 
     * @author ap
     */
    private void setupDiffSatApprovalStrategy(final RuleSetDispatchFeature d) {
        if (DLOptionBean.INSTANCE.getDiffSat().compareTo(DiffSat.SIMPLE) >= 0) {
            bindRuleSet(d, "invariant_weaken", ifZero(isAnnotated("weaken"),
                    longConst(-6000), new SwitchFeature(
                            DiffWeakenFeature.INSTANCE, new Case(longConst(0),
                                    longConst(-6000)),
                            // reject if it doesn't help, but retry costs
                            new Case(longConst(1), inftyConst()), new Case(
                                    inftyConst(), inftyConst()))));
            bindRuleSet(
                    d,
                    "invariant_diff",
                    ifZero(
                            isAnnotated("diffind"),
                            longConst(-6000),
                            ifZero(
                                    PostDiffStrengthFeature.INSTANCE,
                                    longConst(-4000),
                                    ifZero(
                                            DiffWeakenFeature.INSTANCE,
                                            inftyConst(),
                                            // only directly check diffind
                                            new SwitchFeature(
                                                    new HypotheticalProvabilityFeature(
                                                            DLOptionBean.INSTANCE
                                                                    .getDiffSatTimeout()),
                                                    new Case(longConst(0),
                                                            longConst(-4000)),
                                                    // reject if it doesn't
                                                    // help, but retry costs
                                                    new Case(longConst(1),
                                                            inftyConst()),
                                                    new Case(inftyConst(),
                                                            inftyConst()))))));
        }

        // @todo could use DiffInvariantPresentFeature to ensure diff_strengthen
        // isn't used repeatedly in vain (but it currently isn't anyhow)
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
            return TopRuleAppCost.INSTANCE;
        } else {
            RuleAppCost compute = completeF.compute(app, pio, goal);
            return compute;
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
            return false;
        } else {
            return !(approvalF.compute(app, pio, goal) instanceof TopRuleAppCost);
        }
    }

    public boolean foundCounterexample() {
        return counterexample;
    }

    /**
     * Check whether the strategy vetos against the specified RuleApp, during
     * evaluation of cost, evaluation of instantiation, or re-evaluation of
     * cost.
     * 
     * @param app
     * @param pio
     * @param goal
     * @return true in the case of a veto such that the rule will definitely not
     *         be applied. false if there is no veto against app such that it
     *         could be applied (depending on its cost).
     */
    protected boolean veto(RuleApp app, PosInOccurrence pio, Goal goal) {
        if (EliminateExistentialQuantifierRule.INSTANCE.filter(app.rule())) {
            // TODO we should still allow, e.g., and-right to fight prohibitive
            // complexity
            return false;
        }
        if (blockAllRules) {
            return true;
        }
        if (((foCache.containsKey(goal.node()) && foCache.get(goal.node()) == FirstOrder.FO) || FOSequence.INSTANCE
                .compute(app, pio, goal) == LongRuleAppCost.ZERO_COST)) {
            foCache.put(goal.node(), FirstOrder.FO);
            if (DLOptionBean.INSTANCE.isStopAtFO()) {
                return true;
            }
            // first-order counterexamples
            if (stopOnFirstCE
                    || DLOptionBean.INSTANCE.getCounterexampleTest().compareTo(
                            CounterexampleTest.ON) >= 0) {
                CounterExample cached = getFirstInCacheUntilBranch(goal.node(),
                        ceCache);
                if (cached != null) {
                    ceCache.put(goal.node(), cached);
                    return cached == CounterExample.CE;
                } else if (FindInstanceTest.INSTANCE.compute(app, pio, goal) == TopRuleAppCost.INSTANCE) {
                    System.out.println("Found CE");// XXX
                    ceCache.put(goal.node(), CounterExample.CE);
                    boolean containsMetavariable = false;
                    for (IteratorOfConstrainedFormula i = goal.sequent().iterator(); i.hasNext(); ) {
                        if (i.next().formula().metaVars().size() > 0) {
                            containsMetavariable = true;
                            break;
                        }
                    }
                    if (containsMetavariable) {
                        // meta variables yield spurious counterexamples
                    } else {
                        counterexample = true;
                    }
                    if (stopOnFirstCE) {
                        blockAllRules = true;
                    }
                    return true;
                } else {
                    ceCache.put(goal.node(), CounterExample.NO_CE);
                }
            }
        } else {
            foCache.put(goal.node(), FirstOrder.NOT_FO);
        }
        return (vetoF.compute(app, pio, goal) instanceof TopRuleAppCost);
    }

    /**
     * @return
     */
    public static <S> S getFirstInCacheUntilBranch(Node node, Map<Node, S> cache) {
        if (cache.containsKey(node)) {
            return cache.get(node);
        }
        if (node.root() || node.parent().root()
                || node.parent().childrenCount() > 1) {
            return null;
        }
        return getFirstInCacheUntilBranch(node.parent(), cache);
    }

    @Override
    protected RuleAppCost instantiateApp(RuleApp app, PosInOccurrence pio,
            Goal goal) {
        if (veto(app, pio, goal)) {
            return TopRuleAppCost.INSTANCE;
        } else {
            return instantiationF.compute(app, pio, goal);
        }
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
                StrategyProperties strategyProperties, boolean stopOnFirstCE,
                long timeout) {
            return new DLStrategy(p_proof, stopOnFirstCE, timeout);
        }

        public Strategy create(Proof p_proof,
                StrategyProperties strategyProperties, boolean stopOnFirstCE,
                long timeout, Set<Name> taboo) {
            return new TabooDLStrategy(p_proof, stopOnFirstCE, timeout, taboo);
        }

        public Name name() {
            return new Name("DLStrategy");
        }
    }

    @Override
    public long getTimeout(Goal goal, RuleApp app) {
        return timeout;
    }
}
