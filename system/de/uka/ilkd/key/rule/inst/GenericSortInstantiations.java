// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.rule.inst;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.uka.ilkd.key.collection.*;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.SchemaVariable;
import de.uka.ilkd.key.logic.op.SortedSchemaVariable;
import de.uka.ilkd.key.logic.sort.CollectionSort;
import de.uka.ilkd.key.logic.sort.GenericSort;
import de.uka.ilkd.key.logic.sort.IntersectionSort;
import de.uka.ilkd.key.logic.sort.Sort;


/**
 * This class handles the instantiation of generic sorts (used for
 * generic taclets), i.e. the class tries to find a solution for the
 * conditions on the generic sorts given by the type hierarchy and the
 * chosen SV instantiations
 *
 * this class is immutable
 */

public class GenericSortInstantiations {

    public static final GenericSortInstantiations EMPTY_INSTANTIATIONS =
	new GenericSortInstantiations ( DefaultImmutableMap.<GenericSort,Sort>nilMap() );


    private final ImmutableMap<GenericSort,Sort> insts;


    private GenericSortInstantiations ( ImmutableMap<GenericSort,Sort> p_insts ) {
	insts = p_insts;	
    }
    
    /**
     * Create an object that solves the conditions given by the
     * instantiation iterator, i.e. instantiations of the generic
     * sorts used within "p_instantiations" are sought for which are
     * compatible with the instantiations of the SVs
     * @param p_instantiations list of SV instantiations
     * @param p_conditions additional conditions for sort instantiations
     * @throws GenericSortException iff the conditions could not be
     * solved
     */
    public static GenericSortInstantiations create
	( Iterator<ImmutableMapEntry<SchemaVariable,InstantiationEntry>> p_instantiations,
	  ImmutableList<GenericSortCondition>                           p_conditions ) {

	ImmutableList<GenericSort>                          sorts      =
	    ImmutableSLList.<GenericSort>nil();
	GenericSortCondition                       c;
	
        final Iterator<GenericSortCondition>             it;

	// Find the generic sorts within "p_instantiations" and
	// "p_conditions", create the conditions

	for ( it = p_conditions.iterator (); it.hasNext (); )
	    sorts = sorts.prepend ( it.next ().getGenericSort () );

	while ( p_instantiations.hasNext () ) {
	    c = GenericSortCondition.createCondition
		( p_instantiations.next ().value () );
	    if ( c != null ) {
		p_conditions = p_conditions.prepend ( c );
		sorts        = sorts       .prepend ( c.getGenericSort () );
	    }
	}
	return create ( sorts, p_conditions );
    }


    /**
     * Create an object that holds instantiations of the generic sorts
     * "p_sorts" satisfying the conditions "p_conditions"
     * @param p_sorts generic sorts to instantiate
     * @param p_conditions conditions the instantiations have to
     * satisfy
     * @throws GenericSortException if no instantiations has been
     * found
     */
    public static GenericSortInstantiations create
	( ImmutableList<GenericSort>          p_sorts,
	  ImmutableList<GenericSortCondition> p_conditions ) {

	if ( p_sorts.isEmpty() )
	    return EMPTY_INSTANTIATIONS;

	return new GenericSortInstantiations ( solve ( p_sorts,
						       p_conditions ) );
    }


    /**
     * @return Boolean.TRUE if the sorts used within "p_entry" are
     * correct, Boolean.FALSE if the sorts are definitely incorrect,
     * null if the sorts could (perhaps) be made correct by choosing
     * the right generic sort instantiations
     */
    public Boolean checkSorts ( InstantiationEntry p_entry ) {
	if ( !( p_entry instanceof TermInstantiation ) ||
	     p_entry.getSchemaVariable ().isProgramSV () )
	    return Boolean.TRUE;

	final GenericSortCondition c =
	    GenericSortCondition.createCondition ( p_entry );
	if ( c != null ) return checkCondition ( c );
	
	final SortedSchemaVariable sv =
	    (SortedSchemaVariable)p_entry.getSchemaVariable ();
        final Term term = ( (TermInstantiation)p_entry ).getTerm ();
        
        if ( GenericSortCondition.subSortsAllowed ( sv ) )
            return term.sort ().extendsTrans ( sv.sort () ) ? 
                    Boolean.TRUE : Boolean.FALSE;

        return sv.sort () == term.sort () ? 
                Boolean.TRUE : Boolean.FALSE;
    }


