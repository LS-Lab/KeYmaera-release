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

    // ensure the lexer does not silently skip over unrecognized tokens
    protected void mismatch(IntStream input, int ttype, BitSet follow)
    throws RecognitionException {
        throw new MismatchedTokenException(ttype, input);
    }

    // ensure the lexer does not silently skip over unrecognized tokens
    public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow)
    throws RecognitionException {
        throw e;
    }
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
atom[diffAllowed] (EXP^ expexpr[diffAllowed])?
;

atom[boolean diffAllowed]: 
func[diffAllowed]
| NUM
| LPAREN! expr[diffAllowed] RPAREN!
| { diffAllowed }? diff
| MINUS atom[diffAllowed] -> ^(MINUSSIGN atom)
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
