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

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.gui.GUIEvent;
import de.uka.ilkd.key.gui.configuration.Settings;
import de.uka.ilkd.key.gui.configuration.SettingsListener;
import de.uka.ilkd.key.proof.ProofSaver;

/**
 * @author jdq
 * @see DLOptionBean#INSTANCE
 */
public class DLOptionBean implements Settings {

    public static enum CexFinder {
		DFS("Depth first search"), BFS("Breadth first search"), ITER_DEEP(
				"Iterative deepening"), ITER_DEEP_ASTAR(
					"Iterative deepening A*"),  ITER_EXP("Iterative expansion"), ASTAR(
				"A*"), HILL_CLIMB("Hill climbing");

		private String string;

		private CexFinder(String str) {
			this.string = str;
		}

		@Override
		public String toString() {
			return string;
		}
	}

	public static enum TracerStat {
		ON("on"), OFF("off");

		private String string;

		private TracerStat(String str) {
			this.string = str;
		}

		@Override
		public String toString() {
			return string;
		}
	}

	
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
				"auto"), DESPERATE ("desperate");

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
		QUANTIFIERS("loop_inv_box_quan"), FRESH("loop_inv_box_fresh");

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
		OFF("off"), APPLY_EQUALITIES("apply equalities"), NORMALISE_EQUATIONS("normalise"), REDUCTION(
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
	 * Singleton storage for options
	 */
	public static final DLOptionBean INSTANCE = new DLOptionBean();

	private Set<Settings> subOptions;

	private FirstOrderStrategy foStrategy;

	private CexFinder cexFinder;

	private TracerStat tracerStat;


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
	private File csdpBinary;
	private boolean csdpForceInternal;

	private boolean resetStrategyAfterEveryRun;

    private boolean useODEIndFinMethods;
    
    private boolean reduceOnFreshBranch;
    
    private boolean solveODE;
    
    private boolean init;
    
    private boolean ibcOnlyToFO;
    
    private boolean qeOnlyToTrue;

    private boolean addRigidFormulas;

    private boolean pretendWhileLoadingQE;

    private boolean universalClosureOnQE;

	private DLOptionBean() {
		subOptions = new LinkedHashSet<Settings>();
		solveODE = true;
		counterExampleGenerator = "";
		odeSolver = "";
		quantifierEliminator = "";
		simplifier = "";
		groebnerBasisCalculator = "";
		sosChecker = "";
		csdpBinary = new File("/usr/bin/csdp");
		listeners = new HashSet<SettingsListener>();
		
		// init other values to their defaults
		init = true;
		reset();
		init = false;
	}
	
	public void reset() {
		foStrategy = FirstOrderStrategy.LAZY;
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
		solveODE = true;
		applyGammaRules = ApplyRules.ONLY_TO_MODALITIES;
		counterexampleTest = CounterexampleTest.ON;
		invariantRule = InvariantRule.FRESH;
		diffSatStrategy = DiffSat.AUTO;
		ignoreAnnotations = false;
		useIterativeReduceRule = false;
		termFactoryClass = de.uka.ilkd.key.dl.model.impl.TermFactoryImpl.class;
		applyLocalReduce = LocalReduceOption.OFF;
		applyLocalSimplify = false;
		applyGlobalReduce = true;
		usePowersetIterativeReduce = true;
		percentOfPowersetForReduce = 70;
		builtInArithmetic = BuiltInArithmetic.APPLY_EQUALITIES;
		builtInArithmeticIneqs = BuiltInArithmeticIneqs.OFF;
		useSOS = false;
		csdpForceInternal = false;
		useODEIndFinMethods = false;
		ibcOnlyToFO = true;

		cexFinder = CexFinder.ITER_DEEP;
		tracerStat = TracerStat.OFF;
		qeOnlyToTrue = false;
        addRigidFormulas = true;
        pretendWhileLoadingQE = false;
        universalClosureOnQE = true;
		if(!init) {
		    firePropertyChanged();
		}
	}
	
	public boolean isQeOnlyToTrue() {
		return qeOnlyToTrue;
	}

	public void setQeOnlyToTrue(
            boolean qeOnlyToTrue) {
		if(this.qeOnlyToTrue != qeOnlyToTrue) {
			this.qeOnlyToTrue = qeOnlyToTrue;
			firePropertyChanged();
		}
	}

	public CexFinder getCexFinder() {
		return cexFinder;
	}

	public void setCexFinder(CexFinder cexFinder) {
		if (this.cexFinder != cexFinder) {
			this.cexFinder = cexFinder;
			firePropertyChanged();
		}
	}

	public TracerStat getTracerStat() {
		return tracerStat;
	}

	public void setTracerStat(TracerStat tracerStat) {
		if (this.tracerStat != tracerStat) {
			this.tracerStat = tracerStat;
			firePropertyChanged();
		}
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
		readSettings(props, true);
	}

	public void readSettings(Properties props, boolean validate) {
		for (Settings sub : subOptions) {
			sub.readSettings(props);
		}
		String property = props.getProperty(EPropertyConstant.DLOPTIONS_FO_STRATEGY.getKey());
		if (property != null) {
			foStrategy = FirstOrderStrategy.valueOf(property);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_INITIAL_TIMEOUT.getKey());
		if (property != null) {
			initialTimeout = Math
					.round(((float) Integer.parseInt(property)) / 1000f);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_QUADRIC.getKey());
		if (property != null) {
			quadraticTimeoutIncreaseFactor = Integer.parseInt(property);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_LINEAR.getKey());
		if (property != null) {
			linearTimeoutIncreaseFactor = Integer.parseInt(property);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_CONSTANT.getKey());
		if (property != null) {
			constantTimeoutIncreaseFactor = Integer.parseInt(property);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_READD_QUANTIFIERS.getKey());
		if (property != null) {
			readdQuantifiers = property.equals(TRUE);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_SIMPLIFY_BEFORE_REDUCE.getKey());
		if (property != null) {
			simplifyBeforeReduce = property.equals(TRUE);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_SIMPLIFY_AFTER_REDUCE.getKey());
		if (property != null) {
			simplifyAfterReduce = property.equals(TRUE);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_APPLY_UPDATES_TO_MODALITIES.getKey());
		if (property != null) {
			applyUpdatesToModalities = property.equals(TRUE);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_COUNTEREXAMPLE_TEST.getKey());
		if (property != null) {
			counterexampleTest = CounterexampleTest.valueOf(property);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_IGNORE_ANNOTATIONS.getKey());
		if (property != null) {
			ignoreAnnotations = property.equals(TRUE);
		}

		counterExampleGenerator = props
				.getProperty(EPropertyConstant.DLOPTIONS_COUNTEREXAMPLE_GENERATOR.getKey());
		if (validate && (counterExampleGenerator == null || (!(MathSolverManager.getCounterExampleGenerators()
				.contains(counterExampleGenerator))
				&& !counterExampleGenerator.equals("-")))) {
			if (!MathSolverManager.getCounterExampleGenerators().isEmpty()) {
				counterExampleGenerator = MathSolverManager
						.getCounterExampleGenerators().iterator().next();
			} else {
				counterExampleGenerator = "-";
			}
		}
		odeSolver = props.getProperty(EPropertyConstant.DLOPTIONS_ODESOLVER.getKey());
		if (validate && (odeSolver == null || (!(MathSolverManager.getODESolvers().contains(odeSolver))
				&& !odeSolver.equals("-")))) {
			if (!MathSolverManager.getODESolvers().isEmpty()) {
				odeSolver = MathSolverManager.getODESolvers().iterator()
						.next();
			} else {
				odeSolver = "-";
			}
		}
		quantifierEliminator = props
				.getProperty(EPropertyConstant.DLOPTIONS_QUANTIFIER_ELIMINATOR.getKey());
		if (validate && (quantifierEliminator == null || (!(MathSolverManager.getQuantifierEliminators()
				.contains(quantifierEliminator))
				&& !quantifierEliminator.equals("-")))) {
			if (!MathSolverManager.getQuantifierEliminators().isEmpty()) {
				quantifierEliminator = MathSolverManager
						.getQuantifierEliminators().iterator().next();
			} else {
				quantifierEliminator ="-";
			}
		}
		simplifier = props.getProperty(EPropertyConstant.DLOPTIONS_SIMPLIFIER.getKey());
		if (validate && (simplifier == null || (!(MathSolverManager.getSimplifiers().contains(simplifier))
				&& !simplifier.equals("-")))) {
			if (!MathSolverManager.getSimplifiers().isEmpty()) {
				simplifier = MathSolverManager.getSimplifiers().iterator()
						.next();
			} else {
				simplifier = "-";
			}
		}
		groebnerBasisCalculator = props
				.getProperty(EPropertyConstant.DLOPTIONS_GROEBNER_BASIS_CALCULATOR.getKey());
		if (validate && (groebnerBasisCalculator  == null || (!(MathSolverManager.getGroebnerBasisCalculators().contains(groebnerBasisCalculator))
				&& !groebnerBasisCalculator.equals("-")))) {
			if (!MathSolverManager.getGroebnerBasisCalculators().isEmpty()) {
				groebnerBasisCalculator = MathSolverManager.getGroebnerBasisCalculators().iterator()
						.next();
			} else {
				groebnerBasisCalculator = "-";
			}
		}
		sosChecker = props
			.getProperty(EPropertyConstant.DLOPTIONS_SOS_CHECKER.getKey());
		if (validate && (sosChecker == null || (!(MathSolverManager.getSOSCheckers().contains(sosChecker))
				&& !sosChecker.equals("-")))) {
			if (!MathSolverManager.getSOSCheckers().isEmpty()) {
				sosChecker = MathSolverManager.getSOSCheckers().iterator()
						.next();
			} else {
				sosChecker = "-";
			}
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
		property = props.getProperty(EPropertyConstant.DLOPTIONS_APPLY_GAMMA_RULES.getKey());
		if (property != null) {
			applyGammaRules = ApplyRules.valueOf(property);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_INVARIANT_RULE.getKey());
		if (property != null) {
			invariantRule = InvariantRule.valueOf(property);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_USE_DIFF_SAT.getKey());
		if (property != null) {
			diffSatStrategy = DiffSat.valueOf(property);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_DIFFSAT_TIMEOUT.getKey());
		if (property != null) {
			diffSatTimeout = Math
					.round(((float) Integer.parseInt(property)) / 1000f);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_LOOPSAT_TIMEOUT.getKey());
		if (property != null) {
			loopSatTimeout = Math
					.round(((float) Integer.parseInt(property)) / 1000f);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_SIMPLIFY_TIMEOUT.getKey());
		if (property != null) {
			simplifyTimeout = Integer.parseInt(property);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_ITERATIVE_REDUCE_RULE.getKey());
		if (property != null) {
			useIterativeReduceRule = Boolean.valueOf(property);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_TERM_FACTORY_CLASS.getKey());
		if (property != null) {
			try {
				termFactoryClass = (Class<? extends TermFactory>) getClass()
						.getClassLoader().loadClass(property);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_APPLY_LOCAL_REDUCE.getKey());
		if (property != null) {
			if (property.equalsIgnoreCase("false")) {
				applyLocalReduce = LocalReduceOption.OFF;
			} else if (property.equalsIgnoreCase("true")) {
				applyLocalReduce = LocalReduceOption.ALWAYS;
			} else {
				applyLocalReduce = LocalReduceOption.valueOf(property);
			}
		}

		property = props.getProperty(EPropertyConstant.DLOPTIONS_APPLY_LOCAL_SIMPLIFY.getKey());
		if (property != null) {
			applyLocalSimplify = Boolean.valueOf(property);
		}

		property = props.getProperty(EPropertyConstant.DLOPTIONS_APPLY_GLOBAL_REDUCE.getKey());
		if (property != null) {
			applyGlobalReduce = Boolean.valueOf(property);
		}

		property = props.getProperty(EPropertyConstant.DLOPTIONS_SIMPLIFY_AFTER_ODESOLVE.getKey());
		if (property != null) {
			simplifyAfterODESolve = Boolean.valueOf(property);
		}

		property = props.getProperty(EPropertyConstant.DLOPTIONS_USE_POWERSET_ITERATIVE_REDUCE.getKey());
		if (property != null) {
			usePowersetIterativeReduce = Boolean.valueOf(property);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_USE_SOS.getKey());
		if (property != null) {
			useSOS = Boolean.valueOf(property);
		}

		property = props
				.getProperty(EPropertyConstant.DLOPTIONS_PERCENT_OF_POWERSET_FOR_ITERATIVE_REDUCE.getKey());
		if (property != null) {
			percentOfPowersetForReduce = Integer.valueOf(property);
		}

		property = props.getProperty(EPropertyConstant.DLOPTIONS_BUILT_IN_ARITHMETIC.getKey());
		if (property != null) {
			builtInArithmetic = BuiltInArithmetic.valueOf(property);
		}

		property = props.getProperty(EPropertyConstant.DLOPTIONS_BUILT_IN_ARITHMETIC_INEQS.getKey());
		if (property != null) {
			builtInArithmeticIneqs = BuiltInArithmeticIneqs.valueOf(property);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_CSDP_PATH.getKey());
		if (property != null) {
			csdpBinary = new File(property);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_CSDP_FORCE_INTERNAL.getKey());
		if (property != null) {
			csdpForceInternal = Boolean.valueOf(property);
		}

		property = props.getProperty(EPropertyConstant.DLOPTIONS_CEX_FINDER
				.getKey());
		if (property != null) {
			cexFinder = CexFinder.valueOf(property);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_TRACER_STAT
				.getKey());
		if (property != null) {
			tracerStat = TracerStat.valueOf(property);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_USE_ODE_IND_FIN_METHODS.getKey());
		if(property != null) {
		    useODEIndFinMethods = Boolean.valueOf(property);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_REDUCE_ON_FRESH_BRANCH.getKey());
		if(property != null) {
		    reduceOnFreshBranch = Boolean.valueOf(property);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_SOLVE_ODE.getKey());
		if(property != null) {
		    solveODE = Boolean.valueOf(property);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_IBC_ONLY_TO_FO.getKey());
		if(property != null) {
		    ibcOnlyToFO = Boolean.valueOf(property);
		}
		property = props.getProperty(EPropertyConstant.DLOPTIONS_QE_ONLY_TO_TRUE.getKey());
		if (property != null) {
			qeOnlyToTrue = Boolean.valueOf(property);
		}
        property = props.getProperty(EPropertyConstant.DLOPTIONS_ADD_RIGID_FORMULAS.getKey());
        if (property != null) {
            addRigidFormulas = Boolean.valueOf(property);
        }
        property = props.getProperty(EPropertyConstant.DLOPTIONS_PRETEND_QE_TRUE_WHILE_LOADING.getKey());
        if (property != null) {
            pretendWhileLoadingQE = Boolean.valueOf(property);
        }

        property = props.getProperty(EPropertyConstant.DLOPTIONS_UNIVERSAL_CLOSURE_ON_QE.getKey());
        if (property != null) {
            universalClosureOnQE = Boolean.valueOf(property);
        }

		try {
			de.uka.ilkd.key.dl.DLInitializer.updateCustomizers();
		} catch(Exception e) {}
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
		props.setProperty(EPropertyConstant.DLOPTIONS_FO_STRATEGY.getKey(), foStrategy.name());
		props
				.setProperty(EPropertyConstant.DLOPTIONS_INITIAL_TIMEOUT.getKey(), "" + initialTimeout
						* 1000);
		props.setProperty(EPropertyConstant.DLOPTIONS_QUADRIC.getKey(), ""
				+ quadraticTimeoutIncreaseFactor);
		props.setProperty(EPropertyConstant.DLOPTIONS_LINEAR.getKey(), "" + linearTimeoutIncreaseFactor);
		props.setProperty(EPropertyConstant.DLOPTIONS_CONSTANT.getKey(), ""
				+ constantTimeoutIncreaseFactor);

		props.setProperty(EPropertyConstant.DLOPTIONS_READD_QUANTIFIERS.getKey(), Boolean.valueOf(
				readdQuantifiers).toString());
		props.setProperty(EPropertyConstant.DLOPTIONS_SIMPLIFY_BEFORE_REDUCE.getKey(), Boolean.valueOf(
				simplifyBeforeReduce).toString());
		props.setProperty(EPropertyConstant.DLOPTIONS_SIMPLIFY_AFTER_REDUCE.getKey(), Boolean.valueOf(
				simplifyAfterReduce).toString());
		props.setProperty(EPropertyConstant.DLOPTIONS_APPLY_UPDATES_TO_MODALITIES.getKey(), Boolean
				.valueOf(applyUpdatesToModalities).toString());
		props.setProperty(EPropertyConstant.DLOPTIONS_COUNTEREXAMPLE_TEST.getKey(), counterexampleTest
				.name());
		props.setProperty(EPropertyConstant.DLOPTIONS_IGNORE_ANNOTATIONS.getKey(), Boolean
				.toString(ignoreAnnotations));

		if (counterExampleGenerator != null) {
			props.setProperty(EPropertyConstant.DLOPTIONS_COUNTEREXAMPLE_GENERATOR.getKey(),
					counterExampleGenerator);
		}
		if (odeSolver != null) {
			props.setProperty(EPropertyConstant.DLOPTIONS_ODESOLVER.getKey(), odeSolver);
		}
		if (quantifierEliminator != null) {
			props.setProperty(EPropertyConstant.DLOPTIONS_QUANTIFIER_ELIMINATOR.getKey(),
					quantifierEliminator);
		} else {
			props.setProperty(EPropertyConstant.DLOPTIONS_QUANTIFIER_ELIMINATOR.getKey(), "-");
		}
		if (simplifier != null) {
			props.setProperty(EPropertyConstant.DLOPTIONS_SIMPLIFIER.getKey(), simplifier);
		}
		if (groebnerBasisCalculator != null) {
			props.setProperty(EPropertyConstant.DLOPTIONS_GROEBNER_BASIS_CALCULATOR.getKey(),
					groebnerBasisCalculator);
		}
		if (sosChecker != null) {
			props.setProperty(EPropertyConstant.DLOPTIONS_SOS_CHECKER.getKey(),
					sosChecker);
		}

		props.setProperty(EPropertyConstant.DLOPTIONS_APPLY_GAMMA_RULES.getKey(), applyGammaRules.name());
		props.setProperty(EPropertyConstant.DLOPTIONS_INVARIANT_RULE.getKey(), invariantRule.name());

		props.setProperty(EPropertyConstant.DLOPTIONS_USE_DIFF_SAT.getKey(), diffSatStrategy.name());
		props
				.setProperty(EPropertyConstant.DLOPTIONS_DIFFSAT_TIMEOUT.getKey(), "" + diffSatTimeout
						* 1000);
		props
				.setProperty(EPropertyConstant.DLOPTIONS_LOOPSAT_TIMEOUT.getKey(), "" + loopSatTimeout
						* 1000);
		props.setProperty(EPropertyConstant.DLOPTIONS_SIMPLIFY_TIMEOUT.getKey(), "" + simplifyTimeout);

		props.setProperty(EPropertyConstant.DLOPTIONS_ITERATIVE_REDUCE_RULE.getKey(), Boolean
				.toString(useIterativeReduceRule));
		props.setProperty(EPropertyConstant.DLOPTIONS_TERM_FACTORY_CLASS.getKey(), termFactoryClass
				.getName());
		props
				.setProperty(EPropertyConstant.DLOPTIONS_APPLY_LOCAL_REDUCE.getKey(), applyLocalReduce
						.name());
		props.setProperty(EPropertyConstant.DLOPTIONS_APPLY_LOCAL_SIMPLIFY.getKey(), Boolean
				.toString(applyLocalSimplify));
		props.setProperty(EPropertyConstant.DLOPTIONS_APPLY_GLOBAL_REDUCE.getKey(), Boolean
				.toString(applyGlobalReduce));
		props.setProperty(EPropertyConstant.DLOPTIONS_SIMPLIFY_AFTER_ODESOLVE.getKey(), Boolean
				.toString(simplifyAfterODESolve));
		props.setProperty(EPropertyConstant.DLOPTIONS_USE_POWERSET_ITERATIVE_REDUCE.getKey(), Boolean
				.toString(usePowersetIterativeReduce));
		props.setProperty(EPropertyConstant.DLOPTIONS_USE_SOS.getKey(), Boolean
				.toString(useSOS));
		props.setProperty(EPropertyConstant.DLOPTIONS_PERCENT_OF_POWERSET_FOR_ITERATIVE_REDUCE.getKey(),
				"" + percentOfPowersetForReduce);
		props.setProperty(EPropertyConstant.DLOPTIONS_BUILT_IN_ARITHMETIC.getKey(), builtInArithmetic
				.name());
		props.setProperty(EPropertyConstant.DLOPTIONS_BUILT_IN_ARITHMETIC_INEQS.getKey(),
				builtInArithmeticIneqs.name());
		props.setProperty(EPropertyConstant.DLOPTIONS_CSDP_FORCE_INTERNAL.getKey(), Boolean.toString(csdpForceInternal));	

		if(!ProofSaver.isInSavingMode()) {
			// we don't want to save user specific pathes when saving proofs
			props.setProperty(EPropertyConstant.DLOPTIONS_CSDP_PATH.getKey(), csdpBinary.getAbsolutePath());
            // we don't want to save the pretend property
            props.setProperty(EPropertyConstant.DLOPTIONS_PRETEND_QE_TRUE_WHILE_LOADING.getKey(), Boolean.toString(pretendWhileLoadingQE));
		}
		props.setProperty(EPropertyConstant.DLOPTIONS_CEX_FINDER.getKey(),
				cexFinder.name());
		props.setProperty(EPropertyConstant.DLOPTIONS_TRACER_STAT.getKey(),
				tracerStat.name());
		props.setProperty(EPropertyConstant.DLOPTIONS_USE_ODE_IND_FIN_METHODS.getKey(), Boolean.toString(useODEIndFinMethods));
		props.setProperty(EPropertyConstant.DLOPTIONS_REDUCE_ON_FRESH_BRANCH.getKey(), Boolean.toString(reduceOnFreshBranch));
		props.setProperty(EPropertyConstant.DLOPTIONS_SOLVE_ODE.getKey(), Boolean.toString(solveODE));
		props.setProperty(EPropertyConstant.DLOPTIONS_IBC_ONLY_TO_FO.getKey(), Boolean.toString(ibcOnlyToFO));
		props.setProperty(EPropertyConstant.DLOPTIONS_QE_ONLY_TO_TRUE.getKey(), Boolean.toString(qeOnlyToTrue));
        props.setProperty(EPropertyConstant.DLOPTIONS_ADD_RIGID_FORMULAS.getKey(), Boolean.toString(addRigidFormulas));
        props.setProperty(EPropertyConstant.DLOPTIONS_UNIVERSAL_CLOSURE_ON_QE.getKey(), Boolean.toString(universalClosureOnQE));

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
	
	public boolean isApplyEquations() {
		return builtInArithmetic == BuiltInArithmetic.APPLY_EQUALITIES;
	}

	/**
	 * @return the applyUpdatesToModalities
	 */
	public boolean isApplyUpdatesToModalities() {
		// FIXME the method is unsound and therefore disabled
		return false;
		//return applyUpdatesToModalities;
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
                || quantifierEliminator == null
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

	/**
	 * @return the csdpBinary
	 */
	public File getCsdpBinary() {
		return csdpBinary;
	}

	/**
	 * @param csdpBinary the csdpBinary to set
	 */
	public void setCsdpBinary(File csdpBinary) {
		if(!csdpBinary.equals(this.csdpBinary)) {
			this.csdpBinary = csdpBinary;
			firePropertyChanged();
		}
	}

	/**
	 * @return the csdpForceInternal
	 */
	public boolean isCsdpForceInternal() {
		return csdpForceInternal;
	}

	/**
	 * @param csdpForceInternal the csdpForceInternal to set
	 */
	public void setCsdpForceInternal(boolean csdpForceInternal) {
		if(this.csdpForceInternal != csdpForceInternal) {
			this.csdpForceInternal = csdpForceInternal;
			firePropertyChanged();
		}
	}

	public boolean isResetStrategyAfterEveryRun() {
		return resetStrategyAfterEveryRun;
	}

	public void setResetStrategyAfterEveryRun(boolean resetStrategyAfterEveryRun) {
	    if(this.resetStrategyAfterEveryRun != resetStrategyAfterEveryRun) {
	        this.resetStrategyAfterEveryRun = resetStrategyAfterEveryRun;
	        firePropertyChanged();
	    }
	}

    /**
     * @return
     */
    public boolean isUseODEIndFinMethods() {
        return useODEIndFinMethods;
    }

    /**
     * @param useODEIndFinMethods the useODEIndFinMethods to set
     */
    public void setUseODEIndFinMethods(boolean useODEIndFinMethods) {
        if(this.useODEIndFinMethods != useODEIndFinMethods) {
            this.useODEIndFinMethods = useODEIndFinMethods;
            firePropertyChanged();
        }
    }

    /**
     * @return the reduceOnFreshBranch
     */
    public boolean isReduceOnFreshBranch() {
        return reduceOnFreshBranch;
    }

    /**
     * @param reduceOnFreshBranch the reduceOnFreshBranch to set
     */
    public void setReduceOnFreshBranch(boolean reduceOnFreshBranch) {
        if(this.reduceOnFreshBranch != reduceOnFreshBranch) {
            System.out.println("Changed!");
            this.reduceOnFreshBranch = reduceOnFreshBranch;
            firePropertyChanged();
        }
    }
    
    /**
     * @return the solveODE
     */
    public boolean isSolveODE() {
        return solveODE;
    }
    
    /**
     * @param solveODE the solveODE to set
     */
    public void setSolveODE(boolean solveODE) {
        if(this.solveODE != solveODE) {
            this.solveODE = solveODE;
            firePropertyChanged();
        }
    }

    public boolean isIbcOnlyToFO() {
        return ibcOnlyToFO;
    }

    public void setIbcOnlyToFO(boolean ibcOnlyToFO) {
        this.ibcOnlyToFO = ibcOnlyToFO;
    }

    public boolean isAddRigidFormulas() {
        return addRigidFormulas;
    }

    public void setAddRigidFormulas(boolean addRigidFormulas) {
        if(this.addRigidFormulas != addRigidFormulas) {
            this.addRigidFormulas = addRigidFormulas;
            firePropertyChanged();
        }
    }

    public boolean isPretendWhileLoadingQE() {
        return pretendWhileLoadingQE;
    }

    public void setPretendWhileLoadingQE(boolean pretendWhileLoadingQE) {
        if(this.pretendWhileLoadingQE != pretendWhileLoadingQE) {
            this.pretendWhileLoadingQE = pretendWhileLoadingQE;
            firePropertyChanged();
        }
    }

    public boolean isUniversalClosureOnQE() {
        return universalClosureOnQE;
    }

    public void setUniversalClosureOnQE(boolean universalClosureOnQE) {
        if(this.universalClosureOnQE != universalClosureOnQE) {
            this.universalClosureOnQE = universalClosureOnQE;
            firePropertyChanged();
        }
    }
}
