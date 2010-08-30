package de.uka.ilkd.key.dl.arithmetics.impl.ch.cohenhormander;
//package cohenhormander;

abstract class CHTerm
case class Var(s: String) extends CHTerm
case class Fn(f: String, ps: List[CHTerm]) extends CHTerm
case class Num(n: ExactNum) extends CHTerm

abstract class Fol
case class R(s: String, ps: List[CHTerm]) extends Fol

abstract class CHFormula
case class False() extends CHFormula
case class True() extends CHFormula
case class Atom(a: Fol) extends CHFormula
case class Not(f: CHFormula) extends CHFormula
case class And(f1: CHFormula, f2: CHFormula) extends CHFormula
case class Or(f1: CHFormula, f2: CHFormula) extends CHFormula
case class Imp(f1: CHFormula, f2: CHFormula) extends CHFormula
case class Iff(f1: CHFormula, f2: CHFormula) extends CHFormula
case class Forall(x: String, f: CHFormula) extends CHFormula
case class Exists(x: String, f: CHFormula) extends CHFormula


class Failure() extends Exception

class ParseFailure(s: String) extends Exception

object P {
  type Token = String;
  type Tokens = List[String];

  def explode(s: String): List[Char] = 
    s.toCharArray().toList

  def matches(s: String): Char => Boolean = {
    (c: Char) => (s contains c)
  }

  def space = matches(" \t\n\r")
  def punctuation = matches("()[]{},")
  def symbolic = matches("~`!@#$%^&*-+=|\\:;<>.?/")
  def numeric = matches("0123456789")
  def alphanumeric = matches(
    "abcdefghijklmnopqrstuvwxyz_'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")

  def lexwhile(prop: Char => Boolean, inp: List[Char]): 
       (String, List[Char]) = inp match {
	 case (c::cs) if prop(c) => 
	   val (tok,rest) = lexwhile(prop, cs);
	   (c + tok, rest)
	 case _ => ("", inp)
       }


  def lex(inp: List[Char]): List[String] = 
    (lexwhile(space, inp))._2  match {
      case Nil => Nil
      case (c::cs) => 
	val prop = if( alphanumeric(c))  alphanumeric
		   else if(symbolic(c))  symbolic
		   else ((c:Char) => false);
        val (toktl,rest) = lexwhile(prop, cs);
        (c + toktl):: (lex(rest))
    }


