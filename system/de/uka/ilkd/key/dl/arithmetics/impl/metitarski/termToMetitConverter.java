package de.uka.ilkd.key.dl.arithmetics.impl.metitarski;

import java.util.TreeSet;
import org.apache.log4j.Logger;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.dl.arithmetics.impl.preprocessor.Node;
import de.uka.ilkd.key.dl.arithmetics.impl.preprocessor.Operators;
import de.uka.ilkd.key.dl.arithmetics.impl.preprocessor.Tree;

/**
 * 
 * @author s0805753@sms.ed.ac.uk.
 * @since 12/02/2012
 * 
 * last modified: 23/05/2012
 *
 */

public class termToMetitConverter {

	private static Logger logger = Logger.getLogger("MetiTarski Converter Logger");
	
	public static String termToMetit(Term form, String name, String comments) {
		Tree formulaTree;

		// Convert immutable formula into a mutable tree representation
		formulaTree = toMutableTree(form);
		
		// Make sure successive quantifiers of the same kind are compressed into 
		// a single quantifier binding the quantified variables
		// i.e. ![X]: ![Y] will become ![X,Y]:
		collapseQuantifiers(formulaTree);
		
		// Convert exponentials returned by Mathematica into functional notation.
		// i.e. E^X will become Exp(X)
		handleAltExp(formulaTree);
		
		// Convert expressions of the form X^(1/2) into sqrt(X)
		handleSqrt(formulaTree);
		
		// Convert bi-implication into a conjunction of implications, i.e.
		// A <=> B will become (A => B) & (B => A)
		handleBiImplication(formulaTree);
		
		String[] problem = treeToMetit(formulaTree);

		return formatProblem(comments, name, problem);
	}
	
	public static String termToMetit(Term form, String name){
		return termToMetit(form,name,"Auto-generated MetiTarski problem");
	}
	
	public static String termToMetit(Term form, boolean findInstance){
		return termToMetit(form, "Problem"+System.currentTimeMillis(), "Auto-generated MetiTarski problem");
	}
	
	public static  Tree toMutableTree(Term form){
		
		if (form.op() == Op.FALSE) {
			logger.info("falsity");
			return new Tree(new Node("FALSUM"));
		} 
		else if (form.op() == Op.TRUE) {
			logger.info("truth");
			return new Tree(new Node("VERUM"));
		}
		
	/*  Constant symbols return a 1-element singleton tree,
	 *  with the constant symbol as its root/leaf. 
	 */
		if(form.arity()==0){
			return new Tree(new Node(processSymbol(form.op().name().toString())));
		  }
		  
	/*	Unary predicate symbols, such as ¬, and unary operators, such as -  
	 * 	as well as first-order quantifiers (∀,∃),
	 * 	return a tree with their respective symbol as the root, and recurse 
	 *	on the operand to add children.
	 */
		else if (form.arity() == 1) {
			Tree tree = new Tree(new Node(form.op().name().toString()));
			if (form.op() == Op.ALL || form.op() == Op.EX) {
				tree.getRootNode().addBoundVars(checkBoundVars(form, 0)); // Add variable(s) to the bound variable set.
			}
			tree.insertNodeInto(toMutableTree(form.sub(0)).getRootNode(),tree.getRootNode(), 0); // Recurse.
			return tree;
		}
	 
	/*	Binary predicate symbols (<,<=,=,>=,>), binary arithmetic operators (*,-,+,/,^),
	 * 	binary logical connectives (∧,∨,→)
	 *  return a (sub-)tree with their symbol at the root, and recurse on both operands
	 *  in sequence to compute the child elements.
	 */
		  else {
			  Tree tree = new Tree(new Node(form.op().name().toString()));
			  for(int i=0; i< form.arity(); i++){
			  tree.insertNodeInto(toMutableTree(form.sub(i)).getRootNode(), tree.getRootNode(),i);	
			  }
			  return tree;
		  }
	}
	
	/* Check for variables bound by the quantifier */
	private static TreeSet<String> checkBoundVars(Term form, int i) {
		TreeSet<String> boundVars = new TreeSet<String>();
		for (int j = 0, vbSize = form.varsBoundHere(i).size(); j < vbSize; j++) {
     		boundVars.add(processSymbol((form.varsBoundHere(i).get(j).name().toString())));
		}
		return boundVars;
	}
	
	private static String processSymbol(String var){
		// MetiTarski requirements : variables must be upper-case.
		// TODO: check for possible variable capture.
		return var.toUpperCase().replaceAll("\\$", "DOLLAR").replaceAll("_", "USCORE");
	}
	
	public static String[] treeToMetit(Tree formulaTree){
		Node root  =  formulaTree.getRootNode();
		return new String[]{ convert(root), Integer.toString(Tree.getVars(formulaTree).size())};
	}
	
	static String convert(Node node) {
		if(node.isVerum()){return Operators.VERUM.Tptp;}
		if(node.isFalsum()){return Operators.FALSUM.Tptp;}
		
		switch (node.getArity()) {
		case 0:
			return node.getValue();
		case 1:
			if (node.isQuantifier()) {
				return Operators.valueOf(node.getValue()).Tptp 
						+ "["+ node.printBoundVars() +"] : "
						+ convert(node.getFirstChild()) ;
			}
			else{
			return Operators.valueOf(node.getValue()).Tptp 
					+ "("
					+ convert(node.getChildAt(0)) 
					+ ")";
			}
		case 2:
			return "("+convert(node.getFirstChild()) 
					+ Operators.valueOf(node.getValue()).Tptp 
					+ convert(node.getSecondChild()) 
					+ ")";
		default: logger.error("No provision made for handling nodes with arities greater than 2 ! Children: ");
					for(int i=0; i<node.getArity(); i++){
						logger.info(node.getValue());
					}
		  		return node.getValue() + "[ARITY "+ node.getArity() +" ERROR]";
		}
	}
	
