/**
 * Graph that describes program transition.
 *
 * Given a top-level DL program, generates a graph that describes the transition of
 * the DL program.
 *
 * A transition graph is composed of psuedo-nodes and action edges. An initial state
 * starts at the first psuedo-node, and ends up in the some other psuedo-node. The
 * last action to apply is either an evaluation action or a question action.
 *
 * @author jyn (jingyin@andrew.cmu.edu)
 * @author Andre Platzer (aplatzer)
 */

package de.uka.ilkd.key.dl.image_compute;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.IODESolver.ODESolverUpdate;
import de.uka.ilkd.key.dl.image_compute.TransitionGraph.PostCond;
import de.uka.ilkd.key.dl.image_compute.graph.Graph;
import de.uka.ilkd.key.dl.image_compute.graph.Node;
import de.uka.ilkd.key.dl.model.And;
import de.uka.ilkd.key.dl.model.Assign;
import de.uka.ilkd.key.dl.model.Biimplies;
import de.uka.ilkd.key.dl.model.Choice;
import de.uka.ilkd.key.dl.model.Chop;
import de.uka.ilkd.key.dl.model.CompoundDLProgram;
import de.uka.ilkd.key.dl.model.Constant;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.ElementaryDLProgram;
import de.uka.ilkd.key.dl.model.FunctionTerm;
import de.uka.ilkd.key.dl.model.IfStatement;
import de.uka.ilkd.key.dl.model.Implies;
import de.uka.ilkd.key.dl.model.Not;
import de.uka.ilkd.key.dl.model.Or;
import de.uka.ilkd.key.dl.model.PredicateTerm;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.dl.model.Quest;
import de.uka.ilkd.key.dl.model.RandomAssign;
import de.uka.ilkd.key.dl.model.Star;
import de.uka.ilkd.key.dl.model.VariableDeclaration;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.*;
import de.uka.ilkd.key.logic.sort.Sort;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import orbital.logic.functor.Function;
import orbital.math.Real;
import orbital.math.ValueFactory;
import orbital.math.Values;
import orbital.math.functional.Functions;

class TransitionGraph
{

    private final Graph<Node, ActionEdge<Node>> transitionGraph;
    private final Node initialNode;
    private final Services services;
    private final NumericalActionFactory naf;
    private final Evaluator ev;
    
    private Function heuristic;

    private static final ValueFactory vf = MachValueFactory.getInstance();
    private static final NodeFactory nf = NodeFactory.getInstance();
    private static final TermBuilder tb = TermBuilder.DF;
    private static final int NUM_INSTANCE = 10;

    /**
     * Classifies post-conditions.
     *
     * Post-conditions are (up to normalization):
     * 1. first-order formulas
     * 2. condition AND programBlock
     * 3. condition OR programBlock
     */
    public class PostCond
    {
        public PostCondType type;
        public Term expr;
        public Term program;

        public PostCond(PostCondType type, Term expr, Term program)
        {
            this.type = type;
            this.expr = expr;
            this.program = program;
        }
        public String toString() {
            return "PostCond[" + type + ":" + prettyPrint(expr) + ", " + prettyPrint(program) + "]";
        }
        private String prettyPrint(Term expr)
        {
            if (expr == null) return "null";
            return expr.toString();
//            StringWriter sw = new StringWriter();
//            try {
//                expr.(new PrettyPrinter(sw));
//            } catch (IOException ioEx) {
//                System.err.println(ioEx);
//                ioEx.printStackTrace();
//                return expr.toString();
//            }
//            return sw.toString();
        }
    }

    /**
     * Consisting of a node and an edge sticking out of the node.
     *
     * Called HalfEdge because we need another node in order for the edge to become
     * part of the graph.
     */
    private class HalfEdge
    {
        Node node;
        Action action;

        HalfEdge(Node node, Action action)
        {
            this.node = node;
            this.action = action;
        }
    }

    /**
     * Encapsulates a DiffSystem.
     *
     * User can query the state the system is in at a given time.
     * The system is increment-independent, and hence can be used by multiple
     * edges in the graph that uses a common diff system, but advances at
     * differing time increments.
     */
    public interface DiffSystemTransition
    {
        /**
         * Updates the given state under given time.
         */
        public void apply(Real t, NumericalState ns);

