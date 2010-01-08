// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//
package de.uka.ilkd.key.smt.taclettranslation;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.collection.ImmutableSet;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.recoderext.ImplicitFieldAdder;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.op.ProgramSV;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.sort.GenericSort;
import de.uka.ilkd.key.logic.sort.ObjectSort;
import de.uka.ilkd.key.logic.sort.ProgramSVSort;
import de.uka.ilkd.key.logic.sort.Sort;

/**
 * Translates program schema variables, that do not belong to any 
 * objects:
 * Supported: see <code>isSupportedProgramSV</code>.
 *
 */
class ProgramSVTranslator {
    
    public Collection<Term> translate(Term term,Sort [] instSorts
	    ,Services services,TacletConditions conditions) 
	    throws IllegalTacletException{
	
	Collection<Term> result = 
	    instantiateVariables(term,instSorts,services,conditions);
	
	
	return result;
    }
    
    
    private Collection<Term> instantiateVariables(Term term,
	    Sort [] instSorts, Services services,TacletConditions conditions)
	    throws IllegalTacletException{
	Collection<Term> result = new LinkedList<Term>();
	
	Term[] programSVTable = collectProgramSV(term);
	if(programSVTable.length == 0){return result;}
	byte[][] refTable = AbstractTacletTranslator.generateReferenceTable(
	        instSorts.length, programSVTable.length);
	Sort [] programSVSorts = new Sort[programSVTable.length];
	for(int i=0; i < programSVTable.length; i++){
	    programSVSorts[i] = ((ProgramSV)programSVTable[i].op()).sort();
	}
	
	AbstractTacletTranslator.checkTable(refTable,instSorts,programSVSorts,conditions);
	for (int r = 0; r < refTable.length; r++) {
	    	    
	    if(refTable[r][0]!=-1){
		Term temp = instantiateVariables(term,refTable[r],programSVTable, instSorts,services);
		if(temp != null){
		    result.add(temp);
		}
	    }
	    
	}
	
	
	return result;
	
    }
    
    
    /**
     * @param term
     * @param bs
     * @param instSorts
     * @return
     * @throws IllegalTacletException 
     */
    private Term instantiateVariables(Term term, byte[] refTable,Term []programSV, Sort[] instSorts,
	    Services services) throws IllegalTacletException {
	Term[] subTerms = new Term[term.arity()];
	ImmutableArray<QuantifiableVariable> variables[] = new ImmutableArray[term.arity()];
	for (int i = 0; i < term.arity(); i++) {
	    variables[i] = term.varsBoundHere(i);
	    subTerms[i] = instantiateVariables(term.sub(i),refTable,programSV,instSorts,services);
	    if(subTerms[i]== null) return null;
	} 
	
	if(isSupportedProgramsSV(term)){
	    ProgramSV sv = (ProgramSV)term.op();
	    Sort sort = null;
	    for(int c=0; c < programSV.length; c++){
		if(programSV[c].equals(term)){
		    sort = instSorts[refTable[c]];
		}
	    }
	    if(!(sort instanceof ObjectSort) ||
	       (sort == null)){
		throw new IllegalTacletException("Should not happen! Check the code!");
	    }
	    String s ="";
	    if(sv.sort().equals(ProgramSVSort.IMPLICITPREPARED)	       ){
		s = ImplicitFieldAdder.IMPLICIT_CLASS_PREPARED;
	    }
	    if(sv.sort().equals(ProgramSVSort.IMPLICITINITINPROGRESS)	       ){
		s = ImplicitFieldAdder.IMPLICIT_CLASS_INIT_IN_PROGRESS;
	    }
	    if(sv.sort().equals(ProgramSVSort.IMPLICITCLINIT)	       ){
		s = ImplicitFieldAdder.IMPLICIT_CLASS_INITIALIZED;
	    }
	    if(sv.sort().equals(ProgramSVSort.IMPLICITERRONEOUS)	       ){
		s = ImplicitFieldAdder.IMPLICIT_CLASS_ERRONEOUS;
	    }
	    if(sv.sort().equals(ProgramSVSort.IMPLICITNEXTTOCREATE)	       ){
		s = ImplicitFieldAdder.IMPLICIT_NEXT_TO_CREATE;
	    }
	    term = AbstractTacletTranslator.createVariableTerm((ObjectSort)sort, 
			s, services);
	    
	    
	}else{
	    term = TermFactory.DEFAULT.createTerm(term.op(), subTerms, variables,
			JavaBlock.EMPTY_JAVABLOCK);    
	}
	
	
	
	return term;
    }
    
    
    private void check(byte[][] referenceTable, Sort[] instTable,
	    Term [] programSV, TacletConditions conditions) {

	for (int r = 0; r < referenceTable.length; r++) {
	    for (int c = 0; c < referenceTable[r].length; c++) {
		int index = referenceTable[r][c];
		if (referenceTable[r][0] == -1)
		    break;

	
		for (int c2 = c + 1; c2 < referenceTable[r].length; c2++) {
		    int index2 = referenceTable[r][c2]; 
		

		}

	    }
	}

    }


    private Term[] collectProgramSV(Term source){
	Collection<Term> result = new HashSet<Term>();
	collectProgramSV(source, result);
	Term [] temp = new Term[result.size()];
	
	return result.toArray(temp);
    }
    
    private boolean isSupportedProgramsSV(Term term){
	if(term.op() instanceof ProgramSV){
	    ProgramSV sv = (ProgramSV)term.op();
	    return sv.sort().equals(ProgramSVSort.IMPLICITPREPARED)||
	            sv.sort().equals(ProgramSVSort.IMPLICITINITINPROGRESS)||
	            sv.sort().equals(ProgramSVSort.IMPLICITCLINIT)||
	            sv.sort().equals(ProgramSVSort.IMPLICITERRONEOUS)||
	            sv.sort().equals(ProgramSVSort.IMPLICITNEXTTOCREATE);
	}
	return false;
    }
    
    private void collectProgramSV(Term source, Collection<Term> result){
	if(isSupportedProgramsSV(source)){
	    result.add(source);
	}


	for (int i = 0; i < source.arity(); i++) {
	    collectProgramSV(source.sub(i), result);
	}
    }
    
    
    

}
