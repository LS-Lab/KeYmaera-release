/**
 * 
 */
package de.uka.ilkd.key.dl.strategy.features;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.dl.formulatools.ContainsMetaVariableVisitor;
import de.uka.ilkd.key.dl.formulatools.ContainsMetaVariableVisitor.Result;
import de.uka.ilkd.key.dl.rules.EliminateExistentialQuantifierRule;
import de.uka.ilkd.key.dl.rules.ReduceRuleApp;
import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Goal.GoalStatus;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * @author jdq
 * 
 */
public class EliminateExistentialApproveFeature implements Feature {

	public static final Feature INSTANCE = new EliminateExistentialApproveFeature();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule
	 * .RuleApp, de.uka.ilkd.key.logic.PosInOccurrence,
	 * de.uka.ilkd.key.proof.Goal)
	 */
	/*@Override*/
	public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
		if (app.rule() instanceof EliminateExistentialQuantifierRule) {
			System.out.println("Approving EliminateExistentialQuantifierRule");// XXX
			List<Metavariable> variables = new ArrayList<Metavariable>();
			if (app instanceof ReduceRuleApp) {
				for (String varName : ((ReduceRuleApp) app).getVariables()) {
					variables
							.add((Metavariable) goal.proof().getNamespaces().variables().lookup(
											new Name(varName)));
				}
			}

			if (variables.isEmpty()) {
				final Set<Metavariable> vars = new HashSet<Metavariable>();
				app.posInOccurrence().constrainedFormula().formula()
						.execPreOrder(new Visitor() {

							/*@Override*/
							public void visit(Term visited) {
								if (visited.op() instanceof Metavariable) {
									vars.add((Metavariable) visited.op());
								}
							}

						});
				if (vars.isEmpty()) {
					System.out.println("No Metavariables found");// XXX
					return TopRuleAppCost.INSTANCE;
				}
				variables.addAll(vars);
			}

			ImmutableList<Goal> openGoals = goal.proof().openGoals();
			Iterator<Goal> goalIt = openGoals.iterator();
			System.out.println("Current goal is " + goal);//XXX
			while (goalIt.hasNext()) {
				Goal curGoal = goalIt.next();
				Iterator<ConstrainedFormula> it = curGoal.sequent().iterator();
				Result result = Result.DOES_NOT_CONTAIN_VAR;
				while (it.hasNext()) {
					ConstrainedFormula next = it.next();
					Result res = ContainsMetaVariableVisitor
							.containsMetaVariableAndIsFO(variables, next
									.formula());
					if ((result == Result.CONTAINS_VAR)
							&& (res == Result.CONTAINS_VAR_BUT_CANNOT_APPLY || res == Result.DOES_NOT_CONTAIN_VAR_AND_IS_NOT_FO)) {
						System.out.println("Cannot apply due to form: " + next + "\n on goal " + curGoal);//XXX
						if(goal.getStatus() == GoalStatus.UNKNOWN) {
							System.out.println("Setting status to BLOCKING");//XXX
							goal.setStatus(GoalStatus.BLOCKING);
						} else {
							System.out.println("Goal status is currently: " + goal.getStatus());//XXX
						}
						return TopRuleAppCost.INSTANCE;
					} else if (result == Result.DOES_NOT_CONTAIN_VAR_AND_IS_NOT_FO) {
						if (res == Result.CONTAINS_VAR) {
							result = Result.CONTAINS_VAR_BUT_CANNOT_APPLY;
						}
					} else if (res == Result.CONTAINS_VAR
							|| res == Result.DOES_NOT_CONTAIN_VAR_AND_IS_NOT_FO) {
						result = res;
					} else if (res == Result.CONTAINS_VAR_BUT_CANNOT_APPLY) {
						System.out.println("Cannot apply due to form: " + next + "\n on goal " + curGoal);//XXX
						if(goal.getStatus() == GoalStatus.UNKNOWN) {
							System.out.println("Setting status to BLOCKING");//XXX
							goal.setStatus(GoalStatus.BLOCKING);
						} else {
							System.out.println("Goal status is currently: " + goal.getStatus());//XXX
						}
						return TopRuleAppCost.INSTANCE;
					}
				}
				if (result == Result.CONTAINS_VAR_BUT_CANNOT_APPLY) {
					System.out.println("Cannot apply");// XXX
					if(goal.getStatus() == GoalStatus.UNKNOWN) {
						System.out.println("Setting status to BLOCKING");//XXX
						goal.setStatus(GoalStatus.BLOCKING);
					} else {
						System.out.println("Goal status is currently: " + goal.getStatus());//XXX
					}
					return TopRuleAppCost.INSTANCE;
				}
			}
			System.out.println("Apply");// XXX
		}
		return LongRuleAppCost.ZERO_COST;
	}

}
