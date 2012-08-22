// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//
package de.uka.ilkd.key.gui;

import java.util.*;

import de.uka.ilkd.key.gui.configuration.Settings;
import de.uka.ilkd.key.gui.configuration.SettingsListener;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.proof.init.Profile;
import de.uka.ilkd.key.rule.Rule;
import de.uka.ilkd.key.smt.AbstractSMTSolver;
import de.uka.ilkd.key.smt.SMTRule;
import de.uka.ilkd.key.smt.SMTRuleMulti;
import de.uka.ilkd.key.smt.SMTSolver;

/** This class encapsulates the information which 
 *  decision procedure should be used.
 */
public class DecisionProcedureSettings implements Settings {
    
    public static final RuleDescriptor NOT_A_RULE = 
	    new RuleDescriptor(new Name("N/A"), "None Selected");
    
    /**
     * Small data container wrapping name and display name of a rule     
     */
    public static class RuleDescriptor implements Comparable<RuleDescriptor> {		
	
	private final Name ruleName;
	private final String displayName;
	
	public RuleDescriptor(Name ruleName, String displayName) {
	    this.ruleName    = ruleName;
	    this.displayName = displayName; 
	}
	public String getDisplayName() {
	    return displayName;
	}

	public Name getRuleName() {
	    return ruleName;
	}
	
	public boolean equals(Object o) {
	    if (o instanceof RuleDescriptor) {
		return ((RuleDescriptor) o).ruleName.equals(ruleName);
	    }
	    return false;
	}
	
	public String toString() {
	    return ruleName + "(" + displayName + ")";
	}
	
	public int hashCode() {
	    return ruleName.hashCode();
	}
	
	public int compareTo(RuleDescriptor rd) {
	    return ruleName.compareTo(rd.ruleName);
	}
    }
    
    /** String used in the Settings to store the active rule */
    private static final String ACTIVE_RULE  = "[DecisionProcedure]ActiveRule";
    
    private static final String TIMEOUT="[DecisionProcedure]Timeout";
    
    private static final String SAVEFILE="[DecisionProcedure]savefile";
    
    private static final String SHOW_SMT_RES_DIA="[DecisionProcedure]showSMTResDialog";
    
    private static final String MULTIPLEPROVERS="[DecisionProcedure]multprovers";
    
    private static final String WAITFORALLPROVERS = "[DecisionProcedure]WaitForAllProvers";
    
    /**@see {@link de.uka.ilkd.key.smt.SmtLibTranslatorWeaker} */
    private static final String WEAKENSMTTRANSLATION = "[DecisionProcedure]WeakenSMTTranslation";

    /** the list of registered SettingListener */
    private LinkedList<SettingsListener> listenerList = new LinkedList<SettingsListener>();
    
    /** the list of RuleDescriptors of available SMTRules */
    private ArrayList<RuleDescriptor> rules = new ArrayList<RuleDescriptor>();
    
    private HashMap<RuleDescriptor, SMTRule> descriptorToRule = new HashMap<RuleDescriptor, SMTRule>();
    
    
    /** stores a reference on the 'MultipleProverRule'*/
    private SMTRuleMulti ruleMultipleProvers = null;
    
    /** the list of all ruledescriptors of all rules that are installed */
    private ArrayList<RuleDescriptor> installedrules = new ArrayList<RuleDescriptor>();
    
    /** the currently active rule */
    private Name activeRule = NOT_A_RULE.getRuleName();
    
    /** the value of the timeout in tenth of seconds.*/
    private int timeout = 600;
    
    private static DecisionProcedureSettings instance;
    
    private static String EXECSTR = "[DecisionProcedure]Exec";
    /** mapping of rule name (key) to execution string (value) */
    private HashMap<String, String> execCommands = new HashMap<String, String>();
    
    /** the string separating different solver-command values. */
    private static final String execSeperator1 = ":"; 
    /** The String separating solvernames from commands in the settingsfile */
    private static final String execSeperator2 = "="; 
    
    /** the string separating different solvers
      */
    private static final String multSeparator1 = ":";
    
    /**the string separating solvernames from the value */
    private static final String multSeparator2 = "=";
    
    
    private String multProversSettings=null;
    private boolean waitForAllProvers = false;

    
    
    /**
     * This is a singleton.
     */
    private DecisionProcedureSettings() {
	super();
    }
    
    /** adds a listener to the settings object 
     * @param l the listener
     */
    public void addSettingsListener(SettingsListener l) {
        listenerList.add(l);
    }
    
    /**
     * retrieves the rule of the specified name or returns <code>null</code> if
     * no such rule exists
     * @param ruleName the String unambiguously specifying a rule 
     * @return the found SMTRule or <code>null</code> 
     */
    public RuleDescriptor findRuleByName(String ruleName) {
	for (RuleDescriptor r : rules) {	
	    Name descNameObj = r.getRuleName();
	    String descName = descNameObj.toString();
	    if (descName.equals(ruleName)) {
		return r;
	    }
	}
	return NOT_A_RULE;
    }
    
