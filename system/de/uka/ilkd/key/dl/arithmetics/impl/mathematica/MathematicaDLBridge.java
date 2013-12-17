/***************************************************************************
 *   Copyright (C) 2007-2012 by Jan-David Quesel and Andre Platzer              *
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

import java.awt.Frame;
import java.io.IOException;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.wolfram.jlink.Expr;
import com.wolfram.jlink.ExprFormatException;

import de.uka.ilkd.key.dl.arithmetics.IODESolver.ODESolverResult;
import de.uka.ilkd.key.dl.arithmetics.IODESolver.ODESolverUpdate;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.PairOfTermAndQuantifierType;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.QuantifierType;
import de.uka.ilkd.key.dl.arithmetics.abort.ServerConsole;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.FailedComputationException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.IncompleteEvaluationException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;
import de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker.PolynomialClassification;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.Expr2TermConverter.UnknownMathFunctionException;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.IKernelLinkWrapper.ExprAndMessages;
import de.uka.ilkd.key.dl.formulatools.DerivativeCreator;
import de.uka.ilkd.key.dl.formulatools.collector.AllCollector;
import de.uka.ilkd.key.dl.formulatools.collector.filter.FilterVariableCollector;
import de.uka.ilkd.key.dl.image_compute.CounterExampleFinder;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Named;
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
	
	private static final boolean DEBUG = false;

	public static final String[] messageBlacklist = new String[] { "nsmet" };

	public static String mBlistString;

	public List<CounterExampleFinder> cexFinders = new ArrayList<CounterExampleFinder>();

	static {
		String or = "";
		for (String str : messageBlacklist) {
			mBlistString = or + str;
			or = "|";
		}
	}

	private static class Update {
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

	private ServerConsole serverConsole;

	/**
	 * Creates a new instance of the MathematicaDLBridge
	 * 
	 * @param serverIP
	 *            the ip of the server running Mathematica
	 * @param port
	 *            the port using to connect to the server
	 */
	public MathematicaDLBridge(String serverIP, int port)
			throws RemoteException {
		this.serverIP = serverIP;
		this.port = port;

	}

	private IKernelLinkWrapper getKernelWrapper() throws RemoteException {
		if (kernelWrapper == null || !Options.INSTANCE.getMathKernel().isFile()) {
//			try {
//				// We just call a method to check if the server is alive
//				ISimplifier simplifier = MathSolverManager
//						.getSimplifier("Mathematica");
//				if (simplifier != null) {
//					simplifier.getQueryCount();
//				}
//			} catch (RemoteException e1) {
				try {
					KernelLinkWrapper.main(new String[] {
							"--mathcall",
							"-linkmode launch -linkname '"
									+ Options.INSTANCE.getMathKernel()
									+ " -mathlink'" });
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//			}
			Registry reg = LocateRegistry.getRegistry(serverIP, port);
			try {
				kernelWrapper = (IKernelLinkWrapper) reg
						.lookup(KernelLinkWrapper.IDENTITY);
				try {
					serverConsole = new ServerConsole("Server Console", new String[] {"server=" + serverIP, "port=" + port}, false);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (NotBoundException e) {
				throw new RemoteException("Problem with KernelLink", e);
			}
		}
		return kernelWrapper;
	}
	
	public void toggleServerConsole() {
		if(serverConsole == null) {
			try {
				getKernelWrapper();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		serverConsole.setVisible(!serverConsole.isVisible());
	}

    /*
     * (non-Javadoc)
     *
     * @see
     * de.uka.ilkd.key.dl.IODESolver#odeODESolverUpdate(de.uka.ilkd.key.dl.
     * DiffSystem, de.uka.ilkd.key.logic.op.LogicVariable,
     * de.uka.ilkd.key.logic.Term, java.Services)
     */
    @Override
    public List<ODESolverUpdate> odeUpdate(DiffSystem form, LogicVariable t,
            Services services, long timeout)
            throws RemoteException, SolverException {
        List<Expr> args = new ArrayList<Expr>();
        Map<String, Expr> vars = new LinkedHashMap<String, Expr>();

        collectDottedProgramVariables(form, vars, t);
        for (ProgramElement el : form.getDifferentialEquations(services.getNamespaces()))
            args.add(DL2Expr.apply(el, t, vars, services));
        for (String name : vars.keySet())
            args.add(new Expr(EQUALS, new Expr[] {
                        new Expr(new Expr(Expr.SYMBOL, name), new Expr[] { new Expr(0) }),
                        new Expr(Expr.SYMBOL, name + "$") }));
        String name = t.name().toString();
        name = NameMasker.mask(name);
        Expr query = new Expr(new Expr(Expr.SYMBOL, "DSolve"), new Expr[] {
                        new Expr(new Expr(Expr.SYMBOL, "List"), args.toArray(new Expr[1])),
                        new Expr(new Expr(Expr.SYMBOL, "List"), vars.values().toArray(new Expr[0])),
                        new Expr(Expr.SYMBOL, name) });
        Expr updateExpressions = evaluate(query, timeout).expression;

        return createODESolverUpdates(updateExpressions, services.getNamespaces());
    }

    public List<ODESolverUpdate> createODESolverUpdates(Expr expr,
			NamespaceSet nss) throws RemoteException, SolverException {
		List<ODESolverUpdate> result = new ArrayList<ODESolverUpdate>();
		if (expr.toString().equalsIgnoreCase("$Aborted")
				|| expr.toString().contains("Abort[]")) {
			throw new IncompleteEvaluationException("Calculation aborted!");
		}
		if (expr.head().equals(LIST)) {
			for (int i = 0; i < expr.args().length; i++) {
				result.addAll(createODESolverUpdates(expr.args()[i], nss));
			}
		} else if (expr.head().equals(RULE)) {
			ODESolverUpdate u = new ODESolverUpdate();
			try {
                String name = expr.args()[0].head().asString();
                if (NameMasker.isMasked(name)) {
                    name = NameMasker.unmask(name);
                    de.uka.ilkd.key.logic.op.ProgramVariable var = (de.uka.ilkd.key.logic.op.ProgramVariable) nss
                            .programVariables().lookup(new Name(name));
                    if (var == null) {
                        // var = new de.uka.ilkd.key.logic.op.LocationVariable(
                        // new ProgramElementName(expr.args()[0].head()
                        // .asString()), getSortR(nss));
                        // nss.programVariables().add(var);
                        throw new IllegalStateException("ProgramVariable "
                                + name
                                + " is not declared");
                    }
                    u.location = TermBuilder.DF.var(var);
                }
			} catch (ExprFormatException e) {
				throw new RemoteException(
						"Could not create ODESolverUpdate for: " + expr, e);
			}
			u.expr = convert(expr.args()[1], nss);
			result.add(u);
		} else if (Expr2TermConverter.isBlacklisted(expr)) {
			throw new UnsolveableException("Blacklisted conversion from "
					+ expr.toString() + " to updates");
		} else {
			throw new IllegalStateException("unknown case " + expr.head()
					+ " in\n" + expr);
		}
		return result;
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uka.ilkd.key.dl.IMathematicaDLBridge#odeSolve(de.uka.ilkd.key.dl.
	 * DiffSystem, de.uka.ilkd.key.logic.op.LogicVariable,
	 * de.uka.ilkd.key.logic.Term, de.uka.ilkd.key.logic.NamespaceSet)
	 */
	public ODESolverResult odeSolve(DiffSystem form, LogicVariable t,
			LogicVariable ts, Term phi, Services services)
			throws RemoteException, SolverException {
		List<Expr> args = new ArrayList<Expr>();
		Map<String, Expr> vars = new LinkedHashMap<String, Expr>();

		collectDottedProgramVariables(form, vars, t);
		Term invariant = form.getInvariant(services);
		final Map<String, Expr> EMPTY = new LinkedHashMap<String, Expr>();
		for (ProgramElement el : form.getDifferentialEquations(services
				.getNamespaces())) {
			args.add(DL2Expr.apply(el, t, vars, services));
		}
		for (String name : vars.keySet()) {
			args.add(new Expr(EQUALS, new Expr[] {
					new Expr(new Expr(Expr.SYMBOL, name),
							new Expr[] { new Expr(0) }),
					new Expr(Expr.SYMBOL, name + "$") }));
		}
		String name = t.name().toString();
		name = NameMasker.mask(name);
		Expr query = new Expr(new Expr(Expr.SYMBOL, "DSolve"), new Expr[] {
				new Expr(new Expr(Expr.SYMBOL, "List"), args
						.toArray(new Expr[1])),
				new Expr(new Expr(Expr.SYMBOL, "List"), vars.values().toArray(
						new Expr[0])), new Expr(Expr.SYMBOL, name) });
		Expr updateExpressions = evaluate(query).expression;

		List<Update> updates = createUpdates(updateExpressions, services
				.getNamespaces());

		List<Term> locations = new ArrayList<Term>();
		List<Term> values = new ArrayList<Term>();
		List<String> varNames = new ArrayList<String>();
		for (String var : vars.keySet()) {
			varNames.add(NameMasker.unmask(var));
		}
		Map<String, Integer> multipleSolutions = new LinkedHashMap<String, Integer>();
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

		String multiple = null;
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
			if (!services.autoMode()) {
				SwingUtilities.invokeLater(new Runnable() {

					public void run() {
						JOptionPane.showMessageDialog(Frame.getFrames()[0], msg);
					}

				});
			}
			throw new UnsolveableException("No solution for variables "
					+ varNames + " in: " + updateExpressions);
		} else if (!multipleSolutions.isEmpty()) {
			StringBuilder builder = new StringBuilder();
			builder
					.append("Found multiple solutions of differential equations: ");
			for (String v : multipleSolutions.keySet()) {
				builder.append("\n" + multipleSolutions.get(v)
						+ " solutions for " + v);
			}
			multiple = builder.toString();
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
				LESS_EQUALS, new Expr(Expr.SYMBOL, NameMasker.mask(ts.name().toString())),
				LESS_EQUALS, new Expr(Expr.SYMBOL, NameMasker.mask(t.name().toString())) }),
				services.getNamespaces());
		invariant = TermBuilder.DF.imp(tsRange, invariant);
		invariant = TermBuilder.DF.all(ts, invariant);
		return multiple != null ? 
  		    new ODESolverResult(invariant,
				de.uka.ilkd.key.logic.TermFactory.DEFAULT.createUpdateTerm(
						locations.toArray(new Term[0]), values
								.toArray(new Term[0]), phi), multiple)
			:
  		    new ODESolverResult(invariant,
				de.uka.ilkd.key.logic.TermFactory.DEFAULT.createUpdateTerm(
						locations.toArray(new Term[0]), values
								.toArray(new Term[0]), phi));

		// return \forall 0 <= t' <= t { solved diff equations } invariants ->
		// ({ solved diff equations } phi)
	}

	@Override
	public Term[] pdeSolve(DiffSystem form, LogicVariable t, Services services) throws RemoteException, SolverException {
		List<Expr> args = new ArrayList<Expr>();
		Map<String, Expr> vars = new LinkedHashMap<String, Expr>();

		collectDottedProgramVariables(form, vars);

		final Expr f = new Expr(Expr.SYMBOL, "$f");
		final Term invariant = form.getInvariant(services);
		Map<String, Term> replacements = new LinkedHashMap<String, Term>();
		DerivativeCreator.collectDiffReplacements(form, replacements, services);
		for (Map.Entry<String, Term> vt : replacements.entrySet()) {
			args.add(new Expr(new Expr(Expr.SYMBOL, "Times"), new Expr[] {
				Term2Expr.apply(vt.getValue(), false),
				//DL2Expr.apply((ProgramElement) vt.getValue(), t, vars, services),
				new Expr(new Expr(Expr.SYMBOL, "D"), new Expr[] {
					new Expr(f, vars.values().toArray(new Expr[0])),
					new Expr(Expr.SYMBOL, NameMasker.mask(vt.getKey()))
				})
		    }));
		}
		Expr pde = new Expr(new Expr(Expr.SYMBOL, "Equal"), new Expr[] {
			new Expr(new Expr(Expr.SYMBOL, "Plus"), args
					.toArray(new Expr[1])),
			new Expr(BigInteger.ZERO)
		});
		final Expr genParam = new Expr(Expr.SYMBOL,"$C");
		Expr query = new Expr(new Expr(Expr.SYMBOL, "DSolve"), new Expr[] {
				pde,
			    new Expr(f, vars.values().toArray(new Expr[0])),
				new Expr(new Expr(Expr.SYMBOL, "List"), vars.values().toArray(
						new Expr[0])),
			    new Expr(new Expr(Expr.SYMBOL, "Rule"), new Expr[] {
			    	new Expr(Expr.SYMBOL, "GeneratedParameters"),
			    	genParam
			    })
				});
		final Expr subfinder = new Expr(new Expr(Expr.SYMBOL, "Rule"), new Expr[] {
			new Expr(
					new Expr(genParam, new Expr[] {new Expr(new Expr(Expr.SYMBOL, "Pattern"), new Expr[]{new Expr(Expr.SYMBOL, "n"), new Expr(new Expr(Expr.SYMBOL,"Blank"), new Expr[] {})})}),
					new Expr[] {new Expr(new Expr(Expr.SYMBOL, "Pattern"), new Expr[]{new Expr(Expr.SYMBOL, "e"), new Expr(new Expr(Expr.SYMBOL,"BlankNullSequence"), new Expr[] {})})}),
			new Expr(new Expr(Expr.SYMBOL, "Apply"), new Expr[] {
				new Expr(Expr.SYMBOL, "List"),
				new Expr(Expr.SYMBOL, "e")
			    })
		});
		query = new Expr(new Expr(Expr.SYMBOL, "Module"), new Expr[] {
			new Expr(new Expr(Expr.SYMBOL,"List"), new Expr[] {genParam}),
			new Expr(new Expr(Expr.SYMBOL, "Cases"), new Expr[] {
				query,
				subfinder,
				new Expr(Expr.SYMBOL, "Infinity")
			})});
		if (DEBUG) {System.out.println("evaluating " + query);}
		Expr expressions = evaluate(query).expression;
		if (expressions.toString().equalsIgnoreCase("$Aborted")
				|| expressions.toString().contains("Abort[]")) {
			throw new IncompleteEvaluationException("Calculation aborted!");
		}
		if (expressions.head().equals(LIST)) {
			List<Term> result = new LinkedList<Term>();
			for (int i = 0; i < expressions.args().length; i++) {
				if (DEBUG) System.out.println("found " + expressions.args()[i]);
				result.add(convert(expressions.args()[i], services.getNamespaces()));
			}
			return result.toArray(new Term[0]);
		} else
			throw new FailedComputationException("Unexpected form of output: " + expressions);
	}


	public Term diffInd(DiffSystem form, Term post, Services services)
			throws RemoteException, SolverException {
		return differentialCall(form, post, null, services, "IDiffInd");
	}

	public Term diffFin(DiffSystem form, Term post, Term ep, Services services)
			throws RemoteException, SolverException {
		Term invariant = form.getInvariant(services);
		return differentialCall(form, post, ep, services, "IDiffFin");
	}
	/**
	 * 
	 * @author s0805753@sms.ed.ac.uk
	 * @param form the Term which is to be converted into Parity normal form.
	 * 
	 * @throws RemoteException
	 * @throws SolverException
	 */
	
	public Term parityNF(Term form, NamespaceSet nss)
			throws RemoteException, SolverException {
		Expr query = Term2ExprConverter.convert2Expr(form);
		query = new Expr(new Expr(Expr.SYMBOL, "AMC`" + "ParityNF"),
				new Expr[] { query });
		Expr result = evaluate(query).expression;
		Term resultTerm = convert(result, nss);
		if (!resultTerm.equals(form)) {
			return resultTerm;
		}
		return form;
	}
	
	/**
	 * Method for computing the boundary of invariant candidates given by
	 * a square-free polynomial inequality.
	 * 
	 * @author s0805753@sms.ed.ac.uk
	 * @param form the quantifier-free formula given by a square-free polynomial.
	 * @throws RemoteException
	 * @throws SolverException
	 */
	
	public Term getBoundary(Term form, NamespaceSet nss)
			throws RemoteException, SolverException {
		Expr query = Term2ExprConverter.convert2Expr(form);
		query = new Expr(new Expr(Expr.SYMBOL, "AMC`" + "GetBoundary"),
				new Expr[] { query });
		Expr result = evaluate(query).expression;
		Term resultTerm = convert(result, nss);
		if (!resultTerm.equals(form)) {
			return resultTerm;
		}
		return form;
	}
	
	/**
	 * 
	 * Method for computing the condition which ensures that the gradient vector
	 * on the boundary of the invariant candidate is non-zero. The candidate 
	 * needs to be given by square-free polynomial (in fact the implementation
	 * finds this square-free description, if one exists).
	 * 
	 * @author s0805753@sms.ed.ac.uk
	 * @param form the quantifier-free formula given by a square-free polynomial.
	 * @throws RemoteException
	 * @throws SolverException
	 */
	
	public Term nonZeroGrad(Term form, ArrayList<String> vars, NamespaceSet nss)
			throws RemoteException, SolverException {
		Expr query = Term2ExprConverter.convert2Expr(form);
		
		ArrayList<Expr> varsExpr = new ArrayList<Expr>();
		for(String var: vars){
			varsExpr.add(new Expr(Expr.SYMBOL, NameMasker.mask(var)));
		}
		
		/* Create a Mathematica List of state variables */
		Expr stateVars = new Expr(new Expr(Expr.SYMBOL, "List"), 
			varsExpr.toArray(new Expr[varsExpr.size()])
			 );
		
		query = new Expr(new Expr(Expr.SYMBOL, "AMC`" + "NonZeroGrad"),
				new Expr[] { query, stateVars });
		
		Expr result = evaluate(query).expression;
		Term resultTerm = convert(result, nss);
		if (!resultTerm.equals(form)) {
			return resultTerm;
		}
		return form;
	}
	
    /**
     * @author s0805753@sms.ed.ac.uk
     * 
     * Computes a conjunctive description of a quantifier-free formula in which
     * all predicate symbols are '<=', if such a description is possible.
     * 
     * N.B. equations '==' are <b>not</b> converted to '<='.
     * @throws RemoteException
     * @throws SolverException
     */
    public Term toLessEqualConjunct(Term form, NamespaceSet nss)
            throws RemoteException, SolverException {
		Expr query = Term2ExprConverter.convert2Expr(form);
		query = new Expr(new Expr(Expr.SYMBOL, "AMC`" + "ToLessEqualConjunct"),
				new Expr[] { query });
		Expr result = evaluate(query).expression;
		Term resultTerm = convert(result, nss);
		if (!resultTerm.equals(form)) {
			return resultTerm;
		}
		return form;
    }
    
    /**
     * @author s0805753@sms.ed.ac.uk
     * 
     * Checks if the formula is a conjunction of atoms where 
     * all predicate symbols are '<='.
     * 
     * N.B. equations '==' are <b>not</b> converted to '<='.
     * 
     */
    public  boolean isLessEqualConjunct(Term form, NamespaceSet nss)
            throws RemoteException, SolverException {
		Expr query = Term2ExprConverter.convert2Expr(form);
		query = new Expr(new Expr(Expr.SYMBOL, "AMC`" + "IsLessEqualConjunct"),
				new Expr[] { query });
		Expr result = evaluate(query).expression;
		Term resultTerm = convert(result, nss);
		if (resultTerm.equals(TermBuilder.DF.tt())) {
			return true;
		}
		return false;
    }
    
    /**
     * @author s0805753@sms.ed.ac.uk
     * 
     * Checks if the formula is a conjunction of atoms where 
     * all predicate symbols are '<='.
     * 
     * N.B. equations '==' are <b>not</b> converted to '<='.
     * 
     */
    public Term getVCs(Term form, ArrayList<Term> vectorField,ArrayList<String> stateVars, NamespaceSet nss)
            throws RemoteException, SolverException {

    	/* Convert a list of KeYmaera terms to a list of Mathematica expressions */
    	ArrayList<Expr> vectorFieldMma = new ArrayList<Expr>();
    	for(Term xdot: vectorField){
    		vectorFieldMma.add(Term2ExprConverter.convert2Expr(xdot));
    	}
    	
		/* Create a Mathematica List for the vector field */
		Expr f = new Expr(new Expr(Expr.SYMBOL, "List"), 
			vectorFieldMma.toArray(new Expr[vectorFieldMma.size()])
			 );
		
		/* Create a Mathematica List of state variables */
		Expr vars = new Expr(new Expr(Expr.SYMBOL, "List"), 
			stateVars.toArray(new Expr[stateVars.size()])
			 );
		
		Expr query = Term2ExprConverter.convert2Expr(form);
		query = new Expr(new Expr(Expr.SYMBOL, "AMC`" + "VCGen"),
				new Expr[] { query, f, vars });
		Expr result = evaluate(query).expression;
		Term resultTerm = convert(result, nss);
		
		return resultTerm;
    }
	
	/**
	 * 
	 * @author ap
	 * @param diffOperator
	 *            the diff operator to apply in Mathematica package
	 * @throws UnsolveableException
	 * @throws ConnectionProblemException
	 * @throws ServerStatusProblemException
	 * @throws IncompleteEvaluationException
	 */
	private Term differentialCall(DiffSystem form, Term post, Term ep,
			Services services, String diffOperator) throws RemoteException,
			SolverException {
		List<Expr> args = new ArrayList<Expr>();

		// use implicit differential symbols
		final LogicVariable t = null;
		Term invariant = form.getInvariant(services);
		final Map<String, Expr> EMPTY = new LinkedHashMap<String, Expr>();
		for (ProgramElement el : form.getDifferentialEquations(services
				.getNamespaces())) {
			args.add(DL2Expr.apply(el, t, EMPTY, services));
		}
		if (Debug.ENABLE_DEBUG) {
			System.out.println(diffOperator
					+ ": "
					+ evaluate(new Expr(new Expr(Expr.SYMBOL, "AMC`"
							+ diffOperator), new Expr[] { Term2ExprConverter
							.convert2Expr(post)
					// new Expr(Expr.SYMBOL, t.name().toString())
							})).expression);
		}
		Expr diffCall;
		if (ep == null) {
			diffCall = new Expr(new Expr(Expr.SYMBOL, "AMC`" + diffOperator),
					new Expr[] {
							Term2ExprConverter.convert2Expr(post),
							// new Expr(Expr.SYMBOL, t.name().toString()),
							new Expr(new Expr(Expr.SYMBOL, "List"), args
									.toArray(new Expr[1])), });
		} else {
			diffCall = new Expr(new Expr(Expr.SYMBOL, "AMC`" + diffOperator),
					new Expr[] {
							Term2ExprConverter.convert2Expr(post),
							Term2ExprConverter.convert2Expr(ep),
							// new Expr(Expr.SYMBOL, t.name().toString()),
							new Expr(new Expr(Expr.SYMBOL, "List"), args
									.toArray(new Expr[1])), });
		}
		Expr diffIndExpression = evaluate(diffCall).expression;

		return TermBuilder.DF.imp(invariant, convert(diffIndExpression,
				services.getNamespaces()));
	}

	public List<Update> createUpdates(Expr expr, NamespaceSet nss)
			throws RemoteException, SolverException {
		List<Update> result = new ArrayList<Update>();
		if (expr.toString().equalsIgnoreCase("$Aborted")
				|| expr.toString().contains("Abort[]")) {
			throw new IncompleteEvaluationException("Calculation aborted!");
		}
		if (expr.head().equals(LIST)) {
			for (int i = 0; i < expr.args().length; i++) {
				result.addAll(createUpdates(expr.args()[i], nss));
			}
		} else if (expr.head().equals(RULE)) {
			Update u = new Update();
			try {
                String name = expr.args()[0].head().asString();
                if (NameMasker.isMasked(name)) {
                    name = NameMasker.unmask(name);
                    de.uka.ilkd.key.logic.op.ProgramVariable var = (de.uka.ilkd.key.logic.op.ProgramVariable) nss
                            .programVariables().lookup(new Name(name));
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
                }
			} catch (ExprFormatException e) {
				throw new RemoteException("Could not create Update for: "
						+ expr, e);
			}
			u.value = convert(expr.args()[1], nss);
			result.add(u);
		} else if (Expr2TermConverter.isBlacklisted(expr)) {
			throw new UnsolveableException("Blacklisted conversion from "
					+ expr.toString() + " to updates");
		} else {
			throw new IllegalStateException("unknown case " + expr.head()
					+ " in\n" + expr);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.IMathematicaDLBridge#convert(com.wolfram.jlink.Expr)
	 */
	public Term convert(Expr expr, NamespaceSet nss) throws RemoteException,
			SolverException {
		try {
			return Expr2TermConverter.convert(expr, nss,
				new LinkedHashMap<Name, LogicVariable>());
		}
		catch (UnknownMathFunctionException e) {
			throw new UnknownMathFunctionException(e + "\nin " + expr, e);
		}
	}

	/**
	 * Collect all program variables which are children of a Dot.
	 * 
	 * @param form
	 *            the current root element.
	 * 
	 * @param vars
	 *            the Map used for storing the result
	 * @param t
	 *            the variable used as time
	 */
	public static final void collectDottedProgramVariables(ProgramElement form,
			Map<String, Expr> vars, Named t) {
		collectDottedProgramVariables(form, vars, t.name().toString());
	}
	
	public static final void collectDottedProgramVariables(ProgramElement form,
			Map<String, Expr> vars, String name) {
		if (form instanceof Dot) {
			ProgramVariable pv = (ProgramVariable) ((Dot) form).getChildAt(0);
			String pvName = pv.getElementName().toString();
			pvName = NameMasker.mask(pvName);
			vars.put(pvName, new Expr(new Expr(Expr.SYMBOL, pvName),
					new Expr[] { new Expr(Expr.SYMBOL, NameMasker.mask(name)) }));
		}
		if (form instanceof DLNonTerminalProgramElement) {
			DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) form;
			for (ProgramElement p : dlnpe) {
				collectDottedProgramVariables(p, vars, name);
			}
		}

	}

	/**
	 * Collect all program variables which are children of a Dot (without adding time).
	 * 
	 * @param form
	 *            the current root element.
	 * 
	 * @param vars
	 *            the Map used for storing the result
	 */
	public static final void collectDottedProgramVariables(ProgramElement form,
			Map<String, Expr> vars) {
		if (form instanceof Dot) {
			ProgramVariable pv = (ProgramVariable) ((Dot) form).getChildAt(0);
			String pvName = pv.getElementName().toString();
			pvName = NameMasker.mask(pvName);
			vars.put(pvName, new Expr(Expr.SYMBOL, pvName));
		}
		if (form instanceof DLNonTerminalProgramElement) {
			DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) form;
			for (ProgramElement p : dlnpe) {
				collectDottedProgramVariables(p, vars);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.IMathematicaDLBridge#simplify(de.uka.ilkd.key.logic
	 * .Term, java.util.Set)
	 */
	public Term simplify(Term form, Set<Term> assumptions, NamespaceSet nss)
			throws RemoteException, SolverException {
		Expr query = Term2ExprConverter.convert2Expr(form);
		Set<Expr> ass = new LinkedHashSet<Expr>();
		for (Term t : assumptions) {
			ass.add(Term2ExprConverter.convert2Expr(t));
		}
		if (DLOptionBean.INSTANCE.getSimplifyTimeout() > 0) {
			ass.add(new Expr(RULE, new Expr[] {
					new Expr(Expr.SYMBOL, "TimeConstraint"),
					new Expr(BigInteger.valueOf((DLOptionBean.INSTANCE
							.getSimplifyTimeout()))) }));
		}
		query = new Expr(new Expr(Expr.SYMBOL, "Simplify"), new Expr[] { query,
				new Expr(LIST, ass.toArray(new Expr[0])) });
		Expr result = evaluate(query).expression;
		Term resultTerm = convert(result, nss);
		if (!resultTerm.equals(form)) {
			return resultTerm;
		}
		return form;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.IMathematicaDLBridge#fullSimplify(de.uka.ilkd.key.
	 * logic.Term)
	 */
	public Term fullSimplify(Term form, NamespaceSet nss)
			throws RemoteException, SolverException {
		Expr query = Term2ExprConverter.convert2Expr(form);
		query = new Expr(new Expr(Expr.SYMBOL, "FullSimplify"),
				new Expr[] { query });
		Expr result = evaluate(query).expression;
		Term resultTerm = convert(result, nss);
		if (!resultTerm.equals(form)) {
			return resultTerm;
		}
		return form;
	}

	private ExprAndMessages evaluate(final Expr expr, long timeout)
			throws RemoteException, SolverException {
		ExprAndMessages evaluate;
		IKernelLinkWrapper wrapper = getKernelWrapper();
		try {
			evaluate = wrapper.evaluate(expr, timeout, Options.INSTANCE
					.getMemoryConstraint());
		} catch (RemoteException e) {
			Registry reg = LocateRegistry.getRegistry(serverIP, port);
			try {
				wrapper = (IKernelLinkWrapper) reg
						.lookup(KernelLinkWrapper.IDENTITY);
			} catch (NotBoundException f) {
				throw new ConnectionProblemException("Problem with KernelLink",
						f);
			}
			evaluate = wrapper.evaluate(expr, timeout, Options.INSTANCE
					.getMemoryConstraint());
		}
		if (evaluate.messages != null) {
			if (!evaluate.messages.toString().equals("{}")) {
				System.err.println("Message while evaluating: " + expr
						+ "\n Message was: " + evaluate.messages); // XXX
			}
			if (evaluate.messages.toString()
					.matches(".*" + mBlistString + ".*")) {
				throw new UnsolveableException(
						"Mathematica could not solve the given expression: "
								+ expr + ". Reason: "
								+ evaluate.messages.toString());
			}
		}
		return evaluate;
	}

	private ExprAndMessages evaluate(final Expr expr) throws RemoteException,
			SolverException {
		return evaluate(expr, -1);
	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see
//	 * de.uka.ilkd.key.dl.IMathematicaDLBridge#findInstance(de.uka.ilkd.key.
//	 * logic.Term)
//	 */
//	public String findInstance(Term form, long timeout) throws RemoteException,
//			SolverException {
//		Expr query = Term2ExprConverter.convert2Expr(form);
//		List<Expr> vars = new ArrayList<Expr>();
//		Set<String> variables = AllCollector.getItemSet(form).filter(
//				new FilterVariableCollector(null)).getVariables();
//		for (String var : variables) {
//			vars.add(new Expr(Expr.SYMBOL, var.replaceAll("_", USCORE_ESCAPE)));
//		}
//		if (vars.size() > 0) {
//			query = new Expr(new Expr(Expr.SYMBOL, "FindInstance"), new Expr[] {
//					query, new Expr(LIST, vars.toArray(new Expr[0])),
//					new Expr(Expr.SYMBOL, "Reals") });
//			Expr result = evaluate(query, timeout).expression;
//
//			List<String> createFindInstanceString = createFindInstanceString(result);
//			Collections.sort(createFindInstanceString);
//			StringBuilder res = new StringBuilder();
//			for (String s : createFindInstanceString) {
//				res.append(s + "\n");
//			}
//			if (res.toString().contains("FindInstance")) {
//				throw new UnsolveableException("Recursive counterexample "
//						+ res);
//			}
//			return res.toString();
//		}
//		return "";
//	}
	
    /*
     * (non-Javadoc)
     * 
     * @see
     * de.uka.ilkd.key.dl.IMathematicaDLBridge#findInstance(de.uka.ilkd.key.
     * logic.Term)
     */
    public String findInstance(Term form, long timeout, Services services)
        throws RemoteException, SolverException {
        List<String> result = findMultiInstance(form, 1, timeout);
        if (!result.isEmpty())
            return result.get(0);
        return "";
    }

	
    /*   
     * (non-Javadoc)
     * 
     * @see
     * de.uka.ilkd.key.dl.IMathematicaDLBridge#findMultiInstance(de.uka.ilkd.key.
     * logic.Term, int, long)
     */
    public List<String> findMultiInstance(Term form, int ninst, long timeout) throws RemoteException,
            SolverException {
        Expr query = Term2ExprConverter.convert2Expr(form);
        List<Expr> vars = new ArrayList<Expr>();
        Set<String> variables = AllCollector.getItemSet(form).filter(
                new FilterVariableCollector(null)).getVariables();
        for (String var : variables)
            vars.add(new Expr(Expr.SYMBOL, NameMasker.mask(var)));
        List<String> ret = new ArrayList<String>();
        if (vars.size() > 0) { 
            query = new Expr(new Expr(Expr.SYMBOL, "FindInstance"), new Expr[] {
                    query, new Expr(LIST, vars.toArray(new Expr[0])),
                    new Expr(Expr.SYMBOL, "Reals"),
                    new Expr(ninst),
                    });  
            Expr results[] = evaluate(query, timeout).expression.args();

            for (Expr result : results) {
                List<String> createFindInstanceString = createFindInstanceString(result);
                Collections.sort(createFindInstanceString);
                StringBuilder res = new StringBuilder();
                for (String s : createFindInstanceString)
                    res.append(s + "\n");
                if (res.toString().contains("FindInstance")) {
                    throw new UnsolveableException("Recursive counterexample "
                            + res);
                }    
                ret.add(res.toString());
            }    
        }    
        return ret; 
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
			str.append(NameMasker.unmask(result.args()[0].toString()));
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
		for(CounterExampleFinder cex: cexFinders) {
			cex.abortCalculation();
		}
	}

	public String getTimeStatistics() throws RemoteException {
		return getKernelWrapper().getTimeStatistics();
	}

	public long getTotalCalculationTime() throws RemoteException {
		return getKernelWrapper().getTotalCalculationTime();
	}

	public long getTotalMemory() throws RemoteException,
			ServerStatusProblemException, ConnectionProblemException {
		return getKernelWrapper().getTotalMemory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.IMathematicaDLBridge#getCachedAnwserCount()
	 */
	public long getCachedAnwserCount() throws RemoteException {
		return getKernelWrapper().getCachedAnswers();
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
			System.err.println("Exception occurred in resetAbortState: " + e);// XXX
			// e.printStackTrace();
			System.err.println("In most cases this can safely be ignored"); // XXX
		}
	}

	public Term reduce(Term form, List<String> additionalReduce,
			List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss,
			long timeout) throws RemoteException, SolverException {
		Expr query = Term2ExprConverter.convert2Expr(form);
		List<Expr> vars = new ArrayList<Expr>();
		for (String name : additionalReduce) {
			vars.add(new Expr(Expr.SYMBOL, NameMasker.mask(name)));
		}
		for (PairOfTermAndQuantifierType pair : quantifiers) {
			Expr convert2Expr = Term2ExprConverter.convert2Expr(pair.term);
			vars.add(convert2Expr);
			Expr quant = (pair.type == QuantifierType.FORALL) ? FORALL : EXISTS;
			if (quant == query.head()) {
				Expr kernel = query.args()[1];
				assert query.args()[0].head() == LIST : "Term2ExprConverter always builds list quantifiers";
				Expr[] innerVariables = query.args()[0].args();
				Expr[] allVariables = new Expr[innerVariables.length + 1];
				allVariables[0] = convert2Expr;
				System.arraycopy(innerVariables, 0, allVariables, 1,
						innerVariables.length);
				Expr[] mergedQuant = new Expr[2];
				mergedQuant[0] = new Expr(LIST, allVariables);
				mergedQuant[1] = kernel;
				query = new Expr(quant, mergedQuant);
			} else {
				query = new Expr(quant, new Expr[] {
						new Expr(LIST, new Expr[] { convert2Expr }), query });
			}

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
		Expr result = evaluate(query, timeout).expression;
		Term resultTerm = convert(result, nss);
		if (!resultTerm.equals(form)) {
			return resultTerm;
		}
		return form;
	}

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.uka.ilkd.key.dl.IMathematicaDLBridge#findMultiNumInstance(de.uka.ilkd.key.
     * logic.Term, int, long)
     */
    public List<String> findMultiNumInstance(Term form, int ninst, long timeout)
        throws RemoteException, SolverException {
        Expr query = Term2ExprConverter.convert2Expr(form);
        List<Expr> vars = new ArrayList<Expr>();
        Set<String> variables = AllCollector.getItemSet(form).filter(
                new FilterVariableCollector(null)).getVariables();
        for (String var : variables)
            vars.add(new Expr(Expr.SYMBOL, NameMasker.mask(var)));
        List<String> ret = new ArrayList<String>();
        if (vars.size() > 0) {
            query = new Expr(new Expr(Expr.SYMBOL, "FindInstance"), new Expr[] {
                    query, new Expr(LIST, vars.toArray(new Expr[0])),
                    new Expr(Expr.SYMBOL, "Reals"),
                    new Expr(ninst),
                    });
            query = new Expr(new Expr(Expr.SYMBOL, "N"), new Expr[] {query});
            Expr results[] = evaluate(query, timeout).expression.args();

            for (Expr result : results) {
                List<String> createFindInstanceString = createFindInstanceString(result);
                Collections.sort(createFindInstanceString);
                StringBuilder res = new StringBuilder();
                for (String s : createFindInstanceString)
                    res.append(s + "\n");
                if (res.toString().contains("FindInstance")) {
                    throw new UnsolveableException("Recursive counterexample "
                            + res);
                }
                ret.add(res.toString());
            }
        }
        return ret;
    }
	
	/* @Override */
	public String findTransition(Term initial, Term modalForm, long timeout,
			Services services) throws RemoteException, SolverException {
        CounterExampleFinder cef = new CounterExampleFinder(initial, modalForm, services);
		cexFinders.add(cef);
        String s = cef.findSolution();
		cexFinders.remove(cef);
		return s;
        //@note Old partial implementation using Mathematica
//		Term term = modalForm;
//		final de.uka.ilkd.key.rule.updatesimplifier.Update update = de.uka.ilkd.key.rule.updatesimplifier.Update
//				.createUpdate(term);
//		// unbox from update prefix
//		if (term.op() instanceof QuanUpdateOperator) {
//			term = ((QuanUpdateOperator) term.op()).target(term);
//			if (term.op() instanceof QuanUpdateOperator)
//				throw new AssertionError(
//						"assume nested updates have been merged");
//		}
//		if (!(term.op() instanceof Modality && term.javaBlock() != null
//				&& term.javaBlock() != JavaBlock.EMPTY_JAVABLOCK && term
//				.javaBlock().program() instanceof StatementBlock)) {
//			throw new IllegalArgumentException("inapplicable to " + modalForm);
//		}
//		Term post = term.sub(0);
//		final DiffSystem system = (DiffSystem) ((StatementBlock) term
//				.javaBlock().program()).getChildAt(0);
//
//		// @todo fixme change this thingx
//		Named t = new LogicVariable(new Name("tmpts"), RealLDT.getRealSort());
//
//		List<Expr> args = new ArrayList<Expr>();
//
//		Map<String, Expr> vars = new HashMap<String, Expr>();
//
//		collectDottedProgramVariables(system, vars, t);
//		Term invariant = system.getInvariant(services);
//		final Map<String, Expr> EMPTY = new HashMap<String, Expr>();
//		for (ProgramElement el : system.getDifferentialEquations(services
//				.getNamespaces())) {
//			args.add(DL2ExprConverter.convertDiffEquation(el, t, vars));
//		}
//		Expr call = new Expr(new Expr(Expr.SYMBOL, "AMC`" + "IFindTransition"),
//				new Expr[] {
//						Term2ExprConverter.convert2Expr(initial),
//						Term2ExprConverter.update2Expr(update),
//						new Expr(new Expr(Expr.SYMBOL, "List"), args
//								.toArray(new Expr[1])),
//						new Expr(Expr.SYMBOL, t.name().toString()),
//						Term2ExprConverter.convert2Expr(invariant),
//						Term2ExprConverter.convert2Expr(TermBuilder.DF
//								.not(post)),
//						new Expr(new Expr(Expr.SYMBOL, "Rule"), new Expr[] {
//								new Expr(Expr.SYMBOL, "TimeHorizon"),
//								new Expr(TIME_HORIZON) }),
//						new Expr(new Expr(Expr.SYMBOL, "Rule"), new Expr[] {
//								new Expr(Expr.SYMBOL, "Instances"),
//								new Expr(CEX_TRANSITION_INSTANCES) }) });
//		Expr result = evaluate(call, timeout).expression;
//
//		List<String> createFindInstanceString = createFindInstanceString(result);
//		Collections.sort(createFindInstanceString);
//		StringBuilder res = new StringBuilder();
//		for (String s : createFindInstanceString) {
//			res.append(s + "\n");
//		}
//		if (res.toString().contains("IFindTransition")) {
//			throw new UnsolveableException("Recursive counterexample " + res);
//		}
//		return res.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uka.ilkd.key.dl.arithmetics.IGroebnerBasisCalculator#
	 * checkForConstantGroebnerBasis
	 * (de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker
	 * .PolynomialClassification)
	 */
	public boolean checkForConstantGroebnerBasis(
			PolynomialClassification<Term> terms, Services services) {

		Set<Expr> f = new LinkedHashSet<Expr>();
		Set<Expr> g = new LinkedHashSet<Expr>();
		Set<Expr> h = new LinkedHashSet<Expr>();
		Set<Expr> vars = new LinkedHashSet<Expr>();
		Set<String> varNames = new LinkedHashSet<String>();
		for (Term t : terms.f) {
			f.add(Term2ExprConverter.convert2Expr(t.sub(0)));
			Set<String> variables = AllCollector.getItemSet(t).filter(
					new FilterVariableCollector(null)).getVariables();
			varNames.addAll(variables);
			for (String var : variables) {
				vars.add(new Expr(Expr.SYMBOL, NameMasker.mask(var)));
			}
		}
		for (Term t : terms.g) {
			Expr left = Term2ExprConverter.convert2Expr(t.sub(0));
			try {
				if (simplify(t, new LinkedHashSet<Term>(), services.getNamespaces())
						.equals(TermBuilder.DF.ff())) {
					// found a contradiction of the form 0 != 0
					return true;
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SolverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// as != is symetric we might need to add the right side of the
			// inequality
			if (left.toString().equals("0")) {
				g.add(Term2ExprConverter.convert2Expr(t.sub(1)));
			} else {
				g.add(left);
			}
			Set<String> variables = AllCollector.getItemSet(t).filter(
					new FilterVariableCollector(null)).getVariables();
			varNames.addAll(variables);
			for (String var : variables) {
				vars.add(new Expr(Expr.SYMBOL, NameMasker.mask(var)));
			}
		}
		// if h is empty the following proceedure might be unsound, so we just
		// return false
		if (terms.h.isEmpty()) {
			return false;
		}
		for (Term t : terms.h) {
			Expr left = Term2ExprConverter.convert2Expr(t.sub(0));
			// as = is symetric we might need to add the right side of the
			// inequality
			if (left.toString().equals("0")) {
				h.add(Term2ExprConverter.convert2Expr(t.sub(1)));
			} else {
				h.add(left);
			}
			Set<String> variables = AllCollector.getItemSet(t).filter(
					new FilterVariableCollector(null)).getVariables();
			varNames.addAll(variables);
			for (String var : variables) {
				vars.add(new Expr(Expr.SYMBOL, NameMasker.mask(var)));
			}
		}
		PolynomialClassification<Expr> classify2 = new PolynomialClassification<Expr>(
				f, g, h);

		// we try to get a contradiction by computing the groebner basis of all
		// the equalities. if the common basis contains a constant part, the
		// equality system is unsatisfiable, thus we can close this goal

		Expr groebnerBasis;
		try {
			Expr order = new Expr(RULE, new Expr[] {
					new Expr(Expr.SYMBOL, "MonomialOrder"),
					new Expr(Expr.SYMBOL, "DegreeReverseLexicographic") });
			groebnerBasis = evaluate(new Expr(
					new Expr(Expr.SYMBOL, "GroebnerBasis"),
					new Expr[] {
							new Expr(LIST, h.toArray(new Expr[h.size()])),
							new Expr(LIST, vars.toArray(new Expr[vars.size()])),
							order })).expression;

			assert groebnerBasis.head().equals(LIST) : "The head of the returned groebner basis has to be a list";
			System.out.println(groebnerBasis);
			Expr one = new Expr(1);
			Expr expression = evaluate(new Expr(
					new Expr(Expr.SYMBOL, "PolynomialReduce"),
					new Expr[] {
							one,
							groebnerBasis,
							new Expr(LIST, vars.toArray(new Expr[vars.size()])),
							order })).expression;
			System.out.println("Result for reduce 1 is: " + expression);
			if (expression.head().equals(LIST)) {
				if (expression.args().length == 2) {
					// check if the remainder is 0.
					if (expression.args()[1].toString().equals("0")) {
						return true;
					}
				}
			}
			if (!classify2.g.isEmpty()) {
				// we test if one of the inequalities g is unsatisfiable under
				// the
				// variety \forall f \in h: f = 0. if it is, we get false on the
				// left side of the sequent and can close this goal

				System.out.println("Checking g = " + classify2.g);
				for (Expr curG : classify2.g) {
					Expr reduce = evaluate(new Expr(new Expr(Expr.SYMBOL,
							"PolynomialReduce"),
							new Expr[] {
									curG,
									groebnerBasis,
									new Expr(LIST, vars.toArray(new Expr[vars
											.size()])), order })).expression;
					System.out.println("Result for reduce " + curG + " is: "
							+ reduce);
					if (reduce.head().equals(LIST)) {
						if (reduce.args().length == 2) {
							// check if the remainder is 0.
							if (reduce.args()[1].toString().equals("0")) {
								return true;
							}
						}
					}
				}
				// now we check if we can get a constant in a groebner base of
				// the
				// form {h1,...,hn,(g*t)-1}

				// find a fresh variable name
				String baseName = "t";
				String name = "t";
				int i = 0;
				while (varNames.contains(name)) {
					name = baseName + i++;
				}
				Expr freeVar = new Expr(Expr.SYMBOL, NameMasker.mask(name));
				vars.add(freeVar);
				for (Expr curG : classify2.g) {
					Expr curBase = evaluate(new Expr(new Expr(Expr.SYMBOL,
							"GroebnerBasis"),
							new Expr[] {
									new Expr(LIST, appendToArray(groebnerBasis
											.args(), new Expr(MINUS,
											new Expr[] {
													new Expr(MULT, new Expr[] {
															curG, freeVar }),
													one }))),
									new Expr(LIST, vars.toArray(new Expr[vars
											.size()])), order })).expression;
					// if (curBase.args().length == 1 &&
					// curBase.args()[0].toString().equals("1")) {
					// return true;
					// }
					Expr reduce = evaluate(new Expr(new Expr(Expr.SYMBOL,
							"PolynomialReduce"),
							new Expr[] {
									one,
									curBase,
									new Expr(LIST, vars.toArray(new Expr[vars
											.size()])), order })).expression;
					if (reduce.head().equals(LIST)) {
						if (reduce.args().length == 2) {
							// check if the remainder is 0.
							if (reduce.args()[1].toString().equals("0")) {
								return true;
							}
						}
					}
				}
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SolverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public Term[] computeGroebnerBasis(Term[] polynomials, Services services)
			throws RemoteException, SolverException {
		Set<String> varNames = new LinkedHashSet<String>();
		for (Term t : polynomials) {
			Set<String> variables = AllCollector.getItemSet(t).filter(
					new FilterVariableCollector(null)).getVariables();
			varNames.addAll(variables);
		}
		Set<Expr> vars = new LinkedHashSet<Expr>();
		for (String var : new TreeSet<String>(varNames)) {
			vars.add(new Expr(Expr.SYMBOL, NameMasker.mask(var)));
		}
		
		Expr order = new Expr(RULE, new Expr[] {
				new Expr(Expr.SYMBOL, "MonomialOrder"),
				new Expr(Expr.SYMBOL, "DegreeReverseLexicographic") });

		Expr[] pols = new Expr[polynomials.length];
		for (int i = 0; i < polynomials.length; i++) {
			pols[i] = Term2Expr.apply(polynomials[i], false);
		}
		Expr query = new Expr(
				new Expr(Expr.SYMBOL, "GroebnerBasis"),
				new Expr[] {
				    new Expr(LIST, pols),
						new Expr(LIST, vars.toArray(new Expr[vars.size()])),
						order });
		System.out.println(query);
		Expr expressions = evaluate(query).expression;
		if (expressions.head().equals(LIST)) {
			List<Term> result = new LinkedList<Term>();
			for (int i = 0; i < expressions.args().length; i++) {
				result.add(convert(expressions.args()[i], services.getNamespaces()));
			}
			return result.toArray(new Term[0]);
		} else
			throw new FailedComputationException("Unexpected form of output: " + expressions);	
	}

	@Override
	public Term polynomialReduce(Term poly, Term[] reductions, Services services)
			throws RemoteException, SolverException {
		Set<String> varNames = new LinkedHashSet<String>();
		for (Term t : reductions) {
			Set<String> variables = AllCollector.getItemSet(t).filter(
					new FilterVariableCollector(null)).getVariables();
			varNames.addAll(variables);
		}
		{
			Set<String> variables = AllCollector.getItemSet(poly).filter(
					new FilterVariableCollector(null)).getVariables();
			varNames.addAll(variables);
		}
		Set<Expr> vars = new LinkedHashSet<Expr>();
		for (String var : new TreeSet<String>(varNames)) {
			vars.add(new Expr(Expr.SYMBOL, NameMasker.mask(var)));
		}
		
		Expr order = new Expr(RULE, new Expr[] {
				new Expr(Expr.SYMBOL, "MonomialOrder"),
				new Expr(Expr.SYMBOL, "DegreeReverseLexicographic") });

		Expr[] reds = new Expr[reductions.length];
		for (int i = 0; i < reductions.length; i++) {
			reds[i] = Term2Expr.apply(reductions[i], false);
		}
		Expr query = new Expr(
				new Expr(Expr.SYMBOL, "PolynomialReduce"),
				new Expr[] {
					Term2Expr.apply(poly, false),
				    new Expr(LIST, reds),
						new Expr(LIST, vars.toArray(new Expr[vars.size()])),
						order });
		System.out.println(query);
		Expr expression = evaluate(query).expression;
		System.out.println("Result for reduce 1 is: " + expression);
		if (expression.head().equals(LIST)) {
			if (expression.args().length == 2) {
				// check if the remainder is 0.
				return convert(expression.args()[1], services.getNamespaces());
			}
		}
		throw new FailedComputationException("Unexpected form of output: " + expression);
	}

	/**
	 * @param args
	 * @param expr
	 * @return TODO documentation since Jan 27, 2009
	 */
	private Expr[] appendToArray(Expr[] args, Expr expr) {
		Expr[] result = new Expr[args.length + 1];
		System.arraycopy(args, 0, result, 0, args.length);
		result[args.length] = expr;
		return result;
	}

	public Map<String, Double[][]> getPlotData(DiffSystem sys, String t, double minT, double maxT, double sampling, Map<String, Double> initialValues, Services services) throws SolverException, RemoteException{
	           List<Expr> args = new ArrayList<Expr>();
        Map<String, Expr> vars = new LinkedHashMap<String, Expr>();

        collectDottedProgramVariables(sys, vars, t);
        Map<String, Expr> init = new HashMap<String, Expr>();
        for(String s: initialValues.keySet()) {
            if(!vars.containsKey(s)) {
                init.put(s, new Expr(initialValues.get(s)));
            }
        }
        
        for (ProgramElement el : sys.getDifferentialEquations(services.getNamespaces()))
            args.add(DL2Expr.apply(el, t, vars, init, services));
        for (String name : vars.keySet()) {
            final Double initial = initialValues.get(NameMasker.unmask(name));
            final Expr equals = new Expr(EQUALS, new Expr[] {
                        new Expr(new Expr(Expr.SYMBOL, name), new Expr[] { new Expr(0) }),
                        new Expr(initial) });
            args.add(equals);
        }
        final Expr list = new Expr(Expr.SYMBOL, "List");
//        Expr dsolve = new Expr(new Expr(Expr.SYMBOL, "DSolve"), new Expr[] {
//                        new Expr(list, args.toArray(new Expr[args.size()])),
//                        new Expr(list, vars.values().toArray(new Expr[0])),
//                        new Expr(Expr.SYMBOL, NameMasker.mask(t)) });
        Expr[] varsAsExpr = new Expr[vars.keySet().size()];
        int idx = 0;
        for(String s: vars.keySet()) {
            varsAsExpr[idx++] = new Expr(Expr.SYMBOL, s);
        }
        Expr dsolve = new Expr(new Expr(Expr.SYMBOL, "NDSolve"), new Expr[] {
                new Expr(list, args.toArray(new Expr[args.size()])),
                new Expr(list, varsAsExpr),
                new Expr(list, new Expr[] {
                        new Expr(Expr.SYMBOL, NameMasker.mask(t)),
                        new Expr(minT), new Expr(maxT), }) });
        Expr rule = evaluate(dsolve, 1000).expression;
        if(!rule.listQ()) {
            throw new FailedComputationException(
                    "Expecting a list of rewrites but got: "
                            + rule
                            + " when trying to solve a differential equation system.");
        }
        Map<String, Integer> positionMap = new HashMap<String, Integer>();
        args = new ArrayList<Expr>();
        final Expr exprT = new Expr(Expr.SYMBOL, NameMasker.mask(t));
        args.add(exprT);
        for(String var: vars.keySet()) {
            positionMap.put(var, args.size());
            // N[var[t] /. rule] 
            Expr vOfT = new Expr(new Expr(Expr.SYMBOL, var), new Expr[] { exprT });
            Expr replace = new Expr(new Expr(Expr.SYMBOL, "ReplaceAll"), new Expr[] { vOfT, rule });
            args.add(new Expr(new Expr(Expr.SYMBOL, "N"), new Expr[] { replace }));
        }
        
        Expr query = new Expr(new Expr(Expr.SYMBOL, "Table"), new Expr[] {
           new Expr(list, args.toArray(new Expr[args.size()])), new Expr(list, new Expr[] { exprT, new Expr(minT), new Expr(maxT), new Expr(sampling) }) }); 
        Expr updateExpressions = evaluate(query).expression;
        // the result is a list of list that contain n elements, first one is
        // the value of t, the subsequent ones are singleton lists containing
        // the value of vars at that point
        Map<String, Double[][]> result = new LinkedHashMap<String, Double[][]>();
        for(String s: vars.keySet()) {
            final Double[][] doubles = new Double[2][];
            doubles[0] = new Double[updateExpressions.args().length];
            doubles[1] = new Double[updateExpressions.args().length];
            result.put(NameMasker.unmask(s), doubles);
        }
        assert updateExpressions.listQ() : "The head has to be a list";
        for(int i = 0; i < updateExpressions.args().length; i++) {
            for(String s: positionMap.keySet()) {
                final Expr expr = updateExpressions.args()[i];
                try {
                    // at position 0 there is the value of t
                    final double tAsDouble = expr.args()[0].asDouble();
                    result.get(NameMasker.unmask(s))[0][i] = tAsDouble;
                    // at position positionMap.get(s) there is a singleton list with the value of s
                    final double sAsDouble = expr.args()[positionMap.get(s)].args()[0].asDouble();
                    result.get(NameMasker.unmask(s))[1][i] = sAsDouble;
                } catch (ExprFormatException e) {
                    throw new FailedComputationException("Cannot convert "
                            + expr.args()[positionMap.get(s)].args()[0]
                            + " in solution " + expr, e);
                }
            }
        }
        return result;
	}
}
