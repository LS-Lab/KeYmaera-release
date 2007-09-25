/**
 * 
 */
package de.uka.ilkd.key.dl.model.impl;

import java.io.IOException;

import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.IfStatement;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.reference.ExecutionContext;

/**
 * This class is the implementation of the representation of an if statement in
 * dL. {@link IfStatement}
 * 
 * @author jdq
 * @since Jul 16, 2007
 * 
 */
public class IfStatementImpl extends CompoundDLProgramImpl implements
        IfStatement {

    public IfStatementImpl(Formula expr, DLProgram then) {
        this(expr, then, null);
    }

    public IfStatementImpl(Formula expr, DLProgram then, DLProgram else_) {
        addChild(expr);
        addChild(then);
        if (else_ != null) {
            addChild(else_);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.model.impl.DLNonTerminalProgramElementImpl#prettyPrint(de.uka.ilkd.key.java.PrettyPrinter)
     */
    @Override
    public void prettyPrint(PrettyPrinter arg0) throws IOException {
        arg0.printDLIf(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IfStatement#getExpression()
     */
    public Formula getExpression() {
        return (Formula) getChildAt(0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IfStatement#getThen()
     */
    public DLProgram getThen() {
        return (DLProgram) getChildAt(1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IfStatement#getElse()
     */
    public DLProgram getElse() {
        if (getChildCount() == 3) {
            return (DLProgram) getChildAt(2);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.model.impl.CompoundDLProgramImpl#reuseSignature(de.uka.ilkd.key.java.Services,
     *      de.uka.ilkd.key.java.reference.ExecutionContext)
     */
    @Override
    public String reuseSignature(Services services, ExecutionContext ec) {
        return "if("
                + getExpression().reuseSignature(services, ec)
                + ")"
                + " then "
                + getThen().reuseSignature(services, ec)
                + ((getElse() != null) ? " else "
                        + getElse().reuseSignature(services, ec) : "") + " fi";
    }
}
