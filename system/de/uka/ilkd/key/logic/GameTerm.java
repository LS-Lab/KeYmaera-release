// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.logic;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;

class GameTerm extends Term {
    /** 
     * the program
     */
    private final JavaBlock javaBlock;

    /**  sub term */
    private final ImmutableArray<Term> subTerm; 

    /** caches depth */
    private int depth=-1;

    /** 
     * creates a diamond term, so there is an additional
     * parameter javaBlock 
     */
    GameTerm(Operator modality, 
		JavaBlock javaBlock) {
	this(modality, javaBlock, new Term[]{});
    }

    GameTerm(Operator op, 
		JavaBlock javaBlock, 
		Term[] subTerm) {
	super(op, op.sort(subTerm));
	this.subTerm=new ImmutableArray<Term>(subTerm);
	if(javaBlock == null) {
		this.javaBlock = JavaBlock.EMPTY_JAVABLOCK;
	} else {
		this.javaBlock=javaBlock;
    }
	}

    /** @return n-th subterm (always the only one)*/    
    public Term sub(int n) {
	return subTerm.get(n);
    }	
   
    /** @return arity of the quantifier term 1 as int */
    public int arity() {
	return subTerm.size();
    } 

    /**@return depth of the term */
    public int depth() {
	if(this.depth == -1) {
	    int localdepth = 0;
	    for(int i=0;i<subTerm.size();i++) {
		if(subTerm.get(i).depth() > localdepth)
		    localdepth = subTerm.get(i).depth();
	    }
	    this.depth = localdepth + 1;
	}
	return this.depth;
    }

    /** @return JavaBlock if term has diamond */
    public JavaBlock javaBlock() {
	return javaBlock;
    }
    
    /** @return an empty variable list */
    public ImmutableArray<QuantifiableVariable> varsBoundHere(int n) {
	return EMPTY_VAR_LIST;
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	if ( op() == Op.DIA ) {
	    sb.append("\\<").append(javaBlock).append("\\> ");
	    for(int i=0; i<subTerm.size(); i++)
	           sb.append("(").append(subTerm.get(i)).append(")");
	} else if ( op() == Op.BOX ) {
	    sb.append("\\[").append(javaBlock).append("\\] ");
	    for(int i=0; i<subTerm.size(); i++)
	           sb.append("(").append(subTerm.get(i)).append(")");
	} else if ( op() == Op.TOUT ) {
	    sb.append("\\[[").append(javaBlock).append("\\]] ");
	    for(int i=0; i<subTerm.size(); i++)
	           sb.append("(").append(subTerm.get(i)).append(")");
	} else if ( op() == Op.GAME){
		sb.append("\\(").append(subTerm.get(0)).append("\\) ");
		for(int i=0; i<subTerm.size(); i++)
	           sb.append("(").append(subTerm.get(i)).append(")");
	} else if( op() == Op.SEQGAME) {
		sb.append("(").append(subTerm.get(0) + " " + subTerm.get(1)).append(")");
	} else if( op() == Op.CUPGAME) {
		sb.append("(").append(subTerm.get(0) + " ++ " + subTerm.get(1)).append(")");
	} else if( op() == Op.CAPGAME) {
		sb.append("(").append(subTerm.get(0) + " +-+ " + subTerm.get(1)).append(")");
	}

	return sb.toString();
    }
    
    protected int calculateHash(){
	int hashValue = 5;
	hashValue = hashValue*17 + op().hashCode();  
	hashValue = hashValue*17 + sort().hashCode();
	
        for(int i = 0, ar = arity() ; i<ar; i++){
	   hashValue = hashValue*17 + varsBoundHere(i).size();
	   hashValue = hashValue*17 + sub(i).hashCode();
	}
        if(javaBlock() != null) {
        	hashValue = hashValue*17 + javaBlock().hashCode();
        }
        if (hashValue == 0) return 1;
	
        return hashValue;
    }

}
