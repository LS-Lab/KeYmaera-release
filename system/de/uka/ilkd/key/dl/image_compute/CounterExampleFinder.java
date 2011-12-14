/**
 * Looks for counterexamples of a problem.
 *
 * A CounterExampleFinder is given a top-level DL program (untransformed by user) and
 * performs state transitions on the system to find whether there is a set of parameters
 * that could invalidate the outcome (post-condition) of the system.
 *
 * User of this code need instantiate a CounterExampleFinder with an untransformed,
 * top-level DL program and invoke findSolution() of that object should object creation
 * succeeds. An exception is raised in the c'tor should the program given doesn't
 * satisfy certain requirements imposed upon the program. More about restrictions below.
 *
 * The problem at hand is to falsify:
 *      A ==| {u}\['a\](phi)
 * which is equivalent to:
 *      A and {u} \{'a\}(not(phi))
 * Note: \{'a\} is the diamond block
 *
 * For not(phi), we can propagate not into phi. The DLProgram's that we currently
 * handle are those that could eventually be simplified into the form:
 *      1. B and \{'b\}(theta)
 *      2. B or \{'b\}(theta)
 * In case 1. If B is false, then statement's not satisfied. If B is true, statement's
 * satisfied iff \{'b\}(theta) is satisfied. If B is underdetermined, statement's
 * satisfied iff B and \{'b\}(theta) is satisfied, B serving as pre-condition.
 * In case 2. If B is true, then statement's satisfied. If B is false, statement's
 * satisfied iff \{'b\}(theta) is satisfied. If B is underdetermined, statement's
 * satisfied iff not(B) and \{'b\} is satisfied, B serving as pre-condition.
 *
 * We require that post-conditions be written in the form of:
 *      1. A imp \['b\](phi)
 *      2. A and \['b\](phi)
 *      3. A or \['b\](phi)
 *
 * DLProgram's that cannot be simplified into the above two forms are not supported.
 *
 * The initial state of the transition is an "empty" state. FI[A], applying u and other
 * things are taken care of by a TransitionGraph object.
 *
 * @author jyn (jingyin@andrew.cmu.edu)
 * @author Andre Platzer (aplatzer)
 */

package de.uka.ilkd.key.dl.image_compute;

import de.uka.ilkd.key.dl.arithmetics.ICounterExampleGenerator.*;
import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.MathematicaDLBridge;
import de.uka.ilkd.key.dl.image_compute.graph.Node;
import de.uka.ilkd.key.dl.image_compute.NumericalActionFactory.*;
import de.uka.ilkd.key.dl.image_compute.NumericalState.*;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.options.DLOptionBean.CexFinder;
import de.uka.ilkd.key.dl.options.DLOptionBean.TracerStat;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;

import java.util.*;
import java.lang.reflect.*;

import orbital.algorithm.template.HeuristicAlgorithm;
import orbital.algorithm.template.AStar;
import orbital.algorithm.template.BreadthFirstSearch;
import orbital.algorithm.template.DepthFirstSearch;
import orbital.algorithm.template.GeneralSearch;
import orbital.algorithm.template.GeneralSearchProblem;
import orbital.algorithm.template.HillClimbing;
import orbital.algorithm.template.IterativeDeepening;
import orbital.algorithm.template.IterativeExpansion;
import orbital.algorithm.template.IterativeDeepeningAStar;
import orbital.algorithm.template.TransitionModel;
import orbital.logic.functor.Function;
import orbital.logic.functor.MutableFunction;
import orbital.math.Real;
import orbital.math.ValueFactory;
import orbital.math.Values;

public class CounterExampleFinder implements GeneralSearchProblem
{

    private final Services services;
    private final TransitionGraph transitionGraph;
    private final Node initialNode;

    private final Evaluator ev;

    private NumericalState initialState;
    private NumericalState initialStateCopy;

	private boolean abort = false;

    // determines what search algorithm to use
    // algorithms are registered in DLOptionBean.java and DLOptionBeanInfo.java
    // in $KEY_SRC/dl/option
    private static final Map<String, Class<? extends GeneralSearch>> CEX_FINDERS =
        new HashMap<String, Class<? extends GeneralSearch>>();
    static {
        CEX_FINDERS.put(CexFinder.DFS.toString(), DepthFirstSearch.class);
        CEX_FINDERS.put(CexFinder.BFS.toString(), BreadthFirstSearch.class);
        CEX_FINDERS.put(CexFinder.ITER_DEEP.toString(), IterativeDeepening.class);
        CEX_FINDERS.put(CexFinder.ITER_DEEP_ASTAR.toString(), IterativeDeepeningAStar.class);
        CEX_FINDERS.put(CexFinder.ITER_EXP.toString(), IterativeExpansion.class);
        CEX_FINDERS.put(CexFinder.ASTAR.toString(), AStar.class);
        CEX_FINDERS.put(CexFinder.HILL_CLIMB.toString(), HillClimbing.class);
    }

    // determines whether we turn on tracing
    // the predicate is registered in DLOptionBean.java and DLOptionBeanInfo.java
    // in $KEY_SRC/dl/option
    private static final Map<String, Boolean> TRACER_STATUS =
        new HashMap<String, Boolean>();
    static {
        TRACER_STATUS.put(TracerStat.ON.toString(), true);
        TRACER_STATUS.put(TracerStat.OFF.toString(), false);
    }

