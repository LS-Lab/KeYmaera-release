/**
 * 
 */
package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import de.uka.ilkd.key.dl.formulatools.Prog2LogicConverter;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.impl.TermFactoryImpl;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * @author jdq
 *
 */
public class ImplicationIntroductor extends AbstractDLMetaOperator {

	/**
	 * @param name
	 * @param arity
	 */
	public ImplicationIntroductor() {
		super(new Name("#dlimplies"), 2);
	}

	 /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.Operator#validTopLevel(de.uka.ilkd.key.logic.Term)
     */
    public boolean validTopLevel(Term term) {
        return term.arity() == arity() && term.sub(1).sort() == Sort.FORMULA;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.Operator#sort(de.uka.ilkd.key.logic.Term[])
     */
    public Sort sort(Term[] term) {
        return term[1].sort();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.Operator#isRigid(de.uka.ilkd.key.logic.Term)
     */
    public boolean isRigid(Term term) {
        return false;
    }
	
	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.dl.rules.metaconstruct.AbstractDLMetaOperator#calculate(de.uka.ilkd.key.logic.Term, de.uka.ilkd.key.rule.inst.SVInstantiations, de.uka.ilkd.key.java.Services)
	 */
	@Override
	public Term calculate(Term term, SVInstantiations svInst, Services services) {
		DiffSystem one = (DiffSystem) ((StatementBlock) term.sub(0).javaBlock().program()).getChildAt(0);
		DiffSystem two = (DiffSystem) ((StatementBlock) term.sub(1).javaBlock().program()).getChildAt(0);
		
		try {
			TermFactory tf = TermFactory.getTermFactory(TermFactoryImpl.class, services.getNamespaces());
			Formula rOne = null;
			for(ProgramElement p: one) {
				if(rOne == null) {
					rOne = (Formula) p;
				} else {
					rOne = tf.createAnd(rOne, (Formula) p);
				}
			}
			Formula rTwo = null;
			for(ProgramElement p: two) {
				if(rTwo == null) {
					rTwo = (Formula) p;
				} else {
					rTwo = tf.createAnd(rTwo, (Formula) p);
				}
			}
			return Prog2LogicConverter.convert(tf.createImpl(rOne, rTwo), services);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
