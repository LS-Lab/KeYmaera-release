package de.uka.ilkd.key.dl.arithmetics.impl.mathematica.cache;

import com.wolfram.jlink.Expr;

public class ExprRenamer {
    
    public ExprRenamer() {
        
    }
    
    public RenameTable getRenaming( Expr expr ) {
        
    }
    
    /**
     * Renames the variables with the given renametable
     * 
     * @param expr Expression to rename
     * @param table Table with renameinformation
     * @return Renamed expression (copy)
     */
    public Expr rename( Expr expr, RenameTable table ) {
        Expr copy = null; // TODO: Expr kopieren
        
        // Annahme: In copy ist eine tiefe Kopie von expr
        
        renameImpl( copy, table );
        
        return copy;
    }
    
    private void renameImpl( Expr expr, RenameTable table ) {
        
    }
}
