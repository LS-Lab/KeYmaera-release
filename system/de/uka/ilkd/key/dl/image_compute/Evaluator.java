/**
 * Evaluates expressions expressed in Term and FunctionTerm.
 *
 * @author jyn (jingyin@andrew.cmu.edu)
 * @author Andre Platzer (aplatzer)
 */

package de.uka.ilkd.key.dl.image_compute;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.Expr2TermConverter;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.Term2ExprConverter;
import de.uka.ilkd.key.dl.image_compute.NumericalActionFactory.*;
import de.uka.ilkd.key.dl.model.And;
import de.uka.ilkd.key.dl.model.Biimplies;
import de.uka.ilkd.key.dl.model.Constant;
import de.uka.ilkd.key.dl.model.Div;
import de.uka.ilkd.key.dl.model.Equals;
import de.uka.ilkd.key.dl.model.Exists;
import de.uka.ilkd.key.dl.model.Exp;
import de.uka.ilkd.key.dl.model.FreeFunction;
import de.uka.ilkd.key.dl.model.FunctionTerm;
import de.uka.ilkd.key.dl.model.Greater;
import de.uka.ilkd.key.dl.model.GreaterEquals;
import de.uka.ilkd.key.dl.model.Implies;
import de.uka.ilkd.key.dl.model.Less;
import de.uka.ilkd.key.dl.model.LessEquals;
import de.uka.ilkd.key.dl.model.Minus;
import de.uka.ilkd.key.dl.model.MinusSign;
import de.uka.ilkd.key.dl.model.Mult;
import de.uka.ilkd.key.dl.model.Not;
import de.uka.ilkd.key.dl.model.Or;
import de.uka.ilkd.key.dl.model.Plus;
import de.uka.ilkd.key.dl.model.PredicateTerm;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.dl.model.Unequals;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.*;
import de.uka.ilkd.key.logic.sort.Sort;

import java.util.*;

import com.wolfram.jlink.Expr;

import orbital.math.Real;
import orbital.math.Values;
import orbital.math.ValueFactory;
/**
 * 
 * @author jingyi ni
 * @author aplatzer
 *
 */
public class Evaluator
{

	private static final ValueFactory vf = MachValueFactory.getInstance();
	private static final Map<String, Real> NIL = new HashMap<String, Real>();
	private static final Random rand = new Random();
	
	private static final double BIGFALSE = 100000.0;

	private final SymbolAbsentHandler DF_SA_HANDLER;
	private final Services services;
	private SymbolAbsentHandler symbolAbsentHandler;

	private Evaluator(Services services)
	{
		DF_SA_HANDLER = new DefaultSymbolAbsentHandler();
		symbolAbsentHandler = DF_SA_HANDLER;
		this.services = services;
	}

	public Services getServices()
	{
		return services;
	}

	/**
	 * Controls the behavior if a symbol is not present within a set.
	 */
	public static interface SymbolAbsentHandler
	{
		public void handle(NumericalState state, String symbol);
		public boolean invoked();
	}

	/**
	 * Returns the default symbol absent handler.
	 */
	public SymbolAbsentHandler getDefaultSAHandler()
	{
		return DF_SA_HANDLER;
	}

	/**
	 * Returns a find-instance symbol absent handler.
	 */
	public static SymbolAbsentHandler getFindInstanceSAHandler(Term expr, Services services)
	{
		return new FindInstanceSymbolAbsentHandler(expr, services);
	}

	//
	// some common SymbolAbsentHandler
	//

	/**
	 * Default symbol absent handler, assigns a random value to given symbol.
	 */
	public static class DefaultSymbolAbsentHandler implements SymbolAbsentHandler
	{
		private boolean invoked = false;

		public DefaultSymbolAbsentHandler()
		{
			// empty
		}

		public void handle(NumericalState state, String symbol)
		{
			invoked = true;
			Real randReal = getRandReal();
			state.forceUpdate(symbol, randReal);
		}

		public boolean invoked()
		{
			return invoked;
		}

	}

