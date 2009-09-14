/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel                                *
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
/**
 * 
 */
package de.uka.ilkd.key.dl.gui;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.gui.AutoModeListener;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Node;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.proof.ProofEvent;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.AutomatedRuleApplicationManager;

/**
 * This is an implementation of the {@link AutoModeListener}. It is used to set
 * a {@link DummyRuleApplicationManager} when in interactive mode.
 * 
 * @author jdq
 * @since Jul 26, 2007
 * 
 */
public class AutomodeListener implements AutoModeListener {

    public static Goal currentGoal = null;

    public static boolean aborted = false;

    /**
     * This is a dummy implementation of an
     * {@link AutomatedRuleApplicationManager}. It is used to suppress strategy
     * evaluations in interactive mode. It solves the problem that manual
     * application of rules freezes the gui for the current timeout, as the
     * strategy reevaluates, whether it should apply quantifier elimination or
     * not.
     * 
     * @author jdq
     * @since Aug 13, 2007
     * 
     */
    private static final class DummyRuleApplicationManager implements
            AutomatedRuleApplicationManager {

        private AutomatedRuleApplicationManager original;

        /**
         * 
         */
        public DummyRuleApplicationManager(
                AutomatedRuleApplicationManager original) {
            this.original = original;
        }

        public void clearCache() {
            // TODO Auto-generated method stub

        }

        public AutomatedRuleApplicationManager copy() {
            return this;
        }

        public RuleApp next() {
            // TODO Auto-generated method stub
            return null;
        }

        public void setGoal(Goal p_goal) {
            // TODO Auto-generated method stub

        }

        public void ruleAdded(RuleApp rule, PosInOccurrence pos) {
            // TODO Auto-generated method stub

        }

        public RuleApp peekNext() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @return the original
         */
        public AutomatedRuleApplicationManager getOriginal() {
            return original;
        }
    }

    private Map<Node, AutomatedRuleApplicationManager> goalManagerMap = new WeakHashMap<Node, AutomatedRuleApplicationManager>();

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.gui.AutoModeListener#autoModeStarted(de.uka.ilkd.key.proof.ProofEvent)
     */
    public void autoModeStarted(ProofEvent e) {

        Proof proof = e.getSource();
        if (proof != null) {
             ImmutableList<Goal> openGoals = proof.openGoals();
            if (openGoals != null) {
                Iterator<Goal> goals = openGoals.iterator();
                while (goals.hasNext()) {
                    Goal next = goals.next();
                    if (next.getRuleAppManager() instanceof DummyRuleApplicationManager) {
                        if (goalManagerMap.containsKey(next.node())) {
                            next.setRuleAppManager(goalManagerMap.get(next
                                    .node()));
                        } else {
                            AutomatedRuleApplicationManager queueRuleApplicationManager = ((DummyRuleApplicationManager) next
                                    .getRuleAppManager()).getOriginal().copy();
                            next.setRuleAppManager(queueRuleApplicationManager);
                            queueRuleApplicationManager.clearCache();
                            next.clearAndDetachRuleAppIndex();
                            next.ruleAppIndex().fillCache();
                        }
                    }
                }
            }
        }
        goalManagerMap.clear();
        if (aborted && currentGoal != null) {
            currentGoal.getRuleAppManager().clearCache();
            currentGoal.clearAndDetachRuleAppIndex();
            currentGoal.ruleAppIndex().fillCache();
        }
        currentGoal = null;
        aborted = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.gui.AutoModeListener#autoModeStopped(de.uka.ilkd.key.proof.ProofEvent)
     */
    public void autoModeStopped(ProofEvent e) {
        Proof proof = Main.getInstance().mediator().getProof();
        if (proof != null) {
             Iterator<Goal> goals = proof.openGoals().iterator();
            while (goals.hasNext()) {
                Goal next = goals.next();
                if (next.getRuleAppManager() != null
                        && !(next.getRuleAppManager() instanceof DummyRuleApplicationManager)) {
                    goalManagerMap.put(next.node(), next.getRuleAppManager());
                    DummyRuleApplicationManager dummyRuleApplicationManager = new DummyRuleApplicationManager(
                            next.getRuleAppManager());
                    next.setRuleAppManager(dummyRuleApplicationManager);
                }
            }
        }
        if (currentGoal != null) {
            if (e.getSource().openGoals().contains(currentGoal)) {
                Main.getInstance().mediator().goalChosen(currentGoal);
            } else if (!e.getSource().openGoals().isEmpty()) {
                Main.getInstance().mediator().goalChosen(
                        currentGoal.proof().openGoals().head());
            }
        }
    }

}
