/************************************************************************
 *  Formula converter from KeYmaera syntax to infix TPTP (part of the 
 *  MetiTarski-KeYmera interface).
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

package de.uka.ilkd.key.dl.arithmetics.impl.metitarski;

import java.util.HashSet;
import org.apache.log4j.Logger;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.dl.arithmetics.impl.preprocessor.Operators;
import de.uka.ilkd.key.dl.arithmetics.impl.preprocessor.Tree;

/**
 * This class provides functionality to transform a KeYmaera Term into a mutable binary tree
 * representation, and provides tree restructuring methods needed to create a valid infix
 * TPTP problem for MetiTarski. Some of the methods implemented in this class are, in fact, 
 * optional (e.g. {@link #collapseQuantifiers(Tree)}, but result in a cleaner TPTP problem
 * statement.
 * 
 * @author s0805753@sms.ed.ac.uk.
 * @since 12/02/2012
 */

public class termToMetitConverter {

   private static Logger logger = Logger.getLogger("MetiTarski Converter Logger");
   
   public static String termToMetit(Term form, String name, String comments) {
      Tree formulaTree;

      /* Convert immutable formula into a mutable tree representation      */
      formulaTree = toMutableTree(form);
      
      /* Make sure successive quantifiers of the same kind are compressed into 
       * a single quantifier binding the quantified variables
       * i.e. ![X]: ![Y] will become ![X,Y]:
       * collapseQuantifiers(formulaTree);                                 */
      
      /* Convert exponentials returned by Mathematica into functional notation, 
       * i.e. E^X will become Exp(X)                                       */
      formulaTree = handleMmaExponential(formulaTree);
      
      /* Convert expressions of the form X^(1/2) into sqrt(X)              */
      formulaTree = handleSqrt(formulaTree);
      
      /* Convert bi-implication into a conjunction of implications, i.e.
       * A <=> B will become (A => B) & (B => A)                           */
      formulaTree = handleEquiv(formulaTree);
      
      String[] problem = treeToMetit(formulaTree);

      return formatProblem(comments, name, problem);
   }
   
   /**
    * Compiles an infix TPTP problem given a KeY Term formula and a String label
    * @return String formula in infix TPTP.
    */
   public  String termToMetit(Term form, String name){
      return termToMetit(form,name,"Auto-generated MetiTarski problem");
   }
   
   public static String termToMetit(Term form, boolean findInstance){
      return termToMetit(  form,
                           "Problem" + System.currentTimeMillis(), 
                           "Auto-generated MetiTarski problem");
   }
   
   public static  Tree toMutableTree(Term form){
      if (form.op() == Op.FALSE) {
         logger.info("FALSE");
         return new Tree("FALSUM");
      } 
      else if (form.op() == Op.TRUE) {
         logger.info("TRUE");
         return new Tree("VERUM");
      }
      
   /*  Constant symbols return a 1-element singleton tree with the constant 
    *  symbol as its root/leaf.                                            */
      if(form.arity()==0){
         return new Tree(processSymbol(form.op().name().toString()));
        }
        
   /* Unary predicate symbols, such as ¬, and unary operators, such as -, as 
    * well as first-order quantifiers (∀,∃) return a tree with their respective 
    * symbol as the root, and recurse  on the operand to add children.     */
      else if (form.arity() == 1) {
         Tree tree = new Tree(form.op().name().toString());
         if (form.op() == Op.ALL || form.op() == Op.EX) {
            tree = new Tree(form.op().name().toString(), checkBoundVars(form, 0));
         }
         tree.setLeft(toMutableTree(form.sub(0))); // Recurse.
         return tree;
      }
    
   /* Binary predicate symbols (<,<=,=,>=,>), binary arithmetic operators (*,-,+,/,^),
    * binary logical connectives (∧,∨,→) return a (sub-)tree with their symbol at the root 
    * and recurse on both operands in sequence to compute the child elements. */
        else {
           Tree tree = new Tree((form.op().name().toString()));
           tree.setLeft(toMutableTree(form.sub(0)));  // Recurse on left sub-formula.
           tree.setRight(toMutableTree(form.sub(1))); // Recurse on right sub-formula.
           return tree;
        }
   }
   
