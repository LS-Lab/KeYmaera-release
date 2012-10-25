package de.uka.ilkd.key.dl.rules.metaconstruct.test;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.collection.ImmutableSet;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.Constant;
import de.uka.ilkd.key.dl.model.DLStatementBlock;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.Expression;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.LogicalVariable;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.Variable;
import de.uka.ilkd.key.dl.model.impl.TermFactoryImpl;
import de.uka.ilkd.key.dl.rules.metaconstruct.DiffNormalize;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.AbstractMetaOperator;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.sort.AbstractNonCollectionSort;
import de.uka.ilkd.key.logic.sort.ProgramSVSort;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * Test-Class for DiffNormalize.java and
 * ProgramSVSort.DLNormalizedDiffSystemSort
 * 
 * @author miroel
 */
public class TestDiffNormalize {

	private static final TermFactory tf;
	private static final Services s;
	private static final DiffNormalize normalizeInstance;
	private static final SVInstantiations svInstance;
	private static final ProgramSVSort svSortNormalized;
	private static final ProgramSVSort svSortOnlyOrdinary;

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
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testIsNotOrdinaryAndNotNormalized() {
		List<Formula> formulasDiff1 = new ArrayList<Formula>();
		List<Expression> childrenDiff1 = new ArrayList<Expression>();
		Dot y = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("y")), 1);
		Constant c = tf.createConstant(BigDecimal.TEN);
		childrenDiff1.add(y);
		childrenDiff1.add(c);
		formulasDiff1.add(tf.createPredicateTerm(tf.createGreater(), childrenDiff1));
		DiffSystem diff1 = tf.createDiffSystem(formulasDiff1);
		assertFalse(isOnlyOrdinary(diff1));
		assertFalse(isNormalized(diff1));
	}

	@Test
	public void testIsNormalizedWithConstant() {
		List<Formula> formulasDiff1 = new ArrayList<Formula>();
		List<Expression> childrenDiff1 = new ArrayList<Expression>();
		Dot y = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("y")), 1);
		Constant c = tf.createConstant(BigDecimal.TEN);
		childrenDiff1.add(y);
		childrenDiff1.add(c);
		formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(), childrenDiff1));
		DiffSystem diff1 = tf.createDiffSystem(formulasDiff1);

		assertFalse(isOnlyOrdinary(diff1));
		assertTrue(isNormalized(diff1)); // y'=10

		Collections.reverse(childrenDiff1);
		formulasDiff1.clear();
		formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(), childrenDiff1));
		diff1 = tf.createDiffSystem(formulasDiff1);

		assertTrue(isOnlyOrdinary(diff1));
		assertFalse(isNormalized(diff1)); // 10=y'
	}

	@Test
	public void testIsNormalizedWithOtherDot() {
		List<Formula> formulasDiff1 = new ArrayList<Formula>();
		List<Expression> childrenDiff1 = new ArrayList<Expression>();
		Dot y = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("y")), 1);
		Dot z = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("z")), 1);

		childrenDiff1.add(y);
		childrenDiff1.add(z);
		formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(), childrenDiff1));
		DiffSystem diff1 = tf.createDiffSystem(formulasDiff1);

		assertTrue(isOnlyOrdinary(diff1));
		assertFalse(isNormalized(diff1)); // y'=z'

		Collections.reverse(childrenDiff1);
		formulasDiff1.clear();
		formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(), childrenDiff1));
		diff1 = tf.createDiffSystem(formulasDiff1);

		assertTrue(isOnlyOrdinary(diff1));
		assertFalse(isNormalized(diff1)); // z'=y'
	}

	// @Test
	// public void testConnectFormulaWithAnd() { //Hier ists noch Fehlerhaft...
	// List<Formula> formulasDiff1 = new ArrayList<Formula>();
	// List<Expression> childrenDiff1 = new ArrayList<Expression>();
	// Dot y =
	// tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("y")),
	// 1);
	// Constant c = tf.createConstant(BigDecimal.TEN);
	// childrenDiff1.add(y);
	// childrenDiff1.add(c);
	// formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(),
	// childrenDiff1));
	// childrenDiff1.clear();
	// childrenDiff1.add(y);
	// childrenDiff1.add(tf.createLogicalVariable("x"));
	// formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(),
	// childrenDiff1));
	// DiffSystem diff1 = tf.createDiffSystem(formulasDiff1);
	//
	// List<Formula> formulasDiff2 = new ArrayList<Formula>();
	// formulasDiff2.add(tf.createAnd(formulasDiff1.get(0),
	// formulasDiff1.get(1)));
	// DiffSystem diff2 = tf.createDiffSystem(formulasDiff2);
	//
	// assertTrue(isOnlyOrdinary(diff1));
	// assertFalse(isNormalized(diff1));
	//
	// DiffSystem diff1AfterNormalization =
	// termToDiffSystem(normalizeInstance.calculate(diffSystemToTerm(diff1),
	// svInstance, s));
	// assertEquals(diff1AfterNormalization, diff2);
	// assertFalse(isOnlyOrdinary(diff1AfterNormalization));
	// assertTrue(isNormalized(diff1AfterNormalization));
	// }

	@Test
	public void testNegation() {
		// Diff1 - DiffSystem to be normalized -y'=0
		List<Formula> formulasDiff1 = new ArrayList<Formula>();
		List<Expression> childrenDiff1 = new ArrayList<Expression>();
		Dot d = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("y")), 1);
		Constant c0 = tf.createConstant(BigDecimal.ZERO);
		childrenDiff1.add(tf.createMinusSign(d));
		childrenDiff1.add(c0);
		formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(), childrenDiff1));
		DiffSystem diff1 = tf.createDiffSystem(formulasDiff1);

		// Diff2 - Expected normalized DiffSystem y'=-0
		List<Formula> formulasDiff2 = new ArrayList<Formula>();
		List<Expression> childrenDiff2 = new ArrayList<Expression>();
		childrenDiff2.add(d);
		childrenDiff2.add(tf.createMinusSign(c0));
		formulasDiff2.add(tf.createPredicateTerm(tf.createEquals(), childrenDiff2));
		DiffSystem diff2 = tf.createDiffSystem(formulasDiff2);
		System.out.println(diff2);
		DiffSystem diff1AfterNormalization = termToDiffSystem(normalizeInstance.calculate(diffSystemToTerm(diff1), svInstance, s));
		DiffSystem diff2AfterNormalization = termToDiffSystem(normalizeInstance.calculate(diffSystemToTerm(diff2), svInstance, s));
		
		assertFalse(isNormalized(diff1));
		assertTrue(isOnlyOrdinary(diff1));
		assertEquals(diff2, diff1AfterNormalization);
		assertFalse(isOnlyOrdinary(diff2));
		assertEquals(diff2, diff2AfterNormalization);
	}

	@Test
	public void testAddandSubtract() {
		// Diff1 - DiffSystem to be normalized 5-(2+y')=1
		List<Formula> formulasDiff1 = new ArrayList<Formula>();
		List<Expression> childrenDiff1 = new ArrayList<Expression>();
		Dot d = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("y")), 1);
		Constant c0 = tf.createConstant(new BigDecimal(5));
		Constant c1 = tf.createConstant(new BigDecimal(2));
		Constant c2 = tf.createConstant(BigDecimal.ONE);

		childrenDiff1.add(tf.createMinus(c0, tf.createPlus(c1, d)));
		childrenDiff1.add(c2);
		formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(), childrenDiff1));
		DiffSystem diff1 = tf.createDiffSystem(formulasDiff1);

		// Diff2 - Expected normalized DiffSystem y'=-(1-5)-2
		List<Formula> formulasDiff2 = new ArrayList<Formula>();
		List<Expression> childrenDiff2 = new ArrayList<Expression>();

		childrenDiff2.add(d);
		childrenDiff2.add(tf.createMinus(tf.createMinusSign(tf.createMinus(c2, c0)), c1));
		formulasDiff2.add(tf.createPredicateTerm(tf.createEquals(), childrenDiff2));
		DiffSystem diff2 = tf.createDiffSystem(formulasDiff2);

		DiffSystem diff1AfterNormalization = termToDiffSystem(normalizeInstance.calculate(diffSystemToTerm(diff1), svInstance, s));
		DiffSystem diff2AfterNormalization = termToDiffSystem(normalizeInstance.calculate(diffSystemToTerm(diff2), svInstance, s));

		assertFalse(isNormalized(diff1));
		assertTrue(isOnlyOrdinary(diff1));
		assertEquals(diff2, diff1AfterNormalization);
		assertTrue(isNormalized(diff2));
		assertFalse(isOnlyOrdinary(diff2));
		assertEquals(diff2, diff2AfterNormalization);
	}

	@Test
	public void testDivisionByZero() {
		// Diff1 - DiffSystem to be normalized 0*y'=1
		List<Formula> formulasDiff1 = new ArrayList<Formula>();
		List<Expression> childrenDiff1 = new ArrayList<Expression>();
		Dot d = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("y")), 1);

		childrenDiff1.add(tf.createMult(tf.createConstant(BigDecimal.ZERO), d));
		childrenDiff1.add(tf.createConstant(BigDecimal.ONE));
		formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(), childrenDiff1));
		DiffSystem diff1 = tf.createDiffSystem(formulasDiff1);

		// Diff2 - Expected normalized DiffSystem E R dy : 0*dy=1 & y'=dy
		List<Formula> formulasDiff2 = new ArrayList<Formula>();
		List<Expression> childrenDiff2_replaceDot = new ArrayList<Expression>();
		List<Expression> childrenDiff2_introduce_equation = new ArrayList<Expression>();
		List<Variable> vars = new ArrayList<Variable>();
		LogicalVariable newVar = tf.createLogicalVariable("dy");

		vars.add(newVar);
		childrenDiff2_replaceDot.add(tf.createMult(tf.createConstant(BigDecimal.ZERO), newVar));
		childrenDiff2_replaceDot.add(tf.createConstant(BigDecimal.ONE));
		childrenDiff2_introduce_equation.add(d);
		childrenDiff2_introduce_equation.add(newVar);
		formulasDiff2.add(tf.createExists(tf.createVariableDeclaration(RealLDT.getRealSort(), vars),
				tf.createAnd(tf.createPredicateTerm(tf.createEquals(), childrenDiff2_replaceDot), tf.createPredicateTerm(tf.createEquals(), childrenDiff2_introduce_equation))));
		DiffSystem diff2 = tf.createDiffSystem(formulasDiff2);

		DiffSystem diff1AfterNormalization = termToDiffSystem(normalizeInstance.calculate(diffSystemToTerm(diff1), svInstance, s));
		DiffSystem diff2AfterNormalization = termToDiffSystem(normalizeInstance.calculate(diffSystemToTerm(diff2), svInstance, s));

		assertFalse(isNormalized(diff1));
		assertTrue(isOnlyOrdinary(diff1));
		assertEquals(diff2, diff1AfterNormalization);
		assertTrue(isNormalized(diff2));
		assertFalse(isOnlyOrdinary(diff2));
		assertEquals(diff2, diff2AfterNormalization);
	}

	@Test
	public void testDivisionByNonZeroConstant() {
		// Diff1 - DiffSystem to be normalized 1*y'=1
		List<Formula> formulasDiff1 = new ArrayList<Formula>();
		List<Expression> childrenDiff1 = new ArrayList<Expression>();
		Dot d = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("y")), 1);

		childrenDiff1.add(tf.createMult(tf.createConstant(BigDecimal.ONE), d));
		childrenDiff1.add(tf.createConstant(BigDecimal.ONE));
		formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(), childrenDiff1));
		DiffSystem diff1 = tf.createDiffSystem(formulasDiff1);

		// Diff2 - Expected normalized DiffSystem y'=1/1
		List<Formula> formulasDiff2 = new ArrayList<Formula>();
		List<Expression> childrenDiff2 = new ArrayList<Expression>();

		childrenDiff2.add(d);
		childrenDiff2.add(tf.createDiv(tf.createConstant(BigDecimal.ONE), tf.createConstant(BigDecimal.ONE)));
		formulasDiff2.add(tf.createPredicateTerm(tf.createEquals(), childrenDiff2));
		DiffSystem diff2 = tf.createDiffSystem(formulasDiff2);

		DiffSystem diff1AfterNormalization = termToDiffSystem(normalizeInstance.calculate(diffSystemToTerm(diff1), svInstance, s));
		DiffSystem diff2AfterNormalization = termToDiffSystem(normalizeInstance.calculate(diffSystemToTerm(diff2), svInstance, s));

		assertFalse(isNormalized(diff1));
		assertTrue(isOnlyOrdinary(diff1));
		assertEquals(diff2, diff1AfterNormalization);
		assertTrue(isNormalized(diff2));
		assertFalse(isOnlyOrdinary(diff2));
		assertEquals(diff2, diff2AfterNormalization);
	}

	@Test
	public void testInverseDotWithConstantAfterEquation() {
		// Diff1 - DiffSystem to be normalized 1/y'=4
		List<Formula> formulasDiff1 = new ArrayList<Formula>();
		List<Expression> childrenDiff1 = new ArrayList<Expression>();
		Dot d = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("y")), 1);
		Constant c0 = tf.createConstant(new BigDecimal(1));
		Constant c1 = tf.createConstant(new BigDecimal(4));

		childrenDiff1.add(tf.createDiv(c0, d));
		childrenDiff1.add(c1);
		formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(), childrenDiff1));
		DiffSystem diff1 = tf.createDiffSystem(formulasDiff1);

		// Diff2 - Expected normalized DiffSystem y'=1/4
		List<Formula> formulasDiff2 = new ArrayList<Formula>();
		List<Expression> childrenDiff2 = new ArrayList<Expression>();

		childrenDiff2.add(d);
		childrenDiff2.add(tf.createDiv(c0, c1));
		formulasDiff2.add(tf.createPredicateTerm(tf.createEquals(), childrenDiff2));
		DiffSystem diff2 = tf.createDiffSystem(formulasDiff2);

		DiffSystem diff1AfterNormalization = termToDiffSystem(normalizeInstance.calculate(diffSystemToTerm(diff1), svInstance, s));
		DiffSystem diff2AfterNormalization = termToDiffSystem(normalizeInstance.calculate(diffSystemToTerm(diff2), svInstance, s));

		assertFalse(isNormalized(diff1));
		assertTrue(isOnlyOrdinary(diff1));
		assertEquals(diff2, diff1AfterNormalization);
		assertTrue(isNormalized(diff2));
		assertFalse(isOnlyOrdinary(diff2));
		assertEquals(diff2, diff2AfterNormalization);
	}

	@Test
	public void testInverseDotWithNoConstantAfterEquation() {
		// Diff1 - DiffSystem to be normalized 1/y'=4+x
		List<Formula> formulasDiff1 = new ArrayList<Formula>();
		List<Expression> childrenDiff1 = new ArrayList<Expression>();
		Dot d = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("y")), 1);
		Variable x = tf.createLogicalVariable("x");
		Constant c0 = tf.createConstant(new BigDecimal(1));
		Constant c1 = tf.createConstant(new BigDecimal(4));

		childrenDiff1.add(tf.createDiv(c0, d));
		childrenDiff1.add(tf.createPlus(c1, x));
		formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(), childrenDiff1));
		DiffSystem diff1 = tf.createDiffSystem(formulasDiff1);

		// Diff2 - Expected normalized DiffSystem E R dy: dy*(4+x)=1 & y'=dy
		List<Formula> formulasDiff2 = new ArrayList<Formula>();
		List<Expression> childrenDiff2 = new ArrayList<Expression>();
		List<Expression> childrenDiff2_introduce_equation = new ArrayList<Expression>();
		Variable newVar = tf.createLogicalVariable("dy");
		List<Variable> vars = new ArrayList<Variable>();

		vars.add(newVar);
		childrenDiff2.add(tf.createMult(newVar, tf.createPlus(c1, x)));
		childrenDiff2.add(c0);
		childrenDiff2_introduce_equation.add(d);
		childrenDiff2_introduce_equation.add(newVar);
		formulasDiff2.add(tf.createExists(tf.createVariableDeclaration(RealLDT.getRealSort(), vars),
				tf.createAnd(tf.createPredicateTerm(tf.createEquals(), childrenDiff2), tf.createPredicateTerm(tf.createEquals(), childrenDiff2_introduce_equation))));
		DiffSystem diff2 = tf.createDiffSystem(formulasDiff2);

		DiffSystem diff1AfterNormalization = termToDiffSystem(normalizeInstance.calculate(diffSystemToTerm(diff1), svInstance, s));
		DiffSystem diff2AfterNormalization = termToDiffSystem(normalizeInstance.calculate(diffSystemToTerm(diff2), svInstance, s));

		assertFalse(isNormalized(diff1));
		assertTrue(isOnlyOrdinary(diff1));
		assertEquals(diff2, diff1AfterNormalization);
		assertTrue(isNormalized(diff2));
		assertFalse(isOnlyOrdinary(diff2));
		assertEquals(diff2, diff2AfterNormalization);
	}

	private static boolean isNormalized(DiffSystem sys) {
		return svSortNormalized.canStandFor(sys, null, s);
	}

	private static boolean isOnlyOrdinary(DiffSystem sys) {
		return svSortOnlyOrdinary.canStandFor(sys, null, s);
	}

	protected static Term diffSystemToTerm(DiffSystem diff) {
		Term createTerm = de.uka.ilkd.key.logic.TermFactory.DEFAULT
				.createTerm(Op.BOX, new Term[] { TermBuilder.DF.tt() }, new ImmutableArray[0], JavaBlock.createJavaBlock(new DLStatementBlock(diff)));
		return TermBuilder.DF.and(createTerm, createTerm);
	}

	protected static DiffSystem termToDiffSystem(Term t) {
		return (DiffSystem) t.javaBlock().program().getFirstElement();
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
