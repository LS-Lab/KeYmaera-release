/**
 * File created 28.02.2007
 */
package de.uka.ilkd.key.dl.model.impl;

import java.io.IOException;
import java.util.List;

import de.uka.ilkd.key.dl.formulatools.VariableDeclaration;
import de.uka.ilkd.key.dl.model.Variable;
import de.uka.ilkd.key.dl.model.VariableType;
import de.uka.ilkd.key.java.NameAbstractionTable;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.ReuseableProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.SourceElement;
import de.uka.ilkd.key.java.reference.ExecutionContext;

/**
 * This class is the implementation of the representation of variable
 * declarations in dL. {@link VariableDeclaration}
 * 
 * @author jdq
 * @since 28.02.2007
 * 
 */
public class VariableDeclarationImpl extends DLNonTerminalProgramElementImpl
        implements VariableDeclaration {

    /**
     * @param variable
     */
    public VariableDeclarationImpl(VariableType type, List<Variable> variables) {
        addChild(type);
        for (Variable variable : variables) {
            addChild(variable);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.model.impl.DLNonTerminalProgramElementImpl#prettyPrint(de.uka.ilkd.key.java.PrettyPrinter)
     */
    @Override
    public void prettyPrint(PrettyPrinter arg0) throws IOException {
        arg0.printDLVariableDeclaration(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.VariableDeclaration#getType()
     */
    public VariableType getType() {
        return (VariableType) getChildAt(0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.model.impl.DLNonTerminalProgramElementImpl#equalsModRenaming(de.uka.ilkd.key.java.SourceElement,
     *      de.uka.ilkd.key.java.NameAbstractionTable)
     */
    @Override
    public boolean equalsModRenaming(SourceElement se, NameAbstractionTable nat) {
        if (se instanceof VariableDeclaration) {
            VariableDeclaration decl = (VariableDeclaration) se;
            if(getChildCount() != decl.getChildCount()) {
                return false;
            }
            for (int i = 0; i < getChildCount(); i++) {
                if (!decl.getChildAt(i).equalsModRenaming(getChildAt(i), nat)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.ReuseableProgramElement#reuseSignature(de.uka.ilkd.key.java.Services,
     *      de.uka.ilkd.key.java.reference.ExecutionContext)
     */
    public String reuseSignature(Services services, ExecutionContext ec) {
        StringBuilder builder = new StringBuilder();
        String space ="";
        for(ProgramElement pe: this) {
            if(pe instanceof ReuseableProgramElement) {
                builder.append(space + ((ReuseableProgramElement)pe).reuseSignature(services, ec));
                space = " ";
            }
        }
        return builder.toString();
    }

}
