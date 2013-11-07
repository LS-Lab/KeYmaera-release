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
 * 
 */
package de.uka.ilkd.key.dl.model.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.uka.ilkd.key.logic.*;
import org.antlr.runtime.tree.CommonTree;

import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.And;
import de.uka.ilkd.key.dl.model.Assign;
import de.uka.ilkd.key.dl.model.AtomicTerm;
import de.uka.ilkd.key.dl.model.Biimplies;
import de.uka.ilkd.key.dl.model.Box;
import de.uka.ilkd.key.dl.model.Choice;
import de.uka.ilkd.key.dl.model.Chop;
import de.uka.ilkd.key.dl.model.Comparsion;
import de.uka.ilkd.key.dl.model.Constant;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.Diamond;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.Equals;
import de.uka.ilkd.key.dl.model.Exists;
import de.uka.ilkd.key.dl.model.Expression;
import de.uka.ilkd.key.dl.model.Forall;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.FreePredicate;
import de.uka.ilkd.key.dl.model.Function;
import de.uka.ilkd.key.dl.model.FunctionTerm;
import de.uka.ilkd.key.dl.model.Greater;
import de.uka.ilkd.key.dl.model.GreaterEquals;
import de.uka.ilkd.key.dl.model.IfExpr;
import de.uka.ilkd.key.dl.model.IfStatement;
import de.uka.ilkd.key.dl.model.Implies;
import de.uka.ilkd.key.dl.model.Less;
import de.uka.ilkd.key.dl.model.LessEquals;
import de.uka.ilkd.key.dl.model.LogicalVariable;
import de.uka.ilkd.key.dl.model.MetaVariable;
import de.uka.ilkd.key.dl.model.NonRigidFunction;
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
import de.uka.ilkd.key.dl.model.Unequals;
import de.uka.ilkd.key.dl.model.Variable;
import de.uka.ilkd.key.dl.model.VariableDeclaration;
import de.uka.ilkd.key.dl.model.VariableType;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.logic.op.LocationVariable;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.NonRigidFunctionLocation;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.logic.op.ProgramSV;
import de.uka.ilkd.key.logic.op.SchemaVariable;
import de.uka.ilkd.key.logic.sort.Sort;

/**
 * The TermFactory is used by the TreeParser stage of the dL parser. It serves
 * methods to create syntax tree objects, such that only simple code is needed
 * in the semantic actions of the parser.
 * 
 * @author jdq
 * @since 03.01.2007
 * 
 */
public class TermFactoryImpl extends TermFactory {

	/**
	 * Creates a new TermFactory with a given namespaceset
	 * 
	 * @param namespaces
	 *            the namespaces used to lookup functions, predicates etc that
	 *            were previously defined
	 */
	public TermFactoryImpl(NamespaceSet namespaces) {
		super(namespaces);
	}

	/**
	 * Creates a new model object for a differential equation system
	 * 
	 * @param content
	 *            the content of the system
	 * @return the new model object for the system
	 */
	public DiffSystem createDiffSystem(List<Formula> content) {
		return new DiffSystemImpl(content);
	}

	/**
	 * Creates a predicate term with a predicate with arity 2.
	 * 
	 * @param bop
	 *            the predicate to use
	 * @param expr
	 *            the first argument
	 * @param expr2
	 *            the second argument
	 * @return the formula representing the predicate
	 */
	public PredicateTerm createBinaryRelation(Predicate bop, Expression expr,
			Expression expr2) {
		return new PredicateTermImpl(bop, expr, expr2);
	}

	/**
	 * Creates a function term with a function with arity 2.
	 * 
	 * @param bop
	 *            the function to use
	 * @param expr
	 *            the first argument
	 * @param expr2
	 *            the second argument
	 * @return the formula representing the function
	 */
	public FunctionTerm createBinaryRelation(Function bop, Expression expr,
			Expression expr2) {
		return new FunctionTermImpl(bop, expr, expr2);
	}

