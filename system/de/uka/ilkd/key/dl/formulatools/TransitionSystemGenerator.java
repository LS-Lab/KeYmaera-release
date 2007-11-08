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

    public static enum SpecialSymbols {
        Choice, Star;
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

        private Map<S, Map<A, S>> transitionRelation;

        private S initialState;

        private Set<S> finalStates;

        public TransitionSystem(S initialState) {
            states = new HashSet<S>();
            transitionRelation = new HashMap<S, Map<A, S>>();
            finalStates = new HashSet<S>();
            this.initialState = initialState;
            finalStates.add(initialState);
            states.add(initialState);
        }

        public void addState(S state) {
            states.add(state);
        }

        public void addTransition(S pre, A action, S post) {
            Map<A, S> map = transitionRelation.get(pre);
            if (map == null) {
                map = new HashMap<A, S>();
                transitionRelation.put(pre, map);
            }
            map.put(action, post);
        }

        public Set<S> getStates() {
            return states;
        }

        public Map<S, Map<A, S>> getTransitionRelation() {
            return transitionRelation;
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

    public TransitionSystem<Object, Object> getTransitionModel(DLProgram program) {
        TransitionSystem<Object, Object> sys = new TransitionSystem<Object, Object>(
                new Object());
        if (program instanceof CompoundDLProgram) {
            if (program instanceof Chop) {
                Chop chop = (Chop) program;
                for (ProgramElement elem : chop) {
                    TransitionSystem<Object, Object> transitionModel = getTransitionModel((DLProgram) elem);
                    for (Object s : transitionModel.getStates()) {
                        if (s != transitionModel.getInitialState()) {
                            sys.addState(s);
                        }
                        for (Object a : transitionModel.getTransitionRelation()
                                .get(s).keySet()) {
                            if (s == transitionModel.getInitialState()) {
                                for (Object fin : sys.getFinalStates()) {
                                    sys.addTransition(fin, a, transitionModel
                                            .getTransitionRelation().get(s)
                                            .get(a));
                                }
                            } else {
                                sys.addTransition(s, a, transitionModel
                                        .getTransitionRelation().get(s).get(a));
                            }
                        }
                        sys.getFinalStates().clear();
                        sys.getFinalStates().addAll(
                                transitionModel.getFinalStates());
                    }
                }
            } else if (program instanceof Choice) {
                Choice choice = (Choice) program;
                for (ProgramElement elem : choice) {
                    TransitionSystem<Object, Object> transitionModel = getTransitionModel((DLProgram) elem);
                    for (Object s : transitionModel.getStates()) {
                        sys.addState(s);
                        for (Object a : transitionModel.getTransitionRelation()
                                .get(s).keySet()) {
                            sys.addTransition(s, a, transitionModel
                                    .getTransitionRelation().get(s).get(a));
                        }
                    }
                    sys.addTransition(sys.getInitialState(),
                            SpecialSymbols.Choice, transitionModel
                                    .getInitialState());
                    sys.removeFinalState(sys.getInitialState());
                    sys.getFinalStates().addAll(
                            transitionModel.getFinalStates());
                }
            } else if (program instanceof Star) {
                sys = getTransitionModel((DLProgram) ((CompoundDLProgram) program)
                        .getChildAt(0));
                for (Object fin : sys.getFinalStates()) {
                    sys.addTransition(fin, SpecialSymbols.Star, sys
                            .getInitialState());
                }
            }
        } else {
            Object post = new Object();
            sys.addState(post);
            sys.addTransition(sys.getInitialState(), program, post);
            sys.addFinalState(post);
            sys.removeFinalState(sys.getInitialState());
        }
        return sys;
    }

}
