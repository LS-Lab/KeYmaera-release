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

import de.uka.ilkd.key.dl.rules.metaconstruct.MonomialReals;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.TacletApp;
import de.uka.ilkd.key.strategy.feature.BinaryTacletAppFeature;
import de.uka.ilkd.key.strategy.feature.Feature;
import de.uka.ilkd.key.strategy.termProjection.ProjectionToTerm;


/**
 * Return zero of the least common reducible of two monomials is so trivial that
 * it is not necessary to do the critical pair completion
 */
public class TrivialMonomialLCRFeature extends BinaryTacletAppFeature {
    private final ProjectionToTerm a, b;

    private TrivialMonomialLCRFeature(ProjectionToTerm a, ProjectionToTerm b) {
        this.a = a;
        this.b = b;
    }

    public static Feature create(ProjectionToTerm a, ProjectionToTerm b) {
        return new TrivialMonomialLCRFeature ( a, b );
    }

    protected boolean filter(TacletApp app, PosInOccurrence pos, Goal goal) {
        final MonomialReals aMon =
            MonomialReals.create ( a.toTerm ( app, pos, goal ) );
        final MonomialReals bMon =
            MonomialReals.create ( b.toTerm ( app, pos, goal ) );
        
        return aMon.variablesAreCoprime ( bMon );
   }
}
