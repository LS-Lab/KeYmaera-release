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
package de.uka.ilkd.key.dl.transitionmodel;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.uka.ilkd.key.dl.rules.VisualizationRule;

/**
 * This class is used to represent a labeled transition system with states of
 * type S and labels of type A. It is e.g. used by the
 * {@link DependencyStateGenerator} to perform a variable dependency analysis or
 * the {@link VisualizationRule} that is able to visualize the transition system
 * semantics of hybrid programs.
 * 
 * @author jdq
 * @since Nov 8, 2007
 * 
 */
public class TransitionSystem<S, A> {

	private Set<S> states;

	private Map<S, Map<A, Set<S>>> transitionRelation;

	private Map<S, Map<A, Set<S>>> reverseTransitionRelation;

	private S initialState;

	private S finalState;

	public TransitionSystem(S initialState) {
		states = new LinkedHashSet<S>();
		transitionRelation = new LinkedHashMap<S, Map<A, Set<S>>>();
		reverseTransitionRelation = new LinkedHashMap<S, Map<A, Set<S>>>();
		this.initialState = initialState;
		this.finalState = initialState;
		states.add(initialState);
	}

	public void addState(S state) {
		states.add(state);
	}

	public void addAllTransitions(S pre, Map<A, Set<S>> transitions) {
		if (transitions != null) {
			for (A action : transitions.keySet()) {
				for (S post : transitions.get(action)) {
					addTransition(pre, action, post);
				}
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
			map = new LinkedHashMap<A, Set<S>>();
			transitionRelation.put(pre, map);
		}
		if (map.get(action) == null) {
			map.put(action, new LinkedHashSet<S>());
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
	 *            the finalState to set
	 */
	public void setFinalState(S finalState) {
		this.finalState = finalState;
	}

}
