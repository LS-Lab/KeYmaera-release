/**
 * File created 20.02.2007
 */
package de.uka.ilkd.key.dl.formulatools;

import java.util.HashSet;

import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.RigidFunction;

/**
 * Visitor that returns the skolem symbol that has the highest parameter count.
 * 
 * @author jdq
 * @since 20.02.2007
 * 
 */
public class SkolemSymbolWithMostParametersVisitor extends Visitor {

    private static final SkolemSymbolWithMostParametersVisitor INSTANCE = new SkolemSymbolWithMostParametersVisitor();

    private static HashSet<Term> skolemTerms;

    private SkolemSymbolWithMostParametersVisitor() {
    }

    public synchronized static Term getSkolemSymbolWithMostParameters(
            Term form) {
        skolemTerms = new HashSet<Term>();
        form.execPreOrder(INSTANCE);
        Term result = null;
        for (Term term : skolemTerms) {
            if (result == null) {
                result = term;
            } else {
                if (result.arity() > term.arity()) {
                    result = term;
                }
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
     */
    @Override
    public void visit(Term visited) {
        if ((visited.op() instanceof RigidFunction)
                && ((RigidFunction) visited.op()).isSkolem()) {
            skolemTerms.add(visited);
        }
    }
}
