tree grammar DLStage2;

options {
	ASTLabelType=CommonTree;
	tokenVocab=DL;
}

@header {
	package de.uka.ilkd.key.dl.parser;
	import de.uka.ilkd.key.dl.model.*;
	import de.uka.ilkd.key.java.ProgramElement;
	import java.util.List;
	import java.util.ArrayList;
	import java.util.Map;
	import java.util.HashMap;
	import java.math.BigDecimal;
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
	
	static String canonicalize(String annotationKey) {
		if (annotationKey == null)
  		    return null;
		// support obsolete annotations by conversion
		else if ("strengthen".equals(annotationKey))
		    return "invariant";
		else
			return annotationKey;
	}
}

/*@init {
	tf = new TermFactory();
}*/

prog returns [ DLProgram pe ]:   st = stat { pe = st; } EOF; 

stat returns [ DLProgram pe ] scope { ArrayList<Formula> params; } @init {$stat::params = new ArrayList<Formula>(); }: 
	st = astat { pe = st; } 
;

annotation[ DLProgram pe ]: (ANNOTATION w = WORD 
	{
		String annotationKey = canonicalize(w.toString());
		if(!pe.getDLAnnotations().containsKey(annotationKey)) { 
			pe.setDLAnnotation(annotationKey, new ArrayList<Formula>()); 
		} else { 
			throw new RecognitionException(); /* todo add text filename and line */
		} 
	} (f = form[true] { pe.getDLAnnotation(canonicalize(w.toString())).add(f); })* )
;

astat returns [ DLProgram pe ] : 
^(CHOP st = stat { pe = st; } (st = stat { pe = tf.createChop(pe, st); })* (annotation[pe])*)
| ^(CHOICE st = stat { pe = st; } (st = stat { pe = tf.createChoice(pe, st); })* (annotation[pe])*)
| ^(PARALLEL st = stat { pe = st; } (st = stat { pe = tf.createParallel(pe, st); })* (annotation[pe])*)
| ^(STAR st = stat { pe = tf.createStar(st); } (annotation[pe])*)
| ^(QUEST frm = form[false] { pe = tf.createQuest(frm); } (annotation[pe])*)
| (pelem = asordiffsys { pe = pelem; })
| ^(VARDEC dec = vardecl[true] { pe = dec; } (annotation[pe])*)
| ^(SVARDEC w = WORD sv = svar { pe = tf.schemaCreateVariableDeclaration(w, sv); } (annotation[pe])*)
| ^(IF frm = form[false] st = stat (st2 = stat)? { pe = tf.createIf(frm, st, st2); } (annotation[pe])*)
| ^(WHILE frm = form[false] st = stat { DLProgram star = tf.createStar(tf.createChop(tf.createQuest(frm), st)); pe = tf.createChop(star, tf.createQuest(tf.createNot(frm)));} (annotation[star])*) 
| ^(FORALL ^(VARDEC decl = vardecl[false]) aod = asordiffsys { pe = tf.createQuantified(decl, aod); tf.unbind(); })
| {schemaMode}? ^(FORALL sv = svar aod = asordiffsys { pe = tf.schemaCreateQuantified(sv, aod); })
| {schemaMode}? sv = svar { pe = tf.schemaProgramVariable(sv); }
;

asordiffsys returns [ DLProgram pe ]:
^(ASSIGN as = assign { pe = as; } (annotation[pe])*)
| ^(DIFFSYSTEM dsc = form[true] { $stat::params.add(dsc); } (d = form[true] { $stat::params.add(d); })* { pe = tf.createDiffSystem($stat::params); $stat::params.clear(); } (annotation[pe])*)
;

form[boolean diff] returns [ Formula fe ] scope { ArrayList<Expression> params; } @init { $form::params = new ArrayList<Expression>(); }: 
^(OR frm = form[diff] { fe = frm; } ( f = form[diff] { fe = tf.createOr(fe, f); })*)
| ^(AND frm = form[diff] { fe = frm; } ( f = form[diff] { fe = tf.createAnd(fe, f); })*)
| ^(IMPL frm = form[diff] { fe = frm; } ( f = form[diff] { fe = tf.createImpl(fe, f); })*)
| ^(BIIMPL frm = form[diff] { fe = frm; } ( f = form[diff] { fe = tf.createBiImpl(fe, f); })*)
| ^(NOT frm = form[diff] { fe = tf.createNot(frm); })
| ^(LB pe = stat frm = form[diff] { fe = tf.createBox(pe, frm); })
| ^(DIA pe = stat frm = form[diff] { fe = tf.createDiamond(pe, frm); })
| ^(bop = brel e = expr[diff] e2 = expr[diff]) { fe = tf.createBinaryRelation(bop, e, e2); }
| ^(FORALL ^(VARDEC decl = vardecl[false]) (CHOP|BULLET) frm = form[diff] { fe = tf.createForall(decl, frm); tf.unbind(); })
| ^(EXISTS ^(VARDEC decl = vardecl[false]) (CHOP|BULLET) frm = form[diff] { fe = tf.createExists(decl, frm); tf.unbind(); })
| ^(t = WORD (e = expr[diff] { $form::params.add(e); })* { fe = tf.createPredicateTerm(t, $form::params); $form::params.clear(); })
| t = (WORD_DOLLAR | WORD) { fe = tf.createPredicateTerm(t); }
| {schemaMode}? sv = svar { fe = tf.schemaTermVariable(sv, diff); }
| ^(TRUE { fe = tf.createBinaryRelation(tf.createEquals(), tf.createConstant(new BigDecimal(0)), tf.createConstant(new BigDecimal(0))); })
| ^(FALSE { fe = tf.createBinaryRelation(tf.createEquals(), tf.createConstant(new BigDecimal(0)), tf.createConstant(new BigDecimal(1))); })
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

