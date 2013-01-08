package de.uka.ilkd.key.dl.rules.metaconstruct.test;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import de.uka.ilkd.key.collection.ImmutableSet;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.Constant;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.Expression;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.LogicalVariable;
import de.uka.ilkd.key.dl.model.PredicateTerm;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.Variable;
import de.uka.ilkd.key.dl.model.impl.TermFactoryImpl;
import de.uka.ilkd.key.dl.rules.metaconstruct.DiffNormalize;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.op.AbstractMetaOperator;
import de.uka.ilkd.key.logic.sort.AbstractNonCollectionSort;
import de.uka.ilkd.key.logic.sort.ProgramSVSort;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

public class TestHasOnlyOneDot {

	private static final TermFactory tf;
	private static final Services s;
	private static final DiffNormalize normalizeInstance;
	private static final SVInstantiations svInstance;
	private static final ProgramSVSort svSortNormalized;
	private static final ProgramSVSort svSortOnlyOrdinary;
	private static final Method flatExpression;
	private static final Method isNormalizeable;
	private static final Method replaceDottedVariables;
	private static final Method transformEqualsWithOneDot;
	
	static {
		try {
			s = new Services();
			s.getNamespaces().sorts().add(new SortR(new Name("R")));
			new RealLDT(s.getNamespaces().sorts(), s.getNamespaces().functions());
			tf = TermFactory.getTermFactory(TermFactoryImpl.class, s.getNamespaces());
			normalizeInstance = AbstractMetaOperator.DIFFNORMALIZE;
			svInstance = SVInstantiations.EMPTY_SVINSTANTIATIONS;
			svSortNormalized = ProgramSVSort.DL_NORMALIZED_DIFF_SYSTEM_SORT_INSTANCE;
			svSortOnlyOrdinary = ProgramSVSort.DL_ORDINARY_AND_NOT_NORMALIZED_DIFF_SYSTEM_SORT_INSTANCE;
			isNormalizeable = DiffNormalize.class.getDeclaredMethod("isNormalizeable", PredicateTerm.class);
			isNormalizeable.setAccessible(true);
			replaceDottedVariables = DiffNormalize.class.getDeclaredMethod("replaceDottedVariables", ProgramElement.class, HashMap.class,TermFactory.class);
			replaceDottedVariables.setAccessible(true);
			transformEqualsWithOneDot = DiffNormalize.class.getDeclaredMethod("transformEqualsWithOneDot", Expression.class, Expression.class, Dot.class, TermFactory.class);
			transformEqualsWithOneDot.setAccessible(true);
			flatExpression = DiffNormalize.class.getDeclaredMethod("flatExpression", Expression.class, TermFactory.class);
			flatExpression.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testFlatExpression() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Dot a = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("a")), 1);
		Dot b = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("b")), 1);
		Constant c0 = tf.createConstant(BigDecimal.valueOf(3));
		Constant c1 = tf.createConstant(BigDecimal.valueOf(4)); //4*a'*(b'+3) + (-a)
		Expression e0 = tf.createPlus(tf.createMult(c1, tf.createMult(a, tf.createPlus(b, c0))), tf.createMinusSign(a));
		Expression expected = tf.createPlus(tf.createPlus(tf.createMult(c1, tf.createMult(a, b)), tf.createMult(c1, tf.createMult(a, c0))), tf.createMult(tf.createConstant(BigDecimal.valueOf(-1)), a));
		assertEquals(expected, flatExpression.invoke(normalizeInstance, e0, tf));
	}
	
	@Test
	public void testEqualsTransformationWithDotInExp() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Dot y = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("y")), 1);
		Constant c0 = tf.createConstant(BigDecimal.valueOf(2));
		Constant c1 = tf.createConstant(BigDecimal.valueOf(3)); 
		Expression e0 = tf.createExp(tf.createPlus(y, c0), c1); //(2+y')^3
		Expression e1 = tf.createConstant(BigDecimal.ZERO); //0
		PredicateTerm pTerm = tf.createPredicateTerm(tf.createEquals(), e0, e1);
		assertFalse((Boolean)isNormalizeable.invoke(normalizeInstance,pTerm));
	}
	
	@Test
	public void testEqualsTransformationWithOneDot() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Dot d = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("d")), 1);
		Constant c0 = tf.createConstant(BigDecimal.valueOf(3));
		Constant c1 = tf.createConstant(BigDecimal.valueOf(4)); //4*a'*(3+3) + (-a')
		Expression e0 = tf.createPlus(tf.createMult(c1, tf.createMult(d, tf.createPlus(c0, c0))), tf.createMinusSign(d));
		Expression e1 = tf.createMinus(c1, d); //4-a'
		PredicateTerm pTerm = tf.createPredicateTerm(tf.createEquals(), e0, e1);
		final Constant ZERO = tf.createConstant(BigDecimal.ZERO);
		Expression aMinusB = tf.createConstant(BigDecimal.valueOf(24));
		Expression gMinusF = tf.createConstant(BigDecimal.valueOf(4));
		//Expected : 24 != 0 & a' = 4/24  |  24 = 0 & a' = 0
		Formula expected = tf.createOr(tf.createAnd(tf.createPredicateTerm(tf.createUnequals(), aMinusB, ZERO), tf.createPredicateTerm(tf.createEquals(), d, tf.createDiv(gMinusF, aMinusB))), tf.createAnd(tf.createPredicateTerm(tf.createEquals(), aMinusB, ZERO), tf.createPredicateTerm(tf.createEquals(), gMinusF, ZERO)));
		assertTrue((Boolean)isNormalizeable.invoke(normalizeInstance, pTerm));
		assertEquals(expected, replaceDottedVariables.invoke(normalizeInstance, pTerm, new HashMap<Variable, LogicalVariable>(), tf));
	}
	
	@Test
	public void testEqualsTransformationWithOneDotAndVariables() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		final Constant ZERO = tf.createConstant(BigDecimal.ZERO);
		Variable x = tf.createLogicalVariable(tf.getNamespaces().getUniqueName("x"));
		Variable y = tf.createLogicalVariable(tf.getNamespaces().getUniqueName("y"));
		Dot d = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("a")), 1);
		Constant c0 = tf.createConstant(BigDecimal.valueOf(3));
		Constant c1 = tf.createConstant(BigDecimal.valueOf(4)); //y*(4x-3a'+y) = 0
		Expression e0 = tf.createMult(y, tf.createPlus(tf.createMinus(tf.createMult(c1,x), tf.createMult(c0, d)), y));
		Expression e1 = ZERO;
		PredicateTerm pTerm = tf.createPredicateTerm(tf.createEquals(), e0, e1);
		assertTrue((Boolean)isNormalizeable.invoke(normalizeInstance, pTerm));
		//Expected : 24 != 0 & a' = 4/24  |  24 = 0 & a' = 0
		Expression aMinusB = tf.createMult(tf.createConstant(BigDecimal.valueOf(-3)), y);
		Expression gMinusF = tf.createPlus(tf.createMult(tf.createMult(tf.createConstant(BigDecimal.valueOf(-1)), y), y), tf.createMult(tf.createMult(tf.createConstant(BigDecimal.valueOf(-4)), y), x));
		Formula expected = tf.createOr(tf.createAnd(tf.createPredicateTerm(tf.createUnequals(), aMinusB, ZERO), tf.createPredicateTerm(tf.createEquals(), d, tf.createDiv(gMinusF, aMinusB))), tf.createAnd(tf.createPredicateTerm(tf.createEquals(), aMinusB, ZERO), tf.createPredicateTerm(tf.createEquals(), gMinusF, ZERO)));
		assertEquals(expected, transformEqualsWithOneDot.invoke(normalizeInstance, e0, e1, d, tf));
	}
	
	@Test
	public void testEqualsTransformationWithOneDotAndVariablesWithExponent() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		final Constant ZERO = tf.createConstant(BigDecimal.ZERO);
		Variable x = tf.createLogicalVariable(tf.getNamespaces().getUniqueName("x"));
		Variable y = tf.createLogicalVariable(tf.getNamespaces().getUniqueName("y"));
		Dot d = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("a")), 1);
		Constant c0 = tf.createConstant(BigDecimal.valueOf(3));
		Constant c1 = tf.createConstant(BigDecimal.valueOf(4)); 
		//4(x^2)y' + y'
		Expression e0 = tf.createPlus(tf.createMult(tf.createMult(c1, tf.createExp(x, tf.createConstant(BigDecimal.valueOf(2)))), d), d);
		Expression e1 = ZERO;
		PredicateTerm pTerm = tf.createPredicateTerm(tf.createEquals(), e0, e1);
		assertTrue((Boolean)isNormalizeable.invoke(normalizeInstance, pTerm));
		//System.out.println(replaceDottedVariables.invoke(normalizeInstance, pTerm, new HashMap<Variable, LogicalVariable>(), tf));
		//Expected : 4x^2+1!=0 & a'= 0/(4x^2+1) | 4x^2+1=0 & a'=0 
	}
	
	/**
	 * Helper class.
	 * 
	 * @author Timo Michelsen
	 */
	protected static class SortR extends AbstractNonCollectionSort {

		// public static final SortR R = new SortR(new Name("R"));

		public SortR(Name name) {
			super(name);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.uka.ilkd.key.logic.sort.AbstractSort#extendsSorts()
		 */
		@Override
		public ImmutableSet<Sort> extendsSorts() {
			// TODO Auto-generated method stub
			return null;
		}

	}
}
