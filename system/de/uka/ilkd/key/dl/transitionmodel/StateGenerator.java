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

import java.util.List;

import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.Formula;

/**
 * A state generator is used by the {@link TransitionSystemGenerator} to project
 * the semantics for different applications.
 * 
 * @author jdq
 * @since Nov 9, 2007
 * 
 */
public interface StateGenerator<S, A> {
	public S getPostState(S pre, A action);

	public A getSymbolForBackloop(S pre, S post);

	public A getSpecialSymbolNoop(S pre, S post);

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

	public A generateThenAction(Formula f);

	public A generateElseAction(Formula f);

}
