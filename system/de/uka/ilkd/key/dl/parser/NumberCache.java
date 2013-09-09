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
        String n = number.stripTrailingZeros().toPlainString();
        // bugfix for 0.0 and so on (will be fixed in BigDecimal in Java 8
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6480539
        while(n.contains(".") && (n.endsWith("0") || n.endsWith("."))) {
            n = n.substring(0, n.length() - 1);
        }
        assert new BigDecimal(n).compareTo(number) == 0 : "Stripping trailing zeros should not change the value of a number " + number + " != " + n;
        Name name = new Name(n);
		Function num = NUMBERS.get(name);
		if (num == null) {
            if(number.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Numbers should always be positive. Otherwise use neg(num) in order to negate it.");
            }
			num = new RigidFunction(name, r, new Sort[0]);
			NUMBERS.put(name, num);
		}
		return num;
	}
}
