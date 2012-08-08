// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

/** this class inherits from TacletGoalTemplate. It is used if there is a
 * replacewith in the ruleGoals that replaces a sequent with a
 * sequent. The replacewith for terms/formulae is realized in another
 * class calles RewriteTacletGoalTemplate.
*/
package de.uka.ilkd.key.rule;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.collection.DefaultImmutableSet;
import de.uka.ilkd.key.collection.ImmutableSet;
import de.uka.ilkd.key.logic.BoundVarsVisitor;
import de.uka.ilkd.key.logic.Sequent;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.SchemaVariable;

public class AntecSuccTacletGoalTemplate extends TacletGoalTemplate{
    /** sequent that replaces another one */
    private Sequent replacewith=Sequent.EMPTY_SEQUENT;
    private boolean fresh;

    /** creates new Goaldescription 
     *@param addedSeq new Sequent to be added
     *@param addedRules IList<Taclet> contains the new allowed rules
     * at this branch 
     *@param replacewith the Sequent that replaces another one
     */
    public AntecSuccTacletGoalTemplate(Sequent addedSeq,
			   ImmutableList<Taclet> addedRules,
			   Sequent replacewith,
			   ImmutableSet<SchemaVariable> pvs, boolean fresh) {
	super(addedSeq, addedRules, pvs);
	TacletBuilder.checkContainsFreeVarSV(replacewith, 
                null, "replacewith sequent");
	this.replacewith = replacewith;
	this.fresh = fresh;
    }

    public AntecSuccTacletGoalTemplate(Sequent addedSeq,
				     ImmutableList<Taclet> addedRules,				     
				     Sequent replacewith) {
	this(addedSeq, addedRules, replacewith,
	     DefaultImmutableSet.<SchemaVariable>nil(), false);
    }

    /** a Taclet may replace a Sequent by another. The new Sequent is returned.
     * this Sequent.
     * @return Sequent being paramter in the rule goal replacewith(Seq)
     */
    public Sequent replaceWith() {
	return replacewith;
    }

    /**
     * rertieves and returns all variables that are bound in the 
     * goal template
     * @return all variables that occur bound in this goal template
     */
    protected ImmutableSet<QuantifiableVariable> getBoundVariables() {
        final BoundVarsVisitor bvv = new BoundVarsVisitor();
        bvv.visit(replaceWith());
        return bvv.getBoundVariables().union(super.getBoundVariables());
    }
    
    /**
     * @return Sequent being paramter in the rule goal replacewith(Seq)
     */
    Object replaceWithExpressionAsObject() {
	return replacewith;
    }


    public boolean equals(Object o) {
	if ( ! ( o instanceof AntecSuccTacletGoalTemplate ) )
	    return false;
	AntecSuccTacletGoalTemplate other = (AntecSuccTacletGoalTemplate) o;

	return super.equals(other)
	    && replacewith.equals(other.replacewith);
    }
    
    public int hashCode(){
    	int result = 17;
    	result = 37 * result + super.hashCode();
    	result = 37 * result + replacewith.hashCode();
    	return result;
    }

    
    /** toString */
    public String toString() {
	String result=super.toString();
	if(!fresh) {
	    result+="\\replacewith("+replaceWith()+") ";
	} else {
	    result+="\\addfreshgoal("+replaceWith()+") ";
	}
	return result;
    }

    /**
     * @return the fresh
     */
    public boolean isFresh() {
        return fresh;
    }
}
