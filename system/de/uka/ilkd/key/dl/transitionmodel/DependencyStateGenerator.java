package de.uka.ilkd.key.dl.transitionmodel;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uka.ilkd.key.dl.model.Assign;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.dl.model.Quest;
import de.uka.ilkd.key.dl.model.RandomAssign;
import de.uka.ilkd.key.dl.model.impl.NotImpl;
import de.uka.ilkd.key.dl.model.impl.QuestImpl;
import de.uka.ilkd.key.java.ProgramElement;

/**
 * TODO jdq documentation since Nov 9, 2007
 * 
 * @author jdq
 * @since Nov 9, 2007
 * 
 */
public class DependencyStateGenerator implements
        StateGenerator<DependencyState, DLProgram> {

    /**
     * TODO jdq documentation since Nov 21, 2007
     * 
     * @author jdq
     * @since Nov 21, 2007
     * 
     */
    public class WriteAndReadSets {
        Set<ProgramVariable> reads = new HashSet<ProgramVariable>();
        Set<ProgramVariable> writes = new HashSet<ProgramVariable>();

    }

    public static DependencyState generateDependencyMap(DLProgram program) {
        TransitionSystem<DependencyState, DLProgram> transitionModel = TransitionSystemGenerator
                .getTransitionModel(program, new DependencyStateGenerator(),
                        new DependencyState());
        return transitionModel.getFinalState();
    }

    /**
     * TODO jdq documentation since Nov 12, 2007
     * 
     * @param action
     * @return
     */
    private static Map<ProgramVariable, LinkedHashSet<ProgramVariable>> getDependencies(
            DLProgram action) {
        HashMap<ProgramVariable, LinkedHashSet<ProgramVariable>> result = new HashMap<ProgramVariable, LinkedHashSet<ProgramVariable>>();

        if (action instanceof Assign) {
            Assign ass = (Assign) action;
            LinkedHashSet<ProgramVariable> vars = new LinkedHashSet<ProgramVariable>();
            vars.addAll(getAllVariables(ass.getChildAt(1)));
            ProgramElement childAt = ass.getChildAt(0);
            if (childAt instanceof ProgramVariable) {
                LinkedHashSet<ProgramVariable> set = result.get(childAt);
                if (set == null) {
                    set = new LinkedHashSet<ProgramVariable>();
                    result.put((ProgramVariable) childAt, set);
                }
                set.addAll(vars);
                set.remove(childAt); // variables should not depend on
                // themselves
                assert result.get(childAt) != null;
            } else {
                throw new IllegalArgumentException(
                        "Dont know how to assign something to " + childAt);
            }
        } else if (action instanceof DiffSystem) {
            // handle differential equation system
            DiffSystem system = (DiffSystem) action;
            for (ProgramElement elem : system) {
                LinkedHashSet<ProgramVariable> allVariables = getAllVariables(elem);
                for (ProgramVariable pv : getDottedVariables(elem)) {
                    LinkedHashSet<ProgramVariable> set = result.get(pv);
                    if (set == null) {
                        set = new LinkedHashSet<ProgramVariable>();
                        result.put(pv, set);
                    }
                    set.addAll(allVariables);
                    set.remove(pv); // variables should not depend on themselves
                    assert result.get(pv) != null;
                }
            }
        } else if (action instanceof RandomAssign) {
            ProgramElement childAt = ((RandomAssign) action).getChildAt(0);
            if (childAt instanceof ProgramVariable) {
                LinkedHashSet<ProgramVariable> set = result.get(childAt);
                if (set == null) {
                    set = new LinkedHashSet<ProgramVariable>();
                    result.put((ProgramVariable) childAt, set);
                }
                assert result.get(childAt) != null;
            }
        }

        return result;
    }

    /**
     * TODO jdq documentation since Nov 12, 2007
     * 
     * @param elem
     */
    private static LinkedHashSet<ProgramVariable> getDottedVariables(
            ProgramElement childAt) {
        LinkedHashSet<ProgramVariable> vars = new LinkedHashSet<ProgramVariable>();
        if (childAt instanceof Dot) {
            vars.add((ProgramVariable) ((Dot) childAt).getChildAt(0));
        } else if (childAt instanceof DLNonTerminalProgramElement) {
            DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) childAt;
            for (ProgramElement elem : dlnpe) {
                vars.addAll(getDottedVariables(elem));
            }
        }
        return vars;
    }

    /**
     * TODO jdq documentation since Nov 12, 2007
     * 
     * @param childAt
     * @return
     */
    private static LinkedHashSet<ProgramVariable> getAllVariables(
            ProgramElement childAt) {
        LinkedHashSet<ProgramVariable> vars = new LinkedHashSet<ProgramVariable>();
        if (childAt instanceof ProgramVariable) {
            vars.add((ProgramVariable) childAt);
        } else if (childAt instanceof DLNonTerminalProgramElement) {
            DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) childAt;
            for (ProgramElement elem : dlnpe) {
                vars.addAll(getAllVariables(elem));
            }
        }
        return vars;
    }

    /**
     * TODO jdq documentation since Nov 21, 2007
     * 
     * @param action
     * @return
     */
    private WriteAndReadSets getWriteAndReadSets(ProgramElement childAt) {
        WriteAndReadSets sets = new WriteAndReadSets();
        if (childAt instanceof Assign) {
            Assign ass = (Assign) childAt;
            ProgramVariable var = (ProgramVariable) ass.getChildAt(0);
            LinkedHashSet<ProgramVariable> readVariables = getAllVariables(ass
                    .getChildAt(1));
            sets.writes.add(var);
            sets.reads.addAll(readVariables);
        } else if(childAt instanceof DiffSystem) {
            DiffSystem sys = (DiffSystem) childAt;
            sets.writes.addAll(getDottedVariables(sys));
            sets.reads.addAll(getAllVariables(sys));
        } else if(childAt instanceof Quest) {
            sets.reads.addAll(getAllVariables(childAt));
        } else if(childAt instanceof RandomAssign) {
            sets.writes.addAll(getAllVariables(childAt));
        }
        return sets;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.transitionmodel.StateGenerator#generateMergeState(de.uka.ilkd.key.dl.model.DLProgram,
     *      java.util.List)
     */
    @Override
    public DependencyState generateMergeState(DLProgram program,
            List<DependencyState> states) {
        DependencyState post = new DependencyState();
        post.mergeWith(states);
        return post;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.transitionmodel.StateGenerator#getPostState(java.lang.Object,
     *      java.lang.Object)
     */
    @Override
    public DependencyState getPostState(DependencyState pre, DLProgram action) {
        DependencyState post = new DependencyState(pre);
        post.addDependencies(getDependencies(action));
        WriteAndReadSets sets = getWriteAndReadSets(action);
        System.out.println("Action is " + action);//XXX
        System.out.println("Reads are " + sets.reads);//XXX 
        System.out.println("Writes are " + sets.writes);//XXX 
        System.out.println("WriteBeforeReads are " + pre.getWriteBeforeReadList());//XXX 
        for(ProgramVariable r: sets.reads) {
            if(post.getWriteBeforeReadList().get(r) == null) {
                post.getWriteBeforeReadList().put(r, false);
            }
        }
        for(ProgramVariable r: sets.writes) {
            if(post.getWriteBeforeReadList().get(r) == null) {
                post.getWriteBeforeReadList().put(r, true);
            }
        }
        System.out.println("New WriteBeforeReads are " + post.getWriteBeforeReadList());//XXX
        return post;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.transitionmodel.StateGenerator#getSpecialSymbolNoop(java.lang.Object,
     *      java.lang.Object)
     */
    @Override
    public DLProgram getSpecialSymbolNoop(DependencyState pre,
            DependencyState post) {
        post.mergeWith(Collections.singleton(pre));
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.transitionmodel.StateGenerator#getSymbolForBackloop(java.lang.Object,
     *      java.lang.Object)
     */
    @Override
    public DLProgram getSymbolForBackloop(DependencyState pre,
            DependencyState post) {
        post.mergeWith(Collections.singleton(pre));
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.transitionmodel.StateGenerator#generateAction(de.uka.ilkd.key.dl.model.DLProgram)
     */
    @Override
    public DLProgram generateAction(DLProgram program) {
        return program;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.transitionmodel.StateGenerator#generateBranch(de.uka.ilkd.key.dl.model.DLProgram,
     *      int)
     */
    @Override
    public DLProgram generateBranch(DLProgram program, int pos) {
        if (program instanceof DLNonTerminalProgramElement) {
            return (DLProgram) ((DLNonTerminalProgramElement) program)
                    .getChildAt(pos);
        }
        throw new IllegalArgumentException(
                "Dont know why a terminal program element like " + program
                        + " cause a branch!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.transitionmodel.StateGenerator#generateMerge(de.uka.ilkd.key.dl.model.DLProgram,
     *      int)
     */
    @Override
    public DLProgram generateMerge(DLProgram program, int pos) {
        if (program instanceof DLNonTerminalProgramElement) {
            return (DLProgram) ((DLNonTerminalProgramElement) program)
                    .getChildAt(pos);
        }
        throw new IllegalArgumentException(
                "Dont know why a terminal program element like " + program
                        + " cause a branch!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.transitionmodel.StateGenerator#generateElseAction(de.uka.ilkd.key.dl.model.Formula)
     */
    @Override
    public DLProgram generateElseAction(Formula f) {
        DLProgram action = this.generateAction(new QuestImpl(new NotImpl(f)));
        return action;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.transitionmodel.StateGenerator#generateThenAction(de.uka.ilkd.key.dl.model.Formula)
     */
    @Override
    public DLProgram generateThenAction(Formula f) {
        DLProgram action = this.generateAction(new QuestImpl(f));
        return action;
    }
}