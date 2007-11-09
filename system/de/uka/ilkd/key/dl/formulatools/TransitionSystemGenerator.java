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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
        public S getPostState(S pre, A action);

        public A getSpecialSymbolStar();

        public A getSpecialSymbolNoop();

        /**
         * TODO jdq documentation since Nov 9, 2007
         * 
         * @param program
         * @return
         */
        public A generateAction(DLProgram program);

        public A generateBranch(DLProgram program, int pos);

        public A generateMerge(DLProgram program, int pos);

        public S generateMergeState(DLProgram program, List<S> states);
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

        private S finalState;

        public TransitionSystem(S initialState) {
            states = new HashSet<S>();
            transitionRelation = new HashMap<S, Map<A, Set<S>>>();
            reverseTransitionRelation = new HashMap<S, Map<A, Set<S>>>();
            this.initialState = initialState;
            this.finalState = initialState;
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
        public S getFinalState() {
            return finalState;
        }

        /**
         * @param finalState
         *                the finalState to set
         */
        public void setFinalState(S finalState) {
            this.finalState = finalState;
        }

    }

    public TransitionSystem<Object, Object> getTransitionModel(DLProgram program) {
        return getTransitionModel(program, new StateGenerator<Object, Object>() {

            @Override
            public Object generateAction(DLProgram program) {
                return program;
            }

            @Override
            public Object generateBranch(DLProgram program, int pos) {
                return SpecialSymbols.CHOICE;
            }

            @Override
            public Object generateMerge(DLProgram program, int pos) {
                return SpecialSymbols.NOOP;
            }

            @Override
            public Object generateMergeState(DLProgram program,
                    List<Object> states) {
                return new Object();
            }

            @Override
            public Object getPostState(Object pre, Object action) {
                return new Object();
            }

            @Override
            public Object getSpecialSymbolNoop() {
                return SpecialSymbols.NOOP;
            }

            @Override
            public Object getSpecialSymbolStar() {
                return SpecialSymbols.STAR;
            }
            
        }, new Object());
    }

    public <S, A> TransitionSystem<S, A> getTransitionModel(DLProgram program,
            StateGenerator<S, A> stateGenerator, S currentState) {
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
                                    transitionModel.getInitialState()).get(
                                    SpecialSymbols.STAR) == null
                            || transitionModel.getReverseTransitionRelation()
                                    .get(transitionModel.getInitialState())
                                    .get(SpecialSymbols.STAR).isEmpty();
                    if (!noTwoLoop) {
                        noTwoLoop = true;
                        // check if there is a loop in one of the final
                        // states
                        if (sys.getTransitionRelation()
                                .get(sys.getFinalState()).get(
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
                        for (A a : transitionModel.getTransitionRelation().get(
                                s).keySet()) {
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
                                        throw new AssertionError(
                                                "Self loops are not hybrid programs");
                                    } else {
                                        sys.addTransition(fin, a, postState);
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
                        sys.setFinalState(transitionModel.getFinalState());
                    }
                    if (!noTwoLoop) {
                        // if there is a loop we need a new "label", i.e. a
                        // state, where we can jump to when repeating, as
                        // otherwise we could jump back to a state from which we
                        // could jump further back
                        sys.addTransition(finalState, stateGenerator
                                .getSpecialSymbolNoop(), transitionModel
                                .getInitialState());
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
                sys = getTransitionModel(
                        (DLProgram) ((CompoundDLProgram) program).getChildAt(0),
                        stateGenerator, sys.getInitialState());
                sys.addTransition(sys.getFinalState(), stateGenerator
                        .getSpecialSymbolStar(), sys.getInitialState());
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
