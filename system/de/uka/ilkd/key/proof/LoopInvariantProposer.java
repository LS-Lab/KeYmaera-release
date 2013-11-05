// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.proof;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.uka.ilkd.key.dl.DLProfile;
import de.uka.ilkd.key.dl.formulatools.Prog2LogicConverter;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.strategy.termProjection.RigidTermConjunction;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.java.*;
import de.uka.ilkd.key.java.reference.ExecutionContext;
import de.uka.ilkd.key.java.reference.ReferencePrefix;
import de.uka.ilkd.key.java.reference.TypeReference;
import de.uka.ilkd.key.java.statement.LoopStatement;
import de.uka.ilkd.key.java.statement.MethodFrame;
import de.uka.ilkd.key.java.visitor.JavaASTVisitor;
import de.uka.ilkd.key.logic.*;
import de.uka.ilkd.key.logic.op.*;
import de.uka.ilkd.key.pp.LogicPrinter;
import de.uka.ilkd.key.pp.NotationInfo;
import de.uka.ilkd.key.pp.ProgramPrinter;
import de.uka.ilkd.key.rule.PosTacletApp;
import de.uka.ilkd.key.rule.RuleSet;
import de.uka.ilkd.key.rule.Taclet;
import de.uka.ilkd.key.rule.TacletApp;
import de.uka.ilkd.key.rule.updatesimplifier.Update;
import de.uka.ilkd.key.speclang.LocationDescriptorSet;
import de.uka.ilkd.key.speclang.LoopInvariant;
import de.uka.ilkd.key.speclang.LoopPredicateSet;


public class LoopInvariantProposer implements InstantiationProposer {

    /**
     * An instance of LoopInvariantProposer
     */
    public static final LoopInvariantProposer DEFAULT = new LoopInvariantProposer();
    
    
    
    //-------------------------------------------------------------------------
    //constructors
    //------------------------------------------------------------------------- 
    
    private LoopInvariantProposer() {        
    }
    

    
    //-------------------------------------------------------------------------
    //internal methods
    //------------------------------------------------------------------------- 
    
    private LoopStatement getLoopHelp(ProgramElement pe){
        if(pe instanceof LoopStatement){
            return (LoopStatement) pe;
        } else if(pe instanceof StatementContainer){
            return getLoopHelp(((StatementContainer) pe).getStatementAt(0));
        } else {
            assert false;
            return null;
        }
    }
    
    
    private LoopStatement getFirstLoopStatement(Term t){
        while(t.op() instanceof IUpdateOperator){
            t = ( (IUpdateOperator)t.op () ).target ( t );
        }
        return getLoopHelp(t.javaBlock().program());
    }


    private LoopInvariant getLoopInvariant(Term t, Services services) {
        LoopStatement loop = getFirstLoopStatement(t);
        return services.getSpecificationRepository().getLoopInvariant(loop);
    }
    

    
    //-------------------------------------------------------------------------
    //public interface
    //------------------------------------------------------------------------- 

