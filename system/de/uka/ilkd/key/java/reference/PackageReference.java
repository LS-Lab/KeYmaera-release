// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//


package de.uka.ilkd.key.java.reference;

import de.uka.ilkd.key.java.JavaNonTerminalProgramElement;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.SourceElement;
import de.uka.ilkd.key.java.visitor.Visitor;
import de.uka.ilkd.key.logic.ProgramElementName;
import de.uka.ilkd.key.util.ExtList;

/**
 *  Package reference.
 *  @author <TT>AutoDoc</TT>
 */
public class PackageReference extends JavaNonTerminalProgramElement
 implements TypeReferenceInfix, PackageReferenceContainer {

    /**
     *      Prefix.
     */
    protected ReferencePrefix prefix;

    /**
     *      Name.
     */
    protected ProgramElementName name;


    /**
     * Constructor for the transformation of COMPOST ASTs to KeY.
     * @param children the children of this AST element as KeY classes.
     *  May contain: 
     *  a ProgramElementName (as the name of the method reference), 
     *  a ReferencePrefix (as accessPath to the package), 
     * 	Comments.
     */
    public PackageReference(ExtList children) {
	prefix=(PackageReference)children.get(PackageReference.class);
	name=(ProgramElementName)children.get(ProgramElementName.class);
    }

    public PackageReference(ProgramElementName name, 
			    ReferencePrefix prefix) {
	this.prefix = prefix;
	this.name = name;
    }

    public SourceElement getFirstElement() {
        return (prefix == null) ? name : prefix.getFirstElement();
    }

    /**
     *      Returns the number of children of this node.
     *      @return an int giving the number of children of this node
     */
    public int getChildCount() {
        int result = 0;
        if (prefix != null) result++;
        if (name   != null) result++;
        return result;
    }

    /**
     *      Returns the child at the specified index in this node's "virtual"
     *      child array
     *      @param index an index into this node's "virtual" child array
     *      @return the program element at the given position
     *      @exception ArrayIndexOutOfBoundsException if <tt>index</tt> is out
     *                 of bounds
     */
    public ProgramElement getChildAt(int index) {
        if (prefix != null) {
            if (index == 0) return prefix;
            index--;
        }
        if (name != null) {
            if (index == 0) return name;
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    /**
     *      Get reference prefix.
     *      @return the reference prefix.
     */
    public ReferencePrefix getReferencePrefix() {
        return prefix;
    }

    /**
     *      Get the package reference.
     *      @return the package reference.
     */
    public PackageReference getPackageReference() {
        return (prefix instanceof PackageReference) 
	    ? (PackageReference)prefix : null;
    }


    /**
     *      Get name.
     *      @return the string.
     */
    public final String getName() {
        return (name == null) ? null : name.toString();
    }

    /**
     *      Get identifier.
     *      @return the identifier.
     */
    public ProgramElementName getProgramElementName() {
        return name;
    }

    /** calls the corresponding method of a visitor in order to
     * perform some action/transformation on this element
     * @param v the Visitor
     */
    public void visit(Visitor v) {
	v.performActionOnPackageReference(this);
    }

    public void prettyPrint(PrettyPrinter p) throws java.io.IOException {
        p.printPackageReference(this);
    }

    public ReferencePrefix setReferencePrefix(ReferencePrefix r) {
	return this;
    }
}
