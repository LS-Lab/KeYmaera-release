package edu.cmu.cs.ls.lyusimul

/*
object Main {
  def main(args: Array[String]) {
    // explicitly instantiate Arithmetic
    println("a+b+c: " + Arithmetic(Plus, Var("a"), Var("b"), Var("c")))
    
    // use operators
    println("a+b+c" + Var("a") + Var("b") + Var("c"))
    
    // use apply/unapply
    val t3 = Plus(Var("a"), Var("b"), Var("c"))
    println("a+b+c" + t3)
    println("unapply +: " + Plus.unapply(t3))
    
    // operator precedence inherited from scala
    println((Var("a") + Var("b")) * Var("c") ^ Var("d"))
    
    // explicitly right-associative
    println("explicitly right-associative: " + (Var("a") ^ (Var("b") ^ Var("c"))))
    
    // operator unclear
    println("should fail: " + (Var("a") ^ Var("b") ^ Var("c")))
    
    // operator unclear
    println("should fail: " + Arithmetic(Power, Var("a"), Var("b"), Var("c")))
  }
}
*/