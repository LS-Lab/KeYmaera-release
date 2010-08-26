package de.uka.ilkd.key.dl.arithmetics.impl.ch;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;


import de.uka.ilkd.key.dl.arithmetics.impl.ch.cohenhormander.*;




  


import de.uka.ilkd.key.dl.arithmetics.impl.orbital.PolynomTool;
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.PolynomTool.BigFraction;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Named;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.Junctor;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.Quantifier;

/**
 * Converts a term into Cohen-Hormander abstract syntax.
 * 
 */
public class Term2CHConverter {

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
	public Term2CHConverter() {
		
	}

	/**
	 * Function to start to convert a given term.
	 * 
	 * @param form
	 *            Term to convert
	 * @param variables
	 */
	public static CHFormula convert(Term form, NamespaceSet nss) {
		Term2CHConverter converter = new Term2CHConverter();
		return converter.convertImpl(form, nss);
	}

	/**
	 * Implementation of the convert-algorithm
	 * 
	 * @param variables
	 */
	private CHFormula convertImpl(Term form, NamespaceSet nss){
			

		// Getting the string-representation
		// String formula = "(" + convert2String( form ) + ")";
		
		CHFormula formula = convert2ScalaFormula(form, nss, true);

		return formula;
		
	}

	
	private static scala.collection.immutable.List<CHTerm> listOfArray(CHTerm[] args){
		scala.collection.immutable.List<CHTerm> result = de.uka.ilkd.key.dl.arithmetics.impl.ch.cohenhormander.AM.nil();
		for(int i = args.length-1; i >= 0; i--){
			result = result.$colon$colon(args[i]);
		}
		return result;
	}
	
		
	private static scala.collection.immutable.List<CHTerm> list(CHTerm... args){
		return listOfArray(args);	
	}
	
