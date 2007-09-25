/*
 * ConstantImpl.java 1.00 Mo Jan 15 09:51:14 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import java.math.BigDecimal;
import java.util.Map;
import java.util.WeakHashMap;

import de.uka.ilkd.key.dl.model.Constant;
import de.uka.ilkd.key.java.NameAbstractionTable;
import de.uka.ilkd.key.java.SourceElement;

/**
 * Implementation of {@link Constant}. This class asserts there is only one
 * instance of ConstantImpl for each real number. If a number is not referenced
 * any more it is cleaned by the garbage collector ({@link WeakHashMap}).
 * 
 * @version 1.00
 * @author jdq
 * @since Mo Jan 15 09:51:14 CET 2007
 */
public class ConstantImpl extends DLTerminalProgramElementImpl implements
        Constant {

    private static Map<BigDecimal, Constant> instances = new WeakHashMap<BigDecimal, Constant>();

    private BigDecimal value;

    /**
     * Returns a Constant symbol for the given number. It asserts caching of the
     * available constants such that Constant == Constant is true iff
     * Constant.value == Constant.value.
     * 
     * @param value
     *                the numerical value of the constant
     * @return the constant symbol
     */
    public static Constant getConstant(BigDecimal value) {
        Constant result = instances.get(value);
        if (result == null) {
            result = new ConstantImpl(value);
            instances.put(value, result);
        }
        return result;
    }

    /**
     * Creates a new constant
     * 
     * @param value
     *                the value of the symbol
     */
    private ConstantImpl(BigDecimal value) {
        this.value = value;
    }

    /**
     * @see de.uka.ilkd.key.dl.model.Constant#getValue() getValue
     */
    public BigDecimal getValue() {
        return value;
    }

    /**
     * @see de.uka.ilkd.key.dl.model.impl.DLTerminalProgramElementImpl#equalsModRenaming(de.uka.ilkd.key.java.SourceElement,
     *      de.uka.ilkd.key.java.NameAbstractionTable) equalsModRenaming
     */
    public boolean equalsModRenaming(SourceElement arg0,
            NameAbstractionTable arg1) {
        return arg0 == this;
    }

    /**
     * @see de.uka.ilkd.key.dl.model.impl.DLTerminalProgramElementImpl#toString()
     *      toString
     */
    public String toString() {
        return "" + value;
    }
}
