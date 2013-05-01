package edu.cmu.cs.ls.lyusimul

import com.wolfram.jlink._
import edu.cmu.cs.ls._
import edu.cmu.cs.ls.lyusimul._

object EvolveToExpr {
  
  def CURR_BACKTICK : Int = 0
  def LOCA_T : Int = 1
  
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
  
  def hpToOdesListExpr(hp : HP, tranMode : Int) : Expr = hp match {
    case Evolve(h, primes @ _ *) => new Expr(math_sym("List"), primsToOdesList(primes, tranMode : Int).toArray)

    case _ => throw new Exception("not implemented yet")    
  }
  
  // deboHacer: Change name to exprToCsvStri
  def exprsListToCsvStri(x : List[Expr]) : String = x match {
    case Nil => ""
    case x :: Nil => x.toString();
    case x :: xs => x.toString() + ", " + exprsListToCsvStri(xs)
  } 
  
  def setsStriListToNlsvStri(x : List[String]) : String = x match {
    case Nil => ""
    case x :: Nil => x;
    case x :: xs => x + ";\n" + setsStriListToNlsvStri(xs)
  }
  
  def exprsListToNlsvStri(x : List[Expr]) : String = x match {
    case Nil => ""
    case x :: Nil => x.toString();
    case x :: xs => x.toString() + "\n" + exprsListToNlsvStri(xs)
  }
  
  // Deprecated for use of string
//  def evolsListToSetsStriList(evolves: List[Evolve]) : List[String] = evolves match {
//    case Nil => Nil
//    case x :: xs => (
//      "Set[glob`sol, Append[glob`sol, NDSolve[{" +
//      exprsListToCsvStri(hpToOdesList(x, tranMode : Int)) +
//      ", x[Evaluate[Last[glob`tends]]] == Evaluate[x[Evaluate[Last[glob`tends]]] /. Last[glob`sol]][[1]], y[Evaluate[Last[glob`tends]]] == Evaluate[y[Evaluate[Last[glob`tends]]] /. Last[glob`sol]][[1]], v[Evaluate[Last[glob`tends]]] == Evaluate[v[Evaluate[Last[glob`tends]]] /. Last[glob`sol]][[1]], r[Evaluate[Last[glob`tends]]] == Evaluate[r[Evaluate[Last[glob`tends]]] /. Last[glob`sol]][[1]], om[Evaluate[Last[glob`tends]]] == Evaluate[om[Evaluate[Last[glob`tends]]] /. Last[glob`sol]][[1]], dx[Evaluate[Last[glob`tends]]] == Evaluate[dx[Evaluate[Last[glob`tends]]] /. Last[glob`sol]][[1]], dy[Evaluate[Last[glob`tends]]] == Evaluate[dy[Evaluate[Last[glob`tends]]] /. Last[glob`sol]][[1]], ac[Evaluate[Last[glob`tends]]] == Evaluate[ac[Evaluate[Last[glob`tends]]] /. Last[glob`sol]][[1]], dummT[Evaluate[Last[glob`tends]]] == 0," +
//      exprsListToCsvStri(hpToWhenEvents(x, 10)) + 
//      "}, {x, y, v, ac, om, dx, dy, r, dummT}, {t, Evaluate[Last[glob`tends]], glob`tendLimi}]]];"
//      ) :: evolsListToSetsStriList(xs)
//  }
  
  def hpToOdesList(hp: HP, tranMode : Int): List[Expr] = hp match {
    case Evolve(h, primes @ _ *) => primsToOdesList(primes, tranMode : Int)

    case _ => throw new Exception("not implemented yet")
  }
  
  def primsToOdesList(primes: Seq[(Var,Term)], tranMode : Int) : List[Expr] = primes match {
    case Seq(first: (Var, Term), rest @ _ *) => first._1 match {
      case Var(s) => bin_fun("Equal",
          new Expr(new Expr(new Expr(math_sym("Derivative"),
          List(new Expr(1L)).toArray),
          List(math_sym(s)).toArray),
          List(math_sym("loca`t")).toArray),
          MmtManipulation.mmtToExpr(MmtManipulation.termToMmt(first._2), tranMode) ) :: primsToOdesList(rest, tranMode : Int)
      // deboArreglar: don't use mmtToExpr and termToMmt.
      case _ => throw new Exception("Internal Error")
    }
    case _ => Nil
  }
  
  def hpToVarisList(hp: HP): List[Expr] = hp match {
    case Evolve(h, primes @ _ *) => primsToVarisList(primes:_*)
    case _ => throw new Exception("not implemented yet")
  }
  
