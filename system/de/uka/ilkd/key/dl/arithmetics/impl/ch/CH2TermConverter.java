package de.uka.ilkd.key.dl.arithmetics.impl.ch;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import de.uka.ilkd.key.dl.arithmetics.impl.ch.cohenhormander.*;


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
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.Quantifier;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.logic.sort.Sort;
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

	
	private static CHTerm[] 
            list2Array(scala.collection.immutable.List<CHTerm> lst) {
		int length = lst.length();
		CHTerm[] res = new CHTerm[length];
		for(int i = 0; i < length; i++){
                    res[i] = (CHTerm)(lst.apply(i));
		}
		return res;	
	}
	
	
	private static Term convertTerm(CHTerm tm, NamespaceSet nss ){
		Term res = null;

		Name nm;
		Named nmd;
		Function f;
		
		if(tm instanceof Var) {
//			System.out.println("Reverse translating a variable");
			nm = new Name(  ((Var)tm).s());
			nmd = nss.lookup(nm);
			if(nmd instanceof Function){
				f = (Function) nmd;
				res = TermBuilder.DF.func(f);
			} else if(nmd instanceof ProgramVariable){
				res = TermBuilder.DF.var((ProgramVariable)nmd);
			} else if(nmd instanceof LogicVariable){
				res = TermBuilder.DF.var((LogicVariable)nmd);
			} else {
				throw new Error("unable to locate variable:" + nm);
			}
		} else if(tm instanceof Fn) {
			//System.out.println("Reverse translating a Fn");
			Fn fn = (Fn)tm;
			String nmString = fn.f(); 
			CHTerm[] args = list2Array( fn.ps());
			
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

		} else if(tm instanceof Num) {
			//System.out.println("Reverse translating a num");
			
			
			
			if (!(((Num)tm).n() instanceof ExactInt )) {
			 throw new Error("can't convert fraction: "+ ((Num)tm).n());
			}

			// Look in the cache.
			Function num = NUMBERS.get(new Name(((Num)tm).n().toString()));
			if (num == null) {
				Name name = new Name(((Num)tm).n().toString());
				num = new RigidFunction(name,RealLDT.getRealSort() , new Sort[0]);
				NUMBERS.put(name, num);
			}
			
			res = TermBuilder.DF.func(num);
			
		}
		
		return res;
		
	}

	public static Term convertFormula(CHFormula fm, NamespaceSet nss){
		Term res = null;
		
		
		
		if(fm instanceof False){
			res = TermBuilder.DF.ff();
		}else if(fm instanceof True){
			res = TermBuilder.DF.tt();
		} else if (fm instanceof Atom){
			R r = (R) (((Atom)fm).a());
			CHTerm[] chts = list2Array(r.ps());
			Term[] ts = new Term[chts.length];
			for(int i = 0; i < chts.length; i++){
				ts[i] = convertTerm(chts[i], nss);
			}

			
			if(r.s() == "="){
				res = TermBuilder.DF.equals(ts[0],ts[1]);							
			} else {
				Name nm = new Name(scala2KeyNames.get(r.s()));
				Named nmd = nss.functions().lookup(nm); 								
				Function fn = (Function) nmd;
				res = TermBuilder.DF.func(fn,ts);
			}
			

		} else if (fm instanceof Not) {
			Term t = convertFormula(((Not)fm).f(),nss);
			res = TermBuilder.DF.not(t);
			
		} else if (fm instanceof And){
			Term t1 = convertFormula(((And)fm).f1(),nss); 
			Term t2 = convertFormula(((And)fm).f2(),nss);
			res = TermBuilder.DF.and(t1,t2);
		} else if (fm instanceof Or){
			Term t1 = convertFormula(((Or)fm).f1(),nss); 
			Term t2 = convertFormula(((Or)fm).f2(),nss);
			res = TermBuilder.DF.or(t1,t2);
		}else if (fm instanceof Imp){
			Term t1 = convertFormula(((Imp)fm).f1(),nss); 
			Term t2 = convertFormula(((Imp)fm).f2(),nss);
			res = TermBuilder.DF.imp(t1,t2);
		}else if (fm instanceof Iff){
			Term t1 = convertFormula(((Iff)fm).f1(),nss); 
			Term t2 = convertFormula(((Iff)fm).f2(),nss);
			res = TermBuilder.DF.equiv(t1,t2);
		}else{
			throw new Error("bad output from cohenhormander");
			
		}
		
		
		return res;
	}
	

}
