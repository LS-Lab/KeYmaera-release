
package edu.cmu.cs.ls

import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.InputStream
import java.io.FileInputStream

import scala.util.parsing.combinator.lexical._
import scala.util.parsing.combinator.syntactical._
import scala.util.parsing.combinator._

class DLOriginalParser(ins : String) extends StdTokenParsers {
  type Tokens = StdLexical 
  val lexical = new DLLexical
  lexical.delimiters ++= List(",", ";",":", "(", ")", "\\[", "\\]", 
		  					  "\\<", "\\>", "{", "}",
                              "=", "!=", "<", ">", ">=", "<=",
                              "+","-","*", "/", "^",
                              "++", ":=", "@", "?", "\'",
                              "&", "|", "->", "<->", "!"
                            ).iterator

  lexical.reserved ++= List("forall", "exists",
                             "true", "false",
                             "invariant", "variant"
                           ).iterator

   def this(in : InputStream) = {
     this({
	     val br = new BufferedReader(new InputStreamReader(in))
	     var ins1 = ""
	     var ln = br.readLine()
	     while (ln != null){
	       println( ln)
	       ins1 = ins1 + ln + "\n"
	       ln = br.readLine()
	     }
	     println("input = " + ins1)
	     ins1
	     })
   }

   def term: Parser[Term] = 
     prod* ("+" ^^^ {(x:Term, y:Term) => x + y}
          | "-" ^^^ {(x: Term, y: Term) => x - y})

   def prod: Parser[Term] =
     factor* ("*" ^^^ {(x: Term, y: Term) => x * y}
            | "/" ^^^ {(x: Term, y: Term) =>  x / y}) | 
     "-" ~> prod ^^ { x: Term => -x}

   def factor: Parser[Term] =
      atomicTerm~"^"~atomicTerm ^^ {case x~"^"~y => x^y} |
      atomicTerm

   def function : Parser [Fn] =
      (ident <~ "(")~(repsep(term, ",") <~ ")") ^^ {case f~ps => Fn(f, ps:_*)}

   def atomicTerm : Parser[Term] =
     "(" ~> term <~  ")" |
     numericLit ^^ (x => Num(Exact.Integer(Integer.parseInt(x)))) |
     function |
     ident ^^ (x => Var(x))

   def pred : Parser[ArithmeticPred] =
     term~("=" ^^^ Equals | "!=" ^^^ NotEquals | "<" ^^^ Less | ">" ^^^ Greater 
         | "<=" ^^^ LessEquals | ">=" ^^^ GreaterEquals )~term ^^
       { case t1~r~t2 => ArithmeticPred(r, t1, t2)}

   def formula0 : Parser[Formula] =
     formula1* ( "<->" ^^^ {(f1:Formula, f2:Formula) => f1 <-> f2})

   // Implication is right-associative.
   def formula1 : Parser[Formula] =
      rep1sep(formula2, "->") ^^
        ((lst) => lst.reduceRight((f1:Formula, f2:Formula) => f1 -> f2))

   def formula2 : Parser[Formula] =
     formula3* ("|" ^^^ {(f1:Formula, f2:Formula) => f1 | f2})

   def formula3 : Parser[Formula] =
     formula4* ("&" ^^^ {(f1:Formula,f2:Formula) => f1 & f2})

   def formula4 : Parser[Formula] =
     "!" ~> formula5 ^^ {fm => !fm} |
     formula5

   def formula5 : Parser[Formula] =
     "(" ~> formula0 <~  ")" |
     pred |
     "true" ^^^ True |
     "false" ^^^ False |
     ("\\[" ~> hp <~ "\\]")~formula4 ^^ {case a~f => Modality(Box, a, f)} |
     ("\\<" ~> hp <~ "\\>")~formula4 ^^ {case a~f => Modality(Diamond, a, f)}

   def formula : Parser[Formula] = formula0

   def hp : Parser[HP] =
     hp1* (";" ^^^ {(p1:HP, p2:HP) => p1 seq p2})

   def hp1 : Parser[HP] =
     hp2* ("++" ^^^ {(p1:HP, p2:HP) => p1 ++ p2})
     
   def hp2 : Parser[HP] =
     ("(" ~> hp <~ ")")~opt("*"~opt(annotation("invariant"))) ^^ 
     	{case hp~star => star map {_ => Star(hp)} getOrElse hp} |
     ident <~ ":=" <~ "*" ^^ { x => AssignAny(Var(x))} |
     (ident <~ ":=")~term ^^ {case x~t => Assign(Var(x), t)} |     
     "?" ~> formula0 ^^ { x => Check(x)}  |
     ("if" ~> "(" ~> formula0 <~ ")" <~ "then")~hp~
     	opt("else" ~> hp) <~ "fi" ^^ 
     	{case f~thenhp~elsehp => (Check(f) seq thenhp) ++ (elsehp map (hp => Check(!f) seq hp) getOrElse Check(!f))}
     ("{" ~> rep1sep(diffeq, ",") <~ ",")~(formula0 <~ "}")~opt(annotation("invariant")) ^^
        {case dvs~f~invs => Evolve(f, dvs:_*)}

  def diffeq : Parser[(Var, Term)] =
    (ident <~ "=")~term ^?
      {case s~tm  if s.endsWith("\'") 
        	  => (Var(s.substring(0,s.length - 1)), tm)}

  def annotation(name: String) : Parser[List[Formula]] =
    "@" ~> keyword(name) ~> "(" ~> repsep(formula0, ",") <~ ")"

  def sort : Parser[Sort] =
    ident ^^ {case "R" => Real
              case s => St(s)}

  def functionsort : Parser[(String,(List[Sort],Sort))] =
    (ident <~ ":" <~ "(") ~ (repsep(sort,",") <~ ")" <~ "->") ~ sort ^^
      {case f ~ args ~ rtn => (f,(args,rtn))}

  def functionsorts : Parser[Map[String,(List[Sort],Sort)]] =
    "{" ~> repsep(functionsort, ",") <~ "}" ^^
       {case fnsrts => scala.collection.immutable.HashMap.empty ++ fnsrts } |
        success(scala.collection.immutable.HashMap.empty)

   def fm_result : Option[Formula] = {
     // don't infer var / fn distinction
     phrase(formula0)(new lexical.Scanner(ins)) match {
       case Success(r,next) if next.atEnd =>
         Some(r)
       case Success(r,next)  =>
         println("parse failure! Left over input. only parsed: " )
         println(r)
         None
       case f =>
//         println("failure!")
         println(f)
         None
     }
   }
}

object OP {
  def openFile(f: String) : InputStream = {
    new FileInputStream(f)
  }

  def parseFormula(f: String) : Formula = {
    val dlp = new DLParser(f)
    dlp.fm_result match {
      case Some(fm) => fm
      case None =>
        println("could not read a formula from " + f)
        False
    }
  }
}


