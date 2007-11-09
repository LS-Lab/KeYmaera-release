package de.uka.ilkd.key.dl.transitionmodel;

import java.util.List;

import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.transitionmodel.TransitionSystemGenerator.SpecialSymbols;

/**
 * TODO jdq documentation since Nov 9, 2007 
 * @author jdq
 * @since Nov 9, 2007
 * 
 */
public class StateGeneratorAdapter implements
        StateGenerator<Object, Object> {
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
    public Object getSpecialSymbolNoop(Object o, Object ob) {
        return SpecialSymbols.NOOP;
    }

    @Override
    public Object getSpecialSymbolStar(Object pre, Object post) {
        return SpecialSymbols.STAR;
    }
}