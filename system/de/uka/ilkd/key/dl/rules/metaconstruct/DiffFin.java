/***************************************************************************
 *   Copyright (C) 2007 by Andre Platzer                                   *
 *   @informatik.uni-oldenburg.de                                          *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.exceptions.FailedComputationException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;
import de.uka.ilkd.key.dl.formulatools.DerivativeCreator;
import de.uka.ilkd.key.dl.formulatools.ReplaceVisitor;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Exists;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.Variable;
import de.uka.ilkd.key.dl.model.VariableDeclaration;
import de.uka.ilkd.key.dl.model.impl.TermFactoryImpl;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.SchemaVariable;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * @author ap
 */
public class DiffFin extends AbstractDLMetaOperator {

	public static final Name NAME = new Name("#DiffFin");

	public static class RemoveQuantifiersResult {

		private List<Formula> forms = new LinkedList<Formula>();
		private List<LogicVariable> quantifiedVariables = new LinkedList<LogicVariable>();
		private DiffSystem sys;
		private boolean changed = false;

		/**
		 * 
		 */
		public RemoveQuantifiersResult(DiffSystem sys) {
			this.sys = sys;
		}

		/**
		 * @return the sys
		 */
		public DiffSystem getSys() {
			return sys;
		}

		/**
		 * @return the quantifiedVariables
		 */
		public List<LogicVariable> getQuantifiedVariables() {
			return quantifiedVariables;
		}

	}

	public DiffFin() {
		super(NAME, 2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#sort(de.uka.ilkd.key.logic.Term[])
	 */
	/*@Override*/
	public Sort sort(Term[] term) {
		return Sort.FORMULA;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#calculate(de.uka.ilkd.key.logic.Term,
	 *      de.uka.ilkd.key.rule.inst.SVInstantiations,
	 *      de.uka.ilkd.key.java.Services)
	 */
	public Term calculate(Term term, SVInstantiations svInst, Services services) {
		final Term arg = term.sub(0);
		Term ep = term.sub(1);
		System.out.println(ep + " " + ep.op() + " " + ep.op().getClass());//XXX
		if(ep.op() instanceof SchemaVariable) {
			ep = svInst.getTermInstantiation((SchemaVariable) ep.op(), null, services);
		} else if(ep.op() instanceof LogicVariable) {
			services.getNamespaces().variables().add(ep.op());
		}
		DiffSystem system = (DiffSystem) ((StatementBlock) arg.javaBlock()
				.program()).getChildAt(0);
		Term post = arg.sub(0);
		final NamespaceSet nss = services.getNamespaces();
		try {
			if (arg.op() == Modality.DIA) {
				// TODO: build DNF and split...

				RemoveQuantifiersResult r = new RemoveQuantifiersResult(system);
				r = removeQuantifiers(nss, r);
				System.out.println(r.sys);// XXX
				Term diffFin;
				if(DLOptionBean.INSTANCE.isUseODEIndFinMethods()) {
				    diffFin = MathSolverManager.getCurrentODESolver()
				            .diffFin(r.sys, post, ep, services);
				} else {
				    diffFin = DerivativeCreator.diffFin(r.getSys(), post, ep, services);
	                diffFin = TermBuilder.DF.imp(r.getSys().getInvariant(services), diffFin);
				}
				// reintroduce the quantifiers
				Collections.reverse(r.quantifiedVariables);
				for (LogicVariable var : r.quantifiedVariables) {
					diffFin = TermBuilder.DF.all(var, diffFin);
				}
				return diffFin;
			} else {
				throw new IllegalStateException("Unknown modality " + arg.op());
			}
//		} catch (UnsolveableException e) {
//			throw new IllegalStateException(
//					"DiffFin cannot handle these equations", e);
//		} catch (FailedComputationException e) {
//			throw new IllegalStateException(
//					"DiffFin did not handle these equations", e);
		} catch (RuntimeException e) {
			throw (RuntimeException) e;
		} catch (Exception e) {
			throw (InternalError) new InternalError(e.getMessage())
					.initCause(e);
		}
	}

	/**
	 * @param system
	 * @param nss
	 * @param r
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 */
	public static RemoveQuantifiersResult removeQuantifiers(
			final NamespaceSet nss, RemoveQuantifiersResult r)
			throws InvocationTargetException, IllegalAccessException,
			InstantiationException, NoSuchMethodException {
		for (int k = 0; k < r.sys.getChildCount(); k++) {
			Formula f = (Formula) r.sys.getChildAt(k);
			// Check if top level operator is a quantifier
			if (f instanceof Exists) {
				r.changed = true;
				Exists exists = (Exists) f;

				// Get the quantified variables
				VariableDeclaration varDec = (VariableDeclaration) exists
						.getChildAt(0);
				HashMap<QuantifiableVariable, Term> map = new HashMap<QuantifiableVariable, Term>();
				// Iterate through the quantified variables
				for (int i = 1; i < varDec.getChildCount(); i++) {
					// Retrieve the name of the variable
					String string = ((Variable) varDec.getChildAt(i))
							.toString();
					// Create a new name for the substitution
					Name n = new Name(nss.getUniqueName(string, true));
					LogicVariable sym = new LogicVariable(n, RealLDT
							.getRealSort());
					nss.variables().add(sym);
					// put the variable into our replacement map
					map.put(new LogicVariable(new Name(string), RealLDT
							.getRealSort()), TermBuilder.DF.var(sym));
					r.quantifiedVariables.add(sym);
				}
				// Substitute the quantified variables by fresh ones
				Formula formulaAfterSubst = (Formula) ReplaceVisitor.convert((Formula) exists
						.getChildAt(1), map, TermFactory.getTermFactory(
						TermFactoryImpl.class, nss));
				r.forms.add(formulaAfterSubst);
				

			} else {
				r.forms.add(f);
			}
		}
		if (r.changed) {
			// Replace the DGL system by the set of simplified formulas
			r.sys = TermFactory.getTermFactory(TermFactoryImpl.class, nss)
					.createDiffSystem(r.forms);
			// clear the set of generated formulas as we have added them to the diff system
			r.forms.clear();
			r.changed = false;
			// remove the remaining quantifiers recursively
			r = removeQuantifiers(nss, r);
		}
		return r;
	}
}
