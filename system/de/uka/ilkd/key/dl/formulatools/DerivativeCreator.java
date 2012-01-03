package de.uka.ilkd.key.dl.formulatools;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Div;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.Equals;
import de.uka.ilkd.key.dl.model.Exp;
import de.uka.ilkd.key.dl.model.Greater;
import de.uka.ilkd.key.dl.model.GreaterEquals;
import de.uka.ilkd.key.dl.model.Less;
import de.uka.ilkd.key.dl.model.LessEquals;
import de.uka.ilkd.key.dl.model.Minus;
import de.uka.ilkd.key.dl.model.Mult;
import de.uka.ilkd.key.dl.model.Plus;
import de.uka.ilkd.key.dl.model.Minus;
import de.uka.ilkd.key.dl.model.PredicateTerm;
import de.uka.ilkd.key.dl.model.Unequals;
import de.uka.ilkd.key.dl.parser.NumberCache;
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.PolynomTool;
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.PolynomTool.BigFraction;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.TermSymbol;

// FIXME: implementation is probably unsound for 5 * 3... because that would be translated to 5*3 + 5*3
public class DerivativeCreator {

	/**
	 * The function calculates the derivative (induction) of a term based on the derivivates given by @sys
	 *
	 * note that the @sys must not contain any evolution domain
	 */
	public static final Term diffInd(DiffSystem sys, Term post, Services s) {
		HashMap<String, Term> replacements = new HashMap<String, Term>();
		collectDiffReplacements(sys, replacements, s);
		System.out.println("Replacements are: " + replacements);
		Term createNFF = NegationNormalFormCreator.createNFF(post);
		System.out.println("Formula: " + post);
		System.out.println("NFF: " + createNFF);
		return createDerivative(createNFF, replacements, s.getNamespaces(), null);
	}

	/**
	 * The function calculates the derivative (diffFin) of a term based on the derivivates given by @sys
	 *
	 * note that the @sys must not contain any evolution domain
	 */
	public static final Term diffFin(DiffSystem sys, Term post, Term epsilon, Services s) {
		HashMap<String, Term> replacements = new HashMap<String, Term>();
		collectDiffReplacements(sys, replacements, s);
		System.out.println("Replacements are: " + replacements);
		Term createNFF = NegationNormalFormCreator.createNFF(post);
		System.out.println("Formula: " + post);
		System.out.println("NFF: " + createNFF);
		return createDerivative(createNFF, replacements, s.getNamespaces(), epsilon);
	}

