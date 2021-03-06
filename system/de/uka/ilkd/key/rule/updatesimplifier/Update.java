// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
package de.uka.ilkd.key.rule.updatesimplifier;

import java.util.HashMap;
import java.util.HashSet;

import de.uka.ilkd.key.collection.*;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.*;
import de.uka.ilkd.key.logic.sort.AbstractSort;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.util.Debug;

/**
 * represents an update and is used as basis data structure for the
 * updatesimplifier. Currently this is only a naive default implementation.
 */
public abstract class Update {
    
    // just for unique handling of anonymous updates
    // it as to be taken care that this sort does not escape
    private static final Sort SPECIAL_SORT = 
        new AbstractSort(new Name("special_sort")) {
            public ImmutableSet<Sort> extendsSorts() {              
                return DefaultImmutableSet.<Sort>nil();
            }
    	};
    
    /**
     * @author bubel
     */
    public static class UpdateInParts extends Update {

        private final HashMap<Location, ImmutableArray<AssignmentPair>> loc2assignmentPairs;

        private final HashSet<Location> locationCache;

        private final ImmutableArray<AssignmentPair> pairs;

        private ImmutableSet<QuantifiableVariable> freeVars = null;
        
        /**
         * @param pairs
         */
        public UpdateInParts(final ImmutableArray<AssignmentPair> pairs) {
            this.pairs = pairs;
            loc2assignmentPairs = new HashMap<Location, ImmutableArray<AssignmentPair>>();
            this.locationCache = new HashSet<Location>();

            for (int i = 0; i < pairs.size(); i++) {
                locationCache.add(pairs.get(i).location());
            }

        }

        /*
         * (non-Javadoc)
         * 
         * @see de.uka.ilkd.key.rule.updatesimplifier.Update#getAllAssignmentPairs()
         */
        public ImmutableArray<AssignmentPair> getAllAssignmentPairs() {
            return pairs;
        }

        /*
         * (non-Javadoc)
         * 
         * @see de.uka.ilkd.key.rule.updatesimplifier.Update#getAssignmentPairs(de.uka.ilkd.key.logic.op.Operator)
         */
        public ImmutableArray<AssignmentPair> getAssignmentPairs(Location loc) {
            if (!loc2assignmentPairs.containsKey(loc)) {
                ImmutableList<AssignmentPair> result = ImmutableSLList.<AssignmentPair>nil();
                for (int i = pairs.size() - 1; i >= 0; i--) {
                    AssignmentPair assignmentPair = pairs.get(i);
                    final Location op = assignmentPair.location();
                    if (loc.mayBeAliasedBy(op)) {
                        result = result.prepend(assignmentPair);
                    }
                }

                loc2assignmentPairs.put(loc, new ImmutableArray<AssignmentPair>(result
                        .toArray(new AssignmentPair[result.size()])));
            }
            return loc2assignmentPairs.get(loc);
        }

        /*
         * (non-Javadoc)
         * 
         * @see de.uka.ilkd.key.rule.updatesimplifier.Update#hasLocation(de.uka.ilkd.key.logic.op.Operator)
         */
        public boolean hasLocation(Location loc) {
            return locationCache.contains(loc);
        }

        /*
         * (non-Javadoc)
         * 
         * @see de.uka.ilkd.key.rule.updatesimplifier.Update#location(int)
         */
        public Location location(int n) {
            return pairs.get(n).location();
        }

        /*
         * (non-Javadoc)
         * 
         * @see de.uka.ilkd.key.rule.updatesimplifier.Update#locationCount()
         */
        public int locationCount() {
            return pairs.size();
        }

        /* (non-Javadoc)
         * @see de.uka.ilkd.key.rule.updatesimplifier.Update#freeVars()
         */
        public ImmutableSet<QuantifiableVariable> freeVars () {
            if ( freeVars == null ) {
                freeVars = DefaultImmutableSet.<QuantifiableVariable>nil();
                for ( int i = 0; i != pairs.size (); ++i )
                    freeVars = freeVars.union
                                ( pairs.get ( i ).freeVars () );
            }
            return freeVars;
        }

        /* (non-Javadoc)
         * @see de.uka.ilkd.key.rule.updatesimplifier.Update#getAssignmentPair(int)
         */
        public AssignmentPair getAssignmentPair (int locPos) {
            return pairs.get ( locPos );
        }
    }

