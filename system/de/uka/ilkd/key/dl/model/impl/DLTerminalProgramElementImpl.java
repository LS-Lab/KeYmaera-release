
/*
 * DLTerminalProgramElementImpl.java 1.00 Mo Jan 15 09:49:59 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.DLTerminalProgramElement;
import de.uka.ilkd.key.java.NameAbstractionTable;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.SourceData;
import de.uka.ilkd.key.java.SourceElement;
import de.uka.ilkd.key.java.reference.ExecutionContext;
import de.uka.ilkd.key.rule.MatchConditions;
import de.uka.ilkd.key.util.Debug;

/**
 * Implementation of {@link DLTerminalProgramElement}.
 * 
 * @version 1.00
 * @author jdq
 * @since Mo Jan 15 09:49:59 CET 2007
 */
public abstract class DLTerminalProgramElementImpl extends DLProgramElementImpl
        implements DLTerminalProgramElement {

    /**
     * @see de.uka.ilkd.key.dl.model.impl.DLProgramElementImpl#equalsModRenaming(de.uka.ilkd.key.java.SourceElement,
     *      de.uka.ilkd.key.java.NameAbstractionTable) equalsModRenaming
     */
    public boolean equalsModRenaming(SourceElement arg0,
            NameAbstractionTable arg1) {
        // FIXME remove this method and implement it in the subclasses
        throw new UnsupportedOperationException(
                "This method has to be implemented by the subclasses!");
    }

    /**
     * The source element matches if its the _same_ element as this one.
     * 
     * @param source
     *                the element to match
     * @param matchCond
     *                the matchcondition
     * @return matchCond if source == this
     */
    public MatchConditions match(SourceData source, MatchConditions matchCond) {
        final ProgramElement src = source.getSource();
        source.next();
        if (src == this) {
            return matchCond;
        } else {
            Debug
                    .out(
                            "Program match failed. Not same program variable (pattern, source)",
                            this, src);
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.ReuseableProgramElement#reuseSignature(de.uka.ilkd.key.java.Services,
     *      de.uka.ilkd.key.java.reference.ExecutionContext)
     */
    public String reuseSignature(Services services, ExecutionContext ec) {
        return getClass().getName();
    }
}
