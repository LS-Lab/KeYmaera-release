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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.options.DLOptionBean.ApplyRules;
import de.uka.ilkd.key.dl.options.DLOptionBean.DiffSat;
import de.uka.ilkd.key.dl.rules.DebugRule;
import de.uka.ilkd.key.dl.rules.EliminateQuantifierRule;
import de.uka.ilkd.key.dl.rules.EliminateQuantifierRuleWithContext;
import de.uka.ilkd.key.dl.rules.FindInstanceRule;
import de.uka.ilkd.key.dl.rules.ReduceRule;
import de.uka.ilkd.key.dl.rules.VisualizationRule;
import de.uka.ilkd.key.dl.strategy.features.DiffIndCandidates;
import de.uka.ilkd.key.dl.strategy.features.DiffSatFeature;
import de.uka.ilkd.key.dl.strategy.features.DiffWeakenFeature;
import de.uka.ilkd.key.dl.strategy.features.FOFormula;
import de.uka.ilkd.key.dl.strategy.features.FOSequence;
import de.uka.ilkd.key.dl.strategy.features.FindInstanceTest;
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
import de.uka.ilkd.key.strategy.feature.instantiator.RuleAppBuffer;
import de.uka.ilkd.key.strategy.termProjection.AssumptionProjection;
import de.uka.ilkd.key.strategy.termProjection.TermBuffer;

/**
 * @author jdq
 * @author ap
 */
public class DLStrategy extends AbstractFeatureStrategy {

    private final Feature vetoF;

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

    protected DLStrategy(Proof p_proof) {
        this(p_proof, false);
    }

    /**
     * 
     * @param p_proof
     * @param stopOnFirstCE whether to make a full stop and apply
     *  no more rules at all as soon as the first counterexample occurs.
     *  Otherwise, branches without counterexamples are still worked on even though the overall proof cannot close. 
     */
    protected DLStrategy(Proof p_proof, boolean stopOnFirstCE) {
        super(p_proof);
        this.stopOnFirstCE = stopOnFirstCE;

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
        
        setupDiffSatStrategy(d);

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

        vetoF = duplicateF;

        completeF = SumFeature
                .createSum(new Feature[] { AutomatedRuleFeature.INSTANCE,
                        NotWithinMVFeature.INSTANCE, simplifierF, duplicateF,
                        ifMatchedF, d, AgeFeature.INSTANCE, reduceSequence,
                        debugRule, visualRule, contextElimRule,
                        eliminateQuantifier, findInstanceRule,
                        noQuantifierInstantition });

        approvalF = setupApprovalF(p_proof);
        
        instantiationF = setupInstantiationF(p_proof);
    }

    /**
     * DiffSat strategy.
     * @author ap
     */
    private void setupDiffSatStrategy(final RuleSetDispatchFeature d) {
        if (DLOptionBean.INSTANCE.getDiffSat() == DiffSat.BLIND) {
            bindRuleSet(d, "invariant_diff", inftyConst());
            bindRuleSet(d, "invariant_weaken", inftyConst());
            bindRuleSet(d, "invariant_strengthen", inftyConst());
            return;            
        } else {
            bindRuleSet(d, "diff_solve",
                    ifZero(ODESolvableFeature.INSTANCE,
                            longConst(4000),
                            inftyConst()));
        }

        if (DLOptionBean.INSTANCE.getDiffSat().compareTo(DiffSat.SIMPLE)>=0) {
            bindRuleSet(d, "invariant_weaken",
                new SwitchFeature(DiffWeakenFeature.INSTANCE,
                    new Case(longConst(0), longConst(-6000)),
                    // reject if it doesn't help, but retry costs
                    new Case(longConst(1), longConst(5000)),
                    new Case(inftyConst(), inftyConst())));
            bindRuleSet(d, "invariant_diff",
                     ifZero(PostDiffStrengthFeature.INSTANCE,
                            longConst(-4000),
                            ifZero(DiffWeakenFeature.INSTANCE,
                                   inftyConst(),
                                   DLOptionBean.INSTANCE.getDiffSat().compareTo(DiffSat.DIFF)>=0
                                   ? // re-evaluate feature at least after diffstrengthen
                                       // reject if it doesn't help, but retry costs
                                       longConst(6000)
                                   : // only directly check diffind
                                       new SwitchFeature(HypotheticalProvabilityFeature.INSTANCE,
                                         new Case(longConst(0), longConst(-4000)),
                                         // reject if it doesn't help, but retry costs
                                         new Case(longConst(1), longConst(6000)),
                                         new Case(inftyConst(), inftyConst()))
                            )));
        } else {
            bindRuleSet(d, "invariant_diff", inftyConst());
            bindRuleSet(d, "invariant_weaken", inftyConst());
        }

        if (DLOptionBean.INSTANCE.getDiffSat().compareTo(DiffSat.DIFF)>=0) {
            bindRuleSet(d, "invariant_strengthen",
                    ifZero(PostDiffStrengthFeature.INSTANCE,
                            inftyConst(),             // strengthening augmentation validity proofs seems useless
                            ifZero(DiffWeakenFeature.INSTANCE,
                                    inftyConst(),     // never instantiate when cheaper rule successful
                                    longConst(10000)   // go on try to instantiate, except when tabooed
                            )));
        } else {
            bindRuleSet(d, "invariant_strengthen", inftyConst());
        }
    }


    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    //
    // Feature terms that handle the instantiation of incomplete taclet
    // applications
    //
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////


