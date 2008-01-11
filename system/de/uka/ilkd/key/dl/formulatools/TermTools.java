/**
 * 
 */
package de.uka.ilkd.key.dl.formulatools;

import java.util.LinkedHashSet;
import java.util.Set;

import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.IteratorOfConstrainedFormula;
import de.uka.ilkd.key.logic.IteratorOfTerm;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.Visitor;
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


    /**
     * Splits a formula along all its conjunctions into the set of its conjuncts.
     * 
     * @param form
     * @return
     */
    public static Set<Term> splitConjuncts(Term form) {
        Set<Term> conjuncts = new LinkedHashSet<Term>();
        if (form.op() == Junctor.AND) {
            for (int i = 0; i < form.arity(); i++) {
                conjuncts.addAll(splitConjuncts(form.sub(i)));
            }
        } else {
            conjuncts.add(form);
        }
        return conjuncts;
    }

    /**
     * Checks whether 
     * @param set
     * @param formula
     * @return
     */
    private static boolean containsAll(Set<Term> set, Set<Term> formula) {
        all: for (Term fml : formula) {
            if (set.contains(fml)) {
                continue;
            }
            for (Term b : set) {
                if (b.toString().equals(fml.toString())) {
                    // @todo assert namespaces.unique
                    System.out.println(" WARNING: identical printout with different representation " + b + " and " + fml);
                    // FIXME string comparison is an ugly hack
                    continue all;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Quickly checks whether sup subsumes sub, i.e., all conjuncts of formula sub are identical to
     * one of the conjuncts of sub.  
     * @param set
     * @param formula
     * @return
     */
    public static boolean subsumes(Term sup, Term sub) {
        return subsumes(splitConjuncts(sup), sub);
    }
    public static boolean subsumes(Set<Term> sup, Term sub) {
        return subsumes(sup, splitConjuncts(sub));
    }
    public static boolean subsumes(Set<Term> sup, Set<Term> sub) {
        return containsAll(sup, sub);
    }



    /**
     * checks whether term x occurs in term t
     * 
     * @param x
     * @param t
     * @return
     */
    public static boolean occursIn(final Term x, Term t) {
        final boolean result[] = new boolean[] {false};
        t.execPreOrder(new Visitor() {

            @Override
            public void visit(Term visited) {
                if (visited.equals(x)) {
                    result[0] = true;
                }
           }
        });
        return result[0];
    }
}
