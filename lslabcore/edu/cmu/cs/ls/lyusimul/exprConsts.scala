package edu.cmu.cs.ls.lyusimul

import com.wolfram.jlink.Expr

object exprConsts {
  def math_sym(s: String): Expr = 
    new Expr(Expr.SYMBOL, s)

  def math_int(s: String): Expr =
    new Expr(Expr.INTEGER, s)
  
  def math_real(s: String): Expr =
    new Expr(Expr.REAL, s)
  
  def math_str(s: String): Expr =
    new Expr(Expr.STRING, s)

  def math_fn(f: Expr, exprs: Expr*) : Expr = {
    math_fn(f, exprs.toList)
  }
  def math_fn(f: Expr, exprs: List[Expr]) : Expr = {
    new Expr(f,  exprs.toArray)
  }
  def math_fn(f: String, exprs: Expr*) : Expr = {
    math_fn(f, exprs.toList)
  }
  def math_fn(f: String, exprs: List[Expr]) : Expr = {
    new Expr(math_sym(f),  exprs.toArray)
  }
  
  def math_sym_list(exprs: Expr*) : Expr = {
    new Expr(Expr.SYM_LIST, exprs.toList.toArray)
  } 
  
  def math_list(exprs: List[Expr]) : Expr = {
    math_list(exprs:_*)
  }
  def math_list(exprs: Expr*) : Expr = {
    new Expr(Expr.SYM_LIST, exprs.toList.toArray)
  }
  
  val NOT = math_sym("Not");
  val PLUS = math_sym("Plus");
  val MINUS = math_sym("Subtract");
  val MINUSSIGN = math_sym("Minus");
  val MULT = math_sym("Times");
  val DIV = math_sym("Divide");
  val EXP = math_sym("Power");
  val EQUALS = math_sym("Equal");
  val UNEQUAL = math_sym("Unequal");
  val LESS = math_sym("Less");
  val LESS_EQUALS = math_sym("LessEqual");
  val GREATER = math_sym("Greater");
  val GREATER_EQUALS = math_sym("GreaterEqual");
  val INEQUALITY = math_sym("Inequality");
  val FORALL = math_sym("ForAll");
  val EXISTS = math_sym("Exists");
  val AND = math_sym("And");
  val OR = math_sym("Or");
  val IMPL = math_sym("Implies");
  val BIIMPL = math_sym("Equivalent");
  val INVERSE_FUNCTION = math_sym("InverseFunction");
  val INTEGRATE = math_sym("Integrate");
  val RULE = math_sym("Rule");
  val SET = math_sym("Set");
  val DO = math_sym("Do");
  val MODULE = math_sym("Module");
  val COMPOUNDEXPR = math_sym("CompoundExpression");
  val APPEND = math_sym("Append");
  val NDSOLVE = math_sym("NDSolve");
  val EVALUATE = math_sym("Evaluate");
  val LAST = math_sym("Last");
  val FIRST = math_sym("First");
  val IF = math_sym("If");
  val CONTINUE = math_sym("Continue");
}