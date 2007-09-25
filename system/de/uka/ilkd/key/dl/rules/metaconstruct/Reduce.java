/**
 * File created 30.01.2007
 */
package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.rmi.RemoteException;
import java.util.ArrayList;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.PairOfTermAndQuantifierType;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * Metaoperator for wrapping the reduce call to Mathematica
 * 
 * @author jdq
 * @since 30.01.2007
 * 
 */
public class Reduce extends AbstractDLMetaOperator {

    public static final Name NAME = new Name("#reduce");

    /**
     * 
     */
    public Reduce() {
        super(NAME, 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#calculate(de.uka.ilkd.key.logic.Term,
     *      de.uka.ilkd.key.rule.inst.SVInstantiations,
     *      de.uka.ilkd.key.java.Services)
     */
    public Term calculate(Term term, SVInstantiations svInst, Services services) {
        try {
            return MathSolverManager.getCurrentQuantifierEliminator().reduce(
                    term.sub(0), new ArrayList<PairOfTermAndQuantifierType>());
        } catch (RemoteException e) {
            e.printStackTrace();// XXX
            throw new IllegalArgumentException("Exception occurred while trying to eliminate a quantifier", e);
        }
//        return term.sub(0);
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
