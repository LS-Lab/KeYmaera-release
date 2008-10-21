package de.uka.ilkd.key.dl.arithmetics.impl.mathematica.cache.test;

import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.logic.sort.Sort;
import junit.framework.TestCase;

public class CacherTest extends TestCase {
    
    TermBuilder tb = TermBuilder.DF;

    LogicVariable x = new LogicVariable(new Name("x"), SortR.R);
    LogicVariable y = new LogicVariable(new Name("y"), SortR.R);
    LogicVariable a = new LogicVariable(new Name("a"), SortR.R);

    RigidFunction gt = new RigidFunction(new Name("gt"), SortR.FORMULA, new Sort[] { SortR.R, SortR.R });

    public void test_contains() {
        Term term = tb.all(x, tb.ex(y, tb.and(
                tb.func(gt, tb.var(y), tb.var(x)), tb.func(gt, tb.var(a), tb
                                .var(y)))));
        
    }
    
}
