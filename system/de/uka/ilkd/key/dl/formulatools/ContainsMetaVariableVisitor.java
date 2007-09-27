/**
 * File created 20.02.2007
 */
package de.uka.ilkd.key.dl.formulatools;

import java.util.List;

import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.MetaVariable;
import de.uka.ilkd.key.dl.strategy.features.FOSequence;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.IUpdateOperator;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.RigidFunction;

/**
 * This class is a visitor implementation that checks if a given term is a first
 * order term and contains a given Metavariable.
 * 
 * @author jdq
 * @since 27.08.2007
 * 
 */
public class ContainsMetaVariableVisitor extends Visitor {

    public static enum Result {
        CONTAINS_VAR, DOES_NOT_CONTAIN_VAR, CONTAINS_VAR_BUT_CANNOT_APPLY, DOES_NOT_CONTAIN_VAR_AND_IS_NOT_FO
    }

    private static final ContainsMetaVariableVisitor INSTANCE = new ContainsMetaVariableVisitor();

    private boolean foundMetavariable = false;

    private boolean fo = true;

//    private Metavariable var = null;

    private boolean insideSkolem;

    private List<Metavariable> variables;

    private ContainsMetaVariableVisitor() {
    }

    public synchronized static Result containsMetaVariableAndIsFO(
            List<Metavariable> variables, Term form) {
        INSTANCE.variables = variables;
        INSTANCE.fo = true;
        INSTANCE.foundMetavariable = false;
        INSTANCE.insideSkolem = false;
        form.execPreOrder(INSTANCE);
        if (INSTANCE.insideSkolem || (!INSTANCE.fo && INSTANCE.foundMetavariable)) {
            return Result.CONTAINS_VAR_BUT_CANNOT_APPLY;
        } else if (INSTANCE.fo && INSTANCE.foundMetavariable) {
            return Result.CONTAINS_VAR;
        } else if (INSTANCE.fo && !INSTANCE.foundMetavariable) {
            return Result.DOES_NOT_CONTAIN_VAR;
        } else {
            return Result.DOES_NOT_CONTAIN_VAR_AND_IS_NOT_FO;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
     */
    @Override
    public void visit(Term visited) {
        if (!FOSequence.isFOOperator(visited.op())) {
            fo = false;
            if (visited.op() instanceof Modality) {
                DLProgram program = (DLProgram) ((StatementBlock) visited
                        .javaBlock().program()).getFirstElement();
                if (visitProgram(program)) {
                    foundMetavariable = true;
                }
            } else if (visited.op() instanceof IUpdateOperator) {
                IUpdateOperator up = (IUpdateOperator) visited.op();
                for (int i = 0; i < up.locationCount(); i++) {
                    Term value = up.value(visited, i);
                    value.execPreOrder(this);
                }
            }
        } else if (variables.contains(visited.op())) {
            foundMetavariable = true;
//        } else if (visited.op() instanceof RigidFunction) {
//            RigidFunction f = (RigidFunction) visited.op();
//            if (f.isSkolem()) {
//                for(Metavariable var: variables) {
//                    if(visited.metaVars().contains(var)) {
//                        insideSkolem = true;
//                        break;
//                    }
//                }
//                
//            }
        }
    }

    /**
     * TODO jdq documentation since Aug 27, 2007
     * 
     * @param program
     */
    private boolean visitProgram(ProgramElement program) {
        boolean result = false;
        if (program instanceof DLNonTerminalProgramElement) {
            DLNonTerminalProgramElement dln = (DLNonTerminalProgramElement) program;
            for (ProgramElement pe : dln) {
                result |= visitProgram(pe);
            }

        } else if (program instanceof MetaVariable) {
            MetaVariable v = (MetaVariable) program;
            for(Metavariable var: variables) {
                result |= v.getElementName().equals(var.name());    
            }
        }
        return result;
    }
}
