/************************************************************************
 *  KeYmaera-MetiTarski interface.
 *  Copyright (C) 2012  s0805753@sms.ed.ac.uk University of Edinburgh.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 *  
 ************************************************************************/

package de.uka.ilkd.key.dl.arithmetics.impl.preprocessor;

import java.util.HashSet;
import org.apache.log4j.*;

/**
 * This class provides a mutable tree implementation that is used
 * in the process of converting KeY Terms into the infix TPTP syntax
 * used by the MetiTarski theorem prover. 
 * <p>
 * The constructor requires a String object which is interpreted
 * as being at the value at the root of the tree. 
 * </p>
 * 
 * @author s0805753@sms.ed.ac.uk
 * @version 2.1.0
 * @since 10/02/2012
 */

public class Tree {

   private static Logger logger = Logger.getLogger("BinaryTreeLogger");
   
   /* Set Log4j level to INFO for bug-bashing */
   Level logLevel = Level.ERROR;
   
   private String root;
   private HashSet<String> boundVars = new HashSet<String>();
   
   /* Left and right sub-trees */
   private Tree left;
   private Tree right;
   
   /* Parent root */
   private Tree parent;
   
   public Tree(String rootValue) {
      logger.setLevel(logLevel);
      this.root = rootValue;
   }
   
   public Tree(String rootValue, HashSet<String> boundVars) {
      logger.setLevel(logLevel);
      this.root = rootValue;
      this.boundVars = boundVars;
   }

/*------------------------- Basic binary tree functionality -------------------------*/
   
   /**
    * Determine if a given Tree is equal this Tree.
    * <p>
    * N.B. This method checks for object equality.<br>
    * Object reference equality is available through the {@code ==} operator.
    * </p>
    * @param   tree : Tree
    * @return  true if the node is the same object as the root.
    */
   
   boolean equals(Tree tree) {
      if (this == tree) 
         return true;
      if (this.isEmpty()   && !tree.isEmpty())
         return false;
      if (!this.isEmpty()  &&  tree.isEmpty())
         return false;      
      if (!this.getValue().equals(tree.getValue()))
         return false;
      if (this.isLeaf())
         return true;    
      if (!(this.hasLeft() && tree.hasLeft()))
         return false;
      if (!(this.hasRight() &&  tree.hasRight()))
         return false;
      
      return ( this.getLeft()   .equals(tree.getLeft()) && 
               this.getRight()  .equals(tree.getRight()) );
   }
  
   /**
    * Check whether the Node object occurs anywhere in the binary tree.
    * <p>
    * N.B. This method recurses on the binary tree and checks for 
    * reference equality. 
    * </p>
    * @param   value : String
    * @return  true if the Node object appears anywhere along the binary tree.
    */
   
   public boolean containsValue(String value) {
      if ( value.equals(this.getValue()) )
         return true;
      
      if ( this.isLeaf() )
         return false;     
      
      if ( (this.getLeft()).containsValue(value) ) 
         return true;
 
      if ( null == this.getRight() )
         return false;
      
      if ( this.getRight().containsValue(value) ) 
         return true;
      
      return false;
   }
   
   /**
    * This method determines whether there is a sub-tree that occurs in the Tree. 
    * <p>
    * N.B. The sub-tree is tested using object equality, <b>not</b> reference equality.
    * </p>
    * @param subTree
    * @return true if a sub-tree was found
    */
   
   public boolean constainsSubTree(Tree subTree) {
      if(this.equals(subTree))
         return true;
      
      switch (this.getArity()) {
      
      case 0 : break;
      
      case 1 : return this.getLeft().constainsSubTree(subTree);
               
      case 2 : return ( this.getLeft() .constainsSubTree( subTree ) ||
                        this.getRight().constainsSubTree( subTree )  );
               
      default: logger.error("No provision made for arity > 2.");
               break;
      }
      
      return false;
   }

