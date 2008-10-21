package de.uka.ilkd.key.dl.arithmetics.impl.mathematica.cache;

import com.wolfram.jlink.Expr;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.IKernelLinkWrapper.ExprAndMessages;

/**
 * Cacher implements a cache-behavior. It uses
 * the ExprRenamer-Class to rename the given expression
 * to a uniform name and stores their String-representation
 * into the cache. The cache is not publicly available.
 * 
 * @author Timo Michelsen
 */
public class Cacher implements ICacher {

    private Cache cache = new Cache();
    
    /**
     * Checks, if a given expression is already
     * cached.
     */
    public boolean contains(Expr expr) {
        
        // Rename given expression
        RenameTable tbl = ExprRenamer.getRenaming(expr);
        Expr renamedExpr = ExprRenamer.rename(expr, tbl);
        String exprName = Expr2StringConverter.convert(renamedExpr);
        
        return this.cache.containsKey(exprName);
    }

    /**
     * Return the related ExprAndMessages-Instance of a
     * given expression, if it exists in the cache. If not,
     * null will be returned.
     */
    public ExprAndMessages get(Expr expr) {
        
        // Rename given expression
        RenameTable tbl = ExprRenamer.getRenaming(expr);
        Expr renamedExpr = ExprRenamer.rename(expr, tbl);
        String exprName = Expr2StringConverter.convert(renamedExpr);
        
        // Check, if renamed expression is already cached
        if( this.cache.containsKey(exprName)) {
            
            ExprAndMessages exprAndMessages = this.cache.get(exprName);
            
            // Reverse table
            tbl.reverse();
                        
            // Rename the variables back.
            // All Variables have now the names for the current context. 
            exprAndMessages.expression = ExprRenamer.rename(exprAndMessages.expression, tbl);
            
            return exprAndMessages;
            
        } else {
            // related ExprAndMessages-instance not found
            return null;
        }
    }

    /**
     * Puts a given expression, related to a given ExprAndMessages-Instance,
     * into the cache. If the expression already exists in the cache,
     * nothing happens.
     */
    public void put(Expr expr, ExprAndMessages exprAndMessages) {

        // Rename given expression
        RenameTable tbl = ExprRenamer.getRenaming(expr);
        Expr renamedExpr = ExprRenamer.rename(expr, tbl);
        String exprName = Expr2StringConverter.convert(renamedExpr);
        
        // Only put it in the cache, when it doesn't already exists
        if( !this.cache.containsKey(exprName)) {
            this.cache.put(exprName, exprAndMessages);
        }
    }
}
