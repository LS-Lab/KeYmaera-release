/***************************************************************************
 *   Copyright (C) 2007 by André Platzer                                   *
 *   @informatik.uni-oldenburg.de                                    *
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
 * File created 01.02.2007
 */
package de.uka.ilkd.key.dl.rules;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;

import orbital.awt.UIUtilities;
import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.Mathematica;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.MathematicaDLBridge;
import de.uka.ilkd.key.dl.formulatools.TermTools;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.strategy.features.FOSequence;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.*;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.RuleFilter;
import de.uka.ilkd.key.rule.BuiltInRule;
import de.uka.ilkd.key.rule.Rule;
import de.uka.ilkd.key.rule.RuleApp;

import orbital.awt.ChartModel;

/**
 * The FindInstance is a Built-In Rule to test counterexamples for transition systems.
 * 
 * @author ap
 * @see de.uka.ilkd.key.proof.DLProfile
 */
public class FindTransitionRule implements BuiltInRule, RuleFilter {

    public static final FindTransitionRule INSTANCE = new FindTransitionRule();

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.BuiltInRule#isApplicable(de.uka.ilkd.key.proof.Goal,
     *      de.uka.ilkd.key.logic.PosInOccurrence,
     *      de.uka.ilkd.key.logic.Constraint)
     */
    public boolean isApplicable(Goal goal, PosInOccurrence pos,
            Constraint userConstraint) {
        // this is set in hybrid strategy tab
        if (!MathSolverManager.isCounterExampleGeneratorSet())
            return false;
        if (pos == null || !pos.isTopLevel() || goal == null || userConstraint == null)
            return false;
        Term term = pos.subTerm();
        // unbox from update prefix
        if (term.op() instanceof QuanUpdateOperator) {
            term = ((QuanUpdateOperator) term.op()).target(term);
            if (term.op() instanceof QuanUpdateOperator)
                // can't apply until nested updates have been merged
                return false;
        }
        if (!(term.op() instanceof Modality
           && term.javaBlock() != null
           && term.javaBlock() != JavaBlock.EMPTY_JAVABLOCK
           && term.javaBlock().program() instanceof StatementBlock
           // only admits box property modality
           // additionally, only admits box property modality in the nested DLPrograms
           // however, checking nested box property is left to cex finder
           && term.op().toString().equals("box"))) {
            return false;
        }
        assert(goal.sequent() != null && goal.sequent().antecedent() != null);

        return FOSequence.INSTANCE.isFOFormulas(goal.sequent().antecedent().iterator());
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#apply(de.uka.ilkd.key.proof.Goal,
     *      de.uka.ilkd.key.java.Services, de.uka.ilkd.key.rule.RuleApp)
     */
    @SuppressWarnings("unchecked")
    public synchronized ImmutableList<Goal> apply(Goal goal, Services services,
            RuleApp ruleApp) {
        assert(goal != null && services != null && ruleApp != null);
        Term antecedent = TermTools.createJunctorTermNAry(TermBuilder.DF.tt(),
                Op.AND, goal.sequent().antecedent().iterator(),
                Collections.EMPTY_SET, true);
        // @todo ignore succedent?

        try {
            final String result = MathSolverManager.getCurrentCounterExampleGenerator()
                .findTransition(antecedent, ruleApp.posInOccurrence().subTerm(), -1, services);
            /*final JFrame frame = new JFrame(displayName());
            frame.setLayout(new GridBagLayout());
            final JScrollPane scroll = new JScrollPane();
            final JButton okButton = new JButton("OK");
            GridBagConstraints cl = new GridBagConstraints();
            GridBagConstraints cs = new GridBagConstraints();

            cl.fill = GridBagConstraints.BOTH;
            cl.gridx = 0;
            cl.gridy = 0;
            cl.gridwidth = 3;
            cl.gridheight = 5;
            cl.weightx = 1.0;
            cl.weighty = 1.0;
            scroll.setPreferredSize(new Dimension(300, 500));

            final JTree tree = new JTree();
            // TODO: configure the tree
            scroll.add(tree);

            frame.add(scroll, cl);

            cs.fill = GridBagConstraints.NONE;
            cs.gridx = 3;
            cs.gridy = 2;
            cs.anchor = GridBagConstraints.CENTER;
            cs.weightx = 0.5;
            cs.weighty = 0.5;
            okButton.setPreferredSize(new Dimension(100, 50));
            okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg) {
                    frame.setVisible(false);
                }
            });
            frame.add(okButton, cs);

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    frame.pack();
                    UIUtilities.setCenter(frame, Main.getInstance());
                    frame.setVisible(true);
                }

            });*/

        } catch (Exception e) {
            // if there is an error invoking the mathsolver we cannot apply this
            // rule.
            e.printStackTrace();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#displayName()
     */
    public String displayName() {
        return "Counterexample Transition";
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#name()
     */
    public Name name() {
        return new Name(displayName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.proof.RuleFilter#filter(de.uka.ilkd.key.rule.Rule)
     */
    public boolean filter(Rule rule) {
        return rule instanceof FindTransitionRule;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    /*@Override*/
    public String toString() {
        return displayName();
    }

}