    private static final DLOptionBean dlOptionBean = DLOptionBean.INSTANCE;

    /**
     * Instantiates a cex finder with a given program block.
     * @param modalForm is the modal formula with the program
     */
    public CounterExampleFinder(Term preCond, Term modalForm, Services services)
    {
        this.services = services;
        ev = Evaluator.getInstance(services);
        initialState = new NumericalState();
        transitionGraph = new TransitionGraph(preCond, modalForm, ev);
        initialNode = transitionGraph.getInitialNode();
        initialState.setNode(initialNode);
    }

    /**
     * Finds the counterexample.
     */
    public String findSolution()
    {
        Constructor<?> ctors[] = CEX_FINDERS.get(dlOptionBean.getCexFinder().toString())
                                            .getConstructors();
        List<Object> argList = new ArrayList<Object>();
        final int ITERATION = 1;
        String ret = "";
        NumericalState soln = null;
        for (int i = 0; i < ITERATION; i++) {
            System.err.println("working on iteration " + i);
            GeneralSearch gs;
            try {
	            if (HeuristicAlgorithm.class.isAssignableFrom(ctors[0].getDeclaringClass())) {
	        		System.out.println("Heuristic");
	                argList.add(transitionGraph.getHeuristic());
	            }
                gs = (GeneralSearch)ctors[0].newInstance(argList.toArray());
            } catch (Exception e) {
                throw (IllegalArgumentException) new IllegalArgumentException("could not instantiate " + ctors[0].getDeclaringClass()).initCause(e);
            }
            soln = (NumericalState)gs.solve(this);
            if (soln == null)
                ret = "";
            else
                ret = soln.toString();
        }
        if (ret.equals(""))
            System.err.println("NO counterexample found");
        else {
            assert(soln != null);
            System.err.println("COUNTEREXAMPLE:   " + ret);
            if (soln != null) System.err.println("HEURISTIC:\t" + soln.getHeuristic() + "\n" + transitionGraph.getHeuristic());
            /*for (String s : soln.appendLog)
            {
                System.err.println(s);
            }*/
            //System.err.println("==========here's the full trace==========");
            //printHistory(soln.getCommitHistory());
        }

        TreeDisplay tree = new TreeDisplay(soln);
        tree.display();
        return ret;
    }

    /**
     * Returns the initial state of the problem instance.
     *
     * Since this method might get called multiple times in certain
     * algorithms, we guarantee that multiple invocations would get
     * the same result.
     *
     * Since it's pretty likely that initialState gets modified, we
     * generate a copy of initialState everytime we get a request
     * to return an initial state.
     */
    public Object getInitialState()
    {
        NumericalState ret;
        if (initialStateCopy == null)
            initialStateCopy = initialState;
        ret = initialStateCopy;
        initialStateCopy = initialStateCopy.copyOf();
        initialStateCopy.setNode(initialNode);
        return ret;
    }

    /**
     * Returns the function that computes a state's accumulated cost.
     *
     * Each state stores its own accumulated cost, and the function
     * returned just accesses that field of a state.
     */
    public MutableFunction getAccumulatedCostFunction()
    {
        return new MutableFunction()
        {
            public Object apply(Object obj)
            {
                return ((NumericalState) obj).accumulatedCost;
            }

            public Object set(Object objs, Object objCost)
            {
                Object oldCost = ((NumericalState) objs).accumulatedCost;
                ((NumericalState) objs).accumulatedCost = (Real) objCost;
                return oldCost;
            }

            public Object clone()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    public TransitionModel.Transition transition(Object obja, Object objs,
        Object statep)
    {
        ActionEdge<Node> ae = (ActionEdge<Node>) obja;
        return new Transition(ae.getAction(), 1);
    }

    /**
     * Tests whether a state is a solution state.
     *
     * A state becomes a solution state only if it's both terminal and
     * that it violates the program's post-condition.
     */
    public boolean isSolution(Object objs)
    {
        NumericalState ns = (NumericalState) objs;
        assert(ns != null);
        if (ns.getTerminated()) {
            boolean evaluated = ns.getEvaluated();
            return evaluated;
        } else {
            System.out.println(ns.getHeuristic() + "\t" + ns);
        }
        return false;
    }

    /**
     * Returns an iterator to a series of actions applied onto a state.
     *
     * Obtains the ActionEdge pertaining to State's current node in the graph.
     */
    public Iterator actions(Object objs)
    {
        NumericalState state = (NumericalState) objs;
        // no more action to perform on a terminated state
        if (state.getTerminated() || abort)
            return new IteratorUtil.EmptyIterator();
        Collection col = transitionGraph.actions(state.getNode());
        if (col.size() > 1)
            state.setMultiple();
        return col.iterator();
    }

    /**
     * Returns a single state after applying the given action.
     *
     * Depending on the state, we might update the original state and return
     * it or we might clone the original state, update the clone state and
     * return it.
     *
     * In our case (deterministic case), the iterator contains just a single
     * successor state.
     */
    public Iterator states(Object obja, Object objs)
    {
        ActionEdge<Node> ae = (ActionEdge<Node>) obja;
        NumericalState state = (NumericalState) objs;
        if (state.getMultiple())
            state = state.copyOf();
        ae.getAction().apply(state);
        state.setNode(ae.dest());
        return new IteratorUtil.SingleIterator(state);
    }

	public void abortCalculation() {
		abort = true;
	}

}
