/*******************************************************************************
 * Copyright (c) 2012 Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jan-David Quesel - initial API and implementation
 ******************************************************************************/
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

import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.strategy.termProjection.AnnotationProjection;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.TacletApp;
import de.uka.ilkd.key.strategy.feature.BinaryTacletAppFeature;
import de.uka.ilkd.key.strategy.feature.Feature;
import de.uka.ilkd.key.strategy.termProjection.ProjectionToTerm;

/**
 * Feature that returns zero iff a certain annotation is defined for a \find match modality.
 */
public class AnnotatedFeature extends BinaryTacletAppFeature {

    private final ProjectionToTerm instProj;

    public static Feature create(String annotationKey) {
        return new AnnotatedFeature ( annotationKey );
    }

    private AnnotatedFeature(String annotationKey) {
        instProj = AnnotationProjection.create ( annotationKey, false, -1, -1);
    }

    protected boolean filter(TacletApp app, PosInOccurrence pos, Goal goal) {
        if (DLOptionBean.INSTANCE.isIgnoreAnnotations()) {
            // TODO cleanup this hack. We should ignore annotations in the strategy itself not in the system utilities 
            return false;
        }
        return instProj.toTerm ( app, pos, goal ) != null;
    }

}
