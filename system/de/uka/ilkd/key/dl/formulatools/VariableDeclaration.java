/**
 * 
 */
package de.uka.ilkd.key.dl.formulatools;

import de.uka.ilkd.key.dl.model.ElementaryDLProgram;
import de.uka.ilkd.key.dl.model.VariableType;

/**
 * Representation of a variable declaration in an hybrid program
 * 
 * @author jdq
 * @since Jul 16, 2007
 * 
 */
public interface VariableDeclaration extends ElementaryDLProgram {

    /**
     * @return
     */
    VariableType getType();

}
