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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.dl.arithmetics.exceptions.FailedComputationException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.rules.UnknownProgressRule;
import de.uka.ilkd.key.dl.rules.metaconstruct.DLUniversalClosureOp;
import de.uka.ilkd.key.dl.rules.metaconstruct.DiffInd;
import de.uka.ilkd.key.dl.strategy.RuleAppCostTimeout;
import de.uka.ilkd.key.dl.strategy.features.HypotheticalProvabilityFeature.HypotheticalProvability;
import de.uka.ilkd.key.gui.Main;
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
import de.uka.ilkd.key.pp.LogicPrinter;
import de.uka.ilkd.key.pp.ProgramPrinter;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Node;
import de.uka.ilkd.key.proof.TacletFilter;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.rule.Taclet;
import de.uka.ilkd.key.rule.TacletApp;
import de.uka.ilkd.key.rule.inst.IllegalInstantiationException;
import de.uka.ilkd.key.rule.updatesimplifier.Update;
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
     * Remembers diffind initials for the given surrounding term T.
     * A==>B,[D&H]F
     * caches at A==>B,{U}(H->F)
     */
    private final Map<Sequent,RuleAppCost> diffInitCache = new WeakHashMap<Sequent,RuleAppCost>();

    /**
     * Remembers diffinds for the given [D]F modality.
     * diffAugCache.get(D).get(A) remembers whether A is diffind for [D] 
     */
    private final Map<DiffSystem, Map<Term,RuleAppCost>> diffIndCache = new WeakHashMap<DiffSystem, Map<Term,RuleAppCost>>();

    public static final DiffSatFeature INSTANCE = new DiffSatFeature(null);
    
    /**
     * List of rules which are taboo during a hypothetical provability check for DiffSat.
     */
    private static final Set<Name> taboo = new LinkedHashSet<Name>(Arrays.asList(new Name[] {
            new Name("diffstrengthen"),
            new Name("diffweaken")
    }));

    /**
     * the default initial timeout, -1 means use
     * DLOptionBean.INSTANCE.getInitialTimeout()
     */
    private final long initialTimeout;

    private final ProjectionToTerm value;

    private boolean strongCandidates;
    
    /**
     * @param timeout
     *                the default overall (initial) timeout for the hypothetic
     *                proof
     */
    public DiffSatFeature(long timeout, ProjectionToTerm value, boolean strongInd) {
        this.initialTimeout = timeout;
        this.value = value;
        this.strongCandidates=strongInd;
    }
    public DiffSatFeature(long timeout, ProjectionToTerm value) {
        this(timeout, value, false);
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
        //System.out.println("check diffsat" + " for " + myapp.rule().name() + " on " + pos);
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

        return diffSat(app, pos, goal, timeout*1000);
    }

    /**
     * Determines if diffind works for the given augmentation according to DiffSat.
     * 
     * @param pos
     * @param goal
     * @param timeout (in ms)
     */
    private RuleAppCost diffSat(TacletApp app, final PosInOccurrence pos, Goal goal,
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
        final Term invariant = system.getInvariant(goal.proof().getServices());
        final Term post = term.sub(0);
        final Services services = goal.proof().getServices();

        RuleApp diffind = goal.indexOfTaclets().lookup(new Name("diffind"));
        Term candidate = (Term) app.instantiations().lookupValue(new Name("augmented"));
        if (candidate == null) {
            if (value == null)
                throw new IllegalStateException("no such projection " + value);
            candidate = value.toTerm(app, pos, goal);
            if (candidate == null)
                throw new IllegalInstantiationException("Invalid instantiation null"
                    + " for SV 'augmented' in " + app.instantiations());
        }
        String candidatePrint;
        try {
            final LogicPrinter lp = new LogicPrinter(new ProgramPrinter(null), 
                    Main.getInstance().mediator().getNotationInfo(),
                    services);
            lp.printTerm(candidate);
            candidatePrint = lp.toString();
        }
        catch (Exception ignore) {
            candidatePrint = candidate.toString();
        }
        //System.out.println("instantiation " + candidate + " for SV 'augmented'");

        // diffind
        RuleAppCost cached = RuleAppCostTimeout.superior(timeout, get(system, candidate));
        if (cached != null) {
            //@todo also recursively check whether subsets of system are invariant, or supersets of system are not invariant 
            return cached;
        }


        //System.out.println("HYPO: " + diffind.rule().name() + " initial " + candidate);
        // diffind:"Invariant Initially Valid"
        Sequent initial = changedSequent(pos, goal.sequent(), TermBuilder.DF.imp(invariant, candidate), pos.subTerm());
        RuleAppCost cachedInitial = RuleAppCostTimeout.superior(timeout,diffInitCache.get(initial));
        if (cachedInitial!= null && (TopRuleAppCost.INSTANCE == cachedInitial || HypotheticalProvabilityFeature.TIMEOUT_COST.equals(cachedInitial))) {
            return cachedInitial;
        }
        if (LongRuleAppCost.ZERO_COST != cachedInitial) {
            System.out.print("HYPO: " + diffind.rule().name() + " initial for " + candidatePrint);System.out.flush();
            HypotheticalProvability result = HypotheticalProvabilityFeature
                    .provable(goal.proof(), initial, MAX_STEPS, timeout, taboo);
            System.out.println(" " + result);
            switch (result) {
            case PROVABLE:
                diffInitCache.put(initial, LongRuleAppCost.ZERO_COST);
                break;
            case ERROR:
            case DISPROVABLE:
                diffInitCache.put(initial, TopRuleAppCost.INSTANCE);
                return TopRuleAppCost.INSTANCE;
            case UNKNOWN:
                diffInitCache.put(initial, TopRuleAppCost.INSTANCE);
                return TopRuleAppCost.INSTANCE;
            case TIMEOUT:
                // resultCache.put(firstNodeAfterBranch,
                // LongRuleAppCost.create(1));
                diffInitCache.put(initial, RuleAppCostTimeout.create(timeout));
                return HypotheticalProvabilityFeature.TIMEOUT_COST;
            default:
                throw new AssertionError("enum known");
            }
        }

        if (strongCandidates) {
            Term finishTerm = DLUniversalClosureOp.DL_UNIVERSAL_CLOSURE.universalClosure(
                    system,
                    TermBuilder.DF.imp(candidate, post), null,
                    services, false);
            Sequent finish = changedSequent(pos, goal.sequent(), finishTerm, pos.subTerm());
            System.out.print("HYPO: " + diffind.rule().name() + " finish for " + candidatePrint);System.out.println();
            HypotheticalProvability result = HypotheticalProvabilityFeature.provable(goal.proof(), finish, MAX_STEPS,
                    timeout, taboo);
            System.out.println(" " + result);
            // TODO cache but remember that DLUniversalClosureOp introduces different variable names
            switch (result) {
            case PROVABLE:
                break;
            case ERROR:
            case DISPROVABLE:
                return TopRuleAppCost.INSTANCE;
            case UNKNOWN:
                return TopRuleAppCost.INSTANCE;
            case TIMEOUT:
                // resultCache.put(firstNodeAfterBranch,
                // LongRuleAppCost.create(1));
                return HypotheticalProvabilityFeature.TIMEOUT_COST;
            default:
                throw new AssertionError("enum known");
            }
        }
        
        
        // diffind:"ODE Preserves Invariant"
        //System.out.println("HYPO: " + diffind.rule().name() + " step " + candidate);
        Term augTerm = de.uka.ilkd.key.logic.TermFactory.DEFAULT.createProgramTerm(
                    term.op(),
                    term.javaBlock(),
                    candidate);
        Term stepFml;
        try {
            stepFml = DLUniversalClosureOp.DL_UNIVERSAL_CLOSURE.universalClosure(
                    system,
                    DiffInd.DIFFIND.diffInd(augTerm, services), null,
                    services, true);
        } catch (UnsolveableException e) {
            System.out.print("HYPO: " + diffind.rule().name() + " step    for " + candidatePrint + " UNSOLVABLE " + e);
            put(system, candidate, TopRuleAppCost.INSTANCE);
            return TopRuleAppCost.INSTANCE;
        } catch (FailedComputationException e) {
            System.out.print("HYPO: " + diffind.rule().name() + " step    for " + candidatePrint + " FAILED " + e);
            throw new IllegalArgumentException(e.getMessage(), e);
        } catch (SolverException e) {
            throw (InternalError) new InternalError(e.getMessage()).initCause(e);
        }
        Sequent step = changedSequent(pos, goal.sequent(), stepFml, pos.subTerm());
        System.out.print("HYPO: " + diffind.rule().name() + " step    for " + candidatePrint); System.out.flush();
        HypotheticalProvability result = HypotheticalProvabilityFeature.provable(goal.proof(), step, MAX_STEPS,
                timeout, taboo);
        System.out.println(" " + result);
        switch (result) {
        case PROVABLE:
            put(system, candidate, LongRuleAppCost.ZERO_COST);
            return LongRuleAppCost.ZERO_COST;
        case ERROR:
        case DISPROVABLE:
            put(system, candidate, TopRuleAppCost.INSTANCE);
            return TopRuleAppCost.INSTANCE;
        case UNKNOWN:
            put(system, candidate, TopRuleAppCost.INSTANCE);
            return TopRuleAppCost.INSTANCE;
        case TIMEOUT:
            // resultCache.put(firstNodeAfterBranch,
            // LongRuleAppCost.create(1));
            put(system, candidate, RuleAppCostTimeout.create(timeout));
            return HypotheticalProvabilityFeature.TIMEOUT_COST;
        default:
            throw new AssertionError("enum known");
        }
    }

    private static Term createUpdate(final Update update, Term initialFml) {
        // keep update prefix
        Term locs[] = new Term[update.locationCount()];
        Term vals[] = new Term[update.locationCount()];
        for (int i = 0; i < locs.length; i++) {
            locs[i] = update.getAssignmentPair(i).locationAsTerm();
            vals[i] = update.getAssignmentPair(i).value();
        }
        initialFml = de.uka.ilkd.key.logic.TermFactory.DEFAULT.createUpdateTerm(
                locs, vals,
                initialFml);
        return initialFml;
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


    /**
     * Change a sequent,
     * keeping updates from updatePrefixContext (but ignoring the remainders of updatePrefixContext).
     */
    protected static Sequent changedSequent(PosInOccurrence pos, Sequent seq,
            Term fml, Term updatePrefixContext) {
        try {
            if (updatePrefixContext.op() instanceof QuanUpdateOperator) {
                // keep update prefix
                final Update update = Update.createUpdate(updatePrefixContext);
                fml = createUpdate(update, fml);
		if (updatePrefixContext.sub(0).op() instanceof QuanUpdateOperator) {
                    throw new AssertionError("assume nested updates have been merged");
		}
            }
            return seq.changeFormula(new ConstrainedFormula(fml, pos
                .constrainedFormula().constraint()), pos).sequent();
        } catch (RuntimeException e) {
            System.err.println(e + " while replacing " + fml);
            throw e;
        }
    }

    private static RuleApp getRuleAppOf(Name tacletname, PosInOccurrence pos,
            Goal goal) {
        // Main.getInstance().mediator().getInteractiveProver().getAppsForName(goal,
        // tacletname.toString(), pos);
        // goal.indexOfTaclets().lookup(name)
        ImmutableList<TacletApp> l = goal.ruleAppIndex().getTacletAppAt(
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
