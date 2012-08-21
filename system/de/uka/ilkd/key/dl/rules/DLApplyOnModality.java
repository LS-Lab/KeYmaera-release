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
/**
 * File created 05.02.2007
 */
package de.uka.ilkd.key.dl.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import de.uka.ilkd.key.dl.formulatools.ReplaceVisitor;
import de.uka.ilkd.key.dl.model.And;
import de.uka.ilkd.key.dl.model.Assign;
import de.uka.ilkd.key.dl.model.Biimplies;
import de.uka.ilkd.key.dl.model.Choice;
import de.uka.ilkd.key.dl.model.Chop;
import de.uka.ilkd.key.dl.model.CompoundFormula;
import de.uka.ilkd.key.dl.model.Constant;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.DLStatementBlock;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.Expression;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.FunctionTerm;
import de.uka.ilkd.key.dl.model.IfExpr;
import de.uka.ilkd.key.dl.model.IfStatement;
import de.uka.ilkd.key.dl.model.Implies;
import de.uka.ilkd.key.dl.model.LogicalVariable;
import de.uka.ilkd.key.dl.model.MetaVariable;
import de.uka.ilkd.key.dl.model.Not;
import de.uka.ilkd.key.dl.model.Or;
import de.uka.ilkd.key.dl.model.Parallel;
import de.uka.ilkd.key.dl.model.Predicate;
import de.uka.ilkd.key.dl.model.PredicateTerm;
import de.uka.ilkd.key.dl.model.Quest;
import de.uka.ilkd.key.dl.model.RandomAssign;
import de.uka.ilkd.key.dl.model.Star;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.VariableDeclaration;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.NonRigidFunction;
import de.uka.ilkd.key.logic.op.NonRigidFunctionLocation;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.ProgramMethod;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.rule.UpdateSimplifier;
import de.uka.ilkd.key.rule.updatesimplifier.ApplyOnModality;
import de.uka.ilkd.key.rule.updatesimplifier.AssignmentPair;
import de.uka.ilkd.key.rule.updatesimplifier.Update;
import de.uka.ilkd.key.rule.updatesimplifier.UpdateSimplifierTermFactory;

/**
 * Implementation of application algorithm of updates to modalities. This class
 * is used by the UpdateSimplifier. Updates are applied if the location does not
 * occur on the right side of an assignment or its divaration inside a
 * differential equation.
 * 
 * @author jdq
 * @since 05.02.2007
 * 
 */
public class DLApplyOnModality extends ApplyOnModality {

    public TermFactory tf;

    /**
     * @param updateSimplifier
     * @param deletionEnabled
     */
    public DLApplyOnModality(UpdateSimplifier updateSimplifier) {
        super(updateSimplifier, false);

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.updatesimplifier.ApplyOnModality#isApplicable(de.uka.ilkd.key.rule.updatesimplifier.Update,
     *      de.uka.ilkd.key.logic.Term)
     */
    /*@Override*/
    public boolean isApplicable(Update update, Term target, Services services) {
        return super.isApplicable(update, target, services)
                && DLOptionBean.INSTANCE.isApplyUpdatesToModalities()
                && applyableUpdates(update, target, services);
    }

