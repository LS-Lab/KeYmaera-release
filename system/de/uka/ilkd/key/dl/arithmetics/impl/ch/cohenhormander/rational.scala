package de.uka.ilkd.key.dl.arithmetics.impl.ch.cohenhormander;


// exact nums are either rationals or integers. Is this worth it?

trait ExactNum {
  def +(that: ExactNum): ExactNum 
  def -(that: ExactNum): ExactNum 
  def unary_- : ExactNum 
  def *(that: ExactNum): ExactNum
  def /(that: ExactNum): ExactNum
  def <(that: ExactNum): Boolean 
  def <=(that: ExactNum): Boolean
  def >(that: ExactNum): Boolean
  def >=(that: ExactNum): Boolean
  def ==(that: ExactNum): Boolean
  def is_positive : Boolean
  def is_zero : Boolean
  def is_one : Boolean
  def intValue : Int
  def compare(that: ExactNum): Int
}



case class Rational(p: BigInt, q: BigInt) extends ExactNum  {
// This check eats a lot of time!
//  require(q != 0);
  

  def this(p: Int, q: Int) = this(BigInt(p),BigInt(q));
  def this(n: Int) = this(BigInt(n),BigInt(1));
  def this(n: BigInt) = this(n,BigInt(1));
  def this(s: String) = this(BigInt(s),1);

  def +(that: ExactNum): ExactNum = that match {
    case Rational(p1,q1) => 
     new Rational(p * q1 + p1 * q, q * q1)
    case ExactInt(m) => new Rational(p + m * q, q)
  } 

  def -(that: ExactNum): ExactNum = that match {
    case Rational(p1,q1) => 
     new Rational(p * q1 - p1 * q, q * q1)
    case ExactInt(m) => new Rational(p - m*q, q)
  }


  def unary_- : ExactNum = {
    (new Rational( - p, q)).reduce;
  }

  def *(that: ExactNum): ExactNum = that match {
    case Rational(p1,q1) => new Rational(p * p1, q * q1)
 //   case num@ExactInt(m) if num.is_one => this
 //   case num@ExactInt(m) if num.is_zero => num
    case ExactInt(m) => new Rational(p * m, q)
  }

  def /(that: ExactNum): ExactNum = that match {
    case Rational(p1,q1) => new Rational(p * q1, q * p1)
 //   case num@ExactInt(m) if num.is_one => this
    case ExactInt(m) => new Rational(p , q * m)
  }

  def <(that: ExactNum): Boolean = {
    (that - this).is_positive
  }
  def <=(that: ExactNum): Boolean = {
    val v = that - this;
    v.is_positive || v.is_zero
  }
  def >(that: ExactNum): Boolean = {
    (this - that).is_positive
  }
  def >=(that: ExactNum): Boolean = {
    val v = this - that;
    v.is_positive || v.is_zero
  }
  def ==(that: ExactNum): Boolean = that match {
    case Rational(p1,q1) => p * q1 == q * p1
    case ExactInt(m) => m * q == p
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

  def reduce : ExactNum = {
    val g = p gcd q;
    if(g == q) new ExactInt(p/g)
    else  new Rational(p/g, q/g)
  }

  def compare(that: ExactNum): Int  = {
    val d = this - that;
    if(d.is_positive) 1
    else if(d.is_zero) 0
    else  -1
  }

  override def toString = {
    if(q == BigInt(1)) p.toString 
    else {p.toString + "/" + q.toString}
  }

}


case class ExactInt(n: BigInt) extends ExactNum  {
// This check eats a lot of time!
//  require(q != 0);
  

  def this(n: Int) = this(BigInt(n));
  def this(s: String) = this(BigInt(s));

  def +(that: ExactNum): ExactNum = that match {
    case Rational(p,q) => new Rational(q * n + p, q)
    case ExactInt(m) => new ExactInt(n + m)
  }


  def -(that: ExactNum): ExactNum = that match {
    case Rational(p,q) => new Rational(q * n - p, q)
    case ExactInt(m) => new ExactInt(n - m)
  }

  def unary_- : ExactNum = {
    new ExactInt(-n)
  }

  def *(that: ExactNum): ExactNum = that match {
    case Rational(p,q) => new Rational(p * n, q)
    case ExactInt(m) => new ExactInt(n + m)
  }

  def /(that: ExactNum): ExactNum = that match {
    case Rational(p,q) => new Rational(q * n, p)
    case ExactInt(m) => new Rational(n,m)
  }

  def <(that: ExactNum): Boolean = {
    (that - this).is_positive
  }

  def <=(that: ExactNum): Boolean = {
    val v = that - this;
    v.is_positive || v.is_zero
  }

  def >(that: ExactNum): Boolean = {
    (this - that).is_positive
  }

  def >=(that: ExactNum): Boolean = {
    val v = this - that;
    v.is_positive || v.is_zero
  }

  def ==(that: ExactNum): Boolean = that match {
    case Rational(p,q) => n * q == p
    case ExactInt(m) => n == m
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


  def compare(that: ExactNum): Int  = {
    val d = this - that;
    if(d.is_positive) 1
    else if(d.is_zero) 0
    else  -1
  }

  override def toString = {
    n.toString
  }

}

