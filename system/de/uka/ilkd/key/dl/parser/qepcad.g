grammar qepcad;

options {
	language=Java;
}

@header {
	package de.uka.ilkd.key.dl.parser;
	import de.uka.ilkd.key.dl.model.*;
	import de.uka.ilkd.key.java.ProgramElement;
	import java.util.ArrayList;
}

@members {
	TermBuilder tb; 
}

@init {
	tb = TermBuilder.DF;
}

// Parser Rules

formula returns [ Term t ]: st = true { t = st } EOF;

true returns [ Term t ] : TRUE { t = tb.tt(); };

// Lexer-Rules

TRUE 	:	'TRUE';
