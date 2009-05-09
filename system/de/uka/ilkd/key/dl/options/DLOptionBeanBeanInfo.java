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
import java.util.LinkedHashSet;
import java.util.Set;

import orbital.awt.TaggedPropertyEditorSupport;
import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.options.DLOptionBean.ApplyRules;
import de.uka.ilkd.key.dl.options.DLOptionBean.BuiltInArithmetic;
import de.uka.ilkd.key.dl.options.DLOptionBean.BuiltInArithmeticIneqs;
import de.uka.ilkd.key.dl.options.DLOptionBean.CounterexampleTest;
import de.uka.ilkd.key.dl.options.DLOptionBean.DiffSat;
import de.uka.ilkd.key.dl.options.DLOptionBean.FirstOrderStrategy;
import de.uka.ilkd.key.dl.options.DLOptionBean.InvariantRule;
import de.uka.ilkd.key.dl.options.DLOptionBean.LocalReduceOption;

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
                            "diffSat",
                            "Differential Saturation",
                            "select the desired automation degree of Differential Saturation for automatic differential induction",
                            false, false, DiffSatPropertyEditor.class),
                    createDescriptor(
                            "foStrategy",
                            "First Order Strategy",
                            "choose the strategy for first order goals. either STOP or completely UNFOLD, perform EAGER quantifier elimination or LAZY, or activate the Iterative Background Closure (IBC) strategy with incremental timeouts.",
                            false, true, FirstOrderStrategyPropertyEditor.class),
                    createDescriptor(
                            "useIterativeReduceRule",
                            "Iterative Inflation",
                            "whether to activate the Iterative Inflation Order (IIO) with increasingly bigger formulas.",
                            false, true),
                    createDescriptor(
                            "usePowersetIterativeReduce",
                            "Inflation Powerset",
                            "whether to use the powerset for iterative inflation or not",
                            false, true),
                    createDescriptor(
                            "percentOfPowersetForReduce",
                            "Inflation Percent",
                            "the percentage of the powerset to use for the iterative inflation",
                            false, true),

                    createDescriptor(
                            "counterexampleTest",
                            "counterexample",
                            "whether to check for counterexamples before trying to prove exhaustively",
                            false, true, CounterexampleTestPropertyEditor.class),
                    createDescriptor(
                            "initialTimeout",
                            "initial timeout",
                            "the timeout used in the first iteration of the IBC strategy (in seconds)",
                            false, true),
                    createDescriptor(
                            "linearTimeoutIncreaseFactor",
                            "linear timeout",
                            "the linear part of the IBC timeout. That is, the part c of t_new = a*t_old^2 + b*t_old + c (in seconds)"),
                    createDescriptor(
                            "constantTimeoutIncreaseFactor",
                            "constant timeout",
                            "the constant part of the IBC timeout. That is, the part c of t_new = a*t_old^2 + b*t_old + c (in seconds)",
                            false, false, true),
                    createDescriptor(
                            "quadraticTimeoutIncreaseFactor",
                            "quadratic timeout",
                            "the quadratic part of the IBC timeout. That is, the part c of t_new = a*t_old^2 + b*t_old + c (in seconds)",
                            false, false, true),
                    //
                    createDescriptor(
                            "diffSatTimeout",
                            "initial DiffSat timeout",
                            "the timeout used in the first iteration of the DiffSat strategy (in seconds)",
                            false, false),
                    createDescriptor(
                            "loopSatTimeout",
                            "initial LoopSat timeout",
                            "the timeout used in the first iteration of the LoopSat strategy (in seconds)",
                            false, false),
                    createDescriptor(
                            "simplifyTimeout",
                            "simplify timeout",
                            "the timeout used for calls to the simplifier (in seconds)",
                            // @TODO what does 0 mean?
                            false, false),
                    //
                    createDescriptor("simplifyBeforeReduce",
                            "simplify before reduce",
                            "simplify formulas passed to the reduce function of the arithmetic solver"),
                    createDescriptor(
                            "simplifyAfterReduce",
                            "simplify after reduce",
                            "simplify the results generated by the reduce function of the arithmetic solver"),
                    createDescriptor(
                            "simplifyAfterODESolve",
                            "simplify after ODESolve",
                            "simplify the results generated by the ODESolve function of the arithmetic solver"),
                            createDescriptor(
                                    "applyLocalSimplify",
                                    "local simplifications",
                                    "try to simplify single first-order subformulas (before trying to reduce the complete sequent)",
                                    true, false),
                    createDescriptor(
                            "applyLocalReduce",
                            "local reductions",
                            "try to eliminate quantifiers in single first-order formulas (before trying to reduce the complete sequent)",
                            true, false, LocalReducePropertyEditor.class),
                            createDescriptor("applyGlobalReduce", "global reductions",
                                    "try to reduce the complete sequent if possible",
                                    true, false),
                    createDescriptor(
                            "readdQuantifiers",
                            "re-add quantifiers",
                            "During quantifier elimination, re-add the quantfiers of previously quantified variables (i.e. Skolem symbols)",
                            true),
                    //
                    createDescriptor(
                            "builtInArithmetic",
                            "built-in arithmetic",
                            "select to which degree built-in arithmetic rules should be used",
                            false, false, BuiltInArithmeticPropertyEditor.class),
                    //
                    createDescriptor(
                            "builtInArithmeticIneqs",
                            "built-in inequalities",
                            "select whether built-in rules for inequalities are to be used",
                            false, false, BuiltInArithmeticIneqsPropertyEditor.class),
                    createDescriptor(
                    		"useSOS",
                            "semi-definite programs",
                            "select whether to use semi-definite programming and sum of squares rule",
                            false, false),
                            //
                    createDescriptor(
                            "quantifierEliminator",
                            "real arithmetic solver",
                            "select the solver for real arithmetic that should be used to eliminate quantifiers",
                            true, false,
                            QuantifierEliminatorPropertyEditor.class),

                    createDescriptor(
                            "groebnerBasisCalculator",
                            "equation solver",
                            "select the solver for handling equational theories, e.g., by Groebner bases",
                            true, false,
                            GroebnerBasisCalculatorPropertyEditor.class),
                    createDescriptor(
                            "sosChecker",
                            "sos checker",
                            "select the solver for handling the universal fragment of real arithmetic",
                            true, false,
                            SOSCheckerPropertyEditor.class),
                    createDescriptor(
                            "odeSolver",
                            "differential equations",
                            "select the solver that should be used to solve differential equations or handle them by differential induction",
                            true, false, ODESolversPropertyEditor.class),
                    createDescriptor("counterExampleGenerator",
                            "counterexample tool",
                            "select the tool for generating counterexamples",
                            true, false,
                            CounterExampleGeneratorPropertyEditor.class),
                    createDescriptor(
                            "simplifier",
                            "arithmetic simplifier",
                            "select the simplification algorithm that should be used to simplify arithmetical expressions",
                            true, false, SimplifierPropertyEditor.class),
                    createDescriptor(
                            "applyGammaRules",
                            "apply gamma rules",
                            "choose if and when gamma rules should be applied for existential quantifiers",
                            true, false, ApplyRulesPropertyEditor.class),
                    createDescriptor(
                    		"applyUpdatesToModalities",
                            "update modalities",
                            "apply updates to modalites e.g. to get simpler solutions for differential equations",
                            true, false),
                    //
                    createDescriptor(
                            "ignoreAnnotations",
                            "ignore @annotations",
                            "Whether to ignore all proof skeleton @annotations, like @invariant etc.",
                            true, false),
                    createDescriptor(
                    		"csdpBinary",
                            "csdp binary",
                            "The path to the csdp binary file. (Used by groebnerSOS and internal sos)",
                            true, false, FilePropertyEditor.class),
                    createDescriptor(
                            "csdpForceInternal",
                            "force libcsdp",
                            "Force KeYmaerar to use the library version of csdp instead of the binary.",
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

        private static String[] getNames() {
            Set<String> names = MathSolverManager.getODESolvers();
            HashSet<String> values = new LinkedHashSet<String>();
            values.add("");
            values.add("-");
            for (String name : names) {
                values.add(MathSolverManager.getODESolver(name).getName());
            }
            return values.toArray(new String[0]);
        }

        public ODESolversPropertyEditor() {
            super(getNames(), getNames());
        }
    }

    public static class CounterExampleGeneratorPropertyEditor extends
            TaggedPropertyEditorSupport {

        private static String[] getNames() {
            Set<String> names = MathSolverManager.getCounterExampleGenerators();
            HashSet<String> values = new LinkedHashSet<String>();
            values.add("");
            values.add("-");
            for (String name : names) {
                values.add(MathSolverManager.getCounterExampleGenerator(name)
                        .getName());
            }

            return values.toArray(new String[0]);
        }

        public CounterExampleGeneratorPropertyEditor() {
            super(getNames(), getNames());
        }
    }

    public static class QuantifierEliminatorPropertyEditor extends
            TaggedPropertyEditorSupport {

        private static String[] getNames() {
            Set<String> names = MathSolverManager.getQuantifierEliminators();
            HashSet<String> values = new LinkedHashSet<String>();
            values.add("");
            values.add("-");
            for (String name : names) {
                values.add(MathSolverManager.getQuantifierElimantor(name)
                        .getName());
            }
            return values.toArray(new String[0]);
        }

        public QuantifierEliminatorPropertyEditor() {
            super(getNames(), getNames());
        }
    }

    public static class SimplifierPropertyEditor extends
            TaggedPropertyEditorSupport {

        private static String[] getNames() {
            Set<String> names = MathSolverManager.getSimplifiers();
            HashSet<String> values = new LinkedHashSet<String>();
            values.add("");
            values.add("-");
            for (String name : names) {
                values.add(MathSolverManager.getSimplifier(name).getName());
            }

            return values.toArray(new String[0]);
        }

        public SimplifierPropertyEditor() {
            super(getNames(), getNames());
        }
    }

    public static class GroebnerBasisCalculatorPropertyEditor extends
            TaggedPropertyEditorSupport {

        private static String[] getNames() {
            Set<String> names = MathSolverManager.getGroebnerBasisCalculators();
            HashSet<String> values = new LinkedHashSet<String>();
            values.add("");
            values.add("-");
            for (String name : names) {
                values.add(MathSolverManager.getGroebnerBasisCalculator(name)
                        .getName());
            }
            return values.toArray(new String[0]);
        }

        public GroebnerBasisCalculatorPropertyEditor() {
            super(getNames(), getNames());
        }
    }

    public static class SOSCheckerPropertyEditor extends
    TaggedPropertyEditorSupport {
    	
    	private static String[] getNames() {
    		Set<String> names = MathSolverManager.getSOSCheckers();
    		HashSet<String> values = new LinkedHashSet<String>();
    		values.add("");
    		values.add("-");
    		for (String name : names) {
    			values.add(MathSolverManager.getSOSChecker(name)
    					.getName());
    		}
    		return values.toArray(new String[0]);
    	}
    	
    	public SOSCheckerPropertyEditor() {
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

    public static class BuiltInArithmeticPropertyEditor extends
            TaggedPropertyEditorSupport {
        public BuiltInArithmeticPropertyEditor() {
            super(getNames(BuiltInArithmetic.values()), BuiltInArithmetic
                    .values());
        }
    }

    public static class BuiltInArithmeticIneqsPropertyEditor extends
            TaggedPropertyEditorSupport {
        public BuiltInArithmeticIneqsPropertyEditor() {
            super(getNames(BuiltInArithmeticIneqs.values()), BuiltInArithmeticIneqs.values());
        }
    }

    public static class InvariantRulePropertyEditor extends
            TaggedPropertyEditorSupport {
        public InvariantRulePropertyEditor() {
            super(getNames(InvariantRule.values()), InvariantRule.values());
        }
    }

    public static class FirstOrderStrategyPropertyEditor extends
            TaggedPropertyEditorSupport {
        public FirstOrderStrategyPropertyEditor() {
            super(getNames(FirstOrderStrategy.values()), FirstOrderStrategy
                    .values());
        }
    }

    public static class LocalReducePropertyEditor extends
            TaggedPropertyEditorSupport {
        public LocalReducePropertyEditor() {
            super(getNames(LocalReduceOption.values()), LocalReduceOption
                    .values());
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
