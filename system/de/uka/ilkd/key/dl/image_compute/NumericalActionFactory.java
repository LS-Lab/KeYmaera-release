/**
 * NumericalActionFactory that creates instances of Action objects.
 *
 * @author jyn (jingyin@andrew.cmu.edu)
 */

package de.uka.ilkd.key.dl.image_compute;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Random;

import orbital.math.Real;
import orbital.math.ValueFactory;
import de.uka.ilkd.key.dl.image_compute.Evaluator.SymbolAbsentHandler;
import de.uka.ilkd.key.dl.image_compute.TransitionGraph.DiffSystemTransition;
import de.uka.ilkd.key.dl.image_compute.TransitionGraph.PostCond;
import de.uka.ilkd.key.dl.model.Assign;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.RandomAssign;
import de.uka.ilkd.key.dl.model.VariableDeclaration;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.QuanUpdateOperator;

class NumericalActionFactory
{

    private static final ValueFactory vf = MachValueFactory.getInstance();
    private static final TermBuilder tb = TermBuilder.DF;
    private final Random rand;
    private final Evaluator ev;

    /**
     * The action object passed to a state at each commit.
     */
    public interface ActionCommit
    {
    }

    public static class AssignCommit implements ActionCommit
    {
        private String str;

        public AssignCommit(String var, ProgramElement expr)
        {
            str = var + " := " + prettyPrint(expr);
        }

        @Override
        public String toString()
        {
            return str;
        }
    }

    public static class RandAssignCommit implements ActionCommit
    {
        private String str;
        private String var;
        private Real val;

        public RandAssignCommit(String var, Real val)
        {
            str = var + " := * (" + val + ")";
            this.var = var;
            this.val = val;
        }

        @Override
        public String toString()
        {
            return str;
        }
    }

    public static class EvalCommit implements ActionCommit
    {
        private String str;

        public EvalCommit(Term condition, boolean expected, String additional)
        {
            str = prettyPrint(condition) + " evaluated to " + expected + ", " + additional;
        }

        public EvalCommit(ProgramElement condition, boolean expected, String additional)
        {
            str = prettyPrint(condition) + " evaluated to " + expected + ", " + additional;
        }

        @Override
        public String toString()
        {
            return str;
        }
    }

    public static class DiffSystemCommit implements ActionCommit
    {
        private String str;

        public DiffSystemCommit(DiffSystem diffSystem, Real t)
        {
            str = diffSystem + " for " + t + " time steps";
        }

        @Override
        public String toString()
        {
            return str;
        }
    }

    public static class FindInstanceCommit implements ActionCommit
    {
        private String str;
        private Term expr;

        public FindInstanceCommit(Term expr)
        {
            str = "FI[" + prettyPrint(expr) + "]";
            this.expr = expr;
        }

        @Override
        public String toString()
        {
            return str;
        }
    }

    /*public static class LoopCommit implements ActionCommit
    {
        private String str;
        private List<IterationCommit> iterations;

        public LoopCommit()
        {
            str = "loop";
            iterations = new ArrayList<IterationCommit>();
        }

        public LoopCommit(LoopCommit lc)
        {
            str = lc.str;
            iterations = new ArrayList<IterationCommit>(lc.iterations);
        }

        public void addIteration(IterationCommit ic)
        {
            iterations.add(ic);
        }

        public List<IterationCommit> getIterations()
        {
            return iterations;
        }

        @Override
        public String toString()
        {
            return str;
        }
    }

    public static class IterationCommit
    {
        private List<Commit> commits;
        private String str;
        private int idx;

        public IterationCommit(int idx)
        {
            str = "iteration " + idx;
            this.idx = idx;
            commits = new ArrayList<Commit>();
        }

        public IterationCommit(IterationCommit ic)
        {
            idx = ic.idx;
            str = ic.str;
            commits = new ArrayList<Commit>(ic.commits);
        }

        public int getIdx()
        {
            return idx;
        }

        public void addCommit(Commit c)
        {
            commits.add(c);
        }

        public List<Commit> getCommit()
        {
            return commits;
        }

        @Override
        public String toString()
        {
            return str;
        }
    }*/

    private NumericalActionFactory(Evaluator ev)
    {
        rand = new Random();
        this.ev = ev;
    }

    private static String prettyPrint(Term expr)
    {
        // TODO
        return expr.toString();
    }

