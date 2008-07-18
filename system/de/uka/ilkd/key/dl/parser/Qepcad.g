grammar Qepcad;

@header {
	package de.uka.ilkd.key.dl.arithmetics.impl.qepcad;
	import de.uka.ilkd.key.logic.Term;
	import de.uka.ilkd.key.logic.TermBuilder;
}

@lexer::header {
	package de.uka.ilkd.key.dl.arithmetics.impl.qepcad;
}

@members {
	TermBuilder tb = TermBuilder.DF;
}

// Parser Rules

formula returns [ Term t ]: st = booleanValue{ t = st; } EOF;

booleanValue returns [ Term t ] : ( s = ff ) { t = s; } | ( s = tt ) { t = s; };

tt returns [ Term t ] : TRUE { t = tb.tt(); };
ff returns [Term t ] : FALSE { t = tb.ff(); };

// Lexer-Rules

TRUE 	:	'TRUE';
FALSE	:	'FALSE';
