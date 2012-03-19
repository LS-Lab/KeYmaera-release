/*******************************************************************************
 * Copyright (c) 2012 Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jan-David Quesel - initial API and implementation
 ******************************************************************************/
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