   /**
    * Check if the tree is an empty tree, i.e. if the there is
    * no root element.
    * @return true if the root is null.
    */
   
   public boolean isEmpty() {
      if ( null == this.getValue()) return true;
      return false;
   }
   
   /**
    * Determine if the Tree is a leaf, i.e. if it has no sub-trees.
    * @return true if the Tree is a leaf.
    */
   
   public boolean isLeaf() {
      if (this.isEmpty())
         return true;
      if (  null == this.getLeft()    && 
            null == this.getRight()   )
         return true;
      return false;
   }
   
   /**
    * Determine if the Tree has a left sub-tree.
    * @return true if the Tree is a leaf.
    */
   
   private boolean hasLeft() {
      if( null == this.left) 
         return false;
      return true;
   }
   
   /**
    * Determine if the Tree has a right sub-tree.
    * @return true if the Tree is a leaf.
    */
   
   private boolean hasRight() {
      if( null == this.right) 
         return false;
      return true;
   }
   
   /**
    * Determine if the Tree is sub-tree.
    * @return true if the Tree is a leaf.
    */
   
   public boolean hasParent() {
      if( null == this.parent) 
         return false;
      return true;
   }
   
   /*-------------------------[ Getter methods ]-------------------------*/
   
   /**
    * Get the tree root String value.
    * @return root : String
    */
   
   public String getValue() {
      return this.root;
   }
   
   /**
    * Get sub-tree parent node
    * @return
    */
   
   public Tree getParent() {
      return this.parent;
   }
   
   /**
    * Compute the number of direct descendants of this tree.
    * <p>
    * N.B. In this implementation the arity can only be 0,1 or 2.
    * </p>
    * @return int Tree arity.
    */
   
   public int getArity() {
      int arity=0;
      if( this.isEmpty()  || 
          this.isLeaf() )   return arity;
      if(this.hasLeft() )   arity++;
      if(this.hasRight())   arity++;
      return arity;
   }
   
   /**
    * Fetch left sub-tree <u>if it exists</u>.
    * <p>
    * N.B. if there is no left sub-tree, {@code null} is returned.
    * </p>
    * @return Tree left sub-tree.
    */
   
   public Tree getLeft() {
      if(this.hasLeft()) {
         return this.left;
      }
      return null;
   }
   
   /**
    * Fetch right sub-tree <u>if it exists</u>.
    * <p>
    * N.B. if there is no left sub-tree, {@code null} is returned.
    * </p>
    * @return Tree left sub-tree.
    */
   
   public Tree getRight() {
      if(this.hasRight()) {
         return this.right;
      }
      return null;
   }
   
   /*-------------------------[ Setter methods ]-------------------------*/
   
   /**
    * Assign a new String object to be the root value of the tree.
    * <p>
    * N.B. When applying this method, you retain the previously defined 
    * tree structure such as the sub-trees and a parent. The link to the
    * parent can only be removed while inside the parent tree by calling
    * {@link #pruneLeft()}/{@link #pruneRight()}.
    * </p>
    * @param   newRoot Node object to serve as the new tree root element.
    * @return  true if the root object has been updated.
    */
   
   public boolean setValue(String newRoot) {
      if ( null == newRoot )
         return false;
      this.root = newRoot;
      return true;
   }
   
   /**
    * Assign a left sub-tree to the current Tree root.
    * @param left: Tree
    * @return true if the assignment is non-null.
    */
   
   public boolean setLeft(Tree left) {
      if (null == left) return false;
      this.left=left;
      this.getLeft().setParent(this);
      return true;
   }

   /**
    * Assign a right sub-tree to the current Tree root.
    * @param left: Tree
    * @return true if the assignment is non-null.
    */
   
   public boolean setRight(Tree right) {
     if (null == right) return false;
     this.right=right;
     this.getRight().setParent(this);
     return true;     
   }
   
