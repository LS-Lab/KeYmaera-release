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
package de.uka.ilkd.key.dl.formulatools

import scala.collection.JavaConversions._
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.PolynomTool
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.PolynomTool.BigFraction
import de.uka.ilkd.key.logic.Term
import de.uka.ilkd.key.logic.NamespaceSet
import de.uka.ilkd.key.logic.op.Op
import de.uka.ilkd.key.logic.op.QuantifiableVariable
import de.uka.ilkd.key.logic.op.LogicVariable
import de.uka.ilkd.key.logic.op.ProgramVariable
import de.uka.ilkd.key.logic.op.LocationVariable
import de.uka.ilkd.key.logic.TermBuilder
import de.uka.ilkd.key.dl.parser.NumberCache
import java.math.BigDecimal
import de.uka.ilkd.key.dl.logic.ldt.RealLDT
import de.uka.ilkd.key.java.Services

/**
 * @author jdq
 *
 */
object Derive {
  implicit def term2richterm(t: Term): RichTerm = new RichTerm(t)
  implicit def int2term(i: Int) = TermBuilder.DF.func(NumberCache.getNumber(
    new BigDecimal(i), RealLDT.getRealSort()))

  def apply(t: Term, vars: java.util.Map[String, Term], eps: Term, services: Services): Term = {
    val d = Derive(_: Term, vars, eps, services)
    t match {
      case True() | False() => t 
      case Eqv(_, _) | Imp(_, _) | Box(_, _) | Dia(_, _) =>
        throw new UnsupportedOperationException(
          "not yet implemented for operator " + t.op + " in " + t);
      case Not(_) =>
        throw new IllegalArgumentException("please transform the " + t
          + " into negation normal form");
      case All(a, x) => All(d(a), x)
      case Ex(a, x) => Ex(d(a), x)
      case Or(a, b) => d(a) & d(b)
      case And(a, b) => d(a) & d(b)
      case Constant(n) => vars.toMap.get(n) match {
        case Some(t) => t
        case _ => 0
      }
      case nrf@NonRigidFunction(f, args) => vars.toMap.get(nrf.toString) match {
        case Some(t) => t
        case _ => println(nrf.toString + " not found!"); 0
      }
      case MinusSign(a) => -d(a)
      case Plus(a, b) => d(a) + d(b)
      case Minus(a, b) => d(a) - d(b)
      case Mult(a, b) => (d(a) * b) + (a * d(b))
      case Div(a, b) => ((d(a) * b) - (a * d(b))) / (b ^ 2)
      case Exp(_, b) if (b == 0) => 0
      case Exp(a, _) if (a == 1) => 0 // term is constant and thus derived to 0
      case Exp(a, b) if (b == 1) => d(a)
      // derivative of E^f(x) is f'(x) * E^f(x)
      case Exp(MathFun("E", args), b) if args.length == 0 => d(b)*t
      case Exp(a, b) => try {
        b match {
          case MinusSign(e) => d((1: Term) / (a ^ e))
          case _ => {
            val frac = PolynomTool.convertStringToFraction(b.op.name.toString)
            require(b.arity == 0, "literal constants have no subterms")
            b * (a ^ (b - 1)) * d(a)
          }
        }
      } catch {
        case e: Exception => {
          throw new UnsupportedOperationException(
            "Derive not implemented for nonintegral exponents: "
              + b, e);
        }
      }
      case MathFun("E", args) if args.length == 0 => 0
      case MathFun("Pi", args) if args.length == 0 => 0
      case MathFun(f, args) if args.length == 1 => val x = args.head; f match {
        // computing: d(f(x)) = f'(x)*d(x)
        /* Roots */
        case "Sqrt" => (int2term(1) / (int2term(2)*t)) * d(x)
        case "CubeRoot" => (int2term(1) / (int2term(3)*(t^2))) * d(x)
        /* Exp function */
        case "Exp" => d(x)*t
        /* Logarithm */
        case "Log" => (int2term(1) / x) * d(x)
        /* Trigonometric functions */
        case "Sin" => (MathFun("Cos", args, services)) * d(x)
        case "Cos" => (- MathFun("Sin", args, services)) * d(x)
        case "Tan" => (MathFun("Sin", args, services) ^ 2) * d(x)
        case "Csc" => ((-t)*MathFun("Cot", args, services)) * d(x)
        case "Sec" => (t*MathFun("Tan", args, services)) * d(x)
        case "Cot" => (- (MathFun("Csc", args, services) ^ 2)) * d(x)
        /* Inverse Trigonometirc Functions */
        case "ArcSin" => (int2term(1) / (MathFun("Sqrt", Seq(int2term(1)-(x^2)), services))) * d(x)
        case "ArcCsc" => (- int2term(1) /
            (MathFun("Abs", Seq(x), services) *
                MathFun("Sqrt", Seq((x^2) - 1), services))) * d(x)
        case "ArcCos" => (- int2term(1) / (MathFun("Sqrt", Seq(int2term(1)-(x^2)), services))) * d(x)
        case "ArcSec" => (int2term(1) /
            (MathFun("Abs", Seq(x), services) *
                MathFun("Sqrt", Seq((x^2) - 1), services))) * d(x)
        case "ArcTan" => (int2term(1) / (x^2 + 1)) * d(x)
        case "ArcCot" => (- int2term(1) / (x^2 + 1)) * d(x)
        /* Hyperbolic functions */
        case "Sinh" => (MathFun("Cosh", args, services)) * d(x)
        case "Csch" => (- MathFun("Coth", args, services) * t) * d(x)
        case "Cosh" => (MathFun("Sinh", args, services)) * d(x)
        case "Sech" => (- MathFun("Tanh", args, services) * t) * d(x)
        case "Tanh" => (int2term(1) - (t^2)) * d(x)
        case "Coth" => (int2term(1) - (t^2)) * d(x)
      }
      case Equals(a, b) => if (eps == null) (d(t.sub(0)) equal d(t.sub(1))) else {
        throw new IllegalArgumentException(
          "The operator DiffFin is undefined for unequalities.");
      }
      case UnEquals(a, b) => if (eps == null) (d(t.sub(0)) equal d(t.sub(1))) else {
        throw new IllegalArgumentException(
          "The operator DiffFin is undefined for unequalities.");
      }
      case GreaterEquals(a, b) =>
        if (eps == null)
          d(a) geq d(b)
        else
          d(a) geq (d(b) + eps)
      case LessEquals(a, b) =>
        if (eps == null)
          d(a) leq d(b)
        else
          (d(a) + eps) leq d(b)
      case Greater(a, b) => d(a geq b)
      case Less(a, b) => d(a leq b)
      case _ => throw new IllegalArgumentException("Don't known how to convert " + t.op + " in " + t)
    }
  }
}
