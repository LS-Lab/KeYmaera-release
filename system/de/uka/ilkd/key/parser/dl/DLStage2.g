tree grammar DLStage2;

options {
	ASTLabelType=CommonTree;
	tokenVocab=DL;
}

@header {
	package de.uka.ilkd.key.parser.dl;
	import de.uka.ilkd.key.dl.model.*;
	import de.uka.ilkd.key.java.ProgramElement;
	import java.util.ArrayList;
}

@members {
	TermFactory tf; 
	boolean schemaMode = true;

	void setTermFactory(TermFactory tf) {
		this.tf = tf;
	}

	void setSchemaMode(boolean schemaMode) {
		this.schemaMode = schemaMode;
	}
}

/*@init {
	tf = new TermFactory();
}*/

prog returns [ DLProgram pe ]:   st = stat { pe = st; } EOF; 

stat returns [ DLProgram pe ] scope { ArrayList<Formula> params; } @init {$stat::params = new ArrayList<Formula>(); } : 
^(CHOP st = stat { pe = st; } (st = stat { pe = tf.createChop(pe, st); })*)
| ^(CHOICE st = stat { pe = st; } (st = stat { pe = tf.createChoice(pe, st); })*)
| ^(PARALLEL st = stat { pe = st; } (st = stat { pe = tf.createParallel(pe, st); })*)
| ^(STAR st = stat (frm = invariant)?) { pe = tf.createStar(st, frm); }
| ^(QUEST frm = form[false]) { pe = tf.createQuest(frm); }
| ^(ASSIGN as = assign) { pe = as; }
| ^(DIFFSYSTEM dsc = form[true] { $stat::params.add(dsc); } (d = form[true] { $stat::params.add(d); })*) { pe = tf.createDiffSystem($stat::params); $stat::params.clear(); }
| ^(VARDEC dec = vardecl[true]) { pe = dec; }
| ^(IF frm = form[false] st = stat (st2 = stat)? { pe = tf.createIf(frm, st, st2); })
| {schemaMode}? sv = svar { pe = tf.schemaProgramVariable(sv); }
;

form[boolean diff] returns [ Formula fe ] scope { ArrayList<Expression> params; } @init { $form::params = new ArrayList<Expression>(); }: 
^(OR frm = form[diff] { fe = frm; } ( f = form[diff] { fe = tf.createOr(fe, f); })*)
| ^(AND frm = form[diff] { fe = frm; } ( f = form[diff] { fe = tf.createAnd(fe, f); })*)
| ^(IMPL frm = form[diff] { fe = frm; } ( f = form[diff] { fe = tf.createImpl(fe, f); })*)
| ^(BIIMPL frm = form[diff] { fe = frm; } ( f = form[diff] { fe = tf.createBiImpl(fe, f); })*)
| ^(NOT frm = form[diff] { fe = tf.createNot(frm); })
| ^(bop = brel e = expr[diff] e2 = expr[diff]) { fe = tf.createBinaryRelation(bop, e, e2); }
| ^(FORALL ^(VARDEC decl = vardecl[false]) CHOP frm = form[diff] { fe = tf.createForall(decl, frm); })
| ^(EXISTS ^(VARDEC decl = vardecl[false]) CHOP frm = form[diff] { fe = tf.createExists(decl, frm); })
| ^(t = WORD (e = expr[diff] { $form::params.add(e); })* { fe = tf.createPredicateTerm(t, $form::params); $form::params.clear(); })
| t = WORD { fe = tf.createPredicateTerm(t); }
| {schemaMode}? sv = svar { fe = tf.schemaTermVariable(sv, diff); }
;

brel returns [ Comparsion bo ]: 
LESS { bo = tf.createLess(); }
| LESS_EQUALS { bo = tf.createLessEquals(); }
| EQUALS { bo = tf.createEquals(); }
| GREATER_EQUALS { bo = tf.createGreaterEquals(); }
| GREATER { bo = tf.createGreater(); }
| UNEQUALS { bo = tf.createUnequals(); }
| {schemaMode}? sv = svar { bo = tf.schemaBrelVariable(sv); }
;

assign returns [ DLProgram a ]: 
var = WORD (e = expr[false] { a = tf.createAssign(var, e); }
| STAR { a = tf.createRandomAssign(var); })
| {schemaMode}? sv = svar (e = expr[false] { a = tf.createAssignToSchemaVariable(sv, e);} 
| STAR { a = tf.createRandomAssignToSchemaVariable(var); })
;

vardecl[boolean programVariable] returns [ VariableDeclaration a ] scope { List<CommonTree> decls; } @init { $vardecl::decls = new ArrayList<CommonTree>(); }: 
vt = WORD (var = WORD { $vardecl::decls.add(var);} )+ { a = tf.createVariableDeclaration(vt, $vardecl::decls, programVariable); }
;

expr[boolean diffAllowed] returns [ Expression pe ] scope { ArrayList<Expression> params; } @init { $expr::params = new ArrayList<Expression>(); }:
^(op = PLUS e=expr[diffAllowed] { pe = e; } (b=expr[diffAllowed] { pe = tf.createPlus(pe, b); })+)
| ^(op = MINUSSIGN e=expr[diffAllowed] {pe = tf.createMinusSign(e); })
| ^(op = MINUS e=expr[diffAllowed] {pe = e; } (b=expr[diffAllowed] { pe = tf.createMinus(pe, b); })+)
| ^(op = STAR e=expr[diffAllowed] { pe = e; } (b=expr[diffAllowed] { pe = tf.createMult(pe, b); })+)
| ^(op = DIV e=expr[diffAllowed] { pe = e; } (b=expr[diffAllowed] { pe = tf.createDiv(pe, b); })+)
| ^(op = EXP e = expr[diffAllowed] b=expr[false] { pe = e; } { pe = tf.createExp(pe, b); })
| ^(t = WORD (e = expr[diffAllowed] { $expr::params.add(e); })* { pe = tf.createAtomicTerm(t, $expr::params); $expr::params.clear(); })
| t = WORD { pe = tf.createAtomicTerm(t); }
| num = NUM { pe = tf.createConstant(num); }
| {diffAllowed}? d = diff { pe = d; }
| sv = svar { pe = tf.schemaExpressionVariable(sv); }
; 

diff returns [ Dot pe ]: 
^(DOT (w = WORD { pe = tf.createDot(w); } | m = diff { pe = m; pe = tf.raiseDotCount(pe); }))
;

/*diffsystemcontent returns [ Formula dsc ]: 
(brel DOT) => ^(op = brel d = diff de = expr[true]) { dsc = tf.createDiffSystemContent(op, d, de); }
| ^(op = brel t = WORD de = expr[true]){ dsc = tf.createDiffSystemContent(op, t, de); }
;*/

svar returns [ CommonTree t]: ^(SV w = WORD) { t = w; }
;

invariant returns [ Formula frm ]:
^(INVARIANT inv = form[false]) { frm = inv; }
;