    private static String prettyPrint(ProgramElement expr)
    {
        StringWriter sw = new StringWriter();
        try {
            ((DLProgramElement)expr).prettyPrint(new PrettyPrinter(sw));
        } catch (IOException ioEx) {
            System.err.println(ioEx);
            ioEx.printStackTrace();
            return expr.toString();
        }
        return sw.toString();
    }

    /**
     * Returns an instance of a NumericalActionFactory.
     */
    static NumericalActionFactory getInstance(Evaluator ev)
    {
        return new NumericalActionFactory(ev);
    }

    /**
     * Creates an action that checks a certain condition.
     */
    Action createVerifyAction(ProgramElement condition, String additional)
    {
        return new VerifyAction(condition, additional);
    }

    /**
     * If a given condition is not satisfied, given state is marked terminated and
     * evaluated to be false; otherwise, it's a no-op.
     */
    private class VerifyTermAction extends Action
    {
        private final Term condition;
        private final String additional;

        VerifyTermAction(Term condition)
        {
            this(condition, "");
        }

        VerifyTermAction(Term condition, String additional)
        {
            this.condition = condition;
            this.additional = additional;
        }

        @Override
        void apply(NumericalState state)
        {
            if (!ev.evalCond(state, condition)) {
                state.setTerminated();
                state.setEvaluated(false);
                return;
            }
            //state.commit(new EvalCommit(condition, true, additional));
        }

        @Override
        public String toString()
        {
            return "verifying condition " + condition;
        }
    }

    /**
     * Creates an action that checks a certain condition.
     */
    Action createVerifyAction(Term condition, String additional)
    {
        return new VerifyTermAction(condition, additional);
    }

    /**
     * Creates an action that checks the negation of a certain condition.
     */
    Action createUnverifyAction(ProgramElement condition, String additional)
    {
        return new UnverifyAction(condition, additional);
    }

    /**
     * If a given condition is not satisfied, given state is marked terminated and
     * evaluated to be false; otherwise, it's a no-op.
     */
    private class VerifyAction extends Action
    {
        private final ProgramElement condition;
        private final String additional;

        VerifyAction(ProgramElement condition, String additional)
        {
            this.condition = condition;
            this.additional = additional;
        }

        @Override
        void apply(NumericalState state)
        {
            if (!ev.evalCond(state, condition)) {
                state.setTerminated();
                state.setEvaluated(false);
                return;
            }
            //state.commit(new EvalCommit(condition, true, additional));
        }

        @Override
        public String toString()
        {
            return "verifying condition " + condition;
        }
    }

    /**
     * If a given condition is satisfied, given state is makred terminated and evaluated
     * to be false; otherwise, it's a no-op.
     */
    private class UnverifyAction extends Action
    {
        private final ProgramElement condition;
        private final String additional;

        UnverifyAction(ProgramElement condition, String additional)
        {
            this.condition = condition;
            this.additional = additional;
        }

        @Override
        void apply(NumericalState state)
        {
            if (ev.evalCond(state, condition)) {
                state.setTerminated();
                state.setEvaluated(false);
                return;
            }
            //state.commit(new EvalCommit(condition, false, additional));
        }

        @Override
        public String toString()
        {
            return "verifying condition " + condition + " false";
        }
    }

    /**
     * Creates an action that evaluates a certain condition.
     *
     * Whether the condition holds or not would determine whether we can terminate the
     * search or continue onward to a deeper level of nested program.
     */
    Action createEvalAction(PostCond postCond)
    {
        return new EvalAction(postCond);
    }

    /**
     * Action that evaluates a given condition (if any).
     *
     * See the rules of evaluation in CounterExampleFinder.java.
     */
    private class EvalAction extends Action
    {
        private PostCond postCond;

        EvalAction(PostCond postCond)
        {
            this.postCond = postCond;
        }

        // description of the action
        private String name()
        {
            String connective = "";
            switch (postCond.type) {
            case FIRST_ORDER_TYPE:
                break;
            case AND_TYPE:
                connective = "and";
                break;
            case OR_TYPE:
                connective = "or";
                break;
            default:
                assert(false);
                break;
            }
            return postCond.expr + (connective.equals("") ? "" : " ") +
                connective + (postCond.program != null ? " " + postCond.program : "");
        }

