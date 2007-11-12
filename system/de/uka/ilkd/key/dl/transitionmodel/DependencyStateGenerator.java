package de.uka.ilkd.key.dl.transitionmodel;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import de.uka.ilkd.key.dl.model.Assign;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.java.ProgramElement;

/**
 * TODO jdq documentation since Nov 9, 2007
 * 
 * @author jdq
 * @since Nov 9, 2007
 * 
 */
public class DependencyStateGenerator
        implements
        StateGenerator<Map<ProgramVariable, LinkedHashSet<ProgramVariable>>, DLProgram> {

    public static Map<ProgramVariable, LinkedHashSet<ProgramVariable>> generateDependencyMap(
            DLProgram program) {
        TransitionSystem<Map<ProgramVariable, LinkedHashSet<ProgramVariable>>, DLProgram> transitionModel = TransitionSystemGenerator
                .getTransitionModel(program, new DependencyStateGenerator(),
                        new HashMap<ProgramVariable, LinkedHashSet<ProgramVariable>>());
        return transitionModel.getFinalState();
    }
    
    /**
     * TODO jdq documentation since Nov 12, 2007 
     * @param action
     * @return
     */
    private static Map<? extends ProgramVariable, ? extends LinkedHashSet<ProgramVariable>> getDependencies(
            DLProgram action) {
        HashMap<ProgramVariable, LinkedHashSet<ProgramVariable>> result = new HashMap<ProgramVariable, LinkedHashSet<ProgramVariable>>();
        
        if(action instanceof Assign) {
            Assign ass = (Assign) action;
            LinkedHashSet<ProgramVariable> vars = new LinkedHashSet<ProgramVariable>();
            vars.addAll(getAllVariables(ass.getChildAt(1)));
            ProgramElement childAt = ass.getChildAt(0);
            if(childAt instanceof ProgramVariable) {
                LinkedHashSet<ProgramVariable> set = result.get(childAt);
                if(set == null) {
                    set = new LinkedHashSet<ProgramVariable>();
                    result.put((ProgramVariable) childAt, vars);
                } 
                set.addAll(vars);
                set.remove(childAt); //variables should not depend on themselves
            } else {
                throw new IllegalArgumentException("Dont know how to assign something to " + childAt);
            }
        } else if(action instanceof DiffSystem){
            // handle differential equation system
            DiffSystem system = (DiffSystem) action;
            for(ProgramElement elem: system) {
                LinkedHashSet<ProgramVariable> allVariables = getAllVariables(elem);
                for(ProgramVariable pv: getDottedVariables(elem)) {
                    LinkedHashSet<ProgramVariable> set = result.get(pv);
                    if(set == null) {
                        set = new LinkedHashSet<ProgramVariable>();
                        result.put(pv, set);
                    }
                    set.addAll(allVariables);
                    set.remove(pv); //variables should not depend on themselves
                }
            }
        }
        
        return result;
    }

    /**
     * TODO jdq documentation since Nov 12, 2007 
     * @param elem
     */
    private static LinkedHashSet<ProgramVariable> getDottedVariables(ProgramElement childAt) {
        LinkedHashSet<ProgramVariable> vars = new LinkedHashSet<ProgramVariable>();
        if(childAt instanceof Dot) {
            vars.add((ProgramVariable) ((Dot) childAt).getChildAt(0));
        } else if(childAt instanceof DLNonTerminalProgramElement){
            DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) childAt;
            for(ProgramElement elem: dlnpe) {
                vars.addAll(getDottedVariables(elem));
            }
        }
        return vars;
    }

    /**
     * TODO jdq documentation since Nov 12, 2007 
     * @param childAt
     * @return
     */
    private static LinkedHashSet<ProgramVariable> getAllVariables(
            ProgramElement childAt) {
        LinkedHashSet<ProgramVariable> vars = new LinkedHashSet<ProgramVariable>();
        if(childAt instanceof ProgramVariable) {
            vars.add((ProgramVariable) childAt);
        } else if(childAt instanceof DLNonTerminalProgramElement){
            DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) childAt;
            for(ProgramElement elem: dlnpe) {
                vars.addAll(getAllVariables(elem));
            }
        }
        return vars;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.transitionmodel.StateGenerator#generateMergeState(de.uka.ilkd.key.dl.model.DLProgram,
     *      java.util.List)
     */
    @Override
    public Map<ProgramVariable, LinkedHashSet<ProgramVariable>> generateMergeState(
            DLProgram program,
            List<Map<ProgramVariable, LinkedHashSet<ProgramVariable>>> states) {
        Map<ProgramVariable, LinkedHashSet<ProgramVariable>> post = new HashMap<ProgramVariable, LinkedHashSet<ProgramVariable>>();
        for(Map<ProgramVariable, LinkedHashSet<ProgramVariable>> state: states) {
            post.putAll(state);
        }
        return post;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.transitionmodel.StateGenerator#getPostState(java.lang.Object,
     *      java.lang.Object)
     */
    @Override
    public Map<ProgramVariable, LinkedHashSet<ProgramVariable>> getPostState(
            Map<ProgramVariable, LinkedHashSet<ProgramVariable>> pre,
            DLProgram action) {
        Map<ProgramVariable, LinkedHashSet<ProgramVariable>> post = new HashMap<ProgramVariable, LinkedHashSet<ProgramVariable>>();
        post.putAll(pre);
        post.putAll(getDependencies(action));
        return post;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.transitionmodel.StateGenerator#getSpecialSymbolNoop(java.lang.Object,
     *      java.lang.Object)
     */
    @Override
    public DLProgram getSpecialSymbolNoop(
            Map<ProgramVariable, LinkedHashSet<ProgramVariable>> pre,
            Map<ProgramVariable, LinkedHashSet<ProgramVariable>> post) {
        post.putAll(pre);
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.transitionmodel.StateGenerator#getSymbolForBackloop(java.lang.Object,
     *      java.lang.Object)
     */
    @Override
    public DLProgram getSymbolForBackloop(
            Map<ProgramVariable, LinkedHashSet<ProgramVariable>> pre,
            Map<ProgramVariable, LinkedHashSet<ProgramVariable>> post) {
        post.putAll(pre);
        return null;
    }


    /* (non-Javadoc)
     * @see de.uka.ilkd.key.dl.transitionmodel.StateGenerator#generateAction(de.uka.ilkd.key.dl.model.DLProgram)
     */
    @Override
    public DLProgram generateAction(DLProgram program) {
        return program;
    }


    /* (non-Javadoc)
     * @see de.uka.ilkd.key.dl.transitionmodel.StateGenerator#generateBranch(de.uka.ilkd.key.dl.model.DLProgram, int)
     */
    @Override
    public DLProgram generateBranch(DLProgram program, int pos) {
        if(program instanceof DLNonTerminalProgramElement) {
            return (DLProgram) ((DLNonTerminalProgramElement) program).getChildAt(pos);
        }
        throw new IllegalArgumentException("Dont know why a terminal program element like " + program + " cause a branch!");
    }


    /* (non-Javadoc)
     * @see de.uka.ilkd.key.dl.transitionmodel.StateGenerator#generateMerge(de.uka.ilkd.key.dl.model.DLProgram, int)
     */
    @Override
    public DLProgram generateMerge(DLProgram program, int pos) {
        if(program instanceof DLNonTerminalProgramElement) {
            return (DLProgram) ((DLNonTerminalProgramElement) program).getChildAt(pos);
        }
        throw new IllegalArgumentException("Dont know why a terminal program element like " + program + " cause a branch!");
    }
}