   /**
    * Method for linking the subtree to its parent.
    * <p>
    * N.B. this method should <b>never</b> be used for anything other
    * than supplementing the {@link #setLeft(Tree)}/{@link #setRight(Tree)} method 
    * calls.
    * </p>
    * @param  newParent : Tree
    * @return true upon success
    */
   
   private boolean setParent(Tree newParent) {
      if (newParent == null)
         return false;
      this.parent = newParent;
      return true;
   }

   /**
    * Set subtree parent to {@code null}.
    * <p>
    * N.B. This method must <b>only</b> be used to supplement the
    * {@link #pruneLeft()}/{@link #pruneRight()} methods.
    * </p>
    * 
    * @return true upon success
    */
   
   private boolean resetParent() {
      if( null == this.parent)
         return false;
      this.parent = null;
      return true;
   }
   
   /**
    * Method for pruning the left sub-tree.
    * <p>
    * N.B. When you prune the left node, the right node is <b>automatically</b>
    * shifted to the left.
    * </p>
    * @return true upon success.
    */
   
   public boolean pruneLeft() {
      if(!this.hasLeft())
         return false;
      
      this.getLeft().resetParent();
      if(this.hasRight()) {      
         /* Shift right node to the left */
         this.setLeft(this.right);
         this.right=null;
      }
      else {
         this.left=null;
      }
      return true;
   }
   
   /**
    * Method for pruning the right sub-tree.
    * @return true upon success.
    */
   
   public boolean pruneRight() {
      if(!this.hasRight())
         return false;
      this.getRight().resetParent();
      this.right=null;
      return true;
   }

   /*-------------------------[ Tree matching classifiers ]-------------------------*/

   public boolean isBiImplication(){
      if( this.getArity() != 2 ) 
         return false;
      if( this.getValue().equals("equiv") ) 
         return true;
      return false;
   }
   
   boolean isExponent(){
      if( this.getArity() != 2 ) 
         return false;
      if( this.getValue().equals("exp") ) 
         return true;
      return false;
   }
   
   public boolean isTrue(){
      if( this.getValue().equals("VERUM") ) 
         return true;
      return false;
   }
   
   public boolean isFalse(){
      if( this.getValue().equals("FALSUM") ) 
         return true;
      return false;
   }
   
   boolean isVariable() {
      if (  this.getArity() !=  0  || 
            this.isTrue()          || 
            this.isFalse()         || 
            this.getValue().matches("[0-9]+(\\.[0-9]+)?") )  
         return false;
      return true;
   }
   
   public boolean isQuantifier() {
      if ( this.getArity() != 1 )
         return false;
      if ( this.getValue().equals( Operators.all  .KeY  ) || 
           this.getValue().equals( Operators.exist.KeY) ) 
         return true;
      return false;
   }

   boolean isUniversalQuantifier() {
      if ( this.getArity() != 1 ) 
         return false;
      if ( this.getValue().equals( Operators.all.KeY ) )
         return true;
      return false;
   }
   
   boolean isExistentialQuantifier() {
      if ( this.getArity() != 1 )
         return false;
      if (  this.getValue().equals( Operators.exist.KeY ) ) 
         return true;
      return false;
   }

   /**
    * Determine whether the node is a binary arithmetic predicate symbol.
    * @return true if the node is an arithmetic predicate.
    */
   
   boolean isArithmeticPredicate() {
      if ( this.getArity() != 2 )
         return false;
      Operators operator = Operators.valueOf( this.getValue() );
      if ( operator.negatePredicate().equals( this.getValue() ))
         return false;
      return true;
   }
   
   /**
    * Determine if the Tree represents the alternative exponential function 
    * notation obtained from  <i>Mathematica™</i>.
    * @return true upon success.
    */
   
   public boolean isMmaExponential() {
      if (this.isLeaf())
         return false;
      if (  null != this.getValue()       && 
            this.getValue().equals("exp") && 
            this.getLeft().getValue().equals("E")  )
         return true;
      return false;
   }
   
   
   /* Methods for finding square/cube roots */

