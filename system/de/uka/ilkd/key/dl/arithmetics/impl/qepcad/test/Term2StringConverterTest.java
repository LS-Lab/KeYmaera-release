package de.uka.ilkd.key.dl.arithmetics.impl.qepcad.test;

import de.uka.ilkd.key.dl.arithmetics.impl.qepcad.Term2StringConverter;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.logic.sort.AbstractNonCollectionSort;
import de.uka.ilkd.key.logic.sort.SetOfSort;
import de.uka.ilkd.key.logic.sort.Sort;

import junit.framework.TestCase;

class SortR extends AbstractNonCollectionSort {

	public static final SortR R = new SortR(new Name("R"));

	public SortR(Name name) {
		super(name);
	}

	@Override
	public SetOfSort extendsSorts() {
		return null;
	}
}

public class Term2StringConverterTest extends TestCase {

	TermBuilder tb = TermBuilder.DF;

	RigidFunction a = new RigidFunction(new Name("A"), Sort.FORMULA, new Sort[0]);
	RigidFunction b = new RigidFunction(new Name("B"), Sort.FORMULA, new Sort[0]);
	RigidFunction c = new RigidFunction(new Name("C"), Sort.FORMULA, new Sort[0]);

	RigidFunction two = new RigidFunction(new Name("2"), SortR.R, new Sort[0]);
	
	RigidFunction mul = new RigidFunction(new Name("mul"), SortR.R, new Sort[] { Sort.ANY, Sort.ANY });
	RigidFunction add = new RigidFunction(new Name("add"), SortR.R, new Sort[] { Sort.ANY, Sort.ANY });
	RigidFunction eq = new RigidFunction(new Name("equals"), SortR.R, new Sort[] { Sort.ANY, Sort.ANY });
	RigidFunction gt = new RigidFunction(new Name("gt"), SortR.FORMULA, new Sort[] { SortR.R, SortR.R });

	LogicVariable x = new LogicVariable(new Name("x"), SortR.R);
	LogicVariable y = new LogicVariable(new Name("y"), SortR.R);

	public void test_booleanConvert() {

		// Creates the term '(not a) or b'
		Term term = tb.or(tb.not(tb.func(a)), tb.func(b));

		String termString = Term2StringConverter.convert2String(term);

		assertEquals("((~(A))\\/(B))", termString);
	}

	public void test_arithmeticConvert() {

		// Creates the term '(2 * a) + c = b'
		Term term = tb.func(eq, tb.func(add, tb.func(mul, tb.func(two), tb.func(a)), tb.func(c)), tb.func(b));

		String termString = Term2StringConverter.convert2String(term);
		assertEquals("(((2*(A))+(C))=(B))", termString);
	}

	public void test_quantifierConvert() {
		Term term = tb.all(x, tb.ex(y, tb.func(gt, tb.var(y), tb.var(x))));

		String termString = Term2StringConverter.convert2String(term);
		assertEquals("(A x)(E y)((y)>(x))", termString);
	}

	public void test_arrayConvert() {
		String[] args = new String[] { "a", "b", "c", "d" };
		assertEquals("a,b,c,d", Term2StringConverter.array2String(args));
	}
}