    /**
     * retrieves the rule of the specified name or returns <code>null</code> if
     * no such rule exists
     * @param ruleName the String unambiguously specifying a rule 
     * @return the found SMTRule or <code>null</code> 
     */
    public RuleDescriptor findRuleByName(Name ruleName) {
	return this.findRuleByName(ruleName.toString());
    }
    
    
    /** sends the message that the state of this setting has been
     * changed to its registered listeners (not thread-safe)
     */
    protected void fireSettingsChanged() {
        for (SettingsListener aListenerList : listenerList) {
            aListenerList.settingsChanged(new GUIEvent(this));
        }
    }
    
    /**
     * returns the active rule
     * @return the active rule
     */
    public RuleDescriptor getActiveRule() {
	RuleDescriptor rd = this.findRuleByName(this.activeRule);
	if (this.installedrules.contains(rd)) {
	    return rd;
	} else if (this.installedrules.size() == 0) {
	    this.activeRule = NOT_A_RULE.getRuleName();
	    return NOT_A_RULE;
	} else {
	    rd = this.installedrules.get(0);
	    this.setActiveRule(rd.getRuleName());
	    return this.findRuleByName(this.activeRule);
	}
    }
    
    /**
     * Returns a list of all installed rules, sorted alphabetically by rule name.
     */
    public List<RuleDescriptor> getAllRules() {
	List<RuleDescriptor> sortedRules = new ArrayList<RuleDescriptor>();
	sortedRules.addAll(rules);
	Collections.sort(sortedRules);
	return Collections.unmodifiableList(sortedRules);
    }
    
    /**
     * Returns a list of all installed rules, sorted alphabetically by rule name.
     */
    public List<RuleDescriptor> getAvailableRules() {
	List<RuleDescriptor> toReturn = new ArrayList<RuleDescriptor>();
	toReturn.addAll(this.installedrules);
	Collections.sort(toReturn);
	return Collections.unmodifiableList(toReturn);
    }
    
    /**
     * returns the timeout specifying the maximal amount of time an external prover
     * is run
     * @return the timeout in tenth of seconds
     */
    public int getTimeout() {
	return this.timeout;
    }
    
    /** gets a Properties object and has to perform the necessary
     * steps in order to change this object in a way that it
     * represents the stored settings
     */
    public void readSettings(Properties props) {	
	String ruleString = props.getProperty(ACTIVE_RULE);
	this.activeRule = new Name(ruleString);
	
	String timeoutstring = props.getProperty(TIMEOUT);
	if (timeoutstring != null) {
	    int curr = Integer.parseInt(timeoutstring);
	    if (curr > 0) {
		this.timeout = curr;
	    }
	}
	
	this.readExecutionString(props);
	
	multProversSettings = props.getProperty(MULTIPLEPROVERS);
	
	String wfap = props.getProperty(WAITFORALLPROVERS);
    waitForAllProvers = wfap != null && wfap.equals("true");
	
	String sf = props.getProperty(SAVEFILE);
        this.saveFile = !(sf == null) && sf.equals("true");
	
        String sd = props.getProperty(SHOW_SMT_RES_DIA);
        this.showSMTResDialog = !(sd == null) && sd.equals("true");
    
    	String wt = props.getProperty(WEAKENSMTTRANSLATION);
    	this.weakenSMTTranslation = !(wt == null) && wt.equals("true");

    }
    

    
    /**
     * read the execution strings from the properties file
     * @param props
     */
    private void readExecutionString(Properties props) {
	String allCommands = props.getProperty(EXECSTR);
	//all value pairs are stored separated by a |
	if (allCommands != null) {
	    String[] valuepairs = allCommands.split(execSeperator1);
	    for (String s : valuepairs) {
		String[] vals = s.split(execSeperator2);
		if (vals.length == 2) {
		    //if vals does not contain exactly two items, the entry in the settingsfile is not valid
		    //RuleDescriptor rd = findRuleByName(vals[0]);
		    execCommands.put(vals[0], vals[1]);
		}
	    }
	}
    }
    
    
    /**
     * read the multiple provers strings from the properties file, stored in multProversSettings
     */
    private void readMultProversString()
    {
	
	if(multProversSettings != null){
	    String[] valuepairs = multProversSettings.split(multSeparator1);
	    for(String s : valuepairs){
		String[] vals = s.split(multSeparator2);
		if(vals.length == 2){
		    if(ruleMultipleProvers != null)
		    {
			if(vals[1].equals("true")) ruleMultipleProvers.useSMTSolver(vals[0], true);
			else		   ruleMultipleProvers.useSMTSolver(vals[0], false);
		    }
		}
	    }
	}
    }
    