  def make_parser[A](pfn: List[String] => (A, List[String]), s: String):
    A = { 
      val (expr,rest) = pfn(lex(explode(s)));
      if(rest == Nil)
	expr
      else
	throw new Error("Unparsed input")
    }



/*
  def parse_ginfix[A,B](opsym: Token, 
                        opupdate: ((A => B),A) => 
                                   (A => B),
                   sof: A => B,
                   subparser: Tokens => (A,Tokens),
                   inp: Tokens): (B,Tokens) = {
      val (e1,inp1) = subparser(inp);
      if((inp1 != Nil) && inp1.head == opsym)
      parse_ginfix(opsym,opupdate,opupdate(sof,e1),subparser,inp1.tail)
      else (sof(e1),inp1)
  }
                     
  def parse_left_infix[A](opsym: Token, 
                       opcon: (A,A) => A,
                       subparser: Tokens => (A,Tokens)) :
                       Tokens =>  (A,Tokens) = {
   (inp:Tokens) => {
     parse_ginfix(opsym, 
                  (f:A => A,e1:A) => 
                   ((e2:A) => opcon(f(e1),e2)), 
                  (x:A) => x,
                  subparser, inp)
   }
  }

  def parse_right_infix[A](opsym: Token, 
                           opcon: (A,A) => A,
                           subparser: Tokens => (A,Tokens))
                           (inp:Tokens) : (A,Tokens) = 
    parse_ginfix(opsym, 
                 (f:A => A,e1:A) => 
                  ((e2:A) => f(opcon(e1,e2))), 
                 (x:A) => x,
                 subparser, inp)


  def parse_list[A](opsym: Token, 
                    subparser: Tokens => (A,Tokens)):
                    Tokens => (List[A],Tokens) = {
    (inp:Tokens) => {
      parse_ginfix(opsym, 
                   (f:A=>List[A],e1:A) => 
                     ((e2:A) => (f(e1) ++ List(e2))), 
                   (x:A) => List(x),
                   subparser, inp)
    }
  }

  
  def papply[A,B,C](f: A => B, pr: (A,C)): (B,C) = pr match {
    case (ast,rest) => (f(ast), rest)
  }

  def nextin(inp: Tokens, tok: String): Boolean = {
    (inp != Nil) && (inp.head == tok)
  }

  def parse_bracketed[A](subparser: Tokens => (A,Tokens),
                         cbra: String,
                         inp: Tokens): (A, Tokens) = {
    val (ast,rest) = subparser(inp);
    if (nextin(rest,cbra)) (ast,rest.tail)
    else throw new ParseFailure("Closing bracket expected.")
  }



  def parse_atomic_formula(fns: ((Tokens,Tokens)=>(CHFormula,Tokens), 
                                 (Tokens,Tokens)=>(CHFormula,Tokens)),
                           vs: Tokens):
                           Tokens => (CHFormula,Tokens) = {
    (inp:Tokens) => {
      val (ifn,afn) = fns;
      inp match {
        case Nil => throw new ParseFailure("formula expected")
        case "false"::rest => (False(), rest)
        case "true"::rest => (True(),rest)
        case "("::rest => 
              try { ifn(vs,inp) } catch { case p:ParseFailure =>
                parse_bracketed (inp => parse_formula(fns, vs, inp),
                                 ")", rest)
                                       }
        case "~"::rest => papply( (p:CHFormula) => Not(p), 
                                 parse_atomic_formula(fns,vs)(rest))
        case "forall"::x::rest =>
          parse_quant(fns, x::vs, 
                      ((y:Token,p:CHFormula) => Forall(y,p)), x, rest)
        case "exists"::x::rest =>
          parse_quant(fns, x::vs, 
                      ((y:Token,p:CHFormula) => Exists(y,p)), x, rest)
        case _ => afn(vs,inp)
      }
        
    }
  }
  
  def parse_quant(fns:  ((Tokens,Tokens)=>(CHFormula,Tokens), 
                         (Tokens,Tokens)=>(CHFormula,Tokens)),
                  vs: Tokens,
                  qcon: (Token,CHFormula) => CHFormula,
                  x: Token,
                  inp: Tokens): (CHFormula, Tokens) = inp match {
    case Nil => throw new ParseFailure("Body of quantified term expected.")
    case y :: rest =>
      papply((fm:CHFormula) => qcon(x,fm),
             if(y==".") parse_formula(fns,vs,rest)
             else parse_quant(fns,y::vs,qcon,y,rest))
  }

  def parse_formula(fns: ((Tokens,Tokens)=>(CHFormula,Tokens), 
                          (Tokens,Tokens)=>(CHFormula,Tokens)),
                    vs: Tokens,
                    inp: Tokens): (CHFormula,Tokens) = {
    parse_right_infix("<=>",  ((p:CHFormula,q:CHFormula) => Iff(p,q)),
      parse_right_infix("==>", ((p:CHFormula,q:CHFormula) => Imp(p,q)),
        parse_right_infix("\\/", ((p:CHFormula,q:CHFormula) => Or(p,q)),
          parse_right_infix("/\\", ((p:CHFormula,q:CHFormula) => And(p,q)),
            parse_atomic_formula(fns, vs)))))(inp)
  }


  def is_const_name(s: Token): Boolean = {
    (explode(s).forall(numeric)) //|| (s == "nil")
  }

  def parse_atomic_term(vs: Tokens): Tokens => (CHTerm, Tokens) = {
    (inp:Tokens) => inp match {
      case Nil => throw new ParseFailure("term expected")
      case "("::rest => parse_bracketed(parse_term(vs), ")", rest)
      case "-"::rest => papply ((t:CHTerm) => Fn("-",List(t)),
                                parse_atomic_term(vs)(rest))
      case f::"("::")"::rest => (Fn(f,Nil),rest)
      case f::"("::rest =>
        papply ((args:List[CHTerm]) => Fn(f,args),
                parse_bracketed(parse_list(",",parse_term(vs)),
                                ")",rest))
      case a::rest => 
        (if (is_const_name(a) && (! vs.contains(a))) Num(new ExactInt(a)) 
         else Var(a),
         rest)
    }
  }

  def parse_term(vs: Tokens): Tokens => (CHTerm,Tokens) = {
    parse_right_infix("::",((e1:CHTerm,e2:CHTerm) => Fn("::",List(e1,e2))),
     parse_right_infix("+",((e1:CHTerm,e2:CHTerm) => Fn("+",List(e1,e2))),
      parse_left_infix("-",((e1:CHTerm,e2:CHTerm) => Fn("-",List(e1,e2))),
       parse_right_infix("*",((e1:CHTerm,e2:CHTerm) => Fn("*",List(e1,e2))),
        parse_left_infix("/",((e1:CHTerm,e2:CHTerm) => Fn("/",List(e1,e2))),
         parse_left_infix("^",((e1:CHTerm,e2:CHTerm) => Fn("^",List(e1,e2))),
          parse_atomic_term(vs)))))))
  }

  val parset = (inp: String) => make_parser(parse_term(Nil), inp);

 */ 
  val comparators = List("=","<","<=",">",">=");



/*
  def parse_infix_atom(vs: Tokens, inp: Tokens): (CHFormula, Tokens) = {
    val (tm,rest) = parse_term(vs)(inp);
    if(comparators.exists( x => nextin(rest,x)))
      papply((tm1:CHTerm) => Atom(R(rest.head,List(tm,tm1))),
             parse_term(vs) (rest.tail))
    else throw new ParseFailure("")
  }

  def parse_atom(vs: Tokens, inp: Tokens): (CHFormula, Tokens) = {
    try {
      parse_infix_atom(vs,inp)
    } catch {
      case p:ParseFailure =>
        inp match {
          case p::"("::")"::rest => (Atom(R(p,Nil)),rest)
          case p::"("::rest =>
            papply ((args:List[CHTerm]) => Atom(R(p,args)),
                    parse_bracketed(parse_list(",",parse_term(vs)),")",rest))
          case p::rest if p != "(" => (Atom(R(p,Nil)),rest)
          case _ => throw new ParseFailure("parse_atom")
        }
    }
  }


  val parse = (inp: String) => 
    make_parser((inp1:Tokens) => 
      parse_formula((parse_infix_atom,parse_atom),Nil,inp1), inp);


*/
  def bracket[A,B]: Boolean => Int => 
    (A => B => Unit) => A => B => Unit = 
      p => n => f => x => y => {
        if( p ) print("(") else ();
        f(x)(y);
        if( p ) print(")") else ();
      }



