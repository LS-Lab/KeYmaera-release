// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.rule.conditions;


import de.uka.ilkd.key.dl.strategy.features.FOSequence;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Equality;
import de.uka.ilkd.key.logic.op.Junctor;
import de.uka.ilkd.key.logic.op.LocationVariable;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.logic.op.SVSubstitute;
import de.uka.ilkd.key.logic.op.SchemaVariable;
import de.uka.ilkd.key.rule.VariableConditionAdapter;
import de.uka.ilkd.key.rule.inst.SVInstantiations;


/**
 *  Ensures that the given formula is a conjunction over equalities of well-formed first-order formulas
 *  @author smitsch
 */
public class FirstOrderConjEqualitiesCondition extends VariableConditionAdapter {

	/**
	 * Checks a term tree for being a conjunction of equalities.
	 * @author smitsch
	 */
	private static final class ConjOfEqualitiesCheckVisitor extends Visitor {
		private boolean isConjOfEqualities = true;
		private Term currentEq = null;
		
		@Override
		public void subtreeEntered(Term subtreeRoot) {
			super.subtreeEntered(subtreeRoot);
			if (subtreeRoot.op() instanceof Equality) {
				currentEq = subtreeRoot;
			}
		}
		
		@Override
		public void visit(Term visited) {
			isConjOfEqualities = isConjOfEqualities 
					&& ((visited.op() instanceof Junctor && visited.op() == Junctor.AND)
							|| visited.op() instanceof Equality
							/* don't care what's below an equality */
							|| currentEq != null);
		}
		
		@Override
		public void subtreeLeft(Term subtreeRoot) {
			if (currentEq == subtreeRoot) {
				currentEq = null;
			}
		}
	}
	
    private SchemaVariable var;
    private boolean neg;
    
    public FirstOrderConjEqualitiesCondition(SchemaVariable var, boolean neg) {
        this.var = var;
        this.neg = neg;
        if (!var.isFormulaSV()) {
            throw new IllegalArgumentException("Illegal schema variable");
        }
    }

    /**
     * checks if the condition for a correct instantiation is fulfilled
     * @param var the template Variable to be instantiated
     * @param candidate the SVSubstitute which is a candidate for an
     * instantiation of var
     * @param svInst the SVInstantiations that are already known to be needed 
     * @return true iff condition is fulfilled
     */
    public boolean check(SchemaVariable var, 
            SVSubstitute candidate, 
            SVInstantiations svInst,
            Services services) {

        if (var != this.var) { 
            return true; 
        }
        
        if (candidate instanceof Term) {
        	Term candidateTerm = (Term)candidate;
        	final boolean isLocalVar =   
                    FOSequence.isFOFormula(candidateTerm);
            
        	ConjOfEqualitiesCheckVisitor v = new ConjOfEqualitiesCheckVisitor();
        	candidateTerm.execPreOrder(v);
        	return neg ? !(isLocalVar && v.isConjOfEqualities) : isLocalVar && v.isConjOfEqualities;
        	
        } 
        return false;
    }

    public String toString () {
        return "\\isFirstOrderConjEqualities (" + var+ ")";
    }

}
