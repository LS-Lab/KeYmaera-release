package edu.cmu.cs.ls

import scala.math.BigInt.int2bigInt
import scala.annotation.elidable
import scala.annotation.elidable._

object Exact {

  val zero : Num = Integer(0);
  val one : Num = Integer(1);
  val negone : Num = Integer(-1);


  trait Num {
    def +(that: Num): Num
    def -(that: Num): Num
    def unary_- : Num
    def *(that: Num): Num
    def /(that: Num): Num
    def <(that: Num): Boolean
    def <=(that: Num): Boolean
    def >(that: Num): Boolean = (that < this)
    def >=(that: Num): Boolean = (that <= this)
    def ==(that: Num): Boolean = (this <= that && that <= this)
    def !=(that: Num): Boolean = !(this == that)
    def is_positive : Boolean
    def is_zero : Boolean
    def is_one : Boolean
    def intValue : Int
    def compare(that: Num): Int = {
      val d = this - that;
      if (d.is_positive) 1 else if (d.is_zero) 0 else -1
    }
  }
  
  

 case class Rational(p: BigInt, q: BigInt) extends Num  {
  validate
  @elidable(ASSERTION) def validate {
    require (q > 0)
  }

  def this(p: Int, q: Int) = this(BigInt(p), BigInt(q));
  def this(n: Int) = this(BigInt(n), BigInt(1));
  def this(n: BigInt) = this(n, BigInt(1));
  def this(s: String) = this(BigInt(s), 1);

  def +(that: Num): Num = that match {
    case Rational(p1,q1) => new Rational(p * q1 + p1 * q, q * q1)
    case Integer(m) => new Rational(p + m * q, q)
  }

  def -(that: Num): Num = that match {
    case Rational(p1,q1) => new Rational(p * q1 - p1 * q, q * q1)
    case Integer(m) => new Rational(p - m*q, q)
  }


  def unary_- : Num = {
    new Rational(-p, q).reduce
  }

  def *(that: Num): Num = that match {
    case Rational(p1,q1) => new Rational(p * p1, q * q1)
 //   case num@Int(m) if num.is_one => this
 //   case num@Int(m) if num.is_zero => num
    case Integer(m) => new Rational(p * m, q)
  }

  def /(that: Num): Num = that match {
    case Rational(p1,q1) => if (p1 > 0) new Rational(p * q1, q * p1) else new Rational(-p * q1, q * -p1)
 //   case num@Int(m) if num.is_one => this
    case Integer(m) => if (m > 0) new Rational(p, q * m) else new Rational(-p, q * -m)
  }

  def <(that: Num): Boolean = that match {
    case Rational(p1,q1) => p * q1 < q * p1
    case Integer(m) => m * q < p
  }
  
  def <=(that: Num): Boolean = that match {
    case Rational(p1,q1) => p * q1 <= q * p1
    case Integer(m) => m * q <= p
  }
  
  override def ==(that: Num): Boolean = that match {
    case Rational(p1,q1) => p * q1 == q * p1
    case Integer(m) => m * q == p
  }
  
  def is_positive : Boolean = {
    (p * q).signum == 1
  }

  def is_zero : Boolean = {
    p.signum == 0;
  }

  def is_one : Boolean = {
    p == q;
  }

  def intValue : Int = {
    p.intValue / q.intValue
  }

  def reduce : Num = {
    val g = p gcd q;
    if (g == q) new Integer(p/g) else new Rational(p/g, q/g)
  }
  
  override def toString = {
    if(q == BigInt(1)) p.toString
    else {p.toString + "/" + q.toString}
  }

}

 object Rational {
   def normalize(p: BigInt, q: BigInt): Rational = {
	 var sign = 1
	 var num = p
	 var denom = q
	 if (p < 0) {
	   sign = -1
	   num = -p
	 }
	 if (q < 0) {
	   sign = -sign
	   denom = -q
	 }
	 new Rational(sign * num, denom)
   }
 }

 case class Integer(n: BigInt) extends Num  {

  def this(n: Int) = this(BigInt(n));
  def this(s: String) = this(BigInt(s));

  def +(that: Num): Num = that match {
    case Rational(p,q) => new Rational(q * n + p, q)
    case Integer(m) => new Integer(n + m)
  }


  def -(that: Num): Num = that match {
    case Rational(p,q) => new Rational(q * n - p, q)
    case Integer(m) => new Integer(n - m)
  }

  def unary_- : Num = {
    new Integer(-n)
  }

  def *(that: Num): Num = that match {
    case Rational(p,q) => new Rational(p * n, q)
    case Integer(m) => new Integer(n * m)
  }

  def /(that: Num): Num = that match {
    case Rational(p,q) => if (p > 0) new Rational(q * n, p) else new Rational(-q * n, -p)
    case Integer(m) => if (m > 0) new Rational(n, m) else new Rational(-n, -m)
  }

  def <(that: Num): Boolean = that match {
    case Rational(p,q) => n * q < p
    case Integer(m) => n < m
  }

  def <=(that: Num): Boolean = that match {
    case Rational(p,q) => n * q <= p
    case Integer(m) => n <= m
  }
  
  override def ==(that: Num): Boolean = that match {
    case Rational(p,q) => n * q == p
    case Integer(m) => n == m
  }

  def is_positive : Boolean = {
    n.signum == 1
  }

  def is_zero : Boolean = {
    n.signum == 0;
  }

  def is_one : Boolean = {
    n == BigInt(1);
  }

  def intValue : Int = {
    n.intValue
  }

  override def toString = {
    n.toString
  }

 }

}
