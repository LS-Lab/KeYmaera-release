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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedHashSet;
import java.util.List;

import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.impl.NotImpl;
import de.uka.ilkd.key.dl.model.impl.QuestImpl;
import de.uka.ilkd.key.dl.transitionmodel.TransitionSystemGenerator.SpecialSymbols;
import de.uka.ilkd.key.java.PrettyPrinter;

/**
 * This state generator is used to visualize the transition system semantics of
 * hybrid programs.
 * 
 * @author jdq
 * @since Nov 9, 2007
 * 
 */
public class DottyStateGenerator implements
		StateGenerator<DottyStateGenerator.NumberedState, List<String>> {

	public static final class NumberedState {
		private static long maxNumber = 0;

		private static long labelNumber;

		private long number;

		private long label;

		/**
		 * 
		 */
		public NumberedState() {
			this.number = maxNumber++;
			this.label = labelNumber++;
		}

		/**
		 * @return the number
		 */
		public long getNumber() {
			return number;
		}

		public LinkedHashSet<String> transitions = new LinkedHashSet<String>();

		public LinkedHashSet<String> states = new LinkedHashSet<String>();

		/**
		 * Set the number of the next label number to use
		 * 
		 * @param i
		 */
		public static void setLabelNumber(long i) {
			labelNumber = i;
		}

		/**
		 * @return the label
		 */
		public long getLabel() {
			return label;
		}

	}

	public static void generateDottyFile(Writer writer, DLProgram program)
			throws IOException {

		NumberedState.setLabelNumber(0);
		writer.write("digraph program\n");
		writer.write("{\n");
		NumberedState numberedState = new NumberedState();
		numberedState.states.add(numberedState.getNumber() + " [label=\""
				+ numberedState.getLabel() + "\", color=blue, style=fill];");
		TransitionSystem<NumberedState, List<String>> transitionModel = TransitionSystemGenerator
				.getTransitionModel(program, new DottyStateGenerator(),
						numberedState);
		for (String state : transitionModel.getFinalState().states) {
			writer.write(state + "\n");
		}
		for (String trans : transitionModel.getFinalState().transitions) {
			writer.write(trans + "\n");
		}
		writer.write("}\n");
		writer.flush();
	}

	/*@Override*/
	public List<String> generateAction(DLProgram program) {
		StringWriter writer = new StringWriter();
		try {
			program.prettyPrint(new PrettyPrinter(writer));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Collections.singletonList(writer.toString());
	}

	/*@Override*/
	public List<String> generateBranch(DLProgram program, int pos) {
		return Collections.singletonList(SpecialSymbols.CHOICE.toString());
	}

	/*@Override*/
	public List<String> generateMerge(DLProgram program, int pos) {
		return Collections.singletonList(SpecialSymbols.NOOP.toString());
	}

	/*@Override*/
	public DottyStateGenerator.NumberedState generateMergeState(
			DLProgram program, List<DottyStateGenerator.NumberedState> states) {
		NumberedState numberedState = new NumberedState();
		for (NumberedState s : states) {
			numberedState.transitions.addAll(s.transitions);
			numberedState.states.addAll(s.states);
			String string = s.getNumber() + " -> " + numberedState.getNumber()
					+ ";";
			numberedState.transitions.add(string);
		}
		return numberedState;
	}

	/*@Override*/
	public DottyStateGenerator.NumberedState getPostState(
			DottyStateGenerator.NumberedState pre, List<String> action) {
		NumberedState numberedState = new NumberedState();
		numberedState.states.add(numberedState.getNumber() + " [label=\""
				+ numberedState.getLabel() + "\"];");
		numberedState.transitions.addAll(pre.transitions);
		numberedState.states.addAll(pre.states);
		String string = pre.getNumber() + " -> " + numberedState.getNumber()
				+ " [ label=\"" + action + "\" ];";
		numberedState.transitions.add(string);
		return numberedState;
	}

	/*@Override*/
	public List<String> getSpecialSymbolNoop(NumberedState pre,
			NumberedState post) {
		String string = pre.getNumber() + " -> " + post.getNumber() + ";";
		post.transitions.add(string);
		post.transitions.addAll(pre.transitions);
		return Collections.singletonList(SpecialSymbols.NOOP.toString());
	}

	/*@Override*/
	public List<String> getSymbolForBackloop(NumberedState pre,
			NumberedState post) {
		String string = pre.getNumber() + " -> " + post.getNumber()
				+ " [ label=\"*\" ];";
		post.transitions.add(string);
		post.transitions.addAll(pre.transitions);
		post.states.addAll(pre.states);
		return Collections.singletonList(SpecialSymbols.STAR.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.transitionmodel.StateGenerator#generateElseAction(de.uka.ilkd.key.dl.model.Formula)
	 */
	/*@Override*/
	public List<String> generateElseAction(Formula f) {
		List<String> action = this
				.generateAction(new QuestImpl(new NotImpl(f)));
		return action;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.transitionmodel.StateGenerator#generateThenAction(de.uka.ilkd.key.dl.model.Formula)
	 */
	/*@Override*/
	public List<String> generateThenAction(Formula f) {
		List<String> action = this.generateAction(new QuestImpl(f));
		return action;
	}

}
