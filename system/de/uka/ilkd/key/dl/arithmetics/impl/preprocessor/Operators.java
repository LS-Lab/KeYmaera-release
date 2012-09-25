package de.uka.ilkd.key.dl.arithmetics.impl.preprocessor;

public enum Operators {
   	
	/**
	 * Enum type for translating KeYmaera operators to back-end solver syntax.
	 * <p>
	 * <b>N.B.</b> Here we set the Enum constant names to match those of KeYmaera 
	 * operators, this allows us to perform translation in constant time.  
	 * 
	 * @author s0805753@sms.ed.ac.uk
	 * @since 12/02/2012
	 */

	and	("and", " ∧ ", "/\\", "&"),
	or	("or", " ∨ ", "\\/", "|"),
	not 	("not", " ¬ ", "not" , "~"),
	imp	("imp", " → ", "imp", "=>"),
	equiv	("equiv", "↔", "equiv", "equiv"),
	all 	("all", "∀ ", "all", "!"),
	exist 	("exist", "∃ ", "exist", "?"),
	
	mul	("mul", "·", "*", "*"),
	neg	("neg", "-", "-", "-"),
	div	("div", "÷", "/", "/"),
	add	("add", "+", "+", "+"),
	sub	("sub", "-", "-", "-"),
	exp	("exp", "^", "^", "^"),
		
	equals	("equals", "=", "=", "="),	
	geq	("geq", "≥", ">=", ">="),
	neq	("neq", "≠", "!=", "!="),
	leq	("leq", "≤", "<=", "<="),
	lt	("lt", "<", "<", "<"),
	gt	("gt", ">", ">", ">"),
		
	Log	("Log", "ln"),
	Exp	("Exp", "exp"),

	Sin	("Sin", "sin"),
	Cos	("Cos", "cos"),
	Tan	("Tan", "tan"),
	
	Sinh	("Sinh", "sinh"),
	Cosh	("Cosh", "cosh"),
	Tanh	("Tanh", "tanh"),

	ArcSin	("ArcSin", "asin"),
	ArcCos	("ArcCos", "acos"),
	ArcTan	("ArcTan", "atan"),
	
	// Auxiliary
	VERUM 	("true",  "⊤", "VERUM",  "$true"),
	FALSUM 	("false", "⊥", "FALSUM", "$false"),
	
	// MetiTarski square & cube roots; ugly but necessary.
	SQRT	("Sqrt", "√", "sqrt", "sqrt"),
	CBRT	("Cbrt", "∛", "cbrt", "cbrt");

   public String KeY, utf, Rahd, Tptp;

   Operators(String KeY, String utf, String Rahd, String Tptp){
      this.KeY = KeY;
      this.utf=utf;
      this.Rahd=Rahd;
      this.Tptp=Tptp;  		
   }

   Operators(String KeY, String Tptp) {
      this.KeY = KeY;
      this.utf = KeY;
      this.Rahd = KeY;
      this.Tptp = Tptp;
   } 

   public Operators negatePredicate(){
      return Operators.negatePredicate(this);
   }

   public static Operators negatePredicate(Operators operator){
      switch(operator){
         case geq :	return lt;
         case leq :	return gt;
         case lt :	return geq;
         case gt :	return leq;
         case equals :	return neq;
         case neq :	return equals;

         default :	
                     //logger.severe("\""+operator.name() + "\" is not a binary predicate, cannot negate!");
                     return operator;
      }
   }    	    
}
