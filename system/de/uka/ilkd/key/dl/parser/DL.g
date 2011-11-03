grammar DL;

options {
	output=AST;
	ASTLabelType=CommonTree; // type of $stat.tree ref etc...
	k=3;
}

@header {
	package de.uka.ilkd.key.dl.parser;
	import de.uka.ilkd.key.util.Debug;
}

@members {
	boolean schemaMode = true;

	void setSchemaMode(boolean schemaMode) {
		this.schemaMode = schemaMode;
	}

    // don't just print but throw all exceptions
	public void emitErrorMessage(String msg) {
		//@todo turn into an exception type that declares as a "ParseException"
        throw new IllegalStateException("Parse error:\n" + msg);
	}

    // add rule invocation stack to improve debug information of parser errors
	public String getErrorMessage(RecognitionException e, 
		String[] tokenNames) 
	{ 
		List stack = getRuleInvocationStack(e, this.getClass().getName()); 
		String msg = null; 
		if (e instanceof NoViableAltException) { 
			NoViableAltException nvae = (NoViableAltException)e; 
			msg = super.getErrorMessage(e, tokenNames);
			//msg += "no viable alternative at input "+getTokenErrorDisplay(e.token) + " in token="+e.token; 
			// msg = " no viable alt; token="+e.token+ 
			// 	" (decision="+nvae.decisionNumber+ 
			// 	" state "+nvae.stateNumber+")"+ 
			// 	" decision=<<"+nvae.grammarDecisionDescription+">>"; 
		} 
		else { 
			msg = super.getErrorMessage(e, tokenNames); 
		} 
		return getErrorHeader(e) + " " + msg + "\nafter parsing " + stack; 
	} 
	public String getTokenErrorDisplay(Token t) { 
		return t.toString(); 
	}

    // // ensure the lexer does not silently skip over unrecognized tokens
    // protected Object recoverFromMismatchedToken(IntStream input,
    //                                             int ttype,
    //                                             BitSet follow)
    //     throws RecognitionException
    // {
    //     throw new MismatchedTreeNodeException(ttype, (TreeNodeStream)input);
    // }
}