        /**
         * The DiffSystem related with transition.
         */
        public DiffSystem getSystem();
    }

    /**
     * Represents diff systems by their closed form solutions.
     *
     * Assumes that the solution contains only "simple" formulas.
     */
    private class SymbolicDST implements DiffSystemTransition
    {
        List<ODESolverUpdate> updates;
        LogicVariable t;
        Map<String, Real> map;
        String tName;
        DiffSystem ds;

        SymbolicDST(DiffSystem ds)
        {
            t = createLogicVariable("t");
            this.ds = ds;
            tName = t.toString();
            tName = tName.substring(0, tName.length() - 2);
            try {
                updates = MathSolverManager.getCurrentODESolver()
                    .odeUpdate(ds, t, services, -1);
            } catch (Exception e) {
                System.err.println("exp: " + e);
                e.printStackTrace();
                throw new UnsupportedOperationException("failure to compute diff system");
            }
            map = new HashMap<String, Real>();
        }

        public void apply(Real t, NumericalState ns)
        {
            map.put(tName, t);
            computeUpdates(ns, map);
        }

        public DiffSystem getSystem()
        {
            return ds;
        }

        /**
         * Computes the updates at certian time. 
         */
        private void computeUpdates(NumericalState ns, Map<String, Real> map)
        {
            for (ODESolverUpdate u : updates) {
                String var = u.location.toString();
                ns.setSymbol(var, ev.evalExpr(ns, u.expr, map));
            }
        }
    }

    /**
     * Constructs a TransitionGraph object.
     *
     * If given programBlock is non-conformant, instantiation would fail.
     *
     * @pre programBlock.op() instanceof QuanUpdateOperator or Modality.
     * @param modalForm The given modal formula with a program block on which we're finding a CEX.
     */
    TransitionGraph(Term preCond, Term modalForm, Evaluator ev)
    {
        transitionGraph = new Graph<Node, ActionEdge<Node>>();
        services = ev.getServices();
        this.ev = Evaluator.getInstance(services);
        naf = NumericalActionFactory.getInstance(ev);
        initialNode = nf.createTransitionNode();
        transitionGraph.addVertex(initialNode);
        modalForm = invertTerm(services.getNamespaces(), modalForm);

        Node evalNodeNext = nf.createTransitionNode();
        if (!preCond.toString().equals("true")) {
            PostCond cond = new PostCond(PostCondType.FIRST_ORDER_TYPE, preCond, modalForm);
            Action evalAction = naf.createEvalAction(cond);
            transitionGraph.addVertex(evalNodeNext);
            ActionEdge<Node> evalEdge = new ActionEdge<Node>(initialNode, evalNodeNext, evalAction);
            for (int i = 0; i < NUM_INSTANCE; i++)
                transitionGraph.addEdge(evalEdge);
            buildGraph(modalForm, evalNodeNext);
        } else
            buildGraph(modalForm, initialNode);
    }

    public Function getHeuristic() {
	return heuristic;
    }

    public void setHeuristic(Function heuristic) {
        this.heuristic = heuristic;
    }

    private Function createHeuristic(PostCond postCond) {
        if (postCond.type == PostCondType.FIRST_ORDER_TYPE)
            return new TruthDistanceHeuristic(postCond);
        else
            // just uninformative if it's not first-order.
            //@todo improve with best-effort approximation
            return Functions.zero;
    }
    
    /**
     * Heuristic implementation
     * @author aplatzer
     *
     */
    private class TruthDistanceHeuristic implements orbital.logic.functor.Function {
	private final ValueFactory vf = Values.getDefaultInstance();
	private PostCond postCond;
	public TruthDistanceHeuristic(PostCond postCond) {
	    assert postCond.type == PostCondType.FIRST_ORDER_TYPE;
	    this.postCond=postCond;
	    System.out.println("Heuristic " + this);
	}
	public Object apply(Object o) {
	    NumericalState state = (NumericalState) o;
	    Real hval = state.getHeuristic();
	    if (hval != null)
		return hval;
            double proximity = ev.evalApproxCond(state, postCond.expr);
            if (proximity < -0.5) {
        	System.out.println("Surprising heuristic " + proximity + " for " + state);
            }
            if (!state.isTerminated()) {
        	//@TODO should add discrete distance to target state instead of constant.
        	proximity += 0.01;
            }
            hval = vf.valueOf(proximity);
            state.setHeuristic(hval);
	    return hval;
	}
	public String toString() {
	    return "TruthDistanceHeuristic[" + postCond + "]";
	}
    }

