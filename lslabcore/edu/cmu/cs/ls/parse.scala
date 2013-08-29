
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

class DLLexical extends StdLexical {
  override def identChar = letter | elem('_') | elem('\'')
  def rp: RegexParsers = new RegexParsers {}
  override val whitespace: Parser[Any] = rp.regex("""(\s|//.*|(?m)/\*(\*(?!/)|[^*])*\*/)*""".r).asInstanceOf[Parser[Any]]
}

class DLParser(ins : String) extends StdTokenParsers {
  type Tokens = StdLexical 
  val lexical = new DLLexical
  lexical.delimiters ++= List(",", ";",":", "(", ")", "[", "]", "{", "}",
                              "=", "!=", "<", ">", ">=", "<=",
                              "+","-","*", "/", "^",
                              "++", ":=", "@", "?", "\'",
                              "&", "|", "->", "<->", "!"
                            ).iterator

  lexical.reserved ++= List("forall", "exists",
                             "true", "false",
                             "invariant", "variant",
                             "if", "fi", "then", "else"
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
//      atomicTerm~"^"~numericLit ^^ {case x~"^"~y => x^Num(Exact.Integer(Integer.parseInt(y)))} |
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

//   def formula00 : Parser[Formula] =
//     "forall" ~> ident ~ "."~ formula00 ^^
//               { case x ~ "." ~ f => Quantifier(Forall, x, Real, f)} |
//     "exists" ~> ident ~ "."~ formula00 ^^
//               { case x ~ "." ~ f => Quantifier(Exists, x, Real, f)} |
//     "forall" ~> ident ~ ":" ~ ident ~ "." ~ formula00 ^^
//               { case x ~ ":" ~ "Real" ~ "." ~ f => Quantifier(Forall, x, Real, f)
//                 case x ~ ":" ~ c ~ "." ~ f => Quantifier(Forall, x, St(c), f)
//               } |
//     "exists" ~> ident ~ ":" ~ ident ~ "." ~ formula00 ^^
//               { case x ~ ":" ~ "Real" ~ "." ~ f => Quantifier(Exists, x, Real, f)
//                 case x ~ ":" ~ c ~ "." ~ f => Quantifier(Exists, x, St(c), f)
//               } |
//     formula0

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
     "(" ~> formula0 /*formula00*/ <~  ")" |
     pred |
     "true" ^^^ True |
     "false" ^^^ False |
     // XXX doesn't work right for e.g. "[hp] forall x . ..."
     ("[" ~> hp <~ "]")~formula4 ^^ {case a~f => Modality(Box, a, f)} |
     ("<" ~> hp <~ ">")~formula4 ^^ {case a~f => Modality(Diamond, a, f)}

   def formula : Parser[Formula] = formula0 /*formula00*/

   def hp : Parser[HP] =
     hp1* (";" ^^^ {(p1:HP, p2:HP) => (p1, p2) match {
       case (_, EmptyHP) => p1
       case (EmptyHP, _) => p2
       case _ => p1 seq p2
       }
     })

   def hp1 : Parser[HP] =
     hp2* ("++" ^^^ {(p1:HP, p2:HP) => p1 ++ p2}) |
     success(EmptyHP)
     
   def hp2 : Parser[HP] =
     // TODO: look into phaver notation for loops, TODO: maybe * without {}
     ("{" ~> hp <~ "}")~opt("*"~opt(annotation("invariant"))) ^^ 
     	{case hp~star => star map {_ => Star(hp)} getOrElse hp} |
     "?" ~> formula0 /*formula00*/ ^^ { x => Check(x)}  |
     ident <~ ":=" <~ "*" ^^ { x => AssignAny(Var(x))} |
     (ident <~ ":=")~term ^^ {case x~t => Assign(Var(x), t)} |
     ("if" ~> "(" ~> formula0 <~ ")" <~ "{")~(hp <~ "}")~
     	opt("else" ~> "{" ~> hp <~ "}") ^^ 
     	{case f~thenhp~elsehp => (Check(f) seq thenhp) ++ (elsehp map (hp => Check(!f) seq hp) getOrElse Check(!f))} |
     // TODO: allow scattered evolution domain
     // TODO: introduce a new DAL data type and parse rule
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

//  def functionsort : Parser[(String,(List[Sort],Sort))] =
//    (ident <~ ":" <~ "(") ~ (repsep(sort,",") <~ ")" <~ "->") ~ sort ^^
//      {case f ~ args ~ rtn => (f,(args,rtn))}
//
//  def functionsorts : Parser[Map[String,(List[Sort],Sort)]] =
//    "{" ~> repsep(functionsort, ",") <~ "}" ^^
//       {case fnsrts => scala.collection.immutable.HashMap.empty ++ fnsrts } |
//        success(scala.collection.immutable.HashMap.empty)

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

object P {
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


