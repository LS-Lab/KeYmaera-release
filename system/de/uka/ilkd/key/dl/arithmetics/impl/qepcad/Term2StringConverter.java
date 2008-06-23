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
 * Converts a given term in a string.
 * TODO: Replace String with own class (T.M.)
 * 
 * @author Timo Michelsen
 */
public class Term2StringConverter {

	/**
	 * Converts a given term in a string
	 * 
	 * @param term Term to convert
	 * @return string-rep. of the given term
	 */
	public static String convert2String( Term form ) {
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
                for (Expr e : args) {
                    boolean rational = e.numberQ();
                    if (!rational) {
                        return new Expr(DIV, args);
                    }
                }
                return new Expr(RATIONAL, args);
            } else if (f.name().toString().equals("exp")) {
                return new Expr(EXP, args);
            } else {
                try {
                    BigDecimal d = new BigDecimal(form.op().name().toString());
                    try {
                        return new Expr(d.intValueExact());
                    } catch (ArithmeticException e) {
                        return new Expr(Expr.SYM_REAL,
                                new Expr[] { new Expr(d) });
                    }
                } catch (NumberFormatException e) {
                    String name = form.op().name().toString();
                    name = name.replaceAll("_", USCORE_ESCAPE);
                    Expr expr = new Expr(Expr.SYMBOL, name);
                    if (args.length == 0) {
                        return expr;
                    }
                    return new Expr(expr, args);
                }
            }
        } else if (form.op() instanceof LogicVariable
                || form.op() instanceof de.uka.ilkd.key.logic.op.ProgramVariable
                || form.op() instanceof Metavariable) {
            String name = form.op().name().toString();
            name = name.replaceAll("_", USCORE_ESCAPE);
            return new Expr(Expr.SYMBOL, name);
        } else if (form.op() instanceof Junctor) {
            if (form.op() == Junctor.AND) {
                return new Expr(AND, args);
            } else if (form.op() == Junctor.OR) {
                return new Expr(OR, args);
            } else if (form.op() == Junctor.IMP) {
                return new Expr(IMPL, args);
            } else if (form.op() == Junctor.NOT) {
                return new Expr(NOT, args);
            }
        } else if (form.op() instanceof Quantifier) {
            Expr[] newArgs = new Expr[args.length + 1];
            System.arraycopy(args, 0, newArgs, 1, args.length);
            Expr[] vars = new Expr[form.varsBoundHere(0).size()];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = new Expr(Expr.SYMBOL, form.varsBoundHere(0)
                        .getQuantifiableVariable(i).name().toString()
                        .replaceAll("_", USCORE_ESCAPE));
            }
            newArgs[0] = new Expr(LIST, vars);
            if (form.op() == Quantifier.ALL) {
                assert args.length == 1 : "'Unary' KeY quantifier \\forall x ("
                        + args + ")";
                if (args[0].head() == FORALL) {
                    assert args[0].args().length == 2 : "'Binary' quantifier with variables and formula";
                    Expr kernel = args[0].args()[1];
                    assert args[0].args()[0].head() == LIST : "Term2ExprConverter always builds list quantifiers";
                    Expr[] innerVariables = args[0].args()[0].args();
                    // allVariables = (outer)vars + innerVariables
                    Expr[] allVariables = new Expr[innerVariables.length
                            + vars.length];
                    System.arraycopy(vars, 0, allVariables, 0, vars.length);
                    System.arraycopy(innerVariables, 0, allVariables,
                            vars.length, innerVariables.length);
                    Expr[] mergedQuant = new Expr[args.length + 1];
                    assert mergedQuant.length == 2;
                    mergedQuant[0] = new Expr(LIST, allVariables);
                    mergedQuant[1] = kernel;
                    Expr expr = new Expr(FORALL, mergedQuant);
                    assert equalsTranslateBack(QuantifierType.FORALL, expr, new Expr(FORALL, newArgs));
                    return expr;
                } else {
                    return new Expr(FORALL, newArgs);
                }
            } else if (form.op() == Quantifier.EX) {
                assert args.length == 1 : "'Unary' KeY quantifier \\exists x ("
                        + args + ")";
                if (args[0].head() == EXISTS) {
                    assert args[0].args().length == 2 : "'Binary' quantifier with variables and formula";
                    Expr kernel = args[0].args()[1];
                    assert args[0].args()[0].head() == LIST : "Term2ExprConverter always builds list quantifiers";
                    Expr[] innerVariables = args[0].args()[0].args();
                    // allVariables = (outer)vars + innerVariables
                    Expr[] allVariables = new Expr[innerVariables.length
                            + vars.length];
                    System.arraycopy(vars, 0, allVariables, 0, vars.length);
                    System.arraycopy(innerVariables, 0, allVariables,
                            vars.length, innerVariables.length);
                    Expr[] mergedQuant = new Expr[args.length + 1];
                    assert mergedQuant.length == 2;
                    mergedQuant[0] = new Expr(LIST, allVariables);
                    mergedQuant[1] = kernel;
                    Expr expr = new Expr(EXISTS, mergedQuant);
                    assert equalsTranslateBack(QuantifierType.EXISTS, expr, new Expr(EXISTS, newArgs)) : "backtranslation identity";
                    return expr;
                } else {
                    return new Expr(EXISTS, newArgs);
                }
            }
        }
        throw new IllegalArgumentException("Could not convert Term: " + form
                + "Operator was: " + form.op());
	}
}
