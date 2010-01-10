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



import de.uka.ilkd.key.dl.arithmetics.impl.orbital.PolynomTool;
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.PolynomTool.BigFraction;
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
 * Converts a term to a QepcadInput readable for the Qepcad-Program.
 * 
 * @author Timo Michelsen
 */
public class Term2CHConverter {

	static final String USCOREESCAPE = "uscore";
	static final String DOLLARESCAPE = "dollar";
	//private QepCadInput input = new QepCadInput(); // Result
	private ArrayList<String> existingVars = new ArrayList<String>(); // List of

	private static HashMap<String,String> key2ScalaCmpNames = new HashMap<String,String>();
	
	static{
		key2ScalaCmpNames.put("gt", ">");
		key2ScalaCmpNames.put("geq", ">=");
		key2ScalaCmpNames.put("equals", "=");
		key2ScalaCmpNames.put("leq", "<=");
		key2ScalaCmpNames.put("lt", "<");
	}
	
	
	// existing
	// Variables

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
	 * @return QepCadInput-Instance of the given term.
	 */
	public static cohenhormander.Formula convert(Term form,
			List<QuantifiableVariable> variables) {
		Term2CHConverter converter = new Term2CHConverter();
		return converter.convertImpl(form, variables);
	}

	/**
	 * Implementation of the convert-algorithm
	 * 
	 * @param variables
	 */
	private cohenhormander.Formula convertImpl(Term form,
			List<QuantifiableVariable> variables) {

		// Getting the string-representation
		// String formula = "(" + convert2String( form ) + ")";
		
		System.out.println("free vars: " + variables);
		cohenhormander.Formula formula = convert2ScalaFormula(form, null, true);

		// extracts additional information for qepcad
		List<String> freeVarlist = new ArrayList<String>(existingVars);

		// the first parameter is changed by the function
		//this.input.setVariableList("("
		//		+ array2String(getVariableList(freeVarlist, variables)) + ")");
		//this.input.setFreeVariableNum(freeVarlist.size());

		return formula;
		
//		this.input.setFormula(formula);
//		return this.input;
	}

	
	private static scala.List<cohenhormander.Term> listOfArray(cohenhormander.Term[] args){
		scala.List<cohenhormander.Term> result = cohenhormander.AM.nil();
		for(int i = args.length-1; i >= 0; i--){
			result = result.$colon$colon(args[i]);
		}
		return result;
	}
	
		
	private static scala.List<cohenhormander.Term> list(cohenhormander.Term... args){
		return listOfArray(args);	
	}
	
