/**
 * File created 26.01.2007
 */
package de.uka.ilkd.key.dl.model.impl;

import java.util.Iterator;

import de.uka.ilkd.key.java.ProgramElement;

/**
 * This iterator can iterate over ProgramElements. It is used by the
 * {@link DLNonTerminalProgramElementImpl}.
 * 
 * @author jdq
 * @since 26.01.2007
 * 
 */
public class DLNonTerminalProgramElementIteratorImpl implements
		Iterator<ProgramElement> {

	private int i = 0;

	private DLNonTerminalProgramElementImpl impl;

	/**
	 * @param impl
	 */
	public DLNonTerminalProgramElementIteratorImpl(
			DLNonTerminalProgramElementImpl impl) {
		this.impl = impl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return impl.getChildCount() - i > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#next()
	 */
	public ProgramElement next() {
		return impl.getChildAt(i++);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException("Datastructure is immutable!");
	}

}
