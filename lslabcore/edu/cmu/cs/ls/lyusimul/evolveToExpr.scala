package edu.cmu.cs.ls.lyusimul

import com.wolfram.jlink._
import edu.cmu.cs.ls._
import edu.cmu.cs.ls.lyusimul._

object EvolveToExpr {
  
    def math_sym(s: String): Expr =
    new Expr(Expr.SYMBOL, s)

  def math_int(s: String): Expr =
    new Expr(Expr.INTEGER, s)

  def un_fun(f: String, arg: Expr): Expr = {
    new Expr(math_sym(f),
             List(arg).toArray)
  }

  def bin_fun(f: String, arg1: Expr, arg2: Expr): Expr = {
    new Expr(math_sym(f),
             List(arg1,arg2).toArray)
  }
  
  def hpToOdesListExpr(hp : HP) : Expr = hp match {
    case Evolve(h, primes @ _ *) => new Expr(math_sym("List"), primsToOdesList(primes).toArray)

    case _ => throw new Exception("not implemented yet")    
  }
  
  // deboHacer: Change name to exprToCsvStri
  def odesListToCsvStri(x : List[Expr]) : String = x match {
    case Nil => ""
    case x :: Nil => x.toString();
    case x :: xs => x.toString() + ", " + odesListToCsvStri(xs)
  } 
    
  def hpToOdesList(hp: HP): List[Expr] = hp match {
    case Evolve(h, primes @ _ *) => primsToOdesList(primes)

    case _ => throw new Exception("not implemented yet")
  }
  
  def primsToOdesList(primes: Seq[(Var,Term)]) : List[Expr] = primes match {
    case Seq(first: (Var, Term), rest @ _ *) => first._1 match {
      case Var(s) => bin_fun("Equal",
          new Expr(new Expr(new Expr(math_sym("Derivative"),
          List(new Expr(1L)).toArray),
          List(math_sym(s)).toArray),
          List(math_sym("t")).toArray),
          MmtManipulation.mmtToExpr(MmtManipulation.termToMmt(first._2)) ) :: primsToOdesList(rest)
      // deboArreglar: don't use mmtToExpr and termToMmt.
      case _ => throw new Exception("Internal Error")
    }
    case _ => Nil
  }
  
  def hpToVarisList(hp: HP): List[Expr] = hp match {
    case Evolve(h, primes @ _ *) => primsToVarisList(primes:_*)
    case _ => throw new Exception("not implemented yet")
  }
  
  def primsToVarisList(primes : (Var, Term)*) : List[Expr] = primes match {
    case Seq((myVar, myTerm), rest @ _ *) => myVar match {
      case Var(s) => math_sym(s) ::primsToVarisList(rest:_*)
      // deboHacer: You need to include what is in the "term" part
      // deboHacer: remove duplicates

      case _ => Nil
    }
  } 
  
  def hpToWhenEvents(hp: HP, tMax : Double): List[Expr] = hp match {
    case Evolve(h, primes @ _ *) => hToWhenEvents(h, tMax)

    case _ => throw new Exception("not implemented yet")
  }
  
  
  def hToWhenEvents(h: Formula, tMax: Double) : List[Expr] = {
    bin_fun("WhenEvent",
        bin_fun("Or", un_fun("Not", formulaToExpr(h)), bin_fun("Greater", math_sym("t"), new Expr(tMax))),
        bin_fun("CompountExpression", bin_fun("Set", math_sym("global`tend"), math_sym("t")), new Expr("StopIntegration"))) :: Nil
  }  

    // deboArreglar: don't use mmtToExpr and termToMmt in this function
  def termToExpr(myTerm: Term) : Expr = {
    MmtManipulation.mmtToExpr(MmtManipulation.termToMmt(myTerm))
  }
  

  def formulaToExpr(frml: Formula) : Expr = frml match {
    case True => math_sym("True")
    case False => math_sym("False")
    case Atom(t: Term) => termToExpr(t)
    case ArithmeticPred(op : Comparison, term1 : Term, term2 : Term) => op match {
      case Equals => bin_fun("Equal", termToExpr(term1), termToExpr(term2)) 
      case NotEquals => bin_fun("Unequal", termToExpr(term1), termToExpr(term2))
      case Less => bin_fun("Less", termToExpr(term1), termToExpr(term2))
      case LessEquals => bin_fun("LessEqual", termToExpr(term1), termToExpr(term2))
      case Greater => bin_fun("Greater", termToExpr(term1), termToExpr(term2))
      case GreaterEquals => bin_fun("GreaterEqual", termToExpr(term1), termToExpr(term2))
      /* deboPreguntar: OK to handle less and lessequals in the same way? */
      case _ => throw new Exception("not implemented... varargs")
    }

    /* deboPreguntar: how to handle "Pred"? */
    case Pred(_, _) => throw new Exception ("not implemented")

    case Prop(c : Connective, f1 : Formula) => c match {
      case Not => un_fun("Not", formulaToExpr(f1))
      case _ => throw new Exception("not implemented yet... varargs")
    }

    case Prop(c : Connective, f1 : Formula, f2 : Formula) => c match {
      case And => bin_fun("And", formulaToExpr(f1), formulaToExpr(f2))
      case Or => bin_fun("Or", formulaToExpr(f1), formulaToExpr(f2))
      case Imp => bin_fun("Implies", formulaToExpr(f1), formulaToExpr(f2))
      case Iff => bin_fun("Equivalent", formulaToExpr(f1), formulaToExpr(f2))
      case _ => throw new Exception("not implemented yet... varargs")
    }

    // deboArreglar: implement these.
//    case Quantifier(k: QuantifierKind, v: String,
//        c: Sort, f: formula) => c match {
//      }
    case _ => throw new Exception("not implemented... varargs")
  }
    
}
