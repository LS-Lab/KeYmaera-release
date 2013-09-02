package edu.cmu.cs.ls

import scala.annotation.elidable
import scala.annotation.elidable._

/** Sorts like reals or the sort C of cars. */
sealed abstract class Sort
case object Real extends Sort
case object AnySort extends Sort
/** Object sort called nm. */
case class St(nm: String) extends Sort

/** Terms denoting numbers and other objects. */
sealed abstract class Term {
  def +(that: Term): Term = Arithmetic(Plus, this, that)
  def -(that: Term): Term = Arithmetic(Subtract, this, that)
  def *(that: Term): Term = Arithmetic(Multiply, this, that)
  def /(that: Term): Term = Arithmetic(Divide, this, that)
  def ^(that: Term): Term = Arithmetic(Power, this, that)
  def %(that: Term): Term = Arithmetic(Modulo, this, that)
  def unary_- : Term = Arithmetic(Negate, this)
  def eq(that: Term): Formula = ArithmeticPred(Equals, this, that)
  def neq(that: Term): Formula = ArithmeticPred(NotEquals, this, that)
  def <(that: Term): Formula = ArithmeticPred(Less, this, that)
  def <=(that: Term): Formula = ArithmeticPred(LessEquals, this, that)
  def >(that: Term): Formula = ArithmeticPred(Greater, this, that)
  def >=(that: Term): Formula = ArithmeticPred(GreaterEquals, this, that)
}
/** A number literal n. */
case class Num(n: Exact.Num) extends Term
/** A variable of name s. */
case class Var(s: String) extends Term {
  def :=(that: Term): HP = Assign(this, that)
  def :=* : HP = AssignAny(this)
}
/** Interpreted arithmetic function with operator op applied to arguments ps. */
case class Arithmetic(op: ArithmeticOp, ps: Term*) extends Term {
  op.applicable(ps:_*)
}
/** Uninterpreted function f applied to arguments ps. */
case class Fn(f: String, ps: Term*) extends Term

/** Arithmetic operators. */
sealed abstract class ArithmeticOp {
  @elidable(ASSERTION) def applicable(ps: Term*)
}
sealed abstract class UnaryArithmeticOp extends ArithmeticOp {
  def unapply(t: Term): Option[Term] = t match {
    case Arithmetic(op, ps @ _*) => applicable(ps:_*); if (op == this) Some(ps.toList.head) else None
	case _ => None
  }
  @elidable(ASSERTION) final def applicable(ps: Term*) {
	require(ps.length == 1, "one term expected")
  }
}
sealed abstract class BinaryArithmeticOp(strict: Boolean = false) extends ArithmeticOp {
  def unapply(t: Term): Option[(Term, Term)] = t match {
    case Arithmetic(op, ps @ _*) =>
      	applicable(ps:_*)
	    if (op == this) {
	      if (ps.length == 2) {
	        Some((ps.head, ps.tail.head))
	      } else {
	        val rps = ps.reverse
	        Some(rps.head, Arithmetic(this, rps.tail.reverse:_*))
	      }
	    } else {
	      None
	    }
    case _ => None
  }
  @elidable(ASSERTION) final def applicable(ps: Term*) {
	require(if (strict) ps.length == 2 else ps.length >= 2, 
	    if (strict) "two terms expected" else "two or more terms expected")
	if (strict) require(ps match {
		// we cannot distinguish between a^b^c and (a^b)^c => both are errors
	  	case Seq(t1: Arithmetic, _) => t1.op != this
	  	// we do not assert the dual, because it means a^(b^c) explicitly
	  	//case Seq(_, t2: Arithmetic) => t2.op != this
	    case _ => true
	}, "same consecutive operators of strictly binary form not allowed")
  }
}
case object Plus extends BinaryArithmeticOp {
  def apply(t1: Term, t2: Term, ts: Term*): Term 
  	= Arithmetic(Plus, List.concat(List(t1, t2), ts):_*)
}
case object Subtract extends BinaryArithmeticOp {
  def apply(t1: Term, t2: Term, ts: Term*): Term 
  	= Arithmetic(Subtract, List.concat(List(t1, t2), ts):_*)
}
case object Multiply extends BinaryArithmeticOp {
  def apply(t1: Term, t2: Term, ts: Term*): Term 
  	= Arithmetic(Multiply, List.concat(List(t1, t2), ts):_*)
}
case object Divide extends BinaryArithmeticOp {
  def apply(t1: Term, t2: Term, ts: Term*): Term 
  	= Arithmetic(Divide, List.concat(List(t1, t2), ts):_*)
}
case object Power extends BinaryArithmeticOp(true) {
  def apply(t1: Term, t2: Term): Term = (t1 ^ t2)
}
case object Modulo extends BinaryArithmeticOp(true) {
  def apply(t1: Term, t2: Term): Term = (t1 % t2)
}
/** Unary negation */
case object Negate extends UnaryArithmeticOp {
  def apply(t: Term): Term = (-t)
}

