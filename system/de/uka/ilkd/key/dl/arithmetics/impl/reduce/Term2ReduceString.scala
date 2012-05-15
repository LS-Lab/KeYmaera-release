/**
 * *****************************************************************************
 * Copyright (c) 2012 Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 *     Jan-David Quesel - initial API and implementation
 * ****************************************************************************
 */
package de.uka.ilkd.key.dl.arithmetics.impl.reduce

import de.uka.ilkd.key.dl.formulatools.RichTerm
import de.uka.ilkd.key.logic.Term
import de.uka.ilkd.key.dl.formulatools._
import de.uka.ilkd.key.logic.NamespaceSet
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.PolynomTool
import java.math.BigInteger
import de.uka.ilkd.key.collection.ImmutableArray
import de.uka.ilkd.key.logic.op.QuantifiableVariable

/**
 * Converts a term to a String readable by reduce.
 *
 * @author Jan-David Quesel
 */
object Term2ReduceString {
  val TRUE = "true"
  val FALSE = "false"
  val DOLLARESCAPE = "dollar"
  val UNDERSCOREESCAPE = "uscore"

  def elimFraction(t: Term, count: Array[Int]) = Term2ReduceString(PolynomTool.eliminateFractionsFromInequality(t: Term, null), count, false)

  def replaceChars(n: String): String = {
    var r = n.replaceAll("\\$", DOLLARESCAPE).replaceAll("_", UNDERSCOREESCAPE)
    for (c <- 'A' to 'Z') {
      r = r.replaceAll("" + c, (c + "_").toLowerCase())
    }
    return r
  }

  def apply(t: Term, count: Array[Int], elimFract: Boolean): String = {
    val conv = Term2ReduceString(_: Term, count, elimFract)
    val elim = elimFraction(_: Term, count)
    t match {
      case False() => FALSE
      case True() => TRUE
      case Equals(a, b) => if (elimFract) elim(t) else "( " + conv(a) + " = " + conv(b) + " )"
      case UnEquals(a, b) => if (elimFract) elim(t) else "( " + conv(a) + " <> " + conv(b) + " )"
      case Greater(a, b) => if (elimFract) elim(t) else "( " + conv(a) + " > " + conv(b) + " )"
      case GreaterEquals(a, b) => if (elimFract) elim(t) else "( " + conv(a) + " >= " + conv(b) + " )"
      case LessEquals(a, b) => if (elimFract) elim(t) else "( " + conv(a) + " <= " + conv(b) + " )"
      case Less(a, b) => if (elimFract) elim(t) else "( " + conv(a) + " < " + conv(b) + " )"
      case MinusSign(a) => "(-" + conv(a) + ")"
      case Plus(a, b) => "( " + conv(a) + "+" + conv(b) + " )"
      case Minus(a, b) => "( " + conv(a) + "-" + conv(b) + " )"
      case Mult(a, b) => "( " + conv(a) + "*" + conv(b) + " )"
      case Div(a, b) => "( " + conv(a) + "/" + conv(b) + " )"
      case Exp(a, b) => "( " + conv(a) + "^" + conv(b) + " )"
      case Constant(n) => {
        assert(t.arity == 0) // we can only handle variables, constant functions, and numbers here
        val r = replaceChars(n)
        try {
          val frac = PolynomTool.convertStringToFraction(r)
          if (frac.getDenominator().equals(BigInteger.ONE))
            frac.getNumerator().toString
          else
            "( " + frac.getNumerator() + " / " + frac.getDenominator() + " )"
        } catch {
          case e: NumberFormatException => { count(0) += 1; return "(" + r + ")" }
        }
      }
      case Not(a) => "(not " + conv(a) + ")"
      case And(a, b) => "( " + conv(a) + " and " + conv(b) + ")"
      case Or(a, b) => "( " + conv(a) + " or " + conv(b) + ")"
      case Imp(a, b) => "( " + conv(a) + " impl " + conv(b) + ")"
      case Eqv(a, b) => "( " + conv(a) + " equiv " + conv(b) + ")"
      case All(a, v) => "(" + outputVars(v, a, "all", conv, count) + ")"
      case Ex(a, v) => "(" + outputVars(v, a, "ex", conv, count) + ")"
      case _ => throw new IllegalArgumentException("Could not convert Term: " + t
        + "Operator was: " + t.op())
    }
  }

  def outputVars(vars: ImmutableArray[QuantifiableVariable], t: Term, s: String, conv: Term => String, count: Array[Int]): String = {
    var res = ""
      println("vars size " + vars.size)
    for (i <- 0 until vars.size) {
      count(0) += 1
      res += s + "(" + replaceChars(vars.get(i).name.toString) + ", "
    }
    res += conv(t);
    for (i <- 0 until vars.size) {
      res += ")"
    }
    return res
  }

}