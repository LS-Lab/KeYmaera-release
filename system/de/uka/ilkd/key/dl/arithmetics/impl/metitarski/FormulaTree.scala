/************************************************************************
 *  Formula processing for the KeYmaera-MetiTarski interface. 
 *  Copyright (C) 2013  s0805753@sms.ed.ac.uk
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
package de.uka.ilkd.key.dl.arithmetics.impl.metitarski
import de.uka.ilkd.key.logic.Term
import de.uka.ilkd.key.logic.op.Op
import de.uka.ilkd.key.logic.op.QuantifiableVariable
import de.uka.ilkd.key.logic.op.Quantifier

/**
 * Immutable tree implementation using case classes.
 * 
 */
sealed abstract trait ImmutableTree{ 
 
 /* MetiTarski TPTP translation */
    
  def opMap(x:String) = OperatorMap.mapOps.get(x) match {
    case None => x
    case Some(s:List[String]) => s(OperatorMap.metit)
  }
   
  def toMetitFormula(): String = this match{
    
    case Node(op) => op
    
    case Quant(quant, boundVars, subTree) => {
      opMap(quant) + "[" + boundVars.mkString(",") + "] : " + 
      subTree.toMetitFormula()
    }
    
    case UnaryOp(op, subTree) => {
      opMap(op) + "(" + subTree.toMetitFormula() + ")"
    }
    
    case BinaryOp(op, left, right) => {
      "(" + left.toMetitFormula + opMap(op) + right.toMetitFormula + ")"
    }
    
  }
}

/* Case classes for building the formula AST */

case class Node(
    name    :String
    )   extends ImmutableTree

case class Quant(
    name    :String, 
    vars    :Set[String], 
    subTree :ImmutableTree
    ) extends ImmutableTree
    
case class UnaryOp(
    name    :String, 
    subTree :ImmutableTree
    )   extends ImmutableTree

case class BinaryOp(
    name    :String, 
    left    :ImmutableTree, 
    right   :ImmutableTree
    )   extends ImmutableTree
  
/**
 *  <p> A FormulaTree object is constructed from a KeY Term; 
 *  this builds an ImmutableTree representing the original Term and 
 *  provides methods for restructuring the AST in order to make it 
 *  conform to certain MetiTarski conventions.</p> 
 *  <p>N.B. The <i>only</i> 
 */  
    
class FormulaTree(term:Term) {
  
  /* Keeping track of variables and their binding in the formula */
  private var vars            = Set[String]()
  private var quantifiedVars  = Set[String]()
  
  /* Compute free variables */
  private def freeVars        = vars.diff(quantifiedVars)
  
  private val tree = termToTree(term)
  
  def formatMetitProblem(): String = {
    
    val metitProblem = {
     universalClosure(
        collapseQuantifiers(
            expandEquivalence(
                convertCubeRoot(
                    convertSqrt(
                        convertMathematicaExp(tree)
                        ) ) ) ) ).toMetitFormula
     }
   
    val numberOfVars = vars.size
    
    "% Auto-generated MetiTarski problem\n"
    "% Number of variables: " + numberOfVars + "\n"+ 
    "fof(KeYmaera,conjecture, " + metitProblem + ").\n"
  }
  
  /**
   * Creates an ImmutableTree representation of the original KeY
   * Term. 
   */
  private def termToTree(form:Term):ImmutableTree = {
    
    def opString(term:Term): String = term.op().name().toString()
    
    form.arity() match {
    
    case 0 => { 
      if (form.op().isInstanceOf[QuantifiableVariable]){
        vars += processSymbol( opString(form) ) 
      }  
      else{
        println(opString(form) + " is not a QuantifianleVariable")
      }
       Node( processSymbol( opString(form) ) )
     }
    
    case 1 if 
    (form.op().isInstanceOf[Quantifier]) => {
      for (v <- checkBoundVars(form,0) ){ quantifiedVars += v }
      Quant(
        opString(form),
        checkBoundVars(form,0),
        termToTree(form.sub(0))
        ) 
    }
    
    case 1 => UnaryOp(
        opString(form), 
        termToTree(form.sub(0))
        )
    
    case 2 => BinaryOp(
        opString(form), 
        termToTree(form.sub(0)), 
        termToTree(form.sub(1))
        ) 
    
    case _ => Node("ERROR! Unsupported Operator.")
    }
  
  }
 /** Check for variables bound by the quantifier */
 private def checkBoundVars(form:Term, i:Int):Set[String] = {
      var boundVars:Set[String] = Set()
      for( j <- 0 to form.varsBoundHere(i).size()) {
         boundVars +=
         ( processSymbol( (form.varsBoundHere(i).get(j).name().toString()) ) )
      }
      boundVars
   }

 /** Replace illegal characters */
 private def processSymbol(symb:String):String = {
      /* MetiTarski requirements : variables must be upper-case. */
      symb .toUpperCase()   
                  .replaceAll ( "\\$"  ,  "DOLLAR"    )
                  .replaceAll ( "_"    ,  "USCORE"    )
   }

 /* Tree re-structuring methods ---------------------------------------------*/

 /* Constants for pattern matching convenience */

  private val EQUIV = Op.EQV .toString()
  private val AND   = Op.AND .toString()
  private val OR    = Op.OR  .toString()
  private val IMP   = Op.IMP .toString()
  private val NOT   = Op.NOT .toString()
  private val ALL   = Op.ALL .toString()
  
