/***************************************************************************
 *   Copyright (C) 2007 by Andr�� Platzer                                   *
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
/**
 * 
 */
package de.uka.ilkd.key.dl.strategy.features;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import de.uka.ilkd.key.dl.arithmetics.exceptions.FailedComputationException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.Expr2TermConverter.UnknownMathFunctionException;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.impl.QuantifiedImpl;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.rules.metaconstruct.ODESolve;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.QuanUpdateOperator;
import de.uka.ilkd.key.logic.sort.ProgramSVSort;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * Tests whether differential equations have first-order definable solutions.
 * Checks for algebraic non-transcendental solutions.
 * 
 * @author ap
 * 
 */
public class ODESolvableFeature implements Feature {
	/**
	 * Blacklist of transcendental functions.
	 * 
	 * @TODO instead use converse, i.e., only accept whitelist of algebraic
	 *       functions.
	 */
	private static final List<String> transcendentalList = Arrays
			.asList(new String[] {
			// @todo check non-constant Exp
					"Cos", "Sin", "Tan", "Cot", "ArcCos", "ArcSin", "ArcTan", "ArcCot", "Log", "Cosh", "Sinh", "Tanh", "ArcCosh", "ArcSinh", "ArchTanh"});
	private static final List<String> algebraicList = Arrays
			.asList(new String[] {
			// @todo check non-constant Exp
					"add", "sub", "neg", "mul", "div", "exp", });

	public static final Feature INSTANCE = new ODESolvableFeature();
	private Map<List<ProgramElement>, RuleAppCost> solvabilityCache = new WeakHashMap<List<ProgramElement>, RuleAppCost>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule
	 * .RuleApp, de.uka.ilkd.key.logic.PosInOccurrence,
	 * de.uka.ilkd.key.proof.Goal)
	 */
	public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
	    if(!DLOptionBean.INSTANCE.isSolveODE()) {
	        // option checked that says we do not want to solve ode's
	        return TopRuleAppCost.INSTANCE;
	    }
		Term term = pos.subTerm();

		// unbox from update prefix
		while (term.op() instanceof QuanUpdateOperator) {
			term = ((QuanUpdateOperator) term.op()).target(term);
		}
		if (!(term.op() instanceof Modality && term.javaBlock() != null
				&& term.javaBlock() != JavaBlock.EMPTY_JAVABLOCK && term
				.javaBlock().program() instanceof StatementBlock)) {
			throw new IllegalArgumentException("inapplicable to " + pos);
		}
        ProgramElement childAt = ((StatementBlock) term
                .javaBlock().program()).getChildAt(0);
        if(childAt instanceof QuantifiedImpl) {
            //TODO: sometimes we can solve the ODE and we should test for it here
            return TopRuleAppCost.INSTANCE;
        }
        final DiffSystem system = (DiffSystem) childAt;
		if (!ProgramSVSort.DL_SIMPLE_ORDINARY_DIFF_SYSTEM_SORT_INSTANCE
				.canStandFor(system, goal.proof().getServices())) {
			return TopRuleAppCost.INSTANCE;
		}
		final List<ProgramElement> differentialEquations = system
				.getDifferentialEquations(goal.proof().getNamespaces());
		if (differentialEquations.isEmpty()) {
			return LongRuleAppCost.ZERO_COST;
		}
		RuleAppCost cached = solvabilityCache.get(differentialEquations);
		if (cached != null) {
			return cached;
		}
		final Services services = goal.proof().getServices();
		try {
			Term result = ODESolve.ODE_SOLVE.odeSolve(term, true, services);

			final boolean[] algebraic = { true };
			result.execPreOrder(new Visitor() {
				/* @Override */
				public void visit(Term visited) {
					if (transcendentalList.contains(visited.op().name()
							.toString())) {
						algebraic[0] = false;
					}
					if (visited.op().name().toString().equals("exp")) {
						visited.sub(1).execPreOrder(new Visitor() {

							public void visit(Term visited) {
								if (visited.op().arity() == 0) {
									try {
										new BigDecimal(visited.op().name().toString());
									} catch (Exception e) {
										algebraic[0] = false;
									}
								}
							}

						});
					}
				}
			});
			if (algebraic[0]) {
				solvabilityCache.put(differentialEquations,
						LongRuleAppCost.ZERO_COST);
				return LongRuleAppCost.ZERO_COST;
			} else {
				solvabilityCache.put(differentialEquations,
						TopRuleAppCost.INSTANCE);
				return TopRuleAppCost.INSTANCE;
			}
		} catch (UnsolveableException e) {
			solvabilityCache
					.put(differentialEquations, TopRuleAppCost.INSTANCE);
			return TopRuleAppCost.INSTANCE;
		} catch (FailedComputationException e) {
			return TopRuleAppCost.INSTANCE;
		} catch (RemoteException e) {
			e.printStackTrace();
			return TopRuleAppCost.INSTANCE;
		} catch (SolverException e) {
			e.printStackTrace();
			return TopRuleAppCost.INSTANCE;
		} catch (UnknownMathFunctionException e) {
		    // the solution did contain an unknown function, we therefore consider it as unsolvable
			return TopRuleAppCost.INSTANCE;
		}
	}
}
