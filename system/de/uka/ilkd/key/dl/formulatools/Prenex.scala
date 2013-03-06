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

import collection.JavaConversions._
import de.uka.ilkd.key.logic.Term
import de.uka.ilkd.key.logic.Name
import de.uka.ilkd.key.logic.op.QuantifiableVariable
import de.uka.ilkd.key.logic.NamespaceSet
import de.uka.ilkd.key.logic.TermFactory
import de.uka.ilkd.key.logic.op.Op
import de.uka.ilkd.key.logic.TermBuilder
import de.uka.ilkd.key.logic.op.LogicVariable
import de.uka.ilkd.key.logic.op.SubstOp
import de.uka.ilkd.key.collection.ImmutableArray
import de.uka.ilkd.key.logic.sort.Sort

/**
 * Generates the prenex-form of a given Term
 *
 * @author jdq
 */
object Prenex {
  implicit def term2richterm(t: Term): RichTerm = new RichTerm(t)
  implicit def string2name(s: String): Name = new Name(s)

  object QT extends Enumeration {
    type QuantifierType = Value
    val All, Ex = Value: QuantifierType
  }
  import QT.QuantifierType

  def transform(t: Term, nss: NamespaceSet): (Term, java.util.List[QuantifiableVariable]) = {
    val (qs, nt) = collectAndConvert(t, nss, false)
    var res = nt
    var vars = Nil: List[QuantifiableVariable]
    for ((qt, v) <- qs) {
      qt match {
        case QT.All =>
          val org = res
          res = All(res, v)
          if(org != res) {
            // the quantifier did quantify a free variable
            vars = vars :+ v
          }
        case QT.Ex =>
          val org = res
          res = Ex(res, v)
          if(org != res) {
            // the quantifier did quantify a free variable
            vars = vars :+ v
          }
        case a => throw new IllegalArgumentException("Unknown quantifier " + a)
      }
    }
    (res, vars)
  }

  def handleQ(q: QuantifierType, vars: ImmutableArray[QuantifiableVariable], a: Term,
    nss: NamespaceSet, negated: Boolean): (List[(QuantifierType, LogicVariable)], Term) = {
    val pos = collectAndConvert(_: Term, nss, negated)
    val res = pos(a)
    var quants = res._1
    var nt = res._2
    for (i <- 0 until vars.size) {
      val ov = vars.get(i)
      val nv = newVarName(ov.name.toString, ov.sort, nss)
      quants = quants :+ (q match {
        case QT.All => if (negated) (QT.Ex, nv) else (QT.All, nv)
        case QT.Ex => if (negated) (QT.All, nv) else (QT.Ex, nv)
      })
      val subst = TermFactory.DEFAULT.createSubstitutionTerm(
        Op.SUBST, ov, TermFactory.DEFAULT.createVariableTerm(nv), nt);
      subst.op match {
        case sub: SubstOp => nt = sub.apply(subst)
        case _ => throw new IllegalStateException("Op should be a subst")
      }
    }
    (quants, nt)
  }

  def comb(a: (List[(QuantifierType, LogicVariable)], Term),
    b: (List[(QuantifierType, LogicVariable)], Term),
    f: (Term, Term) => Term): (List[(QuantifierType, LogicVariable)], Term) = {
    (a._1 ++ b._1, f(a._2, b._2))
  }

  def collectAndConvert(t: Term, nss: NamespaceSet, negated: Boolean): (List[(QuantifierType, LogicVariable)], Term) = {
    val neg = collectAndConvert(_: Term, nss, !negated)
    val pos = collectAndConvert(_: Term, nss, negated)
    t match {
      case a@True() => (Nil,a)
      case a@False() => (Nil,a)
      case All(a, v) =>
        handleQ(QT.All, v, a, nss, negated)
      case Ex(a, v) =>
        handleQ(QT.Ex, v, a, nss, negated)
      case Not(a) =>
        val res = neg(a)
        (res._1, !res._2)
      case Or(a, b) => comb(pos(a), pos(b), Or(_, _))
      case And(a, b) => comb(pos(a), pos(b), And(_, _))
      case Imp(a, b) => comb(neg(a), pos(b), Imp(_, _))
      case Eqv(a, b) => comb(pos(a), pos(b), Eqv(_, _))
      case Equals(a, b) => (Nil, a equal b)
      case Greater(a, b) => (Nil, a gt b)
      case GreaterEquals(a, b) => (Nil, a geq b)
      case LessEquals(a, b) => (Nil, a leq b)
      case Less(a, b) => (Nil, a lt b)
      case UnEquals(a, b) => (Nil, a unequal b)
      case _ => throw new IllegalArgumentException("Don't known how to convert " + t.op)
    }
  }

  def newVarName(v: String, s: Sort, nss: NamespaceSet): LogicVariable = {
    val n = nss.getUniqueName(v, true) 
    val sym = new LogicVariable(n, s);
    nss.variables().addSafely(sym);
    sym;
  }
}
