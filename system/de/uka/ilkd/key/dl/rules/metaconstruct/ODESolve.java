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

import java.math.BigDecimal;
import java.rmi.RemoteException;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.IODESolver.ODESolverResult;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.parser.NumberCache;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.TermSymbol;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * @author jdq
 * 
 */
public class ODESolve extends AbstractDLMetaOperator {

    public static final Name NAME = new Name("#ODESolve");

    public ODESolve() {
        super(NAME, 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#sort(de.uka.ilkd.key.logic.Term[])
     */
    @Override
    public Sort sort(Term[] term) {
        return Sort.FORMULA;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#calculate(de.uka.ilkd.key.logic.Term,
     *      de.uka.ilkd.key.rule.inst.SVInstantiations,
     *      de.uka.ilkd.key.java.Services)
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

    public Term odeSolve(Term term, Services services) throws RemoteException, SolverException {
        DiffSystem system = (DiffSystem) ((StatementBlock) term
                .javaBlock().program()).getChildAt(0);
        LogicVariable t = null;
        LogicVariable ts = null;
        int i = 0;
        final NamespaceSet nss = services.getNamespaces();
        Name tName = null;
        do {
            tName = new Name("t" + "_" + i++);
        } while (nss.variables().lookup(tName) != null
                || nss.programVariables().lookup(tName) != null);
        t = new LogicVariable(tName, RealLDT.getRealSort());
        i = 0;
        Name tsName = null;
        do {
            tsName = new Name("ts" + "_" + i++);
        } while ((nss.variables().lookup(tsName) != null || nss
                .programVariables().lookup(tsName) != null));
        ts = new LogicVariable(tsName, RealLDT.getRealSort());
        nss.variables().add(t);
        nss.variables().add(ts);
        Term post = term.sub(0);
        Term odeSolve;
        if (system.getDifferentialEquations().isEmpty()) {
            // optimize no differential equations
            Term invariant = system.getInvariant();
            if (term.op() == Modality.BOX
                    || term.op() == Modality.TOUT) {
                return TermBuilder.DF.imp(invariant, post);
            } else if (term.op() == Modality.DIA) {
                return TermBuilder.DF.and(invariant, post);
            } else {
                throw new IllegalStateException("Unknown modality "
                        + term.op());
            }
        } else {
                ODESolverResult odeResult = MathSolverManager
                        .getCurrentODESolver().odeSolve(system, t, ts, post,
                                nss);

                if (term.op() == Modality.BOX
                        || term.op() == Modality.TOUT) {
                    if (system.getInvariant().equals(TermBuilder.DF.tt())) {
                        odeSolve = odeResult.getPostCondition();
                    } else {
                        odeSolve = TermBuilder.DF.imp(odeResult
                                .getInvariantExpression(), odeResult
                                .getPostCondition());
                    }
                    odeSolve = TermBuilder.DF.imp(TermBuilder.DF.func(
                            (Function) nss.functions().lookup(new Name("geq")),
                            new Term[] { TermBuilder.DF.var(t),
                                    TermBuilder.DF.func(getNull(services)) }),
                            odeSolve);
                    return TermBuilder.DF.all(t, odeSolve);
                } else if (term.op() == Modality.DIA) {
                    if (system.getInvariant().equals(TermBuilder.DF.tt())) {
                        odeSolve = odeResult.getPostCondition();
                    } else {
                        odeSolve = TermBuilder.DF.and(odeResult
                                .getInvariantExpression(), odeResult
                                .getPostCondition());
                    }
                    odeSolve = TermBuilder.DF.and(TermBuilder.DF.func(
                            (Function) nss.functions().lookup(new Name("geq")),
                            new Term[] { TermBuilder.DF.var(t),
                                    TermBuilder.DF.func(getNull(services)) }),
                            odeSolve);
                    return TermBuilder.DF.ex(t, odeSolve);
                } else {
                    throw new IllegalStateException("Unknown modality "
                            + term.op());
                }
        }
    }

    /**
     * @return
     */
    private TermSymbol getNull(Services services) {
        Function f = (Function) services.getNamespaces().functions().lookup(
                new Name("0"));
        if (f == null) {
            f = NumberCache.getNumber(BigDecimal.ZERO, (Sort) services
                    .getNamespaces().sorts().lookup(new Name("R")));
            services.getNamespaces().functions().add(f);
        }
        return f;
    }

}
