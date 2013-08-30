/************************************************************************
 *  KeYmaera-MetiTarski interface.
 *  Copyright (C) 2013  s0805753@sms.ed.ac.uk, University of Edinburgh.
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
import de.uka.ilkd.key.logic.op.LogicVariable
import de.uka.ilkd.key.logic.op.Metavariable
import de.uka.ilkd.key.logic.op.ProgramVariable
import de.uka.ilkd.key.logic.op.Function

/**
 * Immutable AST tree implementation using case classes.
 * An ImmutableTree is built from a KeY Term, which can then
 * be re-structured and converted into a String representation
 * of some back-end solver syntax (e.g. MetiTarski).
 *
 * @author  s0805753@sms.ed.ac.uk
 * @since   03/04/2013
 */
sealed abstract trait ImmutableTree{ 
 
 /* MetiTarski TPTP translation methods */
    
  /**
   * Shortcut for translating the KeY operator into the solver's
   * syntax. The mappings are stored in @see OperatorMap
   */
  def opMap(x:String) = OperatorMap.mapOps.get(x) match {
    case None => x
    case Some(s:List[String]) => s(OperatorMap.metit)
  }
   
  /**
   * Convert the AST into infix TPTP for MetiTarski.
   */
  def toMetitFormula(): String = this match{
    
    case Node(op) => opMap(op)
    
    case Quant(quant, boundVars, subTree) => {
      opMap(quant) + "[" + boundVars.mkString(",") + "] : " + 
      subTree.toMetitFormula()
    }
    
    /* Clearer MetiTarski syntax */  
    case UnaryOp(op, subTree) if (
         OperatorMap.isArithmeticOperator( op ) ||
         OperatorMap.isLogicalConnective(  op ) )=> {
      opMap(op) + subTree.toMetitFormula() 
    }
    
    case UnaryOp(op, subTree) => {
      opMap(op) +"("+ subTree.toMetitFormula() +")"
    }
    
    /* Min/Max functions */
    case BinaryOp(op, left, right) if (
        OperatorMap.isMinMax( op ))=> {
        opMap(op) + "(" + left.toMetitFormula +","+ right.toMetitFormula + ")"
    }
        
    case BinaryOp(op, left, right) => {
      "(" + left.toMetitFormula + opMap(op) + right.toMetitFormula + ")"
    }
    
  }
}

/* Case classes for building the formula AST */

case class Node(
    name    :String
    ) extends ImmutableTree

case class Quant(
    name    :String, 
    vars    :Set[String], 
    subTree :ImmutableTree
    ) extends ImmutableTree
    
case class UnaryOp(
    name    :String, 
    subTree :ImmutableTree
    ) extends ImmutableTree

case class BinaryOp(
    name    :String, 
    left    :ImmutableTree, 
    right   :ImmutableTree
    ) extends ImmutableTree
  