    private RuleSetDispatchFeature setupInstantiationF(Proof p_proof) {
        enableInstantiate ();
        final RuleSetDispatchFeature d = RuleSetDispatchFeature.create ();
        setupDiffSatInstantiationStrategy( d );
        disableInstantiate ();
        return d;
    }

    /**
     * DiffSat instantiation strategy.
     * @author ap
     */
    private void setupDiffSatInstantiationStrategy(final RuleSetDispatchFeature d) {
        if (DLOptionBean.INSTANCE.getDiffSat().compareTo(DiffSat.DIFF)>=0) {
            final TermBuffer augInst = new TermBuffer();
            final RuleAppBuffer buffy = new RuleAppBuffer();
            bindRuleSet(d, "invariant_strengthen",
                    ifZero(storeRuleApp(buffy,
                            not(sum(augInst, DiffIndCandidates.INSTANCE,
                                    add(buffy,
                                        instantiate("augment", augInst),
                                        not(new DiffSatFeature(augInst))
                                    )
                            ))),
                            longConst(-1000),
                            inftyConst()
                    ));
        } else {
            bindRuleSet(d, "invariant_strengthen", inftyConst());
        }
        if (DLOptionBean.INSTANCE.getDiffSat().compareTo(DiffSat.AUTO)>=0) {
            final TermBuffer augInst = new TermBuffer();
            final RuleAppBuffer buffy = new RuleAppBuffer();
            bindRuleSet(d, "loop_invariant_proposal",
                    storeRuleApp(buffy,
                    ifZero(add(instantiate("inv", instOf("post")),
                               openCurrentRuleApp(HypotheticalProvabilityFeature.INSTANCE)
                            ),
                           longConst(-2000),
                           ifZero(
                                   not(sum(augInst, DiffIndCandidates.INSTANCE,
                                           add(buffy,
                                               instantiate("inv", augInst),
                                               not(openCurrentRuleApp(HypotheticalProvabilityFeature.INSTANCE))
                                           )
                                   )),
                                   longConst(-1000),
                                   inftyConst()  //@todo use large constant instead to try at least
                           )
                    )));
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    //
    // Feature terms that handle the approval of complete taclet applications
    //
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    private Feature setupApprovalF(Proof p_proof) {
        final RuleSetDispatchFeature d = RuleSetDispatchFeature.create ();
        setupDiffSatApprovalStrategy( d );
        return d;
    }

    /**
     * DiffSat approval strategy.
     * @author ap
     */
    private void setupDiffSatApprovalStrategy(final RuleSetDispatchFeature d) {
        if (DLOptionBean.INSTANCE.getDiffSat().compareTo(DiffSat.SIMPLE)>=0) {
            bindRuleSet(d, "invariant_weaken",
                    new SwitchFeature(DiffWeakenFeature.INSTANCE,
                        new Case(longConst(0), longConst(-6000)),
                        // reject if it doesn't help, but retry costs
                        new Case(longConst(1), inftyConst()),
                        new Case(inftyConst(), inftyConst())));
            bindRuleSet(d, "invariant_diff",
                     ifZero(PostDiffStrengthFeature.INSTANCE,
                            longConst(-4000),
                            ifZero(DiffWeakenFeature.INSTANCE,
                                   inftyConst(),
                                   // only directly check diffind
                                   new SwitchFeature(HypotheticalProvabilityFeature.INSTANCE,
                                         new Case(longConst(0), longConst(-4000)),
                                        // reject if it doesn't help, but retry costs
                                         new Case(longConst(1), inftyConst()),
                                         new Case(inftyConst(), inftyConst()))
                            )));
        }
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
        return blockAllRules;
    }

    /**
     * Check whether the strategy vetos against the specified RuleApp,
     * during evaluation of cost, evaluation of instantiation, or re-evaluation of cost.
     * 
     * @param app
     * @param pio
     * @param goal
     * @return true in the case of a veto such that the rule will definitely not be applied.
     * false if there is no veto against app such that it could be applied (depending on its cost).
     */
    protected boolean veto(RuleApp app, PosInOccurrence pio, Goal goal) {
        if (blockAllRules) {
            return true;
        }
        if (((foCache.containsKey(goal.node()) && foCache.get(goal.node()) == FirstOrder.FO) || FOSequence.INSTANCE
                .compute(app, pio, goal) == LongRuleAppCost.ZERO_COST)) {
            foCache.put(goal.node(), FirstOrder.FO);
            if (DLOptionBean.INSTANCE.isStopAtFO()) {
                return true;
            }
            if (DLOptionBean.INSTANCE.isUseFindInstanceTest()) {
                CounterExample cached = getFirstInCacheUntilBranch(goal.node(),
                        ceCache);
                if (cached != null) {
                    ceCache.put(goal.node(), cached);
                    return cached == CounterExample.CE;
                } else if (FindInstanceTest.INSTANCE.compute(app, pio, goal) == TopRuleAppCost.INSTANCE) {
                    System.out.println("Found CE");// XXX
                    ceCache.put(goal.node(), CounterExample.CE);
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
            return instantiationF.compute ( app, pio, goal );
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
                StrategyProperties strategyProperties, boolean stopOnFirstCE) {
            return new DLStrategy(p_proof, stopOnFirstCE);
        }
        public Strategy create(Proof p_proof,
                StrategyProperties strategyProperties, boolean stopOnFirstCE,
                Set<Name> taboo) {
            return new TabooDLStrategy(p_proof, stopOnFirstCE, taboo);
        }

        public Name name() {
            return new Name("DLStrategy");
        }
    }
}
