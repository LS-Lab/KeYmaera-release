/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel                                *
 *   quesel@informatik.uni-oldenburg.de                                    *
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
package de.uka.ilkd.key.dl.transitionmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.uka.ilkd.key.dl.model.Choice;
import de.uka.ilkd.key.dl.model.Chop;
import de.uka.ilkd.key.dl.model.CompoundDLProgram;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.IfStatement;
import de.uka.ilkd.key.dl.model.Star;
import de.uka.ilkd.key.java.ProgramElement;

/**
 * This class is able to generate the transition system semantics of a program
 * projected depending on the given {@link StateGenerator}.
 * 
 * @author jdq
 * @since Nov 8, 2007
 * 
 */
public class TransitionSystemGenerator {

	public static enum SpecialSymbols {
		CHOICE, STAR, NOOP;
	}

	public static TransitionSystem<Object, Object> getTransitionModel(
			DLProgram program) {
		return getTransitionModel(program, new StateGeneratorAdapter(),
				new Object());
	}

	public static <S, A> TransitionSystem<S, A> getTransitionModel(
			DLProgram program, StateGenerator<S, A> stateGenerator,
			S currentState) {
		TransitionSystem<S, A> sys = new TransitionSystem<S, A>(currentState);
		if (program instanceof CompoundDLProgram) {
			if (program instanceof Chop) {
				Chop chop = (Chop) program;
				for (ProgramElement elem : chop) {
					TransitionSystem<S, A> transitionModel = getTransitionModel(
							(DLProgram) elem, stateGenerator, sys
									.getFinalState());
					boolean noTwoLoop = transitionModel
							.getReverseTransitionRelation().get(
									transitionModel.getInitialState()) == null
							|| transitionModel.getReverseTransitionRelation()
									.get(transitionModel.getInitialState())
									.get(SpecialSymbols.STAR) == null
							|| transitionModel.getReverseTransitionRelation()
									.get(transitionModel.getInitialState())
									.get(SpecialSymbols.STAR).isEmpty();
					if (!noTwoLoop) {
						noTwoLoop = true;
						// check if there is a loop in one of the final
						// states
						if (sys.getTransitionRelation()
								.get(sys.getFinalState()) != null
								&& sys.getTransitionRelation().get(
										sys.getFinalState()).get(
										SpecialSymbols.STAR) != null
								&& !sys.getTransitionRelation().get(
										sys.getFinalState()).get(
										SpecialSymbols.STAR).isEmpty()) {
							noTwoLoop = false;
						}
					}
					S finalState = sys.getFinalState();
					for (S s : transitionModel.getStates()) {
						if (noTwoLoop) {
							// initial state can be merged later
							if (s != transitionModel.getInitialState()) {
								sys.addState(s);
							}
						} else {
							sys.addState(s);
						}
						if (transitionModel.getTransitionRelation().get(s) != null) {
							for (A a : transitionModel.getTransitionRelation()
									.get(s).keySet()) {
								Set<S> postStates = transitionModel
										.getTransitionRelation().get(s).get(a);
								for (S postState : postStates) {
									if (noTwoLoop
											&& s == transitionModel
													.getInitialState()) {
										// merge initial state
										S fin = sys.getFinalState();
										if (postState == transitionModel
												.getInitialState()) {
											sys.addTransition(fin, a, fin);
											// throw new AssertionError(
											// "Self loops are not hybrid
											// programs");
										} else {
											sys
													.addTransition(fin, a,
															postState);
										}
									} else {
										if (noTwoLoop
												&& postState == transitionModel
														.getInitialState()) {
											sys.addTransition(s, a, sys
													.getFinalState());
										} else {
											sys.addTransition(s, a, postState);
										}
									}
								}
							}
						}
						sys.setFinalState(transitionModel.getFinalState());
					}
					if (!noTwoLoop) {
						// if there is a loop we need a new "label", i.e. a
						// state, where we can jump to when repeating, as
						// otherwise we could jump back to a state from which we
						// could jump further back
						sys.addTransition(finalState, stateGenerator
								.getSpecialSymbolNoop(finalState,
										transitionModel.getInitialState()),
								transitionModel.getInitialState());
					}
				}
			} else if (program instanceof Choice) {
				Choice choice = (Choice) program;
				List<S> finalStates = new ArrayList<S>();
				for (int i = 0; i < choice.getChildCount(); i++) {
					ProgramElement elem = choice.getChildAt(i);
					TransitionSystem<S, A> transitionModel = getTransitionModel(
							(DLProgram) elem, stateGenerator, sys
									.getFinalState());
					for (S s : transitionModel.getStates()) {
						sys.addState(s);
						sys.addAllTransitions(s, transitionModel
								.getTransitionRelation().get(s));
					}
					sys.addTransition(sys.getFinalState(), stateGenerator
							.generateBranch(program, i), transitionModel
							.getInitialState());
					finalStates.add(transitionModel.getFinalState());
				}
				S fin = stateGenerator.generateMergeState(program, finalStates);
				sys.setFinalState(fin);
				for (int i = 0; i < finalStates.size(); i++) {
					sys.addTransition(finalStates.get(i), stateGenerator
							.generateMerge(program, i), fin);
				}
			} else if (program instanceof Star) {
				TransitionSystem<S, A> transitionModel = getTransitionModel(
						(DLProgram) ((CompoundDLProgram) program).getChildAt(0),
						stateGenerator, sys.getInitialState());
				for (S s : transitionModel.getTransitionRelation().keySet()) {
					sys.addAllTransitions(s, transitionModel
							.getTransitionRelation().get(s));
				}
				sys.addTransition(sys.getFinalState(), stateGenerator
						.getSymbolForBackloop(sys.getFinalState(),
								transitionModel.getInitialState()),
						transitionModel.getInitialState());
				if (transitionModel.getFinalState() != sys.getFinalState()) {
					sys.addTransition(transitionModel.getFinalState(),
							stateGenerator.getSymbolForBackloop(transitionModel
									.getFinalState(), sys.getFinalState()), sys
									.getFinalState());
				}
			} else if (program instanceof IfStatement) {
				IfStatement ifS = (IfStatement) program;
				TransitionSystem<S, A> transitionModel = getTransitionModel(ifS
						.getThen(), stateGenerator, stateGenerator
						.getPostState(sys.getInitialState(), stateGenerator
								.generateThenAction(ifS.getExpression())));
				for (S s : transitionModel.getTransitionRelation().keySet()) {
					sys.addState(s);
					sys.addAllTransitions(s, transitionModel
							.getTransitionRelation().get(s));
				}
				sys.addTransition(sys.getFinalState(), stateGenerator
						.generateThenAction(ifS.getExpression()),
						transitionModel.getInitialState());
				S finalState = sys.getFinalState();
				sys.setFinalState(transitionModel.getFinalState());
				List<S> states = new ArrayList<S>();
				if (ifS.getElse() != null) {
					transitionModel = getTransitionModel(ifS.getElse(),
							stateGenerator, stateGenerator.getPostState(sys
									.getInitialState(), stateGenerator
									.generateElseAction(ifS.getExpression())));
					for (S s : transitionModel.getTransitionRelation().keySet()) {
						sys.addState(s);
						sys.addAllTransitions(s, transitionModel
								.getTransitionRelation().get(s));
					}
					sys.addTransition(finalState, stateGenerator
							.generateElseAction(ifS.getExpression()),
							transitionModel.getInitialState());
					states.add(sys.getFinalState());
					states.add(transitionModel.getFinalState());

				} else {
					S postState = stateGenerator.getPostState(sys
							.getInitialState(), stateGenerator
							.generateElseAction(ifS.getExpression()));
					sys.addState(postState);
					sys
							.addTransition(sys.getInitialState(),
									stateGenerator.generateElseAction(ifS
											.getExpression()), postState);
					states.add(postState);
					states.add(sys.getFinalState());
				}
				S generateMergeState = stateGenerator.generateMergeState(ifS,
						states);
				sys.addState(generateMergeState);
				sys.addTransition(sys.getFinalState(), stateGenerator
						.generateMerge(ifS, 1), generateMergeState);
				if (ifS.getElse() != null) {
					sys.addTransition(transitionModel.getFinalState(),
							stateGenerator.generateMerge(ifS, 2),
							generateMergeState);
				} else {
					sys.addTransition(transitionModel.getFinalState(),
							stateGenerator.getSpecialSymbolNoop(transitionModel
									.getFinalState(), generateMergeState),
							generateMergeState);
				}
				sys.setFinalState(generateMergeState);
			} else {
				throw new IllegalArgumentException(
						"Unknown composition operator "
								+ program.getClass().getName());
			}
		} else {
			S post = stateGenerator.getPostState(sys.getInitialState(),
					stateGenerator.generateAction(program));
			sys.addState(post);
			sys.addTransition(sys.getInitialState(), stateGenerator
					.generateAction(program), post);
			sys.setFinalState(post);
		}
		return sys;
	}

}
