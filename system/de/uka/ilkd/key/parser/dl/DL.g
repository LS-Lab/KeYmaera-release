grammar DL;

options {
	output=AST;
	ASTLabelType=CommonTree; // type of $stat.tree ref etc...
	k=3;
}

@header {
	package de.uka.ilkd.key.parser.dl;
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
    public void recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow)
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
	package de.uka.ilkd.key.parser.dl;
}

prog:  stat { 
		if (Debug.ENABLE_DEBUG) {
			System.out.println($stat.tree.toStringTree());
		}
	} (CHOP!)? EOF!; 

stat: parallel
;

parallel: chop (PARALLEL^ parallel)?
;

chop: choice (CHOP^ chop)?
;

choice: star (CHOICE^ choice)?
;

star: atomp (invariant? STAR^)?
;

invariant: INVARIANT^ LPAREN! form RPAREN!
;


atomp options { k=3; } : quest | assign | LPAREN! stat RPAREN! | diffsystem | ifThenElse | vardec | {schemaMode}? sv
;

vardec: (type var (COMMA var)*) -> ^(VARDEC type var*)
;

ifThenElse: IF^ LPAREN! form RPAREN! THEN! stat (ELSE! stat)? FI!
;

diffeq: diff EQUALS^ expr[true]
;

quest: QUEST^ form; /* FO grammar goes here */

form: biimplies 
;

biimplies: implies (BIIMPL^ biimplies)?
;

implies: or (IMPL^ implies)?
;

or: and (OR^ or)?
;

and: pred (AND^ and)?
;

pred: 
(expr[false] brel) => expr[false] brel^ expr[false]
| func[false] 
| LPAREN! form RPAREN!
| NOT^ pred
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

func[boolean diffAllowed]: WORD^ (list[diffAllowed])?
;

var: WORD 
;

type: WORD
;

diff: WORD (DOT^)+
;

diffsystem:
LBRACE algodiffeq[true] (COMMA algodiffeq[true])* RBRACE
-> ^(DIFFSYSTEM algodiffeq+)
| diffeq -> ^(DIFFSYSTEM diffeq)
;

algodiffeq[boolean diffAllowed]: 
/* paremter is only used because of wrong parser generation */
(diff) => diffeq
| (LPAREN diff) => LPAREN! diffeq RPAREN!
| form
;

sv: SV^ WORD;

SV		: '#' ;
INVARIANT: '@invariant' ;
VARDEC	: '@decl@';
DIFFSYSTEM : '@';
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
IF		: 'if';
FI		: 'fi';
THEN	: 'then';
ELSE	: 'else';
DOT		: '`';
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
LESS_EQUALS : LESS EQUALS;
GREATER_EQUALS : GREATER EQUALS;
//WORD  	:   (('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9'|'_')+) ;
NUM 	:   '0'..'9'* ('0'..'9'| ('.' '0'..'9'+)) ;
WORD  	:   ('a'..'z'|'A'..'Z'|'0'..'9'|'_')+ ;
WS  	:   (' '|'\t'|'\r'|'\n')+ {skip();} ;
COMMENT :   '/*' .* '*/' {$channel=HIDDEN;};