    /**
     * Exposes the Modality block within a QuanUpdate.
     *
     * @pre quanUpdate.op() instanceof QuanUpdateOperator
     * @param quanUpdate A QuanUpdate.
     * @return The encapsulated Modality block of quanUpdate.
     */
    private Term exposeProgramBlock(Term quanUpdate)
    {
        return ((QuanUpdateOperator)quanUpdate.op()).target(quanUpdate);
    }

    /**
     * Exposes the DLProgram within a Modality block.
     *
     * @pre modalityBlock.op() instanceof Modality
     * @param modalityBlock A Modality block.
     * @return The encapsulated DLProgram of programBlock.
     */
    private DLProgram exposeDLProgram(Term modalityBlock)
    {
        return (DLProgram)((StatementBlock)modalityBlock.javaBlock().program())
            .getStatementAt(0);
    }

    public static enum PostCondType {
        FIRST_ORDER_TYPE,       // condition is first-order
        AND_TYPE,               // condition is cond AND program
        OR_TYPE,                // condition is cond OR program
        NESTED_TYPE             // condition is program
    }

    /**
     * Builds the transition graph.
     *
     * @pre block.op() instanceof Modality or QuanUpdateOperator. block.op().toString()
     * 
     * @post All newly-created nodes/edges are added to the transition graph.
     * @param modalForm Encapsulates a program block with optional quan updates.
     * @param initialNode The node on which we start the first action.
     */
    private void buildGraph(Term modalForm, Node initialNode)
    {
        Node outputNode;
        if (modalForm.op() instanceof QuanUpdateOperator) {
            outputNode = buildQuanUpdate(modalForm, initialNode);
            modalForm = exposeProgramBlock(modalForm);
            initialNode = outputNode;
        }
        assert(modalForm.op() instanceof Modality);
        DLProgram dl = exposeDLProgram(modalForm);
        List<HalfEdge> buildDLOutput =
            buildDLProgram(dl, Arrays.asList(new Node[]{initialNode}));
        // evalNode is the node whose only subsequent action is evaluating post-cond
        Node evalNode = nf.createTransitionNode();
        transitionGraph.addVertex(evalNode);
        for (HalfEdge he : buildDLOutput) {
            ActionEdge<Node> ae = new ActionEdge<Node>(he.node, evalNode, he.action);
            transitionGraph.addEdge(ae);
        }
        // evalNodeNext and evalNode are connected by an EvalAction
        Node evalNodeNext = nf.createTransitionNode();
        PostCond postCond = buildPostCond(modalForm.sub(0));
        setHeuristic(createHeuristic(postCond));
        Action evalAction = naf.createEvalAction(postCond);
        transitionGraph.addVertex(evalNodeNext);
        ActionEdge<Node> evalEdge = new ActionEdge<Node>(evalNode, evalNodeNext, evalAction);
        for (int i = 0; i < NUM_INSTANCE; i++)
            transitionGraph.addEdge(evalEdge);
        switch (postCond.type) {
        case FIRST_ORDER_TYPE:
            break;
        // contains nested programs
        default:
            buildGraph(postCond.program, evalNodeNext);
            break;
        }
    }

    /**
     * Builds a PostCond object given a post-condition Term.
     */
    private PostCond buildPostCond(Term postCond)
    {
        // makes sure that postCond is conformant
        if (termIsFO(postCond)) {
            return new PostCond(PostCondType.FIRST_ORDER_TYPE, postCond, null);
        } else {
            if (postCond.op() instanceof Modality)
                return new PostCond(PostCondType.NESTED_TYPE, null, postCond);
            else if (postCond.op() instanceof Junctor) {
        	//@todo swap arguments if the first one is a modality and the second one not
                if (postCond.op().toString().equals("and")) {
                    // TODO: need to consider QuanUpdateTerms also
                    assert(postCond.sub(1).op() instanceof Modality) : "should swap arguments";
                    return new PostCond(PostCondType.AND_TYPE, postCond.sub(0), postCond.sub(1));
                } else if (postCond.op().toString().equals("or")) {
                    // TODO: need to consider QuanUpdateTerms also
                    assert(postCond.sub(1).op() instanceof Modality) : "should swap arguments";
                    return new PostCond(PostCondType.OR_TYPE, postCond.sub(0), postCond.sub(1));
                } else {
                    // should not have come here
                    assert(false);
                    return null;
                }
            } else {
                // should not have some here
                assert(false);
                return null;
            }
        }
    }

