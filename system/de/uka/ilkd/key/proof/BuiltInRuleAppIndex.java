// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.proof;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.collection.ImmutableSLList;
import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.logic.FormulaChangeInfo;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.PosInTerm;
import de.uka.ilkd.key.logic.Sequent;
import de.uka.ilkd.key.logic.SequentChangeInfo;
import de.uka.ilkd.key.rule.BuiltInRule;
import de.uka.ilkd.key.rule.BuiltInRuleApp;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.rule.SequentWideBuiltInRule;

import java.util.Iterator;

public class BuiltInRuleAppIndex implements java.io.Serializable {

    private BuiltInRuleIndex index;

    private NewRuleListener  newRuleListener =
        NullNewRuleListener.INSTANCE;
    
    public BuiltInRuleAppIndex(BuiltInRuleIndex index) {
	this.index = index;
    }
    
    
    public BuiltInRuleAppIndex(BuiltInRuleIndex index,
			       NewRuleListener  p_newRuleListener) {
	this.index           = index;
	this.newRuleListener = p_newRuleListener;
    }
    
    
    /** 
     * returns a list of built-in rules application applicable
     * for the given goal and position
     */
    public ImmutableList<RuleApp> getBuiltInRule(Goal            goal, 
					PosInOccurrence pos, 
					Constraint      userConstraint) {

	ImmutableList<RuleApp> result = ImmutableSLList.<RuleApp>nil();

        for (BuiltInRule builtInRule : index.rules()) {
            BuiltInRule bir = builtInRule;
            if (bir.isApplicable(goal, pos, userConstraint)) {
                RuleApp app = new BuiltInRuleApp(bir, pos, userConstraint);
                result = result.prepend(app);
            }
        }

	return result;
    }


    /** 
     * returns a copy of this index
     */
    public BuiltInRuleAppIndex copy() {
	return new BuiltInRuleAppIndex(index.copy());
    }

    public void setNewRuleListener ( NewRuleListener p_newRuleListener ) {
    	newRuleListener = p_newRuleListener;
    }

    public BuiltInRuleIndex builtInRuleIndex() {
	return index;
    }

    private NewRuleListener getNewRulePropagator () {
    	return newRuleListener;
    }

    public void scanApplicableRules (Goal       goal,
				     Constraint userConstraint) {
	scanSimplificationRule ( goal, userConstraint, getNewRulePropagator () );
    }

    private void scanSimplificationRule ( Goal       goal,
					  Constraint userConstraint, 
					  NewRuleListener listener ) {
        for (BuiltInRule builtInRule : index.rules()) {
            final BuiltInRule bir = builtInRule;
            scanSimplificationRule(bir, goal, false, userConstraint, listener);
            scanSimplificationRule(bir, goal, true, userConstraint, listener);
        }
    }

  

    private void scanSimplificationRule ( BuiltInRule rule,
					  Goal        goal,
					  boolean     antec,
					  Constraint  userConstraint, 
					  NewRuleListener listener ) {
	final Node                   node = goal.node ();
	final Sequent                seq  = node.sequent ();

        for (Object o : (antec ? seq.antecedent() : seq.succedent())) {
            final ConstrainedFormula cfma = (ConstrainedFormula) o;
            scanSimplificationRule(rule, goal, antec, userConstraint, cfma, listener);
        }
    }


    private void scanSimplificationRule ( BuiltInRule rule, 
                                          Goal goal, 
                                          boolean antec, 
                                          Constraint userConstraint, 
                                          ConstrainedFormula cfma, 
                                          NewRuleListener listener ) {
        final PosInOccurrence    pos = new PosInOccurrence 
		( cfma, PosInTerm.TOP_LEVEL, antec );
        if (rule.isApplicable ( goal, pos, userConstraint ) ) {
            BuiltInRuleApp app = new BuiltInRuleApp(rule, pos, userConstraint );                            
            listener.ruleAdded ( app, pos );
        }
    }

    public void reportRuleApps ( NewRuleListener l,
                                 Goal goal,
                                 Constraint userConstraint ) {
        scanSimplificationRule( goal, userConstraint, l );
    }
    
    /** 
     * called if a formula has been replaced
     * @param sci SequentChangeInfo describing the change of the sequent 
     */  
    public void sequentChanged ( Goal goal, SequentChangeInfo sci ) {
        final Proof proof = goal.proof();
        final Constraint userConstraint = proof.getUserConstraint().getConstraint();
        
        scanSequentWideApps(goal, userConstraint);
        
        scanAddedFormulas ( goal, true, sci, userConstraint );
        scanAddedFormulas ( goal, false, sci, userConstraint );
        
        scanModifiedFormulas ( goal, true, sci, userConstraint );
        scanModifiedFormulas ( goal, false, sci, userConstraint );
    }


    private void scanSequentWideApps(Goal goal, Constraint userConstraint) {
        final Iterator<BuiltInRule> ruleIt = index.rules().iterator();
		final Sequent seq = goal.sequent();
		if (!seq.isEmpty()) {
			while (ruleIt.hasNext()) {
				BuiltInRule rule = ruleIt.next();
				if (rule instanceof SequentWideBuiltInRule) {
					final SequentWideBuiltInRule wideRule = (SequentWideBuiltInRule) rule;
					if (wideRule.isApplicable(goal, userConstraint)) {
						// we need a PosInOccurrence and just select an
						// arbitrary
						// formula in the sequent for that
						final PosInOccurrence pos = new PosInOccurrence(seq
								.iterator().next(), PosInTerm.TOP_LEVEL, !seq
								.antecedent().isEmpty());
						BuiltInRuleApp app = new BuiltInRuleApp(rule, pos,
								userConstraint);
						getNewRulePropagator().ruleAdded(app, pos);
					}
				}
			}
		}
    }
    
    private void scanAddedFormulas ( Goal goal, boolean antec, SequentChangeInfo sci, final Constraint userConstraint ) {
        ImmutableList<ConstrainedFormula> cfmas = sci.addedFormulas( antec );
        final NewRuleListener listener = getNewRulePropagator();
        while ( !cfmas.isEmpty() ) {
            final ConstrainedFormula cfma = cfmas.head();
            for (BuiltInRule builtInRule : index.rules()) {
                final BuiltInRule rule = builtInRule;
                scanSimplificationRule(rule, goal, antec,
                        userConstraint, cfma, listener);
            }
            cfmas = cfmas.tail();
        }
    }


    private void scanModifiedFormulas ( Goal goal, boolean antec, SequentChangeInfo sci, final Constraint userConstraint ) {
        
        final NewRuleListener listener = getNewRulePropagator();
        ImmutableList<FormulaChangeInfo> fcis = sci.modifiedFormulas( antec );

        while ( !fcis.isEmpty() ) {
            final FormulaChangeInfo fci = fcis.head();               
            final ConstrainedFormula cfma = fci.getNewFormula();
            for (BuiltInRule builtInRule : index.rules()) {
                final BuiltInRule rule = builtInRule;
                scanSimplificationRule(rule, goal, antec, userConstraint, cfma, listener);
            }
            fcis = fcis.tail();
        }
    }

}
