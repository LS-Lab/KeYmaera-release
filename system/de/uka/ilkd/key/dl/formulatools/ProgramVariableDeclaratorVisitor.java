/**
 * File created 07.02.2007
 */
package de.uka.ilkd.key.dl.formulatools;

import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.ProgramElementName;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.LocationVariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.logic.sort.Sort;

/**
 * The ProgramVariableDeclaratorVisitor is used to insert declarations of
 * program variables into the namespaces.
 * 
 * @author jdq
 * @since 07.02.2007
 * 
 */
public class ProgramVariableDeclaratorVisitor extends Visitor {

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
     */
    @Override
    public void visit(Term visited) {
        if (visited.op() instanceof Modality) {
            declareVariables(((StatementBlock) visited.javaBlock().program())
                    .getChildAt(0), Main.getInstance().mediator().getServices()
                    .getNamespaces());
        }
    }

    /**
     * Recursivly add all variables reachable from the given element into the
     * given namespace.
     * 
     * @param element
     *                the root of the DLProgram
     * @param nss
     *                the namespace set to get the program variable namespace
     *                from.
     */
    public static void declareVariables(ProgramElement element, NamespaceSet nss) {
        if (element instanceof VariableDeclaration) {
            VariableDeclaration decl = (VariableDeclaration) element;
            for (int i = 1; i < decl.getChildCount(); i++) {
                de.uka.ilkd.key.dl.model.ProgramVariable v = (de.uka.ilkd.key.dl.model.ProgramVariable) decl
                        .getChildAt(i);
                NamespaceSet namespaces = nss;
                de.uka.ilkd.key.logic.op.ProgramVariable kv = (ProgramVariable) namespaces
                        .programVariables().lookup(v.getElementName());
                if (kv == null) {
                    kv = new LocationVariable(new ProgramElementName(v
                            .getElementName().toString()), (Sort) namespaces
                            .sorts().lookup(decl.getType().getElementName()));
                    namespaces.programVariables().add(kv);
                }
            }
        } else if (element instanceof DLNonTerminalProgramElement) {
            DLNonTerminalProgramElement ntpl = (DLNonTerminalProgramElement) element;
            for (int i = 0; i < ntpl.getChildCount(); i++) {
                declareVariables(ntpl.getChildAt(i), nss);
            }
        } 
        
    }

}