   /* Check for variables bound by the quantifier */
   private static HashSet<String> checkBoundVars(Term form, int i) {
      HashSet<String> boundVars = new HashSet<String>();
      for (int j = 0, vbSize = form.varsBoundHere(i).size(); j < vbSize; j++) {
         boundVars.add(processSymbol((form.varsBoundHere(i).get(j).name().toString())));
      }
      return boundVars;
   }
   
   private static String processSymbol(String var){
      /* MetiTarski requirements : variables must be upper-case. */
      // TODO: check for possible variable capture.
      return var  .toUpperCase()   
                  .replaceAll ( "\\$"  ,  "DOLLAR"    )
                  .replaceAll ( "_"    ,  "USCORE"    );
   }
   
   public static String[] treeToMetit(Tree formulaTree){
      
      return new String[]{ convert(formulaTree), Integer.toString(formulaTree.getVars().size())};
   }

   /**
    * Converts mutable tree into infix TPTP by recursively walking the sub-trees
    * and building a translated String representation of every node.
    *
    * @return String representing the formula in infix TPTP syntax.
    */

   static String convert(Tree tree) {
      if(tree.isTrue())
         return Operators.VERUM.Tptp;
      if(tree.isFalse())
         return Operators.FALSUM.Tptp;
      
      switch (tree.getArity()) {

      case 0:
         return tree.getValue();

      case 1:
         if (tree.isQuantifier()) 
         {
            return   Operators.valueOf(tree.getValue()).Tptp 
                     + "["+ tree.printBoundVars() +"] : "
                     + convert(tree.getLeft()) ;
         }
         else
         {
            return   Operators.valueOf(tree.getValue()).Tptp 
                     + "("
                     + convert(tree.getLeft()) 
                     + ")";
         }

      case 2:
         return   "("
                  + convert(tree.getLeft()) 
                  + Operators.valueOf(tree.getValue()).Tptp
                  + convert(tree.getRight())
                  + ")";

      default: 
            logger.error("No provision made for handling nodes with arities greater than 2 ! Children: ");
            return tree.getValue() + "[ARITY "+ tree.getArity() +" ERROR]";
      }
   }
   
   static String formatProblem(String comments, String name, String[] problem){
      StringBuilder output = new StringBuilder();
      
      for(String line: comments.trim().split("\n")){
         output.append("% "+ line + "\n");
      }

      output.append( "% Number of variables: " 
                     + problem[1] 
                     + "\n"  
                     + "fof(" 
                     + name 
                     + ",conjecture, " 
                     + problem[0] 
                     + ").\n" );

      return output.toString();
   }
  
   /*-------------------------[ Tree re-structuring methods ]-------------------------*/
   
   /**
    * Method for converting  bi-implication into a conjunction of implications.
    * <pre>
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
    * </pre>
    *
    * @return restructured Tree
    */ 
   
   private static Tree handleEquiv(Tree tree) {
      if(tree.isBiImplication()) {
         Tree implication1 = new Tree("imp");
         implication1.setLeft(tree.getLeft());
         implication1.setRight(tree.getRight());
         
         Tree implication2 = new Tree("imp");
         implication2.setLeft(tree.getRight());
         implication2.setRight(tree.getLeft());
         
         Tree iff = new Tree("and");
         iff.setLeft ( implication1 );
         iff.setRight( implication2 );
         
         return iff;
      }
      return tree;
   }
   
   /**
    * Method for converting <i>Mathematica™</i>-generated exponentials
    * of the form :
    * <pre>
    *          ^
    *         / \
    *        /   \
    *       E   expr
    * </pre>
    * <p>
    * to their equivalents in functional notation, i.e. 
    * </p>
    * <pre> 
    *       Exp
    *        |
    *       expr
    * </pre>
    * @return restructured Tree
    */
   
