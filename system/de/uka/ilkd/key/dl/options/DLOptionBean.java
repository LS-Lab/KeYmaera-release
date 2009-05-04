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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.gui.GUIEvent;
import de.uka.ilkd.key.gui.KeYMediator;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.gui.configuration.Settings;
import de.uka.ilkd.key.gui.configuration.SettingsListener;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.IteratorOfGoal;
import de.uka.ilkd.key.proof.Proof;

/**
 * @author jdq
 * 
 */
public class DLOptionBean implements Settings {

	/**
	 * @author jdq TODO Documentation since Feb 19, 2009
	 */
	public enum LocalReduceOption {
		OFF, EXISTENTIAL, ALWAYS;
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
	}

	public static enum ApplyRules {
		ALWAYS("Always"), NEVER("Never"), ONLY_TO_MODALITIES("To modalities");

		private String string;

		private ApplyRules(String str) {
			this.string = str;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		/* @Override */
		public String toString() {
			return string;
		}
	}

	public static enum FirstOrderStrategy {
		STOP("stop"), UNFOLD("unfold"), EAGER("eager"), IBC("IBC"), LAZY("lazy");

		private String string;

		private FirstOrderStrategy(String str) {
			this.string = str;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		/* @Override */
		public String toString() {
			return string;
		}
	}

	public static enum DiffSat {
		BLIND("blind"), OFF("off"), SIMPLE("simple"), DIFF("diffauto"), AUTO(
				"auto");

		private String string;

		private DiffSat(String str) {
			this.string = str;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		/* @Override */
		public String toString() {
			return string;
		}
	}

	public static enum CounterexampleTest {
		OFF("off"), ON("on"), TRANSITIONS("transitions");

		private String string;

		private CounterexampleTest(String str) {
			this.string = str;
		}

		/* @Override */
		public String toString() {
			return string;
		}
	}

	public static enum InvariantRule {
		QUANTIFIERS("loop_inv_box_quan");

		private String name;

		private InvariantRule(String name) {
			this.name = name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		/* @Override */
		public String toString() {
			return name;
		}
	}

	public static enum BuiltInArithmetic {
		OFF("off"), NORMALISE_EQUATIONS("normalise"), REDUCTION(
				"normalise&reduce"), FULL("full S-polynomial");

		private String string;

		private BuiltInArithmetic(String str) {
			this.string = str;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		/* @Override */
		public String toString() {
			return string;
		}
	}

	public static enum BuiltInArithmeticIneqs {
		OFF("off"), FOURIER_MOTZKIN("Fourier-Motzkin");

		private String string;

		private BuiltInArithmeticIneqs(String str) {
			this.string = str;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		/* @Override */
		public String toString() {
			return string;
		}
	}

	/**
	 * 
	 */
	private static final String TRUE = Boolean.valueOf(true).toString();

	/**
	 * 
	 */
	private static final String DLOPTIONS_INITIAL_TIMEOUT = "[DLOptions]initialTimeout";

	private static final String DLOPTIONS_DIFFSAT_TIMEOUT = "[DLOptions]diffSatTimeout";

	private static final String DLOPTIONS_LOOPSAT_TIMEOUT = "[DLOptions]loopSatTimeout";

	/**
	 * 
	 */
	private static final String DLOPTIONS_FO_STRATEGY = "[DLOptions]FOStrategy";

	public static final DLOptionBean INSTANCE = new DLOptionBean();

	private static final String DLOPTIONS_QUADRIC = "[DLOptions]quadricTimeoutIncreaseFactor";

	private static final String DLOPTIONS_LINEAR = "[DLOptions]linearTimeoutIncreaseFactor";

	private static final String DLOPTIONS_CONSTANT = "[DLOptions]constantTimeoutIncreaseFactor";

	private static final String DLOPTIONS_READD_QUANTIFIERS = "[DLOptions]readdQuantifiers";

	private static final String DLOPTIONS_SIMPLIFY_BEFORE_REDUCE = "[DLOptions]simplifyBeforeReduce";

	private static final String DLOPTIONS_SIMPLIFY_AFTER_REDUCE = "[DLOptions]simplifyAfterReduce";

	private static final String DLOPTIONS_APPLY_UPDATES_TO_MODALITIES = "[DLOptions]applyToModality";

	private static final String DLOPTIONS_COUNTEREXAMPLE_GENERATOR = "[DLOptions]counterExampleGenerator";

	private static final String DLOPTIONS_ODESOLVER = "[DLOptions]odeSolver";

	private static final String DLOPTIONS_QUANTIFIER_ELIMINATOR = "[DLOptions]quantifierEliminator";

	private static final String DLOPTIONS_SIMPLIFIER = "[DLOptions]simplifier";

	private static final String DLOPTIONS_APPLY_GAMMA_RULES = "[DLOptions]applyGammaRules";

	private static final String DLOPTIONS_COUNTEREXAMPLE_TEST = "[DLOptions]counterexampleTest";

	private static final String DLOPTIONS_INVARIANT_RULE = "[DLOptions]invariantRule";

	private static final String DLOPTIONS_USE_DIFF_SAT = "[DLOptions]DiffSat";

