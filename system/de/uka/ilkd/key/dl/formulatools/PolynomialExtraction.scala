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

case class PolynomialSplit(geq: java.util.Set[Term], gt: java.util.Set[Term], eq: java.util.Set[Term], uneq: java.util.Set[Term]) {
  def ++(b: PolynomialSplit) = new PolynomialSplit(this.geq++b.geq, this.gt++b.gt, this.eq++b.eq, this.uneq++b.uneq)
}

/**
 * @author ap
 *
 */
object PolynomialExtraction {
  implicit def term2richterm(t: Term): RichTerm = new RichTerm(t)
  implicit def int2term(i: Int) = TermBuilder.DF.func(NumberCache.getNumber(
    new BigDecimal(i), RealLDT.getRealSort()))
    
  val empty = java.util.Collections.emptySet[Term]()
    
  def convert(t: Term) = apply(t)

  def apply(t: Term): PolynomialSplit = {
    t match {
      case All(_, _) | Ex(_, _) | Eqv(_, _) | Imp(_, _) | Box(_, _) | Dia(_, _) =>
        throw new UnsupportedOperationException(
          "not yet implemented for operator " + t.op + " in " + t);
      case Not(a) =>
        throw new IllegalArgumentException("please transform the " + t
          + " into negation normal form");
      case Or(a, b) => apply(a) ++ apply(b)
      case And(a, b) => apply(a) ++ apply(b)
      case Equals(a, b) => new PolynomialSplit(empty, empty, Set(a-b), empty)
      case UnEquals(a, b) => new PolynomialSplit(empty, empty, empty, Set(a-b))
      case GreaterEquals(a, b) => new PolynomialSplit(Set(a-b), empty, empty, empty)
      case LessEquals(a, b) => new PolynomialSplit(Set(b-a), empty, empty, empty)
      case Greater(a, b) => new PolynomialSplit(empty, Set(a-b), empty, empty)
      case Less(a, b) => new PolynomialSplit(empty, Set(b-a), empty, empty)
      case _ => {
        //if (t.sort() == RealLDT.getRealSort())
          //Set(t)
        //else
          throw new IllegalArgumentException("Don't known how to convert " + t.op + " in " + t)
      }
    }
  }
}
