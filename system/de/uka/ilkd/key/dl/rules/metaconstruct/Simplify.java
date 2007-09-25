/**
 * File created 30.01.2007
 */
package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * Metaoperator for wrapping the simplify call to Mathematica
 * 
 * @author jdq
 * @since 30.01.2007
 * 
 */
public class Simplify extends AbstractDLMetaOperator {

    public static final Name NAME = new Name("#simplify");

    /**
     * 
     */
    public Simplify() {
        super(NAME, 2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#calculate(de.uka.ilkd.key.logic.Term,
     *      de.uka.ilkd.key.rule.inst.SVInstantiations,
     *      de.uka.ilkd.key.java.Services)
     */
    public Term calculate(Term term, SVInstantiations svInst, Services services) {
        Set<Term> assumptions = new HashSet<Term>();
        assumptions.add(term.sub(1));
        try {
            return MathSolverManager.getCurrentSimplifier().simplify(
                    term.sub(0), assumptions);
        } catch (RemoteException e) {
            e.printStackTrace(); // XXX
        }
        return term.sub(0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#sort(de.uka.ilkd.key.logic.Term[])
     */
    @Override
    public Sort sort(Term[] term) {
        return Sort.FORMULA;
    }

}
