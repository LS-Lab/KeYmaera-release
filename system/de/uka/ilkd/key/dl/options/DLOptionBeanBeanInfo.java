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

import de.uka.ilkd.key.dl.options.DLOptionBean.CexFinder;
import de.uka.ilkd.key.dl.options.DLOptionBean.TracerStat;

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
						"diffSat", EPropertyConstant.DLOPTIONS_USE_DIFF_SAT,false, false, DiffSatPropertyEditor.class),
					createDescriptor(
							"foStrategy", EPropertyConstant.DLOPTIONS_FO_STRATEGY,false, true, FirstOrderStrategyPropertyEditor.class),
							createDescriptor(
								"counterexampleTest",EPropertyConstant.DLOPTIONS_COUNTEREXAMPLE_TEST,false, true, CounterexampleTestPropertyEditor.class),
							//
							createDescriptor(
									"quantifierEliminator", EPropertyConstant.DLOPTIONS_QUANTIFIER_ELIMINATOR,
									false, false, QuantifierEliminatorPropertyEditor.class),

							createDescriptor(
									"groebnerBasisCalculator", EPropertyConstant.DLOPTIONS_GROEBNER_BASIS_CALCULATOR,
									false, false, GroebnerBasisCalculatorPropertyEditor.class),
							createDescriptor(
									"odeSolver", EPropertyConstant.DLOPTIONS_ODESOLVER,
									false, false, ODESolversPropertyEditor.class),
							createDescriptor(
									"counterExampleGenerator", EPropertyConstant.DLOPTIONS_COUNTEREXAMPLE_GENERATOR,
									false, false, CounterExampleGeneratorPropertyEditor.class),
							createDescriptor(
									"simplifier", EPropertyConstant.DLOPTIONS_SIMPLIFIER,
									false, false, SimplifierPropertyEditor.class),
									createDescriptor(
										"builtInArithmetic", EPropertyConstant.DLOPTIONS_BUILT_IN_ARITHMETIC, 
										false, false, BuiltInArithmeticPropertyEditor.class),
								//
								createDescriptor(
										"builtInArithmeticIneqs", EPropertyConstant.DLOPTIONS_BUILT_IN_ARITHMETIC_INEQS,
										false, false, BuiltInArithmeticIneqsPropertyEditor.class),
								//
										createDescriptor(
											"sosChecker", EPropertyConstant.DLOPTIONS_SOS_CHECKER,
											true, false, SOSCheckerPropertyEditor.class),
					createDescriptor(
							"useIterativeReduceRule", EPropertyConstant.DLOPTIONS_ITERATIVE_REDUCE_RULE,false, true, true),
					createDescriptor(
					        "cexFinder", EPropertyConstant.DLOPTIONS_CEX_FINDER, true, false, CexFinderPropertyEditor.class),
					createDescriptor(
					        "tracerStat", EPropertyConstant.DLOPTIONS_TRACER_STAT, false, true, TracerStatPropertyEditor.class),
					createDescriptor(
							"usePowersetIterativeReduce", EPropertyConstant.DLOPTIONS_USE_POWERSET_ITERATIVE_REDUCE,false, true, true),
					createDescriptor(
							"percentOfPowersetForReduce", EPropertyConstant.DLOPTIONS_PERCENT_OF_POWERSET_FOR_ITERATIVE_REDUCE,false, true, true),
					createDescriptor(
							"initialTimeout", EPropertyConstant.DLOPTIONS_INITIAL_TIMEOUT,false, true),
					createDescriptor(
							"linearTimeoutIncreaseFactor",EPropertyConstant.DLOPTIONS_LINEAR, true),					
					createDescriptor(
							"constantTimeoutIncreaseFactor", EPropertyConstant.DLOPTIONS_CONSTANT, true, false, true),
					createDescriptor(
							"quadraticTimeoutIncreaseFactor", EPropertyConstant.DLOPTIONS_QUADRIC,true, false, true),
					createDescriptor(
							"diffSatTimeout", EPropertyConstant.DLOPTIONS_DIFFSAT_TIMEOUT,false, false),
					createDescriptor(
							"loopSatTimeout", EPropertyConstant.DLOPTIONS_LOOPSAT_TIMEOUT,false, false),
					createDescriptor(
							"simplifyTimeout", EPropertyConstant.DLOPTIONS_SIMPLIFY_TIMEOUT,
							// @TODO what does 0 mean?
							true, false),
					//
					createDescriptor("simplifyBeforeReduce", EPropertyConstant.DLOPTIONS_SIMPLIFY_BEFORE_REDUCE, true),
					createDescriptor(
							"simplifyAfterReduce", EPropertyConstant.DLOPTIONS_SIMPLIFY_AFTER_REDUCE, true),
					createDescriptor(
							"simplifyAfterODESolve",EPropertyConstant.DLOPTIONS_SIMPLIFY_AFTER_ODESOLVE, true),
					createDescriptor(
							"applyLocalSimplify", EPropertyConstant.DLOPTIONS_APPLY_LOCAL_SIMPLIFY,true),
					createDescriptor(
							"applyLocalReduce", EPropertyConstant.DLOPTIONS_APPLY_LOCAL_REDUCE,true, false, LocalReducePropertyEditor.class),
					createDescriptor("applyGlobalReduce", EPropertyConstant.DLOPTIONS_APPLY_GLOBAL_REDUCE,true, false),
					createDescriptor(
							"readdQuantifiers", EPropertyConstant.DLOPTIONS_READD_QUANTIFIERS,true, false, false),
					createDescriptor(
							"applyGammaRules", EPropertyConstant.DLOPTIONS_APPLY_GAMMA_RULES,
							true, false, false, ApplyRulesPropertyEditor.class),
					/*createDescriptor(
							"applyUpdatesToModalities", EPropertyConstant.DLOPTIONS_APPLY_UPDATES_TO_MODALITIES,
							true, false),*/
					//
					createDescriptor(
							"ignoreAnnotations", EPropertyConstant.DLOPTIONS_IGNORE_ANNOTATIONS,
							true, false),
					createDescriptor(
							"useSOS", EPropertyConstant.DLOPTIONS_USE_SOS,
							true, false),
					createDescriptor(
							"csdpBinary", EPropertyConstant.DLOPTIONS_CSDP_PATH,
							true, false, true, FilePropertyEditor.class),
					createDescriptor(
							"csdpForceInternal", EPropertyConstant.DLOPTIONS_CSDP_FORCE_INTERNAL,
							true, false),
					createDescriptor(
									"resetStrategyAfterEveryRun", EPropertyConstant.DLOPTIONS_RESET_STRATEGY_AFTER_EVERY_RUN,
									true, false),
					createDescriptor(
									 "useODEIndFinMethods", EPropertyConstant.DLOPTIONS_USE_ODE_IND_FIN_METHODS,
									        true, false),
					createDescriptor(
									 "reduceOnFreshBranch", EPropertyConstant.DLOPTIONS_REDUCE_ON_FRESH_BRANCH,
									        true, false),
					createDescriptor(
									 "solveODE", EPropertyConstant.DLOPTIONS_SOLVE_ODE,
									        true, false),
					createDescriptor("ibcOnlyToFO", EPropertyConstant.DLOPTIONS_IBC_ONLY_TO_FO,
									                true, false),
					createDescriptor("qeOnlyToTrue", EPropertyConstant.DLOPTIONS_QE_ONLY_TO_TRUE,
									                true, false),
                    createDescriptor("invariantRule", EPropertyConstant.DLOPTIONS_INVARIANT_RULE, true,
                        false, InvariantRulePropertyEditor.class),
                    createDescriptor("addRigidFormulas", EPropertyConstant.DLOPTIONS_ADD_RIGID_FORMULAS,
                            true, false),
                    createDescriptor("pretendWhileLoadingQE", EPropertyConstant.DLOPTIONS_PRETEND_QE_TRUE_WHILE_LOADING,
                            true, false),
                    createDescriptor("universalClosureOnQE", EPropertyConstant.DLOPTIONS_UNIVERSAL_CLOSURE_ON_QE,
                            true, false),

			};
			return pds;
		} catch (IntrospectionException ex) {
			ex.printStackTrace();
			return null;
		}
	}