    /**
     * Checks given Term for conformity.
     *
     * Must contain exactly one Modality block of type diamond.
     * @return Whether term is first-order.
     */
    private boolean termIsFO(Term term)
    {
        if (countBox(term) > 0)
            throw new UnsupportedOperationException("cannot handle term " + term);
        int numDia = countDia(term);
        if (numDia > 1)
            throw new UnsupportedOperationException("cannot handle term " + term);
        else
            return numDia == 0;
    }

    /**
     * Counts the number of box Modality in a given Term.
     */
    private int countBox(Term term)
    {
        if (term.op() instanceof Junctor) {
            if (term.op().toString().equals("not")) {
                assert(countBox(term.sub(0)) == 0);
                return 0;
            } else if (term.op().toString().equals("and")) {
                return countBox(term.sub(0)) + countBox(term.sub(1));
            } else if (term.op().toString().equals("or")) {
                return countBox(term.sub(0)) + countBox(term.sub(1));
            } else {
                // should not encounter other cases
                assert(false);
            }
        } else if (term.op() instanceof Equality) {
            if (term.op().toString().equals("equals")) {
                return 0;
            } else {
                // should not encounter other cases
                assert(false);
            }
        } else if (term.op() instanceof Modality) {
            return term.op().toString().equals("box") ? 1 : 0;
        }/* else if (term.op() instanceof QuanUpdateOperator) {
        }*/ else if (term.op() instanceof RigidFunction) {
            return 0;
        } else {
            // should not encounter other cases
            assert(false);
        }
        return 0;
    }

    /**
     * Counts the number of diamond Modality in a given Term.
     */
    private int countDia(Term term)
    {
        if (term.op() instanceof Junctor) {
            if (term.op().toString().equals("not")) {
                assert(countDia(term.sub(0)) == 0);
                return 0;
            } else if (term.op().toString().equals("and")) {
                return countDia(term.sub(0)) + countDia(term.sub(1));
            } else if (term.op().toString().equals("or")) {
                return countDia(term.sub(0)) + countDia(term.sub(1));
            } else {
                // should not encounter other cases
                assert(false);
            }
        } else if (term.op() instanceof Equality) {
            if (term.op().toString().equals("equals")) {
                return 0;
            } else {
                // should not encounter other cases
                assert(false);
            }
        } else if (term.op() instanceof Modality) {
            return term.op().toString().equals("diamond") ? 1 : 0;
        }/* else if (term.op() instanceof QuanUpdateOperator) {
        }*/ else if (term.op() instanceof RigidFunction) {
            return 0;
        } else {
            // should not encounter other cases
            assert(false);
        }
        return 0;
    }

