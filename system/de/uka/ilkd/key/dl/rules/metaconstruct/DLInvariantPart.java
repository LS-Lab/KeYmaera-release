package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.wolfram.jlink.Expr;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.IODESolver.ODESolverResult;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.DL2ExprConverter;
import de.uka.ilkd.key.dl.formulatools.Prog2LogicConverter;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.impl.DiffSystemImpl;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.TermSymbol;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.parser.dl.NumberCache;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * @author ap
 */
public class DLInvariantPart extends AbstractDLMetaOperator {

    public static final Name NAME = new Name("#invariantPart");

    public DLInvariantPart() {
        super(NAME, 1);
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

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#calculate(de.uka.ilkd.key.logic.Term,
     *      de.uka.ilkd.key.rule.inst.SVInstantiations,
     *      de.uka.ilkd.key.java.Services)
     */
    public Term calculate(Term term, SVInstantiations svInst, Services services) {
        DiffSystem system = (DiffSystem) ((StatementBlock) term.sub(0)
                .javaBlock().program()).getChildAt(0);
        Term post = term.sub(0).sub(0);
        Term inv = DiffSystemImpl.getInvariant(system);
        return inv;
    }
}