@rulecatch {
    // ensure the lexer does not silently skip over unrecognized tokens
    catch (RecognitionException e) {
        throw e;
    }
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

prog:  stat { 
		if (Debug.ENABLE_DEBUG) {
			System.out.println($stat.tree.toStringTree());
		}
	} (CHOP!)? EOF!; 

annotation: ANNOTATION WORD LPAREN! form[true]? (COMMA! form[true])* RPAREN!;

stat: parallel
;

parallel: chop (PARALLEL^ parallel)?
;

chop: choice (CHOP^ chop)?
;

choice: star (CHOICE^ choice)?
;

star: atomp (/*invariant?*/ STAR^ (annotation)*)?
;

/*invariant: INVARIANT^ LPAREN! form RPAREN!
;*/

atomp options { k=3; } : itomp^ (annotation)*
;

itomp options { k=3; } : quest | assign | LPAREN! stat RPAREN! | diffsystem | ifThenElse | vardec | {schemaMode}? sv | whileSym
;

whileSym: WHILE^ LPAREN! form[false] RPAREN! stat (ELIHW!|END!)
;

vardec: (type var (COMMA var)*) -> ^(VARDEC type var*)
;

ifThenElse: IF^ LPAREN! form[false] RPAREN! THEN! stat (ELSE! stat)? (FI!|END!)
;

diffeq: diff EQUALS^ expr[true]
;

quest: QUEST^ form[false]; /* FO grammar goes here */

form[boolean diffAllowed]: biimplies[diffAllowed] 
;

biimplies[boolean diffAllowed]: implies[diffAllowed] (BIIMPL^ biimplies[diffAllowed])?
;

implies[boolean diffAllowed]: or[diffAllowed] (IMPL^ implies[diffAllowed])?
;

or[boolean diffAllowed]: and[diffAllowed] (OR^ or[diffAllowed])?
;

and[boolean diffAllowed]: pred[diffAllowed] (AND^ and[diffAllowed])?
;

pred[boolean diffAllowed]: 
(expr[true] brel) => expr[diffAllowed] brel^ expr[diffAllowed]
| func[diffAllowed] 
| LPAREN! form[diffAllowed] RPAREN!
| NOT^ pred[diffAllowed]
| (FORALL|EXISTS)^ vardec CHOP pred[diffAllowed]
| {schemaMode}? sv
| TRUE
| FALSE
;

list[boolean diffAllowed]: LPAREN! expr[diffAllowed] (COMMA! expr[diffAllowed])* RPAREN!
;

brel: LESS
| LESS_EQUALS
| EQUALS
| GREATER_EQUALS
| GREATER
| UNEQUALS
| {schemaMode}? sv
;

assign: (WORD|{schemaMode}? sv) ASSIGN^ (expr[false] | STAR)
;

expr[boolean diffAllowed]:
multExpr[diffAllowed] ((PLUS^|MINUS^) multExpr[diffAllowed])*
; 

multExpr[boolean diffAllowed] options { k = 3; }: 
expexpr[diffAllowed] ((STAR|DIV)^ expexpr[diffAllowed])*
; 

expexpr[boolean diffAllowed]: 
MINUS expexpr[diffAllowed] -> ^(MINUSSIGN expexpr)
| atom[diffAllowed] (EXP^ expexpr[diffAllowed])?
;

atom[boolean diffAllowed]: 
func[diffAllowed]
| NUM
| LPAREN! expr[diffAllowed] RPAREN!
| { diffAllowed }? diff
| {schemaMode}? sv
;

func[boolean diffAllowed]: (WORD_DOLLAR | WORD)^ (list[diffAllowed])?
;

var: WORD_DOLLAR | WORD
;

type: WORD
;

diff: WORD (DOT^)+
;

diffsystem:
LBRACE form[true] (COMMA form[true])* RBRACE
-> ^(DIFFSYSTEM form+)
| diffeq -> ^(DIFFSYSTEM diffeq)
;

sv: SV^ WORD;

TRUE		: 'true' ;
FALSE		: 'false' ;
SV		: '#' ;
//INVARIANT: '@invariant' ;
VARDEC	: '@decl@';
DIFFSYSTEM : '@DIFFSYSTEM@';
ANNOTATION : '@';
NOT		: '!' ;
IMPL	: '->';
BIIMPL	: '<->';
PLUS 	: '+' ;
MINUS	: '-' ;
MINUSSIGN: '@-@' ;
STAR	: '*' ;
DIV		: '/' ;	
EXP 	: '^' ;
ASSIGN	: ':=';
EQUALS	: '=' ;
UNEQUALS: '!=';
CHOICE 	: '++';
PARALLEL: '||';
FORALL	: '\\forall';
EXISTS	: '\\exists';
IF		: 'if';
FI		: 'fi';
THEN	: 'then';
ELSE	: 'else';
DOT		: '`'|'\'';
QUEST	: '?';
LPAREN 	: '(' ;
RPAREN	: ')' ;
LBRACE	: '{' ;
RBRACE	: '}' ;
LB		: '[' ;
RB		: ']' ;
COMMA	: ',' ;
CHOP	: ';' ;
AND		: '&' ;
OR		: '|' ;
LESS	: '<' ;
GREATER	: '>' ;
WHILE   : 'while';
ELIHW   : 'elihw';
END   : 'end';
LESS_EQUALS : LESS EQUALS;
GREATER_EQUALS : GREATER EQUALS;
//WORD  	:   (('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9'|'_')+) ;
NUM 	:   '0'..'9'* ('0'..'9'| ('.' '0'..'9'+)) ;
WORD  	:   ('a'..'z'|'A'..'Z'|'0'..'9'|'_')+ ;
WORD_DOLLAR  	:   '$' ('a'..'z'|'A'..'Z'|'0'..'9'|'_')+ ;
WS  	:   (' '|'\t'|'\r'|'\n')+ {skip();} ;
COMMENT :   '/*' .* '*/' {$channel=HIDDEN;};