assign returns [ DLProgram a ] scope { ArrayList<Expression> args; } @init { $assign::args = new ArrayList<Expression>(); }: 
(var = WORD 
(LPAREN (arg = expr[false] { $assign::args.add(arg); })* RPAREN)? 
(e = expr[false] { a = tf.createAssign(var, $assign::args, e); }
| STAR { a = tf.createRandomAssign(var, $assign::args); }))
| ({schemaMode}? sv = svar (e = expr[false] { a = tf.createAssignToSchemaVariable(sv, e);} 
| STAR { a = tf.createRandomAssignToSchemaVariable(sv); }))
;

vardecl[boolean programVariable] returns [ VariableDeclaration a ] scope { List<CommonTree> decls; Map<CommonTree, List<CommonTree>> argsorts; } 
	@init { $vardecl::decls = new ArrayList<CommonTree>(); $vardecl::argsorts = new HashMap<CommonTree, List<CommonTree>>();}: 
vt =WORD (var = WORD { $vardecl::decls.add(var); } (fargs = funargs { $vardecl::argsorts.put(var, fargs); })? )+ 
	{ a = tf.createVariableDeclaration(vt, $vardecl::decls, $vardecl::argsorts, programVariable); }
;

funargs returns [ List<CommonTree> r ]:
LPAREN (t = WORD { if(r == null) { r = new ArrayList<CommonTree>(); }; r.add(t); })+ RPAREN
;

expr[boolean diffAllowed] returns [ Expression pe ] scope { ArrayList<Expression> params; } @init { $expr::params = new ArrayList<Expression>(); }:
^(op = PLUS e=expr[diffAllowed] { pe = e; } (b=expr[diffAllowed] { pe = tf.createPlus(pe, b); })+)
| ^(op = MINUSSIGN e=expr[diffAllowed] {pe = tf.createMinusSign(e); })
| ^(op = MINUS e=expr[diffAllowed] {pe = e; } (b=expr[diffAllowed] { pe = tf.createMinus(pe, b); })+)
| ^(op = STAR e=expr[diffAllowed] { pe = e; } (b=expr[diffAllowed] { pe = tf.createMult(pe, b); })+)
| ^(op = DIV e=expr[diffAllowed] { pe = e; } (b=expr[diffAllowed] { pe = tf.createDiv(pe, b); })+)
| ^(op = EXP e = expr[diffAllowed] b=expr[false] { pe = e; } { pe = tf.createExp(pe, b); })
| ^(t = WORD (e = expr[diffAllowed] { $expr::params.add(e); })* { pe = tf.createAtomicTerm(t, $expr::params); $expr::params.clear(); })
| t = (WORD | WORD_DOLLAR) { pe = tf.createAtomicTerm(t); }
| num = NUM { pe = tf.createConstant(num); }
| {diffAllowed}? d = diff { pe = d; }
| sv = svar { pe = tf.schemaExpressionVariable(sv); }
| ^(IF f=form[diffAllowed] e=expr[diffAllowed] e2=expr[diffAllowed] { pe = tf.createIfExpr(f,e,e2); })
; 

diff returns [ Dot pe ] scope { ArrayList<Expression> args; int count; } @init { $diff::args = new ArrayList<Expression>(); $diff::count = 1; }:
^(DOT (^(w = WORD (arg = expr[false] { $diff::args.add(arg); })*) (DOT {$diff::count++;})* { pe = tf.createDot($diff::count, w, $diff::args); $diff::args.clear(); } ) )
| ^(DOT (sv = svar) (DOT {$diff::count++;})* { pe = tf.schemaCreateDot(sv, $diff::count); $diff::args.clear(); } )
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
