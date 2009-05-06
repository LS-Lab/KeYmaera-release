package de.uka.ilkd.key.dl.arithmetics.impl.reduce;

import java.math.BigDecimal;
import java.math.BigInteger;

import de.uka.ilkd.key.dl.arithmetics.impl.orbital.PolynomTool;
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.PolynomTool.BigFraction;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.Junctor;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.Quantifier;

/**
 * Converts a term to a QepcadInput readable for the Qepcad-Program.
 * 
 * @author Jan-David Quesel
 */
public class Term2ReduceConverter {

	/**
	 * 
	 */
	static final String TRUE = "true";
	/**
	 * 
	 */
	static final String FALSE = "false";
	static final String DOLLARESCAPE = "dollar";
	static final String UNDERSCOREESCAPE = "uscore";

	/**
	 * Standardconstructor.
	 */
	public Term2ReduceConverter() {
	}

	/**
	 * Function to start to convert a given term.
	 * 
	 * @param form
	 *            Term to convert
	 * @param variables
	 * @return QepCadInput-Instance of the given term.
	 */
	public static String convert(Term form) {
		Term2ReduceConverter converter = new Term2ReduceConverter();
		return converter.convertImpl(form);
	}

	/**
	 * Implementation of the convert-algorithm
	 * 
	 * @param variables
	 */
	private String convertImpl(Term form) {
		String formula = convert2String(form, null, true);
		return formula;
	}

	private String convert2String(Term form, NamespaceSet nss,
			boolean eliminateFractions) {
		if (form.op() == Op.FALSE) {
			return FALSE;
		} else if (form.op() == Op.TRUE) {
			return TRUE;
		} else if (form.op().name().toString().equals("equals")) {
			if (eliminateFractions) {
				return convert2String(PolynomTool
						.eliminateFractionsFromInequality(form, nss), nss,
						false);
			}
			return "( " + convert2String(form.sub(0), nss, true) + " = "
					+ convert2String(form.sub(1), nss, true) + " )";
		} else if (form.op() instanceof Function) {
			Function f = (Function) form.op();
			if (f.name().toString().equals("gt")) {
				if (eliminateFractions) {
					return convert2String(PolynomTool
							.eliminateFractionsFromInequality(form, nss), nss,
							false);
				}
				return "( " + convert2String(form.sub(0), nss, true) + " > "
						+ convert2String(form.sub(1), nss, true) + " )";
			} else if (f.name().toString().equals("geq")) {
				if (eliminateFractions) {
					return convert2String(PolynomTool
							.eliminateFractionsFromInequality(form, nss), nss,
							false);
				}
				return "( " + convert2String(form.sub(0), nss, true) + " >= "
						+ convert2String(form.sub(1), nss, true) + " )";
			} else if (f.name().toString().equals("equals")) {
				if (eliminateFractions) {
					return convert2String(PolynomTool
							.eliminateFractionsFromInequality(form, nss), nss,
							false);
				}
				return "( " + convert2String(form.sub(0), nss, true) + " = "
						+ convert2String(form.sub(1), nss, true) + " )"; 
				// 2x EQUALS ?
			} else if (f.name().toString().equals("neq")) {
				if (eliminateFractions) {
					return convert2String(PolynomTool
							.eliminateFractionsFromInequality(form, nss), nss,
							false);
				}
				return "( " + convert2String(form.sub(0), nss, true) + " <> "
						+ convert2String(form.sub(1), nss, true) + " )";
			} else if (f.name().toString().equals("leq")) {
				if (eliminateFractions) {
					return convert2String(PolynomTool
							.eliminateFractionsFromInequality(form, nss), nss,
							false);
				}
				return "( " + convert2String(form.sub(0), nss, true) + " <= "
						+ convert2String(form.sub(1), nss, true) + " )";
			} else if (f.name().toString().equals("lt")) {
				if (eliminateFractions) {
					return convert2String(PolynomTool
							.eliminateFractionsFromInequality(form, nss), nss,
							false);
				}
				return "( " + convert2String(form.sub(0), nss, true) + " < "
						+ convert2String(form.sub(1), nss, true) + " )";
			} else if (f.name().toString().equals("add")) {
				return "("
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ "+"
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ ")";
			} else if (f.name().toString().equals("sub")) {
				return "("
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ "-"
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ ")";
			} else if (f.name().toString().equals("neg")) {
				return "(-"
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ ")";
			} else if (f.name().toString().equals("mul")) {
				return "("
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ "*"
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ ")";
			} else if (f.name().toString().equals("div")) {
				return "("
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ "/"
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ ")";
			} else if (f.name().toString().equals("exp")) {
				return "("
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ "^"
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
				} catch (NumberFormatException e) {
					String name = form.op().name().toString();
					if (name.contains("$")) {
						name = name.replaceAll("\\$", DOLLARESCAPE);
					}
					if (name.contains("_")) {
						name = name.replaceAll("_", UNDERSCOREESCAPE);
					}
					for(char c = 'A'; c <= 'Z'; c++) {
						name = name.replaceAll("" + c, (c + "_").toLowerCase());
					}
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
			if (name.contains("$")) {
				name = name.replaceAll("\\$", DOLLARESCAPE);
			}
			if (name.contains("_")) {
				name = name.replaceAll("_", UNDERSCOREESCAPE);
			}
			for(char c = 'A'; c <= 'Z'; c++) {
				name = name.replaceAll("" + c, (c + "_").toLowerCase());
			}
			return "(" + name + ")";
		} else if (form.op() instanceof Junctor) {
			if (form.op() == Junctor.AND) {
				return "(("
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ " ) and ("
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ "))";
			} else if (form.op() == Junctor.OR) {
				return "(("
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ ") or ("
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ "))";
			} else if (form.op() == Junctor.IMP) {
				return "(("
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ ") impl ("
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ "))";
			} else if (form.op() == Junctor.NOT) {
				return "( not ("
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ " ))";
			}
		} else if (form.op() instanceof Quantifier) {

			int varsNum = form.varsBoundHere(0).size();
			String[] vars = new String[varsNum];
			for (int i = 0; i < varsNum; i++) {
				String name = form.varsBoundHere(0).getQuantifiableVariable(i)
						.name().toString();
				if (name.contains("$")) {
					name = name.replaceAll("\\$", DOLLARESCAPE);
				}
				if (name.contains("_")) {
					name = name.replaceAll("_", UNDERSCOREESCAPE);
				}
				for(char c = 'A'; c <= 'Z'; c++) {
					name = name.replaceAll("" + c, (c + "_").toLowerCase());
				}
				vars[i] = name;
			}
			String firstArg = convert2String(form.sub(0), nss, eliminateFractions);
			if (form.op() == Quantifier.ALL) {
				String result = "(";
				
				for(String var: vars) {
					result += "all(" + var + ", ";
				}
				result += firstArg;
				for(String var: vars) {
					result += ")";
				}
				result += ")";
				return result;
			} else if (form.op() == Quantifier.EX) {
				String result = "(";
				
				for(String var: vars) {
					result += "ex(" + var + ", ";
				}
				result += firstArg;
				for(String var: vars) {
					result += ")";
				}
				result += ")";
				return result;
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
}