/**
 *  <p>A FormulaTree object is constructed from a KeY Term; 
 *  this builds an ImmutableTree representing the original Term and 
 *  provides methods for restructuring the AST in order to make it 
 *  conform to MetiTarski conventions.
 *  </p> 
 *  <p>N.B. The <i>only</i> publicly visible method in this class is  
 *  @see #formatMetitProblem().</p>
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
                        convertInverseHyperbolics(
                            convertMathematicaExp(tree)
                            ) ) ) ) ) ).toMetitFormula
     }
   
    val numberOfVars = vars.size
    
    "% Auto-generated MetiTarski problem.\n" +
    "% Number of variables: "   + numberOfVars + "\n"+ 
    "fof(KeYmaera,conjecture, " + metitProblem + ").\n"
  }
  
  /**
   * Creates an ImmutableTree representation of the original KeY Term.
   * <i>N.B. Variable lists are populated when this method is called.</i> 
   * @param    form    :Term
   * @return   tree    :ImmutableTree
   */
  private def termToTree(form:Term):ImmutableTree = {
    
    /* Obtain a String representation of the Term's Operator. */
    def opString(term:Term): String = term.op().name().toString()
    
    /* Determine whether the Term is a variable */
    // TODO: This is ugly. Could implement with just a regexp & arity check.
    def isVariable(term:Term): Boolean = {    
      if( form.op().isInstanceOf[ LogicVariable   ] ||
          form.op().isInstanceOf[ ProgramVariable ] ||
          form.op().isInstanceOf[ Metavariable    ] ||
          /* Constants must be treated as real-valued variables */
          ( form.op().isInstanceOf[ Function ] && 
              form.arity()==0 &&
              !opString(form).matches("-?\\d.*") &&
              /* Mathematica constants are not variables */
              !opString(form).equals("E") &&
              !opString(form).equals("Pi")   
          )
      ) true
      else 
        false
    }
    
    form.arity() match {
    
    case 0 => { 
      if (isVariable(form)){
        vars += processSymbol( opString(form) ) 
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
      for( j <- 0 until form.varsBoundHere(i).size()) {
         boundVars +=
         ( processSymbol( (form.varsBoundHere(i).get(j).name().toString()) ) )
      }
      boundVars
   }

 /** Replace illegal characters */
 private def processSymbol(symb:String):String = symb match { 
   
   /* Leave Pi unchanged */
   case "Pi" => "Pi"
     
   /* MetiTarski requirements : variables must be upper-case. */
   case name =>  {name.toUpperCase()   
                  .replaceAll ( "\\$"  ,  "DOLLAR"    )
                  .replaceAll ( "_"    ,  "USCORE"    )}
   }

/***************************************************************************/
/*                        Tree re-structuring methods                      */
/***************************************************************************/

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
   
  private val ACOSH  = "ArcCosh"
  private val ASINH  = "ArcSinh"
  private val ATANH  = "ArcTanh"
    
  private val COSH  = "Cosh"
  private val SINH  = "Sinh"
  private val TANH  = "Tanh"
    
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
    
    if(freeVars.isEmpty) 
      original
    else 
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
          BinaryOp(IMP, expandEquivalence(a), expandEquivalence(b) ), 
          BinaryOp(IMP, expandEquivalence(b), expandEquivalence(a) )
          )  
                       
      case BinaryOp(op,left,right) => 
        BinaryOp( op, expandEquivalence(left), expandEquivalence(right) )
        
      case Quant(quant, vars, subTree) => 
        Quant(quant, vars, expandEquivalence(subTree))

      case UnaryOp(op,subTree) => UnaryOp( op, expandEquivalence(subTree) )
      
      case Node(x) => Node(x)
      
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
      
      case BinaryOp(op,left,right) => 
        BinaryOp(op, convertMathematicaExp(left), convertMathematicaExp(right))      
     
      case Quant(quant, vars, subTree) => 
        Quant(quant, vars, convertMathematicaExp(subTree))

      case UnaryOp(op,subTree) => UnaryOp( op, convertMathematicaExp(subTree) )
      
      case Node(x) => Node(x)    
      
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
         collapseQuantifiers(Quant( quant1, vars1.union(vars2), subTree) ) 
         }

      case Quant(quant, vars, subTree) => 
        Quant(quant,vars, collapseQuantifiers(subTree))
           
      case BinaryOp(op,left,right) => 
        BinaryOp( op, collapseQuantifiers(left), collapseQuantifiers(right) )
        
      case UnaryOp(op,subTree) => UnaryOp( op, collapseQuantifiers(subTree) )
      
      case Node(x) => Node(x)
      
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
                           
      case BinaryOp(op,left,right) => 
        BinaryOp( op, convertSqrt(left), convertSqrt(right) )
      
      case Quant(quant, vars, subTree) => 
        Quant(quant, vars, convertSqrt(subTree))
       
      case UnaryOp(op,subTree) => UnaryOp( op, convertSqrt(subTree) )
      
      case Node(x) => Node(x)
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
                 
      case BinaryOp(op,left,right) => 
        BinaryOp( op, convertCubeRoot(left), convertCubeRoot(right) )
        
      case Quant(quant, vars, subTree) => 
        Quant(quant, vars, convertCubeRoot(subTree))
     
      case UnaryOp(op,subTree) => UnaryOp( op, convertCubeRoot(subTree) )
      
      case Node(x) => Node(x)
    }

   /**
    * Method for converting inverse <i>Mathematica™</i> hyperbolic functions. 
    * of the form, e.g. :
    * <pre>
    *       ArcCosh
    *         |
    *         a
    * </pre>
    * <p>
    * to :  
    * <pre>
    *         ÷
    *        / \
    *       1  Cosh
    *            |
    *            a
    * </pre>
    * @param  tree          : ImmutableTree
    * @return restructured  : ImmutableTree
    */
  def convertInverseHyperbolics(original: ImmutableTree): ImmutableTree = 
    original match {
                 
      case BinaryOp(op,left,right) => BinaryOp( 
            op, 
            convertInverseHyperbolics(left), 
            convertInverseHyperbolics(right) 
            )
        
      case Quant(quant, vars, subTree) => 
        Quant(quant, vars, convertInverseHyperbolics(subTree))
        
      case UnaryOp(ASINH ,subTree) => BinaryOp(
                      DIV, 
          Node("1"),        UnaryOp( SINH, convertInverseHyperbolics(subTree) )
          )
          
      case UnaryOp(ACOSH ,subTree) => BinaryOp(
                      DIV, 
          Node("1"),        UnaryOp( COSH, convertInverseHyperbolics(subTree) )
          )
          
      case UnaryOp(ATANH ,subTree) => BinaryOp(
                      DIV, 
          Node("1"),        UnaryOp( TANH, convertInverseHyperbolics(subTree) )
          )
     
      case UnaryOp(op,subTree) => UnaryOp(
                        op, 
          convertInverseHyperbolics(subTree) 
          )
      
      case Node(x) => Node(x)
    }
}
