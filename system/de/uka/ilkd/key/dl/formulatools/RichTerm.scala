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

import de.uka.ilkd.key.logic.Name
import de.uka.ilkd.key.logic.TermBuilder
import de.uka.ilkd.key.logic.Term
import de.uka.ilkd.key.logic.TermFactory
import de.uka.ilkd.key.dl.logic.ldt.RealLDT
import de.uka.ilkd.key.collection.ImmutableArray
import de.uka.ilkd.key.logic.op.QuantifiableVariable
import de.uka.ilkd.key.logic.op.LogicVariable
import de.uka.ilkd.key.logic.op.ProgramVariable
import de.uka.ilkd.key.logic.op.LocationVariable
import de.uka.ilkd.key.logic.op.Operator
import de.uka.ilkd.key.logic.op.Op
import de.uka.ilkd.key.logic.JavaBlock
import de.uka.ilkd.key.logic.op.RigidFunction
import de.uka.ilkd.key.java.Services

/**
 * This class wraps the KeY Term data structures to allow advanced matching and operator overloading
 *
 * @author jdq
 *
 */
class RichTerm(t: Term) {
  def unary_! : Term = TermBuilder.DF.not(t);
  def |(ot: Term): Term = TermBuilder.DF.or(t, ot)
  def &(ot: Term): Term = TermBuilder.DF.and(t, ot)
  def ->(ot: Term): Term = TermBuilder.DF.imp(t, ot)
  def <->(ot: Term): Term = TermBuilder.DF.equiv(t, ot)
  def unary_- : Term = TermBuilder.DF.func(RealLDT.getFunctionFor(classOf[de.uka.ilkd.key.dl.model.MinusSign]), t)
  def +(ot: Term): Term = TermBuilder.DF.func(RealLDT.getFunctionFor(classOf[de.uka.ilkd.key.dl.model.Plus]), t, ot)
  def -(ot: Term): Term = TermBuilder.DF.func(RealLDT.getFunctionFor(classOf[de.uka.ilkd.key.dl.model.Minus]), t, ot)
  def *(ot: Term): Term = TermBuilder.DF.func(RealLDT.getFunctionFor(classOf[de.uka.ilkd.key.dl.model.Mult]), t, ot)
  def /(ot: Term): Term = TermBuilder.DF.func(RealLDT.getFunctionFor(classOf[de.uka.ilkd.key.dl.model.Div]), t, ot)
  def ^(ot: Term): Term = TermBuilder.DF.func(RealLDT.getFunctionFor(classOf[de.uka.ilkd.key.dl.model.Exp]), t, ot)

  def equal(ot: Term): Term = TermBuilder.DF.equals(t, ot)
  def unequal(ot: Term): Term = TermFactory.DEFAULT.createTerm(RealLDT.getFunctionFor(classOf[de.uka.ilkd.key.dl.model.Unequals]),
    Array(t, ot),
    Array(new ImmutableArray()),
    t.javaBlock)
  def lt(ot: Term): Term = TermFactory.DEFAULT.createTerm(RealLDT.getFunctionFor(classOf[de.uka.ilkd.key.dl.model.Less]),
    Array(t, ot),
    Array(new ImmutableArray()),
    t.javaBlock)
  def gt(ot: Term): Term = TermFactory.DEFAULT.createTerm(RealLDT.getFunctionFor(classOf[de.uka.ilkd.key.dl.model.Greater]),
    Array(t, ot),
    Array(new ImmutableArray()),
    t.javaBlock)
  def leq(ot: Term): Term = TermFactory.DEFAULT.createTerm(RealLDT.getFunctionFor(classOf[de.uka.ilkd.key.dl.model.LessEquals]),
    Array(t, ot),
    Array(new ImmutableArray()),
    t.javaBlock)
  def geq(ot: Term): Term = TermFactory.DEFAULT.createTerm(RealLDT.getFunctionFor(classOf[de.uka.ilkd.key.dl.model.GreaterEquals]),
    Array(t, ot),
    Array(new ImmutableArray()),
    t.javaBlock)

}

object False {
  def apply: Term = TermBuilder.DF.ff()
  def unapply(t: Term): Boolean = t.op == Op.FALSE
}

