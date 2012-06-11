/*******************************************************************************
 * Copyright (c) 2012 Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jan-David Quesel - initial API and implementation
 ******************************************************************************/
package de.uka.ilkd.key.dl.arithmetics.impl.hollight;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
 * @author Jan-David Quesel
 */
public class Term2HarrisonConverter {

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

	private Set<String> variables = new LinkedHashSet<String>();
	private Set<String> quantifiedVariables = new LinkedHashSet<String>();

	/**
	 * Standardconstructor.
	 */
	public Term2HarrisonConverter() {
	}

	/**
	 * Function to start to convert a given term.
	 * 
	 * @param form
	 *            Term to convert
	 * @param list
	 * @param variables
	 * @return QepCadInput-Instance of the given term.
	 */
	public static String convert(Term form, boolean universalClosure) {
		Term2HarrisonConverter converter = new Term2HarrisonConverter();
		return converter.convertImpl(form, universalClosure);
	}

	/**
	 * @param list
	 * @return TODO documentation since 29.04.2009
	 */
	private static String list2quantifiers(Collection<String> list) {
		String result = "";
		for (String v : list) {
			result += v.toString() + " ";
		}
		return result;
	}

	/**
	 * Implementation of the convert-algorithm
	 * 
	 * @param variables
	 */
	private String convertImpl(Term form, boolean universalClosure) {
		String formula = convert2String(form, null, true);
		if (!universalClosure) {
			return formula;
		}
		variables.removeAll(quantifiedVariables);
		if (variables.isEmpty()) {
			return formula;
		} else {
			return "forall " + list2quantifiers(variables) + ". " + formula;
		}
	}

	private String convert2String(Term form, NamespaceSet nss,
			boolean eliminateFractions) {
		return convert2String(form, nss, eliminateFractions, false);
	}

	private String convert2String(Term form, NamespaceSet nss,
			boolean eliminateFractions, boolean pow) {
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
				return "(~( " + convert2String(form.sub(0), nss, true) + " = "
						+ convert2String(form.sub(1), nss, true) + " ))";
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
				+ " / "
				+ convert2String(form.sub(1), nss, eliminateFractions)
				+ ")";
			} else if (f.name().toString().equals("exp")) {
				return "("
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ "^"
						+ convert2String(form.sub(1), nss, eliminateFractions,
								true) + ")";
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
					if (args.length == 0) {
						variables.add(name);
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
			variables.add(name);
			return "(" + name + ")";
		} else if (form.op() instanceof Junctor) {
			if (form.op() == Junctor.AND) {
				return "(("
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ " ) /\\ ("
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ "))";
			} else if (form.op() == Junctor.OR) {
				return "(("
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ ") \\/ ("
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ "))";
			} else if (form.op() == Junctor.IMP) {
				return "(~("
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ ") \\/ ("
						+ convert2String(form.sub(1), nss, eliminateFractions)
						+ "))";
			} else if (form.op() == Junctor.NOT) {
				return "( ~ ("
						+ convert2String(form.sub(0), nss, eliminateFractions)
						+ " ))";
			}
		} else if (form.op() instanceof Quantifier) {

			int varsNum = form.varsBoundHere(0).size();
			String[] vars = new String[varsNum];
			for (int i = 0; i < varsNum; i++) {
				String name = form.varsBoundHere(0).get(i)
						.name().toString();
				if (name.contains("$")) {
					name = name.replaceAll("\\$", DOLLARESCAPE);
				}
				if (name.contains("_")) {
					name = name.replaceAll("_", UNDERSCOREESCAPE);
				}
				quantifiedVariables.add(name);
				vars[i] = name;
			}
			String firstArg = convert2String(form.sub(0), nss,
					eliminateFractions);
			if (form.op() == Quantifier.ALL) {
				String result = "(forall ";

				for (String var : vars) {
					result += var + " ";
				}
				result += ". " + firstArg;
				result += ")";
				return result;
			} else if (form.op() == Quantifier.EX) {
				String result = "(exists ";

				for (String var : vars) {
					result += var + " ";
				}
				result += ". " + firstArg;
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
