package de.uka.ilkd.key.dl.arithmetics.impl.ch;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

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



import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Named;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.Junctor;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.Quantifier;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.dl.arithmetics.exceptions.FailedComputationException;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;

/**
 * Converts a term into Cohen-Hormander abstract syntax.
 * 
 */
public class CH2TermConverter {

	private static HashMap<String,String> scala2KeyNames = new HashMap<String,String>();
	
	static{
		scala2KeyNames.put(">","gt");
		scala2KeyNames.put(">=","geq");
		scala2KeyNames.put("=","equals");
		scala2KeyNames.put("<=","leq");
		scala2KeyNames.put("<","lt");
		scala2KeyNames.put("+","add");
		scala2KeyNames.put("-","sub");
		scala2KeyNames.put("*","mul");
		scala2KeyNames.put("/","div");
	}
	
	

	private static final Map<Name, Function> NUMBERS = new WeakHashMap<Name, Function>();
	
	/**
	 * Standardconstructor.
	 */
	public CH2TermConverter() {
	}

	
	private static cohenhormander.Term[] list2Array(scala.List<cohenhormander.Term> lst) {
		int length = lst.length();
		cohenhormander.Term[] res = new cohenhormander.Term[length];
		for(int i = 0; i < length; i++){
			res[i] = lst.head();
			lst = lst.tail();
		}
		return res;	
	}
	
	
	private static Term convertTerm(cohenhormander.Term tm, NamespaceSet nss ){
		Term res = null;

		Name nm;
		Named nmd;
		Function f;
		
		if(tm instanceof cohenhormander.Var) {
//			System.out.println("Reverse translating a variable");
			nm = new Name(  ((cohenhormander.Var)tm).s());
			nmd = nss.functions().lookup(nm);
			f = (Function) nmd;
			res = TermBuilder.DF.func(f);
		} else if(tm instanceof cohenhormander.Fn) {
			//System.out.println("Reverse translating a Fn");
			cohenhormander.Fn fn = (cohenhormander.Fn)tm;
			String nmString = fn.f(); 
			cohenhormander.Term[] args = list2Array( fn.ps());
			
			if(args.length == 2){
				nm = new Name(scala2KeyNames.get(nmString));
			}else if(nmString == "-" && args.length == 1) {
				nm = new Name("neg");
			} else {
			  throw new Error("unknown function");	
			}

			nmd = nss.functions().lookup(nm);				
			f = (Function) nmd;
			Term[] keyArgs = new Term[args.length];
			for(int i = 0; i < args.length; i++){
				keyArgs[i] = convertTerm(args[i],nss);
			}
			res = TermBuilder.DF.func(f,keyArgs);

		} else if(tm instanceof cohenhormander.Num) {
			//System.out.println("Reverse translating a num");
			//NumberCache
	
			//BigDecimal number;
			
			//if(tm instanceof )
			/*
			Function num = NUMBERS.get(new Name(number.toString()));
			if (num == null) {
				Name name = new Name(number.toString());
				num = new RigidFunction(name,RealLDT.getRealSort(); , new Sort[0]);
				NUMBERS.put(name, num);
			}
			return num;
			*/
			
		}
		
		return res;
		
	}


}
