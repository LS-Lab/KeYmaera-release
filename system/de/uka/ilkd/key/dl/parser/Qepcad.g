grammar Qepcad;

// by Timo Michelsen

@header {
	package de.uka.ilkd.key.dl.parser;
	import de.uka.ilkd.key.logic.Term;
	import de.uka.ilkd.key.logic.TermBuilder;
	import de.uka.ilkd.key.logic.TermFactory;
	import de.uka.ilkd.key.dl.parser.NumberCache;
	import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
	import de.uka.ilkd.key.logic.NamespaceSet;
	import de.uka.ilkd.key.logic.Name;
	import de.uka.ilkd.key.logic.Named;
	import de.uka.ilkd.key.logic.op.LogicVariable;
	import de.uka.ilkd.key.logic.op.Metavariable;
	import de.uka.ilkd.key.logic.op.Function;
	import de.uka.ilkd.key.logic.op.Equality;
	import java.math.BigDecimal;
	import de.uka.ilkd.key.logic.op.ProgramVariable;
	import de.uka.ilkd.key.dl.model.Less;
	import de.uka.ilkd.key.dl.model.Equals;
	import de.uka.ilkd.key.dl.model.Greater;	
	import de.uka.ilkd.key.dl.model.LessEquals;
	import de.uka.ilkd.key.dl.model.GreaterEquals;
	import de.uka.ilkd.key.dl.model.Unequals;
}

@lexer::header {
	package de.uka.ilkd.key.dl.parser;
}

@lexer::members {
	// let the lexer throw an error instead of silently skipping over them with just a printed error.
	public Token nextToken() {
		while (true) {
			state.token = null;
			state.channel = Token.DEFAULT_CHANNEL;
			state.tokenStartCharIndex = input.index();
			state.tokenStartCharPositionInLine = input.getCharPositionInLine();
			state.tokenStartLine = input.getLine();
			state.text = null;
			if ( input.LA(1)==CharStream.EOF ) {
				return Token.EOF_TOKEN;
			}
			try {
				mTokens();
				if ( state.token==null ) {
					emit();
				}
				else if ( state.token==Token.SKIP_TOKEN ) {
					continue;
				}
				return state.token;
			}
			catch (RecognitionException re) {
				reportError(re);
				throw (IllegalStateException) new IllegalStateException("Lexical error in input file " + re).initCause(re);
			}
		}
	}
}

@members {
	TermBuilder tb = TermBuilder.DF;
	
	NamespaceSet nss = null;
	
	public void setNamespaceSet( NamespaceSet set ) {
		this.nss = set;
	}
	
	public NamespaceSet getNamespaceSet() {
		return this.nss;
	}	
	
	public Term getVariable( String var ) {
		Function f = (Function)this.nss.functions().lookup(new Name(var));
		if(f != null) {
			return tb.func(f);
		}
		Named named = this.nss.variables().lookup(new Name(var));
				if( named != null ) {
					if(named instanceof LogicVariable) {
       		 			LogicVariable n = (LogicVariable)named;
            			return tb.var(n);
            		} else if(named instanceof Metavariable) {
            			Metavariable n = (Metavariable)named;
            			return TermFactory.DEFAULT.createFunctionTerm(n);
            		} else {
            			throw new IllegalStateException("Found object of unknown type " + named.getClass()
            				+ " in the variable namespace, named " + named);
            		}
        		} else {
            		ProgramVariable p = (ProgramVariable)this.nss.programVariables().lookup(new Name(var));
            		if( p != null )
                			return tb.var(p);
           		else {
                			System.out.println("WARNING: Variable " + var + " not found! Creating own one...");
                			return tb.var(new LogicVariable( new Name(var), RealLDT.getRealSort()));
            		}
        		}     
    	}
}

// Parser Rules

formula returns [Term t ]		: 	(st = logic { t = st; }) EOF;

