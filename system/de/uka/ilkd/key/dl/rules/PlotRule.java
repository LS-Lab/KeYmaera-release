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

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.Mathematica;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.*;
import de.uka.ilkd.key.dl.parser.NumberCache;
import de.uka.ilkd.key.dl.strategy.features.FOSequence;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.gui.notification.events.ExceptionEvent;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.*;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.logic.sort.ProgramSVSort;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.RuleFilter;
import de.uka.ilkd.key.rule.BuiltInRule;
import de.uka.ilkd.key.rule.Rule;
import de.uka.ilkd.key.rule.RuleApp;
import org.math.plot.Plot2DPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.util.*;

/**
 * This rule is used to plot trajectories of differential equation systems
 *
 * @author jdq
 * @since 15.08.2012
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
//            return pio.isTopLevel()
//                    || ProgramSVSort.DL_SIMPLE_ORDINARY_DIFF_SYSTEM_SORT_INSTANCE
//                    .canStandFor(((DLStatementBlock) pio.subTerm()
//                            .javaBlock().program()).getChildAt(0),
//                            null, goal.proof().getServices());
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.uka.ilkd.key.rule.Rule#apply(de.uka.ilkd.key.proof.Goal,
     * de.uka.ilkd.key.java.Services, de.uka.ilkd.key.rule.RuleApp)
     */
    public synchronized ImmutableList<Goal> apply(final Goal goal,
                                                  final Services services, final RuleApp ruleApp) {
        final Mathematica math = (Mathematica) MathSolverManager
                .getQuantifierElimantor("Mathematica");
        if (math != null) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            final JDialog dialog = new JDialog();
            dialog.getContentPane().setLayout(new BorderLayout());
            dialog.setTitle("Specify initial values");
            dialog.getContentPane().add(panel, BorderLayout.CENTER);
            JButton ok = new JButton("Ok");

            if (ruleApp.posInOccurrence().subTerm().javaBlock().program()
                    .getFirstElement() instanceof DiffSystem) {
                final DiffSystem sys = (DiffSystem) ruleApp.posInOccurrence()
                        .subTerm().javaBlock().program().getFirstElement();
                Set<String> variables = new LinkedHashSet<String>();
                for (ProgramElement f : sys.getDifferentialEquations(services
                        .getNamespaces())) {
                    collectVariables(f, variables);
                }
                final JTextField min = new JTextField("0");
                addTextFieldToPanel(panel, min, "Min for t");
                final JTextField max = new JTextField("10");
                addTextFieldToPanel(panel, max, "Max for t");
                final JTextField sampling = new JTextField("0.1");
                addTextFieldToPanel(panel, sampling, "Sampling");

                final Map<String, JTextField> fields = new LinkedHashMap<String, JTextField>();
                for (String s : variables) {
                    JPanel varPane = new JPanel();
                    varPane.add(new JLabel(s));
                    JTextField sField = new JTextField("0");
                    sField.setColumns(10);
                    varPane.add(sField);
                    fields.put(s, sField);
                    panel.add(varPane);
                }
                ok.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent event) {
                        try {

                            final Map<String, Double> initialValues = new LinkedHashMap<String, Double>();
                            for (String s : fields.keySet()) {
                                initialValues.put(s, Double.parseDouble(fields
                                        .get(s).getText()));
                            }
                            Map<String, Double[][]> plotData = math
                                    .getPlotData(sys, "t$", Double
                                            .parseDouble(min.getText()), Double
                                            .parseDouble(max.getText()), Double
                                            .parseDouble(sampling.getText()),
                                            initialValues, services);
                            JDialog sDia = new JDialog();
                            Plot2DPanel plot = new Plot2DPanel();
                            plot.addLegend(Plot2DPanel.EAST);
                            plot.addPlotToolBar(Plot2DPanel.NORTH);
                            plot.setAxisLabels("t", "x");
                            sDia.add(plot);
                            for (String s : fields.keySet()) {
                                if (plotData.get(s) != null) {
                                    plot.addLinePlot(s,
                                            cDoubletodouble(plotData.get(s)));
                                }
                            }
                            dialog.setVisible(false);
                            dialog.dispose();
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
                });
            } else {

//				final JTextField nGrapsPerRow = new JTextField("3");
//				addTextFieldToPanel(panel, nGrapsPerRow, "nGrapsPerRow");
                final JTextField tendLimi = new JTextField("10");
                addTextFieldToPanel(panel, tendLimi, "Time to follow ODEs");
                final JTextField nUnroLoop = new JTextField("10");
                addTextFieldToPanel(panel, nUnroLoop, "Number of loop unrollings");
                final JTextField randMin = new JTextField("-10");
                addTextFieldToPanel(panel, randMin, "Min for random values");
                final JTextField randMax = new JTextField("10");
                addTextFieldToPanel(panel, randMax, "Max for random values");
                // add a list of variables that should be plotted
                Term subTerm = ruleApp.posInOccurrence().subTerm();
                final LinkedHashSet<String> vars = new LinkedHashSet<String>();
                collectVariables((ProgramElement) subTerm.javaBlock().program()
                        .getFirstElement(), vars);
                final LinkedHashMap<String, JCheckBox> selection = new LinkedHashMap<String, JCheckBox>();
                final LinkedHashMap<String, JTextField> initialValues = new LinkedHashMap<String, JTextField>();
                final JPanel selectionPanel = new JPanel();
                selectionPanel.setLayout(new BoxLayout(selectionPanel, BoxLayout.Y_AXIS));
                selectionPanel.add(new JLabel("Variables to Plot:"));
                Term t = TermBuilder.DF.tt();
                for (ConstrainedFormula f : goal.sequent().antecedent()) {
                    if (isFirstOrderFormula(f)) {
                        t = TermBuilder.DF.and(t, f.formula());
                    }
                }
                for (ConstrainedFormula f : goal.sequent().succedent()) {
                    if (isFirstOrderFormula(f)) {
                        t = TermBuilder.DF.and(t,
                                TermBuilder.DF.not(f.formula()));
                    }
                }
                Map<String, Double> instances = new LinkedHashMap<String, Double>();
                try {
                    instances.putAll(math.findInstanceD(t, -1, services));
                } catch (RemoteException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (SolverException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                Random r = new Random();
                double min = Double.parseDouble(randMin.getText());
                for (String s : vars) {
                    JPanel var = new JPanel(new BorderLayout());
                    selectionPanel.add(var);
                    JCheckBox box = new JCheckBox(s);
                    var.add(box, BorderLayout.WEST);
                    JTextField init = new JTextField();
                    Double d = instances.get(s);
                    if (d != null) {
                        init.setText("" + d);
                    } else {
                        BigDecimal rand = new BigDecimal(min + r.nextDouble() * ((Double.parseDouble(randMax.getText()) - min) + 1));
                        BigDecimal randUp = rand.multiply(new BigDecimal(100));
                        BigDecimal rounded = randUp.setScale(0, RoundingMode.HALF_UP);
                        init.setText("" + (rounded.divide(new BigDecimal(100))));
                    }
                    var.add(init);
                    box.setSelected(true);
                    selection.put(s, box);
                    initialValues.put(s, init);
                }
                panel.add(selectionPanel);
                // collect assumptions about the initial values for our
                // simulation


                ok.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent event) {
                        if(Double.parseDouble(tendLimi.getText()) <= 0) {
                            JOptionPane.showMessageDialog(null, "Time to follow ODEs has to be strictly positive.\nGiven input was: " + tendLimi.getText(), "Input Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        Term subTerm = ruleApp.posInOccurrence().subTerm();
                        Term t = TermBuilder.DF.tt();
                        for (String s : vars) {
                            Named n = services.getNamespaces().lookup(new Name(s));
                            Term var = null;
                            if (n instanceof de.uka.ilkd.key.logic.op.ProgramVariable) {
                                var = TermBuilder.DF.var((de.uka.ilkd.key.logic.op.ProgramVariable) n);
                            } else if (n instanceof LogicVariable) {
                                var = TermBuilder.DF.var((LogicVariable) n);
                            } else if (n instanceof RigidFunction) {
                                var = TermBuilder.DF.func((RigidFunction) n);
                            } else {
                                System.err.println("Don't know what to do with " + n + " of type " + n.getClass());
                            }
                            String initial = initialValues.get(s).getText();
                            BigDecimal d;
                            try {
                                d = new BigDecimal(initial);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                d = new BigDecimal(0);
                            }
                            if (var != null) {
                                Term number;
                                if (d.compareTo(BigDecimal.ZERO) < 0) {
                                    number = TermBuilder.DF.func(RealLDT.getFunctionFor(MinusSign.class),
                                            TermBuilder.DF.func(NumberCache.getNumber(d.negate(), RealLDT.getRealSort())));
                                } else {
                                    number = TermBuilder.DF.func(NumberCache.getNumber(d, RealLDT.getRealSort()));
                                }
                                t = TermBuilder.DF.and(t, TermBuilder.DF.equals(var, number));
                            }
                        }
                        subTerm = TermBuilder.DF.imp(t, subTerm);
                        dialog.setVisible(false);
                        dialog.dispose();
                        final JDialog calc = new JDialog();
                        calc.setTitle("Simulating...");
                        calc.getContentPane().setLayout(new BorderLayout());
                        calc.getContentPane()
                                .add(new JLabel(
                                        "<html><body>Calculating the simulation data.<br>This might take a while.</body></html>"),
                                        BorderLayout.CENTER);
                        JButton abort = new JButton("Abort");
                        calc.getContentPane().add(abort, BorderLayout.SOUTH);
                        abort.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                try {
                                    math.abortCalculation();
                                } catch (RemoteException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }
                                calc.setVisible(false);
                                calc.dispose();
                            }
                        });
                        final Term input = subTerm;
                        Runnable r = new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    final Map<String, Double[][]> plotData = math
                                            .getPlotData(
                                                    input,
                                                    services,
                                                    Double.parseDouble(tendLimi
                                                            .getText()),
                                                    Integer.parseInt(nUnroLoop
                                                            .getText()),
                                                    Double.parseDouble(randMin.getText()),
                                                    Double.parseDouble(randMax
                                                            .getText()));
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            calc.setVisible(false);
                                            calc.dispose();
                                            if (plotData == null) {
                                                return;
                                            }
                                            JDialog sDia = new JDialog();
                                            Plot2DPanel plot = new Plot2DPanel();
                                            plot.addLegend(Plot2DPanel.EAST);
                                            plot.addPlotToolBar(Plot2DPanel.NORTH);
                                            plot.setAxisLabels("t", "x");
                                            sDia.add(plot);
                                            for (String s : plotData.keySet()) {
                                                JCheckBox jCheckBox = selection.get(s);
                                                // the checkbox might be null for the
                                                // global safety thingy
                                                if (jCheckBox == null
                                                        || jCheckBox.isSelected()) {
                                                    plot.addLinePlot(s,
                                                            cDoubletodouble(plotData
                                                                    .get(s)));
                                                }
                                            }
                                            sDia.setSize(600, 400);
                                            sDia.setVisible(true);
                                        }
                                    });

                                } catch (final RemoteException e) {
                                    Main.getInstance().notify(
                                            new ExceptionEvent(e));
                                } catch (final SolverException e) {
                                    Main.getInstance().notify(
                                            new ExceptionEvent(e));
                                }
                            }

                        };
                        calc.pack();
                        calc.setLocationRelativeTo(null);
                        calc.setVisible(true);
                        new Thread(r).start();

                    }

                });
            }

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


    public boolean isFirstOrderFormula(ConstrainedFormula f) {
        final boolean[] result = {true};
        f.formula().execPreOrder(new Visitor() {

            /* @Override */
            public void visit(Term visited) {
                if (!FOSequence.isFOOperator(visited.op())) {
                    result[0] = false;
                }
            }

        });
        boolean fo = result[0];
        return fo;
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
