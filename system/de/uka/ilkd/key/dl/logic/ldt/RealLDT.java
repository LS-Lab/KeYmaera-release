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
package de.uka.ilkd.key.dl.logic.ldt;

import java.util.HashMap;
import java.util.Map;

import de.uka.ilkd.key.dl.model.DLTerminalProgramElement;
import de.uka.ilkd.key.dl.model.Div;
import de.uka.ilkd.key.dl.model.Equals;
import de.uka.ilkd.key.dl.model.Exp;
import de.uka.ilkd.key.dl.model.Greater;
import de.uka.ilkd.key.dl.model.GreaterEquals;
import de.uka.ilkd.key.dl.model.Less;
import de.uka.ilkd.key.dl.model.LessEquals;
import de.uka.ilkd.key.dl.model.Minus;
import de.uka.ilkd.key.dl.model.MinusSign;
import de.uka.ilkd.key.dl.model.Mult;
import de.uka.ilkd.key.dl.model.Plus;
import de.uka.ilkd.key.dl.model.Predicate;
import de.uka.ilkd.key.dl.model.Unequals;
import de.uka.ilkd.key.dl.model.impl.DivImpl;
import de.uka.ilkd.key.dl.model.impl.EqualsImpl;
import de.uka.ilkd.key.dl.model.impl.ExpImpl;
import de.uka.ilkd.key.dl.model.impl.GreaterEqualsImpl;
import de.uka.ilkd.key.dl.model.impl.GreaterImpl;
import de.uka.ilkd.key.dl.model.impl.LessEqualsImpl;
import de.uka.ilkd.key.dl.model.impl.LessImpl;
import de.uka.ilkd.key.dl.model.impl.MinusImpl;
import de.uka.ilkd.key.dl.model.impl.MinusSignImpl;
import de.uka.ilkd.key.dl.model.impl.MultImpl;
import de.uka.ilkd.key.dl.model.impl.PlusImpl;
import de.uka.ilkd.key.dl.model.impl.UnequalsImpl;
import de.uka.ilkd.key.java.Expression;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.abstraction.PrimitiveType;
import de.uka.ilkd.key.java.expression.Literal;
import de.uka.ilkd.key.java.expression.Operator;
import de.uka.ilkd.key.java.reference.ExecutionContext;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Namespace;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.ldt.LDT;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.util.ExtList;

/**
 * This LDT is used in KeYmaera to encapsulate the functions related to real
 * numbers
 * 
 * @author jdq
 * @since Aug 29, 2007
 * 
 */
public class RealLDT extends LDT {

	private static final Map<Function, de.uka.ilkd.key.dl.model.Function> KEY_TO_DL_FUNCTION_MAP = new HashMap<Function, de.uka.ilkd.key.dl.model.Function>();
	private static final Map<Function, de.uka.ilkd.key.dl.model.Predicate> KEY_TO_DL_PREDICATE_MAP = new HashMap<Function, de.uka.ilkd.key.dl.model.Predicate>();
	private static final Map<Class<? extends de.uka.ilkd.key.dl.model.Function>, Function> DL_TO_KEY_FUNCTION_MAP = new HashMap<Class<? extends de.uka.ilkd.key.dl.model.Function>, Function>();
	private static final Map<Class<? extends de.uka.ilkd.key.dl.model.Predicate>, Function> DL_TO_KEY_PREDICATE_MAP = new HashMap<Class<? extends de.uka.ilkd.key.dl.model.Predicate>, Function>();
	private static Sort staticSort;

