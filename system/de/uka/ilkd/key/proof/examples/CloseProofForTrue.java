// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
package de.uka.ilkd.key.proof.examples;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.*;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.proof.*;
import de.uka.ilkd.key.rule.*;

/** The {@link #run} method of this class will construct a proof object
 * for the sequent that has the formula <code>true</code> in the succedent.  
 * It then prints out the proof and its list of open goals.
 * Next, it constructs a taclet that will close such a goal and
 * applies it.
 *
 * <p>Note that it is not strictly necessary to put our taclet into
 * the Taclet index.  We do it here, because TacletApp objects are
 * rather complicated to construct by hand and the {@link
 * RuleAppIndex} can do it for us.
 */

public class CloseProofForTrue {

    /** return the taclet <code>find(==>true) close goal</code> */
    public Taclet buildTaclet() {
	SuccTacletBuilder builder = new SuccTacletBuilder();
	builder.setName(new Name("our_true_right"));
	/* we can reuse the buildFormula method to construct the
	   true for the find part. */
	builder.setFind(buildFormula());
	/* For taclets that do not close the goal, one would need
	   to add some TacletGoalTemplates, but we are finished here. */
	return builder.getTaclet();
    }

    /** Return a taclet index with our taclet in it. */

    public TacletIndex buildTacletIndex() {
	TacletIndex tacInd = new TacletIndex();
	tacInd.add(NoPosTacletApp.createNoPosTacletApp(buildTaclet()));
	return tacInd;
    }

    /** Return an empty built-in rule index */
    public BuiltInRuleIndex buildBuiltInRuleIndex() {
	return new BuiltInRuleIndex();
    }

    /** Return a RuleAppIndex appropriate for our rules.*/
    public RuleAppIndex buildRuleAppIndex() {
	return 
	    new RuleAppIndex(new TacletAppIndex(buildTacletIndex()),
			     new BuiltInRuleAppIndex(buildBuiltInRuleIndex()));
    }

    public Term buildFormula() {
	TermFactory tf = TermFactory.DEFAULT;
	return tf.createJunctorTerm(Op.TRUE, new Term[0]);
    }

    /** return a sequent containing the formula true. */
    public Sequent buildSequent() {
        ConstrainedFormula cf = new ConstrainedFormula(buildFormula());
	Semisequent succ = Semisequent.EMPTY_SEMISEQUENT
	    .insert(0,cf).semisequent();
	return Sequent.createSuccSequent(succ);
    }

    /** return the proof for the constructed sequent. */
    public Proof buildInitialProof() {
	Proof proof = new Proof("A Hand-made proof", new Services());
	Sequent seq = buildSequent();
	Node rootNode = new Node(proof,seq);
	proof.setRoot(rootNode);
	Goal firstGoal = new Goal(rootNode,buildRuleAppIndex());
	proof.add(firstGoal);
	/* In order to apply rules, the Namespaces need to be in place.*/
	proof.setNamespaces(new NamespaceSet());
	return proof;
    }

    /** Return a {@link RuleApp} that will apply 
     * <code>taclet</code> on the first formula of the succedent.
     */
    public RuleApp getRuleApp(Goal goal) {
	/* We find a position that points to the top level of the 
	 * first formula in our sequent */
	PosInOccurrence pos
	    = new PosInOccurrence(goal.sequent().succedent().getFirst(), 
				  PosInTerm.TOP_LEVEL,
				  false);	

	/* now we get the list of rules applicable here... */
	ImmutableList<TacletApp> rApplist = goal.ruleAppIndex().
	    getTacletAppAt(TacletFilter.TRUE, pos, null, Constraint.BOTTOM);
	return rApplist.head();
    }

    /** Build an initial proof with a formula in it. */
    public void run() {
	Proof proof = buildInitialProof();
	printProof("initial Proof",proof);

	Goal goal = proof.openGoals().head();
	RuleApp rapp = getRuleApp(goal);
	System.out.println("Going to apply:"+rapp);
	System.out.println(rapp.complete());

	/* Now, apply the rule */
	goal.apply(rapp);

	printProof("one step later",proof);

    }

    public void printProof(String name,Proof proof) {
	System.out.println(name+":");
	System.out.println(proof);
	System.out.println("Open Goals:");
	System.out.println(proof.openGoals());
	System.out.println("Is the proof closed? : "+
			   (proof.closed()?"Yes!":"No..."));
	System.out.println();
    }

    public static void main(String[] args) {
	new CloseProofForTrue().run();
    }
}

