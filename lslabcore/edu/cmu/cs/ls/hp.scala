package edu.cmu.cs.ls

import scala.annotation.elidable
import scala.annotation.elidable._

/** Hybrid programs */
sealed abstract class HP {
  def seq(that: HP): HP = ComposedHP(Sequence, this, that)
  def ++(that: HP): HP = ComposedHP(Choice, this, that)
}
object HP extends HP {
  def loop(that: HP): HP = ComposedHP(Star, that)
  def ?(that: Formula): HP = Check(that)
}
/** Assign term t to variable v */
case class Assign(v: Var, t: Term) extends HP
/** Assign an arbitrary value to variable v */
case class AssignAny(v: Var) extends HP
/** Check formula h */
case class Check(h: Formula) extends HP
/** Evolve along differential equations var'=term with evolution domain h */
case class Evolve(h: Formula, primes: (Var,Term)*) extends HP {
  applicable(primes:_*)
  @elidable(ASSERTION) def applicable(primes: (Var,Term)*) {
    require(primes.length > 0)
  }
}
case object EmptyHP extends HP
/** Compose hybrid programs ps by operator op */
case class ComposedHP(op: HybridOperator, ps: HP*) extends HP {
 op.applicable(ps:_*)
 def flatten = ComposedHP(op, op.flatten(this):_*)
}

/** Base class for hybrid program operators */
sealed abstract class HybridOperator {
  @elidable(ASSERTION) def applicable(ps: HP*)
  def flatten(p: HP): Seq[HP] = Seq(p)
}
/** Base class for unary hybrid program operators */
sealed abstract class UnaryHybridOperator extends HybridOperator {
  def unapply(p: HP): Option[HP] = p match {
    case ComposedHP(op, ps @ _*) => applicable(ps:_*); if (op == this) Some(ps.toList.head) else None
	case _ => None
  }
  @elidable(ASSERTION) final def applicable(ps: HP*) {
	require(ps.length == 1, "one hybrid program expected")
  }
}

/** Base class for binary hybrid program operators */
sealed abstract class BinaryHybridOperator(strict: Boolean = false) extends HybridOperator {
  def unapply(p: HP): Option[(HP, HP)] = p match {
    case ComposedHP(op, ps @ _*) =>
      	applicable(ps:_*)
	    if (op == this) {
	      if (ps.length == 2) {
	        Some((ps.head, ps.tail.head))
	      } else {
	        val rps = ps.reverse
	        Some(rps.head, ComposedHP(this, rps.tail.reverse:_*))
	      }
	    } else {
	      None
	    }
    case _ => None
  }
  override def flatten(p: HP): Seq[HP] = p match {
    case ComposedHP(op, ps @ _*) => 
      if (op == this) {
    	  flatten(ps.head) ++ ps.tail.map(p => flatten(p)).flatten
      } else {
    	  Seq(p)
      }
    case _ => Seq(p)  
  }
  @elidable(ASSERTION) final def applicable(ps: HP*) {
	require(if (strict) ps.length == 2 else ps.length >= 2, 
	    if (strict) "two hybrid programs expected" else "two or more hybrid programs expected")
	if (strict) require(ps match {
		case Seq(p1: ComposedHP, _) => p1.op != this
	  	case Seq(_, p2: ComposedHP) => p2.op != this
	    case _ => true
	}, "same consecutive operators of strictly binary form not allowed")
  }
}
/** Repeat hybrid programs arbitrariliy often (0..) */
case object Star extends UnaryHybridOperator {
  def apply(p: HP): HP 
  	= ComposedHP(Star, p)
}
/** Execute hybrid programs in a sequence */
case object Sequence extends BinaryHybridOperator {
  def apply(p1: HP, p2: HP, ps: HP*): HP 
  	= ComposedHP(Sequence, List.concat(List(p1, p2), ps):_*) 
}
/** Non-deterministically choose between hybrid programs */
case object Choice extends BinaryHybridOperator {
  def apply(p1: HP, p2: HP, ps: HP*): HP 
  	= ComposedHP(Choice, List.concat(List(p1, p2), ps):_*)
}