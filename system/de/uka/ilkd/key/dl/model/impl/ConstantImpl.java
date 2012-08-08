/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel                                *
 *   quesel@informatik.uni-oldenburg.de                                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
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
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if(obj != null && obj instanceof Constant) {
            Constant c = (Constant) obj;
            return c.getValue().equals(getValue());
        }
        return false;
    }
}
