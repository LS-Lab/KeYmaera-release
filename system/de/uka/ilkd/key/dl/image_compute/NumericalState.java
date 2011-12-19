/**
 * NumericalState for DL program transition.
 *
 * @author jyn (jingyin@andrew.cmu.edu)
 * @author Andre Platzer (aplatzer)
 */

package de.uka.ilkd.key.dl.image_compute;

import de.uka.ilkd.key.dl.image_compute.graph.Node;
import de.uka.ilkd.key.dl.image_compute.NumericalActionFactory.*;

import java.util.*;

import orbital.math.Real;
import orbital.math.ValueFactory;

public class NumericalState
{

    private List<Update> updates;

    private static final ValueFactory vf = MachValueFactory.getInstance();
    private static final Real SO_FAR_AWAY = vf.valueOf(100000.0);

    private Map<String, Real> lastMap = null;

    public Real accumulatedCost;
    private Real time;              // time spent in a DiffSystem
    private boolean terminated;     // whether state has terminated
    private boolean multiple;       // whether state has multiple succeeding actions
    private boolean eval;           // evaluation result of a given state
    private Node node;
                             
    /**
     * Heuristic distance to having satisfied condition
     */
    private Real heuristic = null; /*SO_FAR_AWAY;*/

    public List<String> appendLog;

    /**
     * Encapsulates a single update to a variable.
     *
     * Immutable.
     */
    public static class Update
    {
        String variable;
        Real value;

        @Override
        public String toString()
        {
            return variable + " = " + value;
        }
    }

    public NumericalState()
    {
        accumulatedCost = vf.valueOf(0.0);
        time = vf.valueOf(0.0);
        terminated = false;
        multiple = false;
        updates = new ArrayList<Update>();
        lastMap = new HashMap<String, Real>();
        appendLog = new ArrayList<String>();
    }

    /**
     * Creates a continuation of the current state.
     *
     * Doesn't set the multiple flag.
     * TODO: use an optimized copyOf to save memory.
     * Assumes that a terminated state would not call copyOf()
     */
    public NumericalState copyOf()
    {
        NumericalState ns = new NumericalState();

        List<Update> updatesCopy = new ArrayList<Update>(updates);
        Map<String, Real> lastMapCopy = new HashMap<String, Real>(lastMap);
        List<String> appendLogCopy = new ArrayList<String>(appendLog);

        ns.updates = updatesCopy;
        ns.lastMap = lastMapCopy;
        ns.time = time;
        ns.node = node;
        ns.accumulatedCost = accumulatedCost;
        ns.appendLog = appendLogCopy;

        return ns;
    }

    /**
     * Stages the update until the next commit.
     */
    public void setSymbol(String variable, Real value)
    {
        assert(!terminated);
        Update u = new Update();
        u.variable = variable;
        u.value = value;
        updates.add(u);
    }

    public Real readSymbol(String variable)
    {
        return lastMap.get(variable);
    }

    /**
     * Returns the value of the symbol in the most recently committed snapshot.
     *
     * This behavior makes it easier to implement parallel updates.
     */
    public Real getSymbol(String variable)
    {
        assert(!terminated);
        return lastMap.get(variable);
    }

    /**
     * Forces update of a symbol.
     */
    public void forceUpdate(String variable, Real value)
    {
        setSymbol(variable, value);
        lastMap.put(variable, value);
    }

