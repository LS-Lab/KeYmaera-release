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

    private static long count;

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
