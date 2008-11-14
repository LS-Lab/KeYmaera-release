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
