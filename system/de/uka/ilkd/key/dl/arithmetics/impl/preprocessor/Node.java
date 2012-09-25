package de.uka.ilkd.key.dl.arithmetics.impl.preprocessor;

import java.util.ArrayList;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * <b> Node implementation</b>. <br>
 * This class is used to represent KeY {@code Operators} and {@code Terms}
 * within the Mutable Tree data structure.
 * 
 * @author s0805753@sms.ed.ac.uk
 * @since 02/02/2012
 * 
 */

public class Node {

	private String value;
	private Node parent;
	private ArrayList<Node> children = new ArrayList<Node>();
	private TreeSet<String> boundVars;

	private static Logger logger = Logger.getLogger("NodeLogger");

	public Node(String value) {
		children = new ArrayList<Node>();
		this.setValue(value);
	}

	Node() {
		this(null);
	}

	public void setBoundVars(TreeSet<String> boundVars) {
		if (this.isQuantifier() && boundVars != null) {
			this.boundVars = boundVars;
		}
	}
	
	public void addBoundVars(String var){
		boundVars.add(var);
	}
	
	public void addBoundVars(TreeSet<String> vars){
		if(vars==null) return;
		if(boundVars==null) boundVars = new TreeSet<String>(); 
		boundVars.addAll(vars);
	}

	public TreeSet<String> getBoundVars() {
		return this.boundVars;
	}

	public void setValue(String value) {

		if (value == null || value.isEmpty()) {
			this.value = null;
			return;
		}
		this.value = value;
	}

	boolean setParent(Node newParent) {
		if (newParent == null)
			return false;

		this.parent = newParent;
		return true;
	}

	void resetParent() {
		this.parent = null;
	}

	public String getValue() {
		return this.value;
	}

	public Node getParent() {
		return this.parent;
	}

	public Node getChildAt(int i) {
		if (children.size() <= i) {
			logger.error("Invalid child node index! Node " + this.getValue()
					+ " has arity " + this.getArity() + ". Child at " + i
					+ " requested!");
			return null;
		}
		return children.get(i);
	}

	public boolean isEmpty() {
		if (this.value == null) {
			return true;
		}
		return false;
	}
	
	// Determine whether the node is a binary predicate symbol.
	public boolean isBinaryPredicate() {
		if (this.getArity() != 2)
			return false;
		if (Operators.negatePredicate(Operators.valueOf(this.getValue()))
				.equals(this.getValue()))
			return false;
		return true;
	}
	
	// Determine whether the node is an exponent.
	public boolean isExponent(){
		if(this.getArity()!=2) return false;
		if(this.getValue().equals("exp")) return true;
		return false;
	}
	
	public boolean isVerum(){
		if(this.getValue().equals("VERUM")) return true;
		return false;
	}
	
	public boolean isFalsum(){
		if(this.getValue().equals("FALSUM")) return true;
		return false;
	}
	
	public boolean isVariable() {
		if (	this.getArity()!= 0 	|| 
				this.isVerum() 			|| 
				this.isFalsum()			|| 
				this.getValue().matches("[0-9]+(\\.[0-9]+)?")
				)	return false;
		return true;
	}

	public boolean sameValueAs(Node node) {
		if (node.getValue().equals(this.getValue())) {
			return true;
		}
		return false;
	}

	public int getArity() {
		return this.children.size();
	}

	public Node[] getChildren() {
		Node[] children = new Node[this.getArity()];
		for (int i = 0; i < this.getArity(); i++) {
			children[i] = this.getChildAt(i);
		}
		return children;
	}

	public boolean isLeaf() {
		if (this.children.size() == 0) {
			return true;
		}
		return false;
	}

	public boolean hasNoParent() {
		if (this.parent == null) {
			return true;
		}
		return false;
	}

	boolean replaceChildAt(int i, Node child) {
		if (i < 0 || child == null)
			return false;
		this.removeChildAt(i);
		children.add(i, child);
		child.setParent(this);
		return true;
	}
	
	public boolean addChildAt(int i, Node child) {
		if (i < 0 || child == null)
			return false;
		children.add(i, child);
		child.setParent(this);
		return true;
	}

	public Node getFirstChild() {
		return children.get(0);
	}

	public Node getSecondChild() {
		return children.get(1);
	}
	
	public Node getLastChild() {
		return children.get(children.size() - 1);
	}

	public boolean hasFirstChild() {
		if (children.get(0) == null)
			return false;
		return true;
	}

	boolean addChild(Node child) {
		if (child == null)
			return false;
		children.add(child);
		return true;
	}

	public void removeAllChildren(){
			while(children.size()>0){
				logger.info("Removing child "+ getFirstChild().getValue());
				removeFirstChild();
				logger.info("Now arity is :" + getArity());
			}

	}
	
