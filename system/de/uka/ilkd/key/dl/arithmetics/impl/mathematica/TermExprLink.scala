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
package de.uka.ilkd.key.dl.arithmetics.impl.mathematica

import de.uka.ilkd.key.dl.formulatools.RichTerm
import de.uka.ilkd.key.logic.Term
import de.uka.ilkd.key.dl.formulatools._
import de.uka.ilkd.key.logic.NamespaceSet
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.PolynomTool
import java.math.BigInteger
import de.uka.ilkd.key.collection.ImmutableArray
import de.uka.ilkd.key.logic.op.QuantifiableVariable
import com.wolfram.jlink.Expr

/**
 * Converts a term to an Expr object for J/Link
 *
 * @author Jan-David Quesel
 */
object Term2Expr extends ExpressionConstants {

  implicit def expr2Rich(e: Expr): RichExpr = new RichExpr(e)
  implicit def BigInteger2Expr(i: BigInteger): Expr = new Expr(i)

  class RichExpr(e: Expr) {
    def arr(e: Expr*): Array[Expr] = e.toArray
    def unary_! : Expr = new Expr(NOT, arr(e))
    def |(ot: Expr): Expr = new Expr(OR, arr(e, ot))
    def &(ot: Expr): Expr = new Expr(AND, arr(e, ot))
    def ->(ot: Expr): Expr = new Expr(IMPL, arr(e, ot))
    //    def <->(ot: Expr): Expr = new Expr(BIIMPL, arr(e, ot))
    def <->(ot: Expr): Expr = (e -> ot) & (ot -> e)
    def unary_- : Expr = new Expr(MINUSSIGN, arr(e))
    def +(ot: Expr): Expr = new Expr(PLUS, arr(e, ot))
    def -(ot: Expr): Expr = new Expr(MINUS, arr(e, ot))
    def *(ot: Expr): Expr = new Expr(MULT, arr(e, ot))
    def /(ot: Expr): Expr = if (e.numberQ && ot.numberQ) new Expr(RATIONAL, arr(e, ot)) else new Expr(DIV, arr(e, ot))
    def ^(ot: Expr): Expr = new Expr(EXP, arr(e, ot))

    def equal(ot: Expr): Expr = new Expr(EQUALS, arr(e, ot))
    def unequal(ot: Expr): Expr = new Expr(UNEQUAL, arr(e, ot))
    def lt(ot: Expr): Expr = new Expr(LESS, arr(e, ot))
    def gt(ot: Expr): Expr = new Expr(GREATER, arr(e, ot))
    def leq(ot: Expr): Expr = new Expr(LESS_EQUALS, arr(e, ot))
    def geq(ot: Expr): Expr = new Expr(GREATER_EQUALS, arr(e, ot))
  }

  def elimFraction(t: Term) = this(PolynomTool.eliminateFractionsFromInequality(t: Term, null), false)

  def replaceChars(n: String): String = SKOPE + n.replaceAll("_", USCORE_ESCAPE)

  def apply(t: Term, elimFract: Boolean): Expr = {
    val conv = this(_: Term, elimFract)
    val elim = elimFraction(_: Term)
    t match {
      case False() => FALSE
      case True() => TRUE
      case Equals(a, b) => if (elimFract) elim(t) else conv(a) equal conv(b)
      case UnEquals(a, b) => if (elimFract) elim(t) else conv(a) unequal conv(b)
      case Greater(a, b) => if (elimFract) elim(t) else conv(a) gt conv(b)
      case GreaterEquals(a, b) => if (elimFract) elim(t) else conv(a) geq conv(b)
      case LessEquals(a, b) => if (elimFract) elim(t) else conv(a) leq conv(b)
      case Less(a, b) => if (elimFract) elim(t) else conv(a) lt conv(b)
      case MinusSign(a) => -conv(a)
      case Plus(a, b) => conv(a) + conv(b)
      case Minus(a, b) => conv(a) - conv(b)
      case Mult(a, b) => conv(a) * conv(b)
      case Div(a, b) => conv(a) / conv(b)
      case Exp(a, b) => conv(a) ^ conv(b)
      case Constant(n) => {
        assert(t.arity == 0) // we can only handle variables, constant functions, and numbers here
        try {
          if (Options.INSTANCE.isConvertDecimalsToRationals) {
            val frac = PolynomTool.convertStringToFraction(n)
            if (frac.getDenominator().equals(BigInteger.ONE))
              frac.getNumerator()
            else
              new Expr(frac.getNumerator()) / frac.getDenominator()
          } else {
            val num = new java.math.BigDecimal(n)
            return new Expr(Expr.SYM_REAL, Array(new Expr(num)))
          }
        } catch {
          case e: NumberFormatException => new Expr(Expr.SYMBOL, replaceChars(n))
        }
      }
      case Not(a) => !conv(a)
      case And(a, b) => conv(a) & conv(b)
      case Or(a, b) => conv(a) | conv(b)
      case Imp(a, b) => conv(a) -> conv(b)
      case Eqv(a, b) => conv(a) <-> conv(b)
      case All(a, v) =>
        val ca = conv(a)
        new Expr(FORALL, (ca match {
          case EForall(exp, vars) => convert(v, vars).toList :+ exp
          case _ => convert(v, Array()).toList :+ ca
        }) toArray)
      case Ex(a, v) =>
        val ca = conv(a)
        new Expr(EXISTS, (convert(v,
          ca match {
            case EExists(exp, vars) => vars
            case _ => Array()
          }).toList :+ ca).toArray)
      case Function(f, args) => {
        val fun = new Expr(Expr.SYMBOL, replaceChars(f))
        if (args.isEmpty)
          fun
        else
          new Expr(fun, args.map(conv).toArray)
      }
      case _ => throw new IllegalArgumentException("Could not convert Term: " + t
        + "Operator was: " + t.op())
    }
  }