	/**
	 * FI symbol absent handler, invokves FindInstance on some expression.
	 */
	public static class FindInstanceSymbolAbsentHandler implements SymbolAbsentHandler
	{
		// the expression we're trying to satisfy
		private Term expr;
		private boolean invoked = false;
		private Services services;

		public FindInstanceSymbolAbsentHandler(Term expr, Services services)
		{
			this.expr = expr;
			this.services = services;
		}

		public void handle(NumericalState state, String symbol)
		{
			invoked = true;
			// currently relies on Mathematica to replace known symbols
			expr = replaceKnown(expr, state);
			List<String> findInstanceResults;
			final int NUM_FI = 10;
			try {
				findInstanceResults = MathSolverManager.getCurrentCounterExampleGenerator().findMultiNumInstance(expr, NUM_FI, -1);
			} catch (Exception e) {
				throw new UnsupportedOperationException("couldn't instantiate cond " + expr);
			}
			if (findInstanceResults.size() == 0)
				throw new UnsupportedOperationException("FindInstance couldn't instantiate cond " + expr);
			String findInstanceResult = findInstanceResults.get(rand.nextInt(Math.min(NUM_FI, findInstanceResults.size())));
			StringTokenizer st = new StringTokenizer(findInstanceResult, "\n");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				StringTokenizer assign = new StringTokenizer(token, " = ");
				String nextSymbol = assign.nextToken();
				Real val = vf.valueOf(Double.parseDouble(assign.nextToken()));
				state.setSymbol(nextSymbol, val);
			}
			state.commit(new FindInstanceCommit(expr));
		}

		public boolean invoked()
		{
			return invoked;
		}

		/**
		 * Replaces occurrences of known symbols with their Real values.
		 */
		private Term replaceKnown(Term expr, NumericalState state)
		{
			Expr mathExpr = Term2ExprConverter.convert2Expr(expr);
			Expr replaced = replaceKnown(mathExpr, state);
			Map<Name, LogicVariable> quantifiedVariables =
				new HashMap<Name, LogicVariable>();
			Term result;
			try {
				result = Expr2TermConverter.convert(replaced, services.getNamespaces(), quantifiedVariables);
			} catch (Exception e) {
				throw new UnsupportedOperationException("unable to convert " + replaced);
			}
			return result;
		}