    /**
     * @return Boolean.TRUE if the generic sort instantiations within
     * "this" satisfy "p_condition", null otherwise (this means,
     * "p_condition" could be satisfied by create a new
     * "GenericSortInstantiations"-object)
     */
    public Boolean checkCondition ( GenericSortCondition p_condition ) {
	Sort s = insts.get ( p_condition.getGenericSort () );

	if ( s == null ) { 
	    return null;
	}

        return p_condition.check( s, this ) ? Boolean.TRUE : null;   
    }


    public boolean isInstantiated   ( GenericSort p_gs ) {
	return insts.containsKey ( p_gs );
    }


    public Sort    getInstantiation ( GenericSort p_gs ) {
	return insts.get ( p_gs );
    }

    public boolean isEmpty() {
        return insts.isEmpty();
    }

    /**
     * Create a list of conditions establishing the instantiations
     * stored by this object (not saying anything about further
     * generic sorts)
     */
    public ImmutableList<GenericSortCondition> toConditions () {
	ImmutableList<GenericSortCondition>          res  =
	    ImmutableSLList.<GenericSortCondition>nil();
	Iterator<ImmutableMapEntry<GenericSort,Sort>> it   =
	    insts.entryIterator ();
	ImmutableMapEntry<GenericSort,Sort>           entry;
	
	while ( it.hasNext () ) {
	    entry = it.next ();
	    res   = res.prepend ( GenericSortCondition.createIdentityCondition
				  ( entry.key (), entry.value () ) );
	}

	return res;
    }


    /**
     * @param services the Services class
     * @return p_s iff p_s is not a generic sort, the concrete sort
     * p_s is instantiated with currently otherwise 
     * @throws GenericSortException iff p_s is a generic sort which is
     * not yet instantiated
     */
    public Sort getRealSort ( SchemaVariable p_sv, Services services ) {
	return getRealSort ( ((SortedSchemaVariable)p_sv).sort (), services );
    }

    public Sort getRealSort ( Sort p_s, Services services ) {
	if ( p_s instanceof GenericSort ) {
	    p_s = getInstantiation ( (GenericSort)p_s );
	    if ( p_s == null ) {
		throw GenericSortException.UNINSTANTIATED_GENERIC_SORT;
            }
	} else if (p_s instanceof IntersectionSort) {
            final IntersectionSort inter = (IntersectionSort)p_s; 
            
            ImmutableSet<Sort> sos = DefaultImmutableSet.<Sort>nil();
            
            for (int i = 0, sz = inter.memberCount(); i < sz; i++) {
                sos = sos.add(getRealSort(inter.getComponent(i), services));
            }
            
            final Sort res = IntersectionSort.getIntersectionSort(sos, services);
            
            if (res == null) {
                throw new GenericSortException
                ( "Generic sort is instantiated with an intersection sort" +
                  " that has an empty domain." );
            }
            
            return res;
        } else if ( p_s instanceof CollectionSort ) {
	    Sort s = getRealSort ( ((CollectionSort)p_s).elementSort (), services );

	    s = ((CollectionSort)p_s).cloneFor ( s );

	    if ( s == null )
		throw new GenericSortException
		    ( "Generic collection sort is instantiated with a sort" +
		      " without collections" );

	    return s;
	}

        
        
	return p_s;
    }


    /**
     * Really solve the conditions given
     * @param p_sorts generic sorts that must be instantiated
     * @param p_conditions conditions to be solved
     * @throws GenericSortException no solution could be found
     * @return the/a found solution
     */
    private static ImmutableMap<GenericSort,Sort> solve
	( ImmutableList<GenericSort>          p_sorts,
	  ImmutableList<GenericSortCondition> p_conditions ) {

	ImmutableMap<GenericSort,Sort> res;

	// the generic sorts are sorted topologically, i.e. if sort A
	// is a supersort of sort B, then A appears behind B within
	// "topologicalSorts"
	ImmutableList<GenericSort> topologicalSorts = topology ( p_sorts );

	res = solveHelp ( topologicalSorts,
			  DefaultImmutableMap.<GenericSort,Sort>nilMap(),
			  p_conditions,
			  ImmutableSLList.<GenericSort>nil() );

	if ( res == null )
	    throw new GenericSortException
		( "Conditions for generic sorts could not be solved: ", 
		  p_conditions );

	return res;
    }

    
    private static final class FailException extends Exception {
        private static final long serialVersionUID = 5291022478863582449L;
    }
    private final static FailException FAIL_EXCEPTION = new FailException ();
    