  def seqToEvolsList(hp : HP) : List[Evolve] = hp match {
    case ComposedHP(Sequence, hps @ _ *) => 
      seqToEvolsList(hps.head) ++ hps.tail.map(hp => seqToEvolsList(hp)).flatten 
      // Sequence.flatten(hp)
      // hp.asInstanceOf[ComposedHP].flatten
    case Evolve(_, _ @ _*) => (hp.asInstanceOf[Evolve] :: Nil)
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
  
  def hpToWhenEvents(hp: HP, tMax : Double, tranMode : Int): List[Expr] = hp match {
    case Evolve(h, primes @ _ *) => hToWhenEvents(h, tMax, tranMode : Int)

    case _ => throw new Exception("not implemented yet")
  }
  
  
  def hToWhenEvents(h: Formula, tMax: Double, tranMode : Int) : List[Expr] = {
    bin_fun("WhenEvent",
        //bin_fun("Or", un_fun("Not", formulaToExpr(h)), bin_fun("Greater", math_sym("t"), new Expr(tMax))),
        un_fun("Not", formulaToExpr(h, tranMode : Int)),
        //bin_fun("CompoundExpression", bin_fun("Set", math_sym("glob`tend"), math_sym("t")), new Expr("StopIntegration"))) :: Nil
        bin_fun("CompoundExpression", bin_fun("Set", math_sym("glob`tends"), bin_fun("Append", math_sym("glob`tends"), math_sym("loca`t"))), new Expr("StopIntegration"))) :: Nil
  }

    // deboArreglar: don't use mmtToExpr and termToMmt in this function
  def termToExpr(myTerm: Term, tranMode : Int) : Expr = {
    MmtManipulation.mmtToExpr(MmtManipulation.termToMmt(myTerm), tranMode)
  }
  

  def formulaToExpr(frml: Formula, tranMode : Int) : Expr = frml match {
    case True => math_sym("True")
    case False => math_sym("False")
    case Atom(t: Term) => termToExpr(t, tranMode)
    case ArithmeticPred(op : Comparison, term1 : Term, term2 : Term) => op match {
      case Equals => bin_fun("Equal", termToExpr(term1, tranMode), termToExpr(term2, tranMode)) 
      case NotEquals => bin_fun("Unequal", termToExpr(term1, tranMode), termToExpr(term2, tranMode))
      case Less => bin_fun("Less", termToExpr(term1, tranMode), termToExpr(term2, tranMode))
      case LessEquals => bin_fun("LessEqual", termToExpr(term1, tranMode), termToExpr(term2, tranMode))
      case Greater => bin_fun("Greater", termToExpr(term1, tranMode), termToExpr(term2, tranMode))
      case GreaterEquals => bin_fun("GreaterEqual", termToExpr(term1, tranMode), termToExpr(term2, tranMode))
      /* deboPreguntar: OK to handle less and lessequals in the same way? */
      case _ => throw new Exception("not implemented... varargs")
    }

    /* deboPreguntar: how to handle "Pred"? */
    case Pred(_, _) => throw new Exception ("not implemented")

    case Prop(c : Connective, f1 : Formula) => c match {
      case Not => un_fun("Not", formulaToExpr(f1, tranMode : Int))
      case _ => throw new Exception("not implemented yet... varargs")
    }

    case Prop(c : Connective, f1 : Formula, f2 : Formula) => c match {
      case And => bin_fun("And", formulaToExpr(f1, tranMode : Int), formulaToExpr(f2, tranMode : Int))
      case Or => bin_fun("Or", formulaToExpr(f1, tranMode : Int), formulaToExpr(f2, tranMode : Int))
      case Imp => bin_fun("Implies", formulaToExpr(f1, tranMode : Int), formulaToExpr(f2, tranMode : Int))
      case Iff => bin_fun("Equivalent", formulaToExpr(f1, tranMode : Int), formulaToExpr(f2, tranMode : Int))
      case _ => throw new Exception("not implemented yet... varargs")
    }

    // deboArreglar: implement these.
//    case Quantifier(k: QuantifierKind, v: String,
//        c: Sort, f: formula) => c match {
//      }
    case _ => throw new Exception("not implemented... varargs")
  }
  
  def tendLimiToSetExpr(tendLimi: Int) : Expr =
    bin_fun("Set", math_sym("glob`tendLimi"), new Expr(tendLimi))
    
  
}