	/**
	 * Creates a new constant symbol if neccessary or returns an already
	 * existing one.
	 * 
	 * @param t
	 *            the constant to represent
	 * @return a constant symbol
	 */
	public Constant createConstant(CommonTree t) {
		return createConstant(new BigDecimal(t.getText()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createNegativeConstant(org.antlr.runtime.tree.CommonTree)
	 */
	/*@Override*/
	public Constant createNegativeConstant(CommonTree t) {
		return ConstantImpl.getConstant(new BigDecimal("-" + t.getText()));
	}

	/**
	 * Creates a new symbol with a given name. This method uses the caching
	 * function supplied by the Variable and Function implementations to assert
	 * that there is only one object per name.
	 * 
	 * @param t
	 *            the name of the symbol
	 * @param params
	 *            the parameter of the function if its a function
	 * @return an object representing the given symbol
	 * @throws RuntimeException
	 *             if there were parameters for a variable
	 */
	public AtomicTerm createAtomicTerm(CommonTree t, List<Expression> params) {
		Name name = new Name(t.getText());
		if (getNamespaces().functions().lookup(name) != null) {
			return createFunctionTerm(t, params);
		} else if (params.size() == 0) {
			Named logicVar = getNamespaces().variables().lookup(name);
			if(logicVar == null && t.getText().startsWith("$")) {
				logicVar = new LogicVariable(name, RealLDT.getRealSort());
				getNamespaces().variables().add(logicVar);
			}
			if (logicVar != null) {
				assert getNamespaces().programVariables().lookup(logicVar.name()) == null : "Namespace lookup for " + logicVar.name() + " succeeds in program variables " + getNamespaces().programVariables();
				if (logicVar instanceof LogicVariable) {
					return LogicalVariableImpl.getLogicalVariable(logicVar.name());
				} else if (logicVar instanceof Metavariable) {
					return createMetaVariable(logicVar.name().toString());
				} else {
					throw new IllegalArgumentException(
							"Dont know what to do with variable " + logicVar);
				}
			} else {
				return createProgramVariable(t.getText());
			}
		} else {
		  //@todo improve type of exception. This should not really just be a runtime exception. It's a semantic exception.
			throw new RuntimeException(
			  //@todo print round parentheses f(x,y) instead of square parentheses f[x,y] which the default list print does.
					"Undeclared function \"" + t.getText() + "\" with parameters " + params + "\n"
							+ "Parameters should only be passed to function symbols, not as: " + t.getText() + params);
		}
	}

	/**
	 * Creates a new symbol with a given name. This method uses the caching
	 * function supplied by the Variable and Function implementations to assert
	 * that there is only one object per name.
	 * 
	 * @param t
	 *            the name of the symbol
	 * @return an object representing the given symbol
	 */
	public AtomicTerm createAtomicTerm(CommonTree t) {
		return createAtomicTerm(t, Collections.EMPTY_LIST);
	}

	/**
	 * Creates a new term representing a predicate and its parameters
	 * 
	 * @param t
	 *            the predicate
	 * @param params
	 *            the parameters of the predicate
	 * @return the predicate term
	 */
	public PredicateTerm createPredicateTerm(CommonTree t,
			List<Expression> params) {
		Predicate p = createPredicate(t);
		return createPredicateTerm(p, params);
	}

	/**
	 * Creates a new term representing a predicate and its parameters
	 * 
	 * @param t
	 *            the predicate
	 * @return the predicate term
	 */
	public PredicateTerm createPredicateTerm(CommonTree t) {
		return createPredicateTerm(t, Collections.EMPTY_LIST);
	}

	/**
	 * Creates a new term representing a function and its parameters
	 * 
	 * @param f
	 *            the function
	 * @param params
	 *            the parameters of the function
	 * @return the function term
	 */
	public FunctionTerm createFunctionTerm(CommonTree t, List<Expression> params) {
	    de.uka.ilkd.key.logic.op.Function f = (de.uka.ilkd.key.logic.op.Function) getNamespaces().functions().lookup(new Name(t.getText()));
        assert f != null : "The function " + t + " has to be declared!";
	    if(f instanceof RigidFunction) {
    		return createFunctionTerm(FreeFunctionImpl.getFunction(t.getText()),
    				params);
	    } else {
	        String[] args = new String[f.arity()];
	        for(int i = 0; i < f.arity(); i++) {
	            args[i] = f.argSort(i).name().toString();
	        }
    		return createFunctionTerm(NonRigidFunctionImpl.getFunction(t.getText(), args, true), params);
	    }
	}

	/**
	 * Creates a new term representing a function and its parameters
	 * 
	 * @param f
	 *            the function
	 * @param params
	 *            the parameters of the function
	 * @return the function term
	 */
	public FunctionTerm createFunctionTerm(Function f, List<Expression> params) {
		return new FunctionTermImpl(f, params.toArray(new Expression[0]));
	}

	/**
	 * Creates a new predicate for the given name.
	 * 
	 * @param t
	 *            the name of the predicate
	 * @return a new or cached predicate
	 */
	public FreePredicate createPredicate(CommonTree t) {
		return FreePredicateImpl.getPredicate(t.getText());
	}

	/**
	 * Raises the order of the derivative.
	 * 
	 * @param pe
	 *            the derivative to alternate
	 * @return the derivative itself
	 */
	public Dot raiseDotCount(Dot pe) {
		if (pe instanceof DotImpl) {
			((DotImpl) pe).increment();
		}
		return pe;
	}

	/**
	 * Creates a new object representing a derivative operator. The operator is
	 * used to represent the n'th derivative by storing the n.
	 * 
	 * @param t
	 *            the variable that is derivated.
	 * @return an object representing the derivative
	 */
	public Dot createDot(CommonTree t, List<Expression> args) {
	    return createDot(1, t, args);
	}
	public Dot createDot(int degree, CommonTree t, List<Expression> args) {
	    if(args == null || args.isEmpty()) {
	        return new DotImpl(degree, createProgramVariable(t.getText()));
	    } else {
			de.uka.ilkd.key.logic.op.NonRigidFunctionLocation fun = (NonRigidFunctionLocation) getNamespaces().functions().lookup(new Name(t.getText()));
			String[] argSorts = new String[fun.arity()];
			int i = 0;
			for(Sort s: fun.argSort()) {
			    argSorts[i++] = s.name().toString();
			}
	        NonRigidFunction f = NonRigidFunctionImpl.getFunction(fun.name(), argSorts, true);
	        FunctionTerm fTerm = createFunctionTerm(f, args);
            return new DotImpl(degree, fTerm);
	    }
	}
	
    public Dot createDot(DLProgramElement convert, int order) {
        return new DotImpl(convert, order);
    }

	public Dot schemaCreateDot(CommonTree t) {
		return new DotImpl(schemaProgramSV(t));
	}

	public Dot schemaCreateDot(CommonTree t, int order) {
		return new DotImpl(schemaProgramSV(t), order);
	}
	/**
	 * Creates a program variable if necessary or returns a cached one.
	 * 
	 * @param name
	 *            the name of the variable
	 * @return the new or cached program variable
	 */
	public ProgramVariable createProgramVariable(String name) {
		if(name.startsWith("$")) {
			return ProgramVariableImpl.getProgramVariable("dollar" + name, true);
		}
		if (getNamespaces().programVariables().lookup(new Name(name)) != null) {
			assert getNamespaces().variables().lookup(new Name(name)) == null;
			return (ProgramVariable) getNamespaces().programVariables().lookup(new Name(name));
//			return ProgramVariableImpl.getProgramVariable(name, true);
		} else {
			throw new IllegalStateException("ProgramVariable " + name
					+ " is not declared.");
		}
	}

	/**
	 * Creates a meta variable if necessary or returns a cached one.
	 * 
	 * @param name
	 *            the name of the variable
	 * @return the new or cached meta variable
	 */
	public MetaVariable createMetaVariable(String name) {
		if (getNamespaces().variables().lookup(new Name(name)) != null) {
			return MetaVariableImpl.getMetaVariable(name, true);
		} else {
			throw new IllegalStateException("MetaVariable " + name
					+ " is not declared.");
		}
	}

	/**
	 * Creates a new assign statement
	 * 
	 * @param t
	 *            the variable assigned
	 * @param e
	 *            the value of the variable
	 * @return the assign statement
	 */
	public Assign createAssign(CommonTree t, List<Expression> args, Expression e) {
	    if(args.isEmpty()) {
	        return new AssignImpl(createProgramVariable(t.getText()), e);
	    } else {
            return new AssignImpl(createFunctionTerm(t, args), e);
	    }
	}
	
	public Assign createAssign(ProgramElement left, ProgramElement right) {
		return new AssignImpl(left, right);
	}

	/**
	 * Creates a new assign statement
	 * 
	 * @param t
	 *            the variable assigned
	 * @param e
	 *            the value of the variable
	 * @return the assign statement
	 */
	public Assign createAssign(ProgramVariable t, Expression e) {
		return new AssignImpl(t, e);
	}
	
	public Assign createAssign(FunctionTerm t, Expression e) {
		return new AssignImpl(t, e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createAssignToSchemaVariable(org.antlr.runtime.tree.CommonTree,
	 *      de.uka.ilkd.key.dl.Expression)
	 */
	public Assign createAssignToSchemaVariable(CommonTree t, Expression e) {
		return new AssignImpl(schemaProgramSV(t), e);
	}

	/**
	 * Creates a new schema variable for the given name
	 */
	private ProgramVariable schemaProgramSV(CommonTree t) {
		ProgramVariable lookup = (ProgramVariable) getNamespaces().variables()
				.lookup(new Name("#" + t.getText()));
		return lookup;
	}

	/**
	 * Creates a new greater comparsion
	 * 
	 * @return an object representing a greater-as
	 */
	public Greater createGreater() {
		return GreaterImpl.getInstance();
	}

	/**
	 * Creates a new greater equals comparsion
	 * 
	 * @return an comparsion object representing a greater-equals
	 */
	public GreaterEquals createGreaterEquals() {
		return GreaterEqualsImpl.getInstance();
	}

	/**
	 * Creates a new equals comparsion
	 * 
	 * @return an comparsion object representing an equals
	 */
	public Equals createEquals() {
		return EqualsImpl.getInstance();
	}

	/**
	 * Creates a new less equals comparsion
	 * 
	 * @return an comparsion object representing a less equals
	 */
	public LessEquals createLessEquals() {
		return LessEqualsImpl.getInstance();
	}

	/**
	 * Creates a new less comparsion
	 * 
	 * @return an comparsion object representing a less
	 */
	public Less createLess() {
		return LessImpl.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createChop(de.uka.ilkd.key.dl.DLProgram,
	 *      de.uka.ilkd.key.dl.DLProgram)
	 */
	public Chop createChop(DLProgram pe, DLProgram st) {
		return new ChopImpl(pe, st);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createStar(de.uka.ilkd.key.dl.DLProgram)
	 */
	public Star createStar(DLProgram st) {
		return new StarImpl(st);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createChoice(de.uka.ilkd.key.dl.DLProgram,
	 *      de.uka.ilkd.key.dl.DLProgram)
	 */
	public Choice createChoice(DLProgram pe, DLProgram st) {
		return new ChoiceImpl(pe, st);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createOr(de.uka.ilkd.key.dl.Formula,
	 *      de.uka.ilkd.key.dl.Formula)
	 */
	public Or createOr(Formula fe, Formula f) {
		return new OrImpl(fe, f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createAnd(de.uka.ilkd.key.dl.Formula,
	 *      de.uka.ilkd.key.dl.Formula)
	 */
	public And createAnd(Formula fe, Formula f) {
		return new AndImpl(fe, f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createImpl(de.uka.ilkd.key.dl.Formula,
	 *      de.uka.ilkd.key.dl.Formula)
	 */
	public Implies createImpl(Formula fe, Formula f) {
		return new ImpliesImpl(fe, f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createBiImpl(de.uka.ilkd.key.dl.Formula,
	 *      de.uka.ilkd.key.dl.Formula)
	 */
	public Biimplies createBiImpl(Formula fe, Formula f) {
		return new BiimpliesImpl(fe, f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createNot(de.uka.ilkd.key.dl.Formula)
	 */
	public Not createNot(Formula frm) {
		return new NotImpl(frm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createPlus(de.uka.ilkd.key.dl.Expression,
	 *      de.uka.ilkd.key.dl.Expression)
	 */
	public Expression createPlus(Expression pe, Expression b) {
		return new FunctionTermImpl(PlusImpl.getInstance(), pe, b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createMinus(de.uka.ilkd.key.dl.Expression,
	 *      de.uka.ilkd.key.dl.Expression)
	 */
	public Expression createMinus(Expression pe, Expression b) {
		return new FunctionTermImpl(MinusImpl.getInstance(), pe, b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createMult(de.uka.ilkd.key.dl.Expression,
	 *      de.uka.ilkd.key.dl.Expression)
	 */
	public Expression createMult(Expression pe, Expression b) {
		return new FunctionTermImpl(MultImpl.getInstance(), pe, b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createDiv(de.uka.ilkd.key.dl.Expression,
	 *      de.uka.ilkd.key.dl.Expression)
	 */
	public Expression createDiv(Expression pe, Expression b) {
		return new FunctionTermImpl(DivImpl.getInstance(), pe, b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createExp(de.uka.ilkd.key.dl.Expression,
	 *      de.uka.ilkd.key.dl.Expression)
	 */
	public Expression createExp(Expression pe, Expression b) {
		return new FunctionTermImpl(ExpImpl.getInstance(), pe, b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#schemaProgramVariable(org.antlr.runtime.tree.CommonTree)
	 */
	public DLProgram schemaProgramVariable(CommonTree t) {
		return (DLProgram) getNamespaces().variables().lookup(
				new Name("#" + t.getText()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#schemaTermVariable(org.antlr.runtime.tree.CommonTree,
	 *      boolean)
	 */
	public Formula schemaTermVariable(CommonTree t, boolean diffAllowed) {
		return (Formula) getNamespaces().variables().lookup(
				new Name("#" + t.getText()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#schemaBrelVariable(org.antlr.runtime.tree.CommonTree)
	 */
	public Comparsion schemaBrelVariable(CommonTree t) {
		return (Comparsion) getNamespaces().variables().lookup(
				new Name("#" + t.getText()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#schemaExpressionVariable(org.antlr.runtime.tree.CommonTree)
	 */
	public Expression schemaExpressionVariable(CommonTree t) {
		return (Expression) getNamespaces().variables().lookup(
				new Name("#" + t.getText()));
	}

	/**
	 * Create a new formula node
	 * 
	 * @param frm
	 *            the formula represented by this node
	 * @return a node representing the given formula
	 */
	public Quest createQuest(Formula frm) {
		return new QuestImpl(frm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createPredicateTerm(de.uka.ilkd.key.dl.Predicate,
	 *      java.util.List)
	 */
	/*@Override*/
	public PredicateTerm createPredicateTerm(Predicate pred,
			List<Expression> children) {
		return new PredicateTermImpl(pred, children.toArray(new Expression[0]));
	}
	
	@Override
	public PredicateTerm createPredicateTerm(Predicate pred, Expression... children) {
		return new PredicateTermImpl(pred, children);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createConstant(java.math.BigDecimal)
	 */
	/*@Override*/
	public Constant createConstant(BigDecimal d) {
        String n = d.stripTrailingZeros().toPlainString();
        // bugfix for 0.0 and so on (will be fixed in BigDecimal in Java 8
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6480539
        while(n.contains(".") && (n.endsWith("0") || n.endsWith("."))) {
            n = n.substring(0, n.length() - 1);
        }
        assert new BigDecimal(n).compareTo(d) == 0 : "Stripping trailing zeros should not change the value of a number " + d + " != " + n;
		return ConstantImpl.getConstant(new BigDecimal(n));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createFunctionTerm(java.lang.String,
	 *      java.util.List)
	 */
	/*@Override*/
	public FunctionTerm createFunctionTerm(String name, List<Expression> args) {
		Function f = FreeFunctionImpl.getFunction(name);
		return createFunctionTerm(f, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createLogicalVariable(java.lang.String)
	 */
	/*@Override*/
	public LogicalVariable createLogicalVariable(String name) {
		return LogicalVariableImpl.getLogicalVariable(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createMinusSign(de.uka.ilkd.key.dl.Expression)
	 */
	/*@Override*/
	public Expression createMinusSign(Expression pe) {
		return new FunctionTermImpl(MinusSignImpl.getInstance(), pe);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createRandomAssign(org.antlr.runtime.tree.CommonTree)
	 */
	/*@Override*/
	public RandomAssign createRandomAssign(CommonTree t, List<Expression> args) {
	    if(args.isEmpty()) {
	        return new RandomAssignImpl(createProgramVariable(t.getText()));
	    } else {
			de.uka.ilkd.key.logic.op.NonRigidFunctionLocation fun = (NonRigidFunctionLocation) getNamespaces().functions().lookup(new Name(t.getText()));
			String[] argSorts = new String[fun.arity()];
			int i = 0;
			for(Sort s: fun.argSort()) {
			    argSorts[i++] = s.name().toString();
			}
	        NonRigidFunction f = NonRigidFunctionImpl.getFunction(fun.name(), argSorts, true);
	        FunctionTerm fTerm = createFunctionTerm(f, args);
	        return new RandomAssignImpl(fTerm);
	    }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createRandomAssignToSchemaVariable(org.antlr.runtime.tree.CommonTree)
	 */
	/*@Override*/
	public RandomAssign createRandomAssignToSchemaVariable(CommonTree t) {
		return new RandomAssignImpl(schemaProgramSV(t));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createParallel(de.uka.ilkd.key.dl.DLProgram,
	 *      de.uka.ilkd.key.dl.DLProgram)
	 */
	/*@Override*/
	public Parallel createParallel(DLProgram pe, DLProgram st) {
		return new ParallelImpl(pe, st);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createUnequals()
	 */
	/*@Override*/
	public Unequals createUnequals() {
		return UnequalsImpl.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createVariableDeclaration(org.antlr.runtime.tree.CommonTree,
	 *      org.antlr.runtime.tree.CommonTree)
	 */
	/*@Override*/ public VariableDeclaration createVariableDeclaration(CommonTree type,
			List<CommonTree> decls, Map<CommonTree, List<CommonTree>> argsorts, boolean programVariable) {
		List<DLProgramElement> variables = new ArrayList<DLProgramElement>();
        if(!programVariable) {
            // introduce a new programVariable namespace to later fall back to the parent
            getNamespaces().setVariables(new Namespace(getNamespaces().variables()));
        }
		for (CommonTree var : decls) {
			if (programVariable) {
			    List<CommonTree> args = argsorts.get(var); 
			    if(args == null || args.isEmpty()) {
			        // we have declared a program variable
    				assert getNamespaces().variables().lookup(
    						new Name(var.getText())) == null : "newly declared program variable " + var + " not yet in variables namespace at " + decls;
    				// && getNamespaces().programVariables().lookup(
    				// new Name(var.getText())) == null;
    				if (getNamespaces().programVariables().lookup(
    						new Name(var.getText())) == null) {
    				    Sort sort = (Sort) getNamespaces().sorts().lookup(new Name(type.getText()));
    				    assert sort != null : "variable sort " + type.getText() + " should be known!";
    					getNamespaces().programVariables().addSafely(
    							new LocationVariable(new ProgramElementName(var
    									.getText()), sort));
    				}
//					variables.add(ProgramVariableImpl.getProgramVariable(var
//						.getText(), true));
					variables.add((Variable) getNamespaces().programVariables().lookup(
                        new Name(var.getText())));
			    } else {
			        // we are declaring a non-rigid function symbol
                    if (getNamespaces().functions().lookup(
                            new Name(var.getText())) == null) {
                        Sort sort = (Sort) getNamespaces().sorts().lookup(
                                new Name(type.getText()));
                        assert sort != null : "variable sort " + type.getText()
                                + " should be known!";
                        Sort[] argSorts = new Sort[args.size()];
                        for(int i = 0; i < args.size(); i++) {
                            argSorts[i] = (Sort) getNamespaces().sorts().lookup(
                                new Name(args.get(i).getText()));
                            assert argSorts[i] != null : "argument sort " + type.getText()
                                + " should be known!";
                        }
                        getNamespaces()
                                .functions()
                                .addSafely(
                                        new de.uka.ilkd.key.logic.op.NonRigidFunctionLocation(
                                                new Name(var.getText()), sort,
                                                argSorts, true));
                    }
                    String[] sorts = new String[args.size()];
                    for(int i = 0; i < args.size(); i++) {
                        sorts[i] = args.get(i).getText();
                    }
                    variables.add(NonRigidFunctionImpl.getFunction(var.getText(), sorts, true));
			    }
			} else {
				assert getNamespaces().programVariables().lookup(
						new Name(var.getText())) == null : "newly declared non-program variable " + var + " not yet in program variables namespace at " + decls;
				if (getNamespaces().variables().lookup(new Name(var.getText())) == null) {
				    Sort sort = (Sort) getNamespaces().sorts().lookup(new Name(type.getText()));
				    assert sort != null : "variable sort " + type.getText() + " should be known!";
                    // add a new namespace here
					getNamespaces().variables().addSafely(
							new LogicVariable(new Name(var.getText()), sort));
				}
				variables.add(LogicalVariableImpl.getLogicalVariable(var
						.getText()));
			}
		}
		return new VariableDeclarationImpl(VariableTypeImpl
				.getVariableType(type.getText()), variables);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createVariableDeclaration(org.antlr.runtime.tree.CommonTree,
	 *      org.antlr.runtime.tree.CommonTree)
	 */
	/*@Override*/
	public VariableDeclaration createVariableDeclaration(VariableType type,
			List<String> decls, boolean programVariable, boolean fresh) {
		List<Variable> variables = new ArrayList<Variable>();
		for (String var : decls) {
			if (programVariable) {
				if (fresh) {
					assert getNamespaces().variables().lookup(new Name(var)) == null;
					// && getNamespaces().programVariables().lookup(
					// new Name(var.getText())) == null;
					if (getNamespaces().programVariables()
							.lookup(new Name(var)) == null) {
						getNamespaces().programVariables().addSafely(
								new LocationVariable(
										new ProgramElementName(var), RealLDT
												.getRealSort()));
					}
				}
//				variables
//						.add(ProgramVariableImpl.getProgramVariable(var, true));
				variables.add((Variable) getNamespaces().programVariables().lookup(
                        new Name(var)));
			} else {
				if (fresh) {
					assert getNamespaces().programVariables().lookup(
							new Name(var)) == null;
					if (getNamespaces().variables().lookup(new Name(var)) == null) {
						getNamespaces().variables().addSafely(
								new LogicVariable(new Name(var), RealLDT
										.getRealSort()));
					}
				}
				variables.add(LogicalVariableImpl.getLogicalVariable(var));
			}
		}
		return new VariableDeclarationImpl(type, variables);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createVariableDeclaration(org.antlr.runtime.tree.CommonTree,
	 *      org.antlr.runtime.tree.CommonTree)
	 */
	/*@Override*/
	public VariableDeclaration createVariableDeclaration(Sort type,
			List<Variable> decls) {
		return new VariableDeclarationImpl(VariableTypeImpl
				.getVariableType(type.name()), decls);
	}
	
	public VariableDeclaration schemaCreateVariableDeclaration(CommonTree type,
			CommonTree sv) {
		return new VariableDeclarationImpl(VariableTypeImpl
				.getVariableType(type.getText()), schemaProgramSV(sv));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.TermFactory#createIf(de.uka.ilkd.key.dl.Formula,
	 *      de.uka.ilkd.key.dl.DLProgram, de.uka.ilkd.key.dl.DLProgram)
	 */
	/*@Override*/
	public IfStatement createIf(Formula expr, DLProgram then, DLProgram else_) {
		return new IfStatementImpl(expr, then, else_);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.model.TermFactory#createExists(de.uka.ilkd.key.dl.formulatools.VariableDeclaration,
	 *      de.uka.ilkd.key.dl.model.Formula)
	 */
	/*@Override*/
	public Exists createExists(VariableDeclaration dec, Formula form) {
		return new ExistsImpl(dec, form);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.model.TermFactory#createForall(de.uka.ilkd.key.dl.formulatools.VariableDeclaration,
	 *      de.uka.ilkd.key.dl.model.Formula)
	 */
	/*@Override*/
	public Forall createForall(VariableDeclaration dec, Formula form) {
		return new ForallImpl(dec, form);
	}

    /* (non-Javadoc)
     * @see de.uka.ilkd.key.dl.model.TermFactory#createDiamond(de.uka.ilkd.key.dl.model.DLProgram, de.uka.ilkd.key.dl.model.Formula)
     */
    @Override
    public Diamond createDiamond(DLProgram program, Formula post) {
        return new DiamondImpl(program, post);
    }

    /* (non-Javadoc)
     * @see de.uka.ilkd.key.dl.model.TermFactory#createBox(de.uka.ilkd.key.dl.model.DLProgram, de.uka.ilkd.key.dl.model.Formula)
     */
    @Override
    public Box createBox(DLProgram program, Formula post) {
        return new BoxImpl(program, post);
    }
    
    /* (non-Javadoc)
     * @see de.uka.ilkd.key.dl.model.TermFactory#createQuantified(java.lang.String, java.lang.String, de.uka.ilkd.key.dl.model.DLProgram)
     */
    @Override
    public Quantified createQuantified(VariableDeclaration decl,
            DLProgram statement) {
        return new QuantifiedImpl(decl, statement);
    }

    /* (non-Javadoc)
     * @see de.uka.ilkd.key.dl.model.TermFactory#schemaCreateQuantified(de.uka.ilkd.key.logic.op.SchemaVariable, de.uka.ilkd.key.dl.model.DLProgram)
     */
    @Override
    public Quantified schemaCreateQuantified(CommonTree decl,
            DLProgram statement) {
        return new QuantifiedImpl(schemaProgramSV(decl), statement);
    }
    /* (non-Javadoc)
     * @see de.uka.ilkd.key.dl.model.TermFactory#createIfExpr(de.uka.ilkd.key.dl.model.Formula, de.uka.ilkd.key.dl.model.Expression, de.uka.ilkd.key.dl.model.Expression)
     */
    @Override
    public IfExpr createIfExpr(Formula f, Expression thenExpr,
            Expression elseExpr) {
        return new IfExprImpl(f, thenExpr, elseExpr);
    }

    @Override
    public void unbind() {
        getNamespaces().setVariables(getNamespaces().variables().parent());
    }
}
