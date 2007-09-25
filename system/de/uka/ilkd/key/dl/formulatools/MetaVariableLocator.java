/**
 * 
 */
package de.uka.ilkd.key.dl.formulatools;

import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.MetaVariable;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.SetAsListOfMetavariable;
import de.uka.ilkd.key.logic.op.SetOfMetavariable;

/**
 * TODO jdq documentation since Aug 28, 2007
 * 
 * @author jdq
 * @since Aug 28, 2007
 * 
 */
public class MetaVariableLocator extends Visitor {

    public static final MetaVariableLocator INSTANCE = new MetaVariableLocator();

    private SetOfMetavariable result;

    /**
     * TODO jdq documentation since Aug 28, 2007
     * 
     * @param dominantTerm
     * @return
     */
    public synchronized SetOfMetavariable find(Term dominantTerm) {
        result = SetAsListOfMetavariable.EMPTY_SET;
        dominantTerm.execPreOrder(this);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
     */
    @Override
    public void visit(Term visited) {
        if (visited.op() instanceof Modality) {
            result = result
                    .union(findInsideModality((ProgramElement) ((StatementBlock) visited
                            .javaBlock().program()).getChildAt(0)));
        }

    }

    /**
     * TODO jdq documentation since Aug 28, 2007
     * 
     * @param programElement
     * @return
     */
    private SetOfMetavariable findInsideModality(ProgramElement programElement) {
        SetOfMetavariable result = SetAsListOfMetavariable.EMPTY_SET;
        if (programElement instanceof DLNonTerminalProgramElement) {
            DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) programElement;
            for (ProgramElement p : dlnpe) {
                result = result.union(findInsideModality(p));
            }
        } else if (programElement instanceof MetaVariable) {
            MetaVariable var = (MetaVariable) programElement;
            result = result.add((Metavariable) Main.getInstance().mediator()
                    .getServices().getNamespaces().variables().lookup(
                            var.getElementName()));
        }
        return result;
    }

}
