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
 * 
 */
package de.uka.ilkd.key.dl.formulatools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;

import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.RigidFunction;

/**
 * This class keeps track of skolem functions introduced within the current
 * proof.
 * 
 * @author jdq
 * 
 */
public class SkolemfunctionTracker {

    public static final SkolemfunctionTracker INSTANCE = new SkolemfunctionTracker();

    private long count;

    private Map<RigidFunction, Long> orderMap;

    private final SkolemComparator COMPARATOR = new SkolemComparator();

    private SkolemfunctionTracker() {
        count = 0;
        orderMap = new WeakHashMap<RigidFunction, Long>();
    }

    public void add(RigidFunction sk) {
        orderMap.put(sk, count++);
    }

    public long getCount(RigidFunction sk) {
        return orderMap.get(sk);
    }

    public List<Term> getOrderedList(Set<Term> skolemSymbols) {
        List<Term> result = new ArrayList<Term>();
        TreeSet<Term> set = new TreeSet<Term>(COMPARATOR);
        set.addAll(skolemSymbols);
        for (Term f : set) {
            result.add(f);
        }
        return result;
    }

    private class SkolemComparator implements Comparator<Term> {

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Term o1, Term o2) {
            return (int) (orderMap.get(o2.op()) - orderMap.get(o1.op()));
        }

    }
}