    /**
     * @param update
     * @param target
     * @return
     */
    private boolean applyableUpdates(Update update, Term target, Services services) {
        HashSet protectedVars = collectProgramVariables(target, services);
        if(protectedVars.contains(PROTECT_ALL))
            return false;
        for (int i = 0; i < update.locationCount(); i++) {
            if (!protectedVars.contains(update.location(i))) {
                boolean found = false;
                for (ProgramVariable v : ProgramVariableCollector.INSTANCE
                        .startSearch(update.getAssignmentPair(i).value())) {
                    if (protectedVars.contains(v)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.updatesimplifier.ApplyOnModality#apply(de.uka.ilkd.key.rule.updatesimplifier.Update,
     *      de.uka.ilkd.key.logic.Term, de.uka.ilkd.key.java.Services)
     */
    /*@Override*/
    public Term apply(Update update, Term target, Services services) {
        if (tf == null) {
            try {
                tf = TermFactory.getTermFactory(DLOptionBean.INSTANCE
						.getTermFactoryClass(), services.getNamespaces());
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException(
                        "Cannot create TermFactory for dL formulas");
            }
        }
        // target = super.apply(update, target, services);

        // now we apply the update to the term
        if (target.javaBlock() != null) {
            HashSet protectedVars = collectProgramVariables(target, services);
            target = applyUpdate(target, update, protectedVars);
        }

        return target;
    }

    /**
     * @param target
     * @param update
     * @param protectedVars
     * @return
     */
    private Term applyUpdate(Term target, Update update, HashSet protectedVars) {
        StatementBlock statementBlock = ((StatementBlock) target.javaBlock()
                .program());
        if (statementBlock != null && statementBlock.getChildCount() > 0) {
            DLProgram convert = (DLProgram) convert(statementBlock
                    .getChildAt(0), update, protectedVars);
            JavaBlock jb = JavaBlock.createJavaBlock(new DLStatementBlock(
                    convert));
            List<AssignmentPair> propagatePairs = new ArrayList<AssignmentPair>();
            for (int i = 0; i < update.locationCount(); i++) {
                if (!protectedLocation(update.location(i), protectedVars)) {
                    boolean found = false;
                    for (ProgramVariable v : ProgramVariableCollector.INSTANCE
                            .startSearch(update.getAssignmentPair(i).value())) {
                        if (protectedVars.contains(v)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        propagatePairs.add(update.getAssignmentPair(i));
                    }
                }
            }
            Term result = de.uka.ilkd.key.logic.TermFactory.DEFAULT
                    .createProgramTerm(target.op(), jb,
                            UpdateSimplifierTermFactory.DEFAULT
                                    .createUpdateTerm(propagatePairs
                                            .toArray(new AssignmentPair[0]),
                                            target.sub(0)));
            return UpdateSimplifierTermFactory.DEFAULT.createUpdateTerm(remove(
                    update, target, Main.getInstance().mediator().getServices()), updateSimplifier().simplify(result,
                    Main.getInstance().mediator().getServices()));
        }
        return target;
    }

    /**
     * @param childAt
     * @param update
     * @param protectedVars
     * @return
     */
    private DLProgramElement convert(ProgramElement childAt, Update update,
            HashSet protectedVars) {
        DLProgramElement result = null;
        if (childAt instanceof Chop) {
            Chop chop = (Chop) childAt;
            result = tf.createChop((DLProgram) convert(chop.getChildAt(0),
                    update, protectedVars), (DLProgram) convert(chop
                    .getChildAt(1), update, protectedVars));
        } else if (childAt instanceof Choice) {
            Choice choice = (Choice) childAt;
            result = tf.createChoice((DLProgram) convert(choice.getChildAt(0),
                    update, protectedVars), (DLProgram) convert(choice
                    .getChildAt(1), update, protectedVars));
        } else if (childAt instanceof Star) {
            Star p = (Star) childAt;
            Star createStar = tf.createStar((DLProgram) convert(
                    p.getChildAt(0), update, protectedVars));
            result = createStar;

        } else if (childAt instanceof IfStatement) {
            IfStatement ifS = (IfStatement) childAt;
            result = tf.createIf((Formula) convert(ifS.getExpression(), update,
                    protectedVars), (DLProgram) convert(ifS.getThen(), update,
                    protectedVars),
                    (ifS.getElse() != null) ? (DLProgram) convert(
                            ifS.getElse(), update, protectedVars) : null);
        } else if (childAt instanceof Parallel) {
            Parallel parallel = (Parallel) childAt;
            result = tf.createParallel((DLProgram) convert(parallel
                    .getChildAt(0), update, protectedVars),
                    (DLProgram) convert(parallel.getChildAt(1), update,
                            protectedVars));
        } else if (childAt instanceof Implies) {
            CompoundFormula p = (CompoundFormula) childAt;
            result = tf.createImpl((Formula) convert(p.getChildAt(0), update,
                    protectedVars), (Formula) convert(p.getChildAt(1), update,
                    protectedVars));
        } else if (childAt instanceof Not) {
            CompoundFormula p = (CompoundFormula) childAt;
            result = tf.createNot((Formula) convert(p.getChildAt(0), update,
                    protectedVars));
        } else if (childAt instanceof And) {
            CompoundFormula p = (CompoundFormula) childAt;
            result = tf.createAnd((Formula) convert(p.getChildAt(0), update,
                    protectedVars), (Formula) convert(p.getChildAt(1), update,
                    protectedVars));
        } else if (childAt instanceof Biimplies) {
            CompoundFormula p = (CompoundFormula) childAt;
            result = tf.createBiImpl((Formula) convert(p.getChildAt(0), update,
                    protectedVars), (Formula) convert(p.getChildAt(1), update,
                    protectedVars));
        } else if (childAt instanceof Or) {
            CompoundFormula p = (CompoundFormula) childAt;
            result = tf.createOr((Formula) convert(p.getChildAt(0), update,
                    protectedVars), (Formula) convert(p.getChildAt(1), update,
                    protectedVars));
        } else if (childAt instanceof PredicateTerm) {
            PredicateTerm p = (PredicateTerm) childAt;
            Predicate pred = (Predicate) convert(p.getChildAt(0), update,
                    protectedVars);
            List<Expression> children = new ArrayList<Expression>();
            for (int i = 1; i < p.getChildCount(); i++) {
                children.add((Expression) convert(p.getChildAt(i), update,
                        protectedVars));
            }
            result = tf.createPredicateTerm(pred, children);
        } else if (childAt instanceof FunctionTerm) {
            FunctionTerm p = (FunctionTerm) childAt;
            de.uka.ilkd.key.dl.model.Function pred = (de.uka.ilkd.key.dl.model.Function) convert(
                    p.getChildAt(0), update, protectedVars);
            List<Expression> children = new ArrayList<Expression>();
            for (int i = 1; i < p.getChildCount(); i++) {
                children.add((Expression) convert(p.getChildAt(i), update,
                        protectedVars));
            }
            result = tf.createFunctionTerm(pred, children);
        } else if (childAt instanceof IfExpr) {
            IfExpr i = (IfExpr) childAt;
            result = tf.createIfExpr(
                    (Formula) convert(i.getChildAt(0), update, protectedVars),
                    (Expression) convert(i.getChildAt(1), update, protectedVars),
                    (Expression) convert(i.getChildAt(2), update, protectedVars));
        } else if (childAt instanceof Predicate) {
            result = (Predicate) childAt;
        } else if (childAt instanceof de.uka.ilkd.key.dl.model.Function) {
            result = (de.uka.ilkd.key.dl.model.Function) childAt;
        } else if (childAt instanceof Constant) {
            result = (Constant) childAt;
        } else if (childAt instanceof DiffSystem) {
            List<Formula> children = new ArrayList<Formula>();
            for (ProgramElement p : (DiffSystem) childAt) {
                children.add((Formula) convert(p, update, protectedVars));
            }
            result = tf.createDiffSystem(children);
        } else if (childAt instanceof Assign) {
            Assign a = (Assign) childAt;
            result = tf
                    .createAssign( a.getChildAt(0), (Expression) convert(a
                            .getChildAt(1), update, protectedVars));
        } else if (childAt instanceof Dot) {
            result = (Dot) childAt;
        } else if (childAt instanceof RandomAssign) {
            result = (RandomAssign) childAt;
        } else if (childAt instanceof VariableDeclaration) {
            result = (VariableDeclaration) childAt;
        } else if (childAt instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
            de.uka.ilkd.key.dl.model.ProgramVariable pv = (de.uka.ilkd.key.dl.model.ProgramVariable) childAt;
            // Apply updates beginning from the last location
            for (int i = update.getAllAssignmentPairs().size() - 1; i >= 0; i--) {
                if (!protectedLocation(update.getAssignmentPair(i).location(),
                        protectedVars)) {

                    if (update.location(i).name().toString().equals(
                            pv.getElementName().toString())) {
                        boolean found = false;
                        for (ProgramVariable v : ProgramVariableCollector.INSTANCE
                                .startSearch(update.getAssignmentPair(i)
                                        .value())) {
                            if (protectedVars.contains(v)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            result = ReplaceVisitor.convertToProgram(update
                                    .getAssignmentPair(i).value(), tf);
                            // FIXME: we might need to apply the rest of the update to the term result
                            break;
                        }
                    }
                }
            }
            if (result == null) {
                return pv;
            }
        } else if (childAt instanceof LogicalVariable) {
            result = (LogicalVariable) childAt;
        } else if (childAt instanceof MetaVariable) {
            result = (MetaVariable) childAt;
        } else if (childAt instanceof Quest) {
            result = tf.createQuest((Formula) convert(((Quest) childAt)
                    .getChildAt(0), update, protectedVars));
        }
        if (result == null) {
            throw new IllegalArgumentException("Dont now how to convert: "
                    + childAt);
        }
        if (childAt instanceof DLProgramElement) {
            DLProgramElement el = (DLProgramElement) childAt;
            for (String annotation : el.getDLAnnotations().keySet()) {
                final List<Formula> annotations = el
                        .getDLAnnotation(annotation);
                List<Formula> newAnnon = new ArrayList<Formula>();

                for (Formula form : annotations) {
                    newAnnon
                            .add((Formula) convert(form, update, protectedVars));
                }

                result.setDLAnnotation(annotation, newAnnon);
            }
        }
        return result;
    }

    /**
     * collects all local program variables
     * 
     * @param target
     * @return
     */
    /*@Override*/
    protected HashSet collectProgramVariables(Term target, Services services) {
        if (protectedVarsCache.containsKey(target)) {
            return (HashSet) protectedVarsCache.get(target);
        }
        HashSet foundProgVars = new HashSet();

        final Operator targetOp = target.op();

        if (targetOp instanceof ProgramVariable) {
            foundProgVars.add(targetOp);
        } else if (((targetOp instanceof NonRigidFunction || targetOp instanceof NonRigidFunctionLocation) && !(targetOp instanceof ProgramMethod))) {
            foundProgVars.add(PROTECT_ALL);
            return foundProgVars;
        }

        if (target.javaBlock() != JavaBlock.EMPTY_JAVABLOCK) {
            foundProgVars.addAll(getProgramVariables(((StatementBlock) target
                    .javaBlock().program()).getChildAt(0)));
        }

        for (int i = 0; i < target.arity(); i++) {
            foundProgVars.addAll(collectProgramVariables(target.sub(i), services));
        }

        if (protectedVarsCache.size() >= 1000) {
            protectedVarsCache.clear();
        }

        protectedVarsCache.put(target, foundProgVars);
        return foundProgVars;
    }

    /**
     * @return
     */
    private Collection<ProgramElement> getProgramVariables(ProgramElement form) {
        HashSet<ProgramElement> result = new HashSet<ProgramElement>();
        if (form instanceof Dot) {
            Dot dot = (Dot) form;
            if(dot.getChildAt(0) instanceof ProgramVariable) {
                result.add(dot.getChildAt(0));
            } else if (dot.getChildAt(0) instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
                de.uka.ilkd.key.dl.model.ProgramVariable pv = (de.uka.ilkd.key.dl.model.ProgramVariable) dot
                        .getChildAt(0);
                ProgramVariable kpv = (ProgramVariable) Main.getInstance()
                        .mediator().getServices().getNamespaces()
                        .programVariables().lookup(pv.getElementName());
                if (kpv == null) {
                    throw new IllegalStateException("ProgramVariable " + pv
                            + " is not declared");
                }
                result.add(kpv);
            }
        } else if (form instanceof RandomAssign) {
            RandomAssign dot = (RandomAssign) form;
            if(dot.getChildAt(0) instanceof ProgramVariable) {
                result.add(dot.getChildAt(0));
            } else if (dot.getChildAt(0) instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
                de.uka.ilkd.key.dl.model.ProgramVariable pv = (de.uka.ilkd.key.dl.model.ProgramVariable) dot
                        .getChildAt(0);
                ProgramVariable kpv = (ProgramVariable) Main.getInstance()
                        .mediator().getServices().getNamespaces()
                        .programVariables().lookup(pv.getElementName());
                if (kpv == null) {
                    throw new IllegalStateException("ProgramVariable " + pv
                            + " is not declared");
                }
                result.add(kpv);
            }
        } else if (form instanceof Assign) {
            Assign assign = (Assign) form;
            if(assign.getChildAt(0) instanceof ProgramVariable) {
                result.add(assign.getChildAt(0));
            } else if (assign.getChildAt(0) instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
                de.uka.ilkd.key.dl.model.ProgramVariable pv = (de.uka.ilkd.key.dl.model.ProgramVariable) assign
                        .getChildAt(0);
                ProgramVariable kpv = (ProgramVariable) Main.getInstance()
                        .mediator().getServices().getNamespaces().lookup(
                                pv.getElementName());
                if (kpv != null) {
                    result.add(kpv);
                }
            }
        } else if (form instanceof DLNonTerminalProgramElement) {
            DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) form;
            for (ProgramElement p : dlnpe) {
                result.addAll(getProgramVariables(p));
            }
        }

        return result;
    }

    public static class ProgramVariableCollector extends Visitor {
        public static final ProgramVariableCollector INSTANCE = new ProgramVariableCollector();

        private HashSet<ProgramVariable> variables;

        public HashSet<ProgramVariable> startSearch(Term t) {
            variables = new HashSet<ProgramVariable>();
            t.execPostOrder(this);
            return variables;
        }

        /*
         * (non-Javadoc)
         * 
         * @see de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
         */
        /*@Override*/
        public void visit(Term visited) {
            if (visited.op() instanceof ProgramVariable) {
                variables.add((ProgramVariable) visited.op());
            }
        }

    }
}