        @Override
        void apply(NumericalState state)
        {
            boolean result;
            SymbolAbsentHandler saHandler;

            switch (postCond.type) {
            case FIRST_ORDER_TYPE:
                result = ev.evalCond(state, postCond.expr);
                state.setTerminated();
                state.setEvaluated(result);
                break;
            case AND_TYPE:
                saHandler = Evaluator.getFindInstanceSAHandler(postCond.expr, ev.getServices());
                assert(saHandler != null);
                ev.setSAHandler(saHandler);
                try {
                    result = ev.evalCond(state, postCond.expr);
                } catch (Exception e) {
                    System.err.println("exp: " + e);
                    e.printStackTrace();
                    state.setTerminated();
                    state.setEvaluated(false);
                    return;
                }
                if (!saHandler.invoked()) {
                    if (!result) {
                        state.setTerminated();
                        state.setEvaluated(false);
                    }
                    return;
                }
                // otherwise, we have to evaluate the nested program with postCond.expr
                // serving as pre-condition
                assert(ev.getDefaultSAHandler() != null);
                ev.setSAHandler(ev.getDefaultSAHandler());
                break;
            case OR_TYPE:
                saHandler = Evaluator.getFindInstanceSAHandler(tb.not(postCond.expr), ev.getServices());
                assert(saHandler != null);
                ev.setSAHandler(saHandler);
                try {
                    result = ev.evalCond(state, tb.not(postCond.expr));
                } catch (Exception e) {
                    System.err.println("exp: " + e);
                    e.printStackTrace();
                    state.setTerminated();
                    state.setEvaluated(false);
                    return;
                }
                if (!saHandler.invoked()) {
                    if (!result) {
                        state.setTerminated();
                        state.setEvaluated(true);
                    }
                }
                // otherwise, we have to evaluate the nested program with !postCond.expr
                // serving as pre-condition
                assert(ev.getDefaultSAHandler() != null);
                ev.setSAHandler(ev.getDefaultSAHandler());
                break;
            case NESTED_TYPE:
                break;
            default:
                throw new UnsupportedOperationException("unknown post-cond type: " + postCond.type);
            }
            if (state.getTerminated())
            {
                System.err.println(state);
                state.commit(new EvalCommit(tb.not(postCond.expr), !state.getEvaluated(), "post-condition " + (state.getEvaluated() ? "failure" : "success")));
            }
        }

        @Override
        public String toString()
        {
            return "evaluating condition " + postCond.expr;
        }
    }

    /**
     * Creates an action that performs quantified updates, aka parallel updates.
     */
    Action createQuanUpdateAction(Term quanUpdate)
    {
        return new QuanUpdateAction(quanUpdate);
    }

    /**
     * Action that carries out parallel updates.
     *
     * Parallel means that when updates are carried out "sequentially" (as it's done),
     * we must use the old values on the RHS and not the updated symbol values.
     */
    private class QuanUpdateAction extends Action
    {
        private Term quanUpdate;

        QuanUpdateAction(Term quanUpdate)
        {
            this.quanUpdate = quanUpdate;
        }

        @Override
        void apply(NumericalState state)
        {
            QuanUpdateOperator top = (QuanUpdateOperator)quanUpdate.op();
            for (int i = 0; i < quanUpdate.arity() - 1; i++)
                state.setSymbol(top.location(quanUpdate, i).toString(), ev.evalExpr(state, quanUpdate.sub(i)));
        }

        @Override
        public String toString()
        {
            return "quantified update " + quanUpdate;
        }
    }

    private Action enterLoopAction = new EnterLoopAction();

    Action createEnterLoopAction()
    {
        return enterLoopAction;
    }

    private Action commitIterationAction = new CommitIterationAction();

    Action createCommitIterationAction()
    {
        return commitIterationAction;
    }

    private Action commitLoopAction = new CommitLoopAction();

    Action createCommitLoopAction()
    {
        return commitLoopAction;
    }

    private Action rollBackAction = new RollBackAction();

    Action createRollBackAction()
    {
        return rollBackAction;
    }

    Action createDiffCommitAction(DiffSystem ds)
    {
        return new DiffCommitAction(ds);
    }

    class DiffCommitAction extends Action
    {
        DiffSystem ds;

        DiffCommitAction(DiffSystem ds)
        {
            this.ds = ds;
        }

        @Override
        void apply(NumericalState state)
        {
            state.commit(new DiffSystemCommit(ds, state.getTime()));
            state.setTime(vf.valueOf(0.0));
        }