  def strip_quant: CHFormula => (Tokens, CHFormula) = fm => fm match {
    case Forall(x,yp@Forall(y,p))=> 
      val (xs,q) = strip_quant(yp);
      (x::xs,q)
    case Exists(x,yp@Exists(y,p))=> 
      val (xs,q) = strip_quant(yp);
      (x::xs,q)
    case Forall(x,p) => (List(x),p)
    case Exists(x,p) => (List(x),p)
    case _ => (Nil, fm)
  }
  

  def print_formula: (Int => Fol => Unit) => CHFormula => Unit = pfn => {
    def print_formula1: Int => CHFormula => Unit = pr => fm => fm match {
      case False() => print("false")
      case True() => print("true")
      case Atom(pargs) => pfn(pr)(pargs)
      case Not(p) => bracket (pr > 10)(1)(print_prefix(10))("~")(p)
      case And(p,q) => bracket (pr > 8)(0)(print_infix(8)("/\\"))(p)(q)
      case Or(p,q) => bracket (pr > 6)(0)(print_infix(6)("\\/"))(p)(q)
      case Imp(p,q) => bracket (pr > 4)(0)(print_infix(4)("==>"))(p)(q)
      case Iff(p,q) => bracket (pr > 2)(0)(print_infix(2)("<=>"))(p)(q)
      case Forall(x,p) => 
        bracket(pr>0)(2)(print_qnt)("forall")(strip_quant(fm))
      case Exists(x,p) => 
        bracket(pr>0)(2)(print_qnt)("exists")(strip_quant(fm))
    }
    def print_qnt: Token => ((Tokens,CHFormula)) => Unit = qname => b => {
      val (bvs, bod) = b;
      print(qname);
      bvs.foreach(v => print(" " + v));
      print(". ");
      print_formula1(0)(bod)
    }
    def print_prefix: Int => Token => CHFormula => Unit = newpr => sym => p => {
      print(sym); 
      print("(");
      print_formula1(newpr+1)(p);
      print(")");
    }
    def print_infix: Int => Token => CHFormula => CHFormula => Unit =
      newpr => sym => p => q => {
        print_formula1(newpr+1)(p);
        print(" "+sym+" ");
        print_formula1(newpr)(q)
      }
    print_formula1(0)
  }

