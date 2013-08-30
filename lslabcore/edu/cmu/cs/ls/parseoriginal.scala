
package edu.cmu.cs.ls

import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.InputStream
import java.io.FileInputStream
import scala.util.parsing.combinator.lexical._
import scala.util.parsing.combinator.syntactical._
import scala.util.parsing.input.CharArrayReader.EofCh
import scala.util.parsing.combinator._
import java.io.FileReader
import scala.collection.mutable.HashSet

class KeYLexical() extends StdLexical() {

  case class FloatingPointLit(chars: String) extends Token {
    override def toString = chars
  }

  override def token: Parser[Token] =
    ( identChar ~ rep( identChar | digit )              ^^ { case first ~ rest => processIdent(first :: rest mkString "") }
    | delim
    | '(' ~ delim ~ ')'                                 ^^ { case '(' ~ d ~ ')' => Identifier(d.chars) }
    | '(' ~ '0' ~ '-' ~ ')'                             ^^ { _ => Identifier("0-") }
    | '\"' ~ rep( chrExcept('\"', '\n', EofCh) ) ~ '\"' ^^ { case '\"' ~ chars ~ '\"' => StringLit(chars mkString "") }
    | '\"' ~> failure("unclosed string literal")
   // | floatLit                                          ^^ { case f => FloatingPointLit(f) }
    | floatLit                                          ^^ { case f => NumericLit(f) }
    | signedIntegerLit                                  ^^ { case i => NumericLit(i) }
    )

  // legal identifier chars other than digits
  override def identChar = letter | elem('_') | elem('\'') | elem('#') | elem('$')

  def nonZeroDigit = elem('1') | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
  def integerLit =
      ( elem('0')                 ^^ { _.toString() }
      | rep1(nonZeroDigit, digit) ^^ { _ mkString "" }
      )

  def signedIntegerLit =
      ( elem('-') ~ integerLit ^^ { case a ~ b => a+b }
      | integerLit )
  def plusMinusIntegerLit =
      ( elem('+') ~ integerLit ^^ { case a ~ b => a+b }
      | signedIntegerLit )

  def floatLit =
    ( signedIntegerLit ~ '.' ~ rep1(digit) ~ (elem('e') |  elem('E')) ~ plusMinusIntegerLit ^^ { case a ~ b ~ c ~ d ~ e => a+b+(c.foldLeft("")(_ + _))+d+e }
    | signedIntegerLit                    ~ (elem('e') |  elem('E')) ~ plusMinusIntegerLit ^^ { case a ~ b ~ c => a+b+c }
    | signedIntegerLit ~ '.' ~ rep1(digit)                                                  ^^ { case a ~ b ~ c  => a+b+(c.foldLeft("")(_ + _)) }
    | '.' ~ rep1(digit)																		^^ { case a ~ b => "0"+a+(b.foldLeft("")(_ + _)) }
    )


  /** The set of reserved identifiers: these will be returned as `Keyword's */
  override val reserved = new HashSet[String] ++ List( /* "\\",
                              "sorts", "functions", "problem", "forall", "exists",  */
                              "true", "false", "invariant",
                              "solution", "strengthen", "if", "then", "fi",
                              "generic", "extends", "oneof", "object",
                             "schemaVariables", "modalOperator", "operator",
                             "program", "formula", "term", "variables", "skolemTerm",
                             "location", "function", "modifies", "varcond", "typeof",
                             "elemTypeof", "new", "newLabel", "not", "same","compatible",
                             "sub", "strict", "staticMethodReference", "notFreeIn", "freeLabelIn",
                             "static", "enumConstant", "notSameLiteral", "isReferenceArray",
                             "isArray", "isReference", "isNonImplicit", "isEnumType", "dependingOn",
                             "dependingOnMod", "isQuery", "isNonImplicitQuery", "hasSort", "isLocalVariable",
                             "notIsLocalVariable", "isFirstOrderFormula", "isUpdated", "sameHeapDepPred",
                             "bind", "forall", "exists", "subst", "ifEx", "for", "if", "then", "else",
                             "sum", "bsum", "product", "include", "includeLDTs", "classpath", "bootclasspath",
                             "noDefaultClasses", "javaSource", "noJavaModel", "withOptions", "optionDecl",
                             "settings", "true", "false", "sameUpdateLevel", "inSequentState", "closegoal",
                             "heuristicsDecl", "noninteractive", "displayname", "oldname", "helptext",
                             "replacewith", "addrules", "addprogvars", "heuristics", "find", "add", "assume",
                             "predicates", "functions", "nonRigid", "inter", "rules", "problem", "chooseContract",
                             "proof", "contracts", "invariants",
                             // The first two guys are not really meta operators, treated separately
                             "inType", "isInReachableState", "isAbstractOrInterface", "containerType",
                             "forall", "exists", "true", "false",
                             "solution", "invariant", "strengthen", "sorts",
                             "if", "then", "else", "fi", "while", "do", "end",
                             "repeat", "until", "skip", "abort"
    )

