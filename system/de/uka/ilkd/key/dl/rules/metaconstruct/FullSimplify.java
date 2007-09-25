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
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * Metaoperator for wrapping the full simplify call of Mathematica
 * 
 * @author jdq
 * @since 30.01.2007
 * 
 */
public class FullSimplify extends AbstractDLMetaOperator {

    public static final Name NAME = new Name("#fullsimplify");

    /**
     * 
     */
    public FullSimplify() {
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
        Set<Term> assumptions = new HashSet<Term>();
        assumptions.add(term.sub(1));
        try {
            return MathSolverManager.getCurrentSimplifier().fullSimplify(
                    term.sub(0));
        } catch (RemoteException e) {
            e.printStackTrace(); // XXX
        }
        return term.sub(0);
    }

}
