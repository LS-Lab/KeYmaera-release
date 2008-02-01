//This file is part of KeY - Integrated Deductive Software Design 
//Copyright (C) 2001-2003 Universitaet Karlsruhe, Germany
//                      and Chalmers University of Technology, Sweden
//
//The KeY system is protected by the GNU General Public License.
//See LICENSE.TXT for details.
//

package de.uka.ilkd.key.logic.op;

import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.sort.ArrayOfSort;
import de.uka.ilkd.key.logic.sort.Sort;


public class RigidFunction extends Function {
	
	public static enum FunctionType {
		UNDEFINED, SKOLEM, MATHFUNCTION;
	}
	
	private FunctionType type = FunctionType.UNDEFINED;
	
	/**
	 * Creates a rigid function with given signature.
	 * @param name
	 * @param sort
	 * @param argSorts
	 */
	public RigidFunction(Name name, Sort sort, Sort[] argSorts) {
	    super(name, sort, argSorts);
	}
	
	/**
	 * Creates a rigid function with given signature.
	 * @param name
	 * @param sort
	 * @param argSorts
	 */
	public RigidFunction(Name name, Sort sort, Sort[] argSorts, FunctionType type) {
	    this(name, sort, argSorts);
	    this.type = type;
	}


	
	/**
	 * Creates a rigid function with given signature.
	 * @param name
	 * @param sort
	 * @param argSorts
	 */
	public RigidFunction(Name name, Sort sort, ArrayOfSort argSorts) {
	    super(name, sort, argSorts);            
	}
	
	/**
	 * Creates a rigid function with given signature.
	 * @param name
	 * @param sort
	 * @param argSorts
	 */
	public RigidFunction(Name name, Sort sort, ArrayOfSort argSorts, FunctionType type) {
	    this(name, sort, argSorts);
	    this.type = type;
	}
	
	/**
	 * @return the isSkolem
	 */
	public boolean isSkolem() {
		return type == FunctionType.SKOLEM;
	}
	
	/**
	 * @return the isMathFunction
	 */
	public boolean isMathFunction() {
		return type == FunctionType.MATHFUNCTION;
	}
	
	public String toString() {
	    return super.toString() + (isSkolem() ? "$Sk" : "");
	}
}
