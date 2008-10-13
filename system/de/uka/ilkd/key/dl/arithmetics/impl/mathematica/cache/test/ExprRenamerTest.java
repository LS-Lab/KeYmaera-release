package de.uka.ilkd.key.dl.arithmetics.impl.mathematica.cache.test;

import com.wolfram.jlink.Expr;

import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.Term2ExprConverter;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.cache.ExprRenamer;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.cache.RenameTable;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.logic.sort.AbstractNonCollectionSort;
import de.uka.ilkd.key.logic.sort.SetOfSort;
import de.uka.ilkd.key.logic.sort.Sort;
import junit.framework.TestCase;

class SortR extends AbstractNonCollectionSort {

    public static final SortR R = new SortR(new Name("R"));

    public SortR(Name name) {
        super(name);
    }

    @Override
    public SetOfSort extendsSorts() {
        return null;
    }
}

public class ExprRenamerTest extends TestCase {

    TermBuilder tb = TermBuilder.DF;

    LogicVariable x = new LogicVariable(new Name("x"), SortR.R);
    LogicVariable y = new LogicVariable(new Name("y"), SortR.R);
    LogicVariable a = new LogicVariable(new Name("a"), SortR.R);

    RigidFunction gt = new RigidFunction(new Name("gt"), SortR.FORMULA, new Sort[] { SortR.R, SortR.R });

    public void test_rename() {
        Term term = tb.all(x, tb.ex(y, tb.and(
                tb.func(gt, tb.var(y), tb.var(x)), tb.func(gt, tb.var(a), tb
                                .var(y)))));
        
        Expr expr = Term2ExprConverter.convert2Expr(term);
        RenameTable tbl = new RenameTable();
        tbl.put("x", "z");
        
        ExprRenamer renamer = new ExprRenamer();
        Expr newExpr = renamer.rename(expr, tbl);
        
        System.out.println( "Alt : " + expr.toString() );
        System.out.println( "Neu : " + newExpr.toString() );
    }

    public void test_getRenaming() {

    }
}
