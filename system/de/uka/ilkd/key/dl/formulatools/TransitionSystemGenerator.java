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
package de.uka.ilkd.key.dl.formulatools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uka.ilkd.key.dl.model.Choice;
import de.uka.ilkd.key.dl.model.Chop;
import de.uka.ilkd.key.dl.model.CompoundDLProgram;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.Star;
import de.uka.ilkd.key.java.ProgramElement;

/**
 * TODO jdq documentation since Nov 8, 2007
 * 
 * @author jdq
 * @since Nov 8, 2007
 * 
 */
public class TransitionSystemGenerator {

    /**
     * TODO jdq documentation since Nov 9, 2007
     * 
     * @author jdq
     * @since Nov 9, 2007
     * 
     */
    public interface StateGenerator<S, A> {
        public S getInitialState();

        public S getPostState(S pre, A action);

        public A getSpecialSymbolChoice();

        public A getSpecialSymbolStar();

        public A getSpecialSymbolNoop();

        /**
         * TODO jdq documentation since Nov 9, 2007
         * 
         * @param program
         * @return
         */
        public A generateAction(DLProgram program);
    }

    public static enum SpecialSymbols {
        CHOICE, STAR, NOOP;
    }

    /**
     * TODO jdq documentation since Nov 8, 2007
     * 
     * @author jdq
     * @since Nov 8, 2007
     * 
     */
    public static class TransitionSystem<S, A> {

        private Set<S> states;

        private Map<S, Map<A, Set<S>>> transitionRelation;

        private Map<S, Map<A, Set<S>>> reverseTransitionRelation;

        private S initialState;

        private Set<S> finalStates;

        public TransitionSystem(S initialState) {
            states = new HashSet<S>();
            transitionRelation = new HashMap<S, Map<A, Set<S>>>();
            reverseTransitionRelation = new HashMap<S, Map<A, Set<S>>>();
            finalStates = new HashSet<S>();
            this.initialState = initialState;
            finalStates.add(initialState);
            states.add(initialState);
        }

        public void addState(S state) {
            states.add(state);
        }

        public void addAllTransitions(S pre, Map<A, Set<S>> transitions) {
            for (A action : transitions.keySet()) {
                for (S post : transitions.get(action)) {
                    addTransition(pre, action, post);
                }
            }
        }

        public void addTransition(S pre, A action, S post) {
            addTransition(transitionRelation, pre, action, post);
            addTransition(reverseTransitionRelation, post, action, pre);
        }

        private void addTransition(Map<S, Map<A, Set<S>>> tr, S pre, A action,
                S post) {
            Map<A, Set<S>> map = tr.get(pre);
            if (map == null) {
                map = new HashMap<A, Set<S>>();
                map.put(action, new HashSet<S>());
                transitionRelation.put(pre, map);
            }
            map.get(action).add(post);
        }

        public Set<S> getStates() {
            return states;
        }

        public Map<S, Map<A, Set<S>>> getTransitionRelation() {
            return transitionRelation;
        }

        public Map<S, Map<A, Set<S>>> getReverseTransitionRelation() {
            return reverseTransitionRelation;
        }

        /**
         * @return the initialState
         */
        public S getInitialState() {
            return initialState;
        }

        /**
         * @return the finalStates
         */
        public Set<S> getFinalStates() {
            return finalStates;
        }

        /**
         * @param finalStates
         *                the finalStates to set
         */
        public void addFinalState(S finalState) {
            finalStates.add(finalState);
        }

        /**
         * @param finalStates
         *                the finalStates to set
         */
        public void removeFinalState(S finalState) {
            finalStates.remove(finalState);
        }

    }

