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

import java.util.LinkedHashMap;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.op.NonRigid;
import de.uka.ilkd.key.logic.op.NonRigidFunctionLocation;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.rule.AbstractUpdateRule;
import de.uka.ilkd.key.rule.UpdateSimplifier;
import de.uka.ilkd.key.util.Debug;

/**
 * @author bubel This rule is fall back rule for "unknown" terms with non rigid
 *         top level symbol and just prepends the Update.
 */
public class ApplyOnNonRigidTerm extends AbstractUpdateRule {

    /**
     * @param updateSimplifier
     *            the UpdateSimplifier to which this rule is attached
     */
    public ApplyOnNonRigidTerm(UpdateSimplifier updateSimplifier) {
        super(updateSimplifier);
    }

    /**
     * this rule is applicable if the top level operator is a non rigid symbol
     */
    public boolean isApplicable(Update update, Term target, Services services) {
        return target.op() instanceof NonRigid;
    }

    /**
     * implementation of the fall back rule for terms with an "unknown" non
     * rigid top level symbol
     */
    public Term apply(Update update, final Term target, Services services) {
        Term result;
        if (target.op() instanceof NonRigidFunctionLocation) {
            Term[] args = new Term[target.arity()];
            ImmutableArray<QuantifiableVariable>[] boundVars = new ImmutableArray[target
                    .arity()];
            // pass update homomorph to the arguments
            for (int i = 0; i < target.arity(); i++) {
                args[i] = UpdateSimplifierTermFactory.DEFAULT.createUpdateTerm(
                        update.getAllAssignmentPairs(), updateSimplifier()
                                .simplify(target.sub(i), services));
                boundVars[i] = target.varsBoundHere(i);
            }
            result = TermFactory.DEFAULT.createTerm(target.op(), args,
                    boundVars, target.javaBlock());
            LinkedHashMap<QuantifiableVariable, Term> subst = new LinkedHashMap<QuantifiableVariable, Term>();
            for (int i = 0; i < update.locationCount(); i++) {
                AssignmentPair pair = update.getAssignmentPair(i);
                if (target.op() == pair.locationAsTerm().op()) {
                    // try to unify
                    final boolean[] unifyable = new boolean[1];
                    unifyable[0] = true;
                    Term guard = TermBuilder.DF.tt();
                    for (int j = 0; j < target.arity(); j++) {
                        Operator op = pair.locationAsTerm().sub(j).op();
                        if (op instanceof QuantifiableVariable
                                && pair.boundVars().contains(
                                        (QuantifiableVariable) op)) {
                            // propagate the update to the target and substitute
                            // the quantified argument for this in the value
                            subst.put(
                                    (QuantifiableVariable) op,
                                    UpdateSimplifierTermFactory.DEFAULT.createUpdateTerm(
                                            update.getAllAssignmentPairs(),
                                            updateSimplifier().simplify(
                                                    target.sub(j), services)));
                        } else {
                            // after updating the args have to be the same
                            guard = TermBuilder.DF
                                    .and(guard,
                                            TermBuilder.DF
                                                    .equals(pair
                                                            .locationAsTerm()
                                                            .sub(j),
                                                            UpdateSimplifierTermFactory.DEFAULT
                                                                    .createUpdateTerm(
                                                                            update.getAllAssignmentPairs(),
                                                                            updateSimplifier()
                                                                                    .simplify(
                                                                                            target.sub(j),
                                                                                            services))));
                        }
                    }
                    Term value = pair.value();
                    for (QuantifiableVariable v : subst.keySet()) {
                        value = TermFactory.DEFAULT.createSubstitutionTerm(
                                Op.SUBST, v, subst.get(v), value);
                    }
                    if (guard == TermBuilder.DF.tt()) {
                        result = value;
                    } else {
                        result = TermBuilder.DF.ife(guard, value, result);
                    }
                }
            }
        } else {
            result = UpdateSimplifierTermFactory.DEFAULT.createUpdateTerm(
                    update.getAllAssignmentPairs(), updateSimplifier()
                            .simplify(target, services));
        }
        return result;
    }

    public Term matchingCondition(Update update, Term target, Services services) {
        // we don't really know what to do here ;-)
        Debug.fail("no default implementation of "
                + "matchingCondition(...) available");
        return null; // unreachable
    }
}
