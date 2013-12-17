package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.exceptions.FailedComputationException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;
import de.uka.ilkd.key.dl.formulatools.DerivativeCreator;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.rules.metaconstruct.DiffFin.RemoveQuantifiersResult;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Junctor;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * @author s0805753@sms.ed.ac.uk
 */
public class DiffIndModNonSmooth extends AbstractDLMetaOperator {

    public static final Name NAME = new Name("#DiffIndModNonSmooth");

    public DiffIndModNonSmooth() {
        super(NAME, 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#sort(de.uka.ilkd.key.logic.Term[])
     */
    /*@Override*/
    public Sort sort(Term[] term) {
        return Sort.FORMULA;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#calculate(de.uka.ilkd.key.logic.Term,
     *      de.uka.ilkd.key.rule.inst.SVInstantiations,
     *      de.uka.ilkd.key.java.Services)
     */
    public Term calculate(Term term, SVInstantiations svInst, Services services) {
        try {
            return diffIndModNonSmooth(term.sub(0), services);
        } catch (UnsolveableException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (FailedComputationException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw (InternalError) new InternalError(e.getMessage()).initCause(e);
        }
    }

    /*
     * Collect the <= atoms from a conjunctive formula.
     */
	private ArrayList<Term> getConjuncts(Term formula, ArrayList<Term> conjuncts){
		ArrayList<Term> conjList = conjuncts;
		if(formula.op() == Junctor.AND){
			if(formula.sub(0).op().name().toString() == "leq"){
				conjList.add(formula.sub(0));	
			}
			if(formula.sub(1).op().name().toString() == "leq"){
				conjList.add(formula.sub(1));	
			}
			if(formula.sub(0).op() == Junctor.AND){
				conjList.addAll(getConjuncts(formula.sub(0), new ArrayList<Term>()));	
			}
			if(formula.sub(1).op() == Junctor.AND){
				conjList.addAll(getConjuncts(formula.sub(1), new ArrayList<Term>()));	
			}
		}
		return conjList;
	}
	
	private Term getCondition( Term formula, 
							   Term chi,
							   ArrayList<Term> f, 
			                   ArrayList<String> stateVars, 
			                   Services services ) 
			                		   throws SolverException, RemoteException{
        
		Term diffInd;
		//Term boundary;
		Term VCs;
		
	   // diffInd = DerivativeCreator.diffInd(sys, formula, services);
	    
	   /* boundary = MathSolverManager.getCurrentSimplifier().
        		getBoundary(formula, services.getNamespaces());*/
	    
	   VCs = MathSolverManager.getCurrentSimplifier().
	    		getVCs(formula, chi,f, stateVars, services.getNamespaces()); 
	   
	   return VCs;
	}
	
	/**
	 * @author s0805753@sms.ed.ac.uk
	 * Method for stringing a list of terms into a conjunction.
	 * N.B. an empty conjunction is by convention True
	 * @param conjuncts
	 * @return
	 */
	private Term toConjunction(ArrayList<Term> conjuncts){
		if(conjuncts.size()>=2){
		Term result = conjuncts.get(0);
		for(int i=1; i < conjuncts.size(); i++){
			result = TermBuilder.DF.and(
					result, 
					conjuncts.get(i)
					);		
		}
		return result;
		}
		else if(conjuncts.size() == 1) return conjuncts.get(0);
		else return TermBuilder.DF.tt();
	}
	
    public Term diffIndModNonSmooth(Term term, Services services) throws SolverException {
        DiffSystem system = (DiffSystem) ((StatementBlock) term
                .javaBlock().program()).getChildAt(0);
        Term post = term.sub(0);
        final NamespaceSet nss = services.getNamespaces();
        if (term.op() == Modality.BOX
                || term.op() == Modality.TOUT) {
            try {
				RemoveQuantifiersResult r = new RemoveQuantifiersResult(system);
				r = DiffFin.removeQuantifiers(nss, r);
				StringWriter writer = new StringWriter();
				DiffSystem sys = r.getSys();
				sys.prettyPrint(new PrettyPrinter(writer));
				
				Term diffInd;
				
				/* State variable vector for computing the non-zero gradient condition */
				ArrayList<String> stateVars = DerivativeCreator.stateVector(sys, services);
				
				/* Vector field */
				ArrayList<Term> f = DerivativeCreator.vectorField(sys, services);
				
/*				if(DLOptionBean.INSTANCE.isUseODEIndFinMethods()) {
				    diffInd = MathSolverManager.getCurrentODESolver()
				            .diffInd(sys, post, services);
				} else {*/
				
				/*
				 * Steps in the rule : 
				 * 
				 * 1) Check if the formula S is syntactically suitable, i.e. if it is
				 *    a disjunction of conjunctions  of ≤ atoms.
				 * 
				 * 2) If not, return an unprovable goal. If yes, then
				 *    proceed to generate VCs using the Mathematica package AMC.m
				 *    
				 * 3) Generate the condition from VCs and introduce it as a goal.
				 *  
				 */
//							
//					/* Step 1 */
//					boolean rightForm = MathSolverManager.
//										getCurrentSimplifier().
//										isLessEqualConjunct(post, nss);
//							
//					/* Step 2 */
//					ArrayList<Term> conjuncts = new ArrayList<Term>();
//					if(!rightForm){
//						return TermBuilder.DF.ff();
//					}
//					else{
//						/* Extracting conjuncts */
//						conjuncts = getConjuncts(post, conjuncts);
//					}
//					
//					/* Step 3
//					   Turn conjuncts into conditions (pᵢ=0 ⟶ ∇pᵢ ≠ 0 ∧ ∇pᵢ·f(x)≤0). */		
//					ArrayList<Term> conditionList = new ArrayList<Term>();
//					for(Term conj : conjuncts){
//						conditionList.add(getCondition(conj, sys, stateVars, services));
//					}
//					
//					/* String the conditions into a single conjunction */
//					Term conditions = toConjunction(conditionList);
//
//				    
				    /*  Generate condition 
				     *  χ ⟶ min(max(p11,...,p1n(1)),...,max(pm1,...,pmn(m)))=0 ⟶ ∇min(max(p11,...,p1n(1)),...,max(pm1,...,pmn(m)))<0. */
					
				    Term conditions = getCondition(post, sys.getInvariant(services), f, stateVars, services);
				    diffInd = TermBuilder.DF.imp(
				    		sys.getInvariant(services),
				    		conditions
				    		);
				    
			//	}
				// reintroduce the quantifiers
				Collections.reverse(r.getQuantifiedVariables());
				for (LogicVariable var : r.getQuantifiedVariables()) {
					diffInd= TermBuilder.DF.all(var, diffInd);
				}
				return diffInd;
//            } catch (SolverException e) {
//                throw e;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw (InternalError) new InternalError(e.getMessage()).initCause(e);
            }
        } else {
            throw new IllegalStateException("Unknown modality "
                    + term.op());
        }
    }
}