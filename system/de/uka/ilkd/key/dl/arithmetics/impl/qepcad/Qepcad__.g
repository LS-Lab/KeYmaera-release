lexer grammar Qepcad;
@header {
	package de.uka.ilkd.key.dl.arithmetics.impl.qepcad;
}

// $ANTLR src "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g" 70
TRUE :	'TRUE';
// $ANTLR src "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g" 71
FALSE:	'FALSE';

// $ANTLR src "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g" 73
AND	:	'/\\' ;
// $ANTLR src "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g" 74
OR	:	'\\/';

// $ANTLR src "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g" 76
GE	:	'>=';
// $ANTLR src "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g" 77
GT	:	'>';
// $ANTLR src "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g" 78
LT	:	'<';
// $ANTLR src "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g" 79
LE	:	'<=';
// $ANTLR src "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g" 80
EQ	:	'=';
// $ANTLR src "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g" 81
NE	:	'/=';

// $ANTLR src "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g" 83
ADD	:	'+';
// $ANTLR src "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g" 84
SUB	:	'-';
// $ANTLR src "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g" 85
MUL	:	'*';
// $ANTLR src "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g" 86
DIV	:	'/';

// $ANTLR src "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g" 88
LB 	:	 '[';
// $ANTLR src "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g" 89
RB	:	']';

// $ANTLR src "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g" 91
POW	:	'^';

// $ANTLR src "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g" 93
WS 	:  	('\t' | ' ' | '\r' | '\n'| '\u000C' )+ { $channel = HIDDEN; };
// $ANTLR src "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g" 94
NUM 	:   	'0'..'9'* ('0'..'9'| ('.' '0'..'9'+)) ;
// $ANTLR src "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g" 95
VAR	:	('a'..'z'|'A'..'Z');


