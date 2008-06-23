package de.uka.ilkd.key.dl.arithmetics.impl.qepcad.test;


import de.uka.ilkd.key.dl.arithmetics.impl.qepcad.Term2StringConverter;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.logic.sort.Sort;

import junit.framework.TestCase;

public class Term2StringConverterTest extends TestCase {

	public void test_convert() {
		
		// Creates the term '(not a) or b' 
		TermBuilder tb = TermBuilder.DF;

		RigidFunction rigidFunction = new RigidFunction(new Name("A"),
				Sort.FORMULA, new Sort[0]);
		RigidFunction rigidFunction2 = new RigidFunction(new Name("B"),
				Sort.FORMULA, new Sort[0]);

		Term term = tb.or(tb.not(tb.func(rigidFunction)), tb.func(rigidFunction2));
		
		String termString = Term2StringConverter.convert2String(term);
		
		assertEquals("~a /\\ b", termString);
	}
}
