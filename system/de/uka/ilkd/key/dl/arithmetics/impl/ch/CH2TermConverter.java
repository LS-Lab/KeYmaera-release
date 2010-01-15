package de.uka.ilkd.key.dl.arithmetics.impl.ch;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;

import cohenhormander.Rational;
import cohenhormander.Num;
import cohenhormander.Fn;
import cohenhormander.Var;
import cohenhormander.R;
import cohenhormander.True;
import cohenhormander.False;
import cohenhormander.Atom;
import cohenhormander.Not;
import cohenhormander.And;
import cohenhormander.Or;
import cohenhormander.Imp;
import cohenhormander.Iff;
import cohenhormander.Forall;
import cohenhormander.Exists;



import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.Junctor;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.Quantifier;
import de.uka.ilkd.key.dl.arithmetics.exceptions.FailedComputationException;

/**
 * Converts a term into Cohen-Hormander abstract syntax.
 * 
 */
public class CH2TermConverter {

	private static HashMap<String,String> key2ScalaCmpNames = new HashMap<String,String>();
	
	static{
		key2ScalaCmpNames.put("gt", ">");
		key2ScalaCmpNames.put("geq", ">=");
		key2ScalaCmpNames.put("equals", "=");
		key2ScalaCmpNames.put("leq", "<=");
		key2ScalaCmpNames.put("lt", "<");
	}
	
	

	/**
	 * Standardconstructor.
	 */
	public CH2TermConverter() {
	}

	private static Term convertTerm(cohenhormander.Term tm, NamespaceSet nss ){
		Term res = null;
		
		if(tm instanceof cohenhormander.Var) {
			System.out.println("Reverse translating a variable");
		} else if(tm instanceof cohenhormander.Fn) {
			System.out.println("Reverse translating a Fn");
		} else if(tm instanceof cohenhormander.Num) {
			System.out.println("Reverse translating a num");
		}
		
		return res;
		
	}


}
