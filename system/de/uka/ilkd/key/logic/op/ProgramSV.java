// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.logic.op;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.dl.model.DLBottom;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.java.Comment;
import de.uka.ilkd.key.java.Expression;
import de.uka.ilkd.key.java.LoopInitializer;
import de.uka.ilkd.key.java.Position;
import de.uka.ilkd.key.java.PositionInfo;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.SourceData;
import de.uka.ilkd.key.java.SourceElement;
import de.uka.ilkd.key.java.Statement;
import de.uka.ilkd.key.java.abstraction.KeYJavaType;
import de.uka.ilkd.key.java.reference.ExecutionContext;
import de.uka.ilkd.key.java.reference.PackageReference;
import de.uka.ilkd.key.java.reference.ReferencePrefix;
import de.uka.ilkd.key.java.reference.TypeReference;
import de.uka.ilkd.key.java.visitor.Visitor;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.ProgramConstruct;
import de.uka.ilkd.key.logic.ProgramElementName;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.sort.PlaceHolderSort;
import de.uka.ilkd.key.logic.sort.ProgramSVSort;
import de.uka.ilkd.key.logic.sort.ProgramSVSort.DLVariableDeclarationSort;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.MatchConditions;
import de.uka.ilkd.key.rule.inst.ProgramList;
import de.uka.ilkd.key.rule.inst.SVInstantiations;
import de.uka.ilkd.key.util.Debug;

