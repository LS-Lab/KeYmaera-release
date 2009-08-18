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

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.Mult;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.strategy.feature.SmallerThanFeature;

public abstract class AbstractMonomialSmallerThanFeature
                                         extends SmallerThanFeature {
    
    protected ImmutableList<Term> collectAtoms(Term t) {
        final AtomCollector m = new AtomCollector ();
        m.collect ( t );
        return m.getResult ();
    }
    
    private class AtomCollector extends Collector {
        protected void collect(Term te) {
            if ( te.op () == RealLDT.getFunctionFor(Mult.class) ) {
                collect ( te.sub ( 0 ) );
                collect ( te.sub ( 1 ) );
            } else {
                addTerm ( te );
            }
        }
    }    

}
