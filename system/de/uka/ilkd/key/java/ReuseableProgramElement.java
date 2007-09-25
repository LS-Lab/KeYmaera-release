/**
 * 
 */
package de.uka.ilkd.key.java;

import de.uka.ilkd.key.java.reference.ExecutionContext;

/**
 * TODO jdq documentation since Aug 28, 2007 
 * @author jdq
 * @since Aug 28, 2007
 * 
 */
public interface ReuseableProgramElement {

    /** this is the default implementation of the signature, which is
     *  used to determine program similarity.
     * @param ec TODO
     */
    public abstract String reuseSignature(Services services, ExecutionContext ec);

}