object True {
  def apply: Term = TermBuilder.DF.tt()
  def unapply(t: Term): Boolean = t.op == Op.TRUE
}

class IsOp(c: java.lang.Class[_ <: de.uka.ilkd.key.dl.model.DLTerminalProgramElement]) {
  def check(op: Operator): Boolean = op == RealLDT.getFunctionFor(c)
  def unapply(o: Operator): Boolean = check(o)
}

class IsUnaryOp(c: java.lang.Class[_ <: de.uka.ilkd.key.dl.model.DLTerminalProgramElement]) extends IsOp(c) {
  def unapply(t: Term): Option[Term] = if (check(t.op)) Some(t.sub(0)) else None
}

class IsBinaryOp(c: java.lang.Class[_ <: de.uka.ilkd.key.dl.model.DLTerminalProgramElement]) extends IsOp(c) {
  def unapply(t: Term): Option[(Term, Term)] = if (check(t.op)) Some((t.sub(0), t.sub(1))) else None
}

object MinusSign extends IsUnaryOp(classOf[de.uka.ilkd.key.dl.model.MinusSign])
object Plus extends IsBinaryOp(classOf[de.uka.ilkd.key.dl.model.Plus])
object Minus extends IsBinaryOp(classOf[de.uka.ilkd.key.dl.model.Minus])
object Mult extends IsBinaryOp(classOf[de.uka.ilkd.key.dl.model.Mult])
object Div extends IsBinaryOp(classOf[de.uka.ilkd.key.dl.model.Div])
object Exp extends IsBinaryOp(classOf[de.uka.ilkd.key.dl.model.Exp])
object Greater extends IsBinaryOp(classOf[de.uka.ilkd.key.dl.model.Greater])
object GreaterEquals extends IsBinaryOp(classOf[de.uka.ilkd.key.dl.model.GreaterEquals])
object Less extends IsBinaryOp(classOf[de.uka.ilkd.key.dl.model.Less])
object LessEquals extends IsBinaryOp(classOf[de.uka.ilkd.key.dl.model.LessEquals])
object UnEquals extends IsBinaryOp(classOf[de.uka.ilkd.key.dl.model.Unequals])

object Equals extends BinaryLogicOperator(Op.EQUALS) {
  def apply(t: RichTerm, t2: Term): Term = t equal t2
}

object MathFun {
  def unapply(t: Term): Option[(String, Seq[Term])] = {
    if(t.sort != RealLDT.getRealSort) return None
    t.op match {
      case f: RigidFunction if f.isMathFunction() => 
        Some(f.name.toString, for(i <- 0 until t.arity) yield t.sub(i)) 
      case _ => None
    }
  }
  def apply(f: String, args: Seq[Term], s: Services) : Term = {
    val fun = s.getNamespaces().functions().lookup(new Name(f))
    require(fun != null, "The function " + f + " has to be declared.");
    require(fun.asInstanceOf[RigidFunction].isMathFunction(), "The " +
    		"function " + f + " has to be declared as \\external.");
    val mf = fun.asInstanceOf[RigidFunction]
    TermBuilder.DF.func(mf, args.toArray)
  }
}

object Constant {
  def unapply(o: Operator): Option[String] = {
    if (o.isInstanceOf[LogicVariable]
      || o.isInstanceOf[QuantifiableVariable]
      || o.isInstanceOf[ProgramVariable]
      || o.isInstanceOf[LocationVariable])
      Some(o.name.toString)
    else None
  }
  def unapply(t: Term): Option[String] = {
    // if(t.sort != RealLDT.getRealSort) return None
    if (t.arity == 0) {
      Some(t.op.name.toString)
    } else {
      unapply(t.op)
    }
  }
}

object Function {
  def unapply(t: Term): Option[(String, Seq[Term])] = {
    if(t.sort != RealLDT.getRealSort) return None
    if (t.op.isInstanceOf[RigidFunction]) {
      t match {
        // exclude all built-in functions
        case Equals(_, _) => None
        case UnEquals(_, _) => None
        case Greater(_, _) => None
        case GreaterEquals(_, _) => None
        case LessEquals(_, _) => None
        case Less(_, _) => None
        case MinusSign(_) => None
        case Plus(_, _) => None
        case Minus(_, _) => None
        case Mult(_, _) => None
        case Div(_, _) => None
        case Exp(_, _) => None
        case _ =>
          val children = for (i <- 0 until t.arity) yield t.sub(i)
          Some((t.op.name.toString, children))
      }
    } else None
  }
}

