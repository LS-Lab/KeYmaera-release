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
    @Override
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
