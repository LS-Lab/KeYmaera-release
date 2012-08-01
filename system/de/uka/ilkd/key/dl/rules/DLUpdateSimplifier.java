/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel                                *
 *   quesel@informatik.uni-oldenburg.de                                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
/**
 * File created 05.02.2007
 */
package de.uka.ilkd.key.dl.rules;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.collection.ImmutableSLList;
import de.uka.ilkd.key.rule.IUpdateRule;
import de.uka.ilkd.key.rule.UpdateSimplifier;
import de.uka.ilkd.key.rule.updatesimplifier.ApplyAnonymousUpdateOnNonRigid;
import de.uka.ilkd.key.rule.updatesimplifier.ApplyOnAnonymousUpdate;
import de.uka.ilkd.key.rule.updatesimplifier.ApplyOnLocalVariableOrStaticField;
import de.uka.ilkd.key.rule.updatesimplifier.ApplyOnNonRigidTerm;
import de.uka.ilkd.key.rule.updatesimplifier.ApplyOnNonRigidWithExplicitDependencies;
import de.uka.ilkd.key.rule.updatesimplifier.ApplyOnRigidOperatorTerm;
import de.uka.ilkd.key.rule.updatesimplifier.ApplyOnRigidTerm;
import de.uka.ilkd.key.rule.updatesimplifier.ApplyOnUpdate;

/**
 * This is the modified update simplifier ({@link UpdateSimplifier}) used for
 * simplification of DLPrograms.
 * 
 * @author jdq
 * @since 05.02.2007
 * 
 */
public class DLUpdateSimplifier extends UpdateSimplifier {

    /**
     * 
     */
    public DLUpdateSimplifier() {
        ImmutableList<IUpdateRule> usRules = ImmutableSLList.nil();
        usRules = usRules.append(
                new ApplyOnAnonymousUpdate(this))
                .append(new ApplyAnonymousUpdateOnNonRigid(this))
                .append(new ApplyOnUpdate(this))
                .append(new ApplyOnLocalVariableOrStaticField(this))
                .append(new DLApplyOnModality(this))
                .append(new ApplyOnRigidTerm(this))
                .append(new ApplyOnRigidOperatorTerm(this))
                .append(new ApplyOnNonRigidWithExplicitDependencies(this))
                .append(new ApplyOnNonRigidTerm(this));

        setSimplificationRules(usRules);
    }

    private DLApplyOnModality lnkDLApplyOnModality;
}
