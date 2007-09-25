// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.proof;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.uka.ilkd.key.collection.ListOfString;
import de.uka.ilkd.key.dl.DLProfile;
import de.uka.ilkd.key.dl.formulatools.Prog2LogicConverter;
import de.uka.ilkd.key.dl.formulatools.ProgramVariableCollector;
import de.uka.ilkd.key.dl.model.Star;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.SourceElement;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.java.StatementContainer;
import de.uka.ilkd.key.java.annotation.Annotation;
import de.uka.ilkd.key.java.annotation.LoopInvariantAnnotation;
import de.uka.ilkd.key.java.reference.ExecutionContext;
import de.uka.ilkd.key.java.reference.ReferencePrefix;
import de.uka.ilkd.key.java.reference.TypeReference;
import de.uka.ilkd.key.java.statement.LoopStatement;
import de.uka.ilkd.key.java.statement.MethodFrame;
import de.uka.ilkd.key.java.visitor.JavaASTVisitor;
import de.uka.ilkd.key.logic.ArrayOfTerm;
import de.uka.ilkd.key.logic.EverythingLocationDescriptor;
import de.uka.ilkd.key.logic.ListOfTerm;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.SLListOfTerm;
import de.uka.ilkd.key.logic.SetAsListOfLocationDescriptor;
import de.uka.ilkd.key.logic.SetOfLocationDescriptor;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.IUpdateOperator;
import de.uka.ilkd.key.logic.op.SchemaVariable;
import de.uka.ilkd.key.pp.LogicPrinter;
import de.uka.ilkd.key.pp.NotationInfo;
import de.uka.ilkd.key.pp.ProgramPrinter;
import de.uka.ilkd.key.rule.IteratorOfRuleSet;
import de.uka.ilkd.key.rule.PosTacletApp;
import de.uka.ilkd.key.rule.TacletApp;

public class LoopInvariantProposer implements InstantiationProposer {

    /**
     * An instance of LoopInvariantProposer
     */
    public static final LoopInvariantProposer DEFAULT = new LoopInvariantProposer();

    /**
     * Returns a proposal for the instantiation of <code>var</code> iff
     * <code>app</code> is a TacletApp for a loop invariant taclet and
     * <code>var</code> is the SchemaVariable representing the invariant and
     * the loop on which the taclet matches contains a loop invariant
     * annotation. Otherwise null is returned.
     */
    public String getProposal(TacletApp app, 
    			      SchemaVariable var, 
			      Services services, 
			      Node undoAnchor,
			      ListOfString previousProposals){
	
        final Object inst = tryToInstantiate(app, var, services);
	final LogicPrinter lp = new LogicPrinter(new ProgramPrinter(null), 
						 NotationInfo.createInstance(),
						 services);
	String proposal;        
	try {
	    if (inst instanceof Term){
		lp.printTerm((Term) inst);
		proposal = lp.toString();
	    }  else if (inst instanceof ListOfTerm){
		lp.printTerm((ListOfTerm) inst);
		proposal = lp.toString();
            } else if (inst instanceof SetOfLocationDescriptor) {
                lp.printLocationDescriptors((SetOfLocationDescriptor) inst);
                proposal = lp.toString();
            } else if (var.name().toString().equals("#modifies")) {
                // TODO: get a better design for this
                if(Main.getInstance().mediator().getProfile() instanceof DLProfile) {
                    proposal = "{";
                    String comma = "";
                    for(String name: ProgramVariableCollector.INSTANCE.getProgramVariables(app.posInOccurrence().subTerm())) {
                        proposal += comma + name;
                        comma = ",";
                    }
                    
                    proposal += "}";
                } else {
                lp
                        .printLocationDescriptors(SetAsListOfLocationDescriptor.EMPTY_SET
                                .add(EverythingLocationDescriptor.INSTANCE));
                proposal = lp.toString();
                }
            } else {
                proposal = null;
            }
        } catch (IOException e) {
            proposal = null;
        }

        return proposal;
    }

    /**
     * returns true if the rulesets contain the rule set loop invariant
     */
    public static boolean inLoopInvariantRuleSet(IteratorOfRuleSet ruleSets) {
        while (ruleSets.hasNext()) {
            if (ruleSets.next().name().toString().equals(
                    "loop_invariant_proposal")) {
                return true;
            }
        }
        return false;
    }

