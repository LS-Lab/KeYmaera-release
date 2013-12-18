package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.exceptions.FailedComputationException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.Options;
import de.uka.ilkd.key.dl.formulatools.DerivativeCreator;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.rules.metaconstruct.DiffFin.RemoveQuantifiersResult;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * @author s0805753@sms.ed.ac.uk
 */
public class DiffIndMod extends AbstractDLMetaOperator {

    public static final Name NAME = new Name("#DiffIndMod");

    public DiffIndMod() {
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
            return diffIndMod(term.sub(0), services);
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

    public Term diffIndMod(Term term, Services services) throws SolverException {
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
				Term boundary;
				Term nonZeroGrad;
/*				if(DLOptionBean.INSTANCE.isUseODEIndFinMethods()) {
				    diffInd = MathSolverManager.getCurrentODESolver()
				            .diffInd(sys, post, services);
				} else {*/
				    diffInd = DerivativeCreator.diffInd(sys, post, services);
				    boundary = MathSolverManager.getCurrentSimplifier().
		            		getBoundary(post, services.getNamespaces());
				    
				    ArrayList<String> stateVars = DerivativeCreator.stateVector(sys, services);
				    nonZeroGrad = MathSolverManager.getCurrentSimplifier().
				    		nonZeroGrad(post, stateVars, services.getNamespaces());
				    
				    /* Introduce the border and evolution domain constraint into 
				     * the hypothesis, e.g. p=0 ∧ χ ⟶ ∇p ≠ 0 ∧ ∇p·f(x)≤0 
				     */
				    diffInd = TermBuilder.DF.imp(
				    		TermBuilder.DF.and(
				    				boundary,
				    				sys.getInvariant(services)
				    				), 
						    TermBuilder.DF.and(
						    		nonZeroGrad,
						    		diffInd
						    		)
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
