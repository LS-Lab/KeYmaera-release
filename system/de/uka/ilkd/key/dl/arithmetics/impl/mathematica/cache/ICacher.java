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

import com.wolfram.jlink.Expr;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.IKernelLinkWrapper.ExprAndMessages;

public interface ICacher {

    void put( Expr expr, ExprAndMessages exprAndMessages );
    void put(ICacher cache);
    boolean contains( Expr expr );
    ExprAndMessages get( Expr expr );
    
 
    void setMaxCacheSize(int size);
}
