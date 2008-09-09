/***************************************************************************
 *   Copyright (C) 2007 by André Platzer                                   *
 *   @informatik.uni-oldenburg.de                                    *
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

package de.uka.ilkd.key.dl.strategy;

import java.util.Set;

import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.rule.RuleApp;

/**
 * A DLStrategy with taboos, i.e., certain rule which will never be applied nor tried under no circumstances whatsoever.
 * @author ap
 */
public class TabooDLStrategy extends DLStrategy {

    private Set<Name> taboo;

    /**
     * DLStrategy with taboos, i.e., certain rule which will never be applied nor tried under no circumstances whatsoever.
     * @author ap
     * @see "TabooSearch"
     * @param p_proof
     * @param stopOnFirstCE
     * @param taboo the list of rule names that are taboo, i.e., will never be applied nor tried at all.
     */
    protected TabooDLStrategy(Proof p_proof, boolean stopOnFirstCE, long timeout, Set<Name> taboo) {
        super(p_proof, stopOnFirstCE, timeout);
        if (taboo == null) {
            throw new NullPointerException("Invalid taboo list null");
        }
        this.taboo = taboo;
    }

    protected boolean veto(RuleApp app, PosInOccurrence pio, Goal goal) {
        if (taboo.contains(app.rule().name())) {
            return true;
        } else {
            return super.veto(app, pio, goal);
        }
    }
}