  def print_qformula: (Int => Fol => Unit) => CHFormula => Unit = pfn => fm => {
    print("<<");
    print_formula(pfn)(fm);
    print(">>");
  }

  def print_term: Int => CHTerm => Unit = prec => fm => fm match {
    case Var(x) => print(x)
    case Num(n) => print(n.toString)
    case Fn("^",List(tm1,tm2)) => 
      print_infix_term(true)(prec)(24)("^")(tm1)(tm2)
    case Fn("/",List(tm1,tm2)) => 
      print_infix_term(true)(prec)(22)(" /")(tm1)(tm2)
    case Fn("*",List(tm1,tm2)) => 
      print_infix_term(false)(prec)(20)(" *")(tm1)(tm2)
    case Fn("-",List(tm1,tm2)) => 
      print_infix_term(true)(prec)(18)(" -")(tm1)(tm2)
    case Fn("+",List(tm1,tm2)) => 
      print_infix_term(false)(prec)(16)(" +")(tm1)(tm2)
    case Fn("::",List(tm1,tm2)) => 
      print_infix_term(false)(prec)(14)(" +")(tm1)(tm2)
    case Fn(f,args) => print_fargs(f)(args)
  }
  def print_fargs: Token => List[CHTerm] => Unit = f => args => {
    print(f);
    if(args == Nil) () else {
      print("(");
      print_term(0)(args.head);
      (args.tail).foreach(t => {print(","); print_term(0)(t)});
      print(")");
    }
  }
  def print_infix_term: Boolean => Int => Int => Token => CHTerm => CHTerm => Unit =
    isleft => oldprec => newprec => sym => p => q => {
      if(oldprec>newprec) print("(") else ();
      print_term(if(isleft) newprec else (newprec + 1))(p);
      print(sym);
      if(sym.charAt(0) == ' ') print(" ") else ();
      print_term(if(isleft) (newprec+1) else newprec)(q);
      if (oldprec > newprec) print(")") else ();
    }

  def printert(tm: CHTerm): Unit = {
    print_term(0)(tm)
  }

  def print_atom:  Int => Fol => Unit = prec => fm => fm match {
    case R(p,args) => 
      if(comparators.contains(p) && (args.length == 2))
        print_infix_term(false)(12)(12)(" " + p)(args.apply(0))(args.apply(1))
      else print_fargs(p)(args)
    case _ => throw new Error("print_atom: nonatomic input")
  }

  val print_fol_formula: CHFormula => Unit = print_qformula(print_atom);


  

}

