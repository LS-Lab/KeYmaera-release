package de.uka.ilkd.key.dl.strategy.termProjection;

import de.uka.ilkd.key.dl.formulatools.TermTools;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.strategy.features.DiffIndCandidates;
import de.uka.ilkd.key.dl.strategy.features.FOFormula;
import de.uka.ilkd.key.dl.strategy.features.FOSequence;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.*;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.logic.op.QuanUpdateOperator;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.rule.updatesimplifier.Update;
import de.uka.ilkd.key.strategy.termProjection.ProjectionToTerm;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
        t = constructTerm(pos, goal, t);
        return t;
    }

    public static Term constructTerm(PosInOccurrence pos, Goal goal, Term t) {
        if (DLOptionBean.INSTANCE.isAddRigidFormulas()) {
            Term term = pos.subTerm();
            final Update update = Update.createUpdate(term);
            // unbox from update prefix
            if (term.op() instanceof QuanUpdateOperator) {
                term = ((QuanUpdateOperator) term.op()).target(term);
                if (term.op() instanceof QuanUpdateOperator)
                    throw new AssertionError(
                            "assume that nested updates have been merged");
            }
            if (!(term.op() instanceof Modality && term.javaBlock() != null
                    && term.javaBlock() != JavaBlock.EMPTY_JAVABLOCK && term
                    .javaBlock().program() instanceof StatementBlock)) {
                throw new IllegalArgumentException("inapplicable to " + pos);
            }
            final DLProgram program = (DLProgram) ((StatementBlock) term
                    .javaBlock().program()).getChildAt(0);
            // compute transitive closure of dependency relation
            final Map<ProgramVariable, LinkedHashSet<ProgramVariable>> tdep =
                    DiffIndCandidates.computeTranstitiveDependencies(program, goal.proof().getServices());
            final Set<ProgramVariable> modifieds = DiffIndCandidates.getModifiedVariables(tdep);

            // add all the rigid parts to the candidate formulas
            Term rigid = TermBuilder.DF.tt();
            Semisequent antecedent = goal.sequent().antecedent();
            for (ConstrainedFormula c : antecedent) {
                if (c.formula().isRigid()) {
                    // add all rigid formulas from the antecedent
                    rigid = TermBuilder.DF.and(rigid, c.formula());
                } else if(FOSequence.isFOFormula(c.formula())) {
                    // test whether the formula only contains variables that stay unchanged
                    Set<Operator> signature = TermTools.getSignature(c.formula());
                    boolean rigidForm = true;
                    for(ProgramVariable v: modifieds) {
                        if(signature.contains(v)) {
                            rigidForm = false;
                            break;
                        }
                    }
                    if(rigidForm) {
                        rigid = TermBuilder.DF.and(rigid, c.formula());
                    }
                }
            }
            Semisequent succ = goal.sequent().succedent();
            for (ConstrainedFormula c : succ) {
                if (c.formula().isRigid()) {
                    // add the negation of all rigid formulas in the succedent
                    rigid = TermBuilder.DF.and(rigid, TermBuilder.DF.not(c.formula()));
                } else if(FOSequence.isFOFormula(c.formula())) {
                    // test whether the formula only contains variables that stay unchanged
                    Set<Operator> signature = TermTools.getSignature(c.formula());
                    boolean rigidForm = true;
                    for(ProgramVariable v: modifieds) {
                        if(signature.contains(v)) {
                            rigidForm = false;
                            break;
                        }
                    }
                    if(rigidForm) {
                        rigid = TermBuilder.DF.and(rigid, TermBuilder.DF.not(c.formula()));
                    }
                }
            }
            t = TermBuilder.DF.and(t, rigid);

        }
        return t;
    }
}
