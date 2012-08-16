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
// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License.
// See LICENSE.TXT for details.
//
/*
 * Created on 22.12.2004
 */
package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;
import java.util.TreeSet;

import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.transitionmodel.DependencyState;
import de.uka.ilkd.key.dl.transitionmodel.DependencyStateGenerator;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.VariableNamer;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.AbstractMetaOperator;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.logic.op.SVSubstitute;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.MatchConditions;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * Creates a universal closure with respect to the modifies set of a modality.
 * The first argument contains the modality [a]F determining the modifies set.
 * The second argument contains the actual formula to wrap inside the universal
 * closure.
 */
public class DLUniversalClosureOp extends AbstractMetaOperator {

	public DLUniversalClosureOp() {
		super(new Name("#dlUniversalClosure"), 3);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.logic.op.MetaOperator#calculate(de.uka.ilkd.key.logic
	 * .Term, de.uka.ilkd.key.rule.inst.SVInstantiations,
	 * de.uka.ilkd.key.java.Services)
	 */
	@SuppressWarnings("unchecked")
	public Term calculate(Term term, SVInstantiations svInst, Services services) {
		Term searchIn = term.sub(0);
		Term post = term.sub(1);
		DLProgram program = null;
		if (searchIn.op() == Op.GAME) {
			program = collectProgram(searchIn.sub(0), services.getNamespaces());
		} else if (searchIn.op() instanceof Modality)
			program = (DLProgram) ((StatementBlock) searchIn.javaBlock()
					.program()).getChildAt(0);
		if (program == null) {
			throw new IllegalArgumentException("inapplicable");
		}
		Term optimizeWrites = term.sub(2);
		boolean optimize = false;
		if (optimizeWrites.equals(TermBuilder.DF.tt())) {
			optimize = true;
		} else if (optimizeWrites.equals(TermBuilder.DF.ff())) {
			optimize = false;
		} else {
			assert false : "Invalid argument to DLUniversalClosure: "
					+ optimizeWrites;
		}
		return universalClosure(program, post, svInst, services, optimize);
	}