public class ProgramSV extends SortedSchemaVariable implements
        ProgramConstruct, DLBottom {

    private static final ProgramList EMPTY_LIST_INSTANTIATION = 
        new ProgramList
        (new ImmutableArray<ProgramElement>(new ProgramElement[0]));

    /**
     * creates a new SchemaVariable used as a placeholder for program constructs
     * 
     * @param name
     *                the Name of the SchemaVariable
     * @param listSV
     *                a boolean which is true iff the schemavariable is allowed
     *                to match a list of program constructs
     */
    ProgramSV(Name name, ProgramSVSort s, boolean listSV) {
        super(name, ProgramConstruct.class, s, listSV);
    }

    /**
     * returns true iff this SchemaVariable is used to match part of a program
     * 
     * @return true iff this SchemaVariable is used to match part of a program
     */
    public boolean isProgramSV() {
        return true;
    }

    /**
     * @return comments if the schemavariable stands for programm construct and
     *         has comments attached to it (not supported yet)
     */
    public Comment[] getComments() {
        return new Comment[0];
    }

    public SourceElement getFirstElement() {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.SourceElement#getLastElement()
     */
    public SourceElement getLastElement() {
        return this;
    }

    /**
     * Returns the start position of the primary token of this element.
     * To get the start position of the syntactical first token,
     * call the corresponding method of <CODE>getFirstElement()</CODE>.
     * @return the start position of the primary token.
     */
    public Position getStartPosition() {
        return Position.UNDEFINED;
    }

    /**
     * Returns the end position of the primary token of this element. To get the
     * end position of the syntactical first token, call the corresponding
     * method of <CODE>getLastElement()</CODE>.
     * 
     * @return the end position of the primary token.
     */
    public Position getEndPosition() {
        return Position.UNDEFINED;
    }

    /**
     * Returns the relative position (number of blank heading lines and columns)
     * of the primary token of this element. To get the relative position of the
     * syntactical first token, call the corresponding method of
     * <CODE>getFirstElement()</CODE>.
     * 
     * @return the relative position of the primary token.
     */
    public Position getRelativePosition() {
        return Position.UNDEFINED;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.SourceElement#getPositionInfo()
     */
    public PositionInfo getPositionInfo() {
        return PositionInfo.UNDEFINED;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.reference.TypeReference#getReferencePrefix()
     */
    public ReferencePrefix getReferencePrefix() {
        return null;
    }

    /**
     * @param r
     * @return
     */
    public ReferencePrefix setReferencePrefix(ReferencePrefix r) {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.reference.TypeReference#getDimensions()
     */
    public int getDimensions() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.reference.TypeReferenceContainer#getTypeReferenceCount()
     */
    public int getTypeReferenceCount() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.reference.TypeReferenceContainer#getTypeReferenceAt(int)
     */
    public TypeReference getTypeReferenceAt(int index) {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.reference.PackageReferenceContainer#getPackageReference()
     */
    public PackageReference getPackageReference() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.ExpressionContainer#getExpressionCount()
     */
    public int getExpressionCount() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.statement.IForUpdates#getExpressionAt(int)
     */
    public Expression getExpressionAt(int index) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.NonTerminalProgramElement#getChildCount()
     */
    public int getChildCount() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.NonTerminalProgramElement#getChildAt(int)
     */
    public ProgramElement getChildAt(int index) {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.NonTerminalProgramElement#getIndexOfChild(de.uka.ilkd.key.java.ProgramElement)
     */
    public int getIndexOfChild(ProgramElement child) {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.NonTerminalProgramElement#getChildPositionCode(de.uka.ilkd.key.java.ProgramElement)
     */
    public int getChildPositionCode(ProgramElement child) {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.NonTerminalProgramElement#getIndexOfChild(int)
     */
    public int getIndexOfChild(int positionCode) {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.NonTerminalProgramElement#getRoleOfChild(int)
     */
    public int getRoleOfChild(int positionCode) {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.StatementContainer#getStatementCount()
     */
    public int getStatementCount() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.statement.ILoopInit#size()
     */
    public int size() {
        return 0;
    }

    public ImmutableArray<Expression> getUpdates() {
        return null;
    }

    public ImmutableArray<LoopInitializer> getInits() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.StatementContainer#getStatementAt(int)
     */
    public Statement getStatementAt(int i) {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.reference.TypeReference#getProgramElementName()
     */
    public ProgramElementName getProgramElementName() {
        return new ProgramElementName(toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.reference.TypeReference#getName()
     */
    public String getName() {
        return name().toString();
    }

    /**
     * calls the corresponding method of a visitor in order to perform some
     * action/transformation on this element
     * 
     * @param v
     *                the Visitor
     */
    public void visit(Visitor v) {
        v.performActionOnSchemaVariable(this);
    }

    /**
     * this pretty printer method is for the program pretty printer and needs
     * not to be overwritten by ProgramSV but at the moment it is not
     */
    public void prettyPrint(PrettyPrinter w) throws IOException {
        w.printSchemaVariable(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.reference.TypeReference#getKeYJavaType()
     */
    public KeYJavaType getKeYJavaType() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.IProgramVariable#getKeYJavaType(de.uka.ilkd.key.java.Services)
     */
    public KeYJavaType getKeYJavaType(Services javaServ) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.Expression#getKeYJavaType(de.uka.ilkd.key.java.Services,
     *      de.uka.ilkd.key.java.reference.ExecutionContext)
     */
    public KeYJavaType getKeYJavaType(Services javaServ, ExecutionContext ec) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.Operator#match(de.uka.ilkd.key.logic.op.Operator,
     *      de.uka.ilkd.key.rule.MatchConditions, de.uka.ilkd.key.java.Services)
     */
    public MatchConditions match(SVSubstitute substitute, MatchConditions mc,
            Services services) {

        final ProgramSVSort svSort = (ProgramSVSort) sort();

        if (substitute instanceof Term && svSort.canStandFor((Term) substitute)) {
            return addInstantiation((Term) substitute, mc, services);
        } else if (substitute instanceof ProgramElement
                && svSort.canStandFor((ProgramElement) substitute, mc
                        .getInstantiations().getExecutionContext(), services)) {
            return addInstantiation((ProgramElement) substitute, mc, services);
        }
        Debug.out("FAILED. Cannot match ProgramSV with given "
                + "instantiation(template, orig)", this, substitute);
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.SchemaVariableAdapter#toString()
     */
    public String toString() {
        return toString("program " + sort().name());
    }

    /**
     * adds a found mapping from schema variable <code>var</code> to program
     * element <code>pe</code> and returns the updated match conditions or
     * null if mapping is not possible because of violating some variable
     * condition
     * 
     * @param pe
     *                the ProgramElement <code>var</code> is mapped to
     * @param matchCond
     *                the MatchConditions to be updated
     * @param services
     *                the Services provide access to the Java model x *
     * @return the updated match conditions including mapping <code>var</code>
     *         to <code>pe</code> or null if some variable condition would be
     *         hurt by the mapping
     */
    private MatchConditions addProgramInstantiation(ProgramElement pe,
            MatchConditions matchCond, Services services) {
        if (matchCond == null) {
            return null;
        }

        SVInstantiations insts = matchCond.getInstantiations();

        final Object foundInst = insts.getInstantiation(this);

        if (foundInst != null) {
            final Object newInst;
            if (foundInst instanceof Term) {
                newInst = services.getTypeConverter().convertToLogicElement(pe,
                        insts.getExecutionContext());
            } else {
                newInst = pe;
            }

            if (foundInst.equals(newInst)) {
                return matchCond;
            } else {
                return null;
            }
        }

        insts = insts.add(this, pe);
        return insts == null ? null : matchCond.setInstantiations(insts);
    }

    /**
     * adds a found mapping from schema variable <code>var</code> to the list
     * of program elements <code>list</code> and returns the updated match
     * conditions or null if mapping is not possible because of violating some
     * variable condition
     * 
     * @param list
     *                the ProgramList <code>var</code> is mapped to
     * @param matchCond
     *                the MatchConditions to be updated
     * @param services
     *                the Services provide access to the Java model
     * @return the updated match conditions including mapping <code>var</code>
     *         to <code>list</code> or null if some variable condition would
     *         be hurt by the mapping
     */
    private MatchConditions addProgramInstantiation(ProgramList list,
            MatchConditions matchCond, Services services) {
        if (matchCond == null) {
            return null;
        }

        SVInstantiations insts = matchCond.getInstantiations();
        final ProgramList pl = (ProgramList) insts.getInstantiation(this);
        if (pl != null) {
            if (pl.equals(list)) {
                return matchCond;
            } else {
                return null;
            }
        }

        insts = insts.add(this, list);
        return insts == null ? null : matchCond.setInstantiations(insts);
    }

    /**
     * returns true, if the given SchemaVariable can stand for the
     * ProgramElement
     * 
     * @param match
     *                the ProgramElement to be matched
     * @param services
     *                the Services object encapsulating information about the
     *                java datastructures like (static)types etc.
     * @return true if the SchemaVariable can stand for the given element
     */
    private boolean check(ProgramElement match, ExecutionContext ec,
            Services services) {
        if (match == null) {
            return false;
        }
        if (super.sort() instanceof PlaceHolderSort) {
            return ((ProgramSVSort) super.sort()).canStandFor(match, ec,
                    services);
        }
        return ((ProgramSVSort) sort()).canStandFor(match, ec, services);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.ProgramElement#match(de.uka.ilkd.key.java.SourceData,
     *      de.uka.ilkd.key.rule.MatchConditions)
     */
    public MatchConditions match(SourceData source, MatchConditions matchCond) {
        if (isListSV()) {
            return matchListSV(source, matchCond);
        }
        final Services services = source.getServices();
        final ProgramElement src = source.getSource();
        Debug.out("Program match start (template, source)", this, src);

        final SVInstantiations instantiations = matchCond.getInstantiations();

        final ExecutionContext ec = instantiations.getExecutionContext();

        if (!check(src, ec, services)) {
            Debug.out("taclet: MATCH FAILED. Sort of SchemaVariable cannot "
                    + "stand for the program");
            return null; // FAILED
        }

        final Object instant = instantiations.getInstantiation(this);
        if (instant == null
                || instant.equals(src)
                || (instant instanceof Term && ((Term) instant).op()
                        .equals(src))) {

            matchCond = addProgramInstantiation(src, matchCond, services);

            if (matchCond == null) {
                // FAILED due to incompatibility with already found matchings
                // (e.g. generic sorts)
                return null;
            }
        } else {
            Debug.out("taclet: MATCH FAILED 3. Former match of "
                    + " SchemaVariable incompatible with "
                    + " the current match.");
            return null; // FAILED mismatch
        }
        source.next();
        return matchCond;
    }

    private MatchConditions matchListSV(SourceData source,
            MatchConditions matchCond) {
        final Services services = source.getServices();
        ProgramElement src = source.getSource();

        if (src == null) {
            return addProgramInstantiation(EMPTY_LIST_INSTANTIATION, matchCond,
                    services);
        }

        SVInstantiations instantiations = matchCond.getInstantiations();
        
        final ExecutionContext ec = instantiations.getExecutionContext();        
        
        final java.util.ArrayList<ProgramElement> matchedElements = 
            new java.util.ArrayList<ProgramElement>();        

        while (src != null) {
            if (!check(src, ec, services)) {
                Debug.out("taclet: Stopped list matching because of "
                        + "incompatible elements", this, src);
                break;
            }
            matchedElements.add(src);
            source.next();
            src = source.getSource();
        }

        Debug.out("Program list match: ", this, matchedElements);
        return addProgramInstantiation(new ProgramList(new ImmutableArray<ProgramElement>(matchedElements)), 
                matchCond, services);
    }	

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.NamedElement#getElementName()
     */
    public Name getElementName() {
        return new Name(getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.DLNonTerminalProgramElement#getSymbol()
     */
    public String getSymbol() {
        throw new UnsupportedOperationException(
                "This Operation is not supported!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.DLNonTerminalProgramElement#printInfix()
     */
    public boolean printInfix() {
        throw new UnsupportedOperationException(
                "This Operation is not supported!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<ProgramElement> iterator() {
        throw new UnsupportedOperationException(
                "This Operation is not supported!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.TermSymbol#sort(de.uka.ilkd.key.logic.Term[])
     */
    /*@Override*/
    public Sort sort(Term[] term) {
        Sort sort = super.sort(term);
        if (sort instanceof PlaceHolderSort) {
            return ((PlaceHolderSort) sort).getRealSort(term);
        }
        return sort;
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

    /*@Override*/
    public List<ProgramElement> getDifferentialEquations(NamespaceSet nss) {
        throw new UnsupportedOperationException();
    }

    /*@Override*/
    public Term getInvariant(Services services) {
        throw new UnsupportedOperationException();
    }

    /*@Override*/
    public boolean isDifferentialEquation(ProgramElement el) {
        throw new UnsupportedOperationException();
    }

    /*@Override*/
    public List<Formula> getDLAnnotation(String key) {
        throw new UnsupportedOperationException();
    }

    /*@Override*/
    public Map<String, List<Formula>> getDLAnnotations() {
        throw new UnsupportedOperationException();
    }

    /*@Override*/
    public void setDLAnnotations(Map<String, List<Formula>> annotations) {
        throw new UnsupportedOperationException();
    }
    /*@Override*/
    public void setDLAnnotation(String key, List<Formula> value) {
        throw new UnsupportedOperationException();
    }

    /*@Override*/
    public DiffSystem getDifferentialFragment() {
        throw new UnsupportedOperationException();
    }

    /*@Override*/
    public DiffSystem getInvariantFragment() {
        throw new UnsupportedOperationException();
    }

	/*@Override*/
	public boolean containsDLAnnotation(String key) {
        throw new UnsupportedOperationException();
	}
}
