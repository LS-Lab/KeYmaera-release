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
		Dot y = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("y")), 1);
		Constant c = tf.createConstant(BigDecimal.TEN);
		//y' > 10
		formulasDiff1.add(tf.createPredicateTerm(tf.createGreater(), y,c));
		DiffSystem diff1 = tf.createDiffSystem(formulasDiff1);
		assertFalse(isOnlyOrdinary(diff1));
		assertFalse(isNormalized(diff1));
	}

	@Test
	public void testIsNormalizedWithConstant() {
		List<Formula> formulasDiff1 = new ArrayList<Formula>();
		Dot y = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("y")), 1);
		Constant c = tf.createConstant(BigDecimal.TEN);
		formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(), y,c));
		DiffSystem diff1 = tf.createDiffSystem(formulasDiff1);

		assertFalse(isOnlyOrdinary(diff1));
		assertTrue(isNormalized(diff1)); // y'=10

		formulasDiff1.clear();
		formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(), c,y));
		diff1 = tf.createDiffSystem(formulasDiff1);

		assertTrue(isOnlyOrdinary(diff1));
		assertFalse(isNormalized(diff1)); // 10=y'
	}

	@Test
	public void testIsNormalizedWithOtherDot() {
		List<Formula> formulasDiff1 = new ArrayList<Formula>();
		Dot y = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("y")), 1);
		Dot z = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("z")), 1);

		formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(), y,z));
		DiffSystem diff1 = tf.createDiffSystem(formulasDiff1);

		assertTrue(isOnlyOrdinary(diff1));
		assertFalse(isNormalized(diff1)); // y'=z'
	}

	@Test
	public void testConnectFormulaWithAnd() {
		List<Formula> formulasDiff1 = new ArrayList<Formula>();
		Dot y = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("y")), 1);
		Constant c = tf.createConstant(BigDecimal.TEN);
	
		formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(), y,c));
		formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(), y, tf.createLogicalVariable("x")));
		DiffSystem diff1 = tf.createDiffSystem(formulasDiff1); //y'=10 , y'=x

		List<Formula> formulasDiff2 = new ArrayList<Formula>();
		formulasDiff2.add(tf.createAnd(formulasDiff1.get(0), formulasDiff1.get(1)));
		DiffSystem diff2 = tf.createDiffSystem(formulasDiff2); //y'=10 & y'=x

		assertFalse(isOnlyOrdinary(diff1));
		assertTrue(isNormalized(diff1));

		DiffSystem diff1AfterNormalization = termToDiffSystem(normalizeInstance.calculate(diffSystemToTerm(diff1), svInstance, s));
		assertEquals(diff2, diff1AfterNormalization);
		assertFalse(isOnlyOrdinary(diff1AfterNormalization));
		assertTrue(isNormalized(diff1AfterNormalization));
	}

	@Test
	public void testMinus() {
		// Diff1 - DiffSystem to be normalized -d'=0
		List<Formula> formulasDiff1 = new ArrayList<Formula>();
		Dot d = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("d")), 1);
		Constant c0 = tf.createConstant(BigDecimal.ZERO);

		formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(), tf.createMinusSign(d), c0));
		DiffSystem diff1 = tf.createDiffSystem(formulasDiff1);

		// Diff2 - Expected normalized DiffSystem -1 != 0 & d'=0/0-1 | -1=0 & 0=0
		List<Formula> formulasDiff2 = new ArrayList<Formula>();
		formulasDiff2.add(transform(tf.createConstant(BigDecimal.valueOf(-1)),c0,d));
		DiffSystem diff2 = tf.createDiffSystem(formulasDiff2);

		DiffSystem diff1AfterNormalization = termToDiffSystem(normalizeInstance.calculate(diffSystemToTerm(diff1), svInstance, s));
		DiffSystem diff2AfterNormalization = termToDiffSystem(normalizeInstance.calculate(diffSystemToTerm(diff2), svInstance, s));

		assertFalse(isNormalized(diff1));
		assertTrue(isOnlyOrdinary(diff1));
		assertEquals(diff2, diff1AfterNormalization);
		assertFalse(isOnlyOrdinary(diff2));
		assertTrue(isNormalized(diff2));
		assertEquals(diff2, diff2AfterNormalization);
	}

	@Test
	public void testAddandSubtract() {
		// Diff1 - DiffSystem to be normalized 5-(2+d')=1
		List<Formula> formulasDiff1 = new ArrayList<Formula>();
		Dot d = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("d")), 1);
		Constant c0 = tf.createConstant(new BigDecimal(5));
		Constant c1 = tf.createConstant(new BigDecimal(2));
		Constant c2 = tf.createConstant(BigDecimal.ONE);

		formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(), tf.createMinus(c0, tf.createPlus(c1, d)), c2));
		DiffSystem diff1 = tf.createDiffSystem(formulasDiff1);

		// Diff2 - Expected normalized DiffSystem -1 != 0 & d'=-2/-1 | -1=0 & -2=0
		List<Formula> formulasDiff2 = new ArrayList<Formula>();

		formulasDiff2.add(transform(tf.createConstant(BigDecimal.valueOf(-1)), tf.createConstant(BigDecimal.valueOf(-2)), d));
		DiffSystem diff2 = tf.createDiffSystem(formulasDiff2);

		DiffSystem diff1AfterNormalization = termToDiffSystem(normalizeInstance.calculate(diffSystemToTerm(diff1), svInstance, s));
		DiffSystem diff2AfterNormalization = termToDiffSystem(normalizeInstance.calculate(diffSystemToTerm(diff2), svInstance, s));

		assertFalse(isNormalized(diff1));
		assertTrue(isOnlyOrdinary(diff1));
		assertEquals(diff2, diff1AfterNormalization);
		
		System.out.println(diff2);
		assertTrue(isNormalized(diff2));
		assertFalse(isOnlyOrdinary(diff2));
		assertEquals(diff2, diff2AfterNormalization);
	}

	@Test
	public void testDivisionByZero() {
		// Diff1 - DiffSystem to be normalized 0*d'=1
		List<Formula> formulasDiff1 = new ArrayList<Formula>();
		Dot d = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("d")), 1);

		formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(), tf.createMult(tf.createConstant(BigDecimal.ZERO), d), tf.createConstant(BigDecimal.ONE)));
		DiffSystem diff1 = tf.createDiffSystem(formulasDiff1);

		// Diff2 - Expected normalized DiffSystem 0 != 0 & d'=1/0 | 0=0 & 1=0
		List<Formula> formulasDiff2 = new ArrayList<Formula>();
		formulasDiff2.add(transform(tf.createConstant(BigDecimal.ZERO), tf.createConstant(BigDecimal.ONE), d));
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
		// Diff1 - DiffSystem to be normalized 1*d'=1
		List<Formula> formulasDiff1 = new ArrayList<Formula>();
		Dot d = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("d")), 1);

		formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(), tf.createMult(tf.createConstant(BigDecimal.ONE), d), tf.createConstant(BigDecimal.ONE)));
		DiffSystem diff1 = tf.createDiffSystem(formulasDiff1);

		// Diff2 - Expected normalized DiffSystem 1 != 0 & d'=1/1 | 1=0 & 1=0
		List<Formula> formulasDiff2 = new ArrayList<Formula>();
		formulasDiff2.add(transform(tf.createConstant(BigDecimal.ONE), tf.createConstant(BigDecimal.ONE), d));
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
		// Diff1 - DiffSystem to be normalized 1/d'=4
		List<Formula> formulasDiff1 = new ArrayList<Formula>();
		Dot d = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("d")), 1);
		Constant c0 = tf.createConstant(new BigDecimal(1));
		Constant c1 = tf.createConstant(new BigDecimal(4));

		formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(), tf.createDiv(c0, d), c1));
		DiffSystem diff1 = tf.createDiffSystem(formulasDiff1);

		// Diff2 - Expected normalized DiffSystem E R dd: 1/dd=4 & d'=dd
		List<Formula> formulasDiff2 = new ArrayList<Formula>();
		LogicalVariable dd = tf.createLogicalVariable("dd");
		List<Variable> vars = new ArrayList<Variable>();
		vars.add(dd);
		formulasDiff2.add(tf.createExists(tf.createVariableDeclaration(RealLDT.getRealSort(), vars), tf.createAnd(tf.createPredicateTerm(tf.createEquals(), tf.createDiv(c0, dd), c1), tf.createPredicateTerm(tf.createEquals(), d, dd))));
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
		// Diff1 - DiffSystem to be normalized 1/d'=4+x
		List<Formula> formulasDiff1 = new ArrayList<Formula>();
		Dot d = tf.createDot(tf.createLogicalVariable(tf.getNamespaces().getUniqueName("d")), 1);
		Variable x = tf.createLogicalVariable("x");
		Constant c0 = tf.createConstant(new BigDecimal(1));
		Constant c1 = tf.createConstant(new BigDecimal(4));

		formulasDiff1.add(tf.createPredicateTerm(tf.createEquals(), tf.createDiv(c0, d), tf.createPlus(c1, x)));
		DiffSystem diff1 = tf.createDiffSystem(formulasDiff1);

		// Diff2 - Expected normalized DiffSystem E R dd: 1/dd=4+x & d'=dd
		List<Formula> formulasDiff2 = new ArrayList<Formula>();
		Variable dd = tf.createLogicalVariable("dd");
		
		List<Variable> vars = new ArrayList<Variable>();
		vars.add(dd);
		
		formulasDiff2.add(tf.createExists(tf.createVariableDeclaration(RealLDT.getRealSort(), vars),
				tf.createAnd(tf.createPredicateTerm(tf.createEquals(), tf.createDiv(c0, dd), tf.createPlus(c1, x)), tf.createPredicateTerm(tf.createEquals(), d, dd))));
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
	public void testQuantifiers() {
		// Diff1 - x=d'+4 & E R y: d'=y-3
		Variable x = tf.createLogicalVariable("x");
		Variable y = tf.createLogicalVariable("y");
		Dot d = tf.createDot(tf.createLogicalVariable("d"), 1);
		Constant c0 = tf.createConstant(BigDecimal.valueOf(4));
		Constant c1 = tf.createConstant(BigDecimal.valueOf(3));
		
		List<Variable> vars = new ArrayList<Variable>();
		vars.add(y);
		
		List<Formula> formulasDiff1 = new ArrayList<Formula>();
		formulasDiff1.add(tf.createAnd(tf.createPredicateTerm(tf.createEquals(), x, tf.createPlus(d, c0)), tf.createExists(tf.createVariableDeclaration(RealLDT.getRealSort(), vars), tf.createPredicateTerm(tf.createEquals(), d, tf.createMinus(y, c1)))));
		DiffSystem diff1 = tf.createDiffSystem(formulasDiff1);
		
		// Diff2 - (-1!=0 & d'=(-1*x+4)/-1 | -1=0 & (-1*x+4)=0) & E R y: d'=y-3
			
		List<Formula> formulasDiff2 = new ArrayList<Formula>();
		formulasDiff2.add(tf.createAnd(transform(tf.createConstant(BigDecimal.valueOf(-1)), tf.createPlus(tf.createMult(tf.createConstant(BigDecimal.valueOf(-1)), x), c0), d), tf.createExists(tf.createVariableDeclaration(RealLDT.getRealSort(), vars), tf.createPredicateTerm(tf.createEquals(), d, tf.createMinus(y, c1)))));
		DiffSystem diff2 = tf.createDiffSystem(formulasDiff2);
		
		DiffSystem diff1AfterNormalization = termToDiffSystem(normalizeInstance.calculate(diffSystemToTerm(diff1), svInstance, s));
		DiffSystem diff2AfterNormalization = termToDiffSystem(normalizeInstance.calculate(diffSystemToTerm(diff2), svInstance, s));
		
		assertFalse(isNormalized(diff1));
		assertEquals(diff2, diff1AfterNormalization);
		assertTrue(isNormalized(diff2));
		assertFalse(isOnlyOrdinary(diff2));
		assertEquals(diff2, diff2AfterNormalization);
	}

	private static Formula transform(Expression aMinusB, Expression gMinusF, Dot d) {
		return tf.createOr(tf.createAnd(tf.createPredicateTerm(tf.createUnequals(), aMinusB, tf.createConstant(BigDecimal.ZERO)), tf.createPredicateTerm(tf.createEquals(), d, tf.createDiv(gMinusF, aMinusB))), tf.createAnd(tf.createPredicateTerm(tf.createEquals(), aMinusB, tf.createConstant(BigDecimal.ZERO)), tf.createPredicateTerm(tf.createEquals(), gMinusF,tf.createConstant(BigDecimal.ZERO))));
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
