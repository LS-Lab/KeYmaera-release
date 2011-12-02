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
import de.uka.ilkd.key.dl.model.LogicalVariable;
import de.uka.ilkd.key.dl.model.Minus;
import de.uka.ilkd.key.dl.model.Mult;
import de.uka.ilkd.key.dl.model.Plus;
import de.uka.ilkd.key.dl.model.PredicateTerm;
import de.uka.ilkd.key.dl.model.Unequals;
import de.uka.ilkd.key.dl.parser.NumberCache;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.TermSymbol;


// FIXME: implementation is probably unsound for 5 * 3... because that would be translated to 5*3 + 5*3
public class DerivativeCreator {

	public static final Term diffInd(DiffSystem sys, Term post, Services s) {
		HashMap<String, Term> replacements = new HashMap<String, Term>();
		collectDiffReplacements(sys, replacements, s);
		System.out.println("Replacements are: " + replacements);
		Term createNFF = NegationNormalFormCreator.createNFF(post);
		System.out.println("Formula: " + post);
		System.out.println("NFF: " + createNFF);
		return createDerivative(createNFF, replacements, s.getNamespaces());
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
					System.err.println("Don't know what to do with " + pred
							+ " that is occurring in a diff system");
				}
			} else {
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
			NamespaceSet nss) {
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
			return TermBuilder.DF.and(
					createDerivative(term.sub(0), variables, nss),
					createDerivative(term.sub(0), variables, nss));
		} else if (term.op() == Op.EQV) {
			throw new UnsupportedOperationException(
					"not yet implemented for equivalence " + Op.EQV + " in "
							+ term);
		} else if (term.op() == Op.BOX || term.op() == Op.DIA) {
			throw new UnsupportedOperationException(
					"not yet implemented for equivalence " + Op.BOX + " or "
							+ Op.DIA + " in " + term);
		} else if (term.op() instanceof LogicalVariable
				|| term.op() instanceof QuantifiableVariable
				|| term.op() instanceof ProgramVariable
				|| term.arity() == 0) {
			if (variables.keySet().contains(term.op().name().toString())) {
				return variables.get(term.op().name().toString());
			} else {
				return TermBuilder.DF.func(NumberCache.getNumber(
						new BigDecimal(0), RealLDT.getRealSort()));
			}
		} else if (term.op() == RealLDT.getFunctionFor(Mult.class)) {
			TermSymbol plus = RealLDT.getFunctionFor(Plus.class);
			TermSymbol mult = RealLDT.getFunctionFor(Mult.class);
			return TermBuilder.DF.func(
					plus,
					TermBuilder.DF.func(mult,
							createDerivative(term.sub(0), variables, nss),
							term.sub(1)),
					TermBuilder.DF.func(mult,
							createDerivative(term.sub(1), variables, nss),
							term.sub(0)));
		} else if (term.op() == RealLDT.getFunctionFor(Div.class)) {
			TermSymbol plus = RealLDT.getFunctionFor(Plus.class);
			TermSymbol mult = RealLDT.getFunctionFor(Mult.class);
			TermSymbol div = RealLDT.getFunctionFor(Div.class);
			TermSymbol exp = RealLDT.getFunctionFor(Exp.class);
			return TermBuilder.DF.func(div, TermBuilder.DF.func(
					plus,
					TermBuilder.DF.func(mult,
							createDerivative(term.sub(0), variables, nss),
							term.sub(1)),
					TermBuilder.DF.func(mult,
							createDerivative(term.sub(1), variables, nss),
							term.sub(0))), TermBuilder.DF.func(exp,
					term.sub(1), TermBuilder.DF.func(NumberCache.getNumber(
							new BigDecimal(2), RealLDT.getRealSort()))));
		} else if (term.op() == RealLDT.getFunctionFor(Exp.class)) {
			int expo = Integer.parseInt(term.sub(1).op().name().toString());
			Term m = term.sub(0);
			TermSymbol mult = RealLDT.getFunctionFor(Mult.class);
			for(int i = 1; i < expo; i++) {
				m = TermBuilder.DF.func(mult, m, term.sub(0));
			}
			return createDerivative(m, variables, nss);
//			TermSymbol sub = RealLDT.getFunctionFor(Minus.class);
//			TermSymbol exp = RealLDT.getFunctionFor(Exp.class);
//			return TermBuilder.DF.func(mult, term.sub(1), TermBuilder.DF.func(
//					exp, createDerivative(term.sub(0), variables, nss),
//					TermBuilder.DF.func(sub, term.sub(1), TermBuilder.DF
//							.func(NumberCache.getNumber(new BigDecimal(1),
//									RealLDT.getRealSort())))));
		} else if (term.op() == RealLDT.getFunctionFor(Unequals.class)) {
			return TermBuilder.DF.ff(); // TermBuilder.DF.equals(term.sub(0),
										// term.sub(1));
		} else {
			Term[] args = new Term[term.arity()];
			for (int i = 0; i < term.arity(); i++) {
				args[i] = createDerivative(term.sub(i), variables, nss);
			}
			return TermFactory.DEFAULT.createTerm(term.op(), args,
					new ImmutableArray[0], term.javaBlock());
		}
	}
}
