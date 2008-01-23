package de.uka.ilkd.key.dl.strategy.termProjection;

import java.util.List;

import com.wolfram.jlink.Expr;

import de.uka.ilkd.key.dl.formulatools.Prog2LogicConverter;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.strategy.features.FOSequence;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.op.Junctor;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.QuanUpdateOperator;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.rule.SyntacticalReplaceVisitor;
import de.uka.ilkd.key.strategy.termProjection.ProjectionToTerm;
import de.uka.ilkd.key.util.Debug;

/**
 * Projection to the ultimate first-order post condition down inside a term.
 * @author ap
 */
public class UltimatePostProjection implements ProjectionToTerm {

    private final ProjectionToTerm value;
    
    public UltimatePostProjection(ProjectionToTerm value) {
        this.value = value;
    }
    
    public Term toTerm(RuleApp app, PosInOccurrence pos, Goal goal) {
        Term term = value.toTerm(app, pos, goal);
        // unbox from update prefix and modalities and implication-conditions
        while (true) {
            if (term.op() instanceof QuanUpdateOperator) {
                term = ((QuanUpdateOperator) term.op()).target(term);
            } else if (term.op() instanceof Modality) {
                term = term.sub(0); 
            } else if (FOSequence.INSTANCE.isFOFormula(term)) {
                return term;
            } else if (term.op() == Junctor.IMP) {
                // descend into positive parts of conditions/implications
                term = term.sub(1);
            } else if (term.op() instanceof Junctor) {
                throw new IllegalArgumentException("Not currently defined for operator " + term.op() + " of " + term);
            } else {
                throw new AssertionError("This should not happen as the dL grammar is built from first-order primitives. Got operator " + term.op() + " of " + term);
            }
        }   
    }

}