    private static class UpdateWithUpdateTerm extends Update {

        private final HashMap<Location, ImmutableArray<AssignmentPair>> loc2assignmentPairs;

        private HashSet<Location> locationCache;

        private final Term update;

        private final IUpdateOperator updateOp;

        private ImmutableSet<QuantifiableVariable> freeVars = null;        

        public UpdateWithUpdateTerm(Term update) {
            this.update = update;	    
            this.updateOp = (IUpdateOperator) update.op();
            this.loc2assignmentPairs = new HashMap<Location, ImmutableArray<AssignmentPair>>();
            
            for ( int i = 0; i < updateOp.locationCount (); i++ ) {
                if ( updateOp.location ( i ) == StarLocation.ALL )
                    Debug.fail ( "Anonymous update shall not be created using this "
                                 + "class. (at least as long no generalized terms are "
                                 + "supported)" );
            }
        }

        /**
         * @return the assignment pairs making up the update
         */
        public ImmutableArray<AssignmentPair> getAllAssignmentPairs() {
            final AssignmentPair[] result = 
                new AssignmentPair[locationCount()];
            for (int i = 0; i < locationCount(); i++) {
                result[i] = getAssignmentPair ( i );
            }
            return new ImmutableArray<AssignmentPair>(result);
        }

        public AssignmentPair getAssignmentPair (int locPos) {
            if ( updateOp instanceof AnonymousUpdate )
                return new AssignmentPairLazy ( update, locPos );
            else if ( updateOp instanceof QuanUpdateOperator )
                return new QuanAssignmentPairLazy ( update, locPos );
            else
                Debug.fail ( "Unknown update operator" );            
            return null; // unreachable
        }

        /**
         * determines and returns all assignment pairs whose location part may 
         * be an alias of <code>loc</code>
         */
        public ImmutableArray<AssignmentPair> getAssignmentPairs(Location loc) {
            if (!loc2assignmentPairs.containsKey(loc)) {
                ImmutableList<AssignmentPair> result = ImmutableSLList.<AssignmentPair>nil();
                for (int i = updateOp.locationCount() - 1; i >= 0; i--) {
                    if ( loc.mayBeAliasedBy ( updateOp.location(i) ) )
                        result = result.prepend ( getAssignmentPair ( i ) );
                }

                loc2assignmentPairs.put(loc, 
                        new ImmutableArray<AssignmentPair>(result.toArray(new AssignmentPair[result.size()])));
            }
            return loc2assignmentPairs.get(loc);
        }

        public boolean hasLocation (Location loc) {
            if ( locationCache == null ) {
                this.locationCache = new HashSet<Location> ();

                for ( int i = 0; i < updateOp.locationCount (); i++ ) {
                    locationCache.add ( updateOp.location ( i ) );
                }
            }
            return locationCache.contains ( loc );
        }

        public Location location(int n) {
            return updateOp.location(n);
        }

        /**
         * returns the number of locations
         * 
         * @return the number of locations as int
         */
        public int locationCount() {
            return updateOp.locationCount();
        }

        /* (non-Javadoc)
         * @see de.uka.ilkd.key.rule.updatesimplifier.Update#freeVars()
         */
        public ImmutableSet<QuantifiableVariable> freeVars () {
            if ( freeVars == null ) {
                if ( update.sub ( update.arity () - 1 ).freeVars ().size () == 0 ) {
                    freeVars = update.freeVars ();
                } else {
                    freeVars = DefaultImmutableSet.<QuantifiableVariable>nil();
                    for ( int i = 0; i != locationCount (); ++i )
                        freeVars = freeVars.union ( getAssignmentPair ( i )
                                                    .freeVars () );
                }
            }
            return freeVars;
        }
    }

    /**
     * creates an update representation out of the top level operator of the
     * given update term
     */
    public static Update createUpdate(AssignmentPair[] pairs) {
        return new UpdateInParts(new ImmutableArray<AssignmentPair>(pairs));
    }

