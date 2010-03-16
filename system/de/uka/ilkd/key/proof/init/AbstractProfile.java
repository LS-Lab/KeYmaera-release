// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License.
// See LICENSE.TXT for details.
package de.uka.ilkd.key.proof.init;

import java.util.ArrayList;
import java.util.Iterator;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.collection.ImmutableSLList;
import de.uka.ilkd.key.collection.DefaultImmutableSet;
import de.uka.ilkd.key.collection.ImmutableSet;
import de.uka.ilkd.key.gui.IMain;
import de.uka.ilkd.key.gui.KeYMediator;
import de.uka.ilkd.key.gui.configuration.ProofSettings;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.proof.DefaultGoalChooserBuilder;
import de.uka.ilkd.key.proof.DepthFirstGoalChooserBuilder;
import de.uka.ilkd.key.proof.GoalChooserBuilder;
import de.uka.ilkd.key.proof.RuleSource;
import de.uka.ilkd.key.proof.mgt.AxiomJustification;
import de.uka.ilkd.key.proof.mgt.RuleJustification;
import de.uka.ilkd.key.rule.BuiltInRule;
import de.uka.ilkd.key.rule.Rule;
import de.uka.ilkd.key.rule.Taclet;
import de.uka.ilkd.key.smt.*;
import de.uka.ilkd.key.rule.UpdateSimplifier;
import de.uka.ilkd.key.strategy.StrategyFactory;

public abstract class AbstractProfile implements Profile {

    private final RuleCollection       standardRules;

    private final ImmutableSet<StrategyFactory> strategies;

    private final ImmutableSet<String> supportedGC;
    private final ImmutableSet<GoalChooserBuilder> supportedGCB;

    private GoalChooserBuilder prototype;
    
    protected AbstractProfile(String standardRuleFilename,
            ImmutableSet<GoalChooserBuilder> supportedGCB, IMain main) {

        standardRules = new RuleCollection(RuleSource
                .initRuleFile(standardRuleFilename),
                initBuiltInRules());
        strategies = getStrategyFactories();
        this.supportedGCB = supportedGCB;
        this.supportedGC = extractNames(supportedGCB);
        this.prototype = getDefaultGoalChooserBuilder();
        
        assert( this.prototype!=null );

    }

    private static
        ImmutableSet<String> extractNames(ImmutableSet<GoalChooserBuilder> supportedGCB) {

        ImmutableSet<String> result = DefaultImmutableSet.<String>nil();

        for (GoalChooserBuilder aSupportedGCB : supportedGCB) {
            result = result.add(aSupportedGCB.name());
        }

        return result;
    }

    public AbstractProfile(String standardRuleFilename) {
        this(standardRuleFilename, null);
    }

    public AbstractProfile(String standardRuleFilename, IMain main) {
        this(standardRuleFilename,
                DefaultImmutableSet.<GoalChooserBuilder>nil().
                add(new DefaultGoalChooserBuilder()).
                add(new DepthFirstGoalChooserBuilder()), main);
    }

    public RuleCollection getStandardRules() {
        return standardRules;
    }

    protected ImmutableSet<StrategyFactory> getStrategyFactories() {
        return DefaultImmutableSet.<StrategyFactory>nil();
    }

    protected ImmutableList<BuiltInRule> initBuiltInRules() {
        ImmutableList<BuiltInRule> builtInRules = ImmutableSLList.<BuiltInRule>nil();
	ArrayList<SMTSolver> solverList = new ArrayList<SMTSolver>();
        

	solverList.add(new Z3Solver());
	solverList.add(new YicesSolver());
        solverList.add(new SimplifySolver());
	solverList.add(new CVC3Solver());
        
	// init builtIRule for using several provers at the same time
	builtInRules = builtInRules.prepend(new SMTRuleMulti(solverList));
        
	// builtInRules for single use of provers
	for(SMTSolver s : solverList){
          builtInRules = builtInRules.prepend(new SMTRule(s));
       
	}        

      
        
        
        return builtInRules;
    }


    public ImmutableSet<StrategyFactory> supportedStrategies() {
        return strategies;
    }

    public boolean supportsStrategyFactory(Name strategy) {
        return getStrategyFactory(strategy) != null;
    }

    public StrategyFactory getStrategyFactory(Name n) {
        for (StrategyFactory strategyFactory : getStrategyFactories()) {
            final StrategyFactory sf = strategyFactory;
            if (sf.name().equals(n)) {
                return sf;
            }
        }
        return null;
    }

    /**
     * returns the names of the supported goal chooser
     * builders
     */
     public ImmutableSet<String> supportedGoalChoosers() {
         return supportedGC;
     }

     /**
      * returns the default builder for a goal chooser
      * @return this implementation returns a new instance of
      * {@link DefaultGoalChooserBuilder}
      */
     public GoalChooserBuilder getDefaultGoalChooserBuilder() {
         return new DefaultGoalChooserBuilder();
     }

     /**
      * sets the user selected goal chooser builder to be used as prototype
      * @throws IllegalArgumentException if a goal chooser of the given name is not
      *  supported
      */
     public void setSelectedGoalChooserBuilder(String name) {

         this.prototype = lookupGC(name);

         if (this.prototype == null) {
             throw new IllegalArgumentException("Goal chooser:" + name +
                     " is not supported by this profile.");
         }
     }

     /**
      * looks up the demanded goal chooser is supported and returns a
      * new instance if possible otherwise <code>null</code> is returned
      *
      * @param name the String with the goal choosers name
      * @return a new instance of the builder or <code>null</code> if the
      * demanded chooser is not supported
      */
     public GoalChooserBuilder lookupGC(String name) {
         for (GoalChooserBuilder aSupportedGCB : supportedGCB) {
             final GoalChooserBuilder supprotedGCB = aSupportedGCB;
             if (supprotedGCB.name().equals(name)) {
                 return supprotedGCB.copy();
             }
         }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.proof.init.Profile#getDefaultUpdateSimplifier()
     */
    public UpdateSimplifier getDefaultUpdateSimplifier() {
        return ProofSettings.DEFAULT_SETTINGS
                .getSimultaneousUpdateSimplifierSettings().getSimplifier();
    }

    /**
      * returns a copy of the selected goal chooser builder
      */
     public GoalChooserBuilder getSelectedGoalChooserBuilder(){
        return prototype.copy();
     }

     /**
      * any standard rule has is by default justified by an axiom rule
      * justification
      * @return the justification for the standard rules
      */
     public RuleJustification getJustification(Rule r) {
         return AxiomJustification.INSTANCE;
     }

     /**
      * sets the given settings to some default depending on the profile
      */
     public void updateSettings(ProofSettings settings) {
	 settings.getDecisionProcedureSettings().updateSMTRules(this);
     }

     /**
      * returns the file name of the internal class list
      * @return the file name of the internal class list
      */
     public String getInternalClasslistFilename() {
	 return "JAVALANG.TXT";
     }
}
