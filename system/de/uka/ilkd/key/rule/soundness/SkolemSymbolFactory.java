// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.rule.soundness;

import java.util.Iterator;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.collection.ImmutableSLList;
import de.uka.ilkd.key.java.JavaInfo;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.abstraction.KeYJavaType;
import de.uka.ilkd.key.logic.*;
import de.uka.ilkd.key.logic.op.IProgramVariable;


/**
 * Factory for the four kinds of skolem symbols
 * (for terms, formulas, statements and expressions)
 */
abstract class SkolemSymbolFactory {

    private final Services  services;


    private final Namespace functions = new Namespace ();
    private final Namespace variables = new Namespace ();

    SkolemSymbolFactory(Services p_services) {
        super();
	services = p_services;
    }

    protected void addVariable ( Named p ) {
    	variables.add ( p );
    }

    protected void addFunction ( Named p ) {
    	functions.add ( p );
    }

    protected void addVocabulary ( SkolemSymbolFactory p ){
    	variables.add ( p.variables );
    }

    protected ImmutableArray<IProgramVariable> getProgramVariablesAsArray
	( ImmutableList<IProgramVariable> p ) {

	int                       pvpSize = p.size ();

	IProgramVariable[]         pva     = new IProgramVariable [ pvpSize ];

	Iterator<IProgramVariable> it      = p.iterator ();

	int                       i       = 0;
	while ( it.hasNext () ) {
	    pva[i] = it.next ();
	    ++i;
	}

	return new ImmutableArray<IProgramVariable> ( pva );
    }

    protected ImmutableArray<KeYJavaType> getKeYJavaTypesAsArray
	( ImmutableList<KeYJavaType> p ) {

	int                   pvpSize = p.size ();

	KeYJavaType[]         pva     = new KeYJavaType [ pvpSize ];

	Iterator<KeYJavaType> it      = p.iterator ();

	int                   i       = 0;
	while ( it.hasNext () ) {
	    pva[i] = it.next ();
	    ++i;
	}

	return new ImmutableArray<KeYJavaType> ( pva );
    }

    public ImmutableList<IProgramVariable> getProgramVariablesAsList
	( ImmutableArray<IProgramVariable> p ) {

	ImmutableList<IProgramVariable> res = ImmutableSLList.<IProgramVariable>nil();

	int i = p.size ();
	while ( i-- != 0 )
	    res = res.prepend ( p.get ( i ) );

	return res;

    }

    protected ImmutableList<KeYJavaType> getKeYJavaTypesAsList
	( ImmutableArray<KeYJavaType> p ) {

	ImmutableList<KeYJavaType> res = ImmutableSLList.<KeYJavaType>nil();

	int i = p.size ();
	while ( i-- != 0 )
	    res = res.prepend ( p.get ( i ) );

	return res;

    }

    protected ImmutableArray<KeYJavaType> getProgramVariableTypes
        ( ImmutableList<IProgramVariable> p ) {

	int                       pvpSize  = p.size ();

	KeYJavaType[]             types    = new KeYJavaType [ pvpSize ];
	Iterator<IProgramVariable> it       = p.iterator ();

	int                       i        = 0;
	while ( it.hasNext () ) {
	    types[i] = it.next ().getKeYJavaType ();
	    ++i;
	}

	return new ImmutableArray<KeYJavaType> ( types );
	
    }

    protected TermFactory getTF() {
        return TermFactory.DEFAULT;
    }

    protected Services getServices() {
        return services;
    }

    protected JavaInfo getJavaInfo() {
        return getServices ().getJavaInfo ();
    }

    /**
     * HACK: Create a new and unique identifier
     */
    protected static Name createUniqueName(String baseName) {
        return AbstractSkolemBuilder.createUniqueName(baseName);
    }

    /**
     * HACK: Create a new and unique identifier
     */
    protected static Name createUniqueName(Name baseName) {
        return AbstractSkolemBuilder.createUniqueName(baseName);
    }

    /**
     * HACK: Create a new and unique identifier
     */
    protected static ProgramElementName createUniquePEName(String baseName) {
        return AbstractSkolemBuilder.createUniquePEName(baseName);
    }

    /**
     * HACK: Create a new and unique identifier
     */
    protected static ProgramElementName createUniquePEName(Name baseName) {
        return AbstractSkolemBuilder.createUniquePEName(baseName);
    }


    /**
     * @return a namespace of all variables (i.e. program variables)
     * that have been created as part of the skolem symbol construction
     */
    public Namespace getVariables() {
        return variables;
    }

    /**
     * @return a namespace of all function symbols
     * that have been created as part of the skolem symbol construction
     */
    public Namespace getFunctions() {
        return functions;
    }

}