		/**
		 * Replaces occurrences of known symbols with their Real values.
		 */
		private Expr replaceKnown(Expr expr, NumericalState state)
		{
			if (expr.symbolQ()) {
				String symbol = expr.toString();
				if (state.getSymbol(symbol) != null)
					return new Expr((state.getSymbol(symbol)).doubleValue());
				else
					return expr;
			} else if (expr.numberQ()) {
				return expr;
			} else {
				Expr args[] = new Expr[expr.length()];
				for (int i = 1; i < expr.length() + 1; i++)
					args[i - 1] = replaceKnown(expr.part(i), state);
				return new Expr(expr.head(), args);
			}
		}
	}

	/**
	 * Sets the current symbol absent handler to given.
	 */
	public void setSAHandler(SymbolAbsentHandler symbolAbsentHandler)
	{
		assert(symbolAbsentHandler != null);
		this.symbolAbsentHandler = symbolAbsentHandler;
	}

	/**
	 * Returns a random real for use by application.
	 */
	public static Real getRandReal()
	{
		final int MAX_RAND = 1024;
		double randVal = rand.nextDouble() + rand.nextInt(MAX_RAND);
		if (rand.nextBoolean())
			randVal = -randVal;
		return vf.valueOf(randVal);
	}

	public static Evaluator getInstance(Services services)
	{
		return new Evaluator(services);
	}

	public Real max(Real a, Real b)
	{
		if (a.compareTo(b) > 0)
			return a;
		return b;
	}

	public Real min(Real a, Real b)
	{
		if (a.compareTo(b) < 0)
			return a;
		return b;
	}

	/**
	 * Obtains the value of a symbol from a given state.
	 */
	private Real getStateSymbol(NumericalState state, String symbol, Map<String, Real> map)
	{
		if (map.get(symbol) != null)
			return map.get(symbol);
		if (state.getSymbol(symbol) != null)
			return state.getSymbol(symbol);
		symbolAbsentHandler.handle(state, symbol);
		return state.getSymbol(symbol);
	}

	private Real readStateSymbol(NumericalState state, String symbol, Map<String, Real> map)
	{
		if (map.get(symbol) != null)
			return map.get(symbol);
		if (state.readSymbol(symbol) != null)
			return state.readSymbol(symbol);
		symbolAbsentHandler.handle(state, symbol);
		return state.readSymbol(symbol);
	}

	/**
	 * Obtains the value of a symbol from a given state.
	 */
	private Real getStateSymbol(NumericalState state, String symbol)
	{
		return getStateSymbol(state, symbol, NIL);
	}

	/**
	 * Handles evaluation errors.
	 */
	private void error(Term expr)
	{
		throw new UnsupportedOperationException("unable to evaluate expression [" +
			expr + "] of class <" + expr.getClass().getName() + "> with operator [" +
			expr.op() + "] of class <" + expr.op().getClass().getName() + ">");
	}

	/**
	 * Handles evaluation errors.
	 */
	private void error(ProgramElement progElem)
	{
		throw new UnsupportedOperationException("unable to evaluate expression [" +
			progElem + "] of class <" + progElem.getClass().getName() + ">");
	}

	//
	// the following evaluation code works on expr/cond expressed as a Term
	//

	/*
	 * top-level evaluation of Term conditions
	 */
	public boolean evalCond(NumericalState state, Term cond)
	{
		return evalCond(state, cond, NIL);
	}

	/*
	 * top-level evaluation of Term conditions
	 */
	public boolean evalCond(NumericalState state, Term cond, Map<String, Real> map)
	{
		assert(cond.sort().equals(Sort.FORMULA));
		Operator op = cond.op();
		if (op instanceof Junctor)
			return evalJunctor(state, cond, map);
		else if (op instanceof Equality)
			return evalEquality(state, cond, map);
		else if (op instanceof RigidFunction)
			return evalRigid(state, cond, map);
		else {
			error(cond);
			return false;
		}
	}


	/*
	 * evaluates conditions: NOT, AND, OR, IMP, TRUE, and FALSE
	 */
	public boolean evalJunctor(NumericalState state, Term junctor, Map<String, Real> map)
	{
		String supportedOps[] = {
			"not",
			"and",
			"or",
			"imp",
			"true",
			"false"
		};
		List<String> supportedOpsList = Arrays.asList(supportedOps);
		final int NOT_IDX = 0;
		final int AND_IDX = 1;
		final int OR_IDX = 2;
		final int IMP_IDX = 3;
		final int TRUE_IDX = 4;
		final int FALSE_IDX = 5;

		int idx = supportedOpsList.indexOf(junctor.op().toString());
		boolean args[] = new boolean[junctor.arity()];
		for (int i = 0; i < args.length; i++)
			args[i] = evalCond(state, junctor.sub(i), map);
		switch (idx) {
		case NOT_IDX:
			return !args[0];
		case AND_IDX:
			return args[0] && args[1];
		case OR_IDX:
			return args[0] || args[1];
		case IMP_IDX:
			return !args[0] || args[1];
		case TRUE_IDX:
			return true;
		case FALSE_IDX:
			return false;
		default:
			error(junctor);
			return false;
		}
	}

	/*
	 * evaluates conditions: EQUIV, and ==
	 */
	public boolean evalEquality(NumericalState state, Term equality, Map<String, Real> map)
	{
		String supportedOps[] = {
			"equiv",
			"equals"
		};
		List<String> supportedOpsList = Arrays.asList(supportedOps);
		final int EQUIV_IDX = 0;
		final int EQUALS_IDX = 1;

		int idx = supportedOpsList.indexOf(equality.op().toString());
		switch (idx) {
		case EQUIV_IDX:
			return evalCond(state, equality.sub(0), map)
				== evalCond(state, equality.sub(1), map);
		case EQUALS_IDX:
			return evalExpr(state, equality.sub(0), map)
				.equals(evalExpr(state, equality.sub(1), map));
		default:
			error(equality);
			return false;
		}
	}

	/*
	 * evaluates conditions: >, <, >=, and <=
	 */
	public boolean evalRigid(NumericalState state, Term rigid, Map<String, Real> map)
	{
		String supportedOps[] = {
			"lt",
			"gt",
			"leq",
			"geq"
		};
		List<String> supportedOpsList = Arrays.asList(supportedOps);
		final int LT_IDX = 0;
		final int GT_IDX = 1;
		final int LEQ_IDX = 2;
		final int GEQ_IDX = 3;

		int idx = supportedOpsList.indexOf(rigid.op().toString());
		Real args[] = new Real[rigid.arity()];
		for (int i = 0; i < args.length; i++)
			args[i] = evalExpr(state, rigid.sub(i), map);
		switch (idx) {
		case LT_IDX:
			return args[0].compareTo(args[1]) < 0;
		case GT_IDX:
			return args[0].compareTo(args[1]) > 0;
		case LEQ_IDX:
			return args[0].compareTo(args[1]) <= 0;
		case GEQ_IDX:
			return args[0].compareTo(args[1]) >= 0;
		default:
			error(rigid);
			return false;
		}
	}

	/*
	 * top-level evaluation of Term expressions
	 */
	public Real evalExpr(NumericalState state, Term expr)
	{
		return evalExpr(state, expr, NIL);
	}

	/*
	 * top-level evaluation of Term expressions
	 *
	 * map gets precedence over the state
	 */
	public Real evalExpr(NumericalState state, Term expr, Map<String, Real> map)
	{
		if (expr.op() instanceof RigidFunction)
			return evalArithmetic(state, expr, map);
		else if (expr.op() instanceof LocationVariable) {
			if (map.get(expr.toString()) == null)
				return getStateSymbol(state, expr.toString(), map);
			return map.get(expr.toString());
		} else if (expr.op() instanceof LogicVariable) {
			if (map.get(expr.toString()) == null)
				return getStateSymbol(state, expr.toString(), map);
			return map.get(expr.toString());
		} else {
			error(expr);
			return null;
		}
	}

	/*
	 * evaluates expressions: neg, +, -, *, /, and pow
	 */
	public Real evalArithmetic(NumericalState state, Term expr, Map<String, Real> map)
	{
		// the constant case
		if (expr.arity() == 0) {
			Real ret;
			try {
				ret = vf.valueOf(new Double(expr.toString()));
			} catch (Exception e) {
				ret = getStateSymbol(state, expr.toString(), map);
			}
			return ret;
		}
		final Real ZERO = vf.valueOf(0.0);
		String supportedOps[] = {
			"neg:R",
			"add:R",
			"sub:R",
			"mul:R",
			"div:R",
			"exp:R",	// this is really pow:R, and not exponential in the usual sense
			"Sin:R",
			"Cos:R",
			"Tan:R"
		};
		List<String> supportedOpsList = Arrays.asList(supportedOps);
		final int NEG_IDX = 0;
		final int ADD_IDX = 1;
		final int SUB_IDX = 2;
		final int MUL_IDX = 3;
		final int DIV_IDX = 4;
		final int POW_IDX = 5;
		final int SIN_IDX = 6;
		final int COS_IDX = 7;
		final int TAN_IDX = 8;

		int idx = supportedOpsList.indexOf(expr.op().toString());
		Real args[] = new Real[expr.arity()];
		for (int i = 0; i < args.length; i++)
			args[i] = evalExpr(state, expr.sub(i), map);
		switch (idx) {
		case NEG_IDX:
			return ZERO.subtract(args[0]);
		case ADD_IDX:
			return args[0].add(args[1]);
		case SUB_IDX:
			return args[0].subtract(args[1]);
		case MUL_IDX:
			return args[0].multiply(args[1]);
		case DIV_IDX:
			return args[0].divide(args[1]);
		case POW_IDX:
			return args[0].power(args[1]);
		case SIN_IDX:
			return vf.valueOf(Math.sin(args[0].doubleValue()));
		case COS_IDX:
			return vf.valueOf(Math.cos(args[0].doubleValue()));
		case TAN_IDX:
			return vf.valueOf(Math.tan(args[0].doubleValue()));
		default:
			error(expr);
			return null;
		}
	}

	//
	// the following evaluation code works on expr/cond expressed in a DLProgram element
	//
	
	/*
	 * top-level evaluation of ProgramElement conditions
	 */
	public boolean evalCond(NumericalState state, ProgramElement cond)
	{
		if (cond instanceof PredicateTerm)
			return evalPredicate(state, (PredicateTerm) cond);
		else if (cond instanceof Implies)
			return !evalCond(state, ((Implies) cond).getChildAt(0))
				|| evalCond(state, ((Implies) cond).getChildAt(1));
		else if (cond instanceof Biimplies)
			return evalCond(state, ((Biimplies) cond).getChildAt(0))
				== evalCond(state, ((Biimplies) cond).getChildAt(1));
		else if (cond instanceof And) {
			return evalCond(state, ((And) cond).getChildAt(0))
				&& evalCond(state, ((And) cond).getChildAt(1));
		} else if (cond instanceof Or)
			return evalCond(state, ((Or) cond).getChildAt(0))
				|| evalCond(state, ((Or) cond).getChildAt(1));
		else if (cond instanceof Not)
			return !evalCond(state, ((Not) cond).getChildAt(0));
		else {
			error(cond);
			return false;
		}
	}

	/*
	 * evaluates conditions: ==, !=, <, >, <=, and >=
	 */
	public boolean evalPredicate(NumericalState state, PredicateTerm predicate)
	{
		ProgramElement pred = predicate.getChildAt(0);
		if (pred instanceof Equals)
			return evalExpr(state, predicate.getChildAt(1))
				.compareTo(evalExpr(state, predicate.getChildAt(2))) == 0;
		else if (pred instanceof Unequals)
			return evalExpr(state, predicate.getChildAt(1))
				.compareTo(evalExpr(state, predicate.getChildAt(2))) != 0;
		else if (pred instanceof Less)
			return evalExpr(state, predicate.getChildAt(1))
				.compareTo(evalExpr(state, predicate.getChildAt(2))) < 0;
		else if (pred instanceof Greater)
			return evalExpr(state, predicate.getChildAt(1))
				.compareTo(evalExpr(state, predicate.getChildAt(2))) > 0;
		else if (pred instanceof LessEquals)
			return evalExpr(state, predicate.getChildAt(1))
				.compareTo(evalExpr(state, predicate.getChildAt(2))) <= 0;
		else if (pred instanceof GreaterEquals)
			return evalExpr(state, predicate.getChildAt(1))
				.compareTo(evalExpr(state, predicate.getChildAt(2))) >= 0;
		else {
			error(predicate);
			return false;
		}
	}

	/*
	 * top-level evaluation ProgramElement expressions 
	 */
	public Real evalExpr(NumericalState state, ProgramElement expr)
	{
		if (expr instanceof Constant)
			return vf.valueOf(new Double(expr.toString()));
		else if (expr instanceof FunctionTerm)
			return evalArithmetic(state, (FunctionTerm) expr);
		else if (expr instanceof ProgramVariable)
			return getStateSymbol(state, expr.toString());
		else {
			error(expr);
			return null;
		}
	}

	/*
	 * evaluates expressions: neg, +, -, *, /, and Exp
	 */
	public Real evalArithmetic(NumericalState state, FunctionTerm expr)
	{
		ProgramElement symbol = expr.getChildAt(0);
		final Real ZERO = vf.valueOf(0.0);
		if (symbol instanceof MinusSign)
			return ZERO.subtract(evalExpr(state, expr.getChildAt(1)));
		else if (symbol instanceof Plus)
			return evalExpr(state, expr.getChildAt(1))
				.add(evalExpr(state, expr.getChildAt(2)));
		else if (symbol instanceof Minus)
			return evalExpr(state, expr.getChildAt(1))
				.subtract(evalExpr(state, expr.getChildAt(2)));
		else if (symbol instanceof Mult)
			return evalExpr(state, expr.getChildAt(1))
				.multiply(evalExpr(state, expr.getChildAt(2)));
		else if (symbol instanceof Div)
			return evalExpr(state, expr.getChildAt(1))
				.divide(evalExpr(state, expr.getChildAt(2)));
		else if (symbol instanceof Exp)
			return evalExpr(state, expr.getChildAt(1))
				.power(evalExpr(state, expr.getChildAt(2)));
		else if (symbol instanceof FreeFunction) {
			return getStateSymbol(state, expr.getChildAt(0).toString());
		}
		else {
			error(expr);
			return null;
		}
	}
	
	// approximate evaluation for heuristics

	/**
	 * top-level approximate evaluation of Term conditions
	 * @return a fuzzy similarity indicator. 0 if approximately satisfied. very positive if not very satisfied. Intermediate values indicate partial success. 
         * @author aplatzer
	 */
	public double evalApproxCond(NumericalState state, Term cond, Map<String, Real> map)
	{
		assert(cond.sort().equals(Sort.FORMULA));
		Operator op = cond.op();
		if (op instanceof Junctor)
			return evalApproxJunctor(state, cond, map);
		else if (op instanceof Equality)
			return evalApproxEquality(state, cond, map);
		else if (op instanceof RigidFunction)
			return evalApproxRigid(state, cond, map);
		else {
			error(cond);
			return Double.NaN;
		}
	}

	/**
	 * top-level approximate evaluation of Term conditions
         * @author aplatzer
	 */
	public double evalApproxCond(NumericalState state, Term cond)
	{
		return evalApproxCond(state, cond, NIL);
	}

	/**
	 * evaluates conditions: NOT, AND, OR, IMP, TRUE, and FALSE
         * @author aplatzer
	 */
	public double evalApproxJunctor(NumericalState state, Term junctor, Map<String, Real> map)
	{
		String supportedOps[] = {
			"not",
			"and",
			"or",
			"imp",
			"true",
			"false"
		};
		List<String> supportedOpsList = Arrays.asList(supportedOps);
		final int NOT_IDX = 0;
		final int AND_IDX = 1;
		final int OR_IDX = 2;
		final int IMP_IDX = 3;
		final int TRUE_IDX = 4;
		final int FALSE_IDX = 5;

		int idx = supportedOpsList.indexOf(junctor.op().toString());
		double args[] = new double[junctor.arity()];
		for (int i = 0; i < args.length; i++)
			args[i] = evalApproxCond(state, junctor.sub(i), map);
		switch (idx) {
		case NOT_IDX:
		    //@todo this may not be the best heuristic
		    return Math.exp(-args[0]);
			//return 1-args[0];
		case AND_IDX:
			return Math.max(args[0],args[1]);
		case OR_IDX:
			return Math.min(args[0], args[1]);
		case IMP_IDX:
		    //@todo rather heuristic
			return (args[0]<=args[1]) ? 0 : args[0]-args[1];
		case TRUE_IDX:
			return 0;
		case FALSE_IDX:
			return BIGFALSE;
		default:
			error(junctor);
			return Double.NaN;
		}
	}

	/**
	 * evaluates conditions: EQUIV, and ==
         * @author aplatzer
	 */
	public double evalApproxEquality(NumericalState state, Term equality, Map<String, Real> map)
	{
		String supportedOps[] = {
			"equiv",
			"equals"
		};
		List<String> supportedOpsList = Arrays.asList(supportedOps);
		final int EQUIV_IDX = 0;
		final int EQUALS_IDX = 1;

		int idx = supportedOpsList.indexOf(equality.op().toString());
		switch (idx) {
		case EQUIV_IDX:
			return Math.abs(evalApproxCond(state, equality.sub(0), map) - 
				evalApproxCond(state, equality.sub(1), map));
		case EQUALS_IDX:
			return evalApproxExpr(state, equality.sub(0), map)
				.subtract(evalApproxExpr(state, equality.sub(1), map)).norm().doubleValue();
		default:
			error(equality);
			return Double.NaN;
		}
	}

	/**
	 * evaluates conditions: >, <, >=, and <=
         * @author aplatzer
	 */
	public double evalApproxRigid(NumericalState state, Term rigid, Map<String, Real> map)
	{
		String supportedOps[] = {
			"lt",
			"gt",
			"leq",
			"geq"
		};
		List<String> supportedOpsList = Arrays.asList(supportedOps);
		final int LT_IDX = 0;
		final int GT_IDX = 1;
		final int LEQ_IDX = 2;
		final int GEQ_IDX = 3;

		int idx = supportedOpsList.indexOf(rigid.op().toString());
		Real args[] = new Real[rigid.arity()];
		for (int i = 0; i < args.length; i++)
			args[i] = evalApproxExpr(state, rigid.sub(i), map);
		double cmp = args[0].subtract(args[1]).doubleValue();
		switch (idx) {
		case LT_IDX: /* fall-through */
		case LEQ_IDX:
		    return cmp <= 0 ? 0 : cmp;
		case GEQ_IDX: /* fall-through */
		case GT_IDX:
		    return cmp >= 0 ? 0 : -cmp;
		default:
			error(rigid);
			return Double.NaN;
		}
	}

	public Real evalApproxExpr(NumericalState state, Term expr, Map<String, Real> map)
	{
		if (expr.op() instanceof RigidFunction)
			return evalApproxArithmetic(state, expr, map);
		else if (expr.op() instanceof LocationVariable) {
			if (map.get(expr.toString()) == null)
				return readStateSymbol(state, expr.toString(), map);
			return map.get(expr.toString());
		} else if (expr.op() instanceof LogicVariable) {
			if (map.get(expr.toString()) == null)
				return readStateSymbol(state, expr.toString(), map);
			return map.get(expr.toString());
		} else {
			error(expr);
			return null;
		}
	}

	/*
	 * evaluates expressions: neg, +, -, *, /, and pow
         * @author aplatzer
	 */
	public Real evalApproxArithmetic(NumericalState state, Term expr, Map<String, Real> map)
	{
		// the constant case
		if (expr.arity() == 0) {
			Real ret;
			try {
				ret = vf.valueOf(new Double(expr.toString()));
			} catch (Exception e) {
				ret = readStateSymbol(state, expr.toString(), map);
			}
			return ret;
		}
		final Real ZERO = vf.valueOf(0.0);
		String supportedOps[] = {
			"neg:R",
			"add:R",
			"sub:R",
			"mul:R",
			"div:R",
			"exp:R",	// this is really pow:R, and not exponential in the usual sense
			"Sin:R",
			"Cos:R",
			"Tan:R"
		};
		List<String> supportedOpsList = Arrays.asList(supportedOps);
		final int NEG_IDX = 0;
		final int ADD_IDX = 1;
		final int SUB_IDX = 2;
		final int MUL_IDX = 3;
		final int DIV_IDX = 4;
		final int POW_IDX = 5;
		final int SIN_IDX = 6;
		final int COS_IDX = 7;
		final int TAN_IDX = 8;

		int idx = supportedOpsList.indexOf(expr.op().toString());
		Real args[] = new Real[expr.arity()];
		for (int i = 0; i < args.length; i++)
			args[i] = evalApproxExpr(state, expr.sub(i), map);
		switch (idx) {
		case NEG_IDX:
			return ZERO.subtract(args[0]);
		case ADD_IDX:
			return args[0].add(args[1]);
		case SUB_IDX:
			return args[0].subtract(args[1]);
		case MUL_IDX:
			return args[0].multiply(args[1]);
		case DIV_IDX:
			return args[0].divide(args[1]);
		case POW_IDX:
			return args[0].power(args[1]);
		case SIN_IDX:
			return vf.valueOf(Math.sin(args[0].doubleValue()));
		case COS_IDX:
			return vf.valueOf(Math.cos(args[0].doubleValue()));
		case TAN_IDX:
			return vf.valueOf(Math.tan(args[0].doubleValue()));
		default:
			error(expr);
			return null;
		}
	}
}