//	private static PropertyDescriptor createDescriptor(String propertyName,
//			String displayName, String shortDescription)
//			throws IntrospectionException {
//		return createDescriptor(propertyName, displayName, shortDescription,
//				false);
//	}
//
//	private static PropertyDescriptor createDescriptor(String propertyName,
//			String displayName, String shortDescription, boolean expert)
//			throws IntrospectionException {
//		return createDescriptor(propertyName, displayName, shortDescription,
//				expert, false);
//	}
//
//	private static PropertyDescriptor createDescriptor(String propertyName,
//			String displayName, String shortDescription, boolean expert,
//			boolean preferred) throws IntrospectionException {
//		return createDescriptor(propertyName, displayName, shortDescription,
//				expert, preferred, null);
//	}
//
//	private static PropertyDescriptor createDescriptor(String propertyName,
//			String displayName, String shortDescription, boolean expert,
//			boolean preferred, boolean hidden) throws IntrospectionException {
//		return createDescriptor(propertyName, displayName, shortDescription,
//				expert, preferred, hidden, null);
//	}
//
//	private static PropertyDescriptor createDescriptor(String propertyName,
//			String displayName, String shortDescription, boolean expert,
//			boolean preferred, Class<?> propertyEditor)
//			throws IntrospectionException {
//		return createDescriptor(propertyName, displayName, shortDescription,
//				expert, preferred, false, propertyEditor);
//	}
//
	private static PropertyDescriptor createDescriptor(String propertyName,EPropertyConstant propertyConstant)
			throws IntrospectionException {
	    	return createDescriptor(propertyName, propertyConstant,
	    		false);
	}

	private static PropertyDescriptor createDescriptor(String propertyName,EPropertyConstant propertyConstant, boolean expert)
			throws IntrospectionException {
	    	return createDescriptor(propertyName, propertyConstant,
	    		expert, false);
	}

	private static PropertyDescriptor createDescriptor(String propertyName,EPropertyConstant propertyConstant, boolean expert,
			boolean preferred) throws IntrospectionException {
	    		return createDescriptor(propertyName, propertyConstant,
	    		        expert, preferred, null);
	}	

	private static PropertyDescriptor createDescriptor(String propertyName,EPropertyConstant propertyConstant, boolean expert,
			boolean preferred, boolean hidden) throws IntrospectionException {
	    	return createDescriptor(propertyName, propertyConstant,
	    			expert, preferred, hidden, null);
	}

	private static PropertyDescriptor createDescriptor(String propertyName,EPropertyConstant propertyConstant, boolean expert,
			boolean preferred, Class<?> propertyEditor)
			throws IntrospectionException {
	    	return createDescriptor(propertyName, propertyConstant,
	    			expert, preferred, false, propertyEditor);
	}

	private static PropertyDescriptor createDescriptor(String propertyName,EPropertyConstant propertyConstant, boolean expert,
							boolean preferred, boolean hidden, Class<?> propertyEditor)
								throws IntrospectionException {
	    		PropertyDescriptor result = new PropertyDescriptor(propertyName,beanClass);
	    		result.setDisplayName(propertyConstant.getLabel());
	    		result.setShortDescription(propertyConstant.getToolTip());
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
				values.add(MathSolverManager.getSOSChecker(name).getName());
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
			super(getNames(BuiltInArithmeticIneqs.values()),
					BuiltInArithmeticIneqs.values());
		}
	}

	public static class InvariantRulePropertyEditor extends
			TaggedPropertyEditorSupport {
		public InvariantRulePropertyEditor() {
			super(getNamesAndReplace(InvariantRule.values()), InvariantRule.values());
		}

        private static <E extends Enum<E>> String[] getNamesAndReplace(Enum<E> vals[]) {
            java.util.List<String> names = new ArrayList<String>();
            for (Enum<E> r : vals) {
                String s = r.toString();
                if(s.equals("loop_inv_box_quan")) {
                    s = "local";
                } else if(s.equals("loop_inv_box_fresh")) {
                    s = "global";
                }
                names.add(s);
            }
            return names.toArray(new String[0]);
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

	      public static class CexFinderPropertyEditor extends
	                       TaggedPropertyEditorSupport {
	               public CexFinderPropertyEditor() {
	                       super(getNames(CexFinder.values()), CexFinder
	                                       .values());
	               }
	       }
	
	       public static class TracerStatPropertyEditor extends
	                       TaggedPropertyEditorSupport {
	               public TracerStatPropertyEditor() {
	                       super(getNames(TracerStat.values()), TracerStat
	                                       .values());
	               }
	       }
	

}