    /**
     * returns true if the rulesets contain the rule set loop invariant   
     */
    public boolean inLoopInvariantRuleSet(Taclet taclet) {
        if(taclet == null) {
            return true;
        }
        Iterator<RuleSet> it = taclet.ruleSets();
        while(it.hasNext()) {
            if(it.next().name().toString().equals("loop_invariant_proposal")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the receiver term of the innermost method frame of
     * the java block of the passed term, or null if the innermost
     * frame is that of a static method.
     * @param term A term of the form "{u}[p]psi"
     * @param services The services object. 
     */
    public Term getInnermostSelfTerm(Term term, Services services) {
        //ignore updates
        while(term.op() instanceof IUpdateOperator) {
            term = term.sub(((IUpdateOperator)term.op()).targetPos());
        }
        
        //the remaining term should have a Java block 
        final ProgramElement pe = term.javaBlock().program();
                
        //fetch "self" from innermost method-frame
        Term result = new JavaASTVisitor(pe, services) {
            private Term result;
            private boolean done = false;
            protected void doDefaultAction(SourceElement node) {
                if(node instanceof MethodFrame && !done) {
                    done = true;
                    MethodFrame mf = (MethodFrame) node;
                    ExecutionContext ec 
                        = (ExecutionContext) mf.getExecutionContext();
                    ReferencePrefix rp = ec.getRuntimeInstance();
                    if(!(rp instanceof TypeReference) && rp != null) {
                        result = services.getTypeConverter()
                                         .convertToLogicElement(rp);
                    }
                }
            }
            public Term run() {
                walk(pe);
                return result;
            }
        }.run();
                
        return result;
    }
    
        
    /**
     * Returns an instantiation of <code>var</code> 
     * iff <code>app</code> is a TacletApp for a loop invariant taclet
     * and <code>var</code> is the SchemaVariable representing the invariant 
     * and the loop on which the taclet matches contains a loop invariant
     * annotation. Otherwise null is returned.
     * Depending if the var looked for is a list schemavariable or a normal sv
     * a list of terms or a term is returned
     */
    public Object tryToInstantiate(TacletApp app, 
                                   SchemaVariable var, 
                                   Services services) {
//        if(Main.getInstance().mediator().getProfile() instanceof DLProfile) {
//            return null;
//        }
        Object inst = null;
        if (app instanceof PosTacletApp 
            && inLoopInvariantRuleSet(app.taclet())) {
            final PosInOccurrence pos = app.posInOccurrence();
            Term term = pos.subTerm();
            final Update update = Update.createUpdate(term);
            // unbox from update prefix
            if (term.op() instanceof QuanUpdateOperator) {
                term = ((QuanUpdateOperator) term.op()).target(term);
                if (term.op() instanceof QuanUpdateOperator)
                    return null;
            }
            if (!(term.op() instanceof Modality && term.javaBlock() != null
                    && term.javaBlock() != JavaBlock.EMPTY_JAVABLOCK && term
                    .javaBlock().program() instanceof StatementBlock)) {
                return null;
            }
            final DLProgram program = (DLProgram) ((StatementBlock) term
                    .javaBlock().program()).getChildAt(0);
            List<Formula> invariants = program.getDLAnnotation("invariant");
            Term res;
            if(invariants != null && invariants.size() == 1) {
                res = Prog2LogicConverter.convert(invariants.get(0), services);
            } else if (invariants != null && invariants.size() > 1) {
                System.err.println("Ignoring additional invariant candidates");
                res = Prog2LogicConverter.convert(invariants.get(0), services);
            } else {
                res = TermBuilder.DF.tt();
            }
            if(DLOptionBean.INSTANCE.isAddRigidFormulas()) {
                try {
                    inst = RigidTermConjunction.constructTerm(app.posInOccurrence(), Main.getInstance().mediator().getSelectedGoal(), res );
                } catch(Exception e) {
                    // catch exceptions because the RigidTermConjunction feature cannot deal with QdL
                    e.printStackTrace();
                }
            }
//            final LoopInvariant inv = getLoopInvariant(pos.subTerm(), services);
//            if(inv == null) {
//                return null;
//            }
//
//            // determine instantiation
//            final Term selfTerm = getInnermostSelfTerm(pos.subTerm(), services);
//            final Map<Operator, Function> atPreFunctions = inv.getInternalAtPreFunctions();
//            final String varName = var.name().toString();
//            if (varName.equals("inv")) {
//                assert var.isFormulaSV();
//                inst = inv.getInvariant(selfTerm, atPreFunctions, services);
//            } else if(varName.equals("predicates")) {
//                assert var.isListSV();
//                assert var.matchType() == Term.class;
//                inst =inv.getPredicates(selfTerm, atPreFunctions, services);
//            } else if(varName.equals("#modifies")) {
//                assert var.isListSV();
//                assert var.matchType() == LocationDescriptor.class;
//                inst = inv.getModifies(selfTerm, atPreFunctions, services);
//            } else if(varName.equals("variant")) {
//                assert var.isTermSV();
//                inst = inv.getVariant(selfTerm, atPreFunctions, services);
//            }
        }
        
        return inst;
    }
    
    
    /**
     * Returns a proposal for the instantiation of <code>var</code> 
     * iff <code>app</code> is a TacletApp for a loop invariant taclet
     * and <code>var</code> is the SchemaVariable representing the invariant 
     * and the loop on which the taclet matches contains a loop invariant
     * annotation. Otherwise null is returned.
     */
    public String getProposal(TacletApp app, 
    			      SchemaVariable var, 
			      Services services, 
			      Node undoAnchor,
			      ImmutableList<String> previousProposals){
	
        final Object inst = tryToInstantiate(app, 
                                             var, 
                                             services);
	final LogicPrinter lp = new LogicPrinter(new ProgramPrinter(null), 
						 NotationInfo.createInstance(),
						 services);
                
	String proposal;
	try {
	    if (inst instanceof Term){
            proposal = ProofSaver.printTerm((Term)inst, services, true, false).toString();
//		lp.printTerm((Term) inst);
//		proposal = lp.toString();
	    }  else if (inst instanceof LoopPredicateSet){
		lp.printTerm(((LoopPredicateSet) inst).asSet());
		proposal = lp.toString();
            } else if (inst instanceof LocationDescriptorSet) {
                lp.printLocationDescriptors(((LocationDescriptorSet) inst).asSet());
                proposal = lp.toString();
            } else { 
		proposal = null;
	    }
	} catch (IOException e){
	    proposal = null;
	}
        
	return proposal;
    }
}