	/**
	 * @param searchIn
	 * @return TODO documentation since Dec 19, 2011
	 */
	private DLProgram collectProgram(Term searchIn, NamespaceSet nss) {
		if (searchIn.op() instanceof Modality) {
			return (DLProgram) ((StatementBlock) searchIn.javaBlock().program())
					.getChildAt(0);
		} else if (searchIn.op() == Op.ALOOP || searchIn.op() == Op.ELOOP) {
			return collectProgram(searchIn.sub(0), nss);
		} else if (searchIn.op() == Op.CUPGAME || searchIn.op() == Op.CAPGAME) {
			try {
				return TermFactory.getTermFactory(
						DLOptionBean.INSTANCE.getTermFactoryClass(), nss)
						.createChoice(collectProgram(searchIn.sub(0), nss),
								collectProgram(searchIn.sub(1), nss));
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (searchIn.op() == Op.SEQGAME) {
			try {
				return TermFactory.getTermFactory(
						DLOptionBean.INSTANCE.getTermFactoryClass(), nss)
						.createChop(collectProgram(searchIn.sub(0), nss),
								collectProgram(searchIn.sub(1), nss));
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public Term universalClosure(DLProgram program, Term post,
			SVInstantiations svInst, Services services, boolean optimizeWrites) {
		DependencyState depState = DependencyStateGenerator
				.generateDependencyMap(program);
		LinkedHashMap<de.uka.ilkd.key.dl.model.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable>> generateDependencyMap = depState
				.getDependencies();
		final LinkedHashSet<String> variablesInPost = new LinkedHashSet<String>();
		post.execPreOrder(new Visitor() {

			/*@Override*/
			public void visit(Term visited) {
				if (visited.op() instanceof ProgramVariable) {
					variablesInPost.add(((ProgramVariable) visited.op()).name()
							.toString());
				}
			}

		});
		try {
			FileWriter writer = new FileWriter("/tmp/depgraph.dot");

			writer.write("digraph program\n");
			writer.write("{\n");
			for (de.uka.ilkd.key.dl.model.ProgramVariable var : generateDependencyMap
					.keySet()) {
				writer.write(var.getElementName().toString() + ";\n");
			}
			for (de.uka.ilkd.key.dl.model.ProgramVariable var : generateDependencyMap
					.keySet()) {
				LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable> deps = generateDependencyMap
						.get(var);
				for (de.uka.ilkd.key.dl.model.ProgramVariable dvar : deps) {
					writer.write(dvar.getElementName().toString() + " -> "
							+ var.getElementName().toString() + ";\n");
				}
			}

			writer.write("}\n");
			writer.flush();
			writer.close();
		} catch (IOException ignore) {
			/*
			 * System.err.println("could not create /tmp/depgraph.dot " +
			 * ignore);
			 */
		}

		final LinkedHashMap<de.uka.ilkd.key.dl.model.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable>> transitiveClosure = DependencyStateGenerator
				.createTransitiveClosure(generateDependencyMap);

		final LinkedHashMap<de.uka.ilkd.key.dl.model.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable>> inverseTransitiveClosure = new LinkedHashMap<de.uka.ilkd.key.dl.model.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable>>();
		for (de.uka.ilkd.key.dl.model.ProgramVariable var : generateDependencyMap
				.keySet()) {
			LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable> deps = transitiveClosure
					.get(var);
			for (de.uka.ilkd.key.dl.model.ProgramVariable v : deps) {
				LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable> set = inverseTransitiveClosure
						.get(v);
				if (set == null) {
					inverseTransitiveClosure
							.put(
									v,
									new LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable>());
					assert inverseTransitiveClosure.get(v) != null;
				}
				inverseTransitiveClosure.get(v).add(var);
			}
		}
		System.out.println(inverseTransitiveClosure.keySet());
		Comparator<de.uka.ilkd.key.dl.model.ProgramVariable> comparator = new Comparator<de.uka.ilkd.key.dl.model.ProgramVariable>() {

			/*@Override*/
			public int compare(de.uka.ilkd.key.dl.model.ProgramVariable o1,
					de.uka.ilkd.key.dl.model.ProgramVariable o2) {
				int size = inverseTransitiveClosure.get(o1).size();
				int size2 = inverseTransitiveClosure.get(o2).size();
				if (size == size2) {
					return o1.getElementName().toString().compareTo(
							o2.getElementName().toString());
				} else {
					return size2 - size;
				}
			}

		};
		PriorityQueue<de.uka.ilkd.key.dl.model.ProgramVariable> variableOrder = new PriorityQueue<de.uka.ilkd.key.dl.model.ProgramVariable>(
				inverseTransitiveClosure.size() + 1, comparator);
		variableOrder.addAll(inverseTransitiveClosure.keySet());
		ArrayList<de.uka.ilkd.key.dl.model.ProgramVariable> programVariables = new ArrayList<de.uka.ilkd.key.dl.model.ProgramVariable>();
		while (!variableOrder.isEmpty()) {
			// max is the maximal element, i.e. the element on which most
			// variables depend
			de.uka.ilkd.key.dl.model.ProgramVariable max = variableOrder.poll();
			int i = 0;
			for (i = 0; i < programVariables.size(); i++) {
				if (transitiveClosure.get(programVariables.get(i)) != null
						&& transitiveClosure.get(programVariables.get(i))
								.contains(max)) {
					break;
				}
			}
			programVariables.add(i, max);
			TreeSet<de.uka.ilkd.key.dl.model.ProgramVariable> orderedDeps = new TreeSet<de.uka.ilkd.key.dl.model.ProgramVariable>(
					new Comparator<de.uka.ilkd.key.dl.model.ProgramVariable>() {

						/*@Override*/
						public int compare(
								de.uka.ilkd.key.dl.model.ProgramVariable o1,
								de.uka.ilkd.key.dl.model.ProgramVariable o2) {
							LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable> linkedHashSet = transitiveClosure
									.get(o1);
							LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable> linkedHashSet2 = transitiveClosure
									.get(o2);

							if (linkedHashSet.contains(o2)) {
								return 1;
							} else if (linkedHashSet2.contains(o1)) {
								return -1;
							}
							// this could cause interleaving
							LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable> set = inverseTransitiveClosure
									.get(o1);
							LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable> set2 = inverseTransitiveClosure
									.get(o2);
							if (set != null && set2 != null) {
								int size = set.size();
								int size2 = set2.size();
								if (size != size2) {
									return size - size2;
								}
							}
							return o1.getElementName().toString().compareTo(
									o2.getElementName().toString());

						}

					});
			// the elements that reference "max" also include those elements
			// referencing an element that references "max"
			LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable> backwardDeps = inverseTransitiveClosure
					.get(max);
			orderedDeps.addAll(backwardDeps);

			for (de.uka.ilkd.key.dl.model.ProgramVariable var : orderedDeps) {
				if (!programVariables.contains(var)) {
					for (i = 0; i < programVariables.size(); i++) {
						if ((transitiveClosure.get(programVariables.get(i)) != null && transitiveClosure
								.get(programVariables.get(i)).contains(var))
								|| (inverseTransitiveClosure.get(var) != null && inverseTransitiveClosure
										.get(var).contains(
												programVariables.get(i)))) {
							break;
						}
					}
					programVariables.add(i, var);
					if (variableOrder.contains(var)) {
						// the check if the variable is contained is necessary
						// because java 1.5 calls the comparator, which then
						// causes a null pointer exception while comparing the
						// variable not contained with the first var in the set
						variableOrder.remove(var);
					}
				}
			}
			// find out if need to recreate variableOrder due to isReferencedBy
		}

		// add variables without dependencies
		TreeSet<de.uka.ilkd.key.dl.model.ProgramVariable> freeVars = new TreeSet<de.uka.ilkd.key.dl.model.ProgramVariable>(
				new Comparator<de.uka.ilkd.key.dl.model.ProgramVariable>() {

					/*@Override*/
					public int compare(
							de.uka.ilkd.key.dl.model.ProgramVariable o1,
							de.uka.ilkd.key.dl.model.ProgramVariable o2) {
						return o1.getElementName().toString().compareTo(
								o2.getElementName().toString());
					}

				});

		for (de.uka.ilkd.key.dl.model.ProgramVariable var : transitiveClosure
				.keySet()) {
			if (!programVariables.contains(var)) {
				freeVars.add(var);
			}
		}
		programVariables.addAll(freeVars);

		Collections.reverse(programVariables);

		// Set<String> programVariables = ProgramVariableCollector.INSTANCE
		// .getProgramVariables(term.sub(0));
		for (de.uka.ilkd.key.dl.model.ProgramVariable pvar : programVariables) {
			if (transitiveClosure.keySet().contains(pvar)
					&& (!optimizeWrites
							|| variablesInPost.contains(pvar.getElementName()
									.toString()) || !(depState
							.getWriteBeforeReadList().get(pvar) != null && depState
							.getWriteBeforeReadList().get(pvar)))) {
				String name = pvar.getElementName().toString();
				LogicVariable var = searchFreeVar(services, name);
				if(pvar instanceof de.uka.ilkd.key.logic.op.ProgramVariable) {
					post = TermBuilder.DF.all(var, de.uka.ilkd.key.logic.TermFactory.DEFAULT
							.createUpdateTerm(TermBuilder.DF
									.var((ProgramVariable) pvar),
									TermBuilder.DF.var(var), post));
				} else {
					post = TermBuilder.DF.all(var, de.uka.ilkd.key.logic.TermFactory.DEFAULT
							.createUpdateTerm(TermBuilder.DF
									.var((ProgramVariable) services.getNamespaces()
											.lookup(new Name(name))),
									TermBuilder.DF.var(var), post));
				}
			}
		}
		return post;

	}

	/**
	 * Search a new variable name for the given location variable TODO jdq maybe
	 * we should use the {@link VariableNamer}
	 * 
	 * @param services
	 *            the services to access the namespaces
	 * @param loc
	 *            the name of the previous variable
	 * @return a new programvariable with a fresh name
	 */
	private LogicVariable searchFreeVar(Services services, String loc) {
		return new LogicVariable(new Name(services.getNamespaces().getUniqueName(loc)), RealLDT.getRealSort());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.logic.op.Operator#validTopLevel(de.uka.ilkd.key.logic
	 * .Term)
	 */
	public boolean validTopLevel(Term term) {
		return term.arity() == arity() && term.sub(1).sort() == Sort.FORMULA;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.logic.op.Operator#sort(de.uka.ilkd.key.logic.Term[])
	 */
	public Sort sort(Term[] term) {
		return term[1].sort();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.logic.op.Operator#isRigid(de.uka.ilkd.key.logic.Term)
	 */
	public boolean isRigid(Term term) {
		return false;
	}

	/**
	 * (non-Javadoc) by default meta operators do not match anything
	 * 
	 * @see de.uka.ilkd.key.logic.op.Operator#match(SVSubstitute,
	 *      de.uka.ilkd.key.rule.MatchConditions, de.uka.ilkd.key.java.Services)
	 */
	public MatchConditions match(SVSubstitute subst, MatchConditions mc,
			Services services) {
		return null;
	}
}
