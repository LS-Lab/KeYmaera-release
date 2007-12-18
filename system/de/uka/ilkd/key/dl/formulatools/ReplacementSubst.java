// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.dl.formulatools;

import java.util.Map;

import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.op.*;

/**
 * Performs arbitrary term replacements as substitutions even unsound replacements.
 * @author ap
 */
public class ReplacementSubst {
    protected TermFactory tf = TermFactory.DEFAULT;

    private Map<Term,Term> replacements;
    
    public static ReplacementSubst create(Map<Term,Term> replacements) {
        if (replacements.isEmpty()) {
            return new ReplacementSubst(null) {
              public Term apply(Term t) {
                  return t;
              }
              public String toString() {return "<empty>";}
            };
        } else {
            return new ReplacementSubst(replacements);
        }
    }
    
    /**
     * A replacement substitution, replacing every occurrence of keys in replacements
     * by the respective value.
     * This is only sound for wise choices of replacements.
     * @param replacements a key-value pair (k,v) in replacements says that verbatim occurrences of
     * k will be replaced by v.
     */
    private ReplacementSubst(Map<Term,Term> replacements) {
	this.replacements = replacements;
    }

    public Map<Term, Term> getReplacements() {
        return replacements;
    }

    /** substitute <code>s</code> for <code>v</code> in <code>t</code>,
     * avoiding collisions by replacing bound variables in
     * <code>t</code> if necessary.
     */
    public Term apply(Term t) {
        return apply1( t );
    }

    private final Term apply1(Term t) {
        Term repl = replacements.get(t);
        if (repl != null) { 
            return repl;
        } else {
            return applyOnSubterms( t );
        }
    }

    /** substitute getReplacements() in 
     * every subterm of <code>t</code>, and build a new term.
     * It is assumed, that the case <code>getReplacements().contains(t)<code> is already 
     * handled.
     */
    private Term applyOnSubterms(Term t) {
	final int arity = t.arity();
	final Term[] newSubterms = new Term[arity];
	final ArrayOfQuantifiableVariable[] newBoundVars =
	    new ArrayOfQuantifiableVariable[arity];
	for ( int i=0; i<arity; i++ ) {
	    applyOnSubterm ( t, i, newSubterms, newBoundVars );
        }
	//@todo only if one of the subterms changed!
	return tf.createTerm(t.op(), newSubterms, newBoundVars, t.javaBlock());
    }

    /**
     * Apply the substitution of the subterm <code>subtermIndex</code> of
     * term/formula <code>completeTerm</code>. The result is stored in
     * <code>newSubterms</code> and <code>newBoundVars</code> (at index
     * <code>subtermIndex</code>)
     */
    private void applyOnSubterm (Term completeTerm,
                                   int subtermIndex,
                                   Term[] newSubterms,
                                   ArrayOfQuantifiableVariable[] newBoundVars) {
        if ( completeTerm.varsBoundHere ( subtermIndex ).size() > 0) {
            throw new UnsupportedOperationException("Quantifiers would need clash-resolving and alpha-renaming from ClashFreeSubst. Not yet implemented");
        } else {
            newBoundVars[subtermIndex] = completeTerm.varsBoundHere ( subtermIndex );
            newSubterms[subtermIndex] = apply1(completeTerm.sub ( subtermIndex ));
        }
    }

    
    public String toString() {
        return "replacement " + getReplacements();
    }
}