    /**
     * creates an update representation out of the top level operator of the
     * given update term. If it is no update term an update with zero assignment 
     * pairs is returned (only for temporarly representations)
     */
    public static Update createUpdate(Term t) {        
        if (t.op() instanceof AnonymousUpdate) {
           Term valueTerm = UpdateSimplifierTermFactory.
           DEFAULT.getBasicTermFactory().
           	createFunctionTerm
           	(new RigidFunction(t.op().name(), SPECIAL_SORT, new Sort[0]));
           AssignmentPair pair = new AssignmentPairImpl
           	(StarLocation.ALL, new Term[0],valueTerm);
           return createUpdate
           	(new AssignmentPair[]{pair});
        } else if (!(t.op() instanceof IUpdateOperator)) {
	    return createUpdate(new AssignmentPair[0]);
	}
        return new UpdateWithUpdateTerm(t);
    }

    /**
     * @return all assignment pairs of the update
     */
    public abstract ImmutableArray<AssignmentPair> getAllAssignmentPairs();

    /**
     * determines and returns all assignment pairs whose location part has the
     * same top level operator as the given one
     */
    public abstract ImmutableArray<AssignmentPair> getAssignmentPairs(Location loc);

    /**
     * returns true if the given location is updated by this update
     * 
     * @param loc
     *            the Operator specifying the location which is updated
     * @return true if location occurs on the left side of an assignment pair in
     *         this update
     */
    public abstract boolean hasLocation(Location loc);

    /**
     * returns the n-th location operator
     * 
     * @param n
     *            an int specifying the position of the location operator to be
     *            retrieved
     * @return the n-tl location operator
     */
    public abstract Location location(int n);

    /**
     * returns the number of locations
     * 
     * @return the number of locations as int
     */
    public abstract int locationCount();
    
    /**
     * returns true if this update object describes an anonymous update
     */
    public boolean isAnonymousUpdate() {
        return hasLocation(StarLocation.ALL);
    }
    
    /**
     * @return the set of quantifiable variables that turn up free in this
     *         update (when applying the update, it might happen that such
     *         variables have to be renaming to avoid collisions)
     */
    public abstract ImmutableSet<QuantifiableVariable> freeVars();
    
    public String toString() {
        StringBuffer result = new StringBuffer("{");
        ImmutableArray<AssignmentPair> pairs = getAllAssignmentPairs();
        for (int i = 0; i<pairs.size(); i++) {           
            result.append(pairs.get(i).toString());
            if (i<pairs.size()-1) result.append(" || ");
        }
        result.append("}");
        return result.toString();
    }

    // these classes are used to unify treatment of anonymous and 
    // normal updates. May become obsolete with generalized terms 
    public static class StarLocation extends NonRigidFunction 
    	implements Location {

        // important "name" must be initialized before ALL!
        private final static Name name = new Name("*");

        public final static StarLocation ALL = new StarLocation();
        
        
        private StarLocation() {
            super(name, SPECIAL_SORT, new Sort[0]);
        }
        
        /* (non-Javadoc)
         * @see de.uka.ilkd.key.logic.op.Location#mayBeAliasedBy(de.uka.ilkd.key.logic.op.Location)
         */
        public boolean mayBeAliasedBy(Location loc) {            
            return false;
        }

        /* (non-Javadoc)
         * @see de.uka.ilkd.key.logic.op.Operator#name()
         */
        public Name name() {            
            return name;
        }

        /* (non-Javadoc)
         * @see de.uka.ilkd.key.logic.op.Operator#validTopLevel(de.uka.ilkd.key.logic.Term)
         */
        public boolean validTopLevel(Term term) {
            return term.arity() == 0 && term.sort() != Sort.FORMULA;
        }

        /* (non-Javadoc)
         * @see de.uka.ilkd.key.logic.op.Operator#sort(de.uka.ilkd.key.logic.Term[])
         */
        public Sort sort(Term[] term) {            
            return SPECIAL_SORT;
        }
        
        /* (non-Javadoc)
         * @see de.uka.ilkd.key.logic.op.Operator#arity()
         */
        public int arity() {            
            return 0;
        }

        /* (non-Javadoc)
         * @see de.uka.ilkd.key.logic.op.Operator#isRigid(de.uka.ilkd.key.logic.Term)
         */
        public boolean isRigid(Term term) {          
            return true;
        }
    
    }

    public abstract AssignmentPair getAssignmentPair (int locPos);    
}
