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
package de.uka.ilkd.key.dl.model;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.antlr.runtime.tree.CommonTree;

import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.logic.NamespaceSet;
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
public abstract class TermFactory implements Serializable {
    private static Map<NamespaceSet, TermFactory> instances = new WeakHashMap<NamespaceSet, TermFactory>();

    /**
     * Get a cached instance of TermFactory by namespacesset.
     * 
     * @param factoryClass
     *                the class of for the TermFactory to use
     * @param namespaces
     *                the current namespaces
     * @return a new or cached instance of TermFactory
     * @throws InvocationTargetException
     *                 if there is a problem with the factoryClass
     * @throws IllegalAccessException
     *                 if there is a problem with the factoryClass
     * @throws InstantiationException
     *                 if there is a problem with the factoryClass
     * @throws NoSuchMethodException
     *                 if there is a problem with the factoryClass
     */
    public synchronized static TermFactory getTermFactory(
            Class<? extends TermFactory> factoryClass, NamespaceSet namespaces)
            throws InvocationTargetException, IllegalAccessException,
            InstantiationException, NoSuchMethodException {
        TermFactory result = instances.get(namespaces);
        if (result == null) {
            Constructor<? extends TermFactory> ctor = factoryClass
                    .getConstructor(NamespaceSet.class);
            result = ctor.newInstance(namespaces);
            instances.put(namespaces, result);
        }
        return result;
    }

    private NamespaceSet namespaces;

