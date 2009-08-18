// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//


package de.uka.ilkd.key.dl.strategy.features;

import java.util.Iterator;

import de.uka.ilkd.key.collection.ImmutableSLList;
import de.uka.ilkd.key.collection.ImmutableSet;
import de.uka.ilkd.key.dl.strategy.termProjection.Buffer;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.op.SchemaVariable;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.rule.TacletApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;
import de.uka.ilkd.key.strategy.feature.instantiator.BackTrackingManager;
import de.uka.ilkd.key.strategy.feature.instantiator.CPBranch;
import de.uka.ilkd.key.strategy.feature.instantiator.ChoicePoint;
import de.uka.ilkd.key.util.Debug;


/**
 * Feature representing a <code>ChoicePoint</code> for instantiating a schema
 * variable of a taclet with the term that is returned by a
 * <code>ProjectionToTerm</code>. This feature is useful in particular
 * combined with <code>ForEachCP</code>. Although the feature formally is a
 * choice point, it will always have exactly one branch
 */
public class ProgramSVInstantiationCP implements Feature {

    private final BackTrackingManager manager;

    private final Name svToInstantiate;
    private final Buffer<ProgramElement> value;

    public static Feature create(Name svToInstantiate,
    		Buffer<ProgramElement> value,
                                 BackTrackingManager manager) {
        return new ProgramSVInstantiationCP ( svToInstantiate, value, manager );
    }
        
    private ProgramSVInstantiationCP(Name svToInstantiate,
    		Buffer<ProgramElement> value,
                              BackTrackingManager manager) {
        this.svToInstantiate = svToInstantiate;
        this.value = value;
        this.manager = manager;
    }

    public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
        manager.passChoicePoint ( new CP (app, pos, goal), this );
        return LongRuleAppCost.ZERO_COST;
    }

    private SchemaVariable findSVWithName(TacletApp app) {
        final ImmutableSet<SchemaVariable> vars = app.uninstantiatedVars ();
        final Iterator<SchemaVariable> it = vars.iterator ();
        while ( it.hasNext () ) {
            final SchemaVariable svt = it.next ();
            if ( svt.name ().equals ( svToInstantiate ) ) return svt;
        }
        
        Debug.fail ( "Did not find schema variable "
                     + svToInstantiate + " that I was supposed to instantiate\n" +
                     "(taclet " + app.taclet().name() + ")\n" +
                     "Either the name of the variable is wrong, or the variable\n" +
                     "has already been instantiated as " + app.instantiations().lookupValue(svToInstantiate)+ "." );
        return null;
    }

    
    private class CP implements ChoicePoint {
        
        private final PosInOccurrence pos;
        private final RuleApp         app;
        private final Goal            goal;
    
        private CP(RuleApp app, PosInOccurrence pos, Goal goal) {
            this.pos = pos;
            this.app = app;
            this.goal = goal;
        }

        public Iterator<CPBranch> getBranches(RuleApp oldApp) {
            if ( ! ( oldApp instanceof TacletApp ) )
                Debug.fail ( "Instantiation feature is only applicable to " +
                             "taclet apps, but got " + oldApp );
            final TacletApp tapp = (TacletApp)oldApp;
            
            final SchemaVariable sv = findSVWithName ( tapp );
            final ProgramElement pe = value.to( app, pos, goal );

            final RuleApp newApp =
                tapp.addCheckedInstantiation ( sv,
                                               pe,
                                               goal.proof ().getServices (),
                                               true );

            final CPBranch branch = new CPBranch () {
                public void choose() {}
                public RuleApp getRuleAppForBranch() { return newApp; }
            };
            
            ImmutableSLList<CPBranch> nil = ImmutableSLList.nil();
            return nil.prepend ( branch ).iterator ();
        }
        
    }
}
