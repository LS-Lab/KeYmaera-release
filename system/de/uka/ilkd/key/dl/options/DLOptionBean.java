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
import java.util.Properties;
import java.util.Set;

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

    public static enum ApplyRules {
        ALWAYS("Always"), NEVER("Never"), ONLY_TO_MODALITIES(
                "Only to modalities");

        private String string;

        private ApplyRules(String str) {
            this.string = str;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Enum#toString()
         */
        @Override
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
        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * 
     */
    private static final String DLOPTIONS_SPLIT_BEYOND_FO = "[DLOptions]splitBeyondFO";

    /**
     * 
     */
    private static final String DLOPTIONS_USE_TIMEOUT_STRATEGY = "[DLOptions]useTimeoutStrategy";

    /**
     * 
     */
    private static final String TRUE = new Boolean(true).toString();

    /**
     * 
     */
    private static final String DLOPTIONS_INITIAL_TIMEOUT = "[DLOptions]initialTimeout";

    /**
     * 
     */
    private static final String DLOPTIONS_CALL_REDUCE = "[DLOptions]callReduce";

    public static final DLOptionBean INSTANCE = new DLOptionBean();

    private static final String DLOPTIONS_QUADRIC = "[DLOptions]quadricTimeoutIncreaseFactor";

    private static final String DLOPTIONS_LINEAR = "[DLOptions]linearTimeoutIncreaseFactor";

    private static final String DLOPTIONS_CONSTANT = "[DLOptions]constantTimeoutIncreaseFactor";

    private static final String DLOPTIONS_READD_QUANTIFIERS = "[DLOptions]readdQuantifiers";

    private static final String DLOPTIONS_SIMPLIFY_BEFORE_REDUCE = "[DLOptions]simplifyBeforeReduce";

    private static final String DLOPTIONS_SIMPLIFY_AFTER_REDUCE = "[DLOptions]simplifyAfterReduce";

    private static final String DLOPTIONS_NORMALIZE_EQUATIONS = "[DLOptions]normalizeEquations";

    private static final String DLOPTIONS_APPLY_UPDATES_TO_MODALITIES = "[DLOptions]applyToModality";

    private static final String DLOPTIONS_COUNTEREXAMPLE_GENERATOR = "[DLOptions]counterExampleGenerator";

    private static final String DLOPTIONS_ODESOLVER = "[DLOptions]odeSolver";

    private static final String DLOPTIONS_QUANTIFIER_ELIMINATOR = "[DLOptions]quantifierEliminator";

    private static final String DLOPTIONS_SIMPLFIER = "[DLOptions]simplfier";

    private static final String DLOPTIONS_APPLY_GAMMA_RULES = "[DLOptions]applyGammaRules";

    private static final String DLOPTIONS_USE_FIND_INSTANCE_TEST = "[DLOptions]useFindInstanceTest";

    private static final String DLOPTIONS_STOP_AT_FO = "[DLOptions]stopAtFO";

    private static final String DLOPTIONS_INVARIANT_RULE = "[DLOptions]invariantRule";

    private Set<Settings> subOptions;

    private boolean callReduce;

    private long initialTimeout;

    private int quadraticTimeoutIncreaseFactor;

    private int linearTimeoutIncreaseFactor;

    private int constantTimeoutIncreaseFactor;

    private boolean useTimeoutStrategy;

    private boolean splitBeyondFO;

    private HashSet<SettingsListener> listeners;

    private boolean readdQuantifiers;

    private boolean simplifyBeforeReduce;

    private boolean simplifyAfterReduce;

    private boolean normalizeEquations;

    private boolean applyUpdatesToModalities;

    private boolean useFindInstanceTest;

    private boolean stopAtFO;

    private String counterExampleGenerator;

    private String odeSolver;

    private String quantifierEliminator;

    private String simplifier;

    private ApplyRules applyGammaRules;
    
    private InvariantRule invariantRule;

    private DLOptionBean() {
        subOptions = new HashSet<Settings>();
        callReduce = true;
        initialTimeout = 2000;
        quadraticTimeoutIncreaseFactor = 0;
        linearTimeoutIncreaseFactor = 2;
        constantTimeoutIncreaseFactor = 0;
        splitBeyondFO = false;
        useTimeoutStrategy = true;
        readdQuantifiers = true;
        simplifyBeforeReduce = true;
        simplifyAfterReduce = true;
        normalizeEquations = true;
        applyUpdatesToModalities = true;
        counterExampleGenerator = "";
        odeSolver = "";
        quantifierEliminator = "";
        simplifier = "";
        applyGammaRules = ApplyRules.NEVER;
        useFindInstanceTest = true;
        invariantRule = InvariantRule.QUANTIFIERS;

        listeners = new HashSet<SettingsListener>();
    }

    /**
     * @return the callReduce
     */
    public boolean isCallReduce() {
        return callReduce;
    }

    /**
     * @param callReduce
     *                the callReduce to set
     */
    public void setCallReduce(boolean callReduce) {
        if (this.callReduce != callReduce) {
            this.callReduce = callReduce;
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
     *                the initialTimeout to set
     */
    public void setInitialTimeout(long initialTimeout) {
        if (initialTimeout < 0) {
            initialTimeout = 0;
        }
        this.initialTimeout = initialTimeout;
        firePropertyChanged();
    }

    /**
     * @return the useTimeoutStrategy
     */
    public boolean isUseTimeoutStrategy() {
        return useTimeoutStrategy;
    }

    /**
     * @param useTimeoutStrategy
     *                the useTimeoutStrategy to set
     */
    public void setUseTimeoutStrategy(boolean useTimeoutStrategy) {
        if (this.useTimeoutStrategy != useTimeoutStrategy) {
            this.useTimeoutStrategy = useTimeoutStrategy;
            firePropertyChanged();
        }
    }

    /**
     * @return the splitBeyondFO
     */
    public boolean isSplitBeyondFO() {
        return splitBeyondFO;
    }

    /**
     * @param splitBeyondFO
     *                the splitBeyondFO to set
     */
    public void setSplitBeyondFO(boolean splitBeyondFO) {
        if (this.splitBeyondFO != splitBeyondFO) {
            this.splitBeyondFO = splitBeyondFO;
            firePropertyChanged();
        }
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
     * @see de.uka.ilkd.key.gui.Settings#addSettingsListener(de.uka.ilkd.key.gui.SettingsListener)
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
        String property = props.getProperty(DLOPTIONS_CALL_REDUCE);
        if (property != null) {
            callReduce = property.equals(TRUE);
        }
        property = props.getProperty(DLOPTIONS_INITIAL_TIMEOUT);
        if (property != null) {
            initialTimeout = Integer.parseInt(property);
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
        property = props.getProperty(DLOPTIONS_USE_TIMEOUT_STRATEGY);
        if (property != null) {
            useTimeoutStrategy = property.equals(TRUE);
        }
        property = props.getProperty(DLOPTIONS_SPLIT_BEYOND_FO);
        if (property != null) {
            splitBeyondFO = property.equals(TRUE);
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
        property = props.getProperty(DLOPTIONS_NORMALIZE_EQUATIONS);
        if (property != null) {
            normalizeEquations = property.equals(TRUE);
        }
        property = props.getProperty(DLOPTIONS_APPLY_UPDATES_TO_MODALITIES);
        if (property != null) {
            applyUpdatesToModalities = property.equals(TRUE);
        }
        property = props.getProperty(DLOPTIONS_USE_FIND_INSTANCE_TEST);
        if (property != null) {
            useFindInstanceTest = property.equals(TRUE);
        }
        property = props.getProperty(DLOPTIONS_STOP_AT_FO);
        if (property != null) {
            stopAtFO = property.equals(TRUE);
        }

        counterExampleGenerator = props
                .getProperty(DLOPTIONS_COUNTEREXAMPLE_GENERATOR);
        if (counterExampleGenerator == null) {
            counterExampleGenerator = "";
        }
        odeSolver = props.getProperty(DLOPTIONS_ODESOLVER);
        if (odeSolver == null) {
            odeSolver = "";
        }
        quantifierEliminator = props
                .getProperty(DLOPTIONS_QUANTIFIER_ELIMINATOR);
        if (quantifierEliminator == null) {
            quantifierEliminator = "";
        }
        simplifier = props.getProperty(DLOPTIONS_SIMPLFIER);
        if (simplifier == null) {
            simplifier = "";
        }

        property = props.getProperty(DLOPTIONS_APPLY_GAMMA_RULES);
        if (property != null) {
            applyGammaRules = ApplyRules.valueOf(property);
        }
        property = props.getProperty(DLOPTIONS_INVARIANT_RULE);
        if (property != null) {
            invariantRule = InvariantRule.valueOf(property);
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
        props.setProperty(DLOPTIONS_CALL_REDUCE, new Boolean(callReduce)
                .toString());
        props.setProperty(DLOPTIONS_SPLIT_BEYOND_FO, new Boolean(splitBeyondFO)
                .toString());
        props.setProperty(DLOPTIONS_USE_TIMEOUT_STRATEGY, new Boolean(
                useTimeoutStrategy).toString());
        props.setProperty(DLOPTIONS_INITIAL_TIMEOUT, "" + initialTimeout);
        props.setProperty(DLOPTIONS_QUADRIC, ""
                + quadraticTimeoutIncreaseFactor);
        props.setProperty(DLOPTIONS_LINEAR, "" + linearTimeoutIncreaseFactor);
        props.setProperty(DLOPTIONS_CONSTANT, ""
                + constantTimeoutIncreaseFactor);

        props.setProperty(DLOPTIONS_READD_QUANTIFIERS, new Boolean(
                readdQuantifiers).toString());
        props.setProperty(DLOPTIONS_SIMPLIFY_BEFORE_REDUCE, new Boolean(
                simplifyBeforeReduce).toString());
        props.setProperty(DLOPTIONS_SIMPLIFY_AFTER_REDUCE, new Boolean(
                simplifyAfterReduce).toString());
        props.setProperty(DLOPTIONS_NORMALIZE_EQUATIONS, new Boolean(
                normalizeEquations).toString());
        props.setProperty(DLOPTIONS_APPLY_UPDATES_TO_MODALITIES, new Boolean(
                applyUpdatesToModalities).toString());
        props.setProperty(DLOPTIONS_USE_FIND_INSTANCE_TEST, Boolean
                .toString(useFindInstanceTest));
        props.setProperty(DLOPTIONS_STOP_AT_FO, Boolean.toString(stopAtFO));

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
        }
        if (simplifier != null) {
            props.setProperty(DLOPTIONS_SIMPLFIER, simplifier);
        }

        props.setProperty(DLOPTIONS_APPLY_GAMMA_RULES, applyGammaRules.name());
        props.setProperty(DLOPTIONS_INVARIANT_RULE, invariantRule.name());
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
     *                the constantTimeoutIncreaseFactor to set
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
     *                the linearTimeoutIncreaseFactor to set
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
     *                the quadricTimeoutIncreaseFactor to set
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
     *                the readdQuantifiers to set
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
     *                the simplifyAfterReduce to set
     */
    public void setSimplifyAfterReduce(boolean simplifyAfterReduce) {
        this.simplifyAfterReduce = simplifyAfterReduce;
        firePropertyChanged();
    }

    /**
     * @param simplifyBeforeReduce
     *                the simplifyBeforeReduce to set
     */
    public void setSimplifyBeforeReduce(boolean simplifyBeforeReduce) {
        this.simplifyBeforeReduce = simplifyBeforeReduce;
        firePropertyChanged();
    }

    /**
     * get the value of normalizeEquations
     * 
     * @return the value of normalizeEquations
     */
    public boolean isNormalizeEquations() {
        return normalizeEquations;
    }

    /**
     * set a new value to normalizeEquations
     * 
     * @param normalizeEquations
     *                the new value to be used
     */
    public void setNormalizeEquations(boolean normalizeEquations) {
        this.normalizeEquations = normalizeEquations;
        firePropertyChanged();
    }

    /**
     * @return the applyUpdatesToModalities
     */
    public boolean isApplyUpdatesToModalities() {
        return applyUpdatesToModalities;
    }

    /**
     * @param applyUpdatesToModalities
     *                the applyUpdatesToModalities to set
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
     *                the counterExampleGenerator to set
     */
    public void setCounterExampleGenerator(String counterExampleGenerator) {
        this.counterExampleGenerator = counterExampleGenerator;
        firePropertyChanged();
    }

    /**
     * @return the odeSolver
     */
    public String getOdeSolver() {
        return odeSolver;
    }

    /**
     * @param odeSolver
     *                the odeSolver to set
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
     *                the quantifierEliminator to set
     */
    public void setQuantifierEliminator(String quantifierEliminator) {
        this.quantifierEliminator = quantifierEliminator;
        firePropertyChanged();
    }

    /**
     * @return the simplfier
     */
    public String getSimplifier() {
        return simplifier;
    }

    /**
     * @param simplfier
     *                the simplfier to set
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
     *                the applyGammaRules to set
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
    public boolean isUseFindInstanceTest() {
        return useFindInstanceTest;
    }

    /**
     * @param useFindInstanceTest
     *                the useFindInstanceTest to set
     */
    public void setUseFindInstanceTest(boolean useFindInstanceTest) {
        this.useFindInstanceTest = useFindInstanceTest;
        firePropertyChanged();
    }

    /**
     * @return the stopAtFO
     */
    public boolean isStopAtFO() {
        return stopAtFO;
    }

    /**
     * @param stopAtFO
     *                the stopAtFO to set
     */
    public void setStopAtFO(boolean stopAtFO) {
        this.stopAtFO = stopAtFO;
        firePropertyChanged();
    }

    /**
     * @return the invariantRule
     */
    public InvariantRule getInvariantRule() {
        return invariantRule;
    }

    /**
     * @param invariantRule the invariantRule to set
     */
    public void setInvariantRule(InvariantRule invariantRule) {
        this.invariantRule = invariantRule;
        firePropertyChanged();
    }

}