    private static Term getSelfTerm(Term term, Services services) {
        final Services serv = services;

	//ignore updates
	while(term.op() instanceof IUpdateOperator) {
	    term = term.sub(((IUpdateOperator)term.op()).targetPos());
	}

	//the remaining term should contain a program 
	//(because this method is only called for apps of taclets 
	//in "loop_invariant_proposal")
	final ProgramElement pe = term.javaBlock().program();
		
	//fetch "self" from innermost non-static method-frame
	Term result = new JavaASTVisitor(pe) {
	    private Term result;
	    protected void doAction(ProgramElement node) {
		node.visit(this);
	    }
	    protected void doDefaultAction(SourceElement node) {
		if(node instanceof MethodFrame && result == null) {
		    MethodFrame mf = (MethodFrame) node;
		    ExecutionContext ec 
		    	= (ExecutionContext) mf.getExecutionContext();
		    ReferencePrefix rp = ec.getRuntimeInstance();
                    if(!(rp instanceof TypeReference) && rp !=null) {
                        result = serv.getTypeConverter().convertToLogicElement(rp);
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
     * Returns an instantiation of <code>var</code> iff <code>app</code> is
     * a TacletApp for a loop invariant taclet and <code>var</code> is the
     * SchemaVariable representing the invariant and the loop on which the
     * taclet matches contains a loop invariant annotation. Otherwise null is
     * returned. Depending if the var looked for is a list schemavariable or a
     * normal sv a list of terms or a term is returned
     */
    public Object tryToInstantiate(TacletApp app, SchemaVariable var, Services services){	
        Object inst = null;
        if (app instanceof PosTacletApp && 
	    inLoopInvariantRuleSet(app.taclet().ruleSets())) {
	    final PosInOccurrence pos = ((PosTacletApp) app).posInOccurrence();
	    final LoopInvariantAnnotation firstLoopInvAnnot = 
                getFirstLoopInvariantAnnotation(pos.subTerm());
            if (firstLoopInvAnnot == null) {
                Star s = getFirstStar(pos.subTerm());
                if(s != null && s.getInvariant() != null) {
                        return Prog2LogicConverter.convert(s.getInvariant(), services);
                }
                return null;
            }

            // prepare replacing the loop invariant's "self"-variable
            // by the current "self" term
            final Term selfTerm = getSelfTerm(pos.subTerm(), services);
            Map map = new HashMap();
            map.put(TermBuilder.DF.var(firstLoopInvAnnot.getSelfVar()),
                    selfTerm);
            OpReplacer or = new OpReplacer(map);

            // determine instantiation
            final String varName = var.name().toString();
            if (varName.equals("inv") && firstLoopInvAnnot.invariant() != null) {
                inst = or.replace(firstLoopInvAnnot.invariant());
            } else if (varName.equals("#modifies")
                    && firstLoopInvAnnot.assignable() != null) {
                inst = or.replace(firstLoopInvAnnot.assignable());
            } else if (var.name().toString().equals("post")
                    && firstLoopInvAnnot.post() != null) {
                inst = or.replace(firstLoopInvAnnot.post());
            } else if (varName.equals("variant")
                    && firstLoopInvAnnot.variant() != null) {
                inst = or.replace(firstLoopInvAnnot.variant());
            } else if (varName.equals("#old")) {
                inst = convertToListOfTerm(firstLoopInvAnnot.olds());
            }
        }

        return inst;
    }
    
    /**
     * TODO jdq documentation since Sep 18, 2007 
     * @param subTerm
     * @return
     */
    private Star getFirstStar(Term t) {
        while (t.op() instanceof IUpdateOperator) {
            t = ((IUpdateOperator) t.op()).target(t);
        }
        ProgramElement s = ((StatementBlock)t.javaBlock().program()).getChildAt(0);
        if(s instanceof Star) {
        return (Star) s;
        }
        return null;
    }

    private LoopInvariantAnnotation getFirstLoopInvariantAnnotation(Term t) {
        LoopStatement firstLoopStatement = getFirstLoopStatement(t);
        if (firstLoopStatement != null) {
            final Annotation[] a = firstLoopStatement.getAnnotations();

            for (int i = 0; i < a.length; i++) {
                if (a[i] instanceof LoopInvariantAnnotation) {
                    return (LoopInvariantAnnotation) a[i];
                }
            }
        }
        return null;
    }

    private ListOfTerm convertToListOfTerm(ArrayOfTerm array) {
        ListOfTerm result = SLListOfTerm.EMPTY_LIST;
        for (int i = array.size() - 1; i >= 0; i--) {
            result = result.prepend(array.getTerm(i));
        }
        return result;
    }

    private LoopStatement getFirstLoopStatement(Term t) {
        while (t.op() instanceof IUpdateOperator) {
            t = ((IUpdateOperator) t.op()).target(t);
        }
        return getLoopHelp(t.javaBlock().program());
    }

    private LoopStatement getLoopHelp(ProgramElement pe) {
        if (pe instanceof LoopStatement) {
            return (LoopStatement) pe;
        }
        if (pe instanceof StatementContainer) {
            return getLoopHelp(((StatementContainer) pe).getStatementAt(0));
        }
        // shouldn't happen.
        return null;
    }
}
