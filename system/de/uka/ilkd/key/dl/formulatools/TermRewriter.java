/**
 * 
 */
package de.uka.ilkd.key.dl.formulatools;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.op.RigidFunction;

/**
 * TODO jdq documentation since Sep 27, 2007 
 * @author jdq
 * @since Sep 27, 2007
 * 
 */
public class TermRewriter {

    public static class Match {
        public Match(RigidFunction op, Term var) {
            this.skolemFunction = op;
            this.rewriteTo = var;
            assert skolemFunction.isSkolem();
        }
        RigidFunction skolemFunction;
        Term rewriteTo;
    }
    
    
    public static Term replace(Term term, Set<Match> matches) {
        return replace(term, matches, new boolean[] { false });
    }
    
    private static Term replace(Term term, Set<Match> matches, boolean[] hasChanged) {
        for(Match m: matches) {
            if(term.op() == m.skolemFunction) {
                hasChanged[0] = true;
                return m.rewriteTo;
            }
        }
        boolean[] thisHasChanged = new boolean[] { false };
        List<Term> arguments = new LinkedList<Term>();
        for(int i = 0; i < term.arity(); i++) {
            arguments.add(replace(term.sub(i), matches, thisHasChanged));
        }
        if(thisHasChanged[0]) {
            hasChanged[0] = true;
            return TermFactory.DEFAULT.createTerm(term.op(), arguments.toArray(new Term[0]), term.varsBoundHere(0), null);
        } else {
            return term;
        }
    }
    
    
    
    
}