    /**
     * write the Execution Commands to the file
     * @param prop
     */
    private void writeExecutionString(Properties prop) {
	String toStore = "";
	for (String s : execCommands.keySet()) {
	    RuleDescriptor rd = this.findRuleByName(s);
	    if (rd != NOT_A_RULE) {
		//do not save the execcommand for the not_a_rule dummy
		String comm = execCommands.get(s);
	    	if (comm == null) {
			comm = "";
	    	}
	    	toStore = toStore + rd.ruleName.toString() + execSeperator2 + comm + execSeperator1;
	    }
	}
	//remove the las two || again
	if (toStore.length() >= execSeperator1.length()){
	    //if the program comes here, a the end ad extra || was added.
	    toStore = toStore.substring(0, toStore.length()-execSeperator1.length());
	}
	prop.setProperty(EXECSTR, toStore);
    }
    
    /**
     * Write the values, that specify whether a prover is used for the rule 'multiple provers'. 
     */
    private void writeMultipleProversString(Properties prop) {
	String toStore = "";
	
	ArrayList<String> listNames = ruleMultipleProvers.getNamesOfSolvers(); 
	
	for(String name : listNames){
	    String value = ruleMultipleProvers.SMTSolverIsUsed(name) ? "true" : "false";
	    toStore = toStore + name + multSeparator2 + value + multSeparator1;
	    
	}
	

	if (toStore.length() >= multSeparator1.length()){
	    toStore = toStore.substring(0, toStore.length()-multSeparator1.length());
	}
	prop.setProperty(MULTIPLEPROVERS, toStore);
    }
    
    /**
     * Set a execution command for a certain rule.
     * @param rd the ruledescriptor, which uses this command.
     * @param command the command to use
     */
    public void setExecutionCommand(RuleDescriptor rd, String command, boolean fire) {
	SMTRule r = this.descriptorToRule.get(rd);
	this.execCommands.put(rd.ruleName.toString(), command);
	if (r.isInstalled(true)) {
	    //add the rule to the installed rules (if not there yet)
	    if (!this.installedrules.contains(rd)) {
		this.installedrules.add(rd);
	    }
	} else {
	    //remove from the installed rules
	    if (this.installedrules.contains(rd)) {
		this.installedrules.remove(rd);
	    }
	}
	if(fire)
	this.fireSettingsChanged();
    }
    
    /**
     * Set a execution command for a certain rule.
     * @param r the rule, which uses this command.
     * @param command the command to use
     */
    public void setExecutionCommand(AbstractSMTSolver r, String command) {
	RuleDescriptor rd = this.findRuleByName(r.name());
	this.setExecutionCommand(rd, command,true);
    }
    
    /**
     * get the execution command for a certain rule.
     * @param r the rule
     * @return the execution command
     */
    public String getExecutionCommand(AbstractSMTSolver r) {
	return this.execCommands.get(this.findRuleByName(r.name()).ruleName.toString());
    }
    


    /**
     * recheck, if the rule is installed
     * @param rd the ruleDescriptor
     * @return true, if the given command results in executeable result.
     */
    public boolean checkCommand(RuleDescriptor rd, String Command) {
	SMTRule r = descriptorToRule.get(rd);
	String oldCommand = this.execCommands.get(rd); 
	this.execCommands.put(rd.ruleName.toString(), Command);
	boolean toReturn = r.isInstalled(true);
	//remove the new connad again, as this is ust a test, no store
	this.execCommands.put(rd.ruleName.toString(), oldCommand);
	r.isInstalled(true);
	return toReturn;
    }
    
    public boolean isInstalled(RuleDescriptor rd) {
	return this.installedrules.contains(rd);
    }
    
    public String getExecutionCommand(RuleDescriptor rd) {
	String toReturn = this.execCommands.get(rd.ruleName.toString());
	if (toReturn == null || toReturn.length()==0) {
	    //the default setting is used. Read this one from the DecProc
	    toReturn = this.descriptorToRule.get(rd).defaultExecutionCommand();
	}
	return toReturn;
    }
    
    
    public boolean getMultipleUse(RuleDescriptor rd)  {
	SMTRule rule = descriptorToRule.get(rd);
	SMTSolver s = rule.getSolver();
	return this.ruleMultipleProvers.SMTSolverIsUsed(s);
    }
    
    public void setMultipleUse(RuleDescriptor rd, boolean multipleuse, boolean fire) {
	SMTRule rule = descriptorToRule.get(rd);
	SMTSolver s = rule.getSolver();
	ruleMultipleProvers.useSMTSolver(s, multipleuse);
	if(fire)
	fireSettingsChanged();
    }
    
    
    /**
     * removes the specified listener form the listener list
     * @param l the listener
     */
    public void removeSettingsListener(SettingsListener l) {
	listenerList.remove(l);
    }

