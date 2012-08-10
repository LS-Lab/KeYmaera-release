/**
 * *****************************************************************************
 * Copyright (c) 2012 Andre Platzer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 *     Andre Platzer
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

/**
 * @author ap
 *
 */
object PolynomialExtraction {
  implicit def term2richterm(t: Term): RichTerm = new RichTerm(t)
  implicit def int2term(i: Int) = TermBuilder.DF.func(NumberCache.getNumber(
    new BigDecimal(i), RealLDT.getRealSort()))
    
  def convert(t: Term): java.util.Set[Term] = apply(t)

  def apply(t: Term): Set[Term] = {
    t match {
      case All(_, _) | Ex(_, _) | Eqv(_, _) | Imp(_, _) | Box(_, _) | Dia(_, _) =>
        throw new UnsupportedOperationException(
          "not yet implemented for operator " + t.op + " in " + t);
      case Not(a) =>
        throw new IllegalArgumentException("please transform the " + t
          + " into negation normal form");
      case Or(a, b) => apply(a) ++ apply(b)
      case And(a, b) => apply(a) ++ apply(b)
      case Equals(a, b) => Set(a-b)
      case UnEquals(a, b) => Set(a-b)
      case GreaterEquals(a, b) => Set(a-b)
      case LessEquals(a, b) => Set(b-a)
      case Greater(a, b) => Set(a-b)
      case Less(a, b) => Set(b-a)
      case _ => {
        if (t.sort() == RealLDT.getRealSort())
          Set(t)
        else
          throw new IllegalArgumentException("Don't known how to convert " + t.op + " in " + t)
      }
    }
  }
}
