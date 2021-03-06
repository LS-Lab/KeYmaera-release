// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.rule;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.collection.ImmutableMap;
import de.uka.ilkd.key.collection.ImmutableSet;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.*;
import de.uka.ilkd.key.logic.op.SchemaVariable;
import de.uka.ilkd.key.proof.Goal;

/** 
 * A SuccTaclet represents a taclet whose find part has to match a top level
 * formula in the succedent of the sequent. 
 */
public class SuccTaclet extends FindTaclet{

     /**
     * creates a Schematic Theory Specific Rule (Taclet) with the given
     * parameters that works on the succedent.  
     * @param name the name of the Taclet 
     * @param applPart contains the application part of an Taclet that is
     * the if-sequent, the variable conditions
     * @param goalTemplates a list of goal descriptions.
     * @param heuristics a list of heurisics for the Taclet
     * @param constraint the Constraint under which the Taclet is valid
     * @param attrs attributes for the Taclet; these are boolean values
     * indicating a noninteractive or recursive use of the Taclet. 
     * @param find the find term of the Taclet
     * @param prefixMap a ImmMap<SchemaVariable,TacletPrefix> that contains the
     * prefix for each SchemaVariable in the Taclet
     * @param onlyRigidFunctions 
     */
    public SuccTaclet(Name name, TacletApplPart applPart,  
		    ImmutableList<TacletGoalTemplate> goalTemplates, 
		    ImmutableList<RuleSet> heuristics,
		    Constraint constraint,
		    TacletAttributes attrs,
		    Term find,ImmutableMap<SchemaVariable,TacletPrefix> prefixMap,
		    ImmutableSet<Choice> choices, boolean onlyRigidFunctions){
	super(name, applPart, goalTemplates, heuristics, constraint,
	      attrs, find, prefixMap, choices, onlyRigidFunctions);
	cacheMatchInfo();
    }	

    /** this method is used to determine if top level updates are
     * allowed to be ignored. This is the case if we have an Antec or
     * SuccTaclet but not for a RewriteTaclet
     * @return true if top level updates shall be ignored 
     */
    protected boolean ignoreTopLevelUpdates() {
	return true;
    }

    /** CONSTRAINT NOT USED 
     * applies the replacewith part of Taclets
     * @param gt TacletGoalTemplate used to get the replaceexpression in the Taclet
     * @param goal the Goal where the rule is applied
     * @param posOfFind the PosInOccurrence belonging to the find expression
     * @param services the Services encapsulating all java information
     * @param matchCond the MatchConditions with all required instantiations 
     */
    protected void applyReplacewith(TacletGoalTemplate gt, Goal goal,
				    PosInOccurrence posOfFind,
				    Services services,
				    MatchConditions matchCond) {
	if (gt instanceof AntecSuccTacletGoalTemplate) {
	    AntecSuccTacletGoalTemplate antecSuccTacletGoalTemplate = (AntecSuccTacletGoalTemplate)gt;
        Sequent replWith = antecSuccTacletGoalTemplate.replaceWith();

        LinkedHashSet<PosInOccurrence> positions = new LinkedHashSet<PosInOccurrence>();
        if(antecSuccTacletGoalTemplate.isFresh()) {
            for(ConstrainedFormula f : goal.sequent().antecedent()) {
                positions.add(new PosInOccurrence(f, PosInTerm.TOP_LEVEL, true));
            }
            for(ConstrainedFormula f : goal.sequent().succedent()) {
                positions.add(new PosInOccurrence(f, PosInTerm.TOP_LEVEL, false));
            }
            positions.remove(posOfFind);
            
            for(PosInOccurrence p: positions) {
                goal.removeFormula(p);
            }
            // remove all hidden formulas from the tacletIndex
            Set<NoPosTacletApp> hidden = new HashSet<NoPosTacletApp>();
            for(NoPosTacletApp ta: goal.ruleAppIndex().tacletIndex().allNoPosTacletApps()) {
                if(ta.taclet().displayName().startsWith("insert_hidden")) {
                    hidden.add(ta);
                }
            }
            for(NoPosTacletApp ta: hidden) {
                goal.ruleAppIndex().removeNoPosTacletApp(ta);
            }
        }

	    addToAntec(replWith.antecedent(), goal, 
		       null, services, matchCond, ((AntecSuccTacletGoalTemplate) gt).isFresh());
            if ( createCopies ( goal, posOfFind, matchCond ) ) {
                System.out.println("Adding + " + replWith.succedent());
                addToSucc(replWith.succedent(),
                        goal,
                        posOfFind,
                        services,
                        matchCond, ((AntecSuccTacletGoalTemplate) gt).isFresh());
            } else {
                replaceAtPos ( replWith.succedent (),
                               goal,
                               posOfFind,
                               services,
                               matchCond, ((AntecSuccTacletGoalTemplate) gt).isFresh() );
            }

	} else {
	    // Then there was no replacewith...
	}
    }

    /**
     * adds the sequent of the add part of the Taclet to the goal sequent
     * @param add the Sequent to be added
     * @param goal the Goal to be updated
     * @param posOfFind the PosInOccurrence describes the place where to add
     * the semisequent 
     * @param matchCond the MatchConditions with all required instantiations 
     */
    protected void applyAdd(Sequent add, Goal goal,
			    PosInOccurrence posOfFind,
			    Services services,
			    MatchConditions matchCond) {
	addToAntec(add.antecedent(), goal, null, services, matchCond, false);
	addToSucc(add.succedent(), goal, posOfFind, services, matchCond, false);
    }

    /** toString for the find part */
    StringBuffer toStringFind(StringBuffer sb) {
	return sb.append("\\find(==>").
	    append(find().toString()).append(")\n");
    }

    protected Taclet setName(String s) {
	SuccTacletBuilder b=new SuccTacletBuilder();
	b.setFind(find());
	return super.setName(s, b);
    }

   

}
