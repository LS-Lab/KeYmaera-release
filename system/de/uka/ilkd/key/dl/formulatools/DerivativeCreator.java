package de.uka.ilkd.key.dl.formulatools;

import java.util.Map;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.Div;
import de.uka.ilkd.key.dl.model.Exp;
import de.uka.ilkd.key.dl.model.LogicalVariable;
import de.uka.ilkd.key.dl.model.Mult;
import de.uka.ilkd.key.dl.model.Plus;
import de.uka.ilkd.key.dl.model.Unequals;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.TermSymbol;

// TODO: Check if 0 and 2 are actually in the function namespace
public class DerivativeCreator {

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
				|| term.op() instanceof ProgramVariable) {
			if (variables.keySet().contains(term.op().name().toString())) {
				return variables.get(term.op().name().toString());
			} else {
				return TermBuilder.DF.func((TermSymbol) nss.functions().lookup(
						new Name("0")));
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
							term.sub(0))),
					TermBuilder.DF.func(exp, term.sub(1), TermBuilder.DF
							.func((TermSymbol) nss.lookup(new Name("2")))));
		
		} else if (term.op() == RealLDT.getFunctionFor(Unequals.class)) {
			return TermBuilder.DF.ff(); // we could also return D(a) = D(b)
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
