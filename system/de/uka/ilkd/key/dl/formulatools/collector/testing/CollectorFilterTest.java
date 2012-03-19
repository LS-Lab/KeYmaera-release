/*******************************************************************************
 * Copyright (c) 2009 Timo Michelsen.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Timo Michelsen - initial API and implementation
 ******************************************************************************/
package de.uka.ilkd.key.dl.formulatools.collector.testing;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import de.uka.ilkd.key.dl.formulatools.collector.AllCollector;
import de.uka.ilkd.key.dl.formulatools.collector.FilterVariableSet;
import de.uka.ilkd.key.dl.formulatools.collector.FoundItem;
import de.uka.ilkd.key.dl.formulatools.collector.filter.*;
import de.uka.ilkd.key.logic.*;
import de.uka.ilkd.key.logic.op.*;
import de.uka.ilkd.key.logic.sort.Sort;

/**
 * Testing-Class to test the Collector-Pakage.
 * 
 * @author Timo Michelsen
 * 
 */
public class CollectorFilterTest {

	/**
	 * Some Term to test with
	 */
	private static Term term;

	/**
	 * Creates the Term " not(A) or B "
	 */
	@BeforeClass
	public static void createTerm() {
		TermBuilder tb = TermBuilder.DF;

		RigidFunction rigidFunction = new RigidFunction(new Name("A"),
				Sort.FORMULA, new Sort[0]);
		RigidFunction rigidFunction2 = new RigidFunction(new Name("B"),
				Sort.FORMULA, new Sort[0]);

		term = tb.or(tb.not(tb.func(rigidFunction)), tb.func(rigidFunction2));

	}

	/**
	 * Tests the FoundVariable-Functionality
	 */
	@Test
	public void foundVariableTest() {
		FoundItem f = new FoundItem("var", term);

		assertEquals(f.getName(), "var");
		assertEquals(f.getTerm(), term);
	}

	/**
	 * Tests the AllCollector-Class
	 */
	@Test
	public void allCollectorTest() {
		AllCollector collector = new AllCollector();
		term.execPreOrder(collector);

		Set<String> result = collector.getItemSet().getVariables();

		assertTrue(compareSame(new String[] { "not", "or", "A", "B" }, result
				.toArray(new String[0])));
	}

	/**
	 * Tests the usage of own filterclasses with the decorator-pattern
	 */
	@Test
	public void filterTest() {
		AllCollector collector = new AllCollector();
		term.execPreOrder(collector);
		FilterVariableSet set = collector.getItemSet();

		FilterVariableSet result = set.filter(new FilterNotOperatorName("not",
				new FilterOperatorName("or", null)));

		assertTrue(compareSame(result.getVariables().toArray(new String[0]),
				new String[] { "or" }));
	}

	/**
	 * Test, if the AllCollector with its filters has an aquivalent behavior
	 * like VariableCollector
	 */
	/*@SuppressWarnings("unchecked")
	@Test
	public void variableCollector2AllCollectorTest() {
		// VariableCollector
		Set<String> result = VariableCollector.getVariables(term);

		FilterVariableSet set = AllCollector.getItemSet(term);
		FilterVariableSet set2 = set.filter(new FilterVariableCollector(null));
		
		assertTrue(compareSame(result.toArray(new String[0]), 
							   set2.getVariables().toArray(new String[0])));
	}*/

	/**
	 * Private method to compare two String-arrays, interpreted as Groups: the
	 * position of the elements is not important
	 * 
	 * @param arrayA
	 *            First array to compare
	 * @param arrayB
	 *            Second array to compare
	 * @return true, if the two given arrays are "same", false otherwise
	 */
	private boolean compareSame(String[] arrayA, String[] arrayB) {
		if (arrayA == null || arrayB == null)
			return false;

		if (arrayA.length != arrayB.length)
			return false;

		for (String s : arrayA) {

			boolean found = false;

			for (int i = 0; !found && i < arrayB.length; i++) {
				found = s.equals(arrayB[i]);
			}

			if (!found)
				return false;
		}

		return true;
	}
}
