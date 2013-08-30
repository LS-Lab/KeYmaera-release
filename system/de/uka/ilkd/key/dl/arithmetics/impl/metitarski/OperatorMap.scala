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

/**
 * Singleton class holding the mappings of Operator names
 * to their corresponding solver syntax.
 *
 * @author  s0805753@sms.ed.ac.uk
 * @since   03/04/2013
 */

object OperatorMap {

  val (utf8, metit, qepcad, smt) = (0,1,2,3)

      /* Term ->         UTF-8  , MetiTarski,  QEPCAD,    SMTLIB */
    
  val logicalConstant: Map[String, List[String]] = Map(      
      /* Constants */
      "true"    ->  List( "⊤"   , "$true"   , "[0 = 0]" ),
      "false"   ->  List( "⊥"   , "$false"  , "[0 = 1]" )
      )
      
  val logicalConnective: Map[String, List[String]] = Map(         
      /* Boolean operators */
      "not"   ->  List( " ¬ "   , " ~"      , "~"       ),
      "and"   ->  List( " ∧ "   , " & "     , "/\\"     ),
      "or"    ->  List( " ∨ "   , " | "     , "\\/"     ),
      "imp"   ->  List( " → "   , " => "    , "==>"     ),
      "equiv" ->  List( " ↔ "   , "equiv"   , "<==>"    )
      )
      
 val quantifier: Map[String, List[String]] = Map(    
      /* Quantifiers */
      "all"   ->  List( "∀ "    , "!"       , "A"       ),
      "exist" ->  List( "∃ "    , "?"       , "E"       )
      )
      
 val relationalSymbol: Map[String, List[String]] = Map(    
      /* Relational symbols */
      "equals"->  List( "="     , "="     , "="         ),
      "geq"   ->  List( "≥"     , ">="    , ">="        ),
      "neq"   ->  List( "≠"     , "!="    , "/="        ),
      "leq"   ->  List( "≤"     , "<="    , "<="        ),
      "lt"    ->  List( "<"     , "<"     , "<"         ),
      "gt"    ->  List( ">"     , ">"     , ">"         )
      )

  val arithmeticOperator: Map[String, List[String]] = Map(    
      /* Arithmetic operators */
      "neg"   ->  List( "-"     , "-"     , "-"         ),
      "exp"   ->  List( "^"     , "^"     , "^"         ),
      "mul"   ->  List( "·"     , "*"     , " "         ),
      "div"   ->  List( "÷"     , "/"     , " / "       ),
      "add"   ->  List( "+"     , "+"     , " + "       ),
      "sub"   ->  List( "-"     , "-"     , " - "       )
      )
         
  val specialFunction: Map[String, List[String]] = Map(    
      /* Special functions */
      "Log"   ->  List( "Log"   , "ln"    , "ln"        ),
      "Exp"   ->  List( "Exp"   , "exp"   , "exp"       ),
      
      "Sin"   ->  List( "Sin"   , "sin"   ,  "sin"      ),
      "Cos"   ->  List( "Cos"   , "cos"   ,  "cos"      ),
      "Tan"   ->  List( "Tan"   , "tan"   ,  "tan"      ),
      
      "ArcSin"->  List( "ArcSin"  , "arcsin"  ,   "arcsin"  ),
      "ArcCos"->  List( "ArcCos"  , "arccos"  ,   "arccos"  ),
      "ArcTan"->  List( "ArcTan"  , "arctan"  ,   "arctan"  ),
      
      "Sinh"  ->  List( "Sinh"  , "sinh"  ,   "sinh"  ),
      "Cosh"  ->  List( "Cosh"  , "cosh"  ,   "cosh"  ),
      "Tanh"  ->  List( "Tanh"  , "tanh"  ,   "tanh"  ),
      
      "Sqrt"    ->  List( "√"   , "sqrt"  ,   "sqrt"  ),
      "CubeRoot"->  List( "∛"   , "cbrt"  ,   "cbrt"  ),
      
      "Abs"     ->  List( "Abs" , "abs"   ,   "abs"   ),
      "Min"     ->  List( "Min" , "min"   ,   "min"   ),
      "Max"     ->  List( "Max" , "max"   ,   "max"   ),
      
      "Pi"     	->  List( "π" 	, "pi"    ,   "pi"    )
  )
   
  val mapOps = ( 
           logicalConstant        ++ 
           logicalConnective      ++ 
           quantifier             ++
           relationalSymbol       ++ 
           arithmeticOperator     ++
           specialFunction  
           )
    
  def isLogicalConst(x: String) = 
    OperatorMap.logicalConstant.get(x) match {
    case None     => false
    case Some(s)  => true
  }

  def isLogicalConnective(x: String) = 
    OperatorMap.logicalConnective.get(x) match {
    case None     => false
    case Some(s)  => true
  }

  def isQuantifier(x: String) = 
    OperatorMap.quantifier.get(x) match {
    case None     => false
    case Some(s)  => true
  }

  def isRelationalSymbol(x: String) = 
    OperatorMap.relationalSymbol.get(x) match {
    case None     => false
    case Some(s)  => true
  }

  def isArithmeticOperator(x: String) = 
    OperatorMap.arithmeticOperator.get(x) match {
    case None     => false
    case Some(s)  => true
  }

  def isSpecialFunction(x: String) = 
    OperatorMap.specialFunction.get(x) match {
    case None     => false
    case Some(s)  => true
  }    
  
  def isMinMax(x: String) = x match{
    case "Min" => true
    case "Max" => true
    case  _    => false
  }
}
