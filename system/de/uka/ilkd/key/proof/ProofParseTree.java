package de.uka.ilkd.key.proof;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProofParseTree {
	// this list needs to be reordered after each operation
	private Branch branch;
	
	public ProofParseTree() {
		setBranch(new Branch());
		branch.addRule(new Rule());
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public Branch getBranch() {
		return branch;
	}
	
}

class Branch implements Comparable<Branch> {
	private Node currentNode;
	private Iterator<Node> children;
	private ArrayList<Rule> rules = new ArrayList<Rule>();

	@Override
	public int compareTo(Branch o) {
		if(o.rules.size() > 0 && rules.size() > 0) {
			return rules.get(0).compareTo(o.rules.get(0));
		} else if (o.rules.size() == 0 && rules.size() != 0) {
			return 1;
		} else if (rules.size() == 0 && o.rules.size() != 0) {
			return -1;
		}
		return 0;
	}
	
	public void addRule(Rule r) {
		rules.add(r);
	}
	
	public Rule first() {
		return rules.get(0);
	}
	
	public Rule poll() {
		return rules.remove(0);
	}
	
	public boolean isEmpty() {
		return rules.isEmpty();
	}

	public void setCurrentNode(Node currentNode) {
		this.currentNode = currentNode;
	}

	public Node getCurrentNode() {
		return currentNode;
	}

	public void setChildren(Iterator<Node> children) {
		this.children = children;
	}

	public Iterator<Node> getChildren() {
		return children;
	}

	public Rule last() {
		return rules.get(rules.size() - 1);
	}
}

class Rule implements Comparable<Rule> {
	private int id;
	private int linenr;
	private List<Pair<Character, String>> ruleInfos;
	
	private List<Branch> subBranches;
	
	public Rule() {
		setRuleInfos(new ArrayList<Pair<Character, String>>());
		subBranches = new ArrayList<Branch>();
	}
	
	public Rule(char id2, String s) {
		this();
		addRuleInfo(id2, s);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void addRuleInfo(char c, String str) {
		getRuleInfos().add(new Pair<Character,String>(c, str));
	}
	
	
	@Override
	public int compareTo(Rule arg0) {
		return id - arg0.id;
	}

	public void setLineNumber(int linenr) {
		this.linenr = linenr;
	}
	
	public int getLineNumber() {
		return linenr;
	}

	public void addBranch(Branch b) {
		subBranches.add(b);
	}

	public List<Branch> getSubBranches() {
		return subBranches;
	}

	public void setRuleInfos(List<Pair<Character, String>> ruleInfos) {
		this.ruleInfos = ruleInfos;
	}

	public List<Pair<Character, String>> getRuleInfos() {
		return ruleInfos;
	}
	
	
}

class Pair<I,S> {
	public Pair(I c, S str2) {
		id = c;
		str = str2;
	}
	I id;
	S str;
}