    /**
     * Commits the previous updates.
     */
    public void commit(ActionCommit ac)
    {
        StringBuilder action = new StringBuilder();

        action.append(ac.toString());
        for (Update u : updates)
        {
            action.append("\t");
            action.append(u.toString());
            //System.err.println("u:" + u);
            lastMap.put(u.variable, u.value);
        }
        action.append("\t");
        for (Map.Entry<String, Real> e : lastMap.entrySet())
        {
            action.append("[");
            action.append(e.getKey());
            action.append(" = ");
            action.append(e.getValue());
            action.append("]");
        }
        appendLog.add(action.toString());
        updates = new ArrayList<Update>();
        /*Commit commit = new Commit();

        //System.err.println("committing on: " + ac);
        appendLog.add(ac.toString());
        commit.snapshot = new HashMap<String, Real>(lastMap);
        for (Update u : updates)
            commit.snapshot.put(u.variable, u.value);
        commit.updates = updates;
        updates = new ArrayList<Update>();
        commit.action = ac;
        lastMap = commit.snapshot;
        if (loopEnv != null)
        {
            if (loopEnv.ic == null)
            {
                loopEnv.ic = new IterationCommit(loopEnv.lc.getIterations().size());
            }
            loopEnv.ic.addCommit(commit);
        }
        else
        {
            commits.add(commit);
            //System.err.println("committed:" + commit.action);
        }*/
        //System.err.println("committed state: " + this);
    }

    /**
     * Rolls back to the previous commit and clears uncommitted updates.
     */
    public void rollBack()
    {
        updates.clear();
    }

    /**
     * State's about to enter new loop, saves the previous loop environment, if any.
     */
    public void enterLoop()
    {
        //System.err.println("loop");
        appendLog.add("loop");
        /*if (loopEnv != null)
        {
            loops.add(loopEnv);
        }
        loopEnv = new LoopEnvironment();
        loopEnv.lc = new LoopCommit();
        loopEnv.ic = null;*/
        //loopEnv.ic = new IterationCommit(0);
        //System.err.println("[" + stateID + "]entering loop");
    }

    /**
     * Commits the current iteration.
     */
    public void commitIteration()
    {
        //System.err.println("end iteration");
        appendLog.add("end_iteration");
        //assert(!loopEnv.ic.getCommit().isEmpty());
        /*loopEnv.lc.addIteration(loopEnv.ic);
        int prevIdx = loopEnv.lc.getIterations().size();
        loopEnv.ic = null;*/
        //System.err.println("[" + stateID + "]committed " + prevIdx + " iteration");
    }

    /**
     * Commits the current loop and restores the previous loop environment, if any.
     */
    public void commitLoop()
    {
        //System.err.println("end loop");
        appendLog.add("end_loop");
        /*LoopEnvironment loopEnvOld = loopEnv;
        if (!loops.isEmpty())
            loopEnv = loops.remove(loops.size() - 1);
        else
            loopEnv = null;
        commit(loopEnvOld.lc);*/
    }

    /*
     * Methods for tracing the transition of a state.
     */
    public List<String> getAppendLog()
    {
        return Collections.unmodifiableList(appendLog);
    }

    public Real getCost()
    {
        return accumulatedCost;
    }

    public void setCost(Real cost)
    {
        accumulatedCost = cost;
    }

    public Real getTime()
    {
        return time;
    }

    public void setTime(Real t)
    {
        time = t;
    }

    /**
     * Sets multiple of a state to indicate multiple subsequent actions.
     */
    public void setMultiple()
    {
        multiple = true;
    }

    public boolean getMultiple()
    {
        return multiple;
    }

    public void setTerminated()
    {
        terminated = true;
    }

    public boolean getTerminated()
    {
        return terminated;
    }

    public boolean isTerminated()
    {
        return getTerminated();
    }

    public void setNode(Node node)
    {
        this.node = node;
    }

    public Node getNode()
    {
        return node;
    }

    public void setEvaluated(boolean result)
    {
        assert(terminated);
        eval = result;
    }

    /**
     * Gets the evaluated result of a terminated state.
     */
    public boolean getEvaluated()
    {
        return eval;
    }

    public boolean isEvaluated()
    {
        return getEvaluated();
    }

    public void setHeuristic(Real heuristic) {
	    this.heuristic = heuristic;
	}

    public Real getHeuristic() {
	    return heuristic;
	}

    /**
     * Prints the snapshot of the most current commit.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Real> e : lastMap.entrySet()) {
            sb.append('[');
            sb.append(e.getKey());
            sb.append('=');
            sb.append(e.getValue());
            sb.append(']');
        }
        return sb.toString();
    }

}
