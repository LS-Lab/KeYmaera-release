/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel                                *
 *   quesel@informatik.uni-oldenburg.de                                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
/*
 * DLNonTerminalProgramElementImpl.java 1.00 Fr Jan 12 17:10:39 CET 2007
 *
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.java.NameAbstractionTable;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.SourceElement;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import de.uka.ilkd.key.rule.MatchConditions;
import de.uka.ilkd.key.java.SourceData;
import de.uka.ilkd.key.util.Debug;
import de.uka.ilkd.key.java.NonTerminalProgramElement;
import de.uka.ilkd.key.java.PrettyPrinter;
import java.io.IOException;

/**
 * Implementation of {@link DLNonTerminalProgramElement}.
 * 
 * @version 1.00
 * @author jdq
 * @since Fr Jan 12 17:10:39 CET 2007
 */
public abstract class DLNonTerminalProgramElementImpl extends
		DLProgramElementImpl implements DLNonTerminalProgramElement {

	private List<ProgramElement> children;

	protected DLNonTerminalProgramElementImpl() {
		children = new ArrayList<ProgramElement>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.java.NonTerminalProgramElement#getChildAt(int)
	 */
	public ProgramElement getChildAt(int index) {
		return children.get(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.java.NonTerminalProgramElement#getChildCount()
	 */
	public int getChildCount() {
		return children.size();
	}

	/**
	 * Throws a RuntimeException as this operation is not supported.
	 * 
	 * @throws RuntimeException
	 *             always
	 * @see de.uka.ilkd.key.java.NonTerminalProgramElement#getChildPositionCode(de.uka.ilkd.key.java.ProgramElement)
	 */
	public int getChildPositionCode(ProgramElement child) {
		throw new UnsupportedOperationException(
				"This Operation is not supported!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.java.NonTerminalProgramElement#getIndexOfChild(de.uka.ilkd.key.java.ProgramElement)
	 */
	public int getIndexOfChild(ProgramElement child) {
		return children.indexOf(child);
	}

	/**
	 * Throws a RuntimeException as this operation is not supported.
	 * 
	 * @throws RuntimeException
	 *             always
	 * @see de.uka.ilkd.key.java.NonTerminalProgramElement#getIndexOfChild(int)
	 */
	public int getIndexOfChild(int positionCode) {
		throw new UnsupportedOperationException(
				"This Operation is not supported!");
	}

	/**
	 * Throws a RuntimeException as this operation is not supported.
	 * 
	 * @throws RuntimeException
	 *             always
	 * @see de.uka.ilkd.key.java.NonTerminalProgramElement#getRoleOfChild(int)
	 */
	public int getRoleOfChild(int positionCode) {
		throw new UnsupportedOperationException(
				"This Operation is not supported!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.java.SourceElement#equalsModRenaming(de.uka.ilkd.key.java.SourceElement,
	 *      de.uka.ilkd.key.java.NameAbstractionTable)
	 */
	public boolean equalsModRenaming(SourceElement se, NameAbstractionTable nat) {
		// throw new UnsupportedOperationException("This method has to be
		// implemented by the subclasses");
		if (this.getClass() == se.getClass()
				&& this.children.size() == ((DLNonTerminalProgramElementImpl) se).children
						.size()) {
			DLNonTerminalProgramElementImpl p = (DLNonTerminalProgramElementImpl) se;
			for (int i = 0; i < children.size(); i++) {
				if (!children.get(i).equalsModRenaming(p.children.get(i), nat)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Adds the given program element as child. The method is only used on
	 * construction time as the formulas should be immutable.
	 * 
	 * @param p
	 *            the programelement to add
	 */
	protected void addChild(ProgramElement p) {
		children.add(p);
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.DLProgramElementImpl#toString() toString
	 */
	public String toString() {
		// StringTemplate test = new StringTemplate("$name$ ($children;
		// separator=\", \"$)");
		// for(int i = 0; i < getChildCount(); i++) {
		// test.setAttribute("children", getChildAt(i));
		// }
		// test.setAttribute("name", getClass().getSimpleName());
		// return test.toString();
		return super.toString();
	}

	/**
	 * This implementation of match, uses equality of AST-Classes, thus that
	 * e.g. AndImpl and AndImpl2 would not be matched.
	 * 
	 * @see de.uka.ilkd.key.dl.model.impl.DLProgramElementImpl#match(de.uka.ilkd.key.java.SourceData,
	 *      de.uka.ilkd.key.rule.MatchConditions) match
	 */
	public MatchConditions match(SourceData source, MatchConditions matchCond) {
		final ProgramElement src = source.getSource();

		Debug.out("Program match start (template, source)", this, src);

		if (src == null) {
			return null;
		}

		if (src.getClass() != this.getClass()) {
			Debug.out("Incompatible AST nodes (template, source)", this, src);
			return null;
		}

		final NonTerminalProgramElement ntSrc = (NonTerminalProgramElement) src;
		final SourceData newSource = new SourceData(ntSrc, 0, source
				.getServices());

		matchCond = matchChildren(newSource, matchCond, 0);

		if (matchCond == null) {
			return null;
		}

		source.next();
		return matchCond;

	}

	/**
	 * matches successively all children of this current node. Thereby the
	 * <tt>offset</tt>-th child is matched against
	 * <code>source.getSource()</code>. The call <tt>source.next</tt> has
	 * to be done in the
	 * 
	 * @link ProgramElement#match method of the currently matched child in case
	 *       of a successful match. This is <em>not</em> done here (rationale:
	 *       schemavariables matching on lists can be implemented easy).
	 * 
	 * 
	 * @param source
	 *            the SourceData with the children to be matched
	 * @param matchCond
	 *            the MatchConditions found so far
	 * @param offset
	 *            the int denoting the index of the child to start with
	 * @return the resulting match conditions or <tt>null</tt> if matching
	 *         failed
	 */
	protected MatchConditions matchChildren(SourceData source,
			MatchConditions matchCond, int offset) {

		for (int i = offset, sz = getChildCount(); i < sz; i++) {
			matchCond = getChildAt(i).match(source, matchCond);
			if (matchCond == null) {
				return null;
			}
		}

		final NonTerminalProgramElement ntSrc = (NonTerminalProgramElement) source
				.getElement();

		if (!compatibleBlockSize(source.getChildPos(), ntSrc.getChildCount())) {
			Debug.out("Source has unmatched elements.");
			return null;
		}

		return matchCond;
	}

	/**
	 * used by
	 * 
	 * @link matchChildren to decide if a found match is valid or if there are
	 *       remaining source elements that have not been matched (in which case
	 *       the match failed)
	 */
	protected boolean compatibleBlockSize(int pos, int max) {
		return pos >= max;
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.DLProgramElementImpl#getFirstElement()
	 *      getFirstElement
	 */
	public SourceElement getFirstElement() {
		return getChildAt(0);
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.DLProgramElementImpl#getLastElement()
	 *      getLastElement
	 */
	public SourceElement getLastElement() {
		return getChildAt(getChildCount());
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.DLProgramElementImpl#prettyPrint(de.uka.ilkd.key.java.PrettyPrinter)
	 *      prettyPrint
	 */
	public void prettyPrint(PrettyPrinter arg0) throws IOException {
		arg0.printDLNonTerminalProgramElement(this);
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement#getSymbol() getSymbol
	 */
	public String getSymbol() {
		return getClass().getSimpleName();
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement#printInfix()
	 *      printInfix
	 */
	public boolean printInfix() {
		return false;
	}

    /**
     * @directed
     * @label provides 
     */
    private DLNonTerminalProgramElementIteratorImpl lnkDLNonTerminalProgramElementIteratorImpl;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<ProgramElement> iterator() {
		return new DLNonTerminalProgramElementIteratorImpl(this);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
	    if(obj != null && obj instanceof DLNonTerminalProgramElement) {
	        DLNonTerminalProgramElement dn = (DLNonTerminalProgramElement) obj;
	        if(dn.getChildCount() == this.getChildCount()) {
	            for(int i = 0; i < getChildCount(); i++) {
	                if(!dn.getChildAt(i).equals(getChildAt(i))) {
	                    return false;
	                }
	            }
	            return true;
	        }
	    }
	    return false;
	}
}
