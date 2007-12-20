/**
 * 
 */
package de.uka.ilkd.key.dl.formulatools;

import java.util.Set;

import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.IteratorOfConstrainedFormula;
import de.uka.ilkd.key.logic.IteratorOfTerm;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.op.Junctor;

/**
 * @author andre
 *
 */
public class TermTools {
    /**
     * Explicit n-ary-fied version of {@link
     * de.uka.ilkd.logic.TermFactory#createJunctorTerm(Junctor,Term[])}.
     * 
     * @see orbital.logic.functor.Functionals#foldRight
     * @internal almost identical to
     * @see #createJunctorTermNAry(Term,Junctor,IteratorOfTerm)
     */
    public static final Term createJunctorTermNAry(Term c, Junctor op,
            IteratorOfConstrainedFormula i, Set<Term> skip) {
        Term construct = c;
        while (i.hasNext()) {
            ConstrainedFormula f = i.next();
            Term t = f.formula();
            if (!skip.contains(t)) {
                // ignore tautological constraints, since they do not contribute
                // to
                // the specification
                // but report others
                if (!f.constraint().isBottom())
                    throw new IllegalArgumentException(
                            "there is a non-tautological constraint on " + f
                                    + ". lower constraints, first");
                construct = TermFactory.DEFAULT.createJunctorTermAndSimplify(
                        op, construct, t);
            }
        }
        return construct;
    }

}
