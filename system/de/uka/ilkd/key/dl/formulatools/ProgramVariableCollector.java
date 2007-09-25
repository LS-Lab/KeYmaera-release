/**
 * File created 07.02.2007
 */
package de.uka.ilkd.key.dl.formulatools;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.uka.ilkd.key.dl.model.Assign;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.RandomAssign;
import de.uka.ilkd.key.dl.model.Star;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Modality;

/**
 * The ProgramVariableDeclaratorVisitor is used to insert declarations of
 * program variables into the namespaces.
 * 
 * @author jdq
 * @since 07.02.2007
 * 
 */
public class ProgramVariableCollector extends Visitor {

    public static final ProgramVariableCollector INSTANCE = new ProgramVariableCollector();

    private Set<String> names;

    private boolean found;

    /**
     * Collects all program variables that are changed in the first modality of
     * the given term
     * 
     * @param term
     * @return
     */
    public synchronized Set<String> getProgramVariables(Term term) {
        names = new HashSet<String>();
        found = false;
        term.execPreOrder(this);
        return names;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
     */
    @Override
    public void visit(Term visited) {
        if (!found && visited.op() instanceof Modality) {
            DLProgramElement childAt = (DLProgramElement) ((StatementBlock) visited
                    .javaBlock().program()).getChildAt(0);
            names.addAll(getProgramVariables(childAt));
            found = true;
        }
    }

    /**
     * @return
     */
    private Collection<String> getProgramVariables(ProgramElement form) {
        HashSet<String> result = new HashSet<String>();
        if (form instanceof Dot) {
            Dot dot = (Dot) form;
            if (dot.getChildAt(0) instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
                de.uka.ilkd.key.dl.model.ProgramVariable pv = (de.uka.ilkd.key.dl.model.ProgramVariable) dot
                        .getChildAt(0);
                result.add(pv.getElementName().toString());
            }
        } else if (form instanceof RandomAssign) {
            RandomAssign dot = (RandomAssign) form;
            if (dot.getChildAt(0) instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
                de.uka.ilkd.key.dl.model.ProgramVariable pv = (de.uka.ilkd.key.dl.model.ProgramVariable) dot
                        .getChildAt(0);

                result.add(pv.getElementName().toString());
            }
        } else if (form instanceof Assign) {
            Assign assign = (Assign) form;
            if (assign.getChildAt(0) instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
                de.uka.ilkd.key.dl.model.ProgramVariable pv = (de.uka.ilkd.key.dl.model.ProgramVariable) assign
                        .getChildAt(0);
                result.add(pv.getElementName().toString());
            }
        } else if (form instanceof DLNonTerminalProgramElement) {
            DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) form;
            for (ProgramElement p : dlnpe) {
                result.addAll(getProgramVariables(p));
            }
        }

        return result;
    }
}