	public boolean removeChildAt(int i) {
		if (children.size() <= i)
			return false;
		children.get(i).resetParent();
		children.remove(i);
		return true;
	}

	public boolean removeFirstChild() {
		return removeChildAt(0);
	}

	public boolean removeLastChild() {
		return removeChildAt(children.size() - 1);
	}

	boolean substituteChild(Node original, Node substitution) {
		if (children.contains(original)) {
			int first = children.indexOf(original);
			int last = children.lastIndexOf(original);
			if (first != last) {
				logger.error("The node " + original.getValue()
						+ " is not a unique child of " + this.getValue()
						+ " cannot apply substitution!");
				return false;
			}
			replaceChildAt(children.indexOf(original), substitution);
			return true;
		}
		logger.error("The node " + original.getValue() + " is not a child of "
				+ this.getValue() + ". Cannot apply substitution.");
		return false;
	}

	boolean removeFromParent() {
		if (parent == null)
			return false;
		if(parent.getIndexOf(this)<0) 
			return false;
		parent.removeChildAt(parent.getIndexOf(this));
		this.resetParent();
		return true;
	}

	public int getIndexOf(Node child) {
		if (this.isLeaf()) return -1;
		return children.indexOf(child);
	}

	public boolean hasChild(Node child) {
		if (this.getIndexOf(child) > -1)
			return true;
		return false;
	}

	public String printBoundVars(){
		StringBuilder output = new StringBuilder();
		if(boundVars==null || !this.isQuantifier()) return "";
		for(String boundVar: boundVars){
			output.append(boundVar + " ");
		}
		return output.toString().trim().replaceAll(" ", ", ");
	}
	
	//FIXME: fragile
	public boolean isQuantifier() {
		if(this.getArity()!=1) return false;
		if (this.getValue().equals(Operators.all.KeY)
				| this.getValue().equals(Operators.exist.KeY)) {
			return true;
		}
		return false;
	}

   /*    FIXME: This is implemented for logging purposes only and hence is
    *    redundant as far as the implementation is concerned. Remove should an
    *    opportunity present itself.
    *
    *    since Wed, 18 Apr 2012, 00:39:02
    *    by s0805753
    */
   
	public boolean isUniversalQuantifier() {
		if(this.getArity()!=1) return false;
		if (this.getValue().equals(Operators.all.KeY)){
			return true;
		}
		return false;
	}
	
	public boolean isExistentialQuantifier() {
		if(this.getArity()!=1) return false;
		if (this.getValue().equals(Operators.exist.KeY)) {
			return true;
		}
		return false;
	}

	/* Methods for finding square/cube roots */
	
	public boolean isRational(){
		return this.isRational(" ", " ");
	}
	
	public boolean isRational(long nominator, long denominator){
		return this.isRational(String.valueOf(nominator), String.valueOf(denominator));
	}
	
	public boolean isRational(String nominator, String denominator){
		if(this.getArity()!=2) return false;
		String nominatorPattern = (nominator.matches("[0-9]+"))? nominator : "[0-9]+";
		String denominatorPattern = (denominator.matches("[0-9]+"))? denominator : "[0-9]+";
		logger.info("checking "+ this.getValue() +" for rational number of the form "+ nominatorPattern +"/" + denominatorPattern);
		logger.info(this.getFirstChild().getValue() + " matches nominator? " + this.getFirstChild().getValue().matches(nominatorPattern));
		logger.info(this.getSecondChild().getValue() + " matches denominator? " + this.getSecondChild().getValue().matches(denominatorPattern));

		if (Operators.valueOf(this.getValue()) == Operators.div
				&& (this.getFirstChild().isLeaf() && this.getFirstChild()
						.getValue().matches(nominatorPattern))
				&& (this.getSecondChild().isLeaf() && this.getSecondChild()
						.getValue().matches(denominatorPattern))){
			logger.info("Found rational exponent "+ nominator +"/"+ denominator);
			return true;
		}
		logger.info("NO rational exponent "+ nominator +"/"+ denominator);
		return false;
	}
	
	public boolean isSquareRoot() {
		if(this.getArity()!=2) return false;
		if(this.isExponent()){
			//logger.info("Potential rational exponent : " + this.getValue());
			return this.getSecondChild().isRational(1, 2);
		}
		return false;
	}
	
	public boolean isCubeRoot() {
		if(this.getArity()!=2) return false;
		if(this.isExponent()){
			return this.getSecondChild().isRational(1, 3);
		}
		return false;
	}
	
	
	/* Cloning method */
	
	public Node clone(){
		Node clone = new Node(this.getValue());
		if(this.isQuantifier()) {
			clone.setBoundVars(this.getBoundVars());
		}
		return clone;
	}
	

}
