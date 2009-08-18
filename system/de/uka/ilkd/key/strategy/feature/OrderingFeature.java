// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.strategy.feature;

import java.util.Iterator;

import de.uka.ilkd.key.logic.LexPathOrdering;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.TermOrdering;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.TacletApp;
import de.uka.ilkd.key.rule.VariableCondition;
import de.uka.ilkd.key.rule.conditions.OrderingCondition;

/**
 * Binary feature that returns zero iff the ordering conditions of a taclet
 * application are fulfilled (this is currently used to control the application
 * of equations). Make sure all schema variables are instantiated when this
 * feature is checked.
 */
public class OrderingFeature extends BinaryTacletAppFeature {

    private final TermOrdering termOrdering = createTermOrdering ();

    static TermOrdering createTermOrdering () {
        return new LexPathOrdering ();
        // new CascadeDepthTermOrdering ( new NameTermOrdering () );
    }
    
    public static final Feature INSTANCE = new OrderingFeature ();

    private OrderingFeature () {}
    
    protected boolean filter ( TacletApp app, PosInOccurrence pos, Goal goal ) {
         final Iterator<VariableCondition> it = app.taclet ()
                .getVariableConditions ();

        while (it.hasNext ()) {
            final VariableCondition vc = it.next ();
            if (vc instanceof OrderingCondition
                && !((OrderingCondition)vc).checkOrdering ( app, termOrdering ))
                return false;
        }

        return true;
    }

}
