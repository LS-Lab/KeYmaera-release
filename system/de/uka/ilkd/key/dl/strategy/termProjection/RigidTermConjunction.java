package de.uka.ilkd.key.dl.strategy.termProjection;

import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.logic.*;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.termProjection.ProjectionToTerm;

/**
 * Adds all the rigid formulas to an invariant candidate
 * User: jdq
 * Date: 9/16/13
 * Time: 2:49 PM
 */
public class RigidTermConjunction implements ProjectionToTerm {

    private ProjectionToTerm projection;

    public RigidTermConjunction(ProjectionToTerm t) {
        this.projection = t;
    }

    @Override
    public Term toTerm(RuleApp app, PosInOccurrence pos, Goal goal) {
        Term t = projection.toTerm(app, pos, goal);
        if (DLOptionBean.INSTANCE.isAddRigidFormulas()) {
            // add all the rigid parts to the candidate formulas
            Term rigid = TermBuilder.DF.tt();
            Semisequent antecedent = goal.sequent().antecedent();
            for (ConstrainedFormula c : antecedent) {
                if (c.formula().isRigid()) {
                    // add all rigid formulas from the antecedent
                    rigid = TermBuilder.DF.and(rigid, c.formula());
                }
            }
            Semisequent succ = goal.sequent().succedent();
            for (ConstrainedFormula c : succ) {
                if (c.formula().isRigid()) {
                    // add the negation of all rigid formulas in the succedent
                    rigid = TermBuilder.DF.and(rigid, TermBuilder.DF.not(c.formula()));
                }
            }
            t = TermBuilder.DF.and(t, rigid);
        }
        return t;
    }
}