    /**
     * Method which is called recursively and tries to instantiate one
     * (the first) generic sort from the "p_remainingSorts"-list
     * @param p_remainingSorts generic sorts which needs to be
     * instantiated (topologically sorted)
     * @param p_curRes instantiations so far
     * @param p_conditions conditions (see above)
     * @return a solution if one could be found, null otherwise
     */
    private static ImmutableMap<GenericSort,Sort> solveHelp
	( ImmutableList<GenericSort>          p_remainingSorts,
	  ImmutableMap<GenericSort,Sort>   p_curRes,
	  ImmutableList<GenericSortCondition> p_conditions,
	  ImmutableList<GenericSort>          p_pushedBack ) {

	if ( p_remainingSorts.isEmpty() )
	    return solveForcedInst ( p_pushedBack, p_curRes, p_conditions );

	// next generic sort to seek an instantiation for
	final GenericSort gs = p_remainingSorts.head ();
	p_remainingSorts = p_remainingSorts.tail ();

	// Find the sorts "gs" has to be a supersort of and the
	// identity conditions
	ImmutableList<Sort>                     subsorts     = ImmutableSLList.<Sort>nil();
	ImmutableList<GenericSortCondition>     idConditions =
	                        ImmutableSLList.<GenericSortCondition>nil();

	// subsorts given by the conditions (could be made faster
	// by using a hash map for storing the conditions)
        {
            for (final GenericSortCondition c : p_conditions) {
                if (c.getGenericSort() == gs) {
                    if (c instanceof GenericSortCondition.GSCSupersort)
                        subsorts = subsorts.prepend
                                (((GenericSortCondition.GSCSupersort) c).getSubsort());
                    else if (c instanceof GenericSortCondition.GSCIdentity)
                        idConditions = idConditions.prepend(c);
                }
            }
        }

        
        // add the subsorts given by the already instantiated
        // generic sorts
        subsorts = addChosenInstantiations ( p_curRes, gs, subsorts );

        // Solve the conditions
        final ImmutableList<Sort> chosenList;
        try {
            chosenList = chooseResults ( gs, idConditions );
        } catch ( FailException e ) {
            return null;
        }
        
        if ( chosenList != null ) {
            return descend ( p_remainingSorts,
                              p_curRes,
                              p_conditions,
                              p_pushedBack,
                              gs,
                              subsorts,
                              chosenList );
        } else if ( !subsorts.isEmpty() ) {
            // if anything else has failed, construct minimal
            // supersorts of the found subsorts and try them
            final ImmutableList<Sort> superSorts = minimalSupersorts ( subsorts );

            return descend ( p_remainingSorts,
                              p_curRes,
                              p_conditions,
                              p_pushedBack,
                              gs,
                              subsorts,
                              superSorts );
        } else {
            // and if even that did not work, remove the generic
            // sort from the list and try again later
            return solveHelp ( p_remainingSorts,
                               p_curRes,
                               p_conditions,
                               p_pushedBack.prepend ( gs ) );
        }
    }


    private static ImmutableMap<GenericSort,Sort>
                                descend (ImmutableList<GenericSort> p_remainingSorts,
                                         ImmutableMap<GenericSort,Sort> p_curRes,
                                         ImmutableList<GenericSortCondition> p_conditions,
                                         ImmutableList<GenericSort> p_pushedBack,
                                         GenericSort p_gs,
                                         ImmutableList<Sort> p_subsorts,
                                         ImmutableList<Sort> p_chosenList) {
        for (final Sort chosen : p_chosenList) {
            if (!isSupersortOf(chosen, p_subsorts) // this test is unnecessary in some cases
                    || !p_gs.isPossibleInstantiation(chosen)) continue;

            final ImmutableMap<GenericSort, Sort> res = solveHelp(p_remainingSorts,
                    p_curRes.put(p_gs,
                            chosen),
                    p_conditions,
                    p_pushedBack);
            if (res != null) return res;
        }
        return null;
    }


