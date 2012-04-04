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

/**
 * Represents a table with the information, which variable was
 * renamed.
 * 
 * @author Timo Michelsen
 */
public class RenameTable extends HashMap<String, String>{

    private static final long serialVersionUID = 1L;

    public RenameTable() {
        
    }
    
    /**
     * Swaps keys with values.
     */
    @SuppressWarnings("unchecked")
    public void reverse() {
        
        HashMap<String,String> copy = (HashMap<String, String>) clone();
        
        clear();
        
        for( String key : copy.keySet()) {
            put( copy.get(key), key);
        }
    }
    
    public String toString() {
        String res = "RenameTable: ";
        for( String key : this.keySet() ) {
            res += "(" + key + " -> " + get(key) + ") ";
        }
        return res;
    }
}
