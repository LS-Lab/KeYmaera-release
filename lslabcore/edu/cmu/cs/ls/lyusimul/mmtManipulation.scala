package edu.cmu.cs.ls.lyusimul

import edu.cmu.cs.ls._
import com.wolfram.jlink._
import NameMasker._
import hpToExpr._

object MmtManipulation {

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
  
  def mul_arg_fun(f: String, lst: List[Expr]) : Expr = {
    new Expr(math_sym(f),
             lst.toArray)
  }
  
  // This function does not technically belong in this file, but I put it in this file
  // because it is closely related to the functions termToMmt() and mmtToExpr().
  def termToExpr(myTerm: Term, tranMode : Int) : Expr = myTerm match {
    case Num(Exact.Integer(n)) => math_int(n.toString)
    case Num(Exact.Rational(p,q)) => bin_fun("Divide",
                                     math_int(p.toString),
                                     math_int(q.toString))
                                     
    // deboAcordarme: I am not 100% comfortable with using mmtVarToExpr here
    case Var(s: String) => mmtVarToExpr(s, tranMode)
    case Arithmetic(op, terms @ _*) => op match {
      
      case Negate => un_fun("Minus", termToExpr(terms.head, tranMode))
      case Plus => mul_arg_fun("Plus", terms.toList.map((t: Term) => termToExpr(t, tranMode)))
      case Subtract => mul_arg_fun("Subtract", terms.toList.map((t: Term) => termToExpr(t, tranMode)))
      case Multiply => mul_arg_fun("Times", terms.toList.map((t: Term) => termToExpr(t, tranMode)))
      case Divide => mul_arg_fun("Divide", terms.toList.map((t: Term) => termToExpr(t, tranMode)))
      case Power => mul_arg_fun("Power", terms.toList.map((t: Term) => termToExpr(t, tranMode)))
      case Modulo => mul_arg_fun("Mod", terms.toList.map((t: Term) => termToExpr(t, tranMode)))
      
      case _ => throw new Exception(op.toString() + " not implemented... varargs")
    }

    // deboPreguntar: how to handle varargs???
    case Arithmetic(_, _) => throw new Exception("not implemented... varargs")
    /* deboPreguntar: function name by Fn may be different from function name in
     Mathematica. How to handle this? */
    case Fn(f: String, ps @ _*) => {
      if (f.equals("Abs")) {
        return mul_arg_fun("Abs", ps.toList.map((t: Term) => termToExpr(t, tranMode)))
      } else {
    	throw new Exception("Not implemented: Fn(\"" + f + "\", ***)")        
      }
    }

  }

  def mmtToExpr(mmt: Mmt, tranMode : Int): Expr = mmt match {
    // Note: variable x is treated as a function x with argument t
    // i.e. x => x[t]
    case MmtVar(x) => mmtVarToExpr(x, tranMode)

    case MmtNum(Exact.Integer(n)) => math_int(n.toString)
    case MmtNum(Exact.Rational(p,q)) => bin_fun("Divide",
                                       math_int(p.toString),
                                       math_int(q.toString))
    case MmtPosInfty() => throw new Exception ("not implemented yet")
    case MmtNegInfty() => throw new Exception ("not implemented yet")

    case MmtArithmetic(op: MmtArithmeticOp, mmt1: Mmt) => op match {
      case MmtNegate => un_fun("Minus", mmtToExpr(mmt1, tranMode))
      case _ => throw new Error ("either has varargs")
    }

    case MmtArithmetic(op: MmtArithmeticOp, mmt1: Mmt, mmt2 : Mmt) => op match {
      case MmtPlus => bin_fun("Plus", mmtToExpr(mmt1, tranMode), mmtToExpr(mmt2, tranMode))
      case MmtSubtract => bin_fun("Subtract", mmtToExpr(mmt1, tranMode), mmtToExpr(mmt2, tranMode))
      case MmtMultiply => bin_fun("Times", mmtToExpr(mmt1, tranMode), mmtToExpr(mmt2, tranMode))
      case MmtDivide => bin_fun("Divide", mmtToExpr(mmt1, tranMode), mmtToExpr(mmt2, tranMode))
      case MmtPower => bin_fun("Power", mmtToExpr(mmt1, tranMode), mmtToExpr(mmt2, tranMode))
      case MmtModulo => bin_fun("Mod", mmtToExpr(mmt1, tranMode), mmtToExpr(mmt2, tranMode))
      case _ => throw new Error ("either has varargs")
    }

    case MmtPredFn(f: MmtPredFnOp, mmt1 : Mmt, mmt2 : Mmt) => f match {
      case MmtMax => bin_fun("Max", mmtToExpr(mmt1, tranMode), mmtToExpr(mmt2, tranMode))
      case MmtMin => bin_fun("Min", mmtToExpr(mmt1, tranMode), mmtToExpr(mmt2, tranMode))
    }

    case _ => throw new Error ("either has varargs")
  }
  
  def mmtVarToExpr(x : String, tranMode : Int) : Expr = {
    if (EvolveToExpr.CURR_BACKTICK == tranMode) {
//      return math_sym(mask(x, "curr`"))
      return readState(mask(x, "curr`"))
    } else if (EvolveToExpr.LOCA_T == tranMode) {
      return un_fun(mask(x), math_sym("loca`t"))
    } else if (EvolveToExpr.NEW_T == tranMode) {
      return un_fun(mask(x), math_sym("new`t"))
    } else if (EvolveToExpr.NOTHING == tranMode) {
      return math_sym(mask(x))
    } else {
      throw new Exception("mmtVarToExpr: Impossible case")
    }
  }
  def termsToMmts(terms: Term*) : Seq[Mmt] = {
    var result = Seq[Mmt]()
    for (t <- terms) {
      result = result :+ termToMmt(t)
    }
    return result
  }

