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
 * File created 01.03.2007
 */
package de.uka.ilkd.key.dl.formulatools;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
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
import de.uka.ilkd.key.dl.model.Exists;
import de.uka.ilkd.key.dl.model.Expression;
import de.uka.ilkd.key.dl.model.Forall;
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
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.dl.model.Quantified;
import de.uka.ilkd.key.dl.model.Quest;
import de.uka.ilkd.key.dl.model.RandomAssign;
import de.uka.ilkd.key.dl.model.Star;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.Variable;
import de.uka.ilkd.key.dl.model.VariableDeclaration;
import de.uka.ilkd.key.dl.model.impl.EqualsImpl;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Equality;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.ProgramSV;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.Quantifier;
import de.uka.ilkd.key.logic.op.SchemaVariable;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * Class that is capable of applying a substitution to a DLProgramElement.
 * 
 * @author jdq
 * @since 01.03.2007
 * 
 */
public class ReplaceVisitor {

	/**
	 * @param childAt
	 * @param tf
	 *            TODO
	 * @return
	 */
	public static DLProgramElement convert(ProgramElement childAt,
			Map<QuantifiableVariable, Term> substitutionMap, TermFactory tf) {
		DLProgramElement result = null;
		if (childAt instanceof Chop) {
			Chop chop = (Chop) childAt;
			result = tf.createChop((DLProgram) convert(chop.getChildAt(0),
					substitutionMap, tf), (DLProgram) convert(chop
					.getChildAt(1), substitutionMap, tf));
		} else if (childAt instanceof Choice) {
			Choice choice = (Choice) childAt;
			result = tf.createChoice((DLProgram) convert(choice.getChildAt(0),
					substitutionMap, tf), (DLProgram) convert(choice
					.getChildAt(1), substitutionMap, tf));
		} else if (childAt instanceof Star) {
			Star p = (Star) childAt;
			Star createStar = tf.createStar((DLProgram) convert(
					p.getChildAt(0), substitutionMap, tf));
			result = createStar;
		} else if (childAt instanceof Parallel) {
			Parallel parallel = (Parallel) childAt;
			result = tf.createParallel((DLProgram) convert(parallel
					.getChildAt(0), substitutionMap, tf), (DLProgram) convert(
					parallel.getChildAt(1), substitutionMap, tf));
		} else if (childAt instanceof Implies) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createImpl((Formula) convert(p.getChildAt(0),
					substitutionMap, tf), (Formula) convert(p.getChildAt(1),
					substitutionMap, tf));
		} else if (childAt instanceof Not) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createNot((Formula) convert(p.getChildAt(0),
					substitutionMap, tf));
		} else if (childAt instanceof And) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createAnd((Formula) convert(p.getChildAt(0),
					substitutionMap, tf), (Formula) convert(p.getChildAt(1),
					substitutionMap, tf));
		} else if (childAt instanceof Biimplies) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createBiImpl((Formula) convert(p.getChildAt(0),
					substitutionMap, tf), (Formula) convert(p.getChildAt(1),
					substitutionMap, tf));
		} else if (childAt instanceof Or) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createOr((Formula) convert(p.getChildAt(0),
					substitutionMap, tf), (Formula) convert(p.getChildAt(1),
					substitutionMap, tf));
		} else if (childAt instanceof PredicateTerm) {
			PredicateTerm p = (PredicateTerm) childAt;
			Predicate pred = (Predicate) convert(p.getChildAt(0),
					substitutionMap, tf);
			List<Expression> children = new ArrayList<Expression>();
			for (int i = 1; i < p.getChildCount(); i++) {
				children.add((Expression) convert(p.getChildAt(i),
						substitutionMap, tf));
			}
			result = tf.createPredicateTerm(pred, children);
		} else if (childAt instanceof FunctionTerm) {
			FunctionTerm p = (FunctionTerm) childAt;
			de.uka.ilkd.key.dl.model.Function pred = (de.uka.ilkd.key.dl.model.Function) convert(
					p.getChildAt(0), substitutionMap, tf);
			List<Expression> children = new ArrayList<Expression>();
			for (int i = 1; i < p.getChildCount(); i++) {
				children.add((Expression) convert(p.getChildAt(i),
						substitutionMap, tf));
			}
			result = tf.createFunctionTerm(pred, children);
		} else if(childAt instanceof IfExpr) {
		    IfExpr i = (IfExpr) childAt;
            result = tf.createIfExpr(
                    (Formula) convert(i.getChildAt(0), substitutionMap, tf),
                    (Expression) convert(i.getChildAt(1), substitutionMap, tf),
                    (Expression) convert(i.getChildAt(2), substitutionMap, tf));
		} else if (childAt instanceof Predicate) {
			result = (Predicate) childAt;
		} else if (childAt instanceof de.uka.ilkd.key.dl.model.Function) {
			result = (de.uka.ilkd.key.dl.model.Function) childAt;
		} else if (childAt instanceof Constant) {
			result = (Constant) childAt;
		} else if (childAt instanceof DiffSystem) {
			List<Formula> children = new ArrayList<Formula>();
			for (ProgramElement p : (DiffSystem) childAt) {
				children.add((Formula) convert(p, substitutionMap, tf));
			}
			result = tf.createDiffSystem(children);
		} else if (childAt instanceof Assign) {
			Assign a = (Assign) childAt;
			if(a.getChildAt(0) instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
			    result = tf.createAssign(
			            (de.uka.ilkd.key.dl.model.ProgramVariable) a.getChildAt(0),
			            (Expression) convert(a.getChildAt(1), substitutionMap, tf));
			} else {
			    FunctionTerm ft = (FunctionTerm) a.getChildAt(0);
			    result = tf.createAssign((FunctionTerm)convert(ft, substitutionMap, tf),
                        (Expression) convert(a.getChildAt(1), substitutionMap, tf));
			}
		} else if (childAt instanceof Dot) {
			result = (Dot) childAt;
		} else if (childAt instanceof RandomAssign) {
			result = (RandomAssign) childAt;
		} else if (childAt instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
			de.uka.ilkd.key.dl.model.ProgramVariable pv = (de.uka.ilkd.key.dl.model.ProgramVariable) childAt;
			result = pv;
		} else if (childAt instanceof LogicalVariable) {
			LogicalVariable logicalVariable = (LogicalVariable) childAt;
			for (QuantifiableVariable v : substitutionMap.keySet()) {
				if (logicalVariable.getElementName().equals(v.name())) {
					result = convertToProgram(substitutionMap.get(v), tf);
				}
			}
			if (result == null) {
				result = logicalVariable;
			}
		} else if (childAt instanceof MetaVariable) {
			result = (MetaVariable) childAt;
		} else if (childAt instanceof Quest) {
			result = tf.createQuest((Formula) convert(((Quest) childAt)
					.getChildAt(0), substitutionMap, tf));
		} else if (childAt instanceof VariableDeclaration) {
			result = (VariableDeclaration) childAt;
		} else if (childAt instanceof IfStatement) {
			IfStatement ifS = (IfStatement) childAt;
			result = tf.createIf((Formula) convert(ifS.getExpression(),
					substitutionMap, tf), (DLProgram) convert(ifS.getThen(),
					substitutionMap, tf), (ifS.getElse() == null) ? null
					: (DLProgram) convert(ifS.getElse(), substitutionMap, tf));
		} else if (childAt instanceof Quantified) {
			// we need to remove all variables bound by this quantifier
			DLNonTerminalProgramElement f = (DLNonTerminalProgramElement) childAt;
			VariableDeclaration decl = (VariableDeclaration) f.getChildAt(0);
			DLProgram sub = (DLProgram) f.getChildAt(1);
			Map<QuantifiableVariable, Term> newSubstitutionMap = new HashMap<QuantifiableVariable, Term>(
                    substitutionMap);
            out: for (QuantifiableVariable q : substitutionMap.keySet()) {
                for (int i = 1; i < decl.getChildCount(); i++) {
                    if (q.name().toString().equals(
                            ((Variable) decl.getChildAt(i)).getElementName()
                                    .toString())) {
                        newSubstitutionMap.remove(q);
                        continue out;
                    }
                    for(QuantifiableVariable var: substitutionMap.get(q).freeVars()) {
                        if (var.name().toString().equals(
                                ((Variable) decl.getChildAt(i)).getElementName()
                                        .toString())) {
                            newSubstitutionMap.remove(q);
                            continue out;
                        }   
                    }
                }
            }
			result = tf.createQuantified(decl, (DLProgram) convert(sub, newSubstitutionMap, tf));
		} else if (childAt instanceof Forall || childAt instanceof Exists) {
			// we need to remove all variables bound by this quantifier
			DLNonTerminalProgramElement f = (DLNonTerminalProgramElement) childAt;
			VariableDeclaration decl = (VariableDeclaration) f.getChildAt(0);
			Formula form = (Formula) f.getChildAt(1);
			Map<QuantifiableVariable, Term> newSubstitutionMap = new HashMap<QuantifiableVariable, Term>(
					substitutionMap);
			out: for (QuantifiableVariable q : substitutionMap.keySet()) {
				for (int i = 1; i < decl.getChildCount(); i++) {
					if (q.name().toString().equals(
							((Variable) decl.getChildAt(i)).getElementName()
									.toString())) {
						newSubstitutionMap.remove(q);
						continue out;
					}
					for(QuantifiableVariable var: substitutionMap.get(q).freeVars()) {
						if (var.name().toString().equals(
								((Variable) decl.getChildAt(i)).getElementName()
										.toString())) {
							newSubstitutionMap.remove(q);
							continue out;
						}	
					}
				}
			}
			if (childAt instanceof Forall) {
				result = tf.createForall(decl, (Formula) convert(form,
						newSubstitutionMap, tf));
			} else {
				result = tf.createExists(decl, (Formula) convert(form,
						newSubstitutionMap, tf));
			}
		}
		if (result == null) {
			throw new IllegalArgumentException("Dont know how to convert: "
					+ childAt);
		}
		if (childAt instanceof DLProgramElement) {
			DLProgramElement el = (DLProgramElement) childAt;
			for (String annotation : el.getDLAnnotations().keySet()) {
				final List<Formula> annotations = el
						.getDLAnnotation(annotation);
				List<Formula> newAnnon = new ArrayList<Formula>();

				for (Formula form : annotations) {
					newAnnon.add((Formula) convert(form, substitutionMap, tf));
				}

				result.setDLAnnotation(annotation, newAnnon);
			}
		}
		return result;
	}
	/**
	 * @param childAt
	 * @param tf
	 *            TODO
	 * @return
	 */
	public static DLProgramElement convert(ProgramElement childAt,
			SVInstantiations inst, TermFactory tf) {
		DLProgramElement result = null;
		if (childAt instanceof SchemaVariable) {
			if(inst.getInstantiation((SchemaVariable) childAt) instanceof DLProgramElement) {
				result = (DLProgramElement) inst.getInstantiation((SchemaVariable) childAt);
			} else if(inst.getInstantiation((SchemaVariable) childAt) instanceof Term) {
				Term t = (Term) inst.getInstantiation((SchemaVariable) childAt);
				result = (DLProgramElement) ((DLStatementBlock)t.executableJavaBlock().program()).getFirstElement();
			}
		} else if (childAt instanceof Chop) {
			Chop chop = (Chop) childAt;
			result = tf.createChop((DLProgram) convert(chop.getChildAt(0),
					inst, tf), (DLProgram) convert(chop
					.getChildAt(1), inst, tf));
		} else if (childAt instanceof Choice) {
			Choice choice = (Choice) childAt;
			result = tf.createChoice((DLProgram) convert(choice.getChildAt(0),
					inst, tf), (DLProgram) convert(choice
					.getChildAt(1), inst, tf));
		} else if (childAt instanceof Star) {
			Star p = (Star) childAt;
			Star createStar = tf.createStar((DLProgram) convert(
					p.getChildAt(0), inst, tf));
			result = createStar;
		} else if (childAt instanceof Parallel) {
			Parallel parallel = (Parallel) childAt;
			result = tf.createParallel((DLProgram) convert(parallel
					.getChildAt(0), inst, tf), (DLProgram) convert(
					parallel.getChildAt(1), inst, tf));
		} else if (childAt instanceof Implies) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createImpl((Formula) convert(p.getChildAt(0),
					inst, tf), (Formula) convert(p.getChildAt(1),
					inst, tf));
		} else if (childAt instanceof Not) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createNot((Formula) convert(p.getChildAt(0),
					inst, tf));
		} else if (childAt instanceof And) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createAnd((Formula) convert(p.getChildAt(0),
					inst, tf), (Formula) convert(p.getChildAt(1),
					inst, tf));
		} else if (childAt instanceof Biimplies) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createBiImpl((Formula) convert(p.getChildAt(0),
					inst, tf), (Formula) convert(p.getChildAt(1),
					inst, tf));
		} else if (childAt instanceof Or) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createOr((Formula) convert(p.getChildAt(0),
					inst, tf), (Formula) convert(p.getChildAt(1),
					inst, tf));
		} else if (childAt instanceof PredicateTerm) {
			PredicateTerm p = (PredicateTerm) childAt;
			Predicate pred = (Predicate) convert(p.getChildAt(0),
					inst, tf);
			List<Expression> children = new ArrayList<Expression>();
			for (int i = 1; i < p.getChildCount(); i++) {
				children.add((Expression) convert(p.getChildAt(i),
						inst, tf));
			}
			result = tf.createPredicateTerm(pred, children);
		} else if (childAt instanceof FunctionTerm) {
			FunctionTerm p = (FunctionTerm) childAt;
			de.uka.ilkd.key.dl.model.Function pred = (de.uka.ilkd.key.dl.model.Function) convert(
					p.getChildAt(0), inst, tf);
			List<Expression> children = new ArrayList<Expression>();
			for (int i = 1; i < p.getChildCount(); i++) {
				children.add((Expression) convert(p.getChildAt(i),
						inst, tf));
			}
			result = tf.createFunctionTerm(pred, children);
		} else if(childAt instanceof IfExpr) {
		    IfExpr i = (IfExpr) childAt;
            result = tf.createIfExpr(
                    (Formula) convert(i.getChildAt(0), inst, tf),
                    (Expression) convert(i.getChildAt(1), inst, tf),
                    (Expression) convert(i.getChildAt(2), inst, tf));
		} else if (childAt instanceof Predicate) {
			result = (Predicate) childAt;
		} else if (childAt instanceof de.uka.ilkd.key.dl.model.Function) {
			result = (de.uka.ilkd.key.dl.model.Function) childAt;
		} else if (childAt instanceof Constant) {
			result = (Constant) childAt;
		} else if (childAt instanceof DiffSystem) {
			List<Formula> children = new ArrayList<Formula>();
			for (ProgramElement p : (DiffSystem) childAt) {
				children.add((Formula) convert(p, inst, tf));
			}
			result = tf.createDiffSystem(children);
		} else if (childAt instanceof Assign) {
			Assign a = (Assign) childAt;
			result = tf.createAssign(
			        convert(a.getChildAt(0), inst, tf),
					(Expression) convert(a.getChildAt(1), inst, tf));
		} else if (childAt instanceof Dot) {
		    Dot d = (Dot) childAt;
		    result = tf.createDot(convert(d.getChildAt(0), inst, tf), d.getOrder());
		} else if (childAt instanceof RandomAssign) {
			result = (RandomAssign) childAt;
		} else if (childAt instanceof SchemaVariable) {
			if(inst.getInstantiation((SchemaVariable) childAt) instanceof DLProgramElement) {
				result = (DLProgramElement) inst.getInstantiation((SchemaVariable) childAt);
			} else if(inst.getInstantiation((SchemaVariable) childAt) instanceof Term) {
				Term t = (Term) inst.getInstantiation((SchemaVariable) childAt);
				result = (DLProgramElement) ((DLStatementBlock)t.executableJavaBlock().program()).getFirstElement();
			}
		} else if (childAt instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
			de.uka.ilkd.key.dl.model.ProgramVariable pv = (de.uka.ilkd.key.dl.model.ProgramVariable) childAt;
			result = pv;
		} else if (childAt instanceof LogicalVariable) {
			result = (DLProgramElement) childAt;
		} else if (childAt instanceof MetaVariable) {
			result = (MetaVariable) childAt;
		} else if (childAt instanceof Quest) {
			result = tf.createQuest((Formula) convert(((Quest) childAt)
					.getChildAt(0), inst, tf));
		} else if (childAt instanceof VariableDeclaration) {
			result = (VariableDeclaration) childAt;
		} else if (childAt instanceof IfStatement) {
			IfStatement ifS = (IfStatement) childAt;
			result = tf.createIf((Formula) convert(ifS.getExpression(),
					inst, tf), (DLProgram) convert(ifS.getThen(),
					inst, tf), (ifS.getElse() == null) ? null
					: (DLProgram) convert(ifS.getElse(), inst, tf));
		} else if (childAt instanceof Quantified) {
			// we need to remove all variables bound by this quantifier
			DLNonTerminalProgramElement f = (DLNonTerminalProgramElement) childAt;
			VariableDeclaration decl;
			DLProgram sub = (DLProgram) f.getChildAt(1);
			
			if(f.getChildAt(0) instanceof SchemaVariable) {
			    decl = (VariableDeclaration) inst.getInstantiation((SchemaVariable) f.getChildAt(0));
			} else {
			    decl = (VariableDeclaration) f.getChildAt(0);
			}
			
			result = tf.createQuantified(decl, (DLProgram) convert(sub, inst, tf));
		} else if (childAt instanceof Forall || childAt instanceof Exists) {
			DLNonTerminalProgramElement f = (DLNonTerminalProgramElement) childAt;
			VariableDeclaration decl = (VariableDeclaration) f.getChildAt(0);
			Formula form = (Formula) f.getChildAt(1);
			if (childAt instanceof Forall) {
				result = tf.createForall(decl, (Formula) convert(form,
						inst, tf));
			} else {
				result = tf.createExists(decl, (Formula) convert(form,
						inst, tf));
			}
		}
		if (result == null) {
			throw new IllegalArgumentException("Dont know how to convert: "
					+ childAt);
		}
		if (childAt instanceof DLProgramElement && !(childAt instanceof ProgramSV)) {
			DLProgramElement el = (DLProgramElement) childAt;
			for (String annotation : el.getDLAnnotations().keySet()) {
				final List<Formula> annotations = el
						.getDLAnnotation(annotation);
				List<Formula> newAnnon = new ArrayList<Formula>();

				for (Formula form : annotations) {
					newAnnon.add((Formula) convert(form, inst, tf));
				}

				result.setDLAnnotation(annotation, newAnnon);
			}
		}
		return result;
	}
	
	/**
	 * @param childAt
	 * @param tf
	 *            TODO
	 * @return
	 */
	public static DLProgramElement replaceFunctionTerm(ProgramElement childAt,
			Map<FunctionTerm, ProgramVariable> substitutionMap, TermFactory tf) {
		DLProgramElement result = null;
		if (childAt instanceof Chop) {
			Chop chop = (Chop) childAt;
			result = tf.createChop((DLProgram) replaceFunctionTerm(chop.getChildAt(0),
					substitutionMap, tf), (DLProgram) replaceFunctionTerm(chop
					.getChildAt(1), substitutionMap, tf));
		} else if (childAt instanceof Choice) {
			Choice choice = (Choice) childAt;
			result = tf.createChoice((DLProgram) replaceFunctionTerm(choice.getChildAt(0),
					substitutionMap, tf), (DLProgram) replaceFunctionTerm(choice
					.getChildAt(1), substitutionMap, tf));
		} else if (childAt instanceof Star) {
			Star p = (Star) childAt;
			Star createStar = tf.createStar((DLProgram) replaceFunctionTerm(
					p.getChildAt(0), substitutionMap, tf));
			result = createStar;
		} else if (childAt instanceof Parallel) {
			Parallel parallel = (Parallel) childAt;
			result = tf.createParallel((DLProgram) replaceFunctionTerm(parallel
					.getChildAt(0), substitutionMap, tf), (DLProgram) replaceFunctionTerm(
					parallel.getChildAt(1), substitutionMap, tf));
		} else if (childAt instanceof Implies) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createImpl((Formula) replaceFunctionTerm(p.getChildAt(0),
					substitutionMap, tf), (Formula) replaceFunctionTerm(p.getChildAt(1),
					substitutionMap, tf));
		} else if (childAt instanceof Not) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createNot((Formula) replaceFunctionTerm(p.getChildAt(0),
					substitutionMap, tf));
		} else if (childAt instanceof And) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createAnd((Formula) replaceFunctionTerm(p.getChildAt(0),
					substitutionMap, tf), (Formula) replaceFunctionTerm(p.getChildAt(1),
					substitutionMap, tf));
		} else if (childAt instanceof Biimplies) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createBiImpl((Formula) replaceFunctionTerm(p.getChildAt(0),
					substitutionMap, tf), (Formula) replaceFunctionTerm(p.getChildAt(1),
					substitutionMap, tf));
		} else if (childAt instanceof Or) {
			CompoundFormula p = (CompoundFormula) childAt;
			result = tf.createOr((Formula) replaceFunctionTerm(p.getChildAt(0),
					substitutionMap, tf), (Formula) replaceFunctionTerm(p.getChildAt(1),
					substitutionMap, tf));
		} else if (childAt instanceof PredicateTerm) {
			PredicateTerm p = (PredicateTerm) childAt;
			Predicate pred = (Predicate) replaceFunctionTerm(p.getChildAt(0),
					substitutionMap, tf);
			List<Expression> children = new ArrayList<Expression>();
			for (int i = 1; i < p.getChildCount(); i++) {
				children.add((Expression) replaceFunctionTerm(p.getChildAt(i),
						substitutionMap, tf));
			}
			result = tf.createPredicateTerm(pred, children);
		} else if (childAt instanceof FunctionTerm) {
		    boolean match = false;
		    for(FunctionTerm ft: substitutionMap.keySet()) {
		        if(ft.equals(childAt)) {
		            result = substitutionMap.get(ft);
		            match = true;
		            break;
		        } 
		    }
		    if(!match) {
    			FunctionTerm p = (FunctionTerm) childAt;
    			de.uka.ilkd.key.dl.model.Function pred = (de.uka.ilkd.key.dl.model.Function) replaceFunctionTerm(
    					p.getChildAt(0), substitutionMap, tf);
    			List<Expression> children = new ArrayList<Expression>();
    			for (int i = 1; i < p.getChildCount(); i++) {
    				children.add((Expression) replaceFunctionTerm(p.getChildAt(i),
    						substitutionMap, tf));
    			}
    			result = tf.createFunctionTerm(pred, children);
		    }
		} else if(childAt instanceof IfExpr) {
		    IfExpr i = (IfExpr) childAt;
            result = tf.createIfExpr(
                    (Formula) replaceFunctionTerm(i.getChildAt(0), substitutionMap, tf),
                    (Expression) replaceFunctionTerm(i.getChildAt(1), substitutionMap, tf),
                    (Expression) replaceFunctionTerm(i.getChildAt(2), substitutionMap, tf));
		} else if (childAt instanceof Predicate) {
			result = (Predicate) childAt;
		} else if (childAt instanceof de.uka.ilkd.key.dl.model.Function) {
			result = (de.uka.ilkd.key.dl.model.Function) childAt;
		} else if (childAt instanceof Constant) {
			result = (Constant) childAt;
		} else if (childAt instanceof DiffSystem) {
			List<Formula> children = new ArrayList<Formula>();
			for (ProgramElement p : (DiffSystem) childAt) {
				children.add((Formula) replaceFunctionTerm(p, substitutionMap, tf));
			}
			result = tf.createDiffSystem(children);
		} else if (childAt instanceof Assign) {
			Assign a = (Assign) childAt;
			if(a.getChildAt(0) instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
			    result = tf.createAssign(
			            (de.uka.ilkd.key.dl.model.ProgramVariable) a.getChildAt(0),
			            (Expression) replaceFunctionTerm(a.getChildAt(1), substitutionMap, tf));
			} else {
			    FunctionTerm ft = (FunctionTerm) a.getChildAt(0);
			    result = tf.createAssign((FunctionTerm)replaceFunctionTerm(ft, substitutionMap, tf),
                        (Expression) replaceFunctionTerm(a.getChildAt(1), substitutionMap, tf));
			}
		} else if (childAt instanceof Dot) {
		    Dot d = (Dot) childAt;
		    result = tf.createDot(replaceFunctionTerm(d.getChildAt(0), substitutionMap, tf), d.getOrder());
		} else if (childAt instanceof RandomAssign) {
			result = (RandomAssign) childAt;
		} else if (childAt instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
			de.uka.ilkd.key.dl.model.ProgramVariable pv = (de.uka.ilkd.key.dl.model.ProgramVariable) childAt;
			result = pv;
		} else if (childAt instanceof LogicalVariable) {
			LogicalVariable logicalVariable = (LogicalVariable) childAt;
			result = logicalVariable;
		} else if (childAt instanceof MetaVariable) {
			result = (MetaVariable) childAt;
		} else if (childAt instanceof Quest) {
			result = tf.createQuest((Formula) replaceFunctionTerm(((Quest) childAt)
					.getChildAt(0), substitutionMap, tf));
		} else if (childAt instanceof VariableDeclaration) {
			result = (VariableDeclaration) childAt;
		} else if (childAt instanceof IfStatement) {
			IfStatement ifS = (IfStatement) childAt;
			result = tf.createIf((Formula) replaceFunctionTerm(ifS.getExpression(),
					substitutionMap, tf), (DLProgram) replaceFunctionTerm(ifS.getThen(),
					substitutionMap, tf), (ifS.getElse() == null) ? null
					: (DLProgram) replaceFunctionTerm(ifS.getElse(), substitutionMap, tf));
		}
		if (childAt instanceof DLProgramElement) {
			DLProgramElement el = (DLProgramElement) childAt;
			for (String annotation : el.getDLAnnotations().keySet()) {
				final List<Formula> annotations = el
						.getDLAnnotation(annotation);
				List<Formula> newAnnon = new ArrayList<Formula>();

				for (Formula form : annotations) {
					newAnnon.add((Formula) replaceFunctionTerm(form, substitutionMap, tf));
				}

				result.setDLAnnotation(annotation, newAnnon);
			}
		}
		return result;
	}

	/**
	 * Converts a Term representing an expression into an Expression
	 * ProgramElement
	 * 
	 *            a term representing an expression
	 * @return a ProgramElement representation of the expression
	 */
	public static Expression convertToProgram(Term form, TermFactory tf) {
		List<Expression> argList = new ArrayList<Expression>();
		for (int i = 0; i < form.arity(); i++) {
			argList.add(convertToProgram(form.sub(i), tf));
		}
		Expression[] args = argList.toArray(new Expression[0]);
		if (form.op() instanceof de.uka.ilkd.key.logic.op.Function) {
			de.uka.ilkd.key.logic.op.Function f = (de.uka.ilkd.key.logic.op.Function) form.op();
			if (form.op().name().toString().equals("add")) {
				return tf.createPlus(args[0], args[1]);
			} else if (f.name().toString().equals("sub")) {
				return tf.createMinus(args[0], args[1]);
			} else if (f.name().toString().equals("neg")) {
				return tf.createMinusSign(args[0]);
			} else if (f.name().toString().equals("mul")) {
				return tf.createMult(args[0], args[1]);
			} else if (f.name().toString().equals("div")) {
				return tf.createDiv(args[0], args[1]);
			} else if (f.name().toString().equals("exp")) {
				return tf.createExp(args[0], args[1]);
			} else {
				try {
					BigDecimal d = new BigDecimal(form.op().name().toString());
					return tf.createConstant(d);
				} catch (NumberFormatException e) {
					String name = form.op().name().toString();
					return tf.createFunctionTerm(name, Arrays.asList(args));
				}
			}
		} else if (form.op() instanceof LogicVariable) {
			// @todo assert namespaces.unique
			return tf.createLogicalVariable(form.op().name().toString());
		} else if (form.op() instanceof de.uka.ilkd.key.logic.op.ProgramVariable) {
			// @todo assert namespaces.unique
			return tf.createProgramVariable(form.op().name().toString());
		} else if (form.op() instanceof Metavariable) {
			// @todo assert namespaces.unique
			return tf.createMetaVariable(form.op().name().toString());
		}

		throw new IllegalArgumentException("Could not convert Term: " + form
				+ "Operator was: " + form.op());
	}

	/**
	 * Converts a Term representing a formula into an Formula ProgramElement
	 * 
	 *            a term representing an formula
	 * @return a ProgramElement representation of the formula
	 */
	public static Formula convertFormulaToProgram(Term form, TermFactory tf) {
		if (form.op() == Op.AND) {
			return tf.createAnd(convertFormulaToProgram(form.sub(0), tf),
                    convertFormulaToProgram(form.sub(1), tf));
		} else if (form.op() == Op.OR) {
			return tf.createOr(convertFormulaToProgram(form.sub(0), tf),
                    convertFormulaToProgram(form.sub(1), tf));
		} else if (form.op() == Op.IMP) {
			return tf.createImpl(convertFormulaToProgram(form.sub(0), tf),
					convertFormulaToProgram(form.sub(1), tf));
		} else if (form.op() == Op.EQV) {
			return tf.createBiImpl(convertFormulaToProgram(form.sub(0), tf),
					convertFormulaToProgram(form.sub(1), tf));
		} else if (form.op() == Op.NOT) {
			return tf.createNot(convertFormulaToProgram(form.sub(0), tf));
        } else if (form.op() instanceof Quantifier) {
            ImmutableArray<QuantifiableVariable> quantifiableVariables = form.varsBoundHere(0);
            Formula formula = convertFormulaToProgram(form.sub(0), tf);
            if(quantifiableVariables.size() == 0) {
                return formula;
            } else {
                Sort s = quantifiableVariables.get(0).sort();
                ArrayList<Variable> vars = new ArrayList<Variable>();
                for(QuantifiableVariable q: quantifiableVariables) {
                    vars.add(tf.createLogicalVariable(q.name().toString()));
                }
                VariableDeclaration dec = tf.createVariableDeclaration(s, vars);
                if(form.op() == Op.ALL) {
                    return tf.createForall(dec, formula);
                } else if(form.op() == Op.EX) {
                    return tf.createExists(dec, formula);
                }
                throw new IllegalArgumentException("Don't know what to do with quantifier " + form.op() + " in " + form);
            }
		} else if (form.op() instanceof de.uka.ilkd.key.logic.op.Function) {
			List<Expression> argList = new ArrayList<Expression>();
			for (int i = 0; i < form.arity(); i++) {
				argList.add(convertToProgram(form.sub(i), tf));
			}
			return tf.createPredicateTerm(RealLDT.getPredicate((de.uka.ilkd.key.logic.op.Function) form
					.op()), argList);
		} else if (form.op() instanceof Equality) {
			List<Expression> argList = new ArrayList<Expression>();
			for (int i = 0; i < form.arity(); i++) {
				argList.add(convertToProgram(form.sub(i), tf));
			}
			return tf.createPredicateTerm(EqualsImpl.getInstance(), argList);
		}

		throw new IllegalArgumentException("Could not convert Term: " + form
				+ "Operator was: " + form.op() + " of class "
				+ form.op().getClass());
	}
}
