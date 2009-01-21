// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.rule;

import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.proof.BuiltInRuleAppIndex;
import de.uka.ilkd.key.proof.Goal;

/**
 * Interface for BuiltInRules that cannot be "matched" on particular formulae,
 * but whose applicability is determined by a whole sequent. The matching of
 * such rules (in {@link BuiltInRuleAppIndex}) is different from the matching
 * of other {@link BuiltInRule}s.
 */
public interface SequentWideBuiltInRule extends BuiltInRule {

    /**
     * returns true iff a rule is applicable to the given
     * sequent. This does not necessarily mean that a rule application
     * will change the goal (this decision is made due to performance
     * reasons)
     */
    boolean isApplicable(Goal            goal,
                         Constraint      userConstraint);
}
