package de.uka.ilkd.key.dl.formulatools;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.Equals;
import de.uka.ilkd.key.dl.model.Greater;
import de.uka.ilkd.key.dl.model.GreaterEquals;
import de.uka.ilkd.key.dl.model.Less;
import de.uka.ilkd.key.dl.model.LessEquals;
import de.uka.ilkd.key.dl.model.Unequals;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;

public class NegationNormalFormCreator {

	public static Term createNFF(Term t) {
		return createNFF(t, false);
	}

	public static Term createNFF(Term term, boolean negated) {
		if (negated) {
			if (term.op() == Op.ALL) {
				Term nTerm = term.sub(0);
				QuantifiableVariable[] vars = new QuantifiableVariable[term
						.varsBoundHere(0).size()];
				for (int i = 0; i < term.varsBoundHere(0).size(); i++) {
					vars[i] = term.varsBoundHere(0).get(i);
				}
				return TermBuilder.DF.ex(vars, createNFF(nTerm, negated));
			} else if (term.op() == Op.EX) {
				Term nTerm = term.sub(0);
				QuantifiableVariable[] vars = new QuantifiableVariable[term
						.varsBoundHere(0).size()];
				for (int i = 0; i < term.varsBoundHere(0).size(); i++) {
					vars[i] = term.varsBoundHere(0).get(i);
				}
				return TermBuilder.DF.all(vars, createNFF(nTerm, negated));
			} else if (term.op() == Op.IMP) {
			    assert term.op() == 2;
				return TermBuilder.DF.or(createNFF(term.sub(0), !negated),
						createNFF(term.sub(1), negated));
			} else if (term.op() == Op.NOT) {
			    assert term.op() == 1;
				return createNFF(term.sub(0), !negated);
			} else if (term.op() == Op.OR) {
			    assert term.op() == 2;
				return TermBuilder.DF.and(createNFF(term.sub(0), negated),
						createNFF(term.sub(1), negated));
			} else if (term.op() == Op.AND) {
			    assert term.op() == 2;
				return TermBuilder.DF.or(createNFF(term.sub(0), negated),
						createNFF(term.sub(1), negated));
			} else if (term.op() == Op.EQV) {
			    assert term.op() == 2;
				throw new UnsupportedOperationException(
						"not yet implemented for equivalence " + Op.EQV
								+ " in " + term);
			} else if (term.op() == Op.BOX || term.op() == Op.DIA) {
				throw new UnsupportedOperationException(
						"not yet implemented for equivalence " + Op.BOX
								+ " or " + Op.DIA + " in " + term);
			} else {
				Operator op;
				if (term.op() == Op.EQUALS) {
					op = RealLDT.getFunctionFor(Unequals.class);
				} else if (term.op() == RealLDT.getFunctionFor(Greater.class)) {
					op = RealLDT.getFunctionFor(LessEquals.class);
				} else if (term.op() == RealLDT
						.getFunctionFor(GreaterEquals.class)) {
					op = RealLDT.getFunctionFor(Less.class);
				} else if (term.op() == RealLDT.getFunctionFor(Equals.class)) {
					op = RealLDT.getFunctionFor(Unequals.class);
				} else if (term.op() == RealLDT.getFunctionFor(Unequals.class)) {
					op = RealLDT.getFunctionFor(Equals.class);
				} else if (term.op() == RealLDT
						.getFunctionFor(LessEquals.class)) {
					op = RealLDT.getFunctionFor(Greater.class);
				} else if (term.op() == RealLDT.getFunctionFor(Less.class)) {
					op = RealLDT.getFunctionFor(GreaterEquals.class);
				} else {
					throw new IllegalArgumentException(
							"Don't know how to translate " + term.op()
									+ " into NFF.");
				}
				Term[] args = new Term[term.arity()];
				for (int i = 0; i < term.arity(); i++) {
					args[i] = term.sub(i);
				}
				return TermFactory.DEFAULT.createTerm(op, args,
						new ImmutableArray[0], term.javaBlock());
			}
		} else {
			if (term.op() == Op.ALL) {
				Term nTerm = term.sub(0);
				QuantifiableVariable[] vars = new QuantifiableVariable[term
						.varsBoundHere(0).size()];
				for (int i = 0; i < term.varsBoundHere(0).size(); i++) {
					vars[i] = term.varsBoundHere(0).get(i);
				}
				return TermBuilder.DF.all(vars, createNFF(nTerm, negated));
			} else if (term.op() == Op.EX) {
				Term nTerm = term.sub(0);
				QuantifiableVariable[] vars = new QuantifiableVariable[term
						.varsBoundHere(0).size()];
				for (int i = 0; i < term.varsBoundHere(0).size(); i++) {
					vars[i] = term.varsBoundHere(0).get(i);
				}
				return TermBuilder.DF.ex(vars, createNFF(nTerm, negated));
			} else if (term.op() == Op.IMP) {
			    assert term.arity() == 2;
				return TermBuilder.DF.or(createNFF(term.sub(0), !negated),
						createNFF(term.sub(1), negated));
			} else if (term.op() == Op.NOT) {
			    assert term.arity() == 1;
			    return createNFF(term.sub(0), !negated);
			} else if (term.op() == Op.OR) {
			    assert term.arity() == 2;
				return TermBuilder.DF.or(createNFF(term.sub(0), negated),
						createNFF(term.sub(1), negated));
			} else if (term.op() == Op.AND) {
			    assert term.arity() == 2;
				return TermBuilder.DF.and(createNFF(term.sub(0), negated),
						createNFF(term.sub(1), negated));
			} else if (term.op() == Op.EQV) {
			    assert term.arity() == 2;
				return TermBuilder.DF.equiv(createNFF(term.sub(0), negated),
					createNFF(term.sub(1), negated));
			} else if (term.op() == Op.BOX || term.op() == Op.DIA) {
				throw new UnsupportedOperationException(
						"not yet implemented for equivalence " + Op.BOX
								+ " or " + Op.DIA + " in " + term);
			} else {
				Term[] args = new Term[term.arity()];
				for (int i = 0; i < term.arity(); i++) {
					args[i] = term.sub(i);
				}
				return TermFactory.DEFAULT.createTerm(term.op(), args,
						new ImmutableArray[0], term.javaBlock());
			}
		}
	}
}
