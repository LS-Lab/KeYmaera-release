// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//

package de.uka.ilkd.key.rule.conditions;

import de.uka.ilkd.key.logic.TermOrdering;
import de.uka.ilkd.key.rule.TacletApp;

/**
 * Condition for heuristic application: Compare the instantiations
 * of two termSV or formulaSV, using a term ordering
 */
public interface OrderingCondition {
    
    /**
     * @return true iff the instantiation of the taclet application satisfies
     *         some ordering condition regarding the given ordering
     */
    boolean checkOrdering ( TacletApp    p_app,
                            TermOrdering p_ordering );

}