	/**
	 * Collect all program variables which are children of a Dot.
	 * 
	 * @param form
	 *            the current root element.
	 * 
	 * @param map
	 *            the Map used for storing the result
	 */
	public static final void collectDiffReplacements(ProgramElement form,
			Map<String, Term> map, Services services) {
		if (form instanceof PredicateTerm) {
			PredicateTerm pred = (PredicateTerm) form;
			if (pred.getChildAt(0) instanceof Equals) {
				if (pred.getChildAt(1) instanceof Dot) {
					de.uka.ilkd.key.dl.model.ProgramVariable pv = (de.uka.ilkd.key.dl.model.ProgramVariable) ((Dot) pred
							.getChildAt(1)).getChildAt(0);
					String pvName = pv.getElementName().toString();
					map.put(pvName, Prog2LogicConverter.convert(
							(DLProgramElement) pred.getChildAt(2), services));
				} else {
				    //@todo could "a=b+5" be an equation in the diff-free evolution domain constraint? Or will this not end up here?
					System.err.println("Don't know what to do with " + pred
							+ " that is occurring in a diff system");
				}
			} else {
			    //@todo could "a>=b+5" be an equation in the diff-free evolution domain constraint? Or will this not end up here?
				throw new IllegalArgumentException(
						"Don't know how to handle predicate "
								+ pred.getChildAt(0));
			}
		}
		if (form instanceof DLNonTerminalProgramElement) {
			DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) form;
			for (ProgramElement p : dlnpe) {
				collectDiffReplacements(p, map, services);
			}
		}
	}

	public static Term createDerivative(Term term, Map<String, Term> variables,
			NamespaceSet nss, Term epsilon) {
		if (term.op() == Op.ALL || term.op() == Op.EX) {
			throw new UnsupportedOperationException(
					"not yet implemented for quantifiers " + " in " + term);
		} else if (term.op() == Op.IMP) {
			throw new IllegalArgumentException("please transform the " + term
					+ " into negation normal form");
		} else if (term.op() == Op.NOT) {
			throw new IllegalArgumentException("please transform the " + term
					+ " into negation normal form");
		} else if (term.op() == Op.OR || term.op() == Op.AND) {
		    assert term.arity() == 2;
			return TermBuilder.DF.and(
					createDerivative(term.sub(0), variables, nss, epsilon),
					createDerivative(term.sub(1), variables, nss, epsilon));
		} else if (term.op() == Op.EQV) {
			throw new UnsupportedOperationException(
					"not yet implemented for equivalence " + Op.EQV + " in "
							+ term);
		} else if (term.op() == Op.BOX || term.op() == Op.DIA) {
			throw new UnsupportedOperationException(
					"not yet implemented for equivalence " + Op.BOX + " or "
							+ Op.DIA + " in " + term);
		} else if (term.op() instanceof LogicVariable
				|| term.op() instanceof QuantifiableVariable
				|| term.op() instanceof ProgramVariable || term.arity() == 0) {
			if (variables.keySet().contains(term.op().name().toString())) {
				return variables.get(term.op().name().toString());
			} else {
				return TermBuilder.DF.func(NumberCache.getNumber(
						new BigDecimal(0), RealLDT.getRealSort()));
			}
		} else if (term.op() == RealLDT.getFunctionFor(Mult.class)) {
		    assert term.arity() == 2;
			TermSymbol plus = RealLDT.getFunctionFor(Plus.class);
			TermSymbol mult = RealLDT.getFunctionFor(Mult.class);
			return TermBuilder.DF.func(
					plus,
					TermBuilder.DF.func(mult,
							createDerivative(term.sub(0), variables, nss, epsilon),
							term.sub(1)),
					TermBuilder.DF.func(mult,
							createDerivative(term.sub(1), variables, nss, epsilon),
							term.sub(0)));
			    //@todo case missing for else if (term.op() == RealLDT.getFunctionFor(Plus.class)) { 
		} else if (term.op() == RealLDT.getFunctionFor(Div.class)) {
		    assert term.arity() == 2;
		    //@todo move those "final TermSymbol" constants more global, maybe even private statical final? 
			TermSymbol plus = RealLDT.getFunctionFor(Plus.class);
			TermSymbol minus = RealLDT.getFunctionFor(Minus.class);
			TermSymbol mult = RealLDT.getFunctionFor(Mult.class);
			TermSymbol div = RealLDT.getFunctionFor(Div.class);
			TermSymbol exp = RealLDT.getFunctionFor(Exp.class);
			return TermBuilder.DF.func(div, TermBuilder.DF.func(
					minus,
					TermBuilder.DF.func(mult,
							createDerivative(term.sub(0), variables, nss, epsilon),
							term.sub(1)),
					TermBuilder.DF.func(mult,
							createDerivative(term.sub(1), variables, nss, epsilon),
							term.sub(0))),
					TermBuilder.DF.func(exp,
					term.sub(1), TermBuilder.DF.func(NumberCache.getNumber(
							new BigDecimal(2), RealLDT.getRealSort()))));
		} else if (term.op() == RealLDT.getFunctionFor(Exp.class)) {
		    assert term.arity() == 2;
			// implemented chain rule:

			TermSymbol mult = RealLDT.getFunctionFor(Mult.class);
			TermSymbol minus = RealLDT.getFunctionFor(Minus.class);
			TermSymbol exp = RealLDT.getFunctionFor(Exp.class);

			if(term.sub(0).op().name().toString().equals("1")) {
				// if the base is 1 the exponent does not matter
				return TermBuilder.DF.func(NumberCache.getNumber(new BigDecimal(1)));
			}
			Term subD = createDerivative(term.sub(0), variables, nss, epsilon);
			try {
				BigFraction frac = PolynomTool.convertStringToFraction(term.sub(1).op().name().toString());
				assert term.sub(1).arity() == 0 : "literal constants have no subterms";
				// calculate explicit derivative
				return TermBuilder.DF.func(mult, TermBuilder.DF.func(mult, term.sub(1), TermBuilder.DF.func(exp, term.sub(0), TermBuilder.DF.func(minus,
							term.sub(1), TermBuilder.DF.func(NumberCache.getNumber(new
								BigDecimal(1), RealLDT.getRealSort()))))), subD);
			} catch(Exception e) {
				throw new UnsupportedOperationException("Not implemented for polynomial exponents: " + term.sub(1));
				/* LogicVariable u = new LogicVariable(new Name("$apply$u"), RealLDT.getRealSort());
				HashMap<String, Term> nvars = new HashMap<String, Term>();
				nvars.putAll(variables);
				Term uTerm = TermBuilder.DF.var(u);
				nvars.put(u.name().toString(), uTerm);
				Term subOut = createDerivative(TermBuilder.DF.func(exp, nvars.get(u), term.sub(1)), nvars, nss, epsilon);
				return TermBuilder.DF.func(mult, apply(subOut, subD, uTerm), subD); */
			}
            
			//@todo this does only work for integer exponents not for fractions
			/*int expo = Integer.parseInt(term.sub(1).op().name().toString());
			TermSymbol div = RealLDT.getFunctionFor(Div.class);
			if(expo == 0) {
				return NumberCache.getNumber(new BigDecimal(0), RealLDT.getRealSort());
			}
			boolean divN = false;
			if(expo < 0) {
				expo = -expo;
				divN = true;
			}
			Term m = term.sub(0);
			TermSymbol mult = RealLDT.getFunctionFor(Mult.class);
			for (int i = 1; i < expo; i++) {
				m = TermBuilder.DF.func(mult, m, term.sub(0));
			}
			if(divN) {
				return createDerivative(TermBuilder.DF.func(div,
					NumberCache.getNumber(new BigDecimal(0), RealLDT.getRealSort()), m), variables, 
						nss, epsilon);
			} else {
				return createDerivative(m, variables, nss, epsilon);
			}

			*/
			// TermSymbol sub = RealLDT.getFunctionFor(Minus.class);
			// TermSymbol exp = RealLDT.getFunctionFor(Exp.class);
			// return TermBuilder.DF.func(mult, term.sub(1),
			// TermBuilder.DF.func(
			// exp, createDerivative(term.sub(0), variables, nss),
			// TermBuilder.DF.func(sub, term.sub(1), TermBuilder.DF
			// .func(NumberCache.getNumber(new BigDecimal(1),
			// RealLDT.getRealSort())))));
		} else if (term.op() == RealLDT.getFunctionFor(GreaterEquals.class)) {
		    assert term.arity() == 2;
			Function geq = RealLDT.getFunctionFor(GreaterEquals.class);
			TermSymbol plus = RealLDT.getFunctionFor(Plus.class);
			if(epsilon == null) {
				return TermBuilder.DF.func(geq, term.sub(0), term.sub(1));
			} else {
				return TermBuilder.DF.func(geq, term.sub(0), TermBuilder.DF.func(plus, term.sub(1), epsilon)); 
			}
		} else if (term.op() == RealLDT.getFunctionFor(LessEquals.class)) {
		    assert term.arity() == 2;
			Function leq = RealLDT.getFunctionFor(LessEquals.class);
			TermSymbol plus = RealLDT.getFunctionFor(Plus.class);
			if(epsilon == null) {
				return TermBuilder.DF.func(leq, term.sub(0), term.sub(1));
			} else {
				return TermBuilder.DF.func(leq, TermBuilder.DF.func(plus, term.sub(0), epsilon), term.sub(1)); 
			}
		} else if (term.op() == RealLDT.getFunctionFor(Greater.class)) {
		    assert term.arity() == 2;
			return createDerivative(
					TermBuilder.DF.func(
							RealLDT.getFunctionFor(GreaterEquals.class),
							term.sub(0), term.sub(1)), variables, nss, epsilon);
		} else if (term.op() == RealLDT.getFunctionFor(Less.class)) {
		    assert term.arity() == 2;
			return createDerivative(
					TermBuilder.DF.func(
							RealLDT.getFunctionFor(LessEquals.class),
							term.sub(0), term.sub(1)), variables, nss, epsilon);
		} else if (term.op() == RealLDT.getFunctionFor(Equals.class)) {
		    assert term.arity() == 2;
			if(epsilon == null) {
				return TermBuilder.DF.equals(term.sub(0), term.sub(1));
			} else {
				throw new IllegalArgumentException("The operator DiffFin is undefined for equalities.");
			}
		} else if (term.op() == RealLDT.getFunctionFor(Unequals.class)) {
		    assert term.arity() == 2;
			if(epsilon == null) {
				return TermBuilder.DF.equals(term.sub(0), term.sub(1));
			} else {
				throw new IllegalArgumentException("The operator DiffFin is undefined for unequalities.");
			}
		} else {
		    //@todo I'm not sure if all other cases are handled correctly. Maybe write down more explicitly and just have an "else throw Unsupported"
			Term[] args = new Term[term.arity()];
			for (int i = 0; i < term.arity(); i++) {
				args[i] = createDerivative(term.sub(i), variables, nss, epsilon);
			}
			return TermFactory.DEFAULT.createTerm(term.op(), args,
					new ImmutableArray[0], term.javaBlock());
		}
	}

	private static Term apply(Term in, Term rep, Term u) {
		if(in.equals(u)) {
			return rep;
		} else {
			Term[] subs = new Term[in.arity()];
			for(int i = 0; i < subs.length; i++) {
				subs[i] = apply(in.sub(i), rep, u);
			}
			return TermFactory.DEFAULT.createTerm(in.op(), subs, new ImmutableArray[0], in.javaBlock());
		}
	}
}