    public <S, A> TransitionSystem<S, A> getTransitionModel(DLProgram program,
            StateGenerator<S, A> stateGenerator) {
        TransitionSystem<S, A> sys = new TransitionSystem<S, A>(stateGenerator
                .getInitialState());
        if (program instanceof CompoundDLProgram) {
            if (program instanceof Chop) {
                Chop chop = (Chop) program;
                for (ProgramElement elem : chop) {
                    TransitionSystem<S, A> transitionModel = getTransitionModel(
                            (DLProgram) elem, stateGenerator);
                    boolean noTwoLoop = transitionModel
                            .getReverseTransitionRelation().get(
                                    transitionModel.getInitialState()).get(
                                    SpecialSymbols.STAR) == null
                            || transitionModel.getReverseTransitionRelation()
                                    .get(transitionModel.getInitialState())
                                    .get(SpecialSymbols.STAR).isEmpty();
                    if (!noTwoLoop) {
                        noTwoLoop = true;
                        for (S fin : sys.getFinalStates()) {
                            // check if there is a loop in one of the final
                            // states
                            if (sys.getTransitionRelation().get(fin).get(
                                    SpecialSymbols.STAR) != null
                                    && !sys.getTransitionRelation().get(fin)
                                            .get(SpecialSymbols.STAR).isEmpty()) {
                                noTwoLoop = false;
                                break;
                            }
                        }
                    }
                    for (S s : transitionModel.getStates()) {
                        if (noTwoLoop) {
                            // initial state can be merged later
                            if (s != transitionModel.getInitialState()) {
                                sys.addState(s);
                            }
                        } else {
                            sys.addState(s);
                        }
                        for (A a : transitionModel.getTransitionRelation().get(
                                s).keySet()) {
                            Set<S> postStates = transitionModel
                                    .getTransitionRelation().get(s).get(a);
                            for (S postState : postStates) {
                                if (noTwoLoop
                                        && s == transitionModel
                                                .getInitialState()) {
                                    //merge initial state
                                    for (S fin : sys.getFinalStates()) {
                                        if (postState == transitionModel
                                                .getInitialState()) {
                                            throw new AssertionError("Self loops are not hybrid programs");
                                        } else {
                                            sys
                                                    .addTransition(fin, a,
                                                            postState);
                                        }
                                    }
                                } else {
                                    if (noTwoLoop
                                            && postState == transitionModel
                                                    .getInitialState()) {
                                        for (S fin : sys.getFinalStates()) {
                                            sys.addTransition(s, a, fin);
                                        }
                                    } else {
                                        sys.addTransition(s, a, postState);
                                    }
                                }
                            }
                        }
                    }
                    if (!noTwoLoop) {
                        // if there is a loop we need a new "label", i.e. a
                        // state, where we can jump to when repeating, as
                        // otherwise we could jump back to a state from which we
                        // could jump further back
                        for (S fin : sys.getFinalStates()) {
                            sys.addTransition(fin, stateGenerator
                                    .getSpecialSymbolNoop(), transitionModel
                                    .getInitialState());
                        }
                    }
                    sys.getFinalStates().clear();
                    sys.getFinalStates().addAll(
                            transitionModel.getFinalStates());
                }
            } else if (program instanceof Choice) {
                Choice choice = (Choice) program;
                Set<S> finalStates = new HashSet<S>();
                for (ProgramElement elem : choice) {
                    TransitionSystem<S, A> transitionModel = getTransitionModel(
                            (DLProgram) elem, stateGenerator);
                    for (S s : transitionModel.getStates()) {
                        sys.addState(s);
                        sys.addAllTransitions(s, transitionModel
                                .getTransitionRelation().get(s));
                    }
                    for (S fin : sys.getFinalStates()) {
                        sys.addTransition(fin, stateGenerator
                                .getSpecialSymbolChoice(), transitionModel
                                .getInitialState());
                    }
                    finalStates.addAll(transitionModel.getFinalStates());
                }
                sys.getFinalStates().clear();
                sys.getFinalStates().addAll(finalStates);
            } else if (program instanceof Star) {
                sys = getTransitionModel(
                        (DLProgram) ((CompoundDLProgram) program).getChildAt(0),
                        stateGenerator);
                for (S fin : sys.getFinalStates()) {
                    sys.addTransition(fin, stateGenerator
                            .getSpecialSymbolStar(), sys.getInitialState());
                }
            }
        } else {
            S post = stateGenerator.getPostState(sys.getInitialState(),
                    stateGenerator.generateAction(program));
            sys.addState(post);
            sys.addTransition(sys.getInitialState(), stateGenerator
                    .generateAction(program), post);
            sys.addFinalState(post);
            sys.removeFinalState(sys.getInitialState());
        }
        return sys;
    }

}
