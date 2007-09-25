/**
 * File created 28.02.2007
 */
package de.uka.ilkd.key.dl.rules.metaconstruct;

import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.dl.model.RandomAssign;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * TODO jdq documentation
 * 
 * @author jdq
 * @since 28.02.2007
 * 
 */
public class DLRandomAssign extends AbstractDLMetaOperator {

    /**
     * 
     */
    private static final Name NAME = new Name("#randomass");

    /**
     * @param name
     * @param arity
     */
    public DLRandomAssign() {
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
        Term result = term.sub(0);
        long l = 0;
        LogicVariable var = null;
        ProgramVariable progVar = (ProgramVariable) ((RandomAssign) ((StatementBlock) result
                .javaBlock().program()).getChildAt(0)).getChildAt(0);
        while (var == null) {
            Name name2 = new Name(progVar.getElementName().toString() + "_"
                    + l++);
            if (services.getNamespaces().lookup(name2) == null) {
                var = new LogicVariable(name2, RealLDT.getRealSort());
                services.getNamespaces().variables().add(var);
                break;
            }
        }
        System.out.println(services.getNamespaces().programVariables());//XXX
        if (result.op() == Modality.BOX || result.op() == Modality.TOUT) {
            return TermBuilder.DF
                    .all(
                            var,
                            TermBuilder.DF
                                    .tf()
                                    .createUpdateTerm(
                                            TermBuilder.DF
                                                    .var((de.uka.ilkd.key.logic.op.ProgramVariable) services
                                                            .getNamespaces()
                                                            .programVariables()
                                                            .lookup(
                                                                    progVar
                                                                            .getElementName())),
                                            TermBuilder.DF.var(var),
                                            result.sub(0)));
        } else if (result.op() == Modality.DIA) { // TODO: add
            // Modality.Finally
            return TermBuilder.DF
                    .ex(
                            var,
                            TermBuilder.DF
                                    .tf()
                                    .createUpdateTerm(
                                            TermBuilder.DF
                                                    .var((de.uka.ilkd.key.logic.op.ProgramVariable) services
                                                            .getNamespaces()
                                                            .programVariables()
                                                            .lookup(
                                                                    progVar
                                                                            .getElementName())),
                                            TermBuilder.DF.var(var),
                                            result.sub(0)));
        } else {
            throw new IllegalStateException("Unknown modality type: "
                    + result.op());
        }

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