	static String formatProblem(String comments, String name, String[] problem){
		StringBuilder output = new StringBuilder();
		
		for(String line: comments.trim().split("\n")){
			output.append("% "+ line + "\n");
		}
		output.append("% Number of variables: "+ problem[1]);
		output.append("\n");
		output.append("fof("+name+",conjecture, "+ problem[0] +").\n");
		return output.toString();
	}
	
	/******** Tree restructuring methods ********/
	
	static boolean isAltExp(Node node) {
		// Alternative exponential notation in Mathematica™
		if (node.isLeaf())
			return false;
		if (node.getValue() != null && node.getValue().equals("exp")
				&& node.getFirstChild().getValue().equals("E"))
			return true;
		return false;
	}
	
	
	static Tree handleSqrt(Tree formulaTree){
		/* Method for converting 1/2 in the exponent to Sqrt
		 * of the form :
		 *       ^
		 *      / \
		 *     a  div
		 *        / \
		 *       1   2
		 * 
		 * to :  
		 *       Sqrt
		 *         | 
		 *         a
		 *   
		 */
		
		handleSqrt(formulaTree.getRootNode());
		return formulaTree;
	}
	
	static boolean handleSqrt(Node node) {
		if (node.isLeaf())
			return false;
		if (node.isSquareRoot()) {
			node.setValue("SQRT");
			node.removeChildAt(1); // remove rational number in the exponent
		} else {
			for (int i = 0; i < node.getArity(); i++) {
				handleSqrt(node.getChildAt(i));
			}
		}
		return false;
	}
	
	
	static Tree handleAltExp(Tree formulaTree){
		/* Method for converting Mathematica™-generated exponentials
		 * of the form :
		 *       ^
		 *     /   \
		 *    E    expr
		 * 
		 * to their equivalents in functional notation, i.e.  
		 * 
		 *       Exp
		 *        |
		 *       expr
		 *   
		 */
		
		handleAltExp(formulaTree.getRootNode());
		return formulaTree;
	}
	
	static boolean handleAltExp(Node node) {
		if (node.isLeaf())
			return false;
		if (isAltExp(node)) {
			node.setValue("Exp");
			node.removeFirstChild();
		} else {
			for (int i = 0; i < node.getArity(); i++) {
				handleAltExp(node.getChildAt(i));
			}
		}
		return false;
	}
	
	static Tree handleBiImplication(Tree formulaTree){
		/**
		 * Method for converting  bi-implication into a conjunction of implications.
		 * 
		 *          ↔
		 *         / \
		 *        A   B
		 * 
		 *          ∧
		 *         / \
		 *        /   \
		 *       /     \
		 *      →       → 
		 *     / \     / \
		 *    A   B   B   A
		 */ 
	
		handleBiImplication(formulaTree.getRootNode());
		return formulaTree;
	}
	
	static boolean handleBiImplication(Node node){
		if (node.isLeaf())
			return false;
		if (node.getValue().equals("equiv")) {
			// Create conjunct
			node.setValue("and");
			
			//First pair
			Tree A1 = Tree.cloneSubTree(node.getFirstChild());
			Tree B1 = Tree.cloneSubTree(node.getSecondChild());
			
			//Second pair
			Tree A2 = Tree.cloneSubTree(node.getFirstChild());
			Tree B2 = Tree.cloneSubTree(node.getSecondChild());
			
			node.removeAllChildren();
			
			// Form first implication
			node.addChildAt(0, new Node("imp"));
			//logger.info("Added first child arity: "+ node.getArity());
			node.getFirstChild().addChildAt(0, A1.getRootNode());
			node.getFirstChild().addChildAt(1, B1.getRootNode());
			//logger.info("Added  clones: "+ node.getArity());
			
			// Form second implication
			node.addChildAt(1, new Node("imp"));
			//logger.info("Added second child arity: "+ node.getArity());
			node.getSecondChild().addChildAt(0, B2.getRootNode());
			node.getSecondChild().addChildAt(1, A2.getRootNode());
			//logger.info("Added clones: "+ node.getArity());	
		}
		for (int i = 0; i < node.getArity(); i++) {
				handleBiImplication(node.getChildAt(i));
			}
		return true;
	}
	
	public static boolean collapseQuantifiers(Tree formulaTree){
		collapseQuantifiers(formulaTree.getRootNode());
		return true;
	}
	
	public static boolean collapseQuantifiers(Node node) {

		if (!node.hasNoParent() && node.isQuantifier()
				&& node.getParent().isQuantifier()) {
			if (node.getValue().equals(node.getParent().getValue())) {
				Node parent = node.getParent();
				Node child = node.getFirstChild();
				parent.removeChildAt(0); // remove subtree
				node.removeFirstChild();
				parent.addChildAt(0, child);
				parent.addBoundVars(node.getBoundVars());
				collapseQuantifiers(parent);
				return true;
			} else {
				for (Node child : node.getChildren()) {
					collapseQuantifiers(child);
					return false;
				}
			}
		}
		for (Node child : node.getChildren()) {
			collapseQuantifiers(child);
			return false;
		}

		return false;
	}

}