	private static final String DLOPTIONS_IGNORE_ANNOTATIONS = "[DLOptions]ignoreAnnotations";

	private static final String DLOPTIONS_SIMPLIFY_TIMEOUT = "[DLOptions]simplifyTimeout";

	private static final String DLOPTIONS_ITERATIVE_REDUCE_RULE = "[DLOptions]useIterativeReduceRule";

	private static final String DLOPTIONS_TERM_FACTORY_CLASS = "[DLOptions]termFactoryClass";

	private static final String DLOPTIONS_APPLY_LOCAL_REDUCE = "[DLOptions]applyLocalReduce";

	private static final String DLOPTIONS_APPLY_LOCAL_SIMPLIFY = "[DLOptions]applyLocalSimplify";

	private static final String DLOPTIONS_APPLY_GLOBAL_REDUCE = "[DLOptions]applyGlobalReduce";

	private static final String DLOPTIONS_SIMPLIFY_AFTER_ODESOLVE = "[DLOptions]simplifyAfterODESolve";

	private static final String DLOPTIONS_GROEBNER_BASIS_CALCULATOR = "[DLOptions]groebnerBasisCalculator";

	private static final String DLOPTIONS_SOS_CHECKER = "[DLOptions]sosChecker";

	private static final String DLOPTIONS_USE_POWERSET_ITERATIVE_REDUCE = "[DLOptions]usePowersetIterativeReduce";
	private static final String DLOPTIONS_PERCENT_OF_POWERSET_FOR_ITERATIVE_REDUCE = "[DLOptions]percentOfPowersetForIterativeReduce";

	private static final String DLOPTIONS_BUILT_IN_ARITHMETIC = "[DLOptions]BuiltInArithmetic";
	private static final String DLOPTIONS_BUILT_IN_ARITHMETIC_INEQS = "[DLOptions]BuiltInArithmeticIneqs";
	private static final String DLOPTIONS_USE_SOS = "[DLOptions]useSOS";

	private Set<Settings> subOptions;

	private FirstOrderStrategy foStrategy;

	private long initialTimeout;

	private int quadraticTimeoutIncreaseFactor;

	private int linearTimeoutIncreaseFactor;

	private int constantTimeoutIncreaseFactor;

	private long diffSatTimeout;

	private long loopSatTimeout;

	private HashSet<SettingsListener> listeners;

	private boolean readdQuantifiers;

	private boolean simplifyBeforeReduce;

	private boolean simplifyAfterReduce;

	private boolean applyUpdatesToModalities;

	private CounterexampleTest counterexampleTest;

	private DiffSat diffSatStrategy;

	private String counterExampleGenerator;

	private String odeSolver;

	private String quantifierEliminator;

	private String simplifier;

	private String sosChecker;

	private ApplyRules applyGammaRules;

	private InvariantRule invariantRule;

	private boolean ignoreAnnotations;

	private int simplifyTimeout;

	private boolean useIterativeReduceRule;

	private Class<? extends TermFactory> termFactoryClass;

	private LocalReduceOption applyLocalReduce;

	private boolean applyLocalSimplify;

	private boolean applyGlobalReduce;

	private boolean simplifyAfterODESolve;

	private String groebnerBasisCalculator;

	private boolean usePowersetIterativeReduce;

	private boolean useSOS;

	private int percentOfPowersetForReduce;

	private BuiltInArithmetic builtInArithmetic;
	private BuiltInArithmeticIneqs builtInArithmeticIneqs;

	private DLOptionBean() {
		subOptions = new LinkedHashSet<Settings>();
		foStrategy = FirstOrderStrategy.IBC;
		initialTimeout = 2;
		diffSatTimeout = 4;
		loopSatTimeout = 2000;
		quadraticTimeoutIncreaseFactor = 0;
		linearTimeoutIncreaseFactor = 2;
		constantTimeoutIncreaseFactor = 0;
		simplifyTimeout = 0;
		readdQuantifiers = true;
		simplifyBeforeReduce = false;
		simplifyAfterReduce = false;
		simplifyAfterODESolve = false;
		applyUpdatesToModalities = false;
		counterExampleGenerator = "";
		odeSolver = "";
		quantifierEliminator = "";
		simplifier = "";
		groebnerBasisCalculator = "";
		sosChecker = "";
		applyGammaRules = ApplyRules.ONLY_TO_MODALITIES;
		counterexampleTest = CounterexampleTest.ON;
		invariantRule = InvariantRule.QUANTIFIERS;
		diffSatStrategy = DiffSat.AUTO;
		ignoreAnnotations = false;
		useIterativeReduceRule = false;
		termFactoryClass = de.uka.ilkd.key.dl.model.impl.TermFactoryImpl.class;
		applyLocalReduce = LocalReduceOption.OFF;
		applyLocalSimplify = false;
		applyGlobalReduce = true;
		usePowersetIterativeReduce = true;
		percentOfPowersetForReduce = 70;
		builtInArithmetic = BuiltInArithmetic.OFF;
		builtInArithmeticIneqs = BuiltInArithmeticIneqs.OFF;
		useSOS = false;

		listeners = new HashSet<SettingsListener>();
	}