  /** The set of delimiters (ordering does not matter) */
  override val delimiters = new HashSet[String] ++ List(
                             "\\sorts", "\\functions", "\\programVariables",
                             "\\problem", "\\forall", "\\exists",
                             "{", "}", "\\[", "\\]",  "\\<", "\\>", "&",
                               ",", ";", ":", "(", ")", "[", "]",
                              "=", "<", ">", ">=", "<=",
                             "!", "!=", "+", "-", "*", "/", "^",
                             "++", ":=", "@", "?",
                             //"&",
                             "|", "<->", "->", "==>", "."
                             /* "~",
                             ";", "/", ":", "::", ":=", ".", "..",
                             ",", "(", ")", "[", "]", "{", "}", "[]",
                             "@", "||", "|", "&", "!", "->", "=", "!=",
                             "==>", "^^", "~", "%", "*", "+", "^",
                             ">", ">=", "<", "<=", "\t", "\r", "\n",
                             "''",   "<->" */
    )

}

class DLOriginalParser(ins : String) extends StdTokenParsers {
  type Tokens = StdLexical 
  override val lexical = new KeYLexical
   
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
     //numericLit ^^ (x => Num(Exact.Integer(Integer.parseInt(x)))) |
     numericLit ^^ (x => {
       if(x.toString.contains(".")) {
         val of: Int = x.toString.indexOf('.')
         val decPlaces = BigDecimal(x.toString.length - of - 1);
         val y = BigDecimal(x)*decPlaces;
         Num(Exact.Rational(y.toBigInt, BigInt(10)^decPlaces.toBigInt))
       } else {
         Num(Exact.Integer(Integer.parseInt(x)))
       }
     }) |
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
     	("else" ~> hp <~ "fi") ^^ 
     	{case f~thenhp~elsehp => (Check(f) seq thenhp) ++ (Check(!f) seq elsehp)} |
     ("if" ~> "(" ~> formula0 <~ ")" <~ "then")~hp <~ "fi" ^^ 
     	{case f~thenhp => (Check(f) seq thenhp) ++ Check(!f)} |	
     ("{" ~> rep1sep(diffeq, ","))~(opt("," ~> rep1sep(formula0, ",")) <~ "}")~opt(annotation("invariant")) ^^
        {case dvs~f~invs => Evolve(if (!f.isEmpty) f.get.tail.foldLeft(f.get.head)(_ & _) else True, dvs:_*)}

  def diffeq : Parser[(Var, Term)] =
    (ident <~ "=")~term ^?
      {case s~tm  if s.endsWith("\'") 
        	  => (Var(s.substring(0,s.indexOf("\'"))), tm)}

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
   
  def sorts : Parser[List[Sort]] =
    "\\sorts" ~ "{" ~> repsep(sort, ";") <~ "}"  |
    "\\sorts" ~ "{" ~> repsep(sort, ";") <~ ";" <~ "}" |
    success(Nil)
    
  def functionsort : Parser[(String,(List[Sort],Sort))] =
    (sort ~ ident <~ "(") ~ (rep1sep(sort,",")) <~ "," <~ ")" ^^
      {case rtn ~ f ~ args => (f,(args,rtn))}  |
    (sort ~ ident <~ "(") ~ (rep1sep(sort,",")) <~ ")" ^^
      {case rtn ~ f ~ args => (f,(args,rtn))}  |
    (sort ~ ident) ^^
      {case c ~ f => (f, (Nil, c))}


 /*  def variablesort: Parser[(Sort, List[String])] =
    (sort ~ repsep(ident, ",")) ^^
      {case s ~ vargs => (s, vargs)}    */

    def variablesort: Parser[List[(String,(List[Sort],Sort))]] =
    (sort ~ rep1sep(ident, ",")) ^^
      {case s ~ vargs => {
      var vsrtsl : List[(String,(List[Sort],Sort))] = Nil
      for (v <- vargs) {
     vsrtsl =  (v, (Nil, s)) :: vsrtsl
      }
      vsrtsl
      }
      }

    def functionsorts: Parser[Map[String,(List[Sort],Sort)]] =
    "\\functions" ~> "{" ~> repsep(functionsort, ";") <~ ";" <~ "}" ^^
        {case fnsrts => scala.collection.immutable.HashMap.empty ++ fnsrts} |
    "\\functions" ~> "{" ~> repsep(functionsort, ";") <~ "}" ^^
        {case fnsrts => scala.collection.immutable.HashMap.empty ++ fnsrts} |
    success(scala.collection.immutable.HashMap.empty) 
        
    def programvariablesorts: Parser[Map[String,(List[Sort],Sort)]] =
    "\\programVariables" ~> "{" ~> repsep(variablesort, ";") <~ ";" <~ "}" ^^
       {case varsrts  => {
           var varmap : Map[String,(List[Sort],Sort)] =  scala.collection.immutable.HashMap.empty
           for (vsrtl <- varsrts) {
           varmap = varmap ++ vsrtl
            }
            varmap
            }
       } |
    "\\programVariables" ~> "{" ~> repsep(variablesort, ";") <~ "}" ^^
       {case varsrts  => {
           var varmap : Map[String,(List[Sort],Sort)] =  scala.collection.immutable.HashMap.empty
           for (vsrtl <- varsrts) {
           varmap = varmap ++ vsrtl
            }
            varmap
            }
              } |
    success(scala.collection.immutable.HashMap.empty)

