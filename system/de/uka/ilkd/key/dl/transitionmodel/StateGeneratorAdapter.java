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
import de.uka.ilkd.key.dl.model.impl.NotImpl;
import de.uka.ilkd.key.dl.model.impl.QuestImpl;
import de.uka.ilkd.key.dl.transitionmodel.TransitionSystemGenerator.SpecialSymbols;

/**
 * TODO jdq documentation since Nov 9, 2007
 * 
 * @author jdq
 * @since Nov 9, 2007
 * 
 */
public class StateGeneratorAdapter implements StateGenerator<Object, Object> {
    /*@Override*/
    public Object generateAction(DLProgram program) {
        return program;
    }

    /*@Override*/
    public Object generateBranch(DLProgram program, int pos) {
        return SpecialSymbols.CHOICE;
    }

    /*@Override*/
    public Object generateMerge(DLProgram program, int pos) {
        return SpecialSymbols.NOOP;
    }

    /*@Override*/
    public Object generateMergeState(DLProgram program, List<Object> states) {
        return new Object();
    }

    /*@Override*/
    public Object getPostState(Object pre, Object action) {
        return new Object();
    }

    /*@Override*/
    public Object getSpecialSymbolNoop(Object o, Object ob) {
        return SpecialSymbols.NOOP;
    }

    /*@Override*/
    public Object getSymbolForBackloop(Object pre, Object post) {
        return SpecialSymbols.STAR;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.transitionmodel.StateGenerator#generateElseAction(de.uka.ilkd.key.dl.model.Formula,
     *      java.lang.Object, java.lang.Object)
     */
    /*@Override*/
    public Object generateElseAction(Formula f) {
        return this.generateAction(new QuestImpl(f));
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.transitionmodel.StateGenerator#generateThenAction(de.uka.ilkd.key.dl.model.Formula,
     *      java.lang.Object, java.lang.Object)
     */
    /*@Override*/
    public Object generateThenAction(Formula f) {
        return this.generateAction(new QuestImpl(new NotImpl(f)));
    }
}
