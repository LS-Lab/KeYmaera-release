package de.uka.ilkd.key.dl.arithmetics.impl.qepcad;

import java.math.BigDecimal;

import com.wolfram.jlink.Expr;

import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.QuantifierType;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.Junctor;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.Quantifier;

/**
 * Converts a given term in a string. TODO: Replace String with own class (T.M.)
 * 
 * @author Timo Michelsen
 */
public class Term2StringConverter {

	/**
	 * Converts a given term in a string
	 * 
	 * @param term
	 *            Term to convert
	 * @return string-rep. of the given term
	 */
	public static String convert2String(Term form) {
		String[] args = new String[form.arity()];
		for (int i = 0; i < args.length; i++) {
			args[i] = convert2String(form.sub(i));
		}
		if (form.op() == Op.FALSE) {
			return "FALSE";
		} else if (form.op() == Op.TRUE) {
			return "TRUE";
		} else if (form.op().name().toString().equals("equals")) {
			return "(" + args[0] + "=" + args[1] + ")";
		} else if (form.op() instanceof Function) {
			Function f = (Function) form.op();
			if (f.name().toString().equals("gt")) {
				return "(" + args[0] + ">" + args[1] + ")";
			} else if (f.name().toString().equals("geq")) {
				return "(" + args[0] + ">=" + args[1] + ")";
			} else if (f.name().toString().equals("equals")) {
				return "(" + args[0] + "=" + args[1] + ")"; // 2x EQUALS?
			} else if (f.name().toString().equals("neq")) {
				return "(" + args[0] + "/=" + args[1] + ")";
			} else if (f.name().toString().equals("leq")) {
				return "(" + args[0] + "<=" + args[1] + ")";
			} else if (f.name().toString().equals("lt")) {
				return "(" + args[0] + "<" + args[1] + ")";
			} else if (f.name().toString().equals("add")) {
				return "(" + args[0] + "+" + args[1] + ")";
			} else if (f.name().toString().equals("sub")) {
				return "(" + args[0] + "-" + args[1] + ")";
			} else if (f.name().toString().equals("neg")) {
				return "(-" + args[0] + ")";
			} else if (f.name().toString().equals("mul")) {
				return "(" + args[0] + "*" + args[1] + ")";
			} else if (f.name().toString().equals("div")) {
				return "(" + args[0] + "/" + args[1] + ")";
				// TODO: check
				// for (Expr e : args) {
				// boolean rational = e.numberQ();
				// if (!rational) {
				// return new Expr(DIV, args);
				// }
				// }
				// return new Expr(RATIONAL, args);
			} else if (f.name().toString().equals("exp")) {
				return "(" + args[0] + "^" + args[1] + ")";
			} else {
				try {
					BigDecimal d = new BigDecimal(form.op().name().toString());
					try {
						return String.valueOf(d.intValueExact());
					} catch (ArithmeticException e) {
						return "<TODO>"; // TODO : Change this
						// return new Expr(Expr.SYM_REAL,
						// new Expr[] { new Expr(d) });
					}
				} catch (NumberFormatException e) {
					String name = form.op().name().toString();
					if (args.length == 0) {
						return "(" + name + ")";
					}
					return "(" + name + "(" + array2String(args) + "))";
				}
			}
		} else if (form.op() instanceof LogicVariable
				|| form.op() instanceof de.uka.ilkd.key.logic.op.ProgramVariable
				|| form.op() instanceof Metavariable) {
			return form.op().name().toString();
		} else if (form.op() instanceof Junctor) {
			if (form.op() == Junctor.AND) {
				return "(" + args[0] + "/\\" + args[1] + ")";
			} else if (form.op() == Junctor.OR) {
				return "(" + args[0] + "\\/" + args[1] + ")";
			} else if (form.op() == Junctor.IMP) {
				return "(" + args[0] + "==>" + args[1] + ")";
			} else if (form.op() == Junctor.NOT) {
				return "(~" + args[0] + ")";
			}
		} else if (form.op() instanceof Quantifier) {
			if (form.op() == Quantifier.ALL) {
				return "(A )" + args[0];
			} else if (form.op() == Quantifier.EX) {
				return "(E )" + args[0];
			}
		}
		throw new IllegalArgumentException("Could not convert Term: " + form
				+ "Operator was: " + form.op());
	}

	public static String array2String(String[] args) {
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
