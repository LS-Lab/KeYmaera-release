/*
 * DLProgramElementImpl.java 1.00 Fr Jan 12 16:41:42 CET 2007
 *
 */

package de.uka.ilkd.key.dl.model.impl;

import java.io.IOException;

import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.java.Comment;
import de.uka.ilkd.key.java.Position;
import de.uka.ilkd.key.java.PositionInfo;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.SourceData;
import de.uka.ilkd.key.java.SourceElement;
import de.uka.ilkd.key.java.annotation.Annotation;
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

    /**
     * @return 0 (no annotation in dL)
     * @see de.uka.ilkd.key.dl.model.DLProgramElement#getAnnotationCount()
     *      getAnnotationCount
     */
    public int getAnnotationCount() {
        return 0;
    }

    /**
     * @return an empty array
     * @see de.uka.ilkd.key.dl.model.DLProgramElement#getAnnotations()
     *      getAnnotations
     */
    public de.uka.ilkd.key.java.annotation.Annotation[] getAnnotations() {
        return new Annotation[0];
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
        Class cla = getClass();
        return cla.getSimpleName();
    }
}
