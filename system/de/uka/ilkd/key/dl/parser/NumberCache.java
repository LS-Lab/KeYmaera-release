/**
 * File created 08.03.2007
 */
package de.uka.ilkd.key.dl.parser;

import java.math.BigDecimal;
import java.util.Map;
import java.util.WeakHashMap;

import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.logic.sort.Sort;

/**
 * Caches all function objects for numbers. This done due to performance issues
 * as well as the fact that the namespaces sometimes do not contain the function
 * objects as they should.
 * 
 * Number objects can safely be shared across proves as they are immutable.
 * 
 * @author jdq
 * @since 08.03.2007
 * 
 */
public class NumberCache {
	private static final Map<Name, Function> NUMBERS = new WeakHashMap<Name, Function>();

	public static Function getNumber(BigDecimal number, Sort r) {
		Name name = new Name(number.toPlainString());
		Function num = NUMBERS.get(name);
		if (num == null) {
			num = new RigidFunction(name, r, new Sort[0]);
			NUMBERS.put(name, num);
		}
		return num;
	}
}
