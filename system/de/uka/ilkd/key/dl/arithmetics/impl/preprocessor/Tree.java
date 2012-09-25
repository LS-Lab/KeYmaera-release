package de.uka.ilkd.key.dl.arithmetics.impl.preprocessor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import org.apache.log4j.Logger;

/**
 * @author s0805753@sms.ed.ac.uk
 * @since 10/02/2012
 * 
 */

public class Tree {

    private static Logger logger = Logger.getLogger("TreeLogger");

    private Node root;
    
    public Tree(Node root) {
	this.root = root;
    }

    boolean isRoot(Node node) {
	if (this.root == node)
	    return true;
	return false;
    }

    boolean contains(Node node) {
	return contains(node, this.root);

    }

    private boolean contains(Node node, Node root) {
	if (node.equals(root))
	    return true;
	boolean foundInstance = false;
	for (Node i : root.getChildren()) {
	    foundInstance = contains(node, i);
	}
	return foundInstance;
    }

    boolean isEmpty() {
	if (root == null)
	    return true;
	return false;
    }

    boolean setRoot(Node newRoot) {
	if (newRoot == null)
	    return false;
	this.root = newRoot;
	return true;
    }

    int getChildCount(Node parent) {
	if (contains(parent)) {
	    return parent.getArity();
	}
	logger.error("The tree does not contain node " + parent.getValue());
	return 0;
    }

    Node getChild(Node parent, int index) {
	Node child = new Node();
	if (contains(parent) && parent.getArity() >= index + 1) {
	    child = parent.getChildAt(index);
	    return child;
	}
	if (parent.getArity() <= index) {
	    logger.error("Incorrect child index " + index + "! Node "
		    + parent.getValue() + " has arity " + parent.getArity()
		    + ".");
	    return child;
	}
	logger.error("The tree does not contain node " + parent.getValue());
	return child;
    }

    public Node getRootNode() {
	return this.root;
    }

    // Method for generating a set of (pointers to) atomic formulæ featuring
    // binary predicates.

    public HashSet<Node> getBinaryPredicates() {
	return getBinaryPredicates(this.getRootNode());
    }

    private HashSet<Node> getBinaryPredicates(Node node) {
	HashSet<Node> terms = new HashSet<Node>();

	if (node.isLeaf())
	    return terms;

	if (node.isBinaryPredicate()) {
	    logger.info("Found binary predicate " + node.getValue());
	    terms.add(node);
	    return terms;
	}

	for (int i = 0; i < node.getArity(); i++) {
	    logger.info("Recursing");
	    terms.addAll(getBinaryPredicates(node.getChildAt(i)));
	}

	return terms;
    }

    /* Methods for finding free and bound variables */

    public static TreeSet<String> getVars(Tree tree) {
	// Variables have arity 0 and non-numeric values
	TreeSet<String> output = getVars(tree.getRootNode());
	for (String var : output) {
	    //logger.info("Var: " + var);
	}
	return output;
    }

    private static TreeSet<String> getVars(Node node) {
	/* Quantified variables stored for logging purposes */
	TreeSet<String> vars = new TreeSet<String>();
	TreeSet<String> ExistentialVars = new TreeSet<String>();
	TreeSet<String> UniversalVars = new TreeSet<String>();

	if (node.isVariable()) {
	    vars.add(node.getValue());
	    return vars;
	}
	for (int i = 0; i < node.getArity(); i++) {
	    vars.addAll(getVars(node.getChildAt(i)));
	}
	return vars;
    }

    public static TreeSet<String> getBoundVars(Tree tree) {
	TreeSet<String> output = getBoundVars(tree.getRootNode());
	for (String var : output) {
//	    logger.info("Bound var: " + var);
	}
	return output;
    }

    public static TreeSet<String> getBoundVars(Node node) {
	TreeSet<String> vars = new TreeSet<String>();
	if (node.isQuantifier()) {
	    vars.addAll(node.getBoundVars());
	}
	for (int i = 0; i < node.getArity(); i++) {
	    vars.addAll(getBoundVars(node.getChildAt(i)));
	}
	return vars;
    }

    public static TreeSet<String> getFreeVars(Tree tree) {
	TreeSet<String> freeVars = getVars(tree);
	freeVars.removeAll(getBoundVars(tree));
	for (String var : freeVars) {
	    //logger.info("Free var: " + var);
	}
	return freeVars;
    }

    /* Method returning the term predicate symbol */

    public static Node getTermPredicate(Node node) {
	if (node.isBinaryPredicate())
	    return node;
	if (!node.hasNoParent()) {
	    return getTermPredicate(node.getParent());
	}
	logger.error("The node" + toUnicode(node)
		+ " represents an expression rather than a term!");
	return new Node("NO PREDICATE");
    }

