/**
 * 
 */
package de.uka.ilkd.key.dl.strategy.features;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.uka.ilkd.key.collection.ImmutableSLList;
import de.uka.ilkd.key.dl.formulatools.Prog2LogicConverter;
import de.uka.ilkd.key.dl.formulatools.TermTools;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.QuanUpdateOperator;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.termgenerator.TermGenerator;
import de.uka.ilkd.key.util.Debug;

/**
 * @author ap
 *
 */
public class AnnotationList implements TermGenerator {
    
    private final boolean demandInst;
    private final String annotationKey;
    
    public AnnotationList(String annotationKey, boolean demandInst) {
        this.annotationKey = annotationKey;
        this.demandInst = demandInst;
    }

    /* (non-Javadoc)
     * @see de.uka.ilkd.key.strategy.termgenerator.TermGenerator#generate(de.uka.ilkd.key.rule.RuleApp, de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.proof.Goal)
     */
    /*@Override*/
    public Iterator<Term> generate(RuleApp app, PosInOccurrence pos, Goal goal) {
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
        List<Formula> annotationList = program.getDLAnnotation(annotationKey);
        if (annotationList == null) {
            Debug.assertFalse ( demandInst,
                    "Did not find annotation "
                    + annotationKey + " that I was supposed to examine" +
                    " (taclet " + app.rule().name() + ")" );
            final ImmutableSLList<Term> nil = ImmutableSLList.nil();
            return nil.iterator();
        }
        List<Term> converted = new ArrayList<Term>(annotationList.size());
        for (Formula f : annotationList) {
            converted.add(Prog2LogicConverter.convert(f, services));
        }
        return TermTools.genericToOld(converted).iterator();
        
    }

}