   private static Tree handleMmaExponential(Tree tree){   
      if (tree.isMmaExponential()) {
         
         tree.setValue("Exp");      
         tree.pruneLeft();
         
         System.out.println(tree.toString());

      } else {      
         Tree updated = new Tree(tree.getValue());
         updated.addBoundVars(tree.getBoundVars());
         switch (tree.getArity()) {
         
         case 0 : break;
         
         case 1 : 
                  updated.setLeft(handleMmaExponential(tree.getLeft()));
                  return updated;
                  
         case 2 : updated.setLeft   ( handleMmaExponential  ( tree.getLeft()  )  );
                  updated.setRight  ( handleMmaExponential  ( tree.getRight() )  );
                  return updated;
         
         default: break;
         }
      }
      return tree;
   }
   
   private static Tree collapseQuantifiers(Tree tree) {
      System.out.println("Collapsing: "+ tree.toString());
      
      if (   tree.hasParent()  && 
             tree.isQuantifier() && 
             tree.getParent().isQuantifier()
             ) 
      {  /* Found adjacent quantifiers */
         
         if (tree.getValue().equals(tree.getParent().getValue())) 
         {  /* Found adjacent quantifiers of the same type */

            Tree parent = tree.getParent();           
            Tree child  = tree.getLeft();
            
            parent.setLeft(child);
            parent.addBoundVars (tree.getBoundVars() );

            return collapseQuantifiers(parent.getLeft());
         }
      }
      
      Tree updated = new Tree(tree.getValue(), tree.getBoundVars());
      
      switch (tree.getArity()) {
      
      case 0 : break;
      
      case 1 : 
               updated.setLeft   ( collapseQuantifiers   ( tree.getLeft()  )  );
               return updated;
               
      case 2 : updated.setLeft   ( collapseQuantifiers   ( tree.getLeft()  )  );
               updated.setRight  ( collapseQuantifiers   ( tree.getRight() )  );
               return updated;
      
      default: break;
      }
      return tree;
   }
    
   /**
    * Method for converting 1/2 in the exponent to Sqrt of the form :
    * <pre>
    *       ^
    *      / \
    *     a  div
    *        / \
    *       1   2
    * </pre>
    * <p>
    * to :  
    * <pre>
    *       Sqrt
    *         | 
    *         a
    * </pre>
    *
    * @return restructured Tree
    */
   private static Tree handleSqrt(Tree tree) {
      if (tree.isLeaf())
         return tree;
      if (tree.isSquareRoot()) {
          tree.setValue("SQRT");
          tree.pruneRight(); // remove rational number in the exponent
      } 
      
      Tree updated = new Tree(tree.getValue(), tree.getBoundVars());
      
      switch (tree.getArity()) {
      
      case 0 : break;
      
      case 1 : 
               updated.setLeft   ( handleSqrt   ( tree.getLeft()  )  );
               return updated;
               
      case 2 : updated.setLeft   ( handleSqrt   ( tree.getLeft()  )  );
               updated.setRight  ( handleSqrt   ( tree.getRight() )  );
               return updated;
      
      default: break;
      }
      return tree;
   }
   
   /**
    * Method for converting 1/3 in the exponent to Cbrt of the form :
    * <pre>
    *       ^
    *      / \
    *     a  div
    *        / \
    *       1   3
    * </pre>
    * <p>
    * to :  
    * <pre>
    *       Cbrt
    *         | 
    *         a
    * </pre>
    *
    * @return restructured Tree
    */
   static Tree handleCbrt(Tree tree) {
      if (tree.isLeaf())
         return tree;
      if (tree.isCubeRoot()) {
          tree.setValue("CBRT");
          tree.pruneRight(); 
      } 
      
      Tree updated = new Tree(tree.getValue(), tree.getBoundVars());
      
      switch (tree.getArity()) {
      
      case 0 : break;
      
      case 1 : 
               updated.setLeft   ( handleCbrt   ( tree.getLeft()  )  );
               return updated;
               
      case 2 : updated.setLeft   ( handleCbrt   ( tree.getLeft()  )  );
               updated.setRight  ( handleCbrt   ( tree.getRight() )  );
               return updated;
      
      default: break;
      }
      return tree;
   }
}