  def termToMmt(term: Term) : Mmt = term match {
    case Num(n: Exact.Num) => MmtNum(n)
    case Var(s: String) => MmtVar(s)
    case Arithmetic(op, terms @ _*) => op match {
//      case Negate(_) => new MmtArithmetic(MmtNegate, termToMmt(terms.head))
//      case Plus(_) => MmtArithmetic(MmtPlus, termsToMmts(terms:_*):_*)
//      case Subtract(_) => MmtArithmetic(MmtSubtract, termsToMmts(terms:_*):_*)
//      case Multiply(_) => MmtArithmetic(MmtMultiply, termsToMmts(terms:_*):_*)
//      case Divide(_) => MmtArithmetic(MmtDivide, termsToMmts(terms:_*):_*)
//      case Power(_) => MmtArithmetic(MmtPower, termsToMmts(terms:_*):_*)
//      case Modulo(_) => MmtArithmetic(MmtModulo, termsToMmts(terms:_*):_*)
      
      case Negate => new MmtArithmetic(MmtNegate, termToMmt(terms.head))
      case Plus => MmtArithmetic(MmtPlus, termsToMmts(terms:_*):_*)
      case Subtract => MmtArithmetic(MmtSubtract, termsToMmts(terms:_*):_*)
      case Multiply => MmtArithmetic(MmtMultiply, termsToMmts(terms:_*):_*)
      case Divide => MmtArithmetic(MmtDivide, termsToMmts(terms:_*):_*)
      case Power => MmtArithmetic(MmtPower, termsToMmts(terms:_*):_*)
      case Modulo => MmtArithmetic(MmtModulo, termsToMmts(terms:_*):_*)
      
      case _ => throw new Exception(op.toString() + " not implemented... varargs")
    }
    /*
    case Arithmetic(op: ArithmeticOp, term1: Term) => op match {
      case Negate(_) => MmtArithmetic(MmtNegate, termToMmt(term1))
      case _ => throw new Exception("not implemented... varargs")
    }
    case Arithmetic(op: ArithmeticOp, term1: Term, term2: Term) => op match {
      case Plus => MmtArithmetic(MmtPlus, termToMmt(term1), termToMmt(term2))
      case Subtract => MmtArithmetic(MmtSubtract, termToMmt(term1), termToMmt(term2))
      case Multiply => MmtArithmetic(MmtMultiply, termToMmt(term1), termToMmt(term2))
      case Divide => MmtArithmetic(MmtDivide, termToMmt(term1), termToMmt(term2))
      case Power => MmtArithmetic(MmtPower, termToMmt(term1), termToMmt(term2))
      case Modulo => MmtArithmetic(MmtModulo, termToMmt(term1), termToMmt(term2))
      case _ => throw new Exception("not implemented... varargs")
    }
    */
    
    /* deboPreguntar: function name by Fn may be different from function name in
     Mathematica. How to handle this? */
    case Fn(_, _) => throw new Exception("not implemented")
    // deboPreguntar: how to handle varargs???
  }

  def formulaToMmt(frml: Formula) : Mmt = frml match {
    case True => MmtPosInfty()
    case False => MmtNegInfty()
    case Atom(t: Term) => termToMmt(t)
    case ArithmeticPred(op : Comparison, term1 : Term, term2 : Term) => op match {
      case Equals => MmtArithmetic(MmtNegate, MmtArithmetic(MmtMultiply,
          MmtArithmetic(MmtSubtract, termToMmt(term1), termToMmt(term2)), 
          MmtArithmetic(MmtSubtract, termToMmt(term1), termToMmt(term2)))) 
      case NotEquals => MmtArithmetic(MmtMultiply,
          MmtArithmetic(MmtSubtract, termToMmt(term1), termToMmt(term2)), 
          MmtArithmetic(MmtSubtract, termToMmt(term1), termToMmt(term2))) 
      case Less => MmtArithmetic(MmtSubtract, termToMmt(term2),
          termToMmt(term1))
      case LessEquals => MmtArithmetic(MmtSubtract, termToMmt(term2),
          termToMmt(term1))
      case Greater => MmtArithmetic(MmtSubtract, termToMmt(term1),
          termToMmt(term2))
      case GreaterEquals => MmtArithmetic(MmtSubtract, termToMmt(term1),
          termToMmt(term2))
      /* deboPreguntar: OK to handle less and lessequals in the same way? */
      case _ => throw new Exception("not implemented... varargs")
    }

    /* deboPreguntar: how to handle "Pred"? */
    case Pred(_, _) => throw new Exception ("not implemented")

    case Prop(c : Connective, f1 : Formula) => c match {
      case Not => MmtArithmetic(MmtNegate, formulaToMmt(f1))
      case _ => throw new Exception("not implemented yet... varargs")
    }

    case Prop(c : Connective, f1 : Formula, f2 : Formula) => c match {
      case And => MmtPredFn(MmtMin, formulaToMmt(f1), formulaToMmt(f2))
      case Or => MmtPredFn(MmtMax, formulaToMmt(f1), formulaToMmt(f2))
      case Imp => formulaToMmt(Prop(Or, Prop(Not, f1), f2))
      case Iff => formulaToMmt(Prop(And, Prop(Imp, f1, f2), Prop(Imp, f2, f1)))
      case _ => throw new Exception("not implemented yet... varargs")
    }

    // deboArreglar: implement these.
//    case Quantifier(k: QuantifierKind, v: String,
//        c: Sort, f: formula) => c match {
//      }
    case _ => throw new Exception("not implemented... varargs")
  }

  /*def mmtToExpr(mmt: Mmt) : Expr = mmt match {*/
  /*}*/
    

}

