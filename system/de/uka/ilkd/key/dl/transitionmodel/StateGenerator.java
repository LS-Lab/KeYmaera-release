package de.uka.ilkd.key.dl.transitionmodel;

import java.util.List;

import de.uka.ilkd.key.dl.model.DLProgram;

/**
 * TODO jdq documentation since Nov 9, 2007
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
}