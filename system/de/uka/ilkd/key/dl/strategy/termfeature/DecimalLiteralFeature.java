package de.uka.ilkd.key.dl.strategy.termfeature;

import java.math.BigDecimal;

import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.strategy.termfeature.BinaryTermFeature;
import de.uka.ilkd.key.strategy.termfeature.TermFeature;

public class DecimalLiteralFeature extends BinaryTermFeature {

    public static final TermFeature INSTANCE = new DecimalLiteralFeature ();
    
    private DecimalLiteralFeature () {}

    protected boolean filter(Term term) {
        final Operator op = term.op();
        if (!(op instanceof RigidFunction && ((RigidFunction) op).arity() == 0))
            return false;

        try {
            // try to parse the function name as a literal
            new BigDecimal (op.name().toString());
        } catch (NumberFormatException e) {
            return false;
        }
        
        return true;
    }

}
