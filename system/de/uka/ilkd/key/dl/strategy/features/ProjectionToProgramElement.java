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

import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;

/**
 * Interface for mappings from rule applications to terms. This is used, for
 * instance, for determining the instantiation of a schema variable. We also
 * allow projections to be partial, which is signalled by <code>toTerm</code>
 * returning <code>null</code>
 */
public interface ProjectionToProgramElement {
    ProgramElement toProgramElement ( RuleApp app, PosInOccurrence pos, Goal goal );
}
