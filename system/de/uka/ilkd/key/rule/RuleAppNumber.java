package de.uka.ilkd.key.rule;

public class RuleAppNumber {
	private static int maxRuleAppNumber = 0;
	
	private final int ruleAppNumber;
	
	public RuleAppNumber() {
		ruleAppNumber = maxRuleAppNumber++;
	}

	public int getRuleAppNumber() {
		return ruleAppNumber;
	}
}
