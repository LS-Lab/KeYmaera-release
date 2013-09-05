package de.uka.ilkd.key.dl.strategy.features;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.OrbitalSimplifier;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

import java.rmi.RemoteException;

/**
 * This feature is used in order to make sure that eval_literals is only called if it really simplifies things
 * User: jdq
 * Date: 9/5/13
 * Time: 3:04 PM
 */
public class EvalLiteralsFeature implements Feature {

    public static final EvalLiteralsFeature INSTANCE = new EvalLiteralsFeature();

    @Override
    public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
        Term t = pos.subTerm();
        try {
            Term res = MathSolverManager.getSimplifier("Orbital").simplify(t, goal.proof().getNamespaces());
            if(!res.equals(t)) {
                // something changed during simplification
                return LongRuleAppCost.ZERO_COST;
            }
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return TopRuleAppCost.INSTANCE;
    }
}
