package de.uka.ilkd.key.dl.arithmetics.impl.ch.cohenhormander;
//package cohenhormander;

abstract class Term
case class Var(s: String) extends Term
case class Fn(f: String, ps: List[Term]) extends Term
case class Num(n: ExactNum) extends Term

abstract class Fol
case class R(s: String, ps: List[Term]) extends Fol

abstract class Formula
case class False() extends Formula
case class True() extends Formula
case class Atom(a: Fol) extends Formula
case class Not(f: Formula) extends Formula
case class And(f1: Formula, f2: Formula) extends Formula
case class Or(f1: Formula, f2: Formula) extends Formula
case class Imp(f1: Formula, f2: Formula) extends Formula
case class Iff(f1: Formula, f2: Formula) extends Formula
case class Forall(x: String, f: Formula) extends Formula
case class Exists(x: String, f: Formula) extends Formula


class Failure() extends Exception

class ParseFailure(s: String) extends Exception

object P {
  type Token = String;
  type Tokens = List[String];

  def explode(s: String): List[Char] = 
    List.fromArray(s.toCharArray())

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



  def parse_atomic_formula(fns: ((Tokens,Tokens)=>(Formula,Tokens), 
                                 (Tokens,Tokens)=>(Formula,Tokens)),
                           vs: Tokens):
                           Tokens => (Formula,Tokens) = {
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
        case "~"::rest => papply( (p:Formula) => Not(p), 
                                 parse_atomic_formula(fns,vs)(rest))
        case "forall"::x::rest =>
          parse_quant(fns, x::vs, 
                      ((y:Token,p:Formula) => Forall(y,p)), x, rest)
        case "exists"::x::rest =>
          parse_quant(fns, x::vs, 
                      ((y:Token,p:Formula) => Exists(y,p)), x, rest)
        case _ => afn(vs,inp)
      }
        
    }
  }
  
  def parse_quant(fns:  ((Tokens,Tokens)=>(Formula,Tokens), 
                         (Tokens,Tokens)=>(Formula,Tokens)),
                  vs: Tokens,
                  qcon: (Token,Formula) => Formula,
                  x: Token,
                  inp: Tokens): (Formula, Tokens) = inp match {
    case Nil => throw new ParseFailure("Body of quantified term expected.")
    case y :: rest =>
      papply((fm:Formula) => qcon(x,fm),
             if(y==".") parse_formula(fns,vs,rest)
             else parse_quant(fns,y::vs,qcon,y,rest))
  }

  def parse_formula(fns: ((Tokens,Tokens)=>(Formula,Tokens), 
                          (Tokens,Tokens)=>(Formula,Tokens)),
                    vs: Tokens,
                    inp: Tokens): (Formula,Tokens) = {
    parse_right_infix("<=>",  ((p:Formula,q:Formula) => Iff(p,q)),
      parse_right_infix("==>", ((p:Formula,q:Formula) => Imp(p,q)),
        parse_right_infix("\\/", ((p:Formula,q:Formula) => Or(p,q)),
          parse_right_infix("/\\", ((p:Formula,q:Formula) => And(p,q)),
            parse_atomic_formula(fns, vs)))))(inp)
  }


  def is_const_name(s: Token): Boolean = {
    (explode(s).forall(numeric)) //|| (s == "nil")
  }

  def parse_atomic_term(vs: Tokens): Tokens => (Term, Tokens) = {
    (inp:Tokens) => inp match {
      case Nil => throw new ParseFailure("term expected")
      case "("::rest => parse_bracketed(parse_term(vs), ")", rest)
      case "-"::rest => papply ((t:Term) => Fn("-",List(t)),
                                parse_atomic_term(vs)(rest))
      case f::"("::")"::rest => (Fn(f,Nil),rest)
      case f::"("::rest =>
        papply ((args:List[Term]) => Fn(f,args),
                parse_bracketed(parse_list(",",parse_term(vs)),
                                ")",rest))
      case a::rest => 
        (if (is_const_name(a) && (! vs.contains(a))) Num(new ExactInt(a)) 
         else Var(a),
         rest)
    }
  }

  def parse_term(vs: Tokens): Tokens => (Term,Tokens) = {
    parse_right_infix("::",((e1:Term,e2:Term) => Fn("::",List(e1,e2))),
     parse_right_infix("+",((e1:Term,e2:Term) => Fn("+",List(e1,e2))),
      parse_left_infix("-",((e1:Term,e2:Term) => Fn("-",List(e1,e2))),
       parse_right_infix("*",((e1:Term,e2:Term) => Fn("*",List(e1,e2))),
        parse_left_infix("/",((e1:Term,e2:Term) => Fn("/",List(e1,e2))),
         parse_left_infix("^",((e1:Term,e2:Term) => Fn("^",List(e1,e2))),
          parse_atomic_term(vs)))))))
  }

  val parset = (inp: String) => make_parser(parse_term(Nil), inp);

  val comparators = List("=","<","<=",">",">=");

  def parse_infix_atom(vs: Tokens, inp: Tokens): (Formula, Tokens) = {
    val (tm,rest) = parse_term(vs)(inp);
    if(comparators.exists( x => nextin(rest,x)))
      papply((tm1:Term) => Atom(R(rest.head,List(tm,tm1))),
             parse_term(vs) (rest.tail))
    else throw new ParseFailure("")
  }

  def parse_atom(vs: Tokens, inp: Tokens): (Formula, Tokens) = {
    try {
      parse_infix_atom(vs,inp)
    } catch {
      case p:ParseFailure =>
        inp match {
          case p::"("::")"::rest => (Atom(R(p,Nil)),rest)
          case p::"("::rest =>
            papply ((args:List[Term]) => Atom(R(p,args)),
                    parse_bracketed(parse_list(",",parse_term(vs)),")",rest))
          case p::rest if p != "(" => (Atom(R(p,Nil)),rest)
          case _ => throw new ParseFailure("parse_atom")
        }
    }
  }


  val parse = (inp: String) => 
    make_parser((inp1:Tokens) => 
      parse_formula((parse_infix_atom,parse_atom),Nil,inp1), inp);



  def bracket[A,B]: Boolean => Int => 
    (A => B => Unit) => A => B => Unit = 
      p => n => f => x => y => {
        if( p ) print("(") else ();
        f(x)(y);
        if( p ) print(")") else ();
      }

  def strip_quant: Formula => (Tokens, Formula) = fm => fm match {
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
  

  def print_formula: (Int => Fol => Unit) => Formula => Unit = pfn => {
    def print_formula1: Int => Formula => Unit = pr => fm => fm match {
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
    def print_qnt: Token => ((Tokens,Formula)) => Unit = qname => b => {
      val (bvs, bod) = b;
      print(qname);
      bvs.foreach(v => print(" " + v));
      print(". ");
      print_formula1(0)(bod)
    }
    def print_prefix: Int => Token => Formula => Unit = newpr => sym => p => {
      print(sym); 
      print("(");
      print_formula1(newpr+1)(p);
      print(")");
    }
    def print_infix: Int => Token => Formula => Formula => Unit =
      newpr => sym => p => q => {
        print_formula1(newpr+1)(p);
        print(" "+sym+" ");
        print_formula1(newpr)(q)
      }
    print_formula1(0)
  }

  def print_qformula: (Int => Fol => Unit) => Formula => Unit = pfn => fm => {
    print("<<");
    print_formula(pfn)(fm);
    print(">>");
  }

  def print_term: Int => Term => Unit = prec => fm => fm match {
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
  def print_fargs: Token => List[Term] => Unit = f => args => {
    print(f);
    if(args == Nil) () else {
      print("(");
      print_term(0)(args.head);
      (args.tail).foreach(t => {print(","); print_term(0)(t)});
      print(")");
    }
  }
  def print_infix_term: Boolean => Int => Int => Token => Term => Term => Unit =
    isleft => oldprec => newprec => sym => p => q => {
      if(oldprec>newprec) print("(") else ();
      print_term(if(isleft) newprec else (newprec + 1))(p);
      print(sym);
      if(sym.charAt(0) == ' ') print(" ") else ();
      print_term(if(isleft) (newprec+1) else newprec)(q);
      if (oldprec > newprec) print(")") else ();
    }

  def printert(tm: Term): Unit = {
    print_term(0)(tm)
  }

  def print_atom:  Int => Fol => Unit = prec => fm => fm match {
    case R(p,args) => 
      if(comparators.contains(p) && (args.length == 2))
        print_infix_term(false)(12)(12)(" " + p)(args.apply(0))(args.apply(1))
      else print_fargs(p)(args)
    case _ => throw new Error("print_atom: nonatomic input")
  }

  val print_fol_formula: Formula => Unit = print_qformula(print_atom);


  

}

