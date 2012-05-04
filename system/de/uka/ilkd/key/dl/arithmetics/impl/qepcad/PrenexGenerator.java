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
package de.uka.ilkd.key.dl.arithmetics.impl.qepcad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.QuantifierType;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.SubstOp;

/**
 * Generates the prenex-form of a given Term
 * 
 * @author jdq
 */
public class PrenexGenerator {

	public static class PrenexGeneratorResult {
		private Term term;
		private List<QuantifiableVariable> variables;

		/**
		 * @param term
		 * @param variables
		 */
		public PrenexGeneratorResult(Term term,
				List<QuantifiableVariable> variables) {
			super();
			this.term = term;
			this.variables = variables;
		}

		/**
		 * @return the term
		 */
		public Term getTerm() {
			return term;
		}

		/**
		 * @return the variables
		 */
		public List<QuantifiableVariable> getVariables() {
			return variables;
		}

	}

	private static class Pair {
		QuantifierType type;
		LogicVariable var;

		/**
		 * @param type
		 * @param var
		 */
		public Pair(LogicVariable var, QuantifierType type) {
			this.type = type;
			this.var = var;
		}

	}

	public static PrenexGeneratorResult transform(Term term, NamespaceSet nss) {
		List<Pair> quantifiers = new LinkedList<Pair>();
		term = createPrenexForm(quantifiers, term, false, nss);
		Collections.reverse(quantifiers);
		List<QuantifiableVariable> vars = new ArrayList<QuantifiableVariable>();
		for (Pair p : quantifiers) {
			Term nTerm;
			switch (p.type) {
			case FORALL:
				nTerm = TermBuilder.DF.all(p.var, term);
				if(nTerm != term) {
					term = nTerm;
					vars.add(p.var);
				}
				break;
			case EXISTS:
				nTerm = TermBuilder.DF.ex(p.var, term);
				if(nTerm != term) {
					// check if the variable occurred in the term
					term = nTerm;
					vars.add(p.var);
				}
				break;
			default:
				throw new IllegalArgumentException("Unknown quantifier type: "
						+ p.type);
			}
		}

		return new PrenexGeneratorResult(term,
				vars);
	}

	/**
	 * @param quantifiers
	 * @param term
	 * @param b
	 * @return
	 */
	private static Term createPrenexForm(List<Pair> quantifiers, Term term,
			boolean negated, NamespaceSet nss) {
		if (negated) {
			if (term.op() == Op.ALL || term.op() == Op.EX) {
				Term nTerm = term.sub(0);
				for (int i = 0; i < term.varsBoundHere(0).size(); i++) {
					QuantifiableVariable quantifiableVariable = term
							.varsBoundHere(0).get(i);
					LogicVariable newVariable = getNewVariable(
							quantifiableVariable.name().toString(), nss);
					quantifiers.add(new Pair(newVariable,
							(term.op() == Op.ALL) ? QuantifierType.EXISTS
									: QuantifierType.FORALL));
					nTerm = TermFactory.DEFAULT.createSubstitutionTerm(
							Op.SUBST, quantifiableVariable, TermBuilder.DF
									.var(newVariable), nTerm);
					nTerm = ((SubstOp) nTerm.op()).apply(nTerm);
				}
				return createPrenexForm(quantifiers, nTerm, negated, nss);
			} else if (term.op() == Op.IMP) {
				return TermBuilder.DF.imp(createPrenexForm(quantifiers, term
						.sub(0), !negated, nss), createPrenexForm(quantifiers,
						term.sub(1), negated, nss));
			} else if (term.op() == Op.NOT) {
				return TermBuilder.DF.not(createPrenexForm(quantifiers, term.sub(0),
						!negated, nss));
			} else if (term.op() == Op.EQV) {
			    throw new UnsupportedOperationException("not yet implemented for equivalence " + Op.EQV + " in "+ term);
			} else {
				Term[] args = new Term[term.arity()];
				for (int i = 0; i < term.arity(); i++) {
					args[i] = createPrenexForm(quantifiers, term.sub(i),
							negated, nss);
				}
				return TermFactory.DEFAULT.createTerm(term.op(), args,
						new ImmutableArray[0], term.javaBlock());
			}
		} else {
			if (term.op() == Op.ALL || term.op() == Op.EX) {
				Term nTerm = term.sub(0);
				for (int i = 0; i < term.varsBoundHere(0).size(); i++) {
					QuantifiableVariable quantifiableVariable = term
							.varsBoundHere(0).get(i);
					LogicVariable newVariable = getNewVariable(
							quantifiableVariable.name().toString(), nss);
					quantifiers.add(new Pair(newVariable,
							(term.op() == Op.ALL) ? QuantifierType.FORALL
									: QuantifierType.EXISTS));
					nTerm = TermFactory.DEFAULT.createSubstitutionTerm(
							Op.SUBST, quantifiableVariable, TermBuilder.DF
									.var(newVariable), nTerm);
					nTerm = ((SubstOp) nTerm.op()).apply(nTerm);
				}
				return createPrenexForm(quantifiers, nTerm, negated, nss);
			} else if (term.op() == Op.IMP) {
				return TermBuilder.DF.imp(createPrenexForm(quantifiers, term
						.sub(0), !negated, nss), createPrenexForm(quantifiers,
						term.sub(1), negated, nss));
			} else if (term.op() == Op.NOT) {
				return TermBuilder.DF.not(createPrenexForm(quantifiers, term.sub(0),
						!negated, nss));
			} else if (term.op() == Op.EQV) {
			    throw new UnsupportedOperationException("not yet implemented for equivalence " + Op.EQV + " in "+ term);
			} else {
				Term[] args = new Term[term.arity()];
				for (int i = 0; i < term.arity(); i++) {
					args[i] = createPrenexForm(quantifiers, term.sub(i),
							negated, nss);
				}
				return TermFactory.DEFAULT.createTerm(term.op(), args,
						new ImmutableArray[0], term.javaBlock());
			}
		}
	}

	public static LogicVariable getNewVariable(String string,
			NamespaceSet namespaces) {
		String n2;
		int j;
		if (string.contains("_")) {
			try {
				j = Integer.parseInt(string
						.substring(string.lastIndexOf('_') + 1));
				j++;
				n2 = string.substring(0, string.lastIndexOf('_') + 1);
			} catch (NumberFormatException e) {
				n2 = string + "_";
				j = 0;
			}
		} else {
			n2 = string + "_";
			j = 0;
		}
		Name n = new Name(n2 + j);
		while (namespaces.lookup(n) != null) {
			n = new Name(n2 + ++j);
		}
		LogicVariable sym = new LogicVariable(n, RealLDT.getRealSort());
		namespaces.variables().add(sym);
		return sym;
	}

}