object NonRigidFunction {
  def unapply(t: Term) : Option[(String, Seq[Term])] = { 
    if(t.op.isInstanceOf[de.uka.ilkd.key.logic.op.NonRigidFunctionLocation]) {
      val children = for (i <- 0 until t.arity) yield t.sub(i)
      Some((t.op.name.toString, children))
    } else None
  }
}

object Ex {

  def apply(t: Term, v: LogicVariable): Term = TermBuilder.DF.ex(Array[QuantifiableVariable](v), t)

  def apply(t: Term, orgvars: ImmutableArray[QuantifiableVariable]): Term = {
    val vars = new Array[QuantifiableVariable](orgvars.size);
    for (i <- 0 until orgvars.size()) {
      vars(i) = orgvars.get(i)
    }
    TermBuilder.DF.ex(vars, t)
  }
  def unapply(t: Term): Option[(Term, ImmutableArray[QuantifiableVariable])] = {
    if (t.op == Op.EX)
      Some((t.sub(0), t.varsBoundHere(0)))
    else
      None
  }
}

object All {

  def apply(t: Term, v: LogicVariable): Term = TermBuilder.DF.all(v, t)

  def apply(t: Term, orgvars: ImmutableArray[QuantifiableVariable]): Term = {
    val vars: Array[QuantifiableVariable] = new Array[QuantifiableVariable](orgvars.size);
    for (i <- 0 until orgvars.size()) {
      vars(i) = orgvars.get(i)
    }
    TermBuilder.DF.all(vars, t)
  }
  def unapply(t: Term): Option[(Term, ImmutableArray[QuantifiableVariable])] = {
    if (t.op == Op.ALL)
      Some((t.sub(0), t.varsBoundHere(0)))
    else
      None
  }
}

class UnaryLogicOperator(o: Operator) {
  def unapply(t: Term): Option[Term] = {
    if (t.op == o && t.arity == 1) {
      Some(t.sub(0))
    } else {
      None
    }
  }
}

class BinaryLogicOperator(o: Operator) {
  def unapply(t: Term): Option[(Term, Term)] = {
    if (t.op == o) {
      if (t.arity == 2) {
        Some((t.sub(0), t.sub(1)))
      } else {
        var args = new Array[Term](t.arity - 1);
        for (i <- 1 until t.arity) {
          args(i - 1) = t.sub(i)
        }
        val rest = TermFactory.DEFAULT.createTerm(Op.EQV, args, Array(new ImmutableArray()),
          t.javaBlock)
        Some(t.sub(0), rest)
      }
    } else {
      None
    }
  }
}

class Modality(o: Operator) {

  def unapply(t: Term): Option[(JavaBlock, Term)] = {
    if (t.op == o) Some((t.javaBlock, t.sub(0))) else None
  }
}

object Not extends UnaryLogicOperator(Op.NOT) {
  def apply(t1: RichTerm): Term = (!t1)
}

object And extends BinaryLogicOperator(Op.AND) {
  def apply(t1: RichTerm, t2: Term): Term = (t1 & t2)
}

object Or extends BinaryLogicOperator(Op.OR) {
  def apply(t1: RichTerm, t2: Term): Term = (t1 | t2)
}

object Imp extends BinaryLogicOperator(Op.IMP) {
  def apply(t1: RichTerm, t2: Term): Term = (t1 -> t2)
}

object Eqv extends BinaryLogicOperator(Op.EQV) {
  def apply(t1: RichTerm, t2: Term): Term = (t1 <-> t2)
}

object Box extends Modality(Op.BOX) {
  def apply(j: JavaBlock, t: Term): Term = TermBuilder.DF.box(j, t)
}
object Dia extends Modality(Op.DIA) {
  def apply(j: JavaBlock, t: Term): Term = TermBuilder.DF.dia(j, t)
}