    public void insertNodeInto(Node newNode, Node parentNode, int index) {
	parentNode.addChildAt(index, newNode);
    }

    public void replaceChildNode(Node newNode, Node parentNode, int index) {
	parentNode.replaceChildAt(index, newNode);
    }

    /* Cloning method */

    public static Tree cloneSubTree(Node node) {
	Tree subTree = new Tree(node.clone());
	for (int i = 0; i < node.getArity(); i++) {
	    subTree.insertNodeInto(cloneSubTree(node.getChildAt(i))
		    .getRootNode(), subTree.getRootNode(), i);
	}
	return subTree;
    }

    /* Printing methods */

    @Override
    public String toString() {
	return toUnicode(getRootNode());
    }

    private static String toUnicode(Node node) {
	if (node == null | node.isEmpty())
	    return "";
	switch (node.getArity()) {
	case 0:
	    return node.getValue();
	case 1:
	    if (node.isQuantifier()) {
		return Operators.valueOf(node.getValue()).utf + " "
			+ node.printBoundVars() + ". "
			+ toUnicode(node.getFirstChild());
	    } else {
		return Operators.valueOf(node.getValue()).utf + "("
			+ toUnicode(node.getFirstChild()) + ")";
	    }
	case 2:
	    return "(" + toUnicode(node.getFirstChild())
		    + Operators.valueOf(node.getValue()).utf
		    + toUnicode(node.getSecondChild()) + ")";
	default:
	    logger.error("No provision made for handling nodes with arities greater than 2 !");
	    return node.getValue() + "[ARITY > 2 ERROR]";
	}
    }

    /* Auxiliary logging functionality; added Wed, 18 Apr 2012, 00:32:07 */

    public HashMap<String, Integer> computeStatistics(){
      HashMap<String, Integer> stats = new HashMap<String,Integer>();
     // String problemClass = "";
      stats.put("Number of variables", getVars(this).size());
      stats.put("Number of free variables", (getFreeVars(this).size()));
      stats.put("Number of variables bound by ∀", getBoundUniversal(this).size());
      stats.put("Number of variables bound by ∃", getBoundExistential(this).size());
//      if(getBoundUniversal(this).size()==0 && getBoundExistential(this).size()==0){
//	  problemClass = "(Explicitly) Quantifier-Free";
//      }
//      else if(getBoundUniversal(this).size()>0 && getBoundExistential(this).size()==0){
//	  problemClass = "(Explicitly Purely) Universally Quantified";
//      }
//      else if(getBoundUniversal(this).size()==0 && getBoundExistential(this).size()>0){
//	  problemClass = "(Explicitly Purely) Existentially Quantified";
//      }
//      else if(getBoundUniversal(this).size()>0 && getBoundExistential(this).size()>0){
//	  problemClass = "Mixed Quantifiers";
//      }
//      stats.put("Class of problem", problemClass);
      return stats;

    }

    /* Universal quantifiers : Wed, 18 Apr 2012, 00:26:47 */

    public TreeSet<String> getBoundUniversal(Node node) {
	TreeSet<String> UniversalVars=new TreeSet<String>();
	if (node.isUniversalQuantifier()) {
	    UniversalVars.addAll(node.getBoundVars());
	}
	for (int i = 0; i < node.getArity(); i++) {
	    UniversalVars.addAll(getBoundUniversal(node.getChildAt(i)));
	}
	return UniversalVars;
    }

    public TreeSet<String> getBoundUniversal(Tree tree) {
	TreeSet<String> output = getBoundUniversal(tree.getRootNode());
	for (String var : output) {
	    logger.info("Variable explicitly bound by an ∀ quantifier: " + var);
	}
	return output;
    }

    /* Existential quantifiers : Wed, 18 Apr 2012, 00:48:01 */

    public TreeSet<String> getBoundExistential(Node node) {
	TreeSet<String> ExistentialVars=new TreeSet<String>();
	if (node.isExistentialQuantifier()) {
	    ExistentialVars.addAll(node.getBoundVars());
	}
	for (int i = 0; i < node.getArity(); i++) {
	    ExistentialVars.addAll(getBoundExistential(node.getChildAt(i)));
	}
	return ExistentialVars;
    }

    public TreeSet<String> getBoundExistential(Tree tree) {
	TreeSet<String> output = getBoundExistential(tree.getRootNode());
	for (String var : output) {
	    logger.info("Variable explicitly bound by an ∃ quantifier: " + var);
	}
	return output;
    }

}
