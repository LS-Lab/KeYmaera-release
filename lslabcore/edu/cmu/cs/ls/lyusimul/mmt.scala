/** Mmt: Min-max term */

package edu.cmu.cs.ls.lyusimul

import edu.cmu.cs.ls._
import scala.annotation.elidable
import scala.annotation.elidable._

sealed abstract class Mmt
case class MmtNum(n: Exact.Num) extends Mmt
case class MmtVar(s: String) extends Mmt
case class MmtPosInfty() extends Mmt
case class MmtNegInfty() extends Mmt
case class MmtArithmetic(op: MmtArithmeticOp, ps: Mmt*) extends Mmt {
  op.applicable(ps:_*)
}
// Predefined function
case class MmtPredFn(f: MmtPredFnOp, ps: Mmt*) extends Mmt {
  /* deboPreguntar: OK not to have f.applicable? */
  /*f.applicable(ps:_*)*/
}

sealed abstract class MmtPredFnOp {
  /* deboPreguntar: OK not to have the assertion? */
  /*@elidable(ASSERTION) def applicable(ps: Mmt*)*/
}
case object MmtMax extends MmtPredFnOp
case object MmtMin extends MmtPredFnOp

sealed abstract class MmtArithmeticOp {
  @elidable(ASSERTION) def applicable(ps: Mmt*)
}

sealed abstract class MmtUnaryArithmeticOp extends MmtArithmeticOp {
  def unapply(t: Mmt): Option[Mmt] = t match {
    case MmtArithmetic(op, ps @ _*) => applicable(ps:_*); if (op == this) Some(ps.toList.head) else None
	case _ => None
  }
  @elidable(ASSERTION) final def applicable(ps: Mmt*) {
	require(ps.length == 1, "one term expected")
  }
}
sealed abstract class MmtBinaryArithmeticOp(strict: Boolean = false) extends MmtArithmeticOp {
  def unapply(t: Mmt): Option[(Mmt, Mmt)] = t match {
    case MmtArithmetic(op, ps @ _*) =>
      	applicable(ps:_*)
	    if (op == this) {
	      if (ps.length == 2) {
	        Some((ps.head, ps.tail.head))
	      } else {
	        val rps = ps.reverse
	        Some(rps.head, MmtArithmetic(this, rps.tail.reverse:_*))
	      }
	    } else {
	      None
	    }
    case _ => None
  }
  @elidable(ASSERTION) final def applicable(ps: Mmt*) {
	require(if (strict) ps.length == 2 else ps.length >= 2, 
	    if (strict) "two terms expected" else "two or more terms expected")
	if (strict) require(ps match {
		// we cannot distinguish between a^b^c and (a^b)^c => both are errors
	  	case Seq(t1: MmtArithmetic, _) => t1.op != this
	  	// we do not assert the dual, because it means a^(b^c) explicitly
	  	//case Seq(_, t2: MmtArithmetic) => t2.op != this
	    case _ => true
	}, "same consecutive operators of strictly binary form not allowed")
  }
}
case object MmtPlus extends MmtBinaryArithmeticOp {
  /*def apply(t1: Mmt, t2: Mmt, ts: Mmt*): Mmt */
  /*	= MmtArithmetic(Plus, List.concat(List(t1, t2), ts):_*)*/
}
case object MmtSubtract extends MmtBinaryArithmeticOp {
  /*def apply(t1: Mmt, t2: Mmt, ts: Mmt*): Mmt */
  /*	= MmtArithmetic(Subtract, List.concat(List(t1, t2), ts):_*)*/
}
case object MmtMultiply extends MmtBinaryArithmeticOp {
  /*def apply(t1: Mmt, t2: Mmt, ts: Mmt*): Mmt */
  /*	= MmtArithmetic(Multiply, List.concat(List(t1, t2), ts):_*)*/
}
case object MmtDivide extends MmtBinaryArithmeticOp {
  /*def apply(t1: Mmt, t2: Mmt, ts: Mmt*): Mmt */
  /*	= MmtArithmetic(Divide, List.concat(List(t1, t2), ts):_*)*/
}
case object MmtPower extends MmtBinaryArithmeticOp(true) {
  /*def apply(t1: Mmt, t2: Mmt): Mmt = (t1 ^ t2)*/
}
case object MmtModulo extends MmtBinaryArithmeticOp(true) {
  /*def apply(t1: Mmt, t2: Mmt): Mmt = (t1 % t2)*/
}
/** Unary negation */
case object MmtNegate extends MmtUnaryArithmeticOp {
  /*def apply(t: Mmt): Mmt = (-t)*/
}