       def variablesorts: Parser[Map[String,(List[Sort],Sort)]] =
       repsep(variablesort, ";") ^^
       {case varsrts  => {
           var varmap : Map[String,(List[Sort],Sort)] =  scala.collection.immutable.HashMap.empty
           for (vsrtl <- varsrts) {
           varmap = varmap ++ vsrtl
            }
            varmap
            }
              } |
            success(scala.collection.immutable.HashMap.empty)

   def sequent : Parser[Sequent] =
    sorts ~> functionsorts ~ repsep(formula, ",") ~ ("==>" ~> repsep(formula,",")) ^^
        {case fnsrts ~ c ~ s => Sequent(fnsrts, c,s)}  |
    sorts ~> functionsorts ~ ("\\problem" ~> "{" ~> formula <~ "}") ^^
       {case fnsrts ~ s => Sequent(fnsrts, Nil, List(s))}   |
    sorts ~> functionsorts ~ programvariablesorts ~ ("\\problem" ~> "{" ~> formula <~ "}") ^^
       {case fnsrts ~ varsrts ~ s => Sequent(fnsrts ++ varsrts, Nil, List(s))}   |
    sorts ~> functionsorts ~ ("\\problem" ~> "{" ~> "\\[" ~> variablesorts <~ "\\]") ~ (formula <~ "}") ^^
        {case fnsrts ~ varsrts ~ s => Sequent(fnsrts ++ varsrts, Nil, List(s))}  |
    sorts ~> functionsorts ~ ("\\problem" ~> "{" ~> "\\[" ~> variablesorts <~ ";" <~ "\\]") ~ (formula <~ "}") ^^
        {case fnsrts ~ varsrts ~ s => Sequent(fnsrts ++ varsrts, Nil, List(s))}  |
   // sorts ~> functionsorts ~ ("\\problem" ~> "{" ~> "\\[" ~> variablesorts <~ ";") ~ (repsep(hp | formula00, ";") <~ "\\]") ~ (formula00 <~ "}") ^^
    sorts ~> functionsorts ~ ("\\problem" ~> "{" ~> "\\[" ~> variablesorts <~ ";") ~ (hp <~ "\\]") ~ (formula <~ "}") ^^
       {case fnsrts ~ varsrts ~ a ~ s => Sequent(fnsrts ++ varsrts, Nil, List(Modality(Box,a,s)))} |
   //     {case fnsrts ~ varsrts ~ hp ~ s => Sequent(fnsrts ++ varsrts, Nil, List(s))} |
   // sorts ~> functionsorts ~ ("\\problem" ~> "{" ~> "\\[" ~> variablesorts <~ ";") ~ (repsep(hp | formula00, ";") <~ ";" <~ "\\]") ~ (formula00 <~ "}") ^^
   //     {case fnsrts ~ varsrts ~ hp ~ s => Sequent(fnsrts ++ varsrts, Nil, List(s))}
    sorts ~> functionsorts ~ ("\\problem" ~> "{" ~> "\\[" ~> variablesorts <~ ";") ~ (hp <~ ";" <~ "\\]") ~ (formula <~ "}") ^^
       {case fnsrts ~ varsrts ~ a ~ s => Sequent(fnsrts ++ varsrts, Nil, List(Modality(Box, a, s)))}

   def result : Option[Sequent] = {
     val ls = new lexical.Scanner(ins);
     phrase(sequent)(ls) match {
       case Success(r,next) if next.atEnd =>
         println("success! ")
         println(r)
         Some(r)
       case Success(r,next)  =>
         println("failure! Leftover input. only parsed: " )
         println(r)
         None
       case f =>
         println(f)
         None
     }

   }

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
  
  def parseKeYFile(f: String) : Sequent = {
    val dlp = new DLOriginalParser(f)
    dlp.result match {
      case Some(fm) => fm
      case None =>
        println("could not read a formula from " + f)
        null
    }
  }
  
  def parseKeYFile(i: InputStream) : Sequent = {
    val dlp = new DLOriginalParser(i)
    dlp.result match {
      case Some(fm) => fm
      case None =>
        println("could not read a formula from " + i)
        null
    }
  }

  def parseFormula(f: String) : Formula = {
    val dlp = new DLOriginalParser(f)
    dlp.fm_result match {
      case Some(fm) => fm
      case None =>
        println("could not read a formula from " + f)
        False
    }
  }
  
  def parseFormula(i: InputStream) : Formula = {
    val dlp = new DLOriginalParser(i)
    dlp.fm_result match {
      case Some(fm) => fm
      case None =>
        println("could not read a formula from " + i)
        False
    }
  }

  def main(args: Array[String]) {
    parseFormula("1.02 < 2");
  }
}