        @Override
        public String toString()
        {
            return "committing DiffSystem transition";
        }
    }

    class RollBackAction extends Action
    {
        @Override
        void apply(NumericalState state)
        {
            state.rollBack();
        }

        @Override
        public String toString()
        {
            return "rolling back";
        }
    }

    class EnterLoopAction extends Action
    {
        @Override
        void apply(NumericalState state)
        {
            state.enterLoop();
        }

        @Override
        public String toString()
        {
            return "entering loop";
        }
    }

    class CommitIterationAction extends Action
    {
        @Override
        void apply(NumericalState state)
        {
            state.commitIteration();
        }

        @Override
        public String toString()
        {
            return "commiting iteration";
        }
    }

    class CommitLoopAction extends Action
    {
        @Override
        void apply(NumericalState state)
        {
            state.commitLoop();
        }

        @Override
        public String toString()
        {
            return "commiting loop";
        }
    }

    private Action gotoAction = new GotoAction();

    /**
     * Creates an no-op Action.
     */
    Action createGotoAction()
    {
        return gotoAction;
    }

    /**
     * A no-op action.
     *
     * In the context of a transition graph, it means go from one node to another and
     * doing nothing.
     */
    private class GotoAction extends Action
    {
        @Override
        void apply(NumericalState state)
        {
        }

        @Override
        public String toString()
        {
            return "goto: no-op";
        }
    }

    /**
     * Creates an action that carries out updates of DiffSystem transition.
     */
    Action createDiffUpdateAction(DiffSystemTransition dst, Real increment)
    {
        return new DiffUpdateAction(dst, increment);
    }

    /**
     * Action that updates a DiffSystem transition at a certain time increment.
     */
    private class DiffUpdateAction extends Action
    {
        private final DiffSystemTransition dst;
        private final Real increment;

        DiffUpdateAction(DiffSystemTransition dst, Real increment)
        {
            this.dst = dst;
            this.increment = increment;
        }

        @Override
        void apply(NumericalState state)
        {
            Real currTime = state.getTime().add(increment);
            dst.apply(currTime, state);
            state.setTime(currTime);
        }

        @Override
        public String toString()
        {
            return "diff update at increment " + increment;
        }
    }

    //
    // Actions that mimic statements in a DLProgram.
    //

    /**
     * Creates an Assign action.
     */
    Action createProgramAction(Assign assign)
    {
        return new AssignAction(assign);
    }

    private class AssignAction extends Action
    {
        private final Assign assign;

        AssignAction(Assign assign)
        {
            this.assign = assign;
        }

        @Override
        void apply(NumericalState state)
        {
            Real rhs = ev.evalExpr(state, assign.getChildAt(1));
            String var = assign.getChildAt(0).toString();
            state.setSymbol(var, rhs);
            state.commit(new AssignCommit(var, assign.getChildAt(1)));
        }

        @Override
        public String toString()
        {
            return "assignment " + assign;
        }
    }

    /**
     * Creates a VariableDeclaration action.
     */
    Action createProgramAction(VariableDeclaration varDecl)
    {
        return new VarDeclAction(varDecl);
    }

    /**
     * A no-op action since we don't really declare variables.
     */
    private class VarDeclAction extends Action
    {
        private final VariableDeclaration varDecl;

        VarDeclAction(VariableDeclaration varDecl)
        {
            this.varDecl = varDecl;
        }

        @Override
        void apply(NumericalState state)
        {
        }

        @Override
        public String toString()
        {
            return "variable declaration " + varDecl;
        }
    }

    /**
     * Creates a RandomAssign action.
     */
    Action createProgramAction(RandomAssign randAssign)
    {
        return new RandAssignAction(randAssign);
    }

    /**
     * Action that assigns a random real to a given variable.
     */
    private class RandAssignAction extends Action
    {
        private final RandomAssign randAssign;

        RandAssignAction(RandomAssign randAssign)
        {
            this.randAssign = randAssign;
        }

        @Override
        void apply(NumericalState state)
        {
            Real randVal = ev.getRandReal();
            String var = randAssign.getChildAt(0).toString();
            state.setSymbol(var, randVal);
            state.commit(new RandAssignCommit(var, randVal));
        }

        @Override
        public String toString()
        {
            return "random assignment " + randAssign;
        }
    }

}