  private val POW   = "exp"
  private val DIV   = "div"
  private val E     = "E"
  private val EXP   = "Exp"
  private val SQRT  = "Sqrt"
  private val CBRT  = "CubeRoot"
    
  private val HALF  = BinaryOp(DIV,Node("1"),Node("2"))
  private val THIRD = BinaryOp(DIV,Node("1"),Node("3"))
 
   /**
    * Method for computing the universal closure of a first-order formula.
    * <pre>
    *    ∀ [<free variables>]
    *    |
    *    .
    *   ...
    * </pre>         
    * @param  tree          : ImmutableTree
    * @return restructured  : ImmutableTree
    */ 
   def universalClosure(original: ImmutableTree): ImmutableTree = {
      Quant(ALL, freeVars, original)
  }  
  
   /**
    * Method for converting  equivalence into a conjunction of implications.
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
    * @param  tree          : ImmutableTree
    * @return restructured  : ImmutableTree
    */ 
   def expandEquivalence(original: ImmutableTree): ImmutableTree =
     original match{
    
    case BinaryOp(EQUIV, a, b) =>
      BinaryOp(
          AND, 
          BinaryOp(IMP, a, b ), 
          BinaryOp(IMP, b, a )
          )  
      
      case Quant(quant, vars, subTree) => 
        Quant(quant, vars, expandEquivalence(subTree))
                 
      case BinaryOp(op,left,right) => 
        BinaryOp( op, expandEquivalence(left), expandEquivalence(right) )
        
      case UnaryOp(op,subTree) => UnaryOp( op, expandEquivalence(subTree) )
      
      case x => x
      
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
    * @param  tree          : ImmutableTree
    * @return restructured  : ImmutableTree
    */
  def convertMathematicaExp(original:ImmutableTree): ImmutableTree =
    original match{
    
    case BinaryOp(POW, Node(E), expr) => 
      UnaryOp(EXP, convertMathematicaExp(expr))
      
      case Quant(quant, vars, subTree) => 
        Quant(quant, vars, convertMathematicaExp(subTree))
                 
      case BinaryOp(op,left,right) => 
        BinaryOp( op, convertMathematicaExp(left), convertMathematicaExp(right) )
        
      case UnaryOp(op,subTree) => UnaryOp( op, convertMathematicaExp(subTree) )
      
      case x => x
      
  }
  
   /**
    * Method for reducing the order of a tree with adjacent quantifiers of 
    * the same type. The method works by adding the bound variables of the 
    * quantifier at the lower level to its set of bound variables and then 
    * removing one quantifier from the tree, while inheriting its sub-
    * tree, e.g.
    * <pre>
    *    ∀ [x]
    *    |
    *    ∀ [y]
    *    |
    *    .
    *   ...
    * </pre>
    * is converted to
    * <pre>
    *    ∀ [x,y]
    *    |
    *    .
    *   ...
    * </pre>
    * N.B. This method can significantly reduce the size of generated TPTP
    * output if the problem is given in Prenex normal form and has a large 
    * number of explicitly quantified variables, e.g. a universal closure.
    * @param  tree          : ImmutableTree
    * @return restructured  : ImmutableTree
    */  
  def collapseQuantifiers(original: ImmutableTree): ImmutableTree =
    original match{
    
      case Quant(quant1, vars1, Quant(quant2, vars2, subTree) ) 
           if (quant1 == quant2) => { 
         Quant( quant1, vars1.union(vars2), collapseQuantifiers(subTree) ) 
         }
           
      case BinaryOp(op,left,right) => 
        BinaryOp( op, collapseQuantifiers(left), collapseQuantifiers(right) )
        
      case UnaryOp(op,subTree) => UnaryOp( op, collapseQuantifiers(subTree) )
      
      case x => x
      
    }
  
   /**
    * Method for converting 1/2 in the exponent to a <i>square root</i> 
    * of the form :
    * <pre>
    *       ^
    *      / \
    *     a   ÷
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
    * @param  tree          : ImmutableTree
    * @return restructured  : ImmutableTree
    */   
  def convertSqrt(original:ImmutableTree): ImmutableTree = {
    original match {
      case BinaryOp(POW,a,HALF) => UnaryOp(SQRT, convertSqrt(a))
      
      case Quant(quant, vars, subTree) => 
        Quant(quant, vars, convertSqrt(subTree))
                 
      case BinaryOp(op,left,right) => 
        BinaryOp( op, convertSqrt(left), convertSqrt(right) )
        
      case UnaryOp(op,subTree) => UnaryOp( op, convertSqrt(subTree) )
      
      case x => x
    }
  }

   /**
    * Method for converting 1/3 in the exponent to a <i>cube root</i> 
    * of the form :
    * <pre>
    *       ^
    *      / \
    *     a   ÷
    *        / \
    *       1   3
    * </pre>
    * <p>
    * to :  
    * <pre>
    *       CubeRoot
    *         | 
    *         a
    * </pre>
    * @param  tree          : ImmutableTree
    * @return restructured  : ImmutableTree
    */
  def convertCubeRoot(original: ImmutableTree): ImmutableTree = 
    original match {
    
      case BinaryOp(POW,a,THIRD) => UnaryOp(CBRT, convertCubeRoot(a))
      
      case Quant(quant, vars, subTree) => 
        Quant(quant, vars, convertCubeRoot(subTree))
                 
      case BinaryOp(op,left,right) => 
        BinaryOp( op, convertCubeRoot(left), convertCubeRoot(right) )
        
      case UnaryOp(op,subTree) => UnaryOp( op, convertCubeRoot(subTree) )
      
      case x => x
    }
}