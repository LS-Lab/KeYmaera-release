grammar Qepcad;

@header {
	package de.uka.ilkd.key.dl.arithmetics.impl.qepcad;
	import de.uka.ilkd.key.logic.Term;
	import de.uka.ilkd.key.logic.TermBuilder;
	import de.uka.ilkd.key.dl.parser.NumberCache;
	import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
	import de.uka.ilkd.key.logic.NamespaceSet;
	import de.uka.ilkd.key.logic.Name;
	import de.uka.ilkd.key.logic.op.LogicVariable;
	import de.uka.ilkd.key.logic.op.Function;
	import java.math.BigDecimal;
	import de.uka.ilkd.key.logic.op.ProgramVariable;
	import de.uka.ilkd.key.dl.model.Variable;
}

@lexer::header {
	package de.uka.ilkd.key.dl.arithmetics.impl.qepcad;
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
       		 LogicVariable n = (LogicVariable)this.nss.variables().lookup(new Name(var));
        		if( n != null ) {
            		return tb.var(n);
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

formula returns [Term t ]: st = expression { t = st; } EOF;

expression returns [ Term t ]	: 	e = expr_add {t = e; } ( EQ f = expression {t = tb.equals(e,f);  } )?; // Gleich

expr_add returns [Term t ]		:	e = expr_sub { t = e; } ( ADD f = expr_add { t = tb.func((Function)nss.functions().lookup( new Name("add")),e,f); } )?; // Addition
expr_sub returns [ Term t ]		:	e = atom { t = e; } ( SUB f = expr_add {  t = tb.func((Function)nss.functions().lookup( new Name("sub")),e,f);} )?; // Subtraktion
atom returns [Term t ]		:	( LB st = expression RB { t = st; }) | 
							( st = varOrNum { t = st; } ); 

varOrNum returns [ Term t ]	: 	(st = variable { t = st;} ) | 
							( st = number { t = st; } );
							
variable returns [ Term t ] 		:	s = VAR { t=getVariable(s.getText()); };
number returns [ Term t]		:	n = NUM { t = tb.func(NumberCache.getNumber(BigDecimal.valueOf(Integer.valueOf(n.getText())),RealLDT.getRealSort())); };

// Lexer-Rules

TRUE :	'TRUE';
FALSE:	'FALSE';

AND	:	'/\\' ;
OR	:	'\\/';

GE	:	'>=';
GT	:	'>';
LT	:	'<';
LE	:	'<=';
EQ	:	'=';
NE	:	'/=';

ADD	:	'+';
SUB	:	'-';
MUL	:	'*';
DIV	:	'/';

LB 	:	 '[';
RB	:	']';

POW	:	'^';

WS 	:  	('\t' | ' ' | '\r' | '\n'| '\u000C' )+ { $channel = HIDDEN; };
NUM 	:   	'0'..'9'* ('0'..'9'| ('.' '0'..'9'+)) ;
VAR	:	('a'..'z'|'A'..'Z');


