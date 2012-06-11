/*******************************************************************************
 * Copyright (c) 2009 Timo Michelsen, Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Timo Michelsen   - initial API and implementation
 *     Jan-David Quesel - implementation 
 ******************************************************************************/
package de.uka.ilkd.key.dl.arithmetics.impl.qepcad.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import de.uka.ilkd.key.collection.ImmutableSet;
import de.uka.ilkd.key.dl.arithmetics.impl.qepcad.QepCadInput;
import de.uka.ilkd.key.dl.arithmetics.impl.qepcad.Term2QepCadConverter;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.logic.sort.AbstractNonCollectionSort;
import de.uka.ilkd.key.logic.sort.Sort;

class SortR extends AbstractNonCollectionSort {

	public static final SortR R = new SortR(new Name("R"));

	public SortR(Name name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.logic.sort.AbstractSort#extendsSorts()
	 */
	@Override
	public ImmutableSet<Sort> extendsSorts() {
		// TODO Auto-generated method stub
		return null;
	}

}

public class Term2QepCadConverterTest extends TestCase {

	TermBuilder tb = TermBuilder.DF;

	LogicVariable x = new LogicVariable(new Name("x"), SortR.R);
	LogicVariable y = new LogicVariable(new Name("y"), SortR.R);
	LogicVariable a = new LogicVariable(new Name("a"), SortR.R);

	RigidFunction gt = new RigidFunction(new Name("gt"), SortR.FORMULA,
			new Sort[] { SortR.R, SortR.R });

	public void test_convert() {
		Term term = tb.all(x, tb.ex(y, tb.and(
				tb.func(gt, tb.var(y), tb.var(x)), tb.func(gt, tb.var(a), tb
						.var(y)))));
		List<QuantifiableVariable> vars = new ArrayList<QuantifiableVariable>();
		vars.add(x);
		vars.add(y);
		QepCadInput input = Term2QepCadConverter.convert(term, vars);

		assertEquals("(a,x,y)", input.getVariableList());
		assertEquals(1, input.getFreeVariableNum());
		assertEquals("(Ax)(Ey)[((y)>(x))/\\((a)>(y))].", input.getFormula());
	}
}
