package de.uka.ilkd.key.dl.arithmetics.impl.mathematica.cache;

import com.wolfram.jlink.Expr;

public class Expr2StringConverter {
    
    public static String convert( Expr expr ) {
        return expr.toString();
    }
}