    private static ImmutableList<Sort> chooseResults (GenericSort p_gs,
                                             ImmutableList<GenericSortCondition> p_idConditions)
                                                        throws FailException {
        if ( !p_idConditions.isEmpty() ) {
            // then the instantiation is completely determined by
            // an identity condition
            final Iterator<GenericSortCondition> itC = p_idConditions.iterator ();
            final Sort chosen = condSort ( itC );

            // other identity conditions must lead to the same
            // instantiation
            while ( itC.hasNext () ) {
                if ( chosen != condSort ( itC ) ) throw FAIL_EXCEPTION;
            }

            return ImmutableSLList.<Sort>nil().prepend ( chosen );
        } else {
            // if a list of possible instantiations of the generic
            // sort has been given, use it
            final ImmutableList<Sort> res = toList ( p_gs.getOneOf () );
            if ( res.isEmpty () ) return null;
            return res;
        }
    }


    private static ImmutableList<Sort> toList (ImmutableSet<Sort> p_set) {
        ImmutableList<Sort> res;
        res = ImmutableSLList.<Sort>nil();
        for (Sort aP_set : p_set) res = res.prepend(aP_set);
        return res;
    }


    private static Sort condSort (Iterator<GenericSortCondition> itC) {
        return ( (GenericSortCondition.GSCIdentity)itC.next () ).getSort ();
    }


    private static ImmutableList<Sort> addChosenInstantiations (ImmutableMap<GenericSort,Sort> p_curRes,
                                                       GenericSort p_gs,
                                                       ImmutableList<Sort> p_subsorts) {
        final Iterator<ImmutableMapEntry<GenericSort,Sort>> it = p_curRes.entryIterator ();
        while ( it.hasNext () ) {
            final ImmutableMapEntry<GenericSort,Sort> entry = it.next ();
            if ( entry.key ().extendsTrans ( p_gs ) )
                p_subsorts = p_subsorts.prepend ( entry.value () );
        }
        return p_subsorts;
    }


    /**
     * Method which is called by solveHelp and tries to instantiate
     * the generic sorts for which GSCForceInstantiation-conditions
     * (with "maximum" parameter) are contained within "p_conditions"
     * @param p_curRes instantiations so far
     * @param p_conditions conditions (see above)
     * @return a solution if one could be found, null otherwise
     */
    private static ImmutableMap<GenericSort,Sort> solveForcedInst
	( ImmutableList<GenericSort>          p_remainingSorts,
	  ImmutableMap<GenericSort,Sort>   p_curRes,
	  ImmutableList<GenericSortCondition> p_conditions ) {

	if ( p_remainingSorts.isEmpty() )
	    return p_curRes; // nothing further to be done

	Iterator<GenericSort> it = topology ( p_remainingSorts ).iterator ();
	p_remainingSorts         = ImmutableSLList.<GenericSort>nil();

	// reverse the order of the sorts, to start with the most
	// general one
	while ( it.hasNext () )
	    p_remainingSorts = p_remainingSorts.prepend ( it.next () );

	return solveForcedInstHelp ( p_remainingSorts,
				     p_curRes );
    }


    /**
     * Method which is called recursively and tries to instantiate one
     * (the first) generic sort from the "p_remainingSorts"-list
     * @param p_remainingSorts generic sorts which needs to be
     * instantiated (topologically sorted, starting with the most
     * general sort)
     * @param p_curRes instantiations so far
     * @return a solution if one could be found, null otherwise
     */
    private static ImmutableMap<GenericSort,Sort> solveForcedInstHelp
	( ImmutableList<GenericSort>        p_remainingSorts,
	  ImmutableMap<GenericSort,Sort> p_curRes ) {
	if ( p_remainingSorts.isEmpty() ) {
	    // we're done
	    return p_curRes;
	} else {
	    // next generic sort to seek an instantiation for
	    GenericSort gs   = p_remainingSorts.head ();
	    p_remainingSorts = p_remainingSorts.tail ();

	    Iterator<Sort>           it;
	    Sort                     cur;
	    ImmutableMap<GenericSort,Sort> res;
	    HashSet<Sort>                  todo   = new HashSet<Sort> ();
	    HashSet<Sort>                  done   = new HashSet<Sort> ();
	    Sort                     cand;

	    // search for instantiated supersorts of gs (the method
	    // below is neither fast nor complete, but should find a
	    // solution for relevant situations)

	    // insert into the working list (set)
	    it = gs.extendsSorts ().iterator ();
	    while ( it.hasNext () )
		todo.add ( it.next () );

	    while ( !todo.isEmpty () ) {
		it  = null;
		cur = todo.iterator ().next ();
		todo.remove ( cur );
		done.add    ( cur );

		if ( cur instanceof GenericSort ) {
		    cand = p_curRes.get ( (GenericSort)cur );
		    if ( cand == null )
			it = cur.extendsSorts ().iterator ();
		} else {
		    it   = cur.extendsSorts ().iterator ();
		    cand = cur;
		}

		if ( cand != null &&
		     isPossibleInstantiation ( gs, cand, p_curRes ) ) {
		    res = solveForcedInstHelp ( p_remainingSorts,
						p_curRes.put ( gs, cand ) );
		    if ( res != null )
			return res;
		}

		if ( it != null ) {
		    while ( it.hasNext () ) {
			cur = it.next ();
			if ( !done.contains ( cur ) )
			    todo.add ( cur );
		    }
		}
	    }
	}

	return null;
    }


