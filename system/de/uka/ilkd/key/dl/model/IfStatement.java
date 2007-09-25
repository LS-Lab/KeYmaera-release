/**
 * 
 */
package de.uka.ilkd.key.dl.model;

/**
 * The IfStatement represents if statements occurring in hybrid programs.
 * 
 * @author jdq
 * @since Jul 16, 2007
 * 
 */
public interface IfStatement extends CompoundDLProgram {

    /**
     * @return the condition on which the "then"-action should be performed
     */
    public Formula getExpression();

    /**
     * @return the action executed if the condition is evaluated to true
     */
    public DLProgram getThen();

    /**
     * @return the action executed if the condition is evaluated to false, or
     *         <code>null</code> if there is no action to be taken
     */
    public DLProgram getElse();
}