    /**
     * if the specified rule is known it is set as active rule, otherwise or specifying <code>null</code>
     * deactivates the rule. 
     */
    public void setActiveRule(Name ruleName) {
	final RuleDescriptor rule = ruleName == null ? 
		NOT_A_RULE : findRuleByName(ruleName.toString());
	if (rule != findRuleByName(""+activeRule)) {
	    this.activeRule = rule.getRuleName();
	    fireSettingsChanged();
	}
    }


    /**
     * sets the timeout until an external prover is terminated
     * @param t the timeout in tenth of seconds
     */
    public void setTimeout(int t) {
	if (t > 0 && t != timeout) {
	    this.timeout = t;
	    this.fireSettingsChanged();
	}
    }

    /**
     * updates the current available SMT rules
     * @param profile the active Profile 
     */
    public void updateSMTRules(Profile profile) {
	//Load the available Solver	
	rules = new ArrayList<RuleDescriptor>();
	this.installedrules = new ArrayList<RuleDescriptor>();
	for (Rule r : profile.
		getStandardRules().getStandardBuiltInRules()) {
	    if (r instanceof SMTRule) {
		RuleDescriptor rd = new RuleDescriptor(r.name(),r.displayName());
		rules.add(rd);
		SMTRule smtr = (SMTRule)r;
		this.descriptorToRule.put(rd, smtr);
		if (smtr.isInstalled(false)) {
		    installedrules.add(rd);
		}
	    }
	    if(r instanceof SMTRuleMulti){
		
		ruleMultipleProvers = (SMTRuleMulti) r;
		this.readMultProversString();
		this.setWaitForAllProvers(waitForAllProvers);

	    }
	}
	
    }
    
    private boolean saveFile = false;


    
    public void setSaveFile(boolean sf) {
	if (sf != this.saveFile) {
	    this.saveFile = sf;
	    this.fireSettingsChanged();
	}
    }
    
    /**
     * returns true, if a created problem file should be saved.
     * @return
     */
    public boolean getSaveFile() {
	return this.saveFile;
    }
    
    private boolean showSMTResDialog = false;
    
    /**@see {@link de.uka.ilkd.key.smt.SmtLibTranslatorWeaker} */
    public boolean weakenSMTTranslation = false;
    
    public void setSMTResDialog(boolean b){
	if(b!=this.showSMTResDialog){
	    this.showSMTResDialog = b;
	    this.fireSettingsChanged();
	}
    }
    
    public boolean getShowSMTResDialog(){
	return this.showSMTResDialog;
    }
    
    /**
     * true, if the argument should be used for test
     * TODO implement?
     */
    public boolean useRuleForTest(int arg) {
	return true;
    }

    
    
    /** implements the method required by the Settings interface. The
     * settings are written to the given Properties object. Only entries of the form 
     * <key> = <value> (,<value>)* are allowed.
     * @param props the Properties object where to write the settings as (key, value) pair
     */
    public void writeSettings(Properties props) {	
        props.setProperty(ACTIVE_RULE, "" + activeRule);
        props.setProperty(TIMEOUT, "" + this.timeout);
      
        if (this.saveFile)
            props.setProperty(SAVEFILE, "true");
        else {
            props.setProperty(SAVEFILE, "false");
        }
        if (this.showSMTResDialog)
            props.setProperty(SHOW_SMT_RES_DIA, "true");
        else {
            props.setProperty(SHOW_SMT_RES_DIA, "false");
        }

        if (this.weakenSMTTranslation)
            props.setProperty(WEAKENSMTTRANSLATION, "true");
        else {
            props.setProperty(WEAKENSMTTRANSLATION, "false");
        }

        props.setProperty(WAITFORALLPROVERS, ruleMultipleProvers.isWaitingForAllProvers() ? "true":"false");
        this.writeExecutionString(props);
        this.writeMultipleProversString(props);
    }

    public static DecisionProcedureSettings getInstance() {
	if (instance == null) {
	    instance = new DecisionProcedureSettings();
	}
	
	return instance;
    }

    public boolean isWaitingForAllProvers() {
	if(ruleMultipleProvers == null)	return waitForAllProvers;
	return ruleMultipleProvers.isWaitingForAllProvers();
    }

    public void setWaitForAllProvers(boolean selected) {
	
	if(ruleMultipleProvers != null)
	{
	    if(ruleMultipleProvers.isWaitingForAllProvers() != selected){
		ruleMultipleProvers.setWaitForAllProvers(selected);
		this.fireSettingsChanged();
	    }
	    
	    
	}
	
    }

    /* (non-Javadoc)
     * @see de.uka.ilkd.key.gui.configuration.Settings#reset()
     */
    @Override
    public void reset() {
        // TODO Auto-generated method stub
        
    }



 

}
