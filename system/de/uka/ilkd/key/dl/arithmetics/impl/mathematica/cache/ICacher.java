package de.uka.ilkd.key.dl.arithmetics.impl.mathematica.cache;

import com.wolfram.jlink.Expr;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.IKernelLinkWrapper.ExprAndMessages;

public interface ICacher {

    void put( Expr expr, ExprAndMessages exprAndMessages );
    boolean contains( Expr expr );
    ExprAndMessages get( Expr expr );
    
}
