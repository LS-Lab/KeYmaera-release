// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
/*
 * Created on 22.12.2004
 */
package de.uka.ilkd.key.rule.updatesimplifier;

import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.NonRigid;
import de.uka.ilkd.key.logic.op.NonRigidFunctionLocation;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.rule.AbstractUpdateRule;
import de.uka.ilkd.key.rule.UpdateSimplifier;
import de.uka.ilkd.key.util.Debug;

/**
 * @author bubel
 * This rule is fall back rule for "unknown" terms with non rigid
 * top level symbol and just prepends the Update.
 */
public class ApplyOnNonRigidTerm extends AbstractUpdateRule {

    /**
     * @param updateSimplifier the UpdateSimplifier to which this 
     * rule is attached
     */
    public ApplyOnNonRigidTerm(UpdateSimplifier updateSimplifier) {
        super(updateSimplifier);        
    }

    /**
     * this rule is applicable if the top level operator is a non rigid 
     * symbol
     */
    public boolean isApplicable(Update update, Term target, Services services) {       
        return target.op() instanceof NonRigid;
    }

    /** 
     * implementation of the fall back rule for terms with an "unknown"
     * non rigid top level symbol
     */
    public Term apply(Update update, final Term target, Services services) {       
        if (target.op() instanceof NonRigidFunctionLocation) {
           out: for (int i = update.locationCount() - 1; i >= 0; i--) {
                AssignmentPair pair = update.getAssignmentPair(i);
                if(target.op() == pair.locationAsTerm().op()) {
                    // try to unify
                    final boolean[] unifyable = new boolean[1];
                    unifyable[0] = true;
                    for(int j = 0; j < target.arity(); j++) {
                        Operator op = pair.locationAsTerm().sub(j).op();
                        if(op instanceof QuantifiableVariable && pair.boundVars().contains((QuantifiableVariable) op)) {
                            // check whether the current non-rigid function is contained in the term
                            target.sub(j).execPreOrder(new Visitor() {
                                
                                @Override
                                public void visit(Term visited) {
                                    if(visited.op() == target.op()) {
                                        unifyable[0] = false;
                                    }
                                }
                            });
                        } else {
                            if(!pair.locationAsTerm().sub(j).equals(target.sub(j))) {
                                // FIXME: implement more complex unifications
                                unifyable[0] = false;
                                // we have to stop trying as the innermost update was not applicable
                                break out;
                            }
                        }
                    }
                    if(unifyable[0]) {
                        return pair.value();
                    }
                }
            }
        } 
        return UpdateSimplifierTermFactory.DEFAULT.createUpdateTerm(update
                    .getAllAssignmentPairs(),
                    updateSimplifier().simplify(target, services));
    }

    public Term matchingCondition (Update update, 
	    			   Term target, 
	    			   Services services) {
        // we don't really know what to do here ;-)
        Debug.fail ( "no default implementation of "
                     + "matchingCondition(...) available" );
        return null; // unreachable
    }
}
