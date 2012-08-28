/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel                                *
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
package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.awt.Frame;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.dl.arithmetics.IODESolver.ODESolverUpdate;
import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.IODESolver.ODESolverResult;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.FunctionTerm;
import de.uka.ilkd.key.dl.model.LessEquals;
import de.uka.ilkd.key.dl.model.NonRigidFunction;
import de.uka.ilkd.key.dl.model.Quantified;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.Variable;
import de.uka.ilkd.key.dl.model.VariableDeclaration;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.parser.NumberCache;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Namespace;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.ProgramElementName;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.UpdateFactory;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.LocationVariable;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.logic.op.QuanUpdateOperator;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.TermSymbol;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;
import de.uka.ilkd.key.rule.updatesimplifier.AssignmentPair;
import de.uka.ilkd.key.rule.updatesimplifier.UpdateSimplifierTermFactory;

import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;
import de.uka.ilkd.key.dl.formulatools.ReplaceVisitor;

import de.uka.ilkd.key.gui.Main;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * @author jdq
 * 
 */
public class QODESolve extends AbstractDLMetaOperator {

    public static final Name NAME = new Name("#QODESolve");

    public QODESolve() {
        super(NAME, 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.uka.ilkd.key.logic.op.AbstractMetaOperator#sort(de.uka.ilkd.key.logic
     * .Term[])
     */
    /* @Override */
    public Sort sort(Term[] term) {
        return Sort.FORMULA;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.uka.ilkd.key.logic.op.AbstractMetaOperator#calculate(de.uka.ilkd.key
     * .logic.Term, de.uka.ilkd.key.rule.inst.SVInstantiations,
     * de.uka.ilkd.key.java.Services)
     */
    public Term calculate(Term term, SVInstantiations svInst, Services services) {
        try {
            return odeSolve(term.sub(0), services);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace(); // XXX
            return term;
        }
    }

    public Term odeSolve(Term term, Services services) throws RemoteException,
            SolverException {
        return odeSolve(term, false, services);
    }

    public Term odeSolve(Term term, boolean rejectmultiple, Services services)
            throws RemoteException, SolverException {
        Quantified quantified = (Quantified) ((StatementBlock) term.javaBlock()
                .program()).getChildAt(0);
        VariableDeclaration decl = (VariableDeclaration) quantified
                .getChildAt(0);
        QuantifiableVariable var = new LogicVariable(
                ((Variable) decl.getChildAt(1)).getElementName(),
                (Sort) services.getNamespaces().sorts()
                        .lookup(decl.getType().getElementName()));
        // FIXME: react on name clashes here
        services.getNamespaces().variables().add(var);
        DiffSystem system = (DiffSystem) quantified.getChildAt(1);
        DiffSystem orgSystem = system;
        TermFactory tf;
        try {
            final NamespaceSet nss = services.getNamespaces();
            Name tName = new Name(nss.getUniqueName("t"));
            Name tsName = new Name(nss.getUniqueName("ts"));
            LogicVariable t = new LogicVariable(tName, RealLDT.getRealSort());
            LogicVariable ts = new LogicVariable(tsName, RealLDT.getRealSort());
            nss.variables().add(t);
            nss.variables().add(ts);
            Term post = term.sub(0);
            Term odeSolve;
            if (system.getDifferentialEquations(services.getNamespaces())
                    .isEmpty()) {
                // optimize no differential equations
                Term invariant = system.getInvariant(services);
                if (term.op() == Modality.BOX || term.op() == Modality.TOUT) {
                    return TermBuilder.DF.imp(invariant, post);
                } else if (term.op() == Modality.DIA) {
                    return TermBuilder.DF.and(invariant, post);
                } else {
                    throw new IllegalStateException("Unknown modality "
                            + term.op());
                }
            } else {
                Set<ProgramElement> collectNonRigidFunctionTerms = collectNonRigidFunctionTerms(system);
                Namespace pvNs = services.getNamespaces().programVariables().copy();
                Map<ProgramVariable, ProgramElement> tmpVars = createTmpVariables(
                        collectNonRigidFunctionTerms, services);
                Map<FunctionTerm, de.uka.ilkd.key.dl.model.ProgramVariable> inverse = new LinkedHashMap<FunctionTerm, de.uka.ilkd.key.dl.model.ProgramVariable>();
                for (ProgramVariable p : tmpVars.keySet()) {
                    inverse.put((FunctionTerm) tmpVars.get(p), p);
                }
                tf = TermFactory.getTermFactory(
                        DLOptionBean.INSTANCE.getTermFactoryClass(),
                        services.getNamespaces());
                system = (DiffSystem) ReplaceVisitor.replaceFunctionTerm(
                        system, inverse, tf);
                if (MathSolverManager.isODESolverSet()) {
                    final List<ODESolverUpdate> updates = MathSolverManager
                            .getCurrentODESolver().odeUpdate(system, t,
                                    services, -1);
                    Term[] locations = new Term[updates.size()];
                    Term[] values = new Term[updates.size()];
                    Term[] guards = new Term[updates.size()];
                    ImmutableArray<QuantifiableVariable>[] boundVars = new ImmutableArray[updates
                            .size()];
                    int idx = 0;
                    for (ODESolverUpdate u : updates) {
                        locations[idx] = replaceAll(services, tmpVars,
                                u.location);
                        values[idx] = replaceAll(services, tmpVars, u.expr);
                        guards[idx] = TermBuilder.DF.tt();
                        boundVars[idx] = new ImmutableArray<QuantifiableVariable>(
                                var);
                        idx++;
                    }
                    // remove temporary variables from the namespaces again
                    services.getNamespaces().setProgramVariables(pvNs);

                    Term zero = TermBuilder.DF.func(getNull(services));
                    Term updatedPost = QuanUpdateOperator.normalize(boundVars,
                            guards, locations, values, post);
                    // range: 0 <= ts & ts <= t
                    Term range = TermBuilder.DF.and(TermBuilder.DF.func(
                            RealLDT.getFunctionFor(LessEquals.class), zero,
                            TermBuilder.DF.var(ts)), TermBuilder.DF.func(
                            RealLDT.getFunctionFor(LessEquals.class),
                            TermBuilder.DF.var(ts), TermBuilder.DF.var(t)));
                    Term invariant = TermBuilder.DF
                            .all(var, TermBuilder.DF.all(ts, TermBuilder.DF
                                    .imp(range, QuanUpdateOperator.normalize(
                                            boundVars, guards, locations,
                                            values,
                                            orgSystem.getInvariant(services)))));

                    Term tGeqZero = TermBuilder.DF.func((Function) nss
                            .functions().lookup(new Name("geq")), new Term[] {
                            TermBuilder.DF.var(t), zero });
                    if (term.op() == Modality.BOX || term.op() == Modality.TOUT) {
                        if (system.getInvariant(services).equals(
                                TermBuilder.DF.tt())) {
                            odeSolve = updatedPost;
                        } else {
                            odeSolve = TermBuilder.DF.imp(invariant,
                                    updatedPost);
                        }
                        odeSolve = TermBuilder.DF.imp(tGeqZero, odeSolve);
                        return TermBuilder.DF.all(t, odeSolve);
                    } else if (term.op() == Modality.DIA) {
                        if (system.getInvariant(services).equals(
                                TermBuilder.DF.tt())) {
                            odeSolve = updatedPost;
                        } else {
                            odeSolve = TermBuilder.DF.and(invariant,
                                    updatedPost);
                        }
                        odeSolve = TermBuilder.DF.and(tGeqZero, odeSolve);
                        return TermBuilder.DF.ex(t, odeSolve);
                    } else {
                        throw new IllegalStateException("Unknown modality "
                                + term.op());
                    }
                }
            }
            return term;
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Cannot create TermFactory", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot create TermFactory", e);
        } catch (InstantiationException e) {
            throw new IllegalStateException("Cannot create TermFactory", e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Cannot create TermFactory", e);
        }
    }

    /**
     * @param services
     * @param tmpVars
     * @param odeSolve
     * @return
     */
    private Term replaceAll(Services services,
            Map<ProgramVariable, ProgramElement> tmpVars, Term t) {
        if (tmpVars.containsKey(t.op())) {
            return services.getTypeConverter().convertToLogicElement(
                    tmpVars.get(t.op()));
        } else {
            Term[] args = new Term[t.arity()];
            for (int i = 0; i < t.arity(); i++) {
                args[i] = replaceAll(services, tmpVars, t.sub(i));
            }
            ImmutableArray<QuantifiableVariable>[] boundVars = new ImmutableArray[t
                    .arity()];
            for (int i = 0, arity = t.arity(); i < arity; i++) {
                boundVars[i] = t.varsBoundHere(i);
            }

            return de.uka.ilkd.key.logic.TermFactory.DEFAULT.createTerm(t.op(),
                    args, boundVars, t.javaBlock());
        }

    }

    /**
     * @param collectNonRigidFunctionTerms
     * @param services
     * @return
     */
    private Map<ProgramVariable, ProgramElement> createTmpVariables(
            Set<ProgramElement> collectNonRigidFunctionTerms, Services services) {
        Map<ProgramVariable, ProgramElement> abbr = new LinkedHashMap<ProgramVariable, ProgramElement>();
        String prefix = "tmp";
        for (ProgramElement p : collectNonRigidFunctionTerms) {
            LocationVariable tmp = new LocationVariable(new ProgramElementName(
                    services.getNamespaces().getUniqueName(prefix)), RealLDT.getRealSort());
            services.getNamespaces().programVariables().add(tmp);
            abbr.put(tmp, p);
        }
        return abbr;
    }

    /**
     * @param system
     */
    private Set<ProgramElement> collectNonRigidFunctionTerms(ProgramElement p) {
        // use a treeset as we want to get rid of equal elements
        TreeSet<ProgramElement> result = new TreeSet<ProgramElement>(
                new Comparator<ProgramElement>() {

                    @Override
                    public int compare(ProgramElement o1, ProgramElement o2) {
                        if (o1.equals(o2)) {
                            return 0;
                        } else {
                            return o1.toString().compareTo(o2.toString());
                        }
                    }
                });
        if (p instanceof DLNonTerminalProgramElement) {
            for (ProgramElement c : (DLNonTerminalProgramElement) p) {
                result.addAll(collectNonRigidFunctionTerms(c));
            }
        }
        if (p instanceof FunctionTerm) {
            if (((FunctionTerm) p).getChildAt(0) instanceof NonRigidFunction) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * @return
     */
    private TermSymbol getNull(Services services) {
        Function f = (Function) services.getNamespaces().functions()
                .lookup(new Name("0"));
        if (f == null) {
            f = NumberCache.getNumber(BigDecimal.ZERO, (Sort) services
                    .getNamespaces().sorts().lookup(new Name("R")));
            services.getNamespaces().functions().add(f);
        }
        return f;
    }

}