  def convert(vars: ImmutableArray[QuantifiableVariable], oldvars: Array[Expr]): Array[Expr] =
    Array(new Expr(Expr.SYM_LIST,
      ((for (i <- 0 until vars.size)
        yield new Expr(Expr.SYMBOL, replaceChars(vars.get(i).name.toString))) ++ oldvars).toArray))
}

object EExists extends ExpressionConstants {
  def unapply(e: Expr): Option[(Expr, Array[Expr])] = {
    if (e.head == EXISTS && e.args.length == 2)
      Some((e.args()(1), e.args()(0).args))
    else
      None
  }
}

object EForall extends ExpressionConstants {
  def unapply(e: Expr): Option[(Expr, Array[Expr])] = {
    if (e.head == FORALL && e.args.length == 2) {
      Some((e.args()(1), e.args()(0).args))
    } else {
      None
    }
  }
}

object EConstants extends ExpressionConstants;

trait ExpressionConstants {
  
  val USCORE_ESCAPE = "\\$u";

  val SKOPE = "KeYmaera`"

  val FALSE = new Expr(Expr.SYMBOL, "False");

  val TRUE = new Expr(Expr.SYMBOL, "True");

  val NOT = new Expr(Expr.SYMBOL, "Not");

  val RATIONAL = new Expr(Expr.SYMBOL, "Rational");

  val PLUS = new Expr(Expr.SYMBOL, "Plus");

  val MINUS = new Expr(Expr.SYMBOL, "Subtract");

  val MINUSSIGN = new Expr(Expr.SYMBOL, "Minus");

  val MULT = new Expr(Expr.SYMBOL, "Times");

  val DIV = new Expr(Expr.SYMBOL, "Divide");

  val EXP = new Expr(Expr.SYMBOL, "Power");

  val EQUALS = new Expr(Expr.SYMBOL, "Equal");

  val UNEQUAL = new Expr(Expr.SYMBOL, "Unequal");

  val LESS = new Expr(Expr.SYMBOL, "Less");

  val LESS_EQUALS = new Expr(Expr.SYMBOL, "LessEqual");

  val GREATER = new Expr(Expr.SYMBOL, "Greater");

  val GREATER_EQUALS = new Expr(Expr.SYMBOL, "GreaterEqual");

  val INEQUALITY = new Expr(Expr.SYMBOL, "Inequality");

  val FORALL = new Expr(Expr.SYMBOL, "ForAll");

  val EXISTS = new Expr(Expr.SYMBOL, "Exists");

  val AND = new Expr(Expr.SYMBOL, "And");

  val OR = new Expr(Expr.SYMBOL, "Or");

  val IMPL = new Expr(Expr.SYMBOL, "Implies");

  val BIIMPL = new Expr(Expr.SYMBOL, "Equivalent");

  val INVERSE_FUNCTION = new Expr(Expr.SYMBOL, "InverseFunction");

  val INTEGRATE = new Expr(Expr.SYMBOL, "Integrate");

  val RULE = new Expr(Expr.SYMBOL, "Rule");

  val LIST = new Expr(Expr.SYMBOL, "List");
}