    /**
     * Creates a new TermFactory with a given namespaceset
     * 
     * @param namespaces
     *                the namespaces used to lookup functions, predicates etc
     *                that were previously defined
     */
    protected TermFactory(NamespaceSet namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Get namespaces.
     * 
     * @return namespaces as NamespaceSet.
     */
    public NamespaceSet getNamespaces() {
        return namespaces;
    }

    /***************************************************************************
     * Abstract methods
     **************************************************************************/

    /**
     * Creates a new model object for a differential equation system
     * 
     * @param content
     *                the content of the system
     * @return the new model object for the system
     */
    public abstract DiffSystem createDiffSystem(List<Formula> content);

    /**
     * Creates a predicate term with a predicate with arity 2.
     * 
     * @param bop
     *                the predicate to use
     * @param expr
     *                the first argument
     * @param expr2
     *                the second argument
     * @return the formula representing the predicate
     */
    public abstract PredicateTerm createBinaryRelation(Predicate bop,
            Expression expr, Expression expr2);

    /**
     * Creates a function term with a function with arity 2.
     * 
     * @param bop
     *                the function to use
     * @param expr
     *                the first argument
     * @param expr2
     *                the second argument
     * @return the formula representing the function
     */
    public abstract FunctionTerm createBinaryRelation(Function bop,
            Expression expr, Expression expr2);

    /**
     * Creates a new constant symbol if neccessary or returns an already
     * existing one.
     * 
     * @param t
     *                the constant to represent
     * @return a constant symbol
     */
    public abstract Constant createConstant(CommonTree t);

    /**
     * Creates a new negative constant symbol if neccessary or returns an
     * already existing one.
     * 
     * @param t
     *                the constant to represent
     * @return a constant symbol
     */
    public abstract Constant createNegativeConstant(CommonTree t);

    /**
     * Creates a new symbol with a given name. This method uses the caching
     * function supplied by the Variable and Function implementations to assert
     * that there is only one object per name.
     * 
     * @param t
     *                the name of the symbol
     * @param params
     *                the parameter of the function if its a function
     * @return an object representing the given symbol
     * @throws RuntimeException
     *                 if there were parameters for a variable
     */
    public abstract AtomicTerm createAtomicTerm(CommonTree t,
            List<Expression> params);

    /**
     * Creates a new symbol with a given name. This method uses the caching
     * function supplied by the Variable and Function implementations to assert
     * that there is only one object per name.
     * 
     * @param t
     *                the name of the symbol
     * @param params
     *                the parameter of the function if its a function
     * @return an object representing the given symbol
     * @throws RuntimeException
     *                 if there were parameters for a variable
     */
    public abstract AtomicTerm createAtomicTerm(CommonTree t);

    /**
     * Creates a new term representing a predicate and its parameters
     * 
     * @param f
     *                the predicate
     * @param params
     *                the parameters of the predicate
     * @return the predicate term
     */
    public abstract PredicateTerm createPredicateTerm(CommonTree t,
            List<Expression> params);

    /**
     * Creates a new term representing a predicate and its parameters
     * 
     * @param t
     *                the predicate
     * @return the predicate term
     */
    public abstract PredicateTerm createPredicateTerm(CommonTree t);

    /**
     * Creates a new term representing a function and its parameters
     * 
     * @param f
     *                the function
     * @param params
     *                the parameters of the function
     * @return the function term
     */
    public abstract FunctionTerm createFunctionTerm(CommonTree t,
            List<Expression> params);

    /**
     * Creates a new term representing a function and its parameters
     * 
     * @param f
     *                the function
     * @param params
     *                the parameters of the function
     * @return the function term
     */
    public abstract FunctionTerm createFunctionTerm(Function f,
            List<Expression> params);

    /**
     * Creates a new predicate for the given name.
     * 
     * @param t
     *                the name of the predicate
     * @return a new or cached predicate
     */
    public abstract FreePredicate createPredicate(CommonTree t);

    /**
     * Raises the order of the derivative.
     * 
     * @param pe
     *                the derivative to alternate
     * @return the derivative itself
     */
    public abstract Dot raiseDotCount(Dot pe);

    /**
     * Creates a new object representing a derivative operator. The operator is
     * used to represent the n'th derivative by storing the n.
     * 
     * @param t
     *                the variable that is derivated.
     * @return an object representing the derivative
     */
    public abstract Dot createDot(CommonTree t, List<Expression> args);
    
    public abstract Dot createDot(int degree, CommonTree t, List<Expression> args);

    public abstract Dot schemaCreateDot(CommonTree t);

    public abstract Dot schemaCreateDot(CommonTree t, int order);
    
    /**
     * Creates a program variable if necessary or returns a cached one.
     * 
     * @param name
     *                the name of the variable
     * @return the new or cached program variable
     */
    public abstract ProgramVariable createProgramVariable(String name);

    /**
     * Creates a new assign statement
     * 
     * @param t
     *                the variable assigned
     * @param e
     *                the value of the variable
     * @return the assign statement
     */
    public abstract Assign createAssign(CommonTree t, List<Expression> args, Expression e);
    
    public abstract Assign createAssign(ProgramElement left, ProgramElement right);

    /**
     * Creates an assignment to a schema variable
     * 
     * @param t
     *                the schema variable
     * @param e
     *                the expression that is being assigned
     * @return the assign statement
     */
    public abstract Assign createAssignToSchemaVariable(CommonTree t,
            Expression e);

    /**
     * Creates a new random assign statement
     * 
     * @param t
     *                the variable assigned
     * @return the assign statement
     */
    public abstract RandomAssign createRandomAssign(CommonTree t, List<Expression> args);

    /**
     * Creates a new random assign statement to a schema variable
     * 
     * @param t
     *                the variable assigned
     * @return the assign statement
     */
    public abstract RandomAssign createRandomAssignToSchemaVariable(CommonTree t);

    /**
     * Creates a new greater comparsion
     * 
     * @return an object representing a greater-as
     */
    public abstract Greater createGreater();

    /**
     * Creates a new greater equals comparsion
     * 
     * @return an comparsion object representing a greater-equals
     */
    public abstract GreaterEquals createGreaterEquals();

    /**
     * Creates a new equals comparsion
     * 
     * @return an comparsion object representing an equals
     */
    public abstract Equals createEquals();

    /**
     * Creates a new less equals comparsion
     * 
     * @return an comparsion object representing a less equals
     */
    public abstract LessEquals createLessEquals();

    /**
     * Creates a new less comparsion
     * 
     * @return an comparsion object representing a less
     */
    public abstract Less createLess();

    /**
     * Creates a new unequals comparsion
     * 
     * @return an comparsion object representing an unequals
     */
    public abstract Unequals createUnequals();

    /**
     * Creates a new representation for a variable declaration
     * 
     * @return a new representation for a variable declaration
     */
    public abstract VariableDeclaration createVariableDeclaration(
            CommonTree type, List<CommonTree> decls, Map<CommonTree, List<CommonTree>> argsorts, boolean programVariable);

    /**
     * Creates a new representation for a variable declaration
     * 
     * @return a new representation for a variable declaration
     */
    public abstract VariableDeclaration createVariableDeclaration(
    		VariableType type, List<String> var, boolean programVariable, boolean fresh);
   

    /**
     * Creates a sequential composition of two programs.
     * 
     * @param pe
     *                the first program
     * @param st
     *                the second program
     * @return the sequential composition of the two programs.
     */
    public abstract Chop createChop(DLProgram pe, DLProgram st);

    /**
     * Creates a parallel composition of two programs.
     * 
     * @param pe
     *                the first program
     * @param st
     *                the second program
     * @return the parallel composition of the two programs.
     */
    public abstract Parallel createParallel(DLProgram pe, DLProgram st);

    /**
     * Creates a non-deterministic repetition of the given program.
     * 
     * @param st
     *                the program to repeat.
     * @return a non-deterministic repetition of the given program.
     */
    public abstract Star createStar(DLProgram st);

    /**
     * Creates a non-deterministic choice between two programs.
     * 
     * @param pe
     *                the first program
     * @param st
     *                the second program
     * @return a non-deterministic choice between two programs.
     */
    public abstract Choice createChoice(DLProgram pe, DLProgram st);

    /**
     * Creates the disjunction of two formulas.
     * 
     * @param fe
     *                the first formula
     * @param f
     *                the second formula
     * @return the disjuntion of the two formulas
     */
    public abstract Or createOr(Formula fe, Formula f);

    /**
     * Creates the conjunction of two formulas.
     * 
     * @param fe
     *                the first formula
     * @param f
     *                the second formula
     * @return the conjuntion of the two formulas
     */
    public abstract And createAnd(Formula fe, Formula f);

    /**
     * Creates the implication of two formulas.
     * 
     * @param fe
     *                the first formula
     * @param f
     *                the second formula
     * @return the implication of the two formulas
     */
    public abstract Implies createImpl(Formula fe, Formula f);

    /**
     * Creates the equivalance of two formulas.
     * 
     * @param fe
     *                the first formula
     * @param f
     *                the second formula
     * @return the equivalance of the two formulas
     */
    public abstract Biimplies createBiImpl(Formula fe, Formula f);

    /**
     * Creates the negation of a given formula
     * 
     * @param frm
     *                the formula to negate
     * @return the negation of the formula
     */
    public abstract Not createNot(Formula frm);

    /**
     * Creates the addition of the given expressions.
     * 
     * @param pe
     *                the first expression
     * @param b
     *                the second expression
     * @return the addition of the expressions
     */
    public abstract Expression createPlus(Expression pe, Expression b);

    /**
     * Creates the subtration of the given expressions.
     * 
     * @param pe
     *                the first expression
     * @param b
     *                the second expression
     * @return the subtration of the expressions
     */
    public abstract Expression createMinus(Expression pe, Expression b);

    /**
     * Creates the negation of the given expressions.
     * 
     * @param pe
     *                the expression to negate
     * @return the negation of the expression
     */
    public abstract Expression createMinusSign(Expression pe);

    /**
     * Creates the multiplication of the given expressions.
     * 
     * @param pe
     *                the first expression
     * @param b
     *                the second expression
     * @return the multiplication of the expressions
     */
    public abstract Expression createMult(Expression pe, Expression b);

    /**
     * Creates the division of the given expressions.
     * 
     * @param pe
     *                the first expression
     * @param b
     *                the second expression
     * @return the division of the expressions
     */
    public abstract Expression createDiv(Expression pe, Expression b);

    /**
     * Creates the exponential function of the given expressions.
     * 
     * @param pe
     *                the first expression
     * @param b
     *                the second expression
     * @return the exponential function of the expressions
     */
    public abstract Expression createExp(Expression pe, Expression b);

    /**
     * Create a schema variable for a DLProgram
     */
    public abstract DLProgram schemaProgramVariable(CommonTree t);

    /**
     * Create a schema variable for a Formula
     */
    public abstract Formula schemaTermVariable(CommonTree t, boolean diffAllowed);

    /**
     * Create a schema variable for a Comparsion
     */
    public abstract Comparsion schemaBrelVariable(CommonTree t);

    /**
     * Create a schema variable for a Expression
     */
    public abstract Expression schemaExpressionVariable(CommonTree t);

    /**
     * Create a new formula node
     * 
     * @param frm
     *                the formula represented by this node
     * @return a node representing the given formula
     */
    public abstract Quest createQuest(Formula frm);

    /**
     * Creates a predicate term with the given predicate and parameters
     * 
     * @param pred
     *                the operator to use
     * @param children
     *                the children of the predicate
     * @return a new predicate term
     */
    public abstract PredicateTerm createPredicateTerm(Predicate pred,
            List<Expression> children);

    /**
     * Creates a predicate term with the given predicate and parameters
     * 
     * @param pred
     *                the operator to use
     * @param children
     *                the children of the predicate
     * @return a new predicate term
     */
    public abstract PredicateTerm createPredicateTerm(Predicate pred,
            Expression... children);
    
    /**
     * Creates an assignment between two expressions.
     * 
     * @param expression
     *                the left side of the assignment
     * @param expression2
     *                the right side of the assignment
     * @return a DLProgram representing the assignment
     */
    public abstract Assign createAssign(ProgramVariable expression,
            Expression expression2);

    /**
     * Create a constant for the given value.
     * 
     * @param d
     *                the value of the constant
     * 
     * @return a constant for the given value.
     */
    public abstract Constant createConstant(BigDecimal d);

    /**
     * Creates a function term with the given predicate and parameters
     * 
     * @param name
     *                the operator to use
     * @param args
     *                the children of the function
     * @return a new function term
     */
    public abstract FunctionTerm createFunctionTerm(String name,
            List<Expression> args);

    /**
     * Creates a logical variable for the given name
     * 
     * @param name
     *                the name of the variable
     * @return a logical variable for the given name
     */
    public abstract LogicalVariable createLogicalVariable(String name);

    /**
     * Create an if statement
     * 
     * @param expr
     *                the condition to be evaluated
     * @param then
     *                the program that is to be executed if the condition is
     *                true
     * @param else_
     *                the program that is to be executed if the condition is
     *                false, or null
     * @return the representation of the if statement
     */
    public abstract IfStatement createIf(Formula expr, DLProgram then,
            DLProgram else_);
    
    /**
     * Creates a meta variable if necessary or returns a cached one.
     * 
     * @param name
     *            the name of the variable
     * @return the new or cached meta variable
     */
    public abstract MetaVariable createMetaVariable(String name);
    
    public abstract Forall createForall(VariableDeclaration dec, Formula form);
    
    public abstract Exists createExists(VariableDeclaration dec, Formula form);
    
    public abstract Diamond createDiamond(DLProgram program, Formula post);
    
    public abstract Box createBox(DLProgram program, Formula post);

	/**
	 * @param type
	 * @param decls
	 * @param programVariable
	 * @param fresh
	 * @return
	 */
	public abstract VariableDeclaration createVariableDeclaration(Sort type,
			List<Variable> decls);

    /**
     * @param convert
     * @param order
     * @return
     */
    public abstract Dot createDot(DLProgramElement convert, int order);
    
    public abstract Quantified createQuantified(VariableDeclaration decl, DLProgram statement);
    
    public abstract Quantified schemaCreateQuantified(CommonTree decl, DLProgram statement);
    
	public abstract VariableDeclaration schemaCreateVariableDeclaration(CommonTree type, CommonTree sv);
    public abstract IfExpr createIfExpr(Formula f, Expression thenExpr, Expression elseExpr);

    // unbind the variables that were just added in the quantifier scope
    public abstract void unbind();
}
