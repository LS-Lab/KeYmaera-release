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
package de.uka.ilkd.key.dl.options;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import orbital.awt.TaggedPropertyEditorSupport;
import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.options.DLOptionBean.ApplyRules;
import de.uka.ilkd.key.dl.options.DLOptionBean.CounterexampleTest;
import de.uka.ilkd.key.dl.options.DLOptionBean.DiffSat;
import de.uka.ilkd.key.dl.options.DLOptionBean.InvariantRule;

/**
 * @author jdq
 * 
 */
public class DLOptionBeanBeanInfo extends SimpleBeanInfo {
    public static final String DESCRIPTION = "Adjusts KeYmaera proof strategy options";

    private static final Class<DLOptionBean> beanClass = DLOptionBean.class;

    public DLOptionBeanBeanInfo() {
    }

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor d = new BeanDescriptor(beanClass);
        d.setDisplayName("KeYmaera");
        d.setShortDescription(DESCRIPTION);
        return d;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor[] pds = new PropertyDescriptor[] {
                    // expert, preferred, hidden
                    createDescriptor(
                            "counterexampleTest",
                            "quickly check for counterexamples",
                            "whether to check for counterexamples before trying to prove exhaustively",
                            false, true, CounterexampleTestPropertyEditor.class),
                    createDescriptor(
                            "useTimeoutStrategy",
                            "Iterative Background Closure",
                            "whether to activate the Iterative Background Closure (IBC) strategy with incremental timeouts. Otherwise, call reduce when first-order",
                            false, true),
                    createDescriptor(
                            "initialTimeout",
                            "initial timeout",
                            "the timeout used in the first iteration of the IBC strategy (in milliseconds)",
                            false, true),
                    createDescriptor(
                            "linearTimeoutIncreaseFactor",
                            "linear timeout increase factor",
                            "the linear part of the IBC timeout. That is, the part c of t_new = a*t_old^2 + b*t_old + c (in milliseconds)"),
                    createDescriptor(
                            "constantTimeoutIncreaseFactor",
                            "constant timeout increase factor",
                            "the constant part of the IBC timeout. That is, the part c of t_new = a*t_old^2 + b*t_old + c (in milliseconds)"),
                    createDescriptor(
                            "quadraticTimeoutIncreaseFactor",
                            "quadratic timeout increase factor",
                            "the quadratic part of the IBC timeout. That is, the part c of t_new = a*t_old^2 + b*t_old + c (in milliseconds)"),
                    //
                    createDescriptor("diffSat", "DiffSat strategy",
                            "select which degree of DiffSat strategy to apply",
                            false, false, DiffSatPropertyEditor.class),
                    createDescriptor(
                            "diffSatTimeout",
                            "initial DiffSat timeout",
                            "the timeout used in the first iteration of the DiffSat strategy (in milliseconds)",
                            false, false),
                    createDescriptor(
                            "loopSatTimeout",
                            "initial LoopSat timeout",
                            "the timeout used in the first iteration of the LoopSat strategy (in milliseconds)",
                            false, false),
                    //
                    createDescriptor("callReduce", "call reduce",
                            "try to reduce the whole sequent", true, false),
                    createDescriptor("simplifyBeforeReduce",
                            "simplify before reduce",
                            "simplify formulas passed to the reduce function of the arithmetic solver"),
                    createDescriptor(
                            "simplifyAfterReduce",
                            "simplify after reduce",
                            "simplify the results generated by the reduce function of the arithmetic solver"),
                    createDescriptor(
                            "readdQuantifiers",
                            "re-add quantifiers",
                            "During quantifier elimination, re-add the quantfiers of previously quantified variables (i.e. Skolem symbols)",
                            true),
                    createDescriptor(
                            "normalizeEquations",
                            "normalize inqualities",
                            "normalize inequalities to greater than and greater equals on the antecedent of the sequent, i.e., to the form a>=b ==> or a>b ==>"),
                    createDescriptor(
                            "applyGammaRules",
                            "apply gamma rules",
                            "choose if and when gamma rules should be applied for existential quantifiers",
                            true, false, ApplyRulesPropertyEditor.class),
                    createDescriptor(
                            "applyUpdatesToModalities",
                            "apply updates to modalities",
                            "apply updates to modalites e.g. to get more simpler solutions for differential equations",
                            true, false),
                    //
                    createDescriptor(
                            "quantifierEliminator",
                            "real arithmetic solver",
                            "select the arithmetic solver that should be used to eliminate quantifiers",
                            true, false,
                            QuantifierEliminatorPropertyEditor.class),
                    createDescriptor(
                            "simplifier",
                            "simplifier for arithmetic expressions",
                            "select the arithmetic tool that should be used to simplify expressions",
                            true, false, SimplifierPropertyEditor.class),
                    createDescriptor(
                            "odeSolver",
                            "differential equation solver",
                            "select the arithmetic solver that should be used to solve or otherwise handle differential equations",
                            true, false, ODESolversPropertyEditor.class),
                    createDescriptor(
                            "counterExampleGenerator",
                            "counterexample generator",
                            "select the arithmetic tool that should be used to generate counter examples",
                            true, false,
                            CounterExampleGeneratorPropertyEditor.class),
                    //
                    createDescriptor(
                            "splitBeyondFO",
                            "split FO formulas",
                            "simple heuristic: call reduce if only FO formulas are left in the sequent",
                            true, false, true),
                    createDescriptor(
                            "stopAtFO",
                            "stop strategy on first order goals",
                            "if enabled the strategy will not apply rules to goals that are already first order",
                            true, false),
                    createDescriptor(
                            "ignoreAnnotations",
                            "ignore proof annotations",
                            "Whether to ignore all proof skeleton @annotations",
                            true, false),
            // createDescriptor("invariantRule", "invariant rule",
            // "choose which invariant rule should be used", true,
            // false, InvariantRulePropertyEditor.class),

            };
            return pds;
        } catch (IntrospectionException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static PropertyDescriptor createDescriptor(String propertyName,
            String displayName, String shortDescription)
            throws IntrospectionException {
        return createDescriptor(propertyName, displayName, shortDescription,
                false);
    }

    private static PropertyDescriptor createDescriptor(String propertyName,
            String displayName, String shortDescription, boolean expert)
            throws IntrospectionException {
        return createDescriptor(propertyName, displayName, shortDescription,
                expert, false);
    }

    private static PropertyDescriptor createDescriptor(String propertyName,
            String displayName, String shortDescription, boolean expert,
            boolean preferred) throws IntrospectionException {
        return createDescriptor(propertyName, displayName, shortDescription,
                expert, preferred, null);
    }

    private static PropertyDescriptor createDescriptor(String propertyName,
            String displayName, String shortDescription, boolean expert,
            boolean preferred, boolean hidden) throws IntrospectionException {
        return createDescriptor(propertyName, displayName, shortDescription,
                expert, preferred, hidden, null);
    }

    private static PropertyDescriptor createDescriptor(String propertyName,
            String displayName, String shortDescription, boolean expert,
            boolean preferred, Class<?> propertyEditor)
            throws IntrospectionException {
        return createDescriptor(propertyName, displayName, shortDescription,
                expert, preferred, false, propertyEditor);
    }

    private static PropertyDescriptor createDescriptor(String propertyName,
            String displayName, String shortDescription, boolean expert,
            boolean preferred, boolean hidden, Class<?> propertyEditor)
            throws IntrospectionException {
        PropertyDescriptor result = new PropertyDescriptor(propertyName,
                beanClass);
        result.setDisplayName(displayName);
        result.setShortDescription(shortDescription);
        result.setExpert(expert);
        result.setPreferred(preferred);
        result.setHidden(hidden);
        if (propertyEditor != null) {
            result.setPropertyEditorClass(propertyEditor);
        }
        return result;
    }

    // public static class UsagePropertyEditor extends
    // TaggedPropertyEditorSupport {
    // public UsagePropertyEditor() {
    // super(new String[] {
    // "evaluation", "testing", "end user"
    // }, new Object[] {
    // new Integer(ProgSettings.EVALUATION), new Integer(ProgSettings.TESTING),
    // new Integer(ProgSettings.END_USER)
    // }, new String[] {
    // "ProgSettings.EVALUATION", "ProgSettings.TESTING",
    // "ProgSettings.END_USER"
    // });
    // }
    // }

    public static class ODESolversPropertyEditor extends
            TaggedPropertyEditorSupport {

        private static HashSet<String> values;

        private static String[] getNames() {
            if (values == null) {
                Set<String> names = MathSolverManager.getODESolvers();
                values = new HashSet<String>();
                for (String name : names) {
                    values.add(MathSolverManager.getODESolver(name).getName());
                }
                values.add("");
            }
            return values.toArray(new String[0]);
        }

        public ODESolversPropertyEditor() {
            super(getNames(), getNames());
        }
    }

    public static class CounterExampleGeneratorPropertyEditor extends
            TaggedPropertyEditorSupport {
        private static HashSet<String> values;

        private static String[] getNames() {
            if (values == null) {
                Set<String> names = MathSolverManager
                        .getCounterExampleGenerators();
                values = new HashSet<String>();
                for (String name : names) {
                    values.add(MathSolverManager.getCounterExampleGenerator(
                            name).getName());
                }
                values.add("");
            }
            return values.toArray(new String[0]);
        }

        public CounterExampleGeneratorPropertyEditor() {
            super(getNames(), getNames());
        }
    }

    public static class QuantifierEliminatorPropertyEditor extends
            TaggedPropertyEditorSupport {
        private static HashSet<String> values;

        private static String[] getNames() {
            if (values == null) {
                Set<String> names = MathSolverManager
                        .getQuantifierEliminators();
                values = new HashSet<String>();
                for (String name : names) {
                    values.add(MathSolverManager.getQuantifierElimantor(name)
                            .getName());
                }
                values.add("");
            }
            return values.toArray(new String[0]);
        }

        public QuantifierEliminatorPropertyEditor() {
            super(getNames(), getNames());
        }
    }

    public static class SimplifierPropertyEditor extends
            TaggedPropertyEditorSupport {
        private static HashSet<String> values;

        private static String[] getNames() {
            if (values == null) {
                Set<String> names = MathSolverManager.getSimplifiers();
                values = new HashSet<String>();
                for (String name : names) {
                    values.add(MathSolverManager.getSimplifier(name).getName());
                }
                values.add("");
            }
            return values.toArray(new String[0]);
        }

        public SimplifierPropertyEditor() {
            super(getNames(), getNames());
        }
    }

    public static class ApplyRulesPropertyEditor extends
            TaggedPropertyEditorSupport {
        public ApplyRulesPropertyEditor() {
            super(getNames(ApplyRules.values()), ApplyRules.values());
        }
    }

    public static class CounterexampleTestPropertyEditor extends
            TaggedPropertyEditorSupport {
        public CounterexampleTestPropertyEditor() {
            super(getNames(CounterexampleTest.values()), CounterexampleTest
                    .values());
        }
    }

    public static class DiffSatPropertyEditor extends
            TaggedPropertyEditorSupport {
        public DiffSatPropertyEditor() {
            super(getNames(DiffSat.values()), DiffSat.values());
        }
    }

    public static class InvariantRulePropertyEditor extends
            TaggedPropertyEditorSupport {
        public InvariantRulePropertyEditor() {
            super(getNames(InvariantRule.values()), InvariantRule.values());
        }
    }

    private static <E extends Enum<E>> String[] getNames(Enum<E> vals[]) {
        java.util.List<String> names = new ArrayList<String>();
        for (Enum<E> r : vals) {
            names.add(r.toString());
        }
        return names.toArray(new String[0]);
    }

}