    /**
     * Inverts a given Term.
     *
     * Propagates Not down to the lowest level.
     *
     * @pre There's no isolated Not scattered throughout term.
     * @todo this should call more optimized negation normal form converter that turns !(a<b) into a>=b etc.
     */
    private Term invertTerm(NamespaceSet nss, Term term)
    {
        // TODO: don't know how to create QuanUpdateTerm yet
        /*if (term.op() instanceof QuanUpdateOperator) {
        } else */if (term.op() instanceof Modality) {
            if (term.op().toString().equals("box"))
                return tb.dia(term.javaBlock(), invertTerm(nss, term.sub(0)));
            else if (term.op().toString().equals("dia"))
                return tb.box(term.javaBlock(), invertTerm(nss, term.sub(0)));
            else
                throw new UnsupportedOperationException("couldn't handle program " + term.op());
        } else if (term.op() instanceof Junctor) {
            if (term.op().toString().equals("not"))
                return term.sub(0);
            else if (term.op().toString().equals("and"))
                return tb.or(invertTerm(nss, term.sub(0)), invertTerm(nss, term.sub(1)));
            else if (term.op().toString().equals("or"))
                return tb.and(invertTerm(nss, term.sub(0)), invertTerm(nss, term.sub(1)));
            else if (term.op().toString().equals("imp"))
                return tb.and(term.sub(0), invertTerm(nss, term.sub(1)));
            else if (term.op().toString().equals("true"))
                return tb.ff();
            else if (term.op().toString().equals("false"))
                return tb.tt();
            else
                throw new UnsupportedOperationException("couldn't handle junctor " + term.op());
        } else if (term.op() instanceof Equality) {
            if (term.op().toString().equals("equiv"))
                return tb.or(tb.and(term.sub(0), invertTerm(nss, term.sub(1))), tb.and(term.sub(1), invertTerm(nss, term.sub(0))));
            else if (term.op().toString().equals("equals"))
                return tb.not(term);
            else
                throw new UnsupportedOperationException("couldn't handle equality " + term.op());
        } else if (term.op() instanceof RigidFunction) {
            if (term.op().toString().equals("leq")) {
        	        assert term.arity() == 2;
                return TermBuilder.DF.func(lookupRigidFunction(nss, new Name("gt"), 2), term.sub(0), term.sub(1));
            } else if (term.op().toString().equals("geq")) {
	        assert term.arity() == 2;
	        return TermBuilder.DF.func(lookupRigidFunction(nss, new Name("lt"), 2), term.sub(0), term.sub(1));	
            } else if (term.op().toString().equals("lt")) {
	        assert term.arity() == 2;
	        return TermBuilder.DF.func(lookupRigidFunction(nss, new Name("geq"), 2), term.sub(0), term.sub(1));	
            } else if (term.op().toString().equals("gt")) {
	        assert term.arity() == 2;
	        return TermBuilder.DF.func(lookupRigidFunction(nss, new Name("leq"), 2), term.sub(0), term.sub(1));	
            }
            return tb.not(term);
        } else {
            throw new UnsupportedOperationException("couldn't handle term "
                + term.op());
        }
    }
    /**
     * @param nss
     * @param name
     * @return
     */
    private static RigidFunction lookupRigidFunction(NamespaceSet nss, Name name,
            int argNum) {
	RigidFunction num = (RigidFunction) nss.functions().lookup(name);
        Sort[] argSorts = new Sort[argNum];
        Arrays.fill(argSorts, RealLDT.getRealSort());
        if (num == null) {
            num = new RigidFunction(name, RealLDT.getRealSort(), argSorts);
        }
        return num;
    }

    /**
     * Builds the nodes/edges that make up a program's QuanUpdate's.
     *
     * @pre quanUpdate.op() instanceof QuanUpdateOperator.
     * @post Newly-created edges and nodes are added to the transition graph.
     * @return The newly-created node next to initialNode.
     */
    private Node buildQuanUpdate(Term quanUpdate, Node initialNode)
    {
        Node outputNode = nf.createTransitionNode();
        Action quanUpdateAction = naf.createQuanUpdateAction(quanUpdate);
        ActionEdge<Node> quanUpdateActionEdge = new ActionEdge<Node>(initialNode, outputNode, quanUpdateAction);
        transitionGraph.addVertex(outputNode);
        transitionGraph.addEdge(quanUpdateActionEdge);
        return outputNode;
    }

    /**
     * Checks that a possible DL program meets our requirements.
     *
     * @pre term.op() instanceof Modality
     */
    private void checkProgramTerm(Term term)
    {
        if (term.javaBlock() == null
        || term.javaBlock() == JavaBlock.EMPTY_JAVABLOCK
        || !(term.javaBlock().program() instanceof StatementBlock))
            throw new UnsupportedOperationException("could not handle: "
                + term);
    }

    /**
     * Returns a collection of actions coming out of a Node.
     */
    Collection<ActionEdge<Node>> actions(Node node)
    {
        return transitionGraph.outgoingEdges(node);
    }

    /**
     * Creates the outlet for a DL program given some input nodes.
     *
     * Each DL sub-program in the graph structure has an input and output.
     * Input is provided, but output is given as edges that should be linked by the
     * calller. All subsequent build*Program method follows this structure.
     */
    private List<HalfEdge> buildDLProgram(DLProgram dl, List<Node> input)
    {
        if (dl instanceof ElementaryDLProgram)
            return buildElemDLProgram((ElementaryDLProgram)dl, input);
        else if (dl instanceof CompoundDLProgram)
            return buildCompDLProgram((CompoundDLProgram)dl, input);
        else
            throw new UnsupportedOperationException("doesn't know how to handle "
                + dl.getClass().getName());
    }

