// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//


package de.uka.ilkd.key.dl.strategy.termProjection;

import de.uka.ilkd.key.dl.formulatools.Prog2LogicConverter;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.QuanUpdateOperator;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.rule.SyntacticalReplaceVisitor;
import de.uka.ilkd.key.strategy.termProjection.ProjectionToTerm;
import de.uka.ilkd.key.util.Debug;

/**
 * Projection of rule apps to the value of an annotation in its \find match modality. The
 * projection can be undefined (null) for those apps that do not
 * possess the annotation in question, or it can raise an error for
 * such applications, depending on demandInst
 */
public class AnnotationProjection implements ProjectionToTerm {

    private final boolean demandInst;
    private final String annotationKey;
    
    private AnnotationProjection(String annotationKey, boolean demandInst) {
        this.annotationKey = annotationKey;
        this.demandInst = demandInst;
    }

    public static AnnotationProjection create(String annotationKey, boolean demandInst) {
        return new AnnotationProjection ( annotationKey, demandInst);
    }
    
    public Term toTerm(RuleApp app, PosInOccurrence pos, Goal goal) {
        Term term = pos.subTerm();
        // unbox from update prefix
        while (term.op() instanceof QuanUpdateOperator) {
            term = ((QuanUpdateOperator) term.op()).target(term);
        }
        if (!(term.op() instanceof Modality && term.javaBlock() != null
                && term.javaBlock() != JavaBlock.EMPTY_JAVABLOCK && term
                .javaBlock().program() instanceof StatementBlock)) {
            throw new IllegalArgumentException("inapplicable to " + pos);
        }
        final DLProgram program = (DLProgram) ((StatementBlock) term
                .javaBlock().program()).getChildAt(0);
        final Services services = goal.proof().getServices();
        final Object instObj = program.getDLAnnotation(annotationKey);
        if ( ! ( instObj instanceof DLProgramElement ) ) {
            Debug.assertFalse ( demandInst,
                                "Did not find annotation "
                                + annotationKey + " that I was supposed to examine" +
                                " (taclet " + app.rule().name() + ")" );
            return null;
        }
        final Term annotation = Prog2LogicConverter.convert((DLProgramElement)instObj, services);
        if ( annotation == null) {
            Debug.assertFalse ( demandInst,
                                "Did not find annotation "
                                + annotationKey + " that I was supposed to examine" +
                                " (taclet " + app.rule().name() + ")" );
            return null;
        }
        return instMVs ( annotation, app, goal );
    }

    private static Term instMVs(Term te, RuleApp app, Goal goal) {
        final Services services = goal.proof ().getServices ();
        final Constraint uc = goal.proof ().getUserConstraint ().getConstraint ();
        final Constraint tacletConstraint = app.constraint ();
        final Constraint displayConstraint = tacletConstraint.join ( uc, services );
        
        if ( displayConstraint.isBottom () ) return te;
    
        // Substitute metavariables of the term
        final SyntacticalReplaceVisitor srv =
            new SyntacticalReplaceVisitor ( displayConstraint );
        te.execPostOrder ( srv );
        return srv.getTerm ();
    }

}
