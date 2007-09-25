/**
 * File created 13.02.2007
 */
package de.uka.ilkd.key.dl.arithmetics.impl.mathematica;

import com.wolfram.jlink.Expr;

/**
 * Interface containing constant Expr objects for Expr commonly used while
 * performing transformations.
 * 
 * @author jdq
 * @since 13.02.2007
 * 
 */
public interface ExprConstants {

	final String USCORE_ESCAPE = "\\$uscore";

	final Expr FALSE = new Expr(Expr.SYMBOL, "False");

	final Expr TRUE = new Expr(Expr.SYMBOL, "True");

	final Expr NOT = new Expr(Expr.SYMBOL, "Not");

	final Expr RATIONAL = new Expr(Expr.SYMBOL, "Rational");

	final Expr PLUS = new Expr(Expr.SYMBOL, "Plus");

	final Expr MINUS = new Expr(Expr.SYMBOL, "Subtract");

	final Expr MINUSSIGN = new Expr(Expr.SYMBOL, "Minus");

	final Expr MULT = new Expr(Expr.SYMBOL, "Times");

	final Expr DIV = new Expr(Expr.SYMBOL, "Divide");

	final Expr EXP = new Expr(Expr.SYMBOL, "Power");

	final Expr EQUALS = new Expr(Expr.SYMBOL, "Equal");

	final Expr UNEQUAL = new Expr(Expr.SYMBOL, "Unequal");

	final Expr LESS = new Expr(Expr.SYMBOL, "Less");

	final Expr LESS_EQUALS = new Expr(Expr.SYMBOL, "LessEqual");

	final Expr GREATER = new Expr(Expr.SYMBOL, "Greater");

	final Expr GREATER_EQUALS = new Expr(Expr.SYMBOL, "GreaterEqual");

	final Expr INEQUALITY = new Expr(Expr.SYMBOL, "Inequality");

	final Expr FORALL = new Expr(Expr.SYMBOL, "ForAll");

	final Expr EXISTS = new Expr(Expr.SYMBOL, "Exists");

	final Expr AND = new Expr(Expr.SYMBOL, "And");

	final Expr OR = new Expr(Expr.SYMBOL, "Or");

	final Expr IMPL = new Expr(Expr.SYMBOL, "Implies");

	final Expr INVERSE_FUNCTION = new Expr(Expr.SYMBOL, "InverseFunction");

	final Expr INTEGRATE = new Expr(Expr.SYMBOL, "Integrate");

	final Expr RULE = new Expr(Expr.SYMBOL, "Rule");

	final Expr LIST = new Expr(Expr.SYMBOL, "List");
}
