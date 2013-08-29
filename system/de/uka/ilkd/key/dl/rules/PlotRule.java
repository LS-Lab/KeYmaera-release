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
 * File created 01.02.2007
 */
package de.uka.ilkd.key.dl.rules;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.math.plot.Plot2DPanel;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.Mathematica;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLStatementBlock;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.FreeFunction;
import de.uka.ilkd.key.dl.model.LogicalVariable;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.gui.notification.events.ExceptionEvent;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.sort.ProgramSVSort;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.RuleFilter;
import de.uka.ilkd.key.rule.BuiltInRule;
import de.uka.ilkd.key.rule.Rule;
import de.uka.ilkd.key.rule.RuleApp;

/**
 * This rule is used to plot trajectories of differential equation systems
 * 
 * @author jdq
 * @since 15.08.2012
 * 
 */
public class PlotRule implements BuiltInRule, RuleFilter {

    public static final PlotRule INSTANCE = new PlotRule();

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.uka.ilkd.key.rule.BuiltInRule#isApplicable(de.uka.ilkd.key.proof.Goal,
     * de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.logic.Constraint)
     */
    public boolean isApplicable(Goal goal, PosInOccurrence pio,
            Constraint userConstraint) {
        if (pio != null
                && pio.subTerm().javaBlock() != null
                && pio.subTerm().javaBlock().program() != null
                && pio.subTerm().javaBlock().program() instanceof DLStatementBlock) {
        	return true;
//            return ProgramSVSort.DL_SIMPLE_ORDINARY_DIFF_SYSTEM_SORT_INSTANCE
//                    .canStandFor(((DLStatementBlock) pio.subTerm().javaBlock()
//                            .program()).getChildAt(0), null, goal.proof()
//                            .getServices());
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#apply(de.uka.ilkd.key.proof.Goal,
     * de.uka.ilkd.key.java.Services, de.uka.ilkd.key.rule.RuleApp)
     */
    public synchronized ImmutableList<Goal> apply(Goal goal,
            final Services services, final RuleApp ruleApp) {
        final Mathematica math = (Mathematica) MathSolverManager
                .getQuantifierElimantor("Mathematica");
        if (math != null) {
//            final DiffSystem sys = (DiffSystem) ruleApp.posInOccurrence()
//                    .subTerm().javaBlock().program().getFirstElement();
//            Set<String> variables = new LinkedHashSet<String>();
//            for (ProgramElement f : sys.getDifferentialEquations(services
//                    .getNamespaces())) {
//                collectVariables(f, variables);
//            }
            
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            final JTextField nGrapsPerRow = new JTextField("3");
            addTextFieldToPanel(panel, nGrapsPerRow, "nGrapsPerRow");
            final JTextField tendLimi = new JTextField("10");
            addTextFieldToPanel(panel, tendLimi, "tendLimi");
            final JTextField nUnroLoop = new JTextField("10");
            addTextFieldToPanel(panel, nUnroLoop, "nUnroLoop");
            final JTextField randMin = new JTextField("-10");
            addTextFieldToPanel(panel, randMin, "randMin");
            final JTextField randMax = new JTextField("10");
            addTextFieldToPanel(panel, randMax, "randMax");

//            final Map<String, JTextField> fields = new LinkedHashMap<String, JTextField>();
//            for (String s : variables) {
//                JPanel varPane = new JPanel();
//                varPane.add(new JLabel(s));
//                JTextField sField = new JTextField("0");
//                sField.setColumns(10);
//                varPane.add(sField);
//                fields.put(s, sField);
//                panel.add(varPane);
//            }
            final JDialog dialog = new JDialog();
            dialog.getContentPane().setLayout(new BorderLayout());
            dialog.setTitle("Specify initial values");
            dialog.getContentPane().add(panel, BorderLayout.CENTER);
            JButton ok = new JButton("Ok");
            ok.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent event) {
                    try {
//                        final Map<String, Double> initialValues = new LinkedHashMap<String, Double>();
//                        for (String s : fields.keySet()) {
//                            initialValues.put(s,
//                                    Double.parseDouble(fields.get(s).getText()));
//                        }
//                        Map<String, Double[][]> plotData = math.getPlotData(
//                                sys, "t$", Double.parseDouble(min.getText()),
//                                Double.parseDouble(max.getText()),
//                                Double.parseDouble(sampling.getText()),
//                                initialValues, services);
						Map<String, Double[][]> plotData = math.getPlotData(
								ruleApp.posInOccurrence().topLevel().subTerm(), services,
								Integer.parseInt(nGrapsPerRow.getText()),
								Double.parseDouble(tendLimi.getText()),
								Integer.parseInt(nUnroLoop.getText()),
								Double.parseDouble(randMin.getText()),
								Double.parseDouble(randMax.getText()));
						if(plotData == null) {
	                        dialog.setVisible(false);
	                        dialog.dispose();
							return;
						}
                    	JDialog sDia = new JDialog();
                        Plot2DPanel plot = new Plot2DPanel();
                        plot.addLegend(Plot2DPanel.EAST);
                        plot.addPlotToolBar(Plot2DPanel.NORTH);
                        plot.setAxisLabels("t", "x");
                        sDia.add(plot);
//                        for (String s : fields.keySet()) {
//                            if (plotData.get(s) != null) {
//                                plot.addLinePlot(s,
//                                        cDoubletodouble(plotData.get(s)));
//                            }
//                        }
                        for (String s : plotData.keySet()) {
                                plot.addLinePlot(s,
                                        cDoubletodouble(plotData.get(s)));
                        }
                        dialog.setVisible(false);
                        dialog.dispose();
                        // sDia.pack();
                        sDia.setSize(600, 400);
                        sDia.setVisible(true);
                    } catch (final RemoteException e) {
                        dialog.setVisible(false);
                        dialog.dispose();
                        Main.getInstance().notify(new ExceptionEvent(e));
                    } catch (final SolverException e) {
                        dialog.setVisible(false);
                        dialog.dispose();
                        Main.getInstance().notify(new ExceptionEvent(e));
                    }

                }

                private double[][] cDoubletodouble(Double[][] doubles) {
                    double[][] res = new double[doubles.length][];
                    for (int i = 0; i < res.length; i++) {
                        res[i] = new double[doubles[i].length];
                        for (int j = 0; j < res[i].length; j++) {
                            res[i][j] = doubles[i][j];
                        }
                    }
                    return res;
                }
            });
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            });
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(ok);
            buttonPanel.add(cancel);
            dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
            dialog.pack();
            dialog.setVisible(true);
        }
        return null;
    }

	public void addTextFieldToPanel(JPanel panel,
			final JTextField nGrapsPerRow, String nGrapsPerRowLabel) {
		JPanel nGrapsPerRowPane = new JPanel();
		nGrapsPerRowPane.add(new JLabel(nGrapsPerRowLabel));
		nGrapsPerRow.setColumns(10);
		nGrapsPerRowPane.add(nGrapsPerRow);
		panel.add(nGrapsPerRowPane);
	}

    /**
     * @param f
     * @param variables
     */
    private void collectVariables(ProgramElement f, Set<String> variables) {
        if (f instanceof DLNonTerminalProgramElement) {
            for (ProgramElement n : (DLNonTerminalProgramElement) f) {
                collectVariables(n, variables);
            }
        } else if (f instanceof ProgramVariable) {
            variables.add(((ProgramVariable) f).getElementName().toString());
        } else if (f instanceof LogicalVariable) {
            variables.add(((LogicalVariable) f).getElementName().toString());
        } else if (f instanceof FreeFunction) {
            variables.add(((FreeFunction) f).getElementName().toString());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#displayName()
     */
    public String displayName() {
        return "Plot Trajectory";
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#name()
     */
    public Name name() {
        return new Name("Plot Trajectory");
    }

    /* @Override */
    public String toString() {
        return displayName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.proof.RuleFilter#filter(de.uka.ilkd.key.rule.Rule)
     */
    public boolean filter(Rule rule) {
        return rule instanceof PlotRule;
    }

}