	/**
	 * @param name
	 * @param sorts
	 * @param type
	 */
	public RealLDT(Namespace sorts, Namespace functions) {
		super(new Name("R"), sorts, PrimitiveType.JAVA_DOUBLE);
		RealLDT.staticSort = sort;
		KEY_TO_DL_PREDICATE_MAP.put(
				(Function) functions.lookup(new Name("lt")), LessImpl
						.getInstance());
		KEY_TO_DL_PREDICATE_MAP.put((Function) functions
				.lookup(new Name("leq")), LessEqualsImpl.getInstance());
		KEY_TO_DL_PREDICATE_MAP.put((Function) functions.lookup(new Name(
				"equals")), EqualsImpl.getInstance());
		KEY_TO_DL_PREDICATE_MAP.put((Function) functions
				.lookup(new Name("geq")), GreaterEqualsImpl.getInstance());
		KEY_TO_DL_PREDICATE_MAP.put(
				(Function) functions.lookup(new Name("gt")), GreaterImpl
						.getInstance());
		KEY_TO_DL_PREDICATE_MAP.put((Function) functions
				.lookup(new Name("neq")), UnequalsImpl.getInstance());

		DL_TO_KEY_PREDICATE_MAP.put(Less.class, (Function) functions
				.lookup(new Name("lt")));
		DL_TO_KEY_PREDICATE_MAP.put(LessEquals.class, (Function) functions
				.lookup(new Name("leq")));
		DL_TO_KEY_PREDICATE_MAP.put(Equals.class, (Function) functions
				.lookup(new Name("equals")));
		DL_TO_KEY_PREDICATE_MAP.put(GreaterEquals.class, (Function) functions
				.lookup(new Name("geq")));
		DL_TO_KEY_PREDICATE_MAP.put(Greater.class, (Function) functions
				.lookup(new Name("gt")));
		DL_TO_KEY_PREDICATE_MAP.put(Unequals.class, (Function) functions
				.lookup(new Name("neq")));

		KEY_TO_DL_FUNCTION_MAP.put(
				(Function) functions.lookup(new Name("neg")), MinusSignImpl
						.getInstance());
		KEY_TO_DL_FUNCTION_MAP.put(
				(Function) functions.lookup(new Name("sub")), MinusImpl
						.getInstance());
		KEY_TO_DL_FUNCTION_MAP.put(
				(Function) functions.lookup(new Name("add")), PlusImpl
						.getInstance());
		KEY_TO_DL_FUNCTION_MAP.put(
				(Function) functions.lookup(new Name("mul")), MultImpl
						.getInstance());
		KEY_TO_DL_FUNCTION_MAP.put(
				(Function) functions.lookup(new Name("div")), DivImpl
						.getInstance());
		KEY_TO_DL_FUNCTION_MAP.put(
				(Function) functions.lookup(new Name("exp")), ExpImpl
						.getInstance());

		DL_TO_KEY_FUNCTION_MAP.put(MinusSign.class, (Function) functions
				.lookup(new Name("neg")));
		DL_TO_KEY_FUNCTION_MAP.put(Minus.class, (Function) functions
				.lookup(new Name("sub")));
		DL_TO_KEY_FUNCTION_MAP.put(Plus.class, (Function) functions
				.lookup(new Name("add")));
		DL_TO_KEY_FUNCTION_MAP.put(Mult.class, (Function) functions
				.lookup(new Name("mul")));
		DL_TO_KEY_FUNCTION_MAP.put(Div.class, (Function) functions
				.lookup(new Name("div")));
		DL_TO_KEY_FUNCTION_MAP.put(Exp.class, (Function) functions
				.lookup(new Name("exp")));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.logic.ldt.LDT#getFunctionFor(de.uka.ilkd.key.java.expression.Operator,
	 *      de.uka.ilkd.key.java.Services,
	 *      de.uka.ilkd.key.java.reference.ExecutionContext)
	 */
	/*@Override*/
	public Function getFunctionFor(Operator op, Services serv,
			ExecutionContext ec) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.logic.ldt.LDT#hasLiteralFunction(de.uka.ilkd.key.logic.op.Function)
	 */
	/*@Override*/
	public boolean hasLiteralFunction(Function f) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.logic.ldt.LDT#isResponsible(de.uka.ilkd.key.java.expression.Operator,
	 *      de.uka.ilkd.key.logic.Term[], de.uka.ilkd.key.java.Services,
	 *      de.uka.ilkd.key.java.reference.ExecutionContext)
	 */
	/*@Override*/
	public boolean isResponsible(Operator op, Term[] subs, Services services,
			ExecutionContext ec) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.logic.ldt.LDT#isResponsible(de.uka.ilkd.key.java.expression.Operator,
	 *      de.uka.ilkd.key.logic.Term, de.uka.ilkd.key.logic.Term,
	 *      de.uka.ilkd.key.java.Services,
	 *      de.uka.ilkd.key.java.reference.ExecutionContext)
	 */
	/*@Override*/
	public boolean isResponsible(Operator op, Term left, Term right,
			Services services, ExecutionContext ec) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.logic.ldt.LDT#isResponsible(de.uka.ilkd.key.java.expression.Operator,
	 *      de.uka.ilkd.key.logic.Term, de.uka.ilkd.key.java.Services,
	 *      de.uka.ilkd.key.java.reference.ExecutionContext)
	 */
	/*@Override*/
	public boolean isResponsible(Operator op, Term sub, Services services,
			ExecutionContext ec) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.logic.ldt.LDT#translateLiteral(de.uka.ilkd.key.java.expression.Literal)
	 */
	/*@Override*/
	public Term translateLiteral(Literal lit) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.logic.ldt.LDT#translateTerm(de.uka.ilkd.key.logic.Term,
	 *      de.uka.ilkd.key.util.ExtList)
	 */
	/*@Override*/
	public Expression translateTerm(Term t, ExtList children) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Sort getRealSort() {
		return staticSort;
	}

	public static Function getFunctionFor(
			Class<? extends DLTerminalProgramElement> cla) {
		Function function = DL_TO_KEY_FUNCTION_MAP.get(cla);
		if (function == null) {
			function = DL_TO_KEY_PREDICATE_MAP.get(cla);
		}
		return function;
	}

	public static Predicate getPredicate(Function func) {
		return KEY_TO_DL_PREDICATE_MAP.get(func);
	}

}