	/**
	 * @return the callReduce
	 */
	public FirstOrderStrategy getFoStrategy() {
		return foStrategy;
	}

	/**
	 * @param callReduce
	 *            the callReduce to set
	 */
	public void setFoStrategy(FirstOrderStrategy callReduce) {
		if (this.foStrategy != callReduce) {
			this.foStrategy = callReduce;
			firePropertyChanged();
		}
	}

	/**
	 * @return the initialTimeout
	 */
	public long getInitialTimeout() {
		return initialTimeout;
	}

	/**
	 * Sets the initialTimeout if the given value is non-negative. Zero is used
	 * as minimum value otherwise.
	 * 
	 * @param initialTimeout
	 *            the initialTimeout to set
	 */
	public void setInitialTimeout(long initialTimeout) {
		if (initialTimeout < 0) {
			initialTimeout = 0;
		}
		this.initialTimeout = initialTimeout;
		firePropertyChanged();
	}

	private void firePropertyChanged() {
		// System.out.println("Property changed");//XXX
		// TODO: iterate over all proofs
		final KeYMediator mediator = Main.getInstance().mediator();
		Proof proof = mediator.getProof();
		if (proof != null) {
			proof.setActiveStrategy(mediator.getProfile()
					.getDefaultStrategyFactory().create(proof, null));
			IteratorOfGoal iterator = proof.openGoals().iterator();
			while (iterator.hasNext()) {
				Goal next = iterator.next();
				next.clearAndDetachRuleAppIndex();
			}
		}
		for (SettingsListener l : listeners) {
			l.settingsChanged(new GUIEvent(this));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.gui.Settings#addSettingsListener(de.uka.ilkd.key.gui.
	 * SettingsListener)
	 */
	public void addSettingsListener(SettingsListener l) {
		listeners.add(l);
		for (Settings sub : subOptions) {
			sub.addSettingsListener(l);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.gui.Settings#readSettings(java.util.Properties)
	 */
	public void readSettings(Properties props) {
		for (Settings sub : subOptions) {
			sub.readSettings(props);
		}
		String property = props.getProperty(DLOPTIONS_FO_STRATEGY);
		if (property != null) {
			foStrategy = FirstOrderStrategy.valueOf(property);
		}
		property = props.getProperty(DLOPTIONS_INITIAL_TIMEOUT);
		if (property != null) {
			initialTimeout = Math
					.round(((float) Integer.parseInt(property)) / 1000f);
		}
		property = props.getProperty(DLOPTIONS_QUADRIC);
		if (property != null) {
			quadraticTimeoutIncreaseFactor = Integer.parseInt(property);
		}
		property = props.getProperty(DLOPTIONS_LINEAR);
		if (property != null) {
			linearTimeoutIncreaseFactor = Integer.parseInt(property);
		}
		property = props.getProperty(DLOPTIONS_CONSTANT);
		if (property != null) {
			constantTimeoutIncreaseFactor = Integer.parseInt(property);
		}
		property = props.getProperty(DLOPTIONS_READD_QUANTIFIERS);
		if (property != null) {
			readdQuantifiers = property.equals(TRUE);
		}
		property = props.getProperty(DLOPTIONS_SIMPLIFY_BEFORE_REDUCE);
		if (property != null) {
			simplifyBeforeReduce = property.equals(TRUE);
		}
		property = props.getProperty(DLOPTIONS_SIMPLIFY_AFTER_REDUCE);
		if (property != null) {
			simplifyAfterReduce = property.equals(TRUE);
		}
		property = props.getProperty(DLOPTIONS_APPLY_UPDATES_TO_MODALITIES);
		if (property != null) {
			applyUpdatesToModalities = property.equals(TRUE);
		}
		property = props.getProperty(DLOPTIONS_COUNTEREXAMPLE_TEST);
		if (property != null) {
			counterexampleTest = CounterexampleTest.valueOf(property);
		}
		property = props.getProperty(DLOPTIONS_IGNORE_ANNOTATIONS);
		if (property != null) {
			ignoreAnnotations = property.equals(TRUE);
		}

		counterExampleGenerator = props
				.getProperty(DLOPTIONS_COUNTEREXAMPLE_GENERATOR);
		if (counterExampleGenerator == null) {
			setCounterExampleGenerator("-");
		} else if (!(MathSolverManager.getCounterExampleGenerators()
				.contains(counterExampleGenerator))
				&& !counterExampleGenerator.equals("-")) {
			if (!MathSolverManager.getCounterExampleGenerators().isEmpty()) {
				setCounterExampleGenerator(MathSolverManager
						.getCounterExampleGenerators().iterator().next());
			} else {
				setCounterExampleGenerator("-");
			}
		}
		odeSolver = props.getProperty(DLOPTIONS_ODESOLVER);
		if (odeSolver == null) {
			setOdeSolver("-");
		} else if (!(MathSolverManager.getODESolvers().contains(odeSolver))
				&& !odeSolver.equals("-")) {
			if (!MathSolverManager.getODESolvers().isEmpty()) {
				setOdeSolver(MathSolverManager.getODESolvers().iterator()
						.next());
			} else {
				setOdeSolver("-");
			}
		}
		quantifierEliminator = props
				.getProperty(DLOPTIONS_QUANTIFIER_ELIMINATOR);
		if (quantifierEliminator == null) {
			setQuantifierEliminator("-");
		} else if (!(MathSolverManager.getQuantifierEliminators()
				.contains(quantifierEliminator))
				&& !quantifierEliminator.equals("-")) {
			if (!MathSolverManager.getQuantifierEliminators().isEmpty()) {
				setQuantifierEliminator(MathSolverManager
						.getQuantifierEliminators().iterator().next());
			} else {
				setQuantifierEliminator("-");
			}
		}
		simplifier = props.getProperty(DLOPTIONS_SIMPLIFIER);
		if (simplifier == null) {
			setSimplifier("-");
		} else if (!(MathSolverManager.getSimplifiers().contains(simplifier))
				&& !simplifier.equals("-")) {
			if (!MathSolverManager.getSimplifiers().isEmpty()) {
				setSimplifier(MathSolverManager.getSimplifiers().iterator()
						.next());
			} else {
				setSimplifier("-");
			}
		}
		groebnerBasisCalculator = props
				.getProperty(DLOPTIONS_GROEBNER_BASIS_CALCULATOR);
		if(groebnerBasisCalculator == null) {
			groebnerBasisCalculator = "";
		}
		sosChecker = props
			.getProperty(DLOPTIONS_SOS_CHECKER);
		if(sosChecker == null) {
			sosChecker = "";
		}
		/*
		 * HACK: this causes infinity loop if (groebnerBasisCalculator == null)
		 * { setGroebnerBasisCalculator(""); } else if
		 * (!(MathSolverManager.getGroebnerBasisCalculators()
		 * .contains(groebnerBasisCalculator)) &&
		 * !groebnerBasisCalculator.equals("-")) { if
		 * (!MathSolverManager.getGroebnerBasisCalculators().isEmpty()) {
		 * setGroebnerBasisCalculator(MathSolverManager
		 * .getGroebnerBasisCalculators().iterator().next()); } else {
		 * setGroebnerBasisCalculator("-"); } }
		 */
		property = props.getProperty(DLOPTIONS_APPLY_GAMMA_RULES);
		if (property != null) {
			applyGammaRules = ApplyRules.valueOf(property);
		}
		property = props.getProperty(DLOPTIONS_INVARIANT_RULE);
		if (property != null) {
			invariantRule = InvariantRule.valueOf(property);
		}
		property = props.getProperty(DLOPTIONS_USE_DIFF_SAT);
		if (property != null) {
			diffSatStrategy = DiffSat.valueOf(property);
		}
		property = props.getProperty(DLOPTIONS_DIFFSAT_TIMEOUT);
		if (property != null) {
			diffSatTimeout = Math
					.round(((float) Integer.parseInt(property)) / 1000f);
		}
		property = props.getProperty(DLOPTIONS_LOOPSAT_TIMEOUT);
		if (property != null) {
			loopSatTimeout = Math
					.round(((float) Integer.parseInt(property)) / 1000f);
		}
		property = props.getProperty(DLOPTIONS_SIMPLIFY_TIMEOUT);
		if (property != null) {
			simplifyTimeout = Integer.parseInt(property);
		}
		property = props.getProperty(DLOPTIONS_ITERATIVE_REDUCE_RULE);
		if (property != null) {
			useIterativeReduceRule = Boolean.valueOf(property);
		}
		property = props.getProperty(DLOPTIONS_TERM_FACTORY_CLASS);
		if (property != null) {
			try {
				termFactoryClass = (Class<? extends TermFactory>) getClass()
						.getClassLoader().loadClass(property);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		property = props.getProperty(DLOPTIONS_APPLY_LOCAL_REDUCE);
		if (property != null) {
			if (property.equalsIgnoreCase("false")) {
				applyLocalReduce = LocalReduceOption.OFF;
			} else if (property.equalsIgnoreCase("true")) {
				applyLocalReduce = LocalReduceOption.ALWAYS;
			} else {
				applyLocalReduce = LocalReduceOption.valueOf(property);
			}
		}

		property = props.getProperty(DLOPTIONS_APPLY_LOCAL_SIMPLIFY);
		if (property != null) {
			applyLocalSimplify = Boolean.valueOf(property);
		}

		property = props.getProperty(DLOPTIONS_APPLY_GLOBAL_REDUCE);
		if (property != null) {
			applyGlobalReduce = Boolean.valueOf(property);
		}

		property = props.getProperty(DLOPTIONS_SIMPLIFY_AFTER_ODESOLVE);
		if (property != null) {
			simplifyAfterODESolve = Boolean.valueOf(property);
		}

		property = props.getProperty(DLOPTIONS_USE_POWERSET_ITERATIVE_REDUCE);
		if (property != null) {
			usePowersetIterativeReduce = Boolean.valueOf(property);
		}
		property = props.getProperty(DLOPTIONS_USE_SOS);
		if (property != null) {
			useSOS = Boolean.valueOf(property);
		}

		property = props
				.getProperty(DLOPTIONS_PERCENT_OF_POWERSET_FOR_ITERATIVE_REDUCE);
		if (property != null) {
			percentOfPowersetForReduce = Integer.valueOf(property);
		}

		property = props.getProperty(DLOPTIONS_BUILT_IN_ARITHMETIC);
		if (property != null) {
			builtInArithmetic = BuiltInArithmetic.valueOf(property);
		}

		property = props.getProperty(DLOPTIONS_BUILT_IN_ARITHMETIC_INEQS);
		if (property != null) {
			builtInArithmeticIneqs = BuiltInArithmeticIneqs.valueOf(property);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.gui.Settings#writeSettings(java.util.Properties)
	 */
	public void writeSettings(Properties props) {
		for (Settings sub : subOptions) {
			sub.writeSettings(props);
		}
		props.setProperty(DLOPTIONS_FO_STRATEGY, foStrategy.name());
		props
				.setProperty(DLOPTIONS_INITIAL_TIMEOUT, "" + initialTimeout
						* 1000);
		props.setProperty(DLOPTIONS_QUADRIC, ""
				+ quadraticTimeoutIncreaseFactor);
		props.setProperty(DLOPTIONS_LINEAR, "" + linearTimeoutIncreaseFactor);
		props.setProperty(DLOPTIONS_CONSTANT, ""
				+ constantTimeoutIncreaseFactor);

		props.setProperty(DLOPTIONS_READD_QUANTIFIERS, Boolean.valueOf(
				readdQuantifiers).toString());
		props.setProperty(DLOPTIONS_SIMPLIFY_BEFORE_REDUCE, Boolean.valueOf(
				simplifyBeforeReduce).toString());
		props.setProperty(DLOPTIONS_SIMPLIFY_AFTER_REDUCE, Boolean.valueOf(
				simplifyAfterReduce).toString());
		props.setProperty(DLOPTIONS_APPLY_UPDATES_TO_MODALITIES, Boolean
				.valueOf(applyUpdatesToModalities).toString());
		props.setProperty(DLOPTIONS_COUNTEREXAMPLE_TEST, counterexampleTest
				.name());
		props.setProperty(DLOPTIONS_IGNORE_ANNOTATIONS, Boolean
				.toString(ignoreAnnotations));

		if (counterExampleGenerator != null) {
			props.setProperty(DLOPTIONS_COUNTEREXAMPLE_GENERATOR,
					counterExampleGenerator);
		}
		if (odeSolver != null) {
			props.setProperty(DLOPTIONS_ODESOLVER, odeSolver);
		}
		if (quantifierEliminator != null) {
			props.setProperty(DLOPTIONS_QUANTIFIER_ELIMINATOR,
					quantifierEliminator);
		} else {
			props.setProperty(DLOPTIONS_QUANTIFIER_ELIMINATOR, "-");
		}
		if (simplifier != null) {
			props.setProperty(DLOPTIONS_SIMPLIFIER, simplifier);
		}
		if (groebnerBasisCalculator != null) {
			props.setProperty(DLOPTIONS_GROEBNER_BASIS_CALCULATOR,
					groebnerBasisCalculator);
		}
		if (sosChecker != null) {
			props.setProperty(DLOPTIONS_SOS_CHECKER,
					sosChecker);
		}

		props.setProperty(DLOPTIONS_APPLY_GAMMA_RULES, applyGammaRules.name());
		props.setProperty(DLOPTIONS_INVARIANT_RULE, invariantRule.name());

		props.setProperty(DLOPTIONS_USE_DIFF_SAT, diffSatStrategy.name());
		props
				.setProperty(DLOPTIONS_DIFFSAT_TIMEOUT, "" + diffSatTimeout
						* 1000);
		props
				.setProperty(DLOPTIONS_LOOPSAT_TIMEOUT, "" + loopSatTimeout
						* 1000);
		props.setProperty(DLOPTIONS_SIMPLIFY_TIMEOUT, "" + simplifyTimeout);

		props.setProperty(DLOPTIONS_ITERATIVE_REDUCE_RULE, Boolean
				.toString(useIterativeReduceRule));
		props.setProperty(DLOPTIONS_TERM_FACTORY_CLASS, termFactoryClass
				.getName());
		props
				.setProperty(DLOPTIONS_APPLY_LOCAL_REDUCE, applyLocalReduce
						.name());
		props.setProperty(DLOPTIONS_APPLY_LOCAL_SIMPLIFY, Boolean
				.toString(applyLocalSimplify));
		props.setProperty(DLOPTIONS_APPLY_GLOBAL_REDUCE, Boolean
				.toString(applyGlobalReduce));
		props.setProperty(DLOPTIONS_SIMPLIFY_AFTER_ODESOLVE, Boolean
				.toString(simplifyAfterODESolve));
		props.setProperty(DLOPTIONS_USE_POWERSET_ITERATIVE_REDUCE, Boolean
				.toString(usePowersetIterativeReduce));
		props.setProperty(DLOPTIONS_USE_SOS, Boolean
				.toString(useSOS));
		props.setProperty(DLOPTIONS_PERCENT_OF_POWERSET_FOR_ITERATIVE_REDUCE,
				"" + percentOfPowersetForReduce);
		props.setProperty(DLOPTIONS_BUILT_IN_ARITHMETIC, builtInArithmetic
				.name());
		props.setProperty(DLOPTIONS_BUILT_IN_ARITHMETIC_INEQS,
				builtInArithmeticIneqs.name());
	}

	public void addSubOptionBean(Settings sub) {
		subOptions.add(sub);
		for (SettingsListener l : listeners) {
			sub.addSettingsListener(l);
		}
	}

	/**
	 * @return the constantTimeoutIncreaseFactor
	 */
	public int getConstantTimeoutIncreaseFactor() {
		return constantTimeoutIncreaseFactor;
	}

	/**
	 * @param constantTimeoutIncreaseFactor
	 *            the constantTimeoutIncreaseFactor to set
	 */
	public void setConstantTimeoutIncreaseFactor(
			int constantTimeoutIncreaseFactor) {
		if (constantTimeoutIncreaseFactor != this.constantTimeoutIncreaseFactor) {
			this.constantTimeoutIncreaseFactor = constantTimeoutIncreaseFactor;
			firePropertyChanged();
		}
	}

	/**
	 * @return the linearTimeoutIncreaseFactor
	 */
	public int getLinearTimeoutIncreaseFactor() {
		return linearTimeoutIncreaseFactor;
	}

	/**
	 * @param linearTimeoutIncreaseFactor
	 *            the linearTimeoutIncreaseFactor to set
	 */
	public void setLinearTimeoutIncreaseFactor(int linearTimeoutIncreaseFactor) {
		if (linearTimeoutIncreaseFactor != this.linearTimeoutIncreaseFactor) {
			this.linearTimeoutIncreaseFactor = linearTimeoutIncreaseFactor;
			firePropertyChanged();
		}
	}

	/**
	 * @return the quadricTimeoutIncreaseFactor
	 */
	public int getQuadraticTimeoutIncreaseFactor() {
		return quadraticTimeoutIncreaseFactor;
	}

	/**
	 * @param quadricTimeoutIncreaseFactor
	 *            the quadricTimeoutIncreaseFactor to set
	 */
	public void setQuadraticTimeoutIncreaseFactor(
			int quadricTimeoutIncreaseFactor) {
		if (quadricTimeoutIncreaseFactor != quadraticTimeoutIncreaseFactor) {
			quadraticTimeoutIncreaseFactor = quadricTimeoutIncreaseFactor;
			firePropertyChanged();
		}
	}

	/**
	 * @return
	 */
	public boolean isReaddQuantifiers() {
		return readdQuantifiers;
	}

	/**
	 * @param readdQuantifiers
	 *            the readdQuantifiers to set
	 */
	public void setReaddQuantifiers(boolean readdQuantifiers) {
		this.readdQuantifiers = readdQuantifiers;
		firePropertyChanged();
	}

	/**
	 * @return
	 */
	public boolean isSimplifyBeforeReduce() {
		return simplifyBeforeReduce;
	}

	/**
	 * @return
	 */
	public boolean isSimplifyAfterReduce() {
		return simplifyAfterReduce;
	}

	/**
	 * @param simplifyAfterReduce
	 *            the simplifyAfterReduce to set
	 */
	public void setSimplifyAfterReduce(boolean simplifyAfterReduce) {
		this.simplifyAfterReduce = simplifyAfterReduce;
		firePropertyChanged();
	}

	/**
	 * @param simplifyBeforeReduce
	 *            the simplifyBeforeReduce to set
	 */
	public void setSimplifyBeforeReduce(boolean simplifyBeforeReduce) {
		this.simplifyBeforeReduce = simplifyBeforeReduce;
		firePropertyChanged();
	}

	public boolean isNormalizeEquations() {
		return builtInArithmetic == BuiltInArithmetic.NORMALISE_EQUATIONS
				|| builtInArithmetic == BuiltInArithmetic.REDUCTION
				|| builtInArithmetic == BuiltInArithmetic.FULL
				|| builtInArithmeticIneqs == BuiltInArithmeticIneqs.FOURIER_MOTZKIN;
	}

	public boolean isArithmeticReduction() {
		return builtInArithmetic == BuiltInArithmetic.REDUCTION
				|| builtInArithmetic == BuiltInArithmetic.FULL;
	}

	public boolean isArithmeticSaturation() {
		return builtInArithmetic == BuiltInArithmetic.FULL;
	}

	public boolean isFourierMotzkin() {
		return builtInArithmeticIneqs == BuiltInArithmeticIneqs.FOURIER_MOTZKIN;
	}

	/**
	 * @return the applyUpdatesToModalities
	 */
	public boolean isApplyUpdatesToModalities() {
		return applyUpdatesToModalities;
	}

	/**
	 * @param applyUpdatesToModalities
	 *            the applyUpdatesToModalities to set
	 */
	public void setApplyUpdatesToModalities(boolean applyUpdatesToModalities) {
		this.applyUpdatesToModalities = applyUpdatesToModalities;
		firePropertyChanged();
	}

	/**
	 * @return the counterExampleGenerator
	 */
	public String getCounterExampleGenerator() {
		return counterExampleGenerator;
	}

	/**
	 * @param counterExampleGenerator
	 *            the counterExampleGenerator to set
	 */
	public void setCounterExampleGenerator(String counterExampleGenerator) {
		if(!this.counterExampleGenerator.equals(counterExampleGenerator)) {
			this.counterExampleGenerator = counterExampleGenerator;
			firePropertyChanged();
		}
	}

	/**
	 * @return the odeSolver
	 */
	public String getOdeSolver() {
		return odeSolver;
	}

	/**
	 * @param odeSolver
	 *            the odeSolver to set
	 */
	public void setOdeSolver(String odeSolver) {
		this.odeSolver = odeSolver;
		firePropertyChanged();
	}

	/**
	 * @return the quantifierEliminator
	 */
	public String getQuantifierEliminator() {
		return quantifierEliminator;
	}

	/**
	 * @param quantifierEliminator
	 *            the quantifierEliminator to set
	 */
	public void setQuantifierEliminator(String quantifierEliminator) {
		if (!(quantifierEliminator == null && this.quantifierEliminator == null)
				|| !quantifierEliminator.equals(this.quantifierEliminator)) {
			this.quantifierEliminator = quantifierEliminator;
			firePropertyChanged();
		}
	}

	/**
	 * @return the simplfier
	 */
	public String getSimplifier() {
		return simplifier;
	}

	/**
	 * @param simplfier
	 *            the simplfier to set
	 */
	public void setSimplifier(String simplifier) {
		this.simplifier = simplifier;
		firePropertyChanged();
	}

	/**
	 * @return the applyGammaRules
	 */
	public ApplyRules getApplyGammaRules() {
		return applyGammaRules;
	}

	/**
	 * @param applyGammaRules
	 *            the applyGammaRules to set
	 */
	public void setApplyGammaRules(ApplyRules applyGammaRules) {
		this.applyGammaRules = applyGammaRules;
		firePropertyChanged();
	}

	public Set<Settings> getSubOptions() {
		return subOptions;
	}

	/**
	 * @return the useFindInstanceTest
	 */
	public CounterexampleTest getCounterexampleTest() {
		return counterexampleTest;
	}

	/**
	 * @param useFindInstanceTest
	 *            the useFindInstanceTest to set
	 */
	public void setCounterexampleTest(CounterexampleTest t) {
		this.counterexampleTest = t;
		firePropertyChanged();
	}

	/**
	 * @return the invariantRule
	 */
	public InvariantRule getInvariantRule() {
		return invariantRule;
	}

	/**
	 * @param invariantRule
	 *            the invariantRule to set
	 */
	public void setInvariantRule(InvariantRule invariantRule) {
		this.invariantRule = invariantRule;
		firePropertyChanged();
	}

	/**
	 * @return the useDiffSAT
	 */
	public DiffSat getDiffSat() {
		return diffSatStrategy;
	}

	/**
	 * @param useDiffSAT
	 *            the useDiffSAT to set
	 */
	public void setDiffSat(DiffSat useDiffSAT) {
		this.diffSatStrategy = useDiffSAT;
		firePropertyChanged();
	}

	public long getDiffSatTimeout() {
		return diffSatTimeout;
	}

	public void setDiffSatTimeout(long diffSatTimeout) {
		if (diffSatTimeout < 0) {
			diffSatTimeout = 0;
		}
		this.diffSatTimeout = diffSatTimeout;
		firePropertyChanged();
	}

	public long getLoopSatTimeout() {
		return loopSatTimeout;
	}

	public void setLoopSatTimeout(long loopSatTimeout) {
		if (loopSatTimeout < 0) {
			loopSatTimeout = 0;
		}
		this.loopSatTimeout = loopSatTimeout;
		firePropertyChanged();
	}

	public boolean isIgnoreAnnotations() {
		return ignoreAnnotations;
	}

	public void setIgnoreAnnotations(boolean ignoreAnnotations) {
		if (this.ignoreAnnotations != ignoreAnnotations) {
			this.ignoreAnnotations = ignoreAnnotations;
			firePropertyChanged();
		}
	}

	/**
	 * @return
	 */
	public int getSimplifyTimeout() {
		return simplifyTimeout;
	}

	/**
	 * @param simplifyTimeout
	 *            the simplifyTimeout to set
	 */
	public void setSimplifyTimeout(int simplifyTimeout) {
		if (this.simplifyTimeout != simplifyTimeout) {
			this.simplifyTimeout = simplifyTimeout;
			firePropertyChanged();
		}
	}

	/**
	 * @return the useIterativeReduceRule
	 */
	public boolean isUseIterativeReduceRule() {
		return useIterativeReduceRule;
	}

	/**
	 * @param useIterativeReduceRule
	 *            the useIterativeReduceRule to set
	 */
	public void setUseIterativeReduceRule(boolean useIterativeReduceRule) {
		if (this.useIterativeReduceRule != useIterativeReduceRule) {
			this.useIterativeReduceRule = useIterativeReduceRule;
			firePropertyChanged();
		}
	}

	public BuiltInArithmetic getBuiltInArithmetic() {
		return builtInArithmetic;
	}

	public void setBuiltInArithmetic(BuiltInArithmetic builtInArithmetic) {
		this.builtInArithmetic = builtInArithmetic;
		firePropertyChanged();
	}

	public BuiltInArithmeticIneqs getBuiltInArithmeticIneqs() {
		return builtInArithmeticIneqs;
	}

	public void setBuiltInArithmeticIneqs(
			BuiltInArithmeticIneqs builtInArithmetic) {
		this.builtInArithmeticIneqs = builtInArithmetic;
		firePropertyChanged();
	}

	/**
	 * @return the termFactory
	 */
	public Class<? extends TermFactory> getTermFactoryClass() {
		return termFactoryClass;
	}

	/**
	 * @param termFactory
	 *            the termFactory to set
	 */
	public void setTermFactoryClass(Class<? extends TermFactory> termFactory) {
		if (termFactory != this.termFactoryClass) {
			this.termFactoryClass = termFactory;
			firePropertyChanged();
		}
	}

	/**
	 * @return the applyLocalReduce
	 */
	public LocalReduceOption getApplyLocalReduce() {
		return applyLocalReduce;
	}

	/**
	 * @param applyLocalReduce
	 *            the applyLocalReduce to set
	 */
	public void setApplyLocalReduce(LocalReduceOption applyLocalReduce) {
		if (applyLocalReduce != this.applyLocalReduce) {
			this.applyLocalReduce = applyLocalReduce;
			firePropertyChanged();
		}
	}

	/**
	 * @return the applyLocalSimplify
	 */
	public boolean isApplyLocalSimplify() {
		return applyLocalSimplify;
	}

	/**
	 * @param applyLocalSimplify
	 *            the applyLocalSimplify to set
	 */
	public void setApplyLocalSimplify(boolean applyLocalSimplify) {
		if (applyLocalSimplify != this.applyLocalSimplify) {
			this.applyLocalSimplify = applyLocalSimplify;
			firePropertyChanged();
		}
	}

	/**
	 * @return the applyGlobalReduce
	 */
	public boolean isApplyGlobalReduce() {
		return applyGlobalReduce;
	}

	/**
	 * @param applyGlobalReduce
	 *            the applyGlobalReduce to set
	 */
	public void setApplyGlobalReduce(boolean applyGlobalReduce) {
		if (applyGlobalReduce != this.applyGlobalReduce) {
			this.applyGlobalReduce = applyGlobalReduce;
			firePropertyChanged();
		}
	}

	/**
	 * @return the simplifyAfterODESolve
	 */
	public boolean isSimplifyAfterODESolve() {
		return simplifyAfterODESolve;
	}

	/**
	 * @param simplifyAfterODESolve
	 *            the simplifyAfterODESolve to set
	 */
	public void setSimplifyAfterODESolve(boolean simplifyAfterODESolve) {
		if (simplifyAfterODESolve != this.simplifyAfterODESolve) {
			this.simplifyAfterODESolve = simplifyAfterODESolve;
			firePropertyChanged();
		}
	}

	/**
	 * @return TODO documentation since Jun 9, 2008
	 */
	public String getGroebnerBasisCalculator() {
		return groebnerBasisCalculator;
	}

	/**
	 * @return TODO documentation since Jun 9, 2008
	 */
	public void setGroebnerBasisCalculator(String groebnerBasisCalculator) {
		if (!this.groebnerBasisCalculator.equals(groebnerBasisCalculator)) {
			this.groebnerBasisCalculator = groebnerBasisCalculator;
			firePropertyChanged();
		}

	}

	/**
	 * @return the usePowersetIterativeReduce
	 */
	public boolean isUsePowersetIterativeReduce() {
		return usePowersetIterativeReduce;
	}

	/**
	 * @param usePowersetIterativeReduce
	 *            the usePowersetIterativeReduce to set
	 */
	public void setUsePowersetIterativeReduce(boolean usePowersetIterativeReduce) {
		if (this.usePowersetIterativeReduce != usePowersetIterativeReduce) {
			this.usePowersetIterativeReduce = usePowersetIterativeReduce;
			firePropertyChanged();
		}
	}

	/**
	 * @return the percentOfPowersetForReduce
	 */
	public int getPercentOfPowersetForReduce() {
		return percentOfPowersetForReduce;
	}

	/**
	 * @param percentOfPowersetForReduce
	 *            the percentOfPowersetForReduce to set
	 */
	public void setPercentOfPowersetForReduce(int percentOfPowersetForReduce) {
		if (this.percentOfPowersetForReduce != percentOfPowersetForReduce) {
			this.percentOfPowersetForReduce = percentOfPowersetForReduce;
			firePropertyChanged();
		}
	}

	/**
	 * @return the useSOS
	 */
	public boolean isUseSOS() {
		return useSOS;
	}

	/**
	 * @param useSOS
	 *            the useSOS to set
	 */
	public void setUseSOS(boolean useSOS) {
		if (this.useSOS != useSOS) {
			this.useSOS = useSOS;
			firePropertyChanged();
		}
	}

	/**
	 * @return the sosChecker
	 */
	public String getSosChecker() {
		return sosChecker;
	}

	/**
	 * @param sosChecker the sosChecker to set
	 */
	public void setSosChecker(String sosChecker) {
		if(!sosChecker.equals(this.sosChecker)) {
			this.sosChecker = sosChecker;
			firePropertyChanged();
		}
	}
}
