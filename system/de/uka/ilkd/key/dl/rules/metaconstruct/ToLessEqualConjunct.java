package de.uka.ilkd.key.dl.rules.metaconstruct;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * @author s0805753@sms.ed.ac.uk
 */
public class ToLessEqualConjunct extends AbstractDLMetaOperator {

    public static final Name NAME = new Name("#toLessEqualConjunct");

    public ToLessEqualConjunct() {
        super(NAME, 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#sort(de.uka.ilkd.key.logic.Term[])
     */
    /*@Override*/
    public Sort sort(Term[] term) {
        return Sort.FORMULA;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#calculate(de.uka.ilkd.key.logic.Term,
     *      de.uka.ilkd.key.rule.inst.SVInstantiations,
     *      de.uka.ilkd.key.java.Services)
     */
    
    public Term calculate(Term term, SVInstantiations svInst, Services services) { 	
        try 
        {	return	MathSolverManager.getCurrentSimplifier().
            		toLessEqualConjunct(term.sub(0), services.getNamespaces());
        } 
        catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            e.printStackTrace(); // XXX
        }
        return term.sub(0);
    }
}
