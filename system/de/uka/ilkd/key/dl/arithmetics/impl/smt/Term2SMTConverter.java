package de.uka.ilkd.key.dl.arithmetics.impl.smt;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
 * Converts a term to a SMTInput for SMT LIB programs 
 *  
 * @author jdq
 */
public class Term2SMTConverter {

	static final String USCOREESCAPE = "uscore";
	static final String DOLLARESCAPE = "dollar";
	private SMTInput input = new SMTInput(); // Result
	private ArrayList<String> existingVars = new ArrayList<String>(); // List of

	// existing
	// Variables

	/**
	 * Standardconstructor.
	 */
	public Term2SMTConverter() {
	}

	/**
	 * Function to start to convert a given term.
	 * 
	 * @param form
	 *            Term to convert
	 * @param variables
	 * @return QepCadInput-Instance of the given term.
	 */
	public static SMTInput convert(Term form,
			List<QuantifiableVariable> variables) {
		Term2SMTConverter converter = new Term2SMTConverter();
		return converter.convertImpl(form, variables);
	}

	/**
	 * Implementation of the convert-algorithm
	 * 
	 * @param variables
	 */
	private SMTInput convertImpl(Term form,
			List<QuantifiableVariable> variables) {

		// Getting the string-representation
		// String formula = "(" + convert2String( form ) + ")";
		String formula = convert2String(form, null, false);

		// extracts additional information for qepcad
		List<String> freeVarlist = new ArrayList<String>(existingVars);

		// the first parameter is changed by the function
		this.input.setVariableList(variableDeclarations2String(getVariableList(freeVarlist, variables)));

		if (!formula.startsWith("(")) {
			formula = "( " + formula + " )";
		}

		this.input.setFormula(formula);
		return this.input;
	}

	private String convert2String(Term form, NamespaceSet nss,
			boolean eliminateFractions) {
		if (form.op() == Op.FALSE) {
			return "false";
		} else if (form.op() == Op.TRUE) {
			return "true";
		} else if (form.op().name().toString().equals("equals")) {
			if (eliminateFractions) {
				return convert2String(PolynomTool
						.eliminateFractionsFromInequality(form, nss), nss,
						false);
			}
			return "(= " + convert2String(form.sub(0), nss, true) + " "
					+ convert2String(form.sub(1), nss, true) + " )";
		} else if (form.op() instanceof Function) {
			Function f = (Function) form.op();
			if (f.name().toString().equals("gt")) {
				if (eliminateFractions) {
					return convert2String(PolynomTool
							.eliminateFractionsFromInequality(form, nss), nss,
							false);
				}
				return "(> " + convert2String(form.sub(0), nss, true) + " "
						+ convert2String(form.sub(1), nss, true) + " )";
			} else if (f.name().toString().equals("geq")) {
				if (eliminateFractions) {
					return convert2String(PolynomTool
							.eliminateFractionsFromInequality(form, nss), nss,
							false);
				}
				return "(>= " + convert2String(form.sub(0), nss, true) + " "
						+ convert2String(form.sub(1), nss, true) + " )";
			} else if (f.name().toString().equals("equals")) {
				if (eliminateFractions) {
					return convert2String(PolynomTool
							.eliminateFractionsFromInequality(form, nss), nss,
							false);
				}
				return "(= " + convert2String(form.sub(0), nss, true) + " "
						+ convert2String(form.sub(1), nss, true) + " )";
				// 2x EQUALS ?
			} else if (f.name().toString().equals("neq")) {
				if (eliminateFractions) {
					return convert2String(PolynomTool
							.eliminateFractionsFromInequality(form, nss), nss,
							false);
				}
				return "(not (=" + convert2String(form.sub(0), nss, true) + " "
						+ convert2String(form.sub(1), nss, true) + " ))";
			} else if (f.name().toString().equals("leq")) {
				if (eliminateFractions) {
					return convert2String(PolynomTool
							.eliminateFractionsFromInequality(form, nss), nss,
							false);
				}
				return "(<= " + convert2String(form.sub(0), nss, true) + " "
						+ convert2String(form.sub(1), nss, true) + " )";
			} else if (f.name().toString().equals("lt")) {
				if (eliminateFractions) {
					return convert2String(PolynomTool
							.eliminateFractionsFromInequality(form, nss), nss,
							false);
				}
				return "(< " + convert2String(form.sub(0), nss, true) + " "
						+ convert2String(form.sub(1), nss, true) + " )";
			} else if (f.name().toString().equals("add")) {
				return "(+ "
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ " "
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ ")";
			} else if (f.name().toString().equals("sub")) {
				return "(- "
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ " "
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ ")";
			} else if (f.name().toString().equals("neg")) {
				return "(- "
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ ")";
			} else if (f.name().toString().equals("mul")) {
				return "(* "
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ " "
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ ")";
			} else if (f.name().toString().equals("div")) {
				return "(/ "
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ " "
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ ")";
			} else if (f.name().toString().equals("exp")) {
				String e = convert2String(form.sub(1), nss, eliminateFractions);
				int exp = Integer.parseInt(e.substring(0,e.length() - 1));
				if(exp == 0) {
					return "1";
				}
				String s = convert2String(form.sub(0), nss, eliminateFractions);
				String res = "(*";
				for(int i = 0; i < exp; i++) {
					res += " " + s;
				}
				return res + ")";
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
						return frac.getNumerator().toString() + ".";
					} else {
						return "(/ " + frac.getNumerator() + ". "
								+ frac.getDenominator() + ". )";
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
						return name;
					}
					return "(" + name + " " + array2StringBlanks(args) + ")";
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
			return name;
		} else if (form.op() instanceof Junctor) {
			if (form.op() == Junctor.AND) {
				return "(and "
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ " "
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ ")";
			} else if (form.op() == Junctor.OR) {
				return "(or "
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ " "
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ ")";
			} else if (form.op() == Junctor.IMP) {
				return "(=> "
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ " "
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ ")";
			} else if (form.op() == Junctor.NOT) {
				return "(not "
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ ")";
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
//				addExistingVariable(name);
			}

			if (form.op() == Quantifier.ALL) {
				return "(forall " + array2String(vars) + " "
						+ convert2String(form.sub(0), nss, eliminateFractions) + ")";
			} else if (form.op() == Quantifier.EX) {
				return "(exists " + array2String(vars) + " "
						+ convert2String(form.sub(0), nss, eliminateFractions) + ")";
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

		String result = "(";
		for (int i = 0; i < args.length; i++) {
			result += "(" + args[i] + " Real)";
			if (i != args.length - 1)
				result += " ";
		}

		return result + ")";
	}
	
	private String array2StringBlanks(String[] args) {
		if (args == null)
			return "";

		String result = "";
		for (int i = 0; i < args.length; i++) {
			result += args[i];
			if (i != args.length - 1)
				result += " ";
		}

		return result;
	}
	
	// Converts an array of Strings in
	// one string. The elements are seperated by
	// ','
	private String variableDeclarations2String(String[] args) {
		if (args == null)
			return "";

		String result = "";
		for (int i = 0; i < args.length; i++) {
			result += "(declare-const " + args[i] + " Real)";
			if (i != args.length - 1)
				result += "\n";
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
