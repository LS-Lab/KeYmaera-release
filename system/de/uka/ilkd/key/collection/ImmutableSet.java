package de.uka.ilkd.key.collection;

import java.util.Iterator;

/** interface implemented by non-destructive Sets.
 * CONVENTION: Each SetOf<T> implementation has to offer a public static
 *    final variable .<called>nil()
 */

public interface ImmutableSet<T> extends Iterable<T>, java.io.Serializable {

    /** adds an element */
    ImmutableSet<T> add(T element);

    /** @return union of this set with set */
    ImmutableSet<T> union(ImmutableSet<T> set);

    /** @return Iterator<T> of the set */
    Iterator<T> iterator() ;

    /** @return true iff obj in set */
    boolean contains(T obj);

    /** @return true iff this set is subset of set s */
    boolean subset(ImmutableSet<T> s);

    /** @return int the cardinality of the set */
    int size();

    /** @return true iff the set is empty */
    boolean isEmpty();

    /** @return set without element */
    ImmutableSet<T> remove(T element);

    /** @return true iff the this set is subset of o and vice versa.
     */
    boolean equals(Object o);

    /** adds an element, barfs if the element is already present
     * @param element of type <T> that has to be added to this set
     * @throws de.uka.ilkd.key.collection.NotUniqueException if the element is already present
     */
    ImmutableSet<T> addUnique(T element) throws NotUniqueException;

    /**
     * Convert the set to a Java array
     */
    <S> S[] toArray(S[] array);

}