    /**
     * Sort generic sorts topologically, i.e. if sort A is a supersort
     * of sort B, then A appears behind B within the return value
     * @return sorted sorts
     */
    private static ImmutableList<GenericSort> topology ( ImmutableList<GenericSort> p_sorts ) {
	ImmutableList<GenericSort>     res     = ImmutableSLList.<GenericSort>nil();
	Iterator<GenericSort> it;
	GenericSort           curMax;
	GenericSort           tMax;
	ImmutableList<GenericSort>     tList;

	while ( !p_sorts.isEmpty() ) {
	    // search for a maximal element
	    it      = p_sorts.iterator ();
	    curMax  = it.next ();

	    if ( res.contains ( curMax ) ) {
		p_sorts = p_sorts.tail ();
		continue;
	    }

	    tList   = ImmutableSLList.<GenericSort>nil();

	    while ( it.hasNext () ) {
		tMax = it.next ();
		if ( !res.contains ( tMax ) ) {
		    if ( curMax.extendsTrans ( tMax ) ) {
			tList  = tList.prepend ( curMax );
			curMax = tMax;
		    } else
			tList  = tList.prepend ( tMax );
		}
	    }

	    res     = res.prepend ( curMax );
	    p_sorts = tList;
	}

	return res;
    }

    /**
     * Find all minimal common supersorts of "p_itSorts"
     *
     * PRECONDITION: !p_sorts.isEmpty ()
     */
    private static ImmutableList<Sort> minimalSupersorts ( ImmutableList<Sort> p_sorts ) {
        // if the list only consists of a single sort, return this sort
        if ( p_sorts.size () == 1 ) return p_sorts;
            
        final Iterator<Sort> p_itSorts = p_sorts.iterator();
	HashSet<Sort>        inside    = new HashSet<Sort> ();
	HashSet<Sort>        outside   = new HashSet<Sort> ();
	HashSet<Sort>        todo      = new HashSet<Sort> ();

	// the null sort has to be handled separately, as it doesn't
	// provide a list of direct supersorts
	final boolean checkNULL = insertFirstSort ( p_itSorts, inside );

	// Find a set "inside" of common supersorts of "p_itSorts"
	// that contains all minimal common supersorts
	while ( !inside.isEmpty () && p_itSorts.hasNext () ) {
	    final Sort condition = p_itSorts.next ();

	    // At this point todo.isEmpty ()
	    final HashSet<Sort> t = todo;
	    todo            = inside;
	    inside          = t;

	    while ( !todo.isEmpty () ) {
		final Sort nextTodo = getOneOf ( todo );

		if ( !inside .contains ( nextTodo ) &&
		     !outside.contains ( nextTodo ) ) {
		    if ( condition.extendsTrans ( nextTodo ) )
			inside .add ( nextTodo );
		    else {
			outside.add ( nextTodo );			
			addSortsToSet ( todo, nextTodo.extendsSorts () );
		    }
		}
	    }
	}

	if ( checkNULL ) {
	    // At this point todo.isEmpty ()
	    treatNullSorts ( inside, todo );
	    inside = todo;
	}

	// Find the minimal elements of "inside"
	return findMinimalElements ( inside );
    }


