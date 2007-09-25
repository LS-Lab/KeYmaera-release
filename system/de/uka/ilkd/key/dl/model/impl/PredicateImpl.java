/*
 * PredicateImpl.java 1.00 Mo Jan 15 09:18:59 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.Predicate;
import de.uka.ilkd.key.java.NameAbstractionTable;
import de.uka.ilkd.key.java.SourceElement;

/**
 * Implementation of {@link Predicate}
 * 
 * @version 1.00
 * @author jdq
 * @since Mo Jan 15 09:18:59 CET 2007
 */
public abstract class PredicateImpl extends DLTerminalProgramElementImpl
		implements Predicate {

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.model.impl.DLTerminalProgramElementImpl#equalsModRenaming(de.uka.ilkd.key.java.SourceElement,
	 *      de.uka.ilkd.key.java.NameAbstractionTable)
	 */
	@Override
	public boolean equalsModRenaming(SourceElement arg0,
			NameAbstractionTable arg1) {
		return arg0 == this
				|| (getClass() == arg0.getClass() && arg1.sameAbstractName(
						this, arg0));
	}
}
