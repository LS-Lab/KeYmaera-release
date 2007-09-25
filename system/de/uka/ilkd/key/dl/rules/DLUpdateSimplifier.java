/**
 * File created 05.02.2007
 */
package de.uka.ilkd.key.dl.rules;

import de.uka.ilkd.key.rule.ListOfIUpdateRule;
import de.uka.ilkd.key.rule.SLListOfIUpdateRule;
import de.uka.ilkd.key.rule.UpdateSimplifier;
import de.uka.ilkd.key.rule.updatesimplifier.ApplyAnonymousUpdateOnNonRigid;
import de.uka.ilkd.key.rule.updatesimplifier.ApplyOnAnonymousUpdate;
import de.uka.ilkd.key.rule.updatesimplifier.ApplyOnLocalVariableOrStaticField;
import de.uka.ilkd.key.rule.updatesimplifier.ApplyOnNonRigidTerm;
import de.uka.ilkd.key.rule.updatesimplifier.ApplyOnNonRigidWithExplicitDependencies;
import de.uka.ilkd.key.rule.updatesimplifier.ApplyOnRigidOperatorTerm;
import de.uka.ilkd.key.rule.updatesimplifier.ApplyOnRigidTerm;
import de.uka.ilkd.key.rule.updatesimplifier.ApplyOnUpdate;

/**
 * This is the modified update simplifier ({@link UpdateSimplifier}) used for
 * simplification of DLPrograms.
 * 
 * @author jdq
 * @since 05.02.2007
 * 
 */
public class DLUpdateSimplifier extends UpdateSimplifier {

    /**
     * 
     */
    public DLUpdateSimplifier() {
        ListOfIUpdateRule usRules = SLListOfIUpdateRule.EMPTY_LIST.append(
                new ApplyOnAnonymousUpdate(this)).append(
                new ApplyAnonymousUpdateOnNonRigid(this)).append(
                new ApplyOnUpdate(this)).append(
                new ApplyOnLocalVariableOrStaticField(this)).append(
                new DLApplyOnModality(this)).append(new ApplyOnRigidTerm(this))
                .append(new ApplyOnRigidOperatorTerm(this)).append(
                        new ApplyOnNonRigidWithExplicitDependencies(this))
                .append(new ApplyOnNonRigidTerm(this));

        setSimplificationRules(usRules);
    }

    private DLApplyOnModality lnkDLApplyOnModality;
}
