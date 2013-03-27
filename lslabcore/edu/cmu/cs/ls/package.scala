package edu.cmu.cs

/**
 * Provides classes for representing numbers (rationals, integers), formulas, 
 * arithmetic terms and propositional logical predicates.
 * 
 * =Numbers=
 * The class [[edu.cmu.cs.lslab.Integer]] represents integer numbers, the class
 * [[edu.cmu.cs.lslab.Rational]] rational numbers.
 * 
 * =Sorts=
 * The class [[edu.cmu.cs.lslab.Sort]] is the base class for sort. The package 
 * provides the sort [[edu.cmu.cs.lslab.Real]] for describing real numbers,
 * [[edu.cmu.cs.lslab.AnySort]] denoting the top sort, as well as the class 
 * [[edu.cmu.cs.lslab.St]] for defining new named sorts whose name does not 
 * interfere with `R` (being the name of [[edu.cmu.cs.lslab.RealSort]]).
 * 
 * =Operations=
 * All operations in this package are defined for an arbitrary number of 
 * arguments. '''All operators are by default left-associative'''. Thus,
 * ''elidable'' assertions restrict the arguments for right-associative 
 * operations. Use `-Xelide-below ASSERTION` to remove those assertions at 
 * compile time.
 * 
 * Right-associative operations can be instantiated in a binary manner.
 * {{{
 * scala> // right-associative power
 * scala> val v = Arithmetic(Power, Var("a") 
 *                               :: Arithmetic(Power, Var("b") 
 *                                                 :: Var("c") 
 *                                                 :: Nil) 
 *                               :: Nil)
 * }}}
 * 
 * ==Terms==
 * The class [[edu.cmu.cs.lslab.Term]] is the base class for terms denoting 
 * numbers [[edu.cmu.cs.lslab.Num]], variables [[edu.cmu.cs.lslab.Var]], and 
 * other objects. The class [[edu.cmu.cs.lslab.Arithmetic]] represents 
 * interpreted arithmetic functions of a defined set of arithmetic operators. 
 * The operators are defined as cases of [[edu.cmu.cs.lslab.ArithmeticOp]]. The 
 * class [[edu.cmu.cs.lslab.Fn]] represents uninterpreted functions.
 * 
 * ==Formulas==
 * The class [[edu.cmu.cs.lslab.Formula]] is the base class for formulas: 
 * [[edu.cmu.cs.lslab.ArithmeticPred]] represents interpreted arithmetic 
 * predicates (i.e., comparisons), [[edu.cmu.cs.lslab.Pred]] represents 
 * uninterpreted predicates, [[edu.cmu.cs.lslab.Prop]] represents propositional 
 * logical predicates, and [[edu.cmu.cs.lslab.Quantifier]] represents 
 * existentially and universally quantified formulas. As with terms, the
 * logical propositional operators, logical connectives, and quantifier kinds 
 * are defined as case objects of [[edu.cmu.cs.lslab.Comparison]], 
 * [[edu.cmu.cs.lslab.Connective]], and [[edu.cmu.cs.lslab.QuantifierKind]],
 * respectively.  
 */
package object ls {

}