/* Logical expressions */
logic returns [ Term t ]		:	e = logic_biimpl { t = e; }; // Lesbarkeit
logic_biimpl returns [ Term t ]	:	e = logic_impl { t = e; } ( BIMPL f = logic_biimpl { t = tb.equiv(e, f); })?;
logic_impl returns [ Term t ]	:	e = logic_or { t = e; } 
							( (IMPL_R f = logic_impl {t = tb.imp(e,f); })|
							( IMPL_L f = logic_impl { t = tb.imp(f,e); }) )?;
logic_or returns [ Term t ]		: 	e = logic_and {t = e;} ( OR f = logic_or { t = tb.or(e,f); } )?;	
logic_and returns [Term t]		:	e = predicate{ t = e; } ( AND f = logic_and { t = tb.and(e,f); })?;

					
predicate returns [Term t]		:	( e = expression )  
							((func = pred_func f = expression { t = tb.func( func, e, f );} ) |
							(EQ f = expression { t = tb.equals( e, f ); }) )
						
						| ( LB e = logic RB ) { t = e; }
						| ( NOT e = predicate { t = tb.not(e); }) |
						( TRUE { t = tb.tt(); } ) |
						( FALSE { t = tb.ff(); } );	
											
pred_func returns [ Function f ]	:	GT { f = RealLDT.getFunctionFor(Greater.class);} |
						LT { f = RealLDT.getFunctionFor(Less.class);} |
						GE { f = RealLDT.getFunctionFor(GreaterEquals.class); }|
						NE { f = RealLDT.getFunctionFor(Unequals.class); }|
						LE { f = RealLDT.getFunctionFor(LessEquals.class); } ;
						
/* Arithmetic expressions */
expression returns [ Term t ]	:	e = expr_add { t = e; }; // Lesbarkeit
expr_add returns [Term t ]		:	e = expr_sub { t = e; } ( ADD f = expr_add { t = tb.func((Function)nss.functions().lookup( new Name("add")),e,f); } )?; 

expr_sub returns [ Term t ]		:	e = expr_mul{ t = e; } ( SUB f = expr_mul {  t = tb.func((Function)nss.functions().lookup( new Name("sub")),t,f);} )*;
expr_mul returns [ Term t ]		:	e = expr_pow{ t = e; } ( f = expr_mul {  t = tb.func((Function)nss.functions().lookup( new Name("mul")),e,f);} )?; 
expr_pow returns [ Term t ]	:	e = expr_atom { t = e; } ( POW f = number { t = tb.func((Function)nss.functions().lookup( new Name("exp")), e, f);})?;
expr_atom returns [Term t ]	:	( LP st = expression RP { t = st; }) |
						( st = varOrNum { t = st; } ); 

varOrNum returns [ Term t ]	: 	(st = variable { t = st;} ) | 
						( st = number { t = st; } );

							
variable returns [ Term t ] 		:	s = VAR { t=getVariable(s.getText()); };
number returns [ Term t]		:	n = NUM {t = tb.func(NumberCache.getNumber(BigDecimal.valueOf(Integer.valueOf(n.getText())),RealLDT.getRealSort())); };

// Lexer-Rules

TRUE :	'TRUE';
FALSE:	'FALSE';

AND	:	'/\\' ; // OK
OR	:	'\\/'; // OK
BIMPL:	'<==>'; // Untested	
IMPL_R:	'==>'; // Untested	
IMPL_L:	'<=='; // Untested	
NOT	:	 '~'; 

GE	:	'>='; //OK
GT	:	'>'; // OK
LT	:	'<'; // OK
LE	:	'<='; // OK
EQ	:	'='; // OK
NE	:	'/=' ; // OK

ADD	:	'+'; // OK
SUB	:	'-'; // OK

LP 	:	 '('; //OK
RP	:	')'; // OK
LB 	:	 '['; // OK
RB	:	']'; // OK

POW	:	'^'; // OK

WS 	:  	('\t' | ' ' | '\r' | '\n'| '\u000C' )+ { $channel = HIDDEN; };
NUM 	:   	'0'..'9'* ('0'..'9'| ('.' '0'..'9'+)) ;
VAR	:	(('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9'|'_')*);


