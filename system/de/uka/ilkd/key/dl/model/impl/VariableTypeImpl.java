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
/**
 * 
 */
package de.uka.ilkd.key.dl.model.impl;

import java.util.Map;
import java.util.WeakHashMap;

import de.uka.ilkd.key.dl.model.VariableType;
import de.uka.ilkd.key.java.NameAbstractionTable;
import de.uka.ilkd.key.java.SourceElement;
import de.uka.ilkd.key.logic.Name;

/**
 * This class is the implementation of the representation of variable types
 * in dL. {@link VariableType}
 * 
 * @author jdq
 * @since Jul 16, 2007
 * 
 */
public class VariableTypeImpl extends DLTerminalProgramElementImpl implements
        VariableType {
    private static Map<Name, VariableType> instances = new WeakHashMap<Name, VariableType>();

    /**
     * Creates a new VariableType or returns a cached one with the given name.
     * This method ensures that there is only one variable type object for one
     * function name at a time.
     * 
     * @param name
     *            the name of the program variable
     * @return the new or cached program variable
     */
    public static VariableType getVariableType(String name) {
        return getVariableType(new Name(name));
    }

    /**
     * Creates a new VariableType or returns a cached one with the given name.
     * This method ensures that there is only one variable type object for one
     * function name at a time.
     * 
     * @param name
     *            the name of the program variable
     * @return the new or cached program variable
     */
    public static VariableType getVariableType(Name name) {
        VariableType result = instances.get(name);
        if (result == null) {
            result = new VariableTypeImpl(name);
            instances.put(name, result);
        }
        return result;
    }

    private Name name;

    /**
     * Creates a new program variable with a given name
     * 
     * @param name
     *            the name to use
     */
    protected VariableTypeImpl(Name name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.NamedElement#getElementName()
     */
    public Name getElementName() {
        return name;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.model.impl.DLNonTerminalProgramElementImpl#equalsModRenaming(de.uka.ilkd.key.java.SourceElement,
     *      de.uka.ilkd.key.java.NameAbstractionTable)
     */
    /*@Override*/
    public boolean equalsModRenaming(SourceElement se, NameAbstractionTable nat) {
        if (se instanceof VariableType) {
            VariableType type = (VariableType) se;
            if(type.getElementName().equals(getElementName())) {
                return true;
            }
        }
        return false;
    }

}