    /**
     * Find all minimal elements of the given set <code>p_inside</code>
     */
    private static ImmutableList<Sort> findMinimalElements (Set<Sort> p_inside) {
        if ( p_inside.size () == 1 )
            return ImmutableSLList.<Sort>nil()
                        .prepend ( p_inside.iterator ().next () );

        ImmutableList<Sort> res = ImmutableSLList.<Sort>nil();
        final Iterator<Sort> it = p_inside.iterator ();
        
        mainloop: while ( it.hasNext () ) {
            final Sort sort = it.next ();

            ImmutableList<Sort> res2 = ImmutableSLList.<Sort>nil();
            for (final Sort oldMinimal : res) {

                if (oldMinimal.extendsTrans(sort))
                    continue mainloop;
                else if (!sort.extendsTrans(oldMinimal))
                    res2 = res2.prepend(oldMinimal);
            }

            res = res2.prepend ( sort );
        }
        
        return res;
    }


    /**
     * Remove all sorts that are not supersorts of the null sort, add the found
     * sorts to the set <code>p_emptySet</code>
     */
    private static void treatNullSorts (HashSet<Sort> p_inside, HashSet<Sort> p_emptySet) {

        for (final Sort sort : p_inside) {
            if (Sort.NULL.extendsTrans(sort)) p_emptySet.add(sort);
        }
    }


    private static Sort getOneOf (Set<Sort> p_set) {
        final Sort nextTodo = p_set.iterator ().next ();
        p_set.remove ( nextTodo );
        return nextTodo;
    }


    private static void addSortsToSet (Set<Sort> p_set, ImmutableSet<Sort> p_sorts) {
        for (Sort p_sort : p_sorts) p_set.add(p_sort);
    }


    /**
     * Add the first non-NULL sort given by the iterator <code>p_itSorts</code>
     * to the set <code>inside</code>, return true iff NULL sorts have been
     * found and omitted doing this. If <code>p_itSorts</code> does only
     * deliver the sort NULL, add NULL to the set <code>inside</code>
     */
    private static boolean insertFirstSort (Iterator<Sort> p_itSorts,
                                            HashSet<Sort> inside) {
        boolean checkNULL = false;
        Sort cand = p_itSorts.next ();
        while ( cand == Sort.NULL && p_itSorts.hasNext () ) {
            cand = p_itSorts.next ();
            checkNULL = true;
        }
        inside.add ( cand );
        return checkNULL;
    }


    /**
     * @return true iff "p_s" is supersort of every sort of
     * "p_subsorts"
     */
    private static boolean isSupersortOf ( Sort       p_s,
					   ImmutableList<Sort> p_subsorts ) {

        for (Sort p_subsort : p_subsorts) {
            if (!p_subsort.extendsTrans(p_s))
                return false;
        }

	return true;
    }


    /**
     * @return true iff "p_s" is a valid instantiation of the generic
     * sort "p_gs", and this instantiation is consistent with
     * previously chosen instantiations
     */
    private static boolean isPossibleInstantiation
	( GenericSort                p_gs,
	  Sort                       p_s,
	  ImmutableMap<GenericSort,Sort>   p_curRes) {
        
	if ( !p_gs.isPossibleInstantiation ( p_s ) )
	    return false;

	// check whether the new instantiation is consistent with the
	// already chosen instantiations
	final Iterator<ImmutableMapEntry<GenericSort,Sort>> itEntry = p_curRes.entryIterator ();
	while ( itEntry.hasNext () ) {
	    final ImmutableMapEntry<GenericSort,Sort> entry = itEntry.next ();

            if ( entry .key   ().extendsTrans ( p_gs ) &&
		 !entry.value ().extendsTrans ( p_s  ) ||
		 p_gs.extendsTrans ( entry.key   () ) &&
		 !p_s.extendsTrans ( entry.value () ) )
		return false;
	}	
	return true;
    }


    /** toString */
    public String toString () {
	Iterator<ImmutableMapEntry<GenericSort,Sort>> it  = insts.entryIterator ();
	ImmutableMapEntry<GenericSort,Sort>           entry;
	String                              res = "";

	while ( it.hasNext () ) {
	    if ( !"".equals ( res ) )
		res += ", ";
	    entry  = it.next ();
	    res   += entry.key () + "=" + entry.value ();
	}

	return res;
    }


    /**
     * ONLY FOR JUNIT TESTS
     */

    public ImmutableMap<GenericSort,Sort> getAllInstantiations () {
	return insts;
    }

}