/** Logical formulas. */
sealed abstract class Formula {
  def &(that: Formula): Formula = Prop(And, this, that)
  def |(that: Formula): Formula = Prop(Or, this, that)
  def ->(that: Formula): Formula = Prop(Imp, this, that)
  def <->(that: Formula): Formula = Prop(Iff, this, that)
  def unary_! : Formula = Prop(Not, this)
}
case object True extends Formula
case object False extends Formula
case class Atom(t: Term) extends Formula
/** Interpreted arithmetic predicate of operator op and terms ps. */
case class ArithmeticPred(op: Comparison, ps: Term*) extends Formula {
  op.applicable(ps:_*)
}
/** Uninterpreted predicate p applied to arguments ps. */
case class Pred(p: String, ps: Term*) extends Formula
/** A propositional logical operation with connective c applied to formulas fs. */
case class Prop(c : Connective, fs: Formula*) extends Formula {
  c.applicable(fs:_*)
}
/** Quantifier of kind k over variable v of sort c applied to formula f. */
case class Quantifier(k : QuantifierKind, v : String,
                      c: Sort, f: Formula) extends Formula
/** Modality operation that satisfies formula f */
case class Modality(m: ModalityOperator, hp: HP, f: Formula) extends Formula

/** Interpreted arithmetic comparison operators. */
sealed abstract class Comparison(strict: Boolean = false) {
  def unapply(f: Formula): Option[(Term, Formula)] = f match {
    case ArithmeticPred(op, ps @ _*) =>
      	applicable(ps:_*)
      	if (op == this) {
	      if (ps.length == 2) {
	        Some((ps.head, Atom(ps.tail.head)))
	      } else {
	        val rps = ps.reverse
	        Some(rps.head, ArithmeticPred(this, rps.tail.reverse:_*))
	      }
	    } else {
	      None
	    }
    case _ => None
  }
  @elidable(ASSERTION) final def applicable(ps: Term*) {
	require(if (strict) ps.length == 2 else ps.length >= 2,
	    if (strict) "two terms expected" else "two or more terms expected")
  }
}
case object Equals extends Comparison {
  def apply(t1: Term, t2: Term): Formula = (t1 eq t2)
}
case object NotEquals extends Comparison(true) {
  def apply(t1: Term, t2: Term): Formula = (t1 neq t2)
}
case object Less extends Comparison {
  def apply(t1: Term, t2: Term, ts: Term*): Formula 
  	= ArithmeticPred(Less, List.concat(List(t1, t2), ts):_*)
}
case object LessEquals extends Comparison {
  def apply(t1: Term, t2: Term, ts: Term*): Formula 
  	= ArithmeticPred(LessEquals, List.concat(List(t1, t2), ts):_*)
}
case object Greater extends Comparison {
  def apply(t1: Term, t2: Term, ts: Term*): Formula 
  	= ArithmeticPred(Greater, List.concat(List(t1, t2), ts):_*)
}
case object GreaterEquals extends Comparison {
  def apply(t1: Term, t2: Term, ts: Term*): Formula 
  	= ArithmeticPred(GreaterEquals, List.concat(List(t1, t2), ts):_*)
}

/** Propositional logical connectives. */
sealed abstract class Connective {
  @elidable(ASSERTION) def applicable(fs: Formula*)
}
sealed abstract class UnaryConnective extends Connective {
  def unapply(f: Formula): Option[Formula] = f match {
    case Prop(c, fs @ _*) => applicable(fs:_*); if (c == this) Some(fs.head) else None
    case _ => None
  }
  @elidable(ASSERTION) final def applicable(fs: Formula*) {
	require(fs.length == 1, "one formula expected")
  }
}
sealed abstract class BinaryConnective(strict: Boolean = false) extends Connective {
  def unapply(f: Formula): Option[(Formula, Formula)] = f match {
    case Prop(c, fs @ _*) =>
      	applicable(fs:_*)
	    if (c == this) {
	      if (fs.length == 2) {
	        Some((fs.head, fs.tail.head))
	      } else {
	        val rfs = fs.reverse
	        Some(rfs.head, Prop(this, rfs.tail.reverse:_*))
	      }
	    } else {
	      None
	    }
    case _ => None
  }
  @elidable(ASSERTION) final def applicable(fs: Formula*) {
	require(if (strict) fs.length == 2 else fs.length >= 2,
	    if (strict) "two formulas expected" else "two or more formulas expected")
	if (strict) require(fs match {
		// we cannot distinguish between a->b->c and (a->b)->c => both are errors
	  	case Seq(p1: Prop, _) => p1.c != this
	  	// we do not assert the dual, because it means a->(b->c) explicitly
	  	//case Seq(_, p2: Prop) => p2.c != this
	    case _ => true
	}, "same consecutive operators of strictly binary form not allowed")
  }
}
case object And extends BinaryConnective {
  def apply(f1: Formula, f2: Formula, fs: Formula*): Formula 
  	= Prop(And, List.concat(List(f1, f2), fs):_*)
}
case object Or extends BinaryConnective {
  def apply(f1: Formula, f2: Formula, fs: Formula*): Formula 
  	= Prop(Or, List.concat(List(f1, f2), fs):_*)
}
case object Imp extends BinaryConnective(true) {
  def apply(f1: Formula, f2: Formula): Formula = (f1 -> f2)
}
case object Iff extends BinaryConnective(true) {
  def apply(f1: Formula, f2: Formula): Formula = (f1 <-> f2)
}
case object Not extends UnaryConnective {
  def apply(f: Formula): Formula = (!f)
}

/** Kinds of quantifiers */
sealed abstract class QuantifierKind
case object Forall extends QuantifierKind
case object Exists extends QuantifierKind

/** Modality operators */
sealed abstract class ModalityOperator
case object Box extends ModalityOperator
case object Diamond extends ModalityOperator
case object TemporalBox extends ModalityOperator

/** goals */
case class Sequent(fn_sorts: Map[String, (List[Sort], Sort)],
                   ctxt: List[Formula],
                   scdts: List[Formula])
