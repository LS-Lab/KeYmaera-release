
/*
 * FunctionImpl.java 1.00 Mo Jan 15 09:46:07 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.logic.Name;
import java.util.Map;
import java.util.WeakHashMap;

import de.uka.ilkd.key.dl.model.FreeFunction;

/**
 * Implementation of {@link FreeFunction}. Weak hashing of the functions is
 * done to assert that there is only one instance of this object per name.
 * 
 * @version 1.00
 * @author jdq
 * @since Mo Jan 15 09:46:07 CET 2007
 */
public class FreeFunctionImpl extends FunctionImpl implements FreeFunction {

	private static Map<Name, FreeFunction> instances = new WeakHashMap<Name, FreeFunction>();

	/**
	 * Creates a new Function or returns a cached one with the given name. This
	 * method ensures that there is only one function object for one function
	 * name at a time.
	 * 
	 * @param name
	 *            the name of the function
	 * @return the new or cached function
	 */
	public static FreeFunction getFunction(String name) {
		return getFunction(new Name(name));
	}

	/**
	 * Creates a new Function or returns a cached one with the given name. This
	 * method ensures that there is only one function object for one function
	 * name at a time.
	 * 
	 * @param name
	 *            the name of the function
	 * @return the new or cached function
	 */
	public static FreeFunction getFunction(Name name) {
		FreeFunction result = instances.get(name);
		if (result == null) {
			result = new FreeFunctionImpl(name);
			instances.put(name, result);
		}
		return result;
	}

	private Name name;

	/**
	 * Creates a new function with a given name
	 * 
	 * @param name
	 *            the name to use
	 */
	protected FreeFunctionImpl(Name name) {
		this.name = name;
	}

	/**
	 * Creates a new function
	 */
	protected FreeFunctionImpl() {
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.Function#getName() getName
	 */
	public Name getElementName() {
		return name;
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.Function#setName(java.lang.String) setName
	 */
	public void setName(Name name) {
		this.name = name;
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.FunctionImpl#getSymbol() getSymbol
	 */
	public String getSymbol() {
		return getElementName().toString();
	}

}