   public boolean isRational(){
      return this.isRational(" ", " ");
   }
   
   public boolean isRational(long nominator, long denominator){
      return this.isRational( String.valueOf( nominator  ),
                              String.valueOf( denominator)  );
   }
   
   private boolean isRational(String nominator, String denominator){
      
      if(this.getArity()   !=2   ) 
         return false;
      
      if(Operators.valueOf( this.getValue() ) != Operators.div) 
         return false;
      
      if(!this.getLeft().isLeaf() || !this.getRight().isLeaf())
         return false;

      String nominatorPattern    = (nominator   .matches("[0-9]+"))? nominator   : "[0-9]+" ;
      String denominatorPattern  = (denominator .matches("[0-9]+"))? denominator : "[0-9]+" ;

      if(   this.getLeft() .getValue().matches( nominatorPattern     )  &&
            this.getRight().getValue().matches( denominatorPattern   )   )
      {
         logger.info( "Found rational exponent " + nominator + "/" + denominator );
         return true;
      }
      logger.info( "No rational exponent " + nominator + "/" + denominator );
      return false;
   }
   
   public boolean isSquareRoot() {
      if( this.getArity()     !=2  )  return false;
      if( this.isExponent()        )  return this.getRight().isRational(1, 2);
      return false;
   }
   
   public boolean isCubeRoot() {
      if( this.getArity()    != 2  )  return false;
      if( this.isExponent()        )  return this.getRight().isRational(1, 3);
      return false;
   }
   
//TODO: remove this; no longer required.
//
//   /**
//    * Compile a Set of Nodes that represent the arithmetic predicate symbols in the formula.
//    * <p>
//    * N.B. In this implementation all arithmetic predicates are binary relations.<br>
//    * The symbols are: { <, ≤, =, ≠, ≥, > }<br>
//    * The method produces the set by recursing on all the sub-trees.
//    * </p>
//    * @return HashSet of Nodes that represent the predicates in atomic (sub-)formulae.
//    */
//   
//   public HashSet<Tree> getArithmeticPredicates() {
//      return getArithmeticPredicates(this);
//   }
//
//   private HashSet<Tree> getArithmeticPredicates(Tree tree) {
//      
//      HashSet<Tree> terms = new HashSet<Tree>();
//
//      if (tree.isLeaf())
//         return terms;
//
//      if (tree.isArithmeticPredicate()) {
//         logger.info("Found binary predicate " + tree.getValue());
//         terms.add(tree);
//         return terms;
//      }
//
//      logger.info("Recursing");
//      switch (tree.getArity()) {
//      case 0 : break;
//      
//      case 1 : terms.addAll( getArithmeticPredicates( tree.getLeft() ) );
//               break;
//      
//      case 2 : terms.addAll( getArithmeticPredicates( tree.getLeft() ) );
//               terms.addAll( getArithmeticPredicates( tree.getRight()) );
//               break;
//      default: logger.error("Unable to handle arity > 2! ");
//               break;
//      }
//      return terms;
//   }
//   
//   /**
//    * This method traces back up the tree until it finds an atomic formula with
//    * an arithmetic predicate symbol at the root of the subtree.
//    * 
//    * @param node
//    * @return
//    * @throws NullPointerException
//    */
//   
//   public static Tree getAtom(Tree tree) 
//         throws NullPointerException {
//      
//      if (tree.isArithmeticPredicate())
//         return tree;
//      
//      if (!tree.hasParent()) {
//         return getAtom(tree.getParent());
//      }
//      
//      logger.error("The node" + toUnicode(tree)
//            + " represents an arithmetic expression rather than a term!");
//      
//      return null;
//   }
   
   /*-------------------------[ Variable handling ]-------------------------*/

   /**
    * Compile a Set of Strings representing variable names occurring in the tree.
    * <p>
    * N.B. Variables have arity 0 and cannot begin with a numeric value.
    * </p>
    * @return HashSet of variable names occurring in the formula.
    */
   
