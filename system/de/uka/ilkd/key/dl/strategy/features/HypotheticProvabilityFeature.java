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

import java.rmi.RemoteException;
import java.util.Map;
import java.util.WeakHashMap;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.rules.UnknownProgressRule;
import de.uka.ilkd.key.dl.strategy.DLStrategy;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.IGoalChooser;
import de.uka.ilkd.key.proof.ListOfGoal;
import de.uka.ilkd.key.proof.Node;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.proof.proofevent.NodeChangeJournal;
import de.uka.ilkd.key.proof.proofevent.RuleAppInfo;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.rule.TacletApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.Strategy;
import de.uka.ilkd.key.strategy.TacletAppContainer;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;
import de.uka.ilkd.key.util.Debug;

/**
 * Or-branching Timeout Strategy for testing provability of subgoals within a certain timeout.
 * Feature gives 0 if all subgoals are indeed provable,
 * infinity if some subgoal is definitey not provable because it yields a counterexample,
 * 1 if a timeout occurs before a provable/nonprovable decision has been made. 
 * @author ap
 */
public class HypotheticProvabilityFeature implements Feature {

    private Map<Node, Long> branchingNodesAlreadyTested = new WeakHashMap<Node, Long>();

    private Map<Node, RuleAppCost> resultCache = new WeakHashMap<Node, RuleAppCost>();

    public static final HypotheticProvabilityFeature INSTANCE = new HypotheticProvabilityFeature();

    /**
     * the default initial timeout,
     * -1 means use DLOptionBean.INSTANCE.getInitialTimeout() 
     */
    private final long initialTimeout;
    /**
     * @param timeout the default overall (initial) timeout for the hypothetic proof 
     */
    public HypotheticProvabilityFeature(long timeout) {
        this.initialTimeout = timeout;
    }
    public HypotheticProvabilityFeature() {
        this(-1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule.RuleApp,
     *      de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.proof.Goal)
     */
    public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
        if (!app.complete()) {
            // completing incomplete rule application
            app = completeRuleApp(goal, (TacletApp) app, pos, goal.proof().getActiveStrategy());
            if (app == null || !app.complete()) {
                System.out.println("still incomplete rule application\n" + app);
                return TopRuleAppCost.INSTANCE;
            }
        }
        Node firstNodeAfterBranch = getFirstNodeAfterBranch(goal.node());
        // if (branchingNodesAlreadyTested.containsKey(firstNodeAfterBranch)) {
        // if (resultCache.containsKey(firstNodeAfterBranch)) {
        // return resultCache.get(firstNodeAfterBranch);
        // }
        // return TopRuleAppCost.INSTANCE;
        // } else {
        {
            Long timeout = getLastTimeout(firstNodeAfterBranch);
            if (timeout == null) {
                timeout = initialTimeout >= 0 ? initialTimeout : DLOptionBean.INSTANCE.getInitialTimeout();
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

            System.out.println("HYPO: " + app.rule().name() + " on\n" + goal);
            HypotheticThread testThread = new HypotheticThread(goal, app,
                    timeout);
            testThread.start();
            try {
                testThread.join(timeout);
                System.out.println("HYPO: " + app.rule().name() + " "
                        + testThread.getResult());
                if (testThread.getResult() == HypotheticProvabilityResult.PROVABLE) {
                    return LongRuleAppCost.ZERO_COST;
                    // resultCache.put(firstNodeAfterBranch,
                    // TopRuleAppCost.INSTANCE);
                } else if (testThread.getResult() == HypotheticProvabilityResult.DISPROVABLE
                        || testThread.getResult() == HypotheticProvabilityResult.ERROR) {
                    // resultCache.put(firstNodeAfterBranch,
                    // TopRuleAppCost.INSTANCE);
                    return TopRuleAppCost.INSTANCE;
                } else if (testThread.getResult() == HypotheticProvabilityResult.UNKNOWN) {
                    return LongRuleAppCost.create(1);
                }
            } catch (InterruptedException e) {
                try {
                    MathSolverManager.getCurrentQuantifierEliminator()
                            .abortCalculation();
                } catch (RemoteException f) {
                    testThread.interrupt();

                }
            }
            if (testThread.isAlive()) {
                try {
                    testThread.giveUp = true;
                    testThread.interrupt();
                    MathSolverManager.getCurrentQuantifierEliminator()
                            .abortCalculation();
                } catch (RemoteException f) {
                    testThread.interrupt();

                }
            }
        }

        // resultCache.put(firstNodeAfterBranch, LongRuleAppCost.create(1));
        return LongRuleAppCost.create(1);
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

    
    // rule application engines
    
    /**
     * Create a <code>RuleApp</code> that is suitable to be applied or
     * <code>null</code>.
     * 
     * @see TacletAppContainer#completeRuleApp
     */
    private RuleApp completeRuleApp(Goal p_goal, TacletApp app,
            PosInOccurrence pio, Strategy strategy) {
        // if ( !isStillApplicable ( p_goal ) )
        // return null;
        //    
        // if ( !ifFormulasStillValid ( p_goal ) )
        // return null;

        if (!strategy.isApprovedApp(app, pio, p_goal))
            return null;

        if (pio != null) {
            app = app.setPosInOccurrence(pio);
            if (app == null)
                return null;
        }

        if (!app.complete())
            app = app.tryToInstantiate(p_goal, p_goal.proof().getServices());

        return app;
    }
    /** make Taclet instantions complete with regard to metavariables and
     * skolem functions
     * @see Goal#completeRuleApp
     */ 
    private static RuleApp completeRuleApp (Goal goal, RuleApp ruleApp ) {
        final Proof proof = goal.proof();
        if (ruleApp instanceof TacletApp) {
            TacletApp tacletApp = (TacletApp)ruleApp;
            
            tacletApp = tacletApp.instantiateWithMV ( goal );
            
            ruleApp = tacletApp.createSkolemFunctions 
                ( proof.getNamespaces().functions(), 
                       proof.getServices() );
        }
        return ruleApp;
    }

    /**
     * Possible results of a hypothetic proof attempt.
     * @author ap
     *
     */
    private static enum HypotheticProvabilityResult {
        UNKNOWN, PROVABLE, DISPROVABLE, TIMEOUT, ERROR;
    }

    private static class HypotheticThread extends Thread {

        private static final int MAX_HYPOTHETIC_RULE_APPLICATIONS = 1000;

        private final long timeout;

        private final Goal goal;

        private final RuleApp app;

        private Proof hypothetic;

        private IGoalChooser goalChooser;

        private HypotheticProvabilityResult result;

        /**
         *  giveUp being set to true notifies that this thread should stop
         */
        volatile boolean giveUp = false;

        public HypotheticThread(Goal goal, RuleApp app, long timeout) {
            super("hypothetic prover");
            this.goal = goal;
            this.app = app;
            this.result = HypotheticProvabilityResult.UNKNOWN;
            this.timeout = timeout;
            initializeProof(goal, app);
        }

        /**
         * @param goal
         * @param app
         */
        private void initializeProof(Goal goal, RuleApp app) {
            // new proof with settings like goal.proof() but goal as its
            // only goal
            hypothetic = new Proof(new Name("hypothetic"), goal.proof(), goal
                    .sequent());
            Goal hgoal = hypothetic.getGoal(hypothetic.root());
            assert hgoal != null && hgoal.sequent().equals(goal.sequent());
            Strategy stopEarly = new DLStrategy.Factory().create(hypothetic,
                    null, true);
            hgoal.setGoalStrategy(stopEarly);
            hypothetic.setActiveStrategy(stopEarly);
            // apply app on hypothetic proof
            apply(hgoal, app);
            Debug.out("HYPO: after application");
            // continue hypothetic proof to see if it closes/has
            // counterexamples
            goalChooser = Main.getInstance().mediator().getProfile()
                    .getSelectedGoalChooserBuilder().create();
            goalChooser.init(hypothetic, hypothetic.openGoals());
            maxApplications = MAX_HYPOTHETIC_RULE_APPLICATIONS;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            try {
                result = proofEngine();
                Debug.out("HYPO: regular end " + getResult());
            } catch (NullPointerException e) {
                result = HypotheticProvabilityResult.ERROR;
                System.out.println("Exception during hypothetic proof " + e);
                e.printStackTrace();
                throw e;
            } catch (RuntimeException e) {
                result = HypotheticProvabilityResult.ERROR;
                System.out.println("Exception during hypothetic proof " + e);
                e.printStackTrace();
                throw e;
            } finally {
                Debug.out("HYPO: finished " + getResult());
            }
        }

        /**
         * @return the result
         */
        public HypotheticProvabilityResult getResult() {
            return result;
        }


        /**
         * @see Goal#apply without events, which would cause synchronization blocking
         */
        public ListOfGoal apply(Goal goal, RuleApp p_ruleApp ) {
          //System.err.println(Thread.currentThread());    

                  final Proof proof = goal.proof();
                  
                  // TODO: this is maybe not the right place for this check
                  assert proof.mgt ().ruleApplicable ( p_ruleApp, goal ) :
                           "Someone tried to apply the rule " + p_ruleApp +
                           " that is not justified";
                  
                  final NodeChangeJournal journal = new NodeChangeJournal(proof, goal);
//                  addGoalListener(journal);
                  
                  final RuleApp ruleApp = completeRuleApp(goal, p_ruleApp ); 
                  
                  final ListOfGoal goalList = ruleApp.execute(goal,  
                          proof.getServices());
                  
                  if ( goalList == null ) {
                      // this happens for the simplify decision procedure
                      // we do nothing in this case
                  } else if ( goalList.isEmpty() ) {
                      proof.closeGoal ( goal, ruleApp.constraint () );           
                  } else {
                      proof.replace ( goal, goalList );
                      if ( ruleApp instanceof TacletApp &&
                              ((TacletApp)ruleApp).taclet ().closeGoal () )
                          // the first new goal is the one to be closed
                          proof.closeGoal ( goalList.head (), ruleApp.constraint () );
                  }

                  final RuleAppInfo ruleAppInfo = journal.getRuleAppInfo(p_ruleApp);

                  /* disable events
                  if ( goalList != null )
                      fireRuleApplied( new ProofEvent ( proof, ruleAppInfo ) );
                  */
                  return goalList;
              }

        /**
         * applies rules that are chosen by the active strategy
         * 
         * @return true iff a rule has been applied, false otherwise
         */
        private boolean applyAutomaticRule() {
            // Look for the strategy ...
            RuleApp app = null;
            Goal g;
            while ((g = goalChooser.getNextGoal()) != null) {
                app = g.getRuleAppManager().next();

                if (app == null)
                    goalChooser.removeGoal(g);
                else
                    break;
            }
            if (app == null)
                return false;
            apply(g, app);
            return true;
        }

        private long time;

        private int countApplied;

        private int maxApplications;

        /**
         * returns if the maximum number of rule applications or the timeout has
         * been reached
         * 
         * @return true if automatic rule application shall be stopped because
         *         the maximal number of rules have been applied or the time out
         *         has been reached
         */
        private boolean maxRuleApplicationOrTimeoutExceeded() {
            return giveUp || countApplied >= maxApplications || timeout >= 0 ? System
                    .currentTimeMillis()
                    - time >= timeout
                    : false;
        }

        /**
         * applies rules until this is no longer possible or the thread is
         * interrupted.
         */
        HypotheticProvabilityResult proofEngine() {
            countApplied = 0;
            time = System.currentTimeMillis();
            try {
                Debug.out("Strategy started.");
                while (!maxRuleApplicationOrTimeoutExceeded()) {
                    Debug.out("HYPO: goals " + hypothetic.openGoals().size());
                    if (!applyAutomaticRule()) {
                        // no more rules applicable
                        if (hypothetic.openGoals().isEmpty()) {
                            return HypotheticProvabilityResult.PROVABLE;
                        } else {
                            // if counterexample
                            if (hypothetic.getActiveStrategy() instanceof DLStrategy
                                    && ((DLStrategy) hypothetic
                                            .getActiveStrategy())
                                            .foundCounterexample()) {
                                return HypotheticProvabilityResult.DISPROVABLE;
                            } else {
                                System.out.println("HYPO no more rules on\n"
                                        + hypothetic.openGoals());
                                return HypotheticProvabilityResult.UNKNOWN;
                            }
                        }
                    }
                    countApplied++;
                    if (Thread.interrupted())
                        throw new InterruptedException();
                }
                return HypotheticProvabilityResult.TIMEOUT;
            } catch (InterruptedException e) {
                return HypotheticProvabilityResult.TIMEOUT;
            } finally {
                time = System.currentTimeMillis() - time;
                Debug.out("Strategy stopped.");
                Debug.out("Applied ", countApplied);
                Debug.out("Time elapsed: ", time);
            }
        }
    }
}