	private cohenhormander.Term convert2ScalaTerm(Term form, NamespaceSet nss,
				boolean eliminateFractions){
		if (form.op() instanceof Function){
			Function f = (Function) form.op();
			if (f.name().toString().equals("add")) {
				return new cohenhormander.Fn("+", list( 
						convert2ScalaTerm(form.sub(0), nss, eliminateFractions),
						convert2ScalaTerm(form.sub(1), nss, eliminateFractions)));	
			} else if (f.name().toString().equals("sub")) {
				return new Fn("-", list( convert2ScalaTerm(form.sub(0), nss, eliminateFractions),
                                         convert2ScalaTerm(form.sub(1), nss, eliminateFractions)));	
			} else if (f.name().toString().equals("neg")) {
				return new Fn("*", list( new Num(new Rational(-1)),
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
				cohenhormander.Term[] args = new cohenhormander.Term[form.arity()];
				for (int i = 0; i < args.length; i++) {
					args[i] = convert2ScalaTerm(form.sub(i), nss,
							eliminateFractions);
				}
				try {  
					String numberAsString = form.op().name().toString();
					BigFraction frac = PolynomTool
					.convertStringToFraction(numberAsString);
					if (frac.getDenominator().equals(BigInteger.ONE)) {
						return new Num(new Rational(new scala.BigInt(frac.getNumerator())));
					} else {
						return new Num(new Rational( 
								new scala.BigInt( frac.getNumerator()),
								new scala.BigInt( frac.getDenominator()))); 
					}
				} catch (NumberFormatException nfe) {
					String name = form.op().name().toString();

					addExistingVariable(name);
					if (args.length == 0) {
						return new Var( name );
					}
					return new Fn( name, listOfArray(args));
				}
			}

		}else if (form.op() instanceof LogicVariable
				|| form.op() instanceof de.uka.ilkd.key.logic.op.ProgramVariable
				|| form.op() instanceof Metavariable) {
			String name = form.op().name().toString();

			addExistingVariable(name);
			return new cohenhormander.Var(name);
		}
		
		return new cohenhormander.Num(new cohenhormander.Rational(1));

	}
	
		
		
		
	private cohenhormander.Formula convert2ScalaFormula(Term form, NamespaceSet nss,
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

			addExistingVariable(name);
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
				addExistingVariable(name);
			}
			cohenhormander.Formula result = convert2ScalaFormula(form.sub(0),nss,eliminateFractions);
			
			
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

	
	
	private String convert2String(Term form, NamespaceSet nss,
			boolean eliminateFractions) {
		if (form.op() == Op.FALSE) {
			return "[0 = 1]";
		} else if (form.op() == Op.TRUE) {
			return "[0 = 0]";
		} else if (form.op().name().toString().equals("equals")) {
			if (eliminateFractions) {
				return convert2String(PolynomTool
						.eliminateFractionsFromInequality(form, nss), nss,
						false);
			}
			return "[ " + convert2String(form.sub(0), nss, true) + " = "
					+ convert2String(form.sub(1), nss, true) + " ]";
		} else if (form.op() instanceof Function) {
			Function f = (Function) form.op();
			if (f.name().toString().equals("gt")) {
				if (eliminateFractions) {
					return convert2String(PolynomTool
							.eliminateFractionsFromInequality(form, nss), nss,
							false);
				}
				return "[ " + convert2String(form.sub(0), nss, true) + " > "
						+ convert2String(form.sub(1), nss, true) + " ]";
			} else if (f.name().toString().equals("geq")) {
				if (eliminateFractions) {
					return convert2String(PolynomTool
							.eliminateFractionsFromInequality(form, nss), nss,
							false);
				}
				return "[ " + convert2String(form.sub(0), nss, true) + " >= "
						+ convert2String(form.sub(1), nss, true) + " ]";
			} else if (f.name().toString().equals("equals")) {
				if (eliminateFractions) {
					return convert2String(PolynomTool
							.eliminateFractionsFromInequality(form, nss), nss,
							false);
				}
				return "[ " + convert2String(form.sub(0), nss, true) + " = "
						+ convert2String(form.sub(1), nss, true) + " ]";
				// 2x EQUALS ?
			} else if (f.name().toString().equals("neq")) {
				if (eliminateFractions) {
					return convert2String(PolynomTool
							.eliminateFractionsFromInequality(form, nss), nss,
							false);
				}
				return "[ " + convert2String(form.sub(0), nss, true) + " /= "
						+ convert2String(form.sub(1), nss, true) + " ]";
			} else if (f.name().toString().equals("leq")) {
				if (eliminateFractions) {
					return convert2String(PolynomTool
							.eliminateFractionsFromInequality(form, nss), nss,
							false);
				}
				return "[ " + convert2String(form.sub(0), nss, true) + " <= "
						+ convert2String(form.sub(1), nss, true) + " ]";
			} else if (f.name().toString().equals("lt")) {
				if (eliminateFractions) {
					return convert2String(PolynomTool
							.eliminateFractionsFromInequality(form, nss), nss,
							false);
				}
				return "[ " + convert2String(form.sub(0), nss, true) + " < "
						+ convert2String(form.sub(1), nss, true) + " ]";
			} else if (f.name().toString().equals("add")) {
				return "(("
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ ") + ("
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ "))";
			} else if (f.name().toString().equals("sub")) {
				return "(("
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ ") - ("
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ "))";
			} else if (f.name().toString().equals("neg")) {
				return "(-("
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ "))";
			} else if (f.name().toString().equals("mul")) {
				return "(("
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ ") ("
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ "))";
			} else if (f.name().toString().equals("div")) {
				return "(("
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ ") / ("
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ "))";
			} else if (f.name().toString().equals("exp")) {
				return "(("
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ ")^"
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ ")";
			} else {
				String[] args = new String[form.arity()];
				for (int i = 0; i < args.length; i++) {
					args[i] = convert2String(form.sub(i), nss,
							eliminateFractions);
				}
				try {
					String numberAsString = form.op().name().toString();
					BigFraction frac = PolynomTool
							.convertStringToFraction(numberAsString);
					if (frac.getDenominator().equals(BigInteger.ONE)) {
						return frac.getNumerator().toString();
					} else {
						return "( " + frac.getNumerator() + " / "
								+ frac.getDenominator() + " )";
					}
				} catch (NumberFormatException nfe) {
					String name = form.op().name().toString();
					if (name.contains("_")) {
						name = name.replaceAll("_", USCOREESCAPE);

					}
					if (name.contains("$")) {
						name = name.replaceAll("\\$", DOLLARESCAPE);
					}
					addExistingVariable(name);
					if (args.length == 0) {
						return "(" + name + ")";
					}
					return "(" + name + "(" + array2String(args) + "))";
				}
			}
		} else if (form.op() instanceof LogicVariable
				|| form.op() instanceof de.uka.ilkd.key.logic.op.ProgramVariable
				|| form.op() instanceof Metavariable) {
			String name = form.op().name().toString();
			if (name.contains("_")) {
				name = name.replaceAll("_", USCOREESCAPE);
			}
			if (name.contains("$")) {
				name = name.replaceAll("\\$", DOLLARESCAPE);
			}
			addExistingVariable(name);
			return "(" + name + ")";
		} else if (form.op() instanceof Junctor) {
			if (form.op() == Junctor.AND) {
				return "["
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ "/\\"
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ "]";
			} else if (form.op() == Junctor.OR) {
				return "["
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ "\\/"
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ "]";
			} else if (form.op() == Junctor.IMP) {
				return "["
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ "==>"
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ "]";
			} else if (form.op() == Junctor.NOT) {
				return "[~["
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ "]]";
			}
		} else if (form.op() instanceof Quantifier) {

			int varsNum = form.varsBoundHere(0).size();
			String[] vars = new String[varsNum];
			for (int i = 0; i < varsNum; i++) {
				String name = form.varsBoundHere(0).get(i)
						.name().toString();
				if (name.contains("_")) {
					name = name.replaceAll("_", USCOREESCAPE);
				}
				if (name.contains("$")) {
					name = name.replaceAll("\\$", DOLLARESCAPE);
				}
				vars[i] = name;
				addExistingVariable(name);
			}

			if (form.op() == Quantifier.ALL) {
				return "(A " + array2String(vars) + ")"
						+ convert2String(form.sub(0), nss, eliminateFractions);
			} else if (form.op() == Quantifier.EX) {
				return "(E " + array2String(vars) + ")"
						+ convert2String(form.sub(0), nss, eliminateFractions);
			}
		}
		throw new IllegalArgumentException("Could not convert Term: " + form
				+ "Operator was: " + form.op());
	}

	// Converts an array of Strings in
	// one string. The elements are seperated by
	// ','
	private String array2String(String[] args) {
		if (args == null)
			return "";

		String result = "";
		for (int i = 0; i < args.length; i++) {
			result += args[i];
			if (i != args.length - 1)
				result += ",";
		}

		return result;
	}

	// Inserts a new variable in the list of existing variables,
	// if is not in the list
	private void addExistingVariable(String varName) {
		if (!existingVars.contains(varName)) {
			this.existingVars.add(varName);
		}

	}

	// Gets the variable-list, which is important for qepcad
	private String[] getVariableList(List<String> allVariables,
			List<QuantifiableVariable> quantifiedVars) {

		ArrayList<String> freeVars = new ArrayList<String>();
		List<String> quantified = new ArrayList<String>();

		for (QuantifiableVariable var : quantifiedVars) {
			String name = var.name().toString();
			if (name.contains("_")) {
				name = name.replaceAll("_", USCOREESCAPE);
			}
			if (name.contains("$")) {
				name = name.replaceAll("\\$", DOLLARESCAPE);
			}
			allVariables.remove(name);
			quantified.add(name);
		}
		Collections.reverse(quantified);

		for (String var : allVariables) {
			freeVars.add(var);
		}

		String[] result = new String[allVariables.size() + quantified.size()];
		for (int i = 0; i < freeVars.size(); i++) {
			result[i] = freeVars.get(i);
		}
		for (int i = 0; i < quantified.size(); i++) {
			result[freeVars.size() + i] = quantified.get(i);
		}

		return result;
	}
}