   public  HashSet<String> getVars() {
      HashSet<String> output = getVars(this);
      return output;
   }

   private static HashSet<String> getVars(Tree tree) {    
      HashSet<String> vars = new HashSet<String>();       
      if (tree.isVariable()) 
      {
         vars.add(tree.getValue());
         return vars;
      }       
      switch (tree.getArity()) {
      
      case 0 : break;
      
      case 1 : vars.addAll( getVars( tree.getLeft()  )   );
               break;
               
      case 2 : vars.addAll( getVars( tree.getLeft()  )   );
               vars.addAll( getVars( tree.getRight() )   );
               break;
               
      default: logger.error("No provision made for arity > 2.");
               break;
      }
      
      return vars;
   }

   public boolean addBoundVars(String var){
      if( null == var ) return false;
      if(boundVars==null) boundVars = new HashSet<String>(); 
      this.boundVars.add(var);
      return true;
   }
   
   public boolean addBoundVars(HashSet<String> vars){
      if( null == vars ) return false;
      if(boundVars==null) boundVars = new HashSet<String>(); 
      boundVars.addAll(vars);
      return true;
   }
   
   /**
    * Compute a Set of Strings representing the bound variables in the formula.
    * <p>
    * N.B. A variable is considered bound if it is within the scope of a quantifier.
    * In this implementation this translates to the quantifier Nodes binding the 
    * variable occurring above the variable occurrence in the tree.
    * </p> <p>
    * N.B. This set includes the names of variables bound by both ∀ and ∃.
    * </p>
    * @return HashSet of Strings containing all bound variables names.
    */
   
   public  HashSet<String> getBoundVars() {
      HashSet<String> output = getBoundVars(this);
      return output;
   }

   private  HashSet<String> getBoundVars(Tree tree) {     
      HashSet<String> vars = new HashSet<String>();     
      if (tree.isQuantifier() ) 
      {
         return this.boundVars;
      }     
      switch (tree.getArity()) {
      
      case 0 : break;
      
      case 1 : vars.addAll( getBoundVars( tree.getLeft()  )   );
               break;
               
      case 2 : vars.addAll( getBoundVars( tree.getLeft()  )   );
               vars.addAll( getBoundVars( tree.getRight() )   );
               break;
               
      default: logger.error("No provision made for arity > 2.");
               break;
      }
      
      return vars;
   }
   
   /**
    * Compile a Set of Strings representing the bound variables in the formula
    * that are found to be within the scope of a universal quantifier.
    * 
    * @return HashSet of Strings containing variable names bound by ∀.
    */

   public HashSet<String> getBoundUniversal() {
      HashSet<String> output = getBoundUniversal(this);
      for (String var : output) {
         logger.info("Variable explicitly bound by an ∀ quantifier: " + var);
      }
      return output;
   }
   
   private HashSet<String> getBoundUniversal(Tree tree) {
      HashSet<String> universalVars = new HashSet<String>();
      if (tree.isUniversalQuantifier()) {
         universalVars.addAll(tree.getBoundVars());
      }
      switch (tree.getArity()) {
      
      case 0 : break;
      
      case 1 : universalVars.addAll( getBoundUniversal( tree.getLeft()  )   );
               break;
               
      case 2 : universalVars.addAll( getBoundUniversal( tree.getLeft()  )   );
               universalVars.addAll( getBoundUniversal( tree.getRight() )   );
               break;
               
      default: logger.error("No provision made for arity > 2.");
               break;
      }

      return universalVars;
   }
   
   /**
    * Compile a Set of Strings containing the names of variables within the
    * scope of an existential quantifier.
    * 
    * @return HashSet of Strings containing variable names bound by ∃.
    */
   
   public HashSet<String> getBoundExistential() {
      HashSet<String> output = getBoundExistential(this);
      for (String var : output) {
         logger.info("Variable explicitly bound by an ∃ quantifier: " + var);
      }
      return output;
   }
   