    /**
     * Builds part of the graph involving an ElementaryDLProgram.
     */
    private List<HalfEdge> buildElemDLProgram(ElementaryDLProgram edl, List<Node> input)
    {
        if (edl instanceof DiffSystem)
            return buildDiffSystem((DiffSystem)edl, input);
        else {
            Node newNode = nf.createTransitionNode();
            transitionGraph.addVertex(newNode);
            Action progAction;
            if (edl instanceof Assign)
                progAction = naf.createProgramAction((Assign)edl);
            else if (edl instanceof RandomAssign)
                progAction = naf.createProgramAction((RandomAssign)edl);
            else if (edl instanceof VariableDeclaration)
                progAction = naf.createProgramAction((VariableDeclaration)edl);
            else if (edl instanceof Quest)
                progAction = naf.createVerifyAction(edl.getChildAt(0), "invariant satisfied, proceeding");
            else
                throw new UnsupportedOperationException("could not handle: " + edl.getClass().getName());
            List<HalfEdge> output = new ArrayList<HalfEdge>();
            for (Node n : input)
                output.add(new HalfEdge(n, progAction));
            return output;
        }
    }

    private List<HalfEdge> buildCompDLProgram(CompoundDLProgram cdl, List<Node> input)
    {
        if (cdl instanceof Chop)
            return buildChop((Chop)cdl, input);
        else if (cdl instanceof Choice)
            return buildChoice((Choice)cdl, input);
        else if (cdl instanceof Star)
            return buildStar((Star)cdl, input);
        else if (cdl instanceof IfStatement)
            return buildIfState((IfStatement)cdl, input);
        else
            throw new UnsupportedOperationException("could not handle: " + cdl.getClass().getName());
    }

    private List<HalfEdge> buildChop(Chop chop, List<Node> input)
    {
        List<HalfEdge> output1 = buildDLProgram((DLProgram) chop.getChildAt(0), input);
        Node newNode = nf.createTransitionNode();
        transitionGraph.addVertex(newNode);
        for (HalfEdge he : output1)
            transitionGraph.addEdge(new ActionEdge<Node>(he.node, newNode, he.action));
        return buildDLProgram((DLProgram) chop.getChildAt(1),
            Arrays.asList(newNode));
    }

    private List<HalfEdge> buildChoice(Choice choice, List<Node> input)
    {
        List<HalfEdge> output = buildDLProgram((DLProgram) choice.getChildAt(0), input);
        output.addAll(buildDLProgram((DLProgram) choice.getChildAt(1), input));
        return output;
    }

    private List<HalfEdge> buildIfState(IfStatement ifState, List<Node> input)
    {
        List<HalfEdge> output = new ArrayList<HalfEdge>();
        for (int i=1; i<ifState.getChildCount(); i++) {
            Action checkAction;
            if (i > 1)
                checkAction = naf.createUnverifyAction(ifState.getChildAt(0), "proceed to else-branch");
            else
                checkAction = naf.createVerifyAction(ifState.getChildAt(0), "proceed to then-branch");
            Node newNode = nf.createTransitionNode();
            transitionGraph.addVertex(newNode);
            for (Node n : input)
                transitionGraph.addEdge(new ActionEdge<Node>(n, newNode, checkAction));
            output.addAll(buildDLProgram((DLProgram) ifState.getChildAt(i),
                Arrays.asList(newNode)));
        }
        return output;
    }

