package de.uka.ilkd.key.dl.arithmetics.impl.mathematica.cache;

import java.util.HashMap;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.IKernelLinkWrapper.ExprAndMessages;

/**
 * Cache. Is used by the Cacher-Class to save the given
 * Expressions and their results.
 * 
 * @author Timo Michelsen
 */
public class Cache extends HashMap<String, ExprAndMessages >{

    private static final long serialVersionUID = 1L;
}