   private HashSet<String> getBoundExistential(Tree tree) {
      HashSet<String> existentialVars = new HashSet<String>();
      if (tree.isExistentialQuantifier()) {
         existentialVars.addAll(tree.getBoundVars());
      }
      switch (tree.getArity()) {
      
      case 0 : break;
      
      case 1 : existentialVars.addAll( getBoundUniversal( tree.getLeft()  )   );
               break;
               
      case 2 : existentialVars.addAll( getBoundUniversal( tree.getLeft()  )   );
               existentialVars.addAll( getBoundUniversal( tree.getRight() )   );
               break;
               
      default: logger.error("No provision made for arity > 2.");
               break;
      }

      return existentialVars;
   }
   
   /**
    * Compile a Set of Strings representing the free variables occurring 
    * in the formula tree.
    * <p>
    * N.B. Free variables are those not within the scope of any quantifier.
    * Here we simply take the set difference of all variables and the bound
    * variables.
    * </p>
    * @return HashSet of Strings containing the names of all the free variables.
    */
   
   public HashSet<String> getFreeVars() {
      HashSet<String> freeVars = this.getVars();
      freeVars.removeAll(this.getBoundVars());
      return freeVars;
   }
   
   /*-------------------------[ Overriding inherited methods ]-------------------------*/

   /**
    * This method creates a new Tree object which represents the tree that
    * clones the current root value and the values of its sub-trees.
    * <p>
    * N.B. This method proceeds by recursing on the sub-trees. The result is a Tree
    * which is equal in terms of values and structure, but is stored in a
    * different object and so is not equal when considering object references.
    * </p>
    * @return Tree object clone of the original.
    */

   @Override
   public Tree clone() {
      return clone(this);
   }
   
   private Tree clone(Tree tree) {
      Tree subTree = new Tree(tree.getValue());
      
      switch (tree.getArity()) {
      
      case 0 : break;
      
      case 1 : subTree.setLeft   (tree.getLeft()   .clone() );
               break;
               
      case 2 : subTree.setLeft   (tree.getLeft()   .clone() );
               subTree.setRight  (tree.getRight()  .clone() );
               break;
               
      default: logger.error("No provision made for arity > 2.");
               break;
      }
      
      return subTree;
   }
   
   /**
    * Prints out the bound variables if the tree root is a quantifier.
    * @return String of variables bound by the quantifier
    */
   
   public String printBoundVars(){
      StringBuilder output = new StringBuilder();
      if ( boundVars == null      ||
          !this.isQuantifier()    ) 
         return "";

      for(String boundVar: boundVars){
         output.append(boundVar + " ");
      }
      return output.toString().trim().replaceAll(" ", ", ");
   }
   
   /**
    * Method for converting a (sub-tree) into its Unicode String representation.
    * 
    * @param root Node
    * @return String representing the formula tree in Unicode.
    */

   private static String toUnicode(Tree tree) {
      if (tree == null || tree.isEmpty())
         return "";
      switch (tree.getArity()) {
      case 0:
         return tree.getValue();
      case 1:
         if (tree.isQuantifier()) {
            return Operators.valueOf(tree.getValue()).utf + " "
                  + tree.printBoundVars() + ". "
                  + toUnicode(tree.getLeft());
         } else {
            return Operators.valueOf(tree.getValue()).utf
                  + toUnicode(tree.getLeft()) ;
         }
      case 2:
         return "(" + toUnicode(tree.getLeft())
               + Operators.valueOf(tree.getValue()).utf
               + toUnicode(tree.getRight()) + ")";
      default:
         logger.error("No provision made for handling nodes with arity greater than 2 !");
         return tree.getValue() + "[ARITY > 2 ERROR]";
      }
   }

   /**
    * Printing method produces a Unicode text representation of the formula tree.
    */

   @Override
   public String toString() {
      return toUnicode(this);
   }
}