	private CHTerm convert2ScalaTerm(Term form, NamespaceSet nss,
				boolean eliminateFractions){
		if (form.op() instanceof Function){
			Function f = (Function) form.op();
			if (f.name().toString().equals("add")) {
				return new Fn("+", list( 
						convert2ScalaTerm(form.sub(0), nss, eliminateFractions),
						convert2ScalaTerm(form.sub(1), nss, eliminateFractions)));	
			} else if (f.name().toString().equals("sub")) {
				return new Fn("-", list( convert2ScalaTerm(form.sub(0), nss, eliminateFractions),
                                         convert2ScalaTerm(form.sub(1), nss, eliminateFractions)));	
			} else if (f.name().toString().equals("neg")) {
				return new Fn("*", list( new Num(new ExactInt(-1)),
                                         convert2ScalaTerm(form.sub(0), nss, eliminateFractions)));	
			} else if (f.name().toString().equals("mul")) {
				return new Fn("*", list( convert2ScalaTerm(form.sub(0), nss, eliminateFractions),
                                         convert2ScalaTerm(form.sub(1), nss, eliminateFractions)));	
			} else if (f.name().toString().equals("div")) {
				return new Fn("/", list( convert2ScalaTerm(form.sub(0), nss, eliminateFractions),
						convert2ScalaTerm(form.sub(1), nss, eliminateFractions)));	
			} else if (f.name().toString().equals("exp")) {
				return new Fn("^", list( convert2ScalaTerm(form.sub(0), nss, eliminateFractions),
						convert2ScalaTerm(form.sub(1), nss, eliminateFractions)));	
			} else {
				CHTerm[] args = new CHTerm[form.arity()];
				for (int i = 0; i < args.length; i++) {
					args[i] = convert2ScalaTerm(form.sub(i), nss,
							eliminateFractions);
				}
				try {  
					String numberAsString = form.op().name().toString();
					BigFraction frac = PolynomTool
					.convertStringToFraction(numberAsString);
					if (frac.getDenominator().equals(BigInteger.ONE)) {
						return new Num(new Rational(new scala.math.BigInt(frac.getNumerator())));
					} else {
						return new Num(new Rational( 
								new scala.math.BigInt( frac.getNumerator()),
								new scala.math.BigInt( frac.getDenominator()))); 
					}
				} catch (NumberFormatException nfe) {
					//System.out.println(form + " (a function) is of type " + form.getClass().toString());
					String name = form.op().name().toString();
					//System.out.println("and of name " + name);
					Named nmd = nss.lookup(new Name(name));
					//if(nmd != null)
					//	System.out.println("and its lookup has type " + nmd.getClass().toString());
					if (args.length == 0) {
						return new Var( name );
					}
					return new Fn( name, listOfArray(args));
				}
			}

		}else if (form.op() instanceof LogicVariable
				|| form.op() instanceof de.uka.ilkd.key.logic.op.ProgramVariable
				|| form.op() instanceof Metavariable) {
			//System.out.println(form + " is of type " + form.getClass().toString());
			String name = form.op().name().toString();
			//System.out.println("and of name " + name);
			Named nmd = nss.lookup(new Name(name));
			//if(nmd != null)
			//	System.out.println("and its lookup has type " + nmd.getClass().toString());

			return new Var(name);
		}
		
		return new Num(new Rational(1));

	}
	
		
		
		
	private CHFormula convert2ScalaFormula(Term form, NamespaceSet nss,
			boolean eliminateFractions) {
		if (form.op() == Op.FALSE) {
			return new False();
		} else if (form.op() == Op.TRUE) {
			return new True();
		} else if (form.op().name().toString().equals("equals")) {
			if (eliminateFractions) {
				return convert2ScalaFormula(PolynomTool
						.eliminateFractionsFromInequality(form, nss), nss,
						false);
			}
			return new Atom(new R("=", list(convert2ScalaTerm(form.sub(0),nss,true), 
                    convert2ScalaTerm(form.sub(1),nss,true))));
		} else if (form.op().name().toString().equals("neq")) {
			if (eliminateFractions) {
				return convert2ScalaFormula(PolynomTool
						.eliminateFractionsFromInequality(form, nss), nss,
						false);
			}
			return new Not(new Atom(new R("=", list(convert2ScalaTerm(form.sub(0),nss,true), 
                                                                convert2ScalaTerm(form.sub(1),nss,true)))));
		} else if (form.op().name().toString().equals("equiv")) {
			return new Iff( convert2ScalaFormula(form.sub(0), nss, eliminateFractions),
			        convert2ScalaFormula(form.sub(1), nss, eliminateFractions));	

		} else if (form.op() instanceof Function) {
			Function f = (Function) form.op();
			String nm = f.name().toString();
			 /* if it's a comparison... */
			if (key2ScalaCmpNames.containsKey(nm)){
				if (eliminateFractions) {
					return convert2ScalaFormula(PolynomTool
							.eliminateFractionsFromInequality(form, nss), nss,
							false);
				}
				return new Atom(new R(key2ScalaCmpNames.get(nm),
						list(convert2ScalaTerm(form.sub(0),nss,true), 
	                         convert2ScalaTerm(form.sub(1),nss,true))));	 
			} 
		} else if (form.op() instanceof LogicVariable
				|| form.op() instanceof de.uka.ilkd.key.logic.op.ProgramVariable
				|| form.op() instanceof Metavariable) {
			String name = form.op().name().toString();
			return new Atom(new R(name, list()));
		} else if (form.op() instanceof Junctor) {
			if (form.op() == Junctor.AND) {
				return new And( convert2ScalaFormula(form.sub(0), nss, eliminateFractions),
				        convert2ScalaFormula(form.sub(1), nss, eliminateFractions));	
			} else if (form.op() == Junctor.OR) {
				return new Or( convert2ScalaFormula(form.sub(0), nss, eliminateFractions),
				        convert2ScalaFormula(form.sub(1), nss, eliminateFractions));	
			} else if (form.op() == Junctor.IMP) {
				return new Imp( convert2ScalaFormula(form.sub(0), nss, eliminateFractions),
				        convert2ScalaFormula(form.sub(1), nss, eliminateFractions));	
			} else if (form.op() == Junctor.NOT) {
				return new Not( convert2ScalaFormula(form.sub(0), nss, eliminateFractions));
			}
		} else if (form.op() instanceof Quantifier) {
			int varsNum = form.varsBoundHere(0).size();
			String[] vars = new String[varsNum];
			for (int i = 0; i < varsNum; i++) {
				String name = form.varsBoundHere(0).get(i)
						.name().toString();

				vars[i] = name;
			}
			CHFormula result = convert2ScalaFormula(form.sub(0),nss,eliminateFractions);
			
			
			if (form.op() == Quantifier.ALL){
				for(int i = vars.length - 1; i >= 0; i--){
					result = new Forall(vars[i], result);
				}
			}
			else if (form.op() == Quantifier.EX){
				for(int i = vars.length - 1; i >= 0; i--){
					result = new Exists(vars[i], result);
				}
			}
			return result;

		}
		throw 	new IllegalArgumentException("Could not convert Term: " + form
				+ "Operator was: " + form.op());
	}


	



}
