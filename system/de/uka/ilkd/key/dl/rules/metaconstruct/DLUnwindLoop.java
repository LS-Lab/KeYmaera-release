/**
 * File created 30.01.2007
 */
package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.lang.reflect.InvocationTargetException;

import de.uka.ilkd.key.dl.model.Chop;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.DLStatementBlock;
import de.uka.ilkd.key.dl.model.Star;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.impl.TermFactoryImpl;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * Unwinds a given loop.
 * 
 * @author jdq
 * @since 30.01.2007
 * 
 */
public class DLUnwindLoop extends AbstractDLMetaOperator {

    public static final Name NAME = new Name("#dlunwind");

    /**
     * 
     */
    public DLUnwindLoop() {
        super(NAME, 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#sort(de.uka.ilkd.key.logic.Term[])
     */
    @Override
    public Sort sort(Term[] term) {
        // if(term.length == 0) {
        return Sort.FORMULA;
        // } else {
        // return super.sort(term);
        // }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#calculate(de.uka.ilkd.key.logic.Term,
     *      de.uka.ilkd.key.rule.inst.SVInstantiations,
     *      de.uka.ilkd.key.java.Services)
     */
    public Term calculate(Term term, SVInstantiations svInst, Services services) {
        Term t = term.sub(0);
        DLProgram program = (DLProgram) ((StatementBlock) term.sub(0)
                .javaBlock().program()).getChildAt(0);
        Term post = term.sub(0).sub(0);
        TermFactory dlTf;
        try {
            dlTf = TermFactory.getTermFactory(TermFactoryImpl.class, services
                    .getNamespaces());
            //FIXME: the invariant gets lost here
            Star s = dlTf.createStar(program, null);
            Chop chop = dlTf.createChop(program, s);
            JavaBlock jb = JavaBlock.createJavaBlock(new DLStatementBlock(chop));
            return de.uka.ilkd.key.logic.TermFactory.DEFAULT.createProgramTerm(
                    term.sub(0).op(), jb, post);
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
        return t;
    }

}
