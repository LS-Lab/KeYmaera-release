package de.uka.ilkd.key.dl.arithmetics.impl.metitarski

object OperatorMap {

  val (utf8, metit, qepcad, smt) = (0,1,2,3)
  
  val mapOps = ( logicalConstant	++ 
		  		 logicalConnective	++ 
		  		 quantifier			++
		  		 relationalSymbol	++ 
		  		 arithmeticOperator	++
		  		 specialFunction	
		  		 )
    
      /* Term	->       UTF-8      , MetiTarski,    QEPCAD,    SMTLIB */
    
  val logicalConstant: Map[String, List[String]] = Map(      
      /* Constants */
      "true" 	-> 	List( "⊤"		, "$true"	,	"[0 = 0]"	),
      "false" 	-> 	List( "⊥"		, "$false"	,	"[0 = 1]"	)
      )
      
  val logicalConnective: Map[String, List[String]] = Map(         
      /* Boolean operators */
      "not" 	-> 	List( " ¬ "		, "~"		,	"~"		),
      "and"		->	List( " ∧ "		, "&"		,	"/\\"	),
      "or" 		-> 	List( " ∨ "		, "|"		,	"\\/"	),
      "imp"		->	List( " → "		, "=>"		,	"==>"	),
      "equiv"	->	List( " ↔ "		, "equiv"	,	"<==>"	)
      )
      
 val quantifier: Map[String, List[String]] = Map(    
      /* Quantifiers */
      "all"		->	List( "∀ "		, "!"		,	"A"		),
      "exist"	->	List( "∃ "		, "?"		,	"E"		)
      )
      
 val relationalSymbol: Map[String, List[String]] = Map(    
      /* Relational symbols */
      "equals"	->	List( "="		, "="		,	"="		),
      "geq"		->	List( "≥"		, ">="		,	">="	),
      "neq"		->	List( "≠"		, "!="		,	"/="	),
      "leq"		->	List( "≤"		, "<="		,	"<="	),
      "lt"		->	List( "<"		, "<"		,	"<"		),
      "gt"		->	List( ">"		, ">"		,	">"		)
      )

  val arithmeticOperator: Map[String, List[String]] = Map(    
      /* Arithmetic operators */
      "neg"		->	List( "-"		, "-"		, 	"-"		),
      "exp"		->	List( "^"		, "^"		,	"^"		),
      "mul"		->	List( "·"		, "*"		,	" "		),
      "div"		->	List( "÷"		, "/"		,	" / "	),
      "add"		->	List( "+"		, "+"		,	" + "	),
      "sub"		->	List( "-"		, "-"		,	" - "	)
      )
         
  val specialFunction: Map[String, List[String]] = Map(    
      /* Special functions */
      "Log"		->	List( "Log"		, "ln"		,	"ln"	),
      "Exp"		->	List( "Exp"		, "exp"		,	"exp"	),
      
      "Sin"		->	List( "Sin"		, "sin"		, 	"sin"	),
      "Cos"		->	List( "Cos"		, "cos"		, 	"cos"	),
      "Tan"		->	List( "Tan"		, "tan"		, 	"tan"	),
      
      "ArcSin"	->	List( "ArcSin"	, "asin"	, 	"asin"	),
      "ArcCos"	->	List( "ArcCos"	, "acos"	, 	"acos"	),
      "ArcTan"	->	List( "ArcTan"	, "atan"	, 	"atan"	),
      
      "Sinh"	->	List( "Sinh"	, "sinh"	, 	"sinh"	),
      "Cosh"	->	List( "Cosh"	, "cosh"	, 	"cosh"	),
      "Tanh"	->	List( "Tanh"	, "tanh"	, 	"tanh"	),
      
      "Sqrt" 	-> 	List( "√"		, "sqrt"	, 	"sqrt"	),
      "CubeRoot"-> 	List( "∛"		, "cbrt"	, 	"cbrt"	),
      "Abs" 	-> 	List( "Abs"		, "abs"	    , 	"abs"	)
  )

  def isLogicalConst(x: String) = 
    OperatorMap.logicalConstant.get(x) match {
    case None => false
    case Some(s) => true
  }

  def isLogicalConnective(x: String) = 
    OperatorMap.logicalConnective.get(x) match {
    case None => false
    case Some(s) => true
  }

  def isQuantifier(x: String) = 
    OperatorMap.quantifier.get(x) match {
    case None => false
    case Some(s) => true
  }

  def isRelationalSymbol(x: String) = 
    OperatorMap.relationalSymbol.get(x) match {
    case None => false
    case Some(s) => true
  }

  def isArithmeticOperator(x: String) = 
    OperatorMap.arithmeticOperator.get(x) match {
    case None => false
    case Some(s) => true
  }

  def isSpecialFunction(x: String) = 
    OperatorMap.specialFunction.get(x) match {
    case None => false
    case Some(s) => true
  }    
  
}