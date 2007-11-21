/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel and Andre Platzer              *
 *   quesel@informatik.uni-oldenburg.de                                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
/**
 * File created 25.01.2007
 */
package de.uka.ilkd.key.dl.arithmetics.impl.mathematica;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.wolfram.jlink.Expr;
import com.wolfram.jlink.ExprFormatException;

import de.uka.ilkd.key.dl.arithmetics.IODESolver.ODESolverResult;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.PairOfTermAndQuantifierType;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.QuantifierType;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.IncompleteEvaluationException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnableToConvertInputException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.IKernelLinkWrapper.ExprAndMessages;
import de.uka.ilkd.key.dl.formulatools.VariableCollector;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.SubstOp;
import de.uka.ilkd.key.util.Debug;

/**
 * The MathematicaDLBridge is the implementation of the interface between KeY
 * and Mathematica. It connects to a (remote) server using RMI to use
 * Mathematica bindings.
 * 
 * @author jdq
 * @author ap
 * @since 25.01.2007
 * 
 */
public class MathematicaDLBridge extends UnicastRemoteObject implements
        IMathematicaDLBridge, ExprConstants {

    public static final String[] messageBlacklist = new String[] { "nsmet" };

    public static String mBlistString;

    static {
        String or = "";
        for (String str : messageBlacklist) {
            mBlistString = or + str;
            or = "|";
        }
    }

    private class Update {
        Term location;

        Term value;

    }

    /**
     * 
     */
    private static final long serialVersionUID = -6772275297933663232L;

    /**
     * @label RMI call
     */
    private IKernelLinkWrapper kernelWrapper;

    private String serverIP;

    private int port;

    /**
     * @directed
     */
    private Expr2TermConverter lnkExpr2TermConverter;

    /**
     * @directed
     */
    private VariableCollector lnkVariableCollector;

    /**
     * @directed
     */
    private Term2ExprConverter lnkTerm2ExprConverter;

    /**
     * @directed
     */
    private DL2ExprConverter lnkDL2ExprConverter;

    /**
     * Creates a new instance of the MathematicaDLBridge
     * 
     * @param serverIP
     *                the ip of the server running Mathematica
     * @param port
     *                the port using to connect to the server
     */
    public MathematicaDLBridge(String serverIP, int port)
            throws RemoteException {
        this.serverIP = serverIP;
        this.port = port;

    }

    private IKernelLinkWrapper getKernelWrapper() throws RemoteException {
        if (kernelWrapper == null) {
            Registry reg = LocateRegistry.getRegistry(serverIP, port);
            try {
                kernelWrapper = (IKernelLinkWrapper) reg
                        .lookup(KernelLinkWrapper.IDENTITY);
            } catch (NotBoundException e) {
                throw new RemoteException("Problem with KernelLink", e);
            }
        }
        return kernelWrapper;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IMathematicaDLBridge#odeSolve(de.uka.ilkd.key.dl.DiffSystem,
     *      de.uka.ilkd.key.logic.op.LogicVariable, de.uka.ilkd.key.logic.Term,
     *      de.uka.ilkd.key.logic.NamespaceSet)
     */
    public ODESolverResult odeSolve(DiffSystem form, LogicVariable t,
            LogicVariable ts, Term phi, NamespaceSet nss)
    throws RemoteException, SolverException {
        List<Expr> args = new ArrayList<Expr>();
        Map<String, Expr> vars = new HashMap<String, Expr>();

        collectDottedProgramVariables(form, vars, t);
        Term invariant = form.getInvariant();
        final Map<String, Expr> EMPTY = new HashMap<String, Expr>();
        for (ProgramElement el : form.getDifferentialEquations()) {
            args.add(DL2ExprConverter.convertDiffEquation(el, t, vars));
        }
        for (String name : vars.keySet()) {
            args.add(new Expr(EQUALS, new Expr[] {
                    new Expr(new Expr(Expr.SYMBOL, name),
                            new Expr[] { new Expr(0) }),
                    new Expr(Expr.SYMBOL, name + "$") }));
        }
        Expr query = new Expr(new Expr(Expr.SYMBOL, "DSolve"), new Expr[] {
                new Expr(new Expr(Expr.SYMBOL, "List"), args
                        .toArray(new Expr[1])),
                new Expr(new Expr(Expr.SYMBOL, "List"), vars.values().toArray(
                        new Expr[0])),
                new Expr(Expr.SYMBOL, t.name().toString()) });
        Expr updateExpressions = evaluate(query).expression;

        List<Update> updates = createUpdates(updateExpressions, nss);

        List<Term> locations = new ArrayList<Term>();
        List<Term> values = new ArrayList<Term>();
        List<String> varNames = new ArrayList<String>();
        for (String var : vars.keySet()) {
            varNames.add(var);
        }
        Map<String, Integer> multipleSolutions = new HashMap<String, Integer>();
        for (Update u : updates) {
            final String varName = u.location.op().name().toString();
            if (varNames.contains(varName)) {
                varNames.remove(varName);
            } else {
                Integer count = multipleSolutions.get(varName);
                if (count == null) {
                    count = 1;
                }
                count++;
                multipleSolutions.put(varName, count);
            }
            locations.add(u.location);
            values.add(u.value);
        }

        if (!varNames.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            builder
                    .append("No solutions for some variables of the differential equations: ");
            String comma = "";
            for (String v : varNames) {
                builder.append(comma + v);
                comma = ", ";
            }
            final String msg = builder.toString();
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    JOptionPane.showMessageDialog(Main.getInstance(), msg);
                }

            });
            throw new IllegalStateException("No solution for variables "
                    + varNames + " in: " + updateExpressions);
        } else if (!multipleSolutions.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            builder
                    .append("Found multiple solutions of differential equations: ");
            for (String v : multipleSolutions.keySet()) {
                builder.append("\n" + multipleSolutions.get(v)
                        + " solutions for " + v);
            }
            final String msg = builder.toString();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {

                    JOptionPane.showMessageDialog(Main.getInstance(), msg
                            + "\n First solution is to be used.");

                }

            });
        }

        invariant = TermBuilder.DF.tf().createSubstitutionTerm(
                SubstOp.SUBST,
                t,
                TermBuilder.DF.var(ts),
                de.uka.ilkd.key.logic.TermFactory.DEFAULT.createUpdateTerm(
                        locations.toArray(new Term[0]), values
                                .toArray(new Term[0]), invariant));
        invariant = ((SubstOp) invariant.op()).apply(invariant);
        // insert 0 <= ts <= t
        Term tsRange = convert(new Expr(INEQUALITY, new Expr[] { new Expr(0),
                LESS_EQUALS, new Expr(Expr.SYMBOL, ts.name().toString()),
                LESS_EQUALS, new Expr(Expr.SYMBOL, t.name().toString()) }), nss);
        invariant = TermBuilder.DF.imp(tsRange, invariant);
        invariant = TermBuilder.DF.all(ts, invariant);
        return new ODESolverResult(invariant,
                de.uka.ilkd.key.logic.TermFactory.DEFAULT.createUpdateTerm(
                        locations.toArray(new Term[0]), values
                                .toArray(new Term[0]), phi));

        // return \forall 0 <= t' <= t { solved diff equations } invariants ->
        // ({ solved diff equations } phi)
    }

    public Term diffInd(DiffSystem form, Term post, NamespaceSet nss)
    throws RemoteException, SolverException {
        return differentialCall(form, post, nss, "IDiffInd");
    }

    public Term diffFin(DiffSystem form, Term post, NamespaceSet nss)
    throws RemoteException, SolverException {
        Term invariant = form.getInvariant();
        if (!invariant.equals(TermBuilder.DF.tt()))
            throw new UnsupportedOperationException(
                    "not yet implemented for invariant!=true");
        return differentialCall(form, post, nss, "IDiffFin");
    }

    /**
     * 
     * @author ap
     * @param diffOperator
     *                the diff operator to apply in Mathematica package
     * @throws UnsolveableException
     * @throws ConnectionProblemException
     * @throws ServerStatusProblemException
     * @throws IncompleteEvaluationException 
     */
    private Term differentialCall(DiffSystem form, Term post, NamespaceSet nss,
            String diffOperator) throws RemoteException, SolverException {
        List<Expr> args = new ArrayList<Expr>();

        // use implicit differential symbols
        final LogicVariable t = null;
        Term invariant = form.getInvariant();
        final Map<String, Expr> EMPTY = new HashMap<String, Expr>();
        for (ProgramElement el : form.getDifferentialEquations()) {
            args.add(DL2ExprConverter.convertDiffEquation(el, t, EMPTY));
        }
        Expr loading = new Expr(new Expr(Expr.SYMBOL, "Needs"),
                new Expr[] { new Expr(Expr.STRING, "AMC`"),
                        new Expr(Expr.STRING, "~/AMC.m") });
        // evaluate(loading);
        if (Debug.ENABLE_DEBUG) {
            System.out.println(diffOperator
                    + ": "
                    + evaluate(new Expr(new Expr(Expr.SYMBOL, "AMC`"
                            + diffOperator), new Expr[] { Term2ExprConverter
                            .convert2Expr(post)
                    // new Expr(Expr.SYMBOL, t.name().toString())
                            })).expression);
        }
        Expr diffCall = new Expr(new Expr(Expr.SYMBOL, "AMC`" + diffOperator),
                new Expr[] {
                        Term2ExprConverter.convert2Expr(post),
                        // new Expr(Expr.SYMBOL, t.name().toString()),
                        new Expr(new Expr(Expr.SYMBOL, "List"), args
                                .toArray(new Expr[1])), });
        Expr query = new Expr(new Expr(Expr.SYMBOL, "CompoundExpression"),
                new Expr[] { loading, diffCall });
        Expr diffIndExpression = evaluate(query).expression;

        return TermBuilder.DF.imp(invariant, convert(diffIndExpression, nss));
    }

    public List<Update> createUpdates(Expr expr, NamespaceSet nss)
            throws RemoteException, SolverException {
        List<Update> result = new ArrayList<Update>();
        if (expr.head().equals(LIST)) {
            for (int i = 0; i < expr.args().length; i++) {
                result.addAll(createUpdates(expr.args()[i], nss));
            }
        } else if (expr.head().equals(RULE)) {
            Update u = new Update();
            try {
                de.uka.ilkd.key.logic.op.ProgramVariable var = (de.uka.ilkd.key.logic.op.ProgramVariable) nss
                        .programVariables().lookup(
                                new Name(expr.args()[0].head().asString()));
                if (var == null) {
                    // var = new de.uka.ilkd.key.logic.op.LocationVariable(
                    // new ProgramElementName(expr.args()[0].head()
                    // .asString()), getSortR(nss));
                    // nss.programVariables().add(var);
                    throw new IllegalStateException("ProgramVariable "
                            + expr.args()[0].head().asString()
                            + " is not declared");
                }
                u.location = TermBuilder.DF.var(var);
            } catch (ExprFormatException e) {
                throw new RemoteException("Could not create Update for: "
                        + expr, e);
            }
            u.value = convert(expr.args()[1], nss);
            result.add(u);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IMathematicaDLBridge#convert(com.wolfram.jlink.Expr)
     */
    public Term convert(Expr expr, NamespaceSet nss) throws RemoteException,
            SolverException {
        return Expr2TermConverter.convert(expr, nss,
                new HashMap<Name, LogicVariable>());
    }

    /**
     * Collect all program variables which are children of a Dot.
     * 
     * @param form
     *                the current root element.
     * 
     * @param vars
     *                the Map used for storing the result
     * @param t
     *                the variable used as time
     */
    public static final void collectDottedProgramVariables(ProgramElement form,
            Map<String, Expr> vars, LogicVariable t) {
        if (form instanceof Dot) {
            ProgramVariable pv = (ProgramVariable) ((Dot) form).getChildAt(0);
            vars.put(pv.getElementName().toString(), new Expr(new Expr(
                    Expr.SYMBOL, pv.getElementName().toString()),
                    new Expr[] { new Expr(Expr.SYMBOL, t.name().toString()) }));
        }
        if (form instanceof DLNonTerminalProgramElement) {
            DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) form;
            for (ProgramElement p : dlnpe) {
                collectDottedProgramVariables(p, vars, t);
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IMathematicaDLBridge#simplify(de.uka.ilkd.key.logic.Term,
     *      java.util.Set)
     */
    public Term simplify(Term form, Set<Term> assumptions)
    throws RemoteException, SolverException {
        Expr query = Term2ExprConverter.convert2Expr(form);
        Set<Expr> ass = new HashSet<Expr>();
        for (Term t : assumptions) {
            ass.add(Term2ExprConverter.convert2Expr(t));
        }
        query = new Expr(new Expr(Expr.SYMBOL, "Simplify"), new Expr[] { query,
                new Expr(LIST, ass.toArray(new Expr[0])) });
        Expr result = evaluate(query).expression;
        Term resultTerm = convert(result, Main.getInstance().mediator()
                .namespaces());
        if (!resultTerm.equals(form)) {
            return resultTerm;
        }
        return form;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IMathematicaDLBridge#fullSimplify(de.uka.ilkd.key.logic.Term)
     */
    public Term fullSimplify(Term form) throws RemoteException, SolverException{
        Expr query = Term2ExprConverter.convert2Expr(form);
        query = new Expr(new Expr(Expr.SYMBOL, "FullSimplify"),
                new Expr[] { query });
        Expr result = evaluate(query).expression;
        Term resultTerm = convert(result, Main.getInstance().mediator()
                .namespaces());
        if (!resultTerm.equals(form)) {
            return resultTerm;
        }
        return form;
    }

    private ExprAndMessages evaluate(final Expr expr) throws RemoteException, SolverException {
        ExprAndMessages evaluate;
        IKernelLinkWrapper wrapper = getKernelWrapper();
        try {
            evaluate = wrapper.evaluate(expr);
        } catch (RemoteException e) {
            Registry reg = LocateRegistry.getRegistry(serverIP, port);
            try {
                wrapper = (IKernelLinkWrapper) reg
                        .lookup(KernelLinkWrapper.IDENTITY);
            } catch (NotBoundException f) {
                throw new ConnectionProblemException("Problem with KernelLink",
                        f);
            }
            evaluate = wrapper.evaluate(expr);
        }
        if (!evaluate.messages.toString().equals("{}")) {
            System.err.println("Message while evaluating: " + expr
                    + "\n Message was: " + evaluate.messages); // XXX
        }
        if (evaluate.messages.toString().matches(".*" + mBlistString + ".*")) {
            throw new UnsolveableException(
                    "Mathematica could not solve the given expression: " + expr
                            + ". Reason: " + evaluate.messages.toString());
        }
        return evaluate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IMathematicaDLBridge#findInstance(de.uka.ilkd.key.logic.Term)
     */
    public String findInstance(Term form) throws RemoteException, SolverException {
        Expr query = Term2ExprConverter.convert2Expr(form);
        List<Expr> vars = new ArrayList<Expr>();
        for (String var : VariableCollector.getVariables(form)) {
            vars.add(new Expr(Expr.SYMBOL, var.replaceAll("_", USCORE_ESCAPE)));
        }
        if (vars.size() > 0) {
            query = new Expr(new Expr(Expr.SYMBOL, "FindInstance"), new Expr[] {
                    query, new Expr(LIST, vars.toArray(new Expr[0])),
                    new Expr(Expr.SYMBOL, "Reals") });
            Expr result = evaluate(query).expression;

            List<String> createFindInstanceString = createFindInstanceString(result);
            Collections.sort(createFindInstanceString);
            StringBuilder res = new StringBuilder();
            for (String s : createFindInstanceString) {
                res.append(s + "\n");
            }
            return res.toString();
        }
        return "";
    }

    /**
     * @param result
     * @return
     */
    private List<String> createFindInstanceString(Expr result) {
        List<String> resultList = new ArrayList<String>();
        if (result.head().equals(LIST)) {
            for (Expr r : result.args()) {
                resultList.addAll(createFindInstanceString(r));
            }
        } else if (result.head().equals(RULE)) {
            StringBuilder str = new StringBuilder();
            str.append(result.args()[0].toString().replaceAll(USCORE_ESCAPE,
                    "_"));
            str.append(" = ");
            if (result.args()[1].head().equals(RATIONAL)) {
                str.append(result.args()[1].args()[0] + "/"
                        + result.args()[1].args()[1]);
            } else {
                str.append(result.args()[1]);
            }
            resultList.add(str.toString());
        } else {
            resultList.add(result.toString());
        }
        return resultList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IMathematicaDLBridge#abortCalculation()
     */
    public void abortCalculation() throws RemoteException {
        getKernelWrapper().interruptCalculation();
    }

    public String getTimeStatistics() throws RemoteException {
        return getKernelWrapper().getTimeStatistics();
    }

    public long getTotalCalculationTime() throws RemoteException {
        return getKernelWrapper().getTotalCalculationTime();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IMathematicaDLBridge#getCachedAnwserCount()
     */
    public long getCachedAnwserCount() throws RemoteException {
        return getKernelWrapper().getCachedAnwsers();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IMathematicaDLBridge#getQueryCount()
     */
    public long getQueryCount() throws RemoteException {
        return getKernelWrapper().getCallCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IMathematicaDLBridge#resetAbortState()
     */
    public void resetAbortState() throws RemoteException {
        try {
            getKernelWrapper().resetAbortState();
        } catch (Exception e) {
            System.err.println("Exception occerred in resetAbortState");// XXX
            e.printStackTrace();
            System.err.println("In most cases this can safely be ignored"); // XXX
        }
    }

    public Term reduce(Term form, List<String> additionalReduce,
            List<PairOfTermAndQuantifierType> quantifiers)
    throws RemoteException, SolverException{
        Expr query = Term2ExprConverter.convert2Expr(form);
        List<Expr> vars = new ArrayList<Expr>();
        for (PairOfTermAndQuantifierType pair : quantifiers) {
            Expr convert2Expr = Term2ExprConverter.convert2Expr(pair.term);
            vars.add(convert2Expr);
            query = new Expr((pair.type == QuantifierType.FORALL) ? FORALL
                    : EXISTS, new Expr[] {
                    new Expr(LIST, new Expr[] { convert2Expr }), query });
        }
        for (String name : additionalReduce) {
            String sym = name.replaceAll("_", USCORE_ESCAPE);
            vars.add(new Expr(Expr.SYMBOL, sym));
        }
        Expr arg3 = new Expr(Expr.SYMBOL, "Reals");
        Expr[] argList = new Expr[] { query, };
        if (Options.INSTANCE.getQuantifierEliminationMethod().isSupportsList()) {
            argList = new Expr[] {
                    query,
                    new Expr(LIST, Options.INSTANCE.isUseEliminateList() ? vars
                            .toArray(new Expr[0]) : new Expr[0]), arg3 };
        }
        query = new Expr(new Expr(Expr.SYMBOL, Options.INSTANCE
                .getQuantifierEliminationMethod().toString()), argList);
        // query = new Expr(new Expr(Expr.SYMBOL, "Reduce"), new Expr[] { query,
        // new Expr(LIST, vars.toArray(new Expr[0])),
        // new Expr(Expr.SYMBOL, "Reals") });
        Expr result = evaluate(query).expression;
        Term resultTerm = convert(result, Main.getInstance().mediator()
                .namespaces());
        if (!resultTerm.equals(form)) {
            return resultTerm;
        }
        return form;
    }
}
