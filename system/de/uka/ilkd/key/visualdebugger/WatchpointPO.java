package de.uka.ilkd.key.visualdebugger;

import de.uka.ilkd.key.gui.configuration.ProofSettings;
import de.uka.ilkd.key.logic.Sequent;
import de.uka.ilkd.key.proof.BuiltInRuleIndex;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.proof.ProofAggregate;
import de.uka.ilkd.key.proof.TacletIndex;
import de.uka.ilkd.key.proof.init.InitConfig;
import de.uka.ilkd.key.proof.init.ModStrategy;
import de.uka.ilkd.key.proof.init.ProofInputException;
import de.uka.ilkd.key.proof.init.ProofOblInput;
import de.uka.ilkd.key.proof.mgt.Contract;
import de.uka.ilkd.key.proof.mgt.Contractable;

public class WatchpointPO implements ProofOblInput {

    private BuiltInRuleIndex builtInRules;
    private InitConfig initConfig;
    private String name;

    /** the proof aggregate for this proof obligation */
    private ProofAggregate po;
    private Sequent sequent = null;
    private ProofSettings settings;

    private TacletIndex taclets;
    
    /**
     * Instantiates a new WatchpointPO.
     * 
     * @name name the name of this WatchpointPO
     * @sequent the sequent
     */
    public WatchpointPO(String name, Sequent sequent) {
        this.name = name;
        this.sequent = sequent;
    }

    public boolean askUserForEnvironment() {
        return false;
    }

    public Contractable[] getObjectOfContract() {
        return new Contractable[0];
    }

    public ProofAggregate getPO() {
        if (po == null) {
            System.out.println("enterning getPO() in WatchpointPO...");

            if ( sequent == null || taclets == null
                    || builtInRules == null || initConfig == null
                    || settings == null) {
                
                throw new IllegalStateException("watchpoint specification proof "
                        + "obligation is not initialized completely.");
            }

            Proof proof = null;
            if (sequent != null) {
                proof = new Proof(name, sequent, "", taclets, builtInRules,
                        initConfig.getServices(), settings);
            }
            proof.setSimplifier(settings
                    .getSimultaneousUpdateSimplifierSettings().getSimplifier());
            po = ProofAggregate.createProofAggregate(proof, name);

        }
        return po;
    }

    public boolean initContract(Contract ct) {
        // TODO Auto-generated method stub
        return false;
    }

    public String name() {
        // TODO Auto-generated method stub
        return name;
    }

    public void readActivatedChoices() throws ProofInputException {
        // TODO Auto-generated method stub

    }

    public void readProblem(ModStrategy mod) throws ProofInputException {
        // TODO Auto-generated method stub

    }

    public void readSpecs() {
        // TODO Auto-generated method stub

    }

    public void setInitConfig(InitConfig i) {
        // TODO Auto-generated method stub

    }
    /**
     * the indices with the rules to be used for specification computation
     * 
     * @param taclets
     *                the TacletIndex with the taclet rule base to be used
     * @param builtInRules
     *                the BuiltInRuleIndex with all available built-in rules
     */
    public void setIndices(TacletIndex taclets, BuiltInRuleIndex builtInRules) {
        this.taclets = taclets;
        this.builtInRules = builtInRules;
    }
    /**
     * the proof settings to be used
     * 
     * @param settings
     *                the ProofSettings to be used for e.g. the update
     *                simplifier to be taken
     */
    public void setProofSettings(ProofSettings settings) {
        this.settings = settings;
    }
    /**
     * the initial config containing for example the services which provide
     * access to the Java model
     */
    public void setConfig(InitConfig i) {
        this.initConfig = i;
    }

}