    private List<HalfEdge> buildStar(Star star, List<Node> input)
    {
        Node startLoopNode = nf.createTransitionNode();
        transitionGraph.addVertex(startLoopNode);
        Action enterLoopAction = naf.createEnterLoopAction();
        Action gotoAction = naf.createGotoAction();
        for (Node n : input)
            transitionGraph.addEdge(new ActionEdge<Node>(n, startLoopNode, enterLoopAction));

        List<HalfEdge> output = buildDLProgram((DLProgram) star.getChildAt(0), Arrays.asList(startLoopNode));
        List<Node> newNodes = new ArrayList<Node>();
        for (HalfEdge he : output) {
            Node newNode = nf.createTransitionNode();
            newNodes.add(newNode);
            transitionGraph.addVertex(newNode);
            transitionGraph.addEdge(new ActionEdge<Node>(he.node, newNode, he.action));
        }

        Node endIterationNode = nf.createTransitionNode();
        transitionGraph.addVertex(endIterationNode);
        Action commitIterationAction = naf.createCommitIterationAction();
        // committing current iteration
        for (Node n : newNodes)
            transitionGraph.addEdge(new ActionEdge<Node>(n, endIterationNode, commitIterationAction));
        output.clear();
        // escaping the loop early
        for (Node n : input) {
            HalfEdge he = new HalfEdge(n, gotoAction);
            output.add(he);
        }
        // going back for one more iteration
        transitionGraph.addEdge(new ActionEdge<Node>(endIterationNode, startLoopNode, gotoAction));

        Node endLoopNode = nf.createTransitionNode();
        transitionGraph.addVertex(endLoopNode);
        Action commitLoopAction = naf.createCommitLoopAction();
        transitionGraph.addEdge(new ActionEdge<Node>(endIterationNode, endLoopNode, commitLoopAction));

        HalfEdge he = new HalfEdge(endLoopNode, gotoAction);
        output.add(he);

        return output;
    }

    // time variant t* increments at different granularities
    private List<HalfEdge> buildDiffSystem(DiffSystem diffSystem, List<Node> input)
    {
        Term invariant = diffSystem.getInvariant(services);
        Action checkInvariant = naf.createVerifyAction(invariant, "invariant satisfied, proceed to DiffSystem transition");
        DiffSystemTransition dst = new SymbolicDST(diffSystem);
        //@TODO play with increments
        List<Real> increments =
            Arrays.asList(vf.valueOf(0.1), vf.valueOf(1), vf.valueOf(10));
        Node checkNode = nf.createTransitionNode();
        transitionGraph.addVertex(checkNode);
        for (Node n : input)
            transitionGraph.addEdge(new ActionEdge<Node>(n, checkNode, checkInvariant));
        List<Node> diffStarts = new ArrayList<Node>();
        Action gotoAction = naf.createGotoAction();
        List<HalfEdge> output = new ArrayList<HalfEdge>();
        // skip the DiffSystem
        output.add(new HalfEdge(checkNode, gotoAction));
        // building internal structure of the DiffSystem transition
        for (Real incr : increments) {
            Node startNode = nf.createTransitionNode();
            diffStarts.add(startNode);
            Node midNode = nf.createTransitionNode();
            Node endNode = nf.createTransitionNode();
            Action rollBackAction = naf.createRollBackAction();
            Action diffCommitAction = naf.createDiffCommitAction(diffSystem);

            transitionGraph.addVertices(Arrays.asList(startNode, midNode, endNode));
            transitionGraph.addEdge(new ActionEdge<Node>(startNode, midNode, naf.createDiffUpdateAction(dst, incr)));
            transitionGraph.addEdge(new ActionEdge<Node>(midNode, endNode, checkInvariant));
            transitionGraph.addEdge(new ActionEdge<Node>(endNode, startNode, rollBackAction));
            output.add(new HalfEdge(endNode, diffCommitAction));
        }
        // link input w/ starting nodes
        for (Node d : diffStarts)
            transitionGraph.addEdge(new ActionEdge<Node>(checkNode, d, gotoAction));

        return output;
    }

    Map<String, Integer> nextUnused = new HashMap<String, Integer>();

    // creates a new logic variable w/ a unique name
    // copied from prior code
    private synchronized LogicVariable createLogicVariable(String basename)
    {
        int next = 0;
        Name name;
        NamespaceSet nss = services.getNamespaces();
        if (nextUnused.containsKey(basename)) {
            next = nextUnused.get(basename);
            name = new Name(basename + next);
            nextUnused.put(basename, next+1);
        } else {
            do {
                name = new Name(basename+(next++));
            } while (nss.variables().lookup(name) != null ||
                nss.programVariables().lookup(name) != null);
            nextUnused.put(basename, next);
        }
        LogicVariable t = new LogicVariable(name, RealLDT.getRealSort());
        nss.variables().add(t);
        return t;
    }

    Node getInitialNode()
    {
        return initialNode;
    }

}
