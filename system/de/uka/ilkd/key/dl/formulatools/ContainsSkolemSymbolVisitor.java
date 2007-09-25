/**
 * File created 20.02.2007
 */
package de.uka.ilkd.key.dl.formulatools;

import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.RigidFunction;

/**
 * This class is a visitor implementation that checks if a given term is a first
 * order term and contains skolem symbols.
 * 
 * @author jdq
 * @since 20.02.2007
 * 
 */
public class ContainsSkolemSymbolVisitor extends Visitor {

    private static final ContainsSkolemSymbolVisitor INSTANCE = new ContainsSkolemSymbolVisitor();

    private boolean foundSkolem = false;

    private boolean fo = true;

    private ContainsSkolemSymbolVisitor() {
    }

    public synchronized static boolean containsSkolemSymbolAndIsFO(Term form) {
        INSTANCE.fo = true;
        INSTANCE.foundSkolem = false;
        form.execPreOrder(INSTANCE);
        return INSTANCE.fo && INSTANCE.foundSkolem;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
     */
    @Override
    public void visit(Term visited) {
        if (fo) {
            if (visited.op() instanceof Modality) {
                fo = false;
            } else if ((visited.op() instanceof RigidFunction)
                    && ((RigidFunction) visited.op()).isSkolem()) {
                foundSkolem = true;
            }
        }
    }
}
