/***************************************************************************
 *   Copyright (C) 2007 by Jan-David Quesel                                *
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
 * DLProgramElementImpl.java 1.00 Fr Jan 12 16:41:42 CET 2007
 *
 */

package de.uka.ilkd.key.dl.model.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.java.Comment;
import de.uka.ilkd.key.java.Position;
import de.uka.ilkd.key.java.PositionInfo;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.SourceData;
import de.uka.ilkd.key.java.SourceElement;
import de.uka.ilkd.key.java.visitor.Visitor;
import de.uka.ilkd.key.rule.MatchConditions;

/**
 * Implementation of {@link DLProgramElement}.
 * 
 * @version 1.00
 * @author jdq
 * @since Fr Jan 12 16:41:42 CET 2007
 */
public abstract class DLProgramElementImpl implements DLProgramElement {

    private Map<String, List<Formula>> annotations;
    
    public DLProgramElementImpl() {
        annotations = new HashMap<String, List<Formula>>();
    }
    
    /* (non-Javadoc)
     * @see de.uka.ilkd.key.dl.model.DLProgramElement#setDLAnotation(java.lang.String, java.lang.String)
     */
    /*@Override*/
    public void setDLAnnotation(String key, List<Formula> value) {
        annotations.put(key, value);
    }
    
    /* (non-Javadoc)
     * @see de.uka.ilkd.key.dl.model.DLProgramElement#getDLAnotation(java.lang.String)
     */
    /*@Override*/
    public List<Formula> getDLAnnotation(String key) {
        return annotations.get(key);
    }
    
    /* (non-Javadoc)
     * @see de.uka.ilkd.key.dl.model.DLProgramElement#getDLAnotation(java.lang.String)
     */
    /*@Override*/
    public boolean containsDLAnnotation(String key) {
        return annotations.containsKey(key);
    }

    /* (non-Javadoc)
     * @see de.uka.ilkd.key.dl.model.DLProgramElement#getDLAnnotations()
     */
    /*@Override*/
    public Map<String, List<Formula>> getDLAnnotations() {
        return annotations;
    }
    /*@Override*/
    public void setDLAnnotations(Map<String, List<Formula>> annotations) {
        this.annotations.clear();
        this.annotations.putAll(annotations);
    }

    /**
     * @return an empty array
     * @see de.uka.ilkd.key.dl.model.DLProgramElement#getComments() getComments
     */
    public de.uka.ilkd.key.java.Comment[] getComments() {
        return new Comment[0];
    }

    /**
     * @return undefined position
     * @see de.uka.ilkd.key.dl.model.DLProgramElement#getEndPosition()
     *      getEndPosition
     */
    public Position getEndPosition() {
        return Position.UNDEFINED;
    }

    /**
     * @return this
     * @see de.uka.ilkd.key.dl.model.DLProgramElement#getFirstElement()
     *      getFirstElement
     */
    public SourceElement getFirstElement() {
        return this;
    }

    /**
     * @return this
     * @see de.uka.ilkd.key.dl.model.DLProgramElement#getLastElement()
     *      getLastElement
     */
    public SourceElement getLastElement() {
        return this;
    }

    /**
     * @return postition undefined
     * @see de.uka.ilkd.key.dl.model.DLProgramElement#getPositionInfo()
     *      getPositionInfo
     */
    public PositionInfo getPositionInfo() {
        return PositionInfo.UNDEFINED;
    }

    /**
     * Returns Position.UNDEFINED
     * 
     * @see de.uka.ilkd.key.dl.model.DLProgramElement#getRelativePosition()
     *      getRelativePosition
     */
    public Position getRelativePosition() {
        return Position.UNDEFINED;
    }

    /**
     * @see de.uka.ilkd.key.dl.model.DLProgramElement#getStartPosition()
     *      getStartPosition
     */
    public Position getStartPosition() {
        return Position.UNDEFINED;
    }

    /**
     * @return null
     * @see de.uka.ilkd.key.dl.model.DLProgramElement#match(de.uka.ilkd.key.java.SourceData,
     *      de.uka.ilkd.key.rule.MatchConditions) match
     */
    public MatchConditions match(SourceData arg0, MatchConditions arg1) {
        return null;
    }

    /**
     * @see de.uka.ilkd.key.dl.model.DLProgramElement#prettyPrint(de.uka.ilkd.key.java.PrettyPrinter)
     *      prettyPrint
     */
    public void prettyPrint(PrettyPrinter arg0) throws IOException {
        arg0.printDLProgramElement(this);
        // throw new IOException("This method has to be implemented by the
        // subclasses!");
    }

    /**
     * @see de.uka.ilkd.key.dl.model.DLProgramElement#visit(de.uka.ilkd.key.java.visitor.Visitor)
     *      visit
     */
    public void visit(Visitor arg0) {
    }

    /**
     * @see java.lang.Object#toString() toString
     */
    public String toString() {
    	StringWriter writer = new StringWriter();
    	try {
			prettyPrint(new PrettyPrinter(writer));
			return writer.toString();
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	Class cla = getClass();
        return cla.getSimpleName();
    }
}
