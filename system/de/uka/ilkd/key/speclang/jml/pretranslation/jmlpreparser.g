// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

header {
    package de.uka.ilkd.key.speclang.jml.pretranslation;
    
    import java.io.StringReader;
    import java.util.Iterator;
    
    import de.uka.ilkd.key.collection.*;
    import de.uka.ilkd.key.java.Position;
    import de.uka.ilkd.key.speclang.*;
    import de.uka.ilkd.key.speclang.translation.*;
}


class KeYJMLPreParser extends Parser;


options {
    k = 1;	//do not change - interplay with lexer depends on k=1!
    defaultErrorHandler = false;
    importVocab = KeYJMLPreLexer;
}


{
    private KeYJMLPreLexer lexer;
    private SLTranslationExceptionManager excManager;
    private ImmutableSet<PositionedString> warnings 	
    	= DefaultImmutableSet.<PositionedString>nil();
    
    
    private KeYJMLPreParser(KeYJMLPreLexer lexer,
                            String fileName,
                            Position offsetPos) {
    	this(lexer);
    	this.lexer      = lexer;
    	this.excManager = new SLTranslationExceptionManager(this, 
    							    fileName, 
    							    offsetPos); 
    }
    
    
    public KeYJMLPreParser(String comment, 
    			   String fileName, 
    			   Position offsetPos) {
	this(new KeYJMLPreLexer(new StringReader(comment)), 
	     fileName, 
	     offsetPos); 
    }
       
        
    private PositionedString createPositionedString(String text, 
    						    Token t) {
    	return excManager.createPositionedString(text, t);
    }
    
    
    private void raiseError(String msg) throws SLTranslationException {
        throw excManager.createException(msg);
    }
    
    
    private void raiseNotSupported(String feature) 
    		throws SLTranslationException {    		
	PositionedString warning 
		= excManager.createPositionedString(feature + " not supported");
    	warnings = warnings.add(warning);
    }
        
    
    public ImmutableList<TextualJMLConstruct> parseClasslevelComment() 
    		throws SLTranslationException {
        try {
            return classlevel_comment();
        } catch(ANTLRException e) {
	    throw excManager.convertException(e);
        }
    }
    
    
    public ImmutableList<TextualJMLConstruct> parseMethodlevelComment() 
    		throws SLTranslationException {
        try {
            return methodlevel_comment();
        } catch(ANTLRException e) {
	    throw excManager.convertException(e);
        }
    }
    
    
    public ImmutableSet<PositionedString> getWarnings() {
    	return warnings;
    }
}



//-----------------------------------------------------------------------------
//comments
//-----------------------------------------------------------------------------

classlevel_comment 
	returns [ImmutableList<TextualJMLConstruct> result 
		 = ImmutableSLList.<TextualJMLConstruct>nil()] 
	throws SLTranslationException
{
    ImmutableList<String> mods = ImmutableSLList.<String>nil();
    ImmutableList<TextualJMLConstruct> list;
}
:
    (
        options { greedy = false; }
    	:
    	mods=modifiers 
    	list=classlevel_element[mods]  
    	{ 
	    if(list!=null) {
	    	result = result.append(list); 
	    }
	}
    )*
    EOF
;


classlevel_element[ImmutableList<String> mods] 
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException
:
        result=class_invariant[mods]
    |   result=method_specification[mods]
    |   (method_declaration[mods]) => result=method_declaration[mods]
    |   result=field_declaration[mods]    
    |   result=history_constraint[mods]
    |   result=represents_clause[mods]
    |   result=initially_clause[mods]
    |   result=monitors_for_clause[mods]
    |   result=readable_if_clause[mods]
    |   result=writable_if_clause[mods]
    |   result=datagroup_clause[mods]
    |   result=set_statement[mods]    //RecodeR workaround
    |   result=assert_statement[mods] //RecodeR workaround
    |   result=assume_statement[mods] //RecodeR workaround
    |   result=nowarn_pragma[mods] 
    |   EOF
;


methodlevel_comment 
	returns [ImmutableList<TextualJMLConstruct> result 
		 = ImmutableSLList.<TextualJMLConstruct>nil()] 
	throws SLTranslationException
{
    ImmutableList<String> mods = ImmutableSLList.<String>nil();
    ImmutableList<TextualJMLConstruct> list;
}
:
    (
    	mods=modifiers 
    	list=methodlevel_element[mods]  { result = result.append(list); }
    )*
    EOF
;


methodlevel_element[ImmutableList<String> mods] 
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException
:
        result=field_declaration[mods]
    |   result=set_statement[mods]
    |   result=loop_specification[mods]
    |   result=assert_statement[mods]
    |   result=assume_statement[mods]
    |   result=nowarn_pragma[mods]
;



//-----------------------------------------------------------------------------
//modifiers
//-----------------------------------------------------------------------------

modifiers 
	returns [ImmutableList<String> result = ImmutableSLList.<String>nil()] 
	throws SLTranslationException
{
    String s;
}
:
    (
    	options { greedy = true; }
    	:
	s=modifier  { result = result.append(s); } 
    )*
;


modifier returns [String result = null]:
        abs:ABSTRACT            { result = abs.getText(); }
    |   fin:FINAL               { result = fin.getText(); }
    |   gho:GHOST               { result = gho.getText(); } 
    |   hel:HELPER              { result = hel.getText(); }
    |   ins:INSTANCE            { result = ins.getText(); }
    |   mod:MODEL               { result = mod.getText(); }
    |   nnu:NON_NULL            { result = nnu.getText(); }
    |   nul:NULLABLE            { result = nul.getText(); } 
    |   nld:NULLABLE_BY_DEFAULT { result = nld.getText(); }
    |   pri:PRIVATE             { result = pri.getText(); }
    |   pro:PROTECTED           { result = pro.getText(); }
    |   pub:PUBLIC              { result = pub.getText(); }
    |   pur:PURE                { result = pur.getText(); }
    |   spr:SPEC_PROTECTED      { result = spr.getText(); }
    |   spu:SPEC_PUBLIC         { result = spu.getText(); }
    |   sta:STATIC              { result = sta.getText(); }
;



//-----------------------------------------------------------------------------
//class invariants
//-----------------------------------------------------------------------------

class_invariant[ImmutableList<String> mods] 
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException
{
    PositionedString ps;
}
:
    invariant_keyword ps=expression
    {
    	TextualJMLClassInv inv = new TextualJMLClassInv(mods, ps);
    	result = ImmutableSLList.<TextualJMLConstruct>nil().prepend(inv);
    }
;


invariant_keyword 
:
        INVARIANT 
    |   INVARIANT_RED 
;



//-----------------------------------------------------------------------------
//method specifications
//-----------------------------------------------------------------------------

method_specification[ImmutableList<String> mods] 
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException
{
    ImmutableList<TextualJMLConstruct> list = ImmutableSLList.<TextualJMLConstruct>nil();
}
:
    (also_keyword)*
    result=spec_case[mods]
    (
    	options { greedy = true; }
    	:
    	(also_keyword)+ list=spec_case[ImmutableSLList.<String>nil()]  
    	{ 
    	    result = result.append(list); 
    	}
    )*
;


also_keyword
:
	ALSO
    |	FOR_EXAMPLE
    |   IMPLIES_THAT
;


spec_case[ImmutableList<String> mods]
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException
:
      	result=lightweight_spec_case[mods]
    | 	result=heavyweight_spec_case[mods]
;



//-----------------------------------------------------------------------------
//lightweight specification cases
//-----------------------------------------------------------------------------

lightweight_spec_case[ImmutableList<String> mods]
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException
:
    result=generic_spec_case[mods, Behavior.NONE]
;



//-----------------------------------------------------------------------------
//heavyweight specification cases
//-----------------------------------------------------------------------------

heavyweight_spec_case[ImmutableList<String> mods]
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException
{
    String s;
}
:
    (s=modifier  { mods = mods.append(s); })?
    (
	    result=behavior_spec_case[mods]
	|   result=exceptional_behavior_spec_case[mods]
      	|   result=normal_behavior_spec_case[mods]
    )
;


behavior_spec_case[ImmutableList<String> mods]
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException
:
    behavior_keyword 
    result=generic_spec_case[mods, Behavior.BEHAVIOR]
;


behavior_keyword
:
	BEHAVIOR
    | 	BEHAVIOUR
;


normal_behavior_spec_case[ImmutableList<String> mods]
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException
:
    normal_behavior_keyword 
    result=generic_spec_case[mods, Behavior.NORMAL_BEHAVIOR]
;


normal_behavior_keyword
:
      	NORMAL_BEHAVIOR
    | 	NORMAL_BEHAVIOUR
;


exceptional_behavior_spec_case[ImmutableList<String> mods]
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException
:
    exceptional_behavior_keyword 
    result=generic_spec_case[mods, Behavior.EXCEPTIONAL_BEHAVIOR]
;


exceptional_behavior_keyword
:
      	EXCEPTIONAL_BEHAVIOR
    |	EXCEPTIONAL_BEHAVIOUR
;



//-----------------------------------------------------------------------------
//generic specification cases
//-----------------------------------------------------------------------------

generic_spec_case[ImmutableList<String> mods, Behavior b] 
	returns [ImmutableList<TextualJMLConstruct> result 
		 = ImmutableSLList.<TextualJMLConstruct>nil()] 
	throws SLTranslationException
{
    ImmutableList<PositionedString> requires;
}
:
    (spec_var_decls)? 
    (
        requires=spec_header 
        (   
            (generic_spec_body[mods, b])
            =>
            result=generic_spec_body[mods, b]
        )?
        {
            if(result.isEmpty()) {
                result = result.append(new TextualJMLSpecCase(mods, b));
            }

            for(Iterator<TextualJMLConstruct> it = result.iterator(); 
                it.hasNext(); ) {
            	TextualJMLSpecCase sc = (TextualJMLSpecCase) it.next();
                sc.addRequires(requires);
            }
        }
      	| 
      	result=generic_spec_body[mods, b]
    )
;


spec_var_decls throws SLTranslationException
{
    PositionedString ps;
}
:
    (	
    	    FORALL ps=expression
    	|   OLD ps=expression
    )+
    {
    	raiseNotSupported("specification variables");
    }
;

    
spec_header 
	returns [ImmutableList<PositionedString> result 
		 = ImmutableSLList.<PositionedString>nil()] 
	throws SLTranslationException
{
    PositionedString ps;
}
:
    (
    	options { greedy = true; }
    	:
    	ps=requires_clause  { result = result.append(ps); }
    )+
;


requires_clause 
	returns [PositionedString result = null] 
	throws SLTranslationException
:
    requires_keyword result=expression
;


requires_keyword
:
    REQUIRES |
    REQUIRES_RED
;


generic_spec_body[ImmutableList<String> mods, Behavior b] 
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException
{
    TextualJMLSpecCase sc;
}
:
    result=simple_spec_body[mods, b]
    | 
    (
        NEST_START 
    	result=generic_spec_case_seq[mods, b] 
    	NEST_END
    )    	
;


generic_spec_case_seq[ImmutableList<String> mods, Behavior b] 
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException
{
    ImmutableList<TextualJMLConstruct> list;
}
:
    result=generic_spec_case[mods, b]
    (
        (also_keyword)+ 
        list=generic_spec_case[mods, b]
        { 
            result = result.append(list); 
        }
    )*
;


simple_spec_body[ImmutableList<String> mods, Behavior b] 
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException
{
    TextualJMLSpecCase sc = new TextualJMLSpecCase(mods, b);
    result = ImmutableSLList.<TextualJMLConstruct>nil().prepend(sc);
}
:
    (
    	options { greedy = true; }
    	:
    	simple_spec_body_clause[sc, b]
    )+
;


simple_spec_body_clause[TextualJMLSpecCase sc, Behavior b] 
	throws SLTranslationException
{
    PositionedString ps;
}
:
    (
	    ps=assignable_clause     { sc.addAssignable(ps); }
	|   ps=ensures_clause        { sc.addEnsures(ps); }
	|   ps=signals_clause        { sc.addSignals(ps); }
	|   ps=signals_only_clause   { sc.addSignalsOnly(ps); }
	|   ps=diverges_clause       { sc.addDiverges(ps); }
	|   ps=name_clause           { sc.addName(ps);}
	|   captures_clause 
	|   when_clause
	|   working_space_clause
	|   duration_clause
    )
    {
    	if(b == Behavior.EXCEPTIONAL_BEHAVIOR 
    	   && !sc.getEnsures().isEmpty()) {
    	    raiseError("ensures not allowed in exceptional behavior.");
    	} else if(b == Behavior.NORMAL_BEHAVIOR 
    	          && !sc.getSignals().isEmpty()) {
      	    raiseError("signals not allowed in normal behavior.");
    	} else if(b == Behavior.NORMAL_BEHAVIOR 
    	          && !sc.getSignalsOnly().isEmpty()) {
	    raiseError("signals_only not allowed in normal behavior.");
    	}
    }
;



//-----------------------------------------------------------------------------
//simple specification body clauses
//-----------------------------------------------------------------------------

assignable_clause 
	returns [PositionedString result = null] 
	throws SLTranslationException
:
    assignable_keyword result=expression
;


assignable_keyword
:
    	ASSIGNABLE 
    |   ASSIGNABLE_RED 
    |   MODIFIABLE 
    |   MODIFIABLE_RED 
    |   MODIFIES 
    |   MODIFIES_RED
;


ensures_clause 
	returns [PositionedString result = null] 
	throws SLTranslationException
:
    ensures_keyword result=expression
;


ensures_keyword
:
    	ENSURES 
    |   ENSURES_RED
;


signals_clause 
	returns [PositionedString result = null] 
	throws SLTranslationException
:
    signals_keyword result=expression
;


signals_keyword
:
	SIGNALS 
    |   SIGNALS_RED 
    |   EXSURES 
    |   EXSURES_RED
;


signals_only_clause 
	returns [PositionedString result = null] 
	throws SLTranslationException
:
    signals_only_keyword result=expression
;


signals_only_keyword
:
    	SIGNALS_ONLY 
    |   SIGNALS_ONLY_RED
;


diverges_clause 
	returns [PositionedString result = null]
	throws SLTranslationException
:
    diverges_keyword result=expression
;


diverges_keyword
:
    	DIVERGES 
    |   DIVERGES_RED
;


captures_clause throws SLTranslationException
{
    PositionedString ps;
}
:
    captures_keyword ps=expression
    {
    	raiseNotSupported("captures clauses");
    }   
;


captures_keyword
:
    	CAPTURES 
    |   CAPTURES_RED
;


name_clause 
	returns [PositionedString result = null]
	throws SLTranslationException
:
    spec:SPEC_NAME name:STRING_LITERAL SEMICOLON 
    {
	result=createPositionedString(name.getText(), spec);
    }    
;


when_clause throws SLTranslationException
{
    PositionedString ps;
}
:
    when_keyword ps=expression
    {
    	raiseNotSupported("when clauses");
    }
;


when_keyword
:
    	WHEN 
    |   WHEN_RED
;


working_space_clause throws SLTranslationException
{
    PositionedString ps;
}
:
    working_space_keyword ps=expression
    {
    	raiseNotSupported("working_space clauses");
    }
;


working_space_keyword
:
    	WORKING_SPACE 
    |   WORKING_SPACE_RED
;


duration_clause throws SLTranslationException
{
    PositionedString ps;
}
:
    duration_keyword ps=expression
    {
    	raiseNotSupported("duration clauses");
    }
;


duration_keyword
:
    	DURATION 
    |   DURATION_RED
;



//-----------------------------------------------------------------------------
//field declarations
//-----------------------------------------------------------------------------

field_declaration[ImmutableList<String> mods] 
	returns [ImmutableList<TextualJMLConstruct> result = null]
{
    StringBuffer sb = new StringBuffer();
    String s;
}
:
    type:IDENT 	      { sb.append(type.getText() + " "); } 
    name:IDENT 	      { sb.append(name.getText()); }
    (
    	    init:INITIALISER  { sb.append(init.getText()); }
    	|   semi:SEMICOLON    { sb.append(semi.getText()); }
    )
    {
        PositionedString ps = createPositionedString(sb.toString(), type);
    	TextualJMLFieldDecl fd = new TextualJMLFieldDecl(mods, ps);
    	result = ImmutableSLList.<TextualJMLConstruct>nil().prepend(fd);
    }
;




//-----------------------------------------------------------------------------
//method declarations
//-----------------------------------------------------------------------------

method_declaration[ImmutableList<String> mods] 
	returns [ImmutableList<TextualJMLConstruct> result = null]
{
    StringBuffer sb = new StringBuffer();
    String s;
}
:
    type:IDENT 	   	{ sb.append(type.getText() + " "); } 
    name:IDENT 	   	{ sb.append(name.getText()); }
    params:PARAM_LIST   { sb.append(params.getText()); }
    (
    	    body:BODY  	    { sb.append(body.getText()); }
    	|   semi:SEMICOLON  { sb.append(semi.getText()); }
    )
    {
        PositionedString ps = createPositionedString(sb.toString(), type);
    	TextualJMLMethodDecl md 
    		= new TextualJMLMethodDecl(mods, ps, name.getText());
    	result = ImmutableSLList.<TextualJMLConstruct>nil().prepend(md);
    }
;



//-----------------------------------------------------------------------------
//unsupported classlevel stuff
//-----------------------------------------------------------------------------

history_constraint[ImmutableList<String> mods] 
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException
{
    PositionedString ps;
} 
:
    constraint_keyword ps=expression
    {
    	raiseNotSupported("history constraints");
    	result = ImmutableSLList.<TextualJMLConstruct>nil();
    } 
;


constraint_keyword 
:
        CONSTRAINT 
    |   CONSTRAINT_RED
;


represents_clause[ImmutableList<String> mods] 
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException 
{
    PositionedString ps;
}
:
    represents_keyword ps=expression
    {
    	raiseNotSupported("represents clauses");
	result = ImmutableSLList.<TextualJMLConstruct>nil();
    }
;


represents_keyword
:
        REPRESENTS
    |   REPRESENTS_RED
;
    
    
initially_clause[ImmutableList<String> mods] 
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException 
{
    PositionedString ps;
} 
:
    INITIALLY ps=expression
    {
    	raiseNotSupported("initially clauses");
    	result = ImmutableSLList.<TextualJMLConstruct>nil();
    }
;
    

monitors_for_clause[ImmutableList<String> mods] 
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException 
{
    PositionedString ps;
} 
: 
    MONITORS_FOR ps=expression
    {
    	raiseNotSupported("monitors_for clauses");
    	result = ImmutableSLList.<TextualJMLConstruct>nil();    	
    }    
;
    

readable_if_clause[ImmutableList<String> mods] 
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException 
{
    PositionedString ps;
} 
:
    READABLE ps=expression
    {
    	raiseNotSupported("readable-if clauses");
    	result = ImmutableSLList.<TextualJMLConstruct>nil();    	
    }    
;


writable_if_clause[ImmutableList<String> mods] 
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException 
{
    PositionedString ps;
} 
:
    WRITABLE ps=expression
    {
    	raiseNotSupported("writable-if clauses");
    	result = ImmutableSLList.<TextualJMLConstruct>nil();    	
    }   
;


datagroup_clause[ImmutableList<String> mods] 
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException 
:
    in_group_clause | maps_into_clause
;


in_group_clause  throws SLTranslationException
{
    PositionedString ps;
} 
:
    in_keyword ps=expression
    {
    	raiseNotSupported("in-group clauses");
    } 
;


in_keyword 
:
	IN
    | 	IN_RED
;


maps_into_clause throws SLTranslationException
{
    PositionedString ps;
} 
:
    maps_keyword ps=expression
    {
    	raiseNotSupported("maps-into clauses");
    } 
;


maps_keyword 
:
    	MAPS 
    | 	MAPS_RED
;


nowarn_pragma[ImmutableList<String> mods] 
	returns [ImmutableList<TextualJMLConstruct> result = null]
	throws SLTranslationException
{
    PositionedString ps;
}
:
    NOWARN ps=expression
    {
    	raiseNotSupported("nowarn pragmas");
    	result = ImmutableSLList.<TextualJMLConstruct>nil();    	
    }
;



//-----------------------------------------------------------------------------
//set statements
//-----------------------------------------------------------------------------

set_statement[ImmutableList<String> mods] 
	returns [ImmutableList<TextualJMLConstruct> result = null]
{
    PositionedString ps;
} 
:
    SET ps=expression
    {
    	TextualJMLSetStatement ss = new TextualJMLSetStatement(mods, ps);
    	result = ImmutableSLList.<TextualJMLConstruct>nil().prepend(ss);
    }
;



//-----------------------------------------------------------------------------
//loop specifications
//-----------------------------------------------------------------------------

loop_specification[ImmutableList<String> mods] 
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException
{
   PositionedString ps;
   TextualJMLLoopSpec ls = new TextualJMLLoopSpec(mods);
   result = ImmutableSLList.<TextualJMLConstruct>nil().prepend(ls);
}
:
    (
    	options { greedy = true; }
    	:
    	    ps=loop_invariant       { ls.addInvariant(ps); }
    	|   ps=skolem_declaration   { ls.addSkolemDeclaration(ps); }
        |   ps=loop_predicates      { ls.addPredicates(ps); }
        |   ps=assignable_clause    { ls.addAssignable(ps); }
        |   ps=variant_function     { ls.setVariant(ps); } 
    )+
;


loop_invariant returns [PositionedString result = null]
:
    maintaining_keyword result=expression
;


maintaining_keyword 
:
        MAINTAINING
    |   MAINTAINING_REDUNDANTLY
    |   LOOP_INVARIANT
    |   LOOP_INVARIANT_REDUNDANTLY
;


skolem_declaration returns [PositionedString result = null]
:
    SKOLEM_CONSTANT result=expression
;


loop_predicates returns [PositionedString result = null]
:
    LOOP_PREDICATE result=expression
;


variant_function returns [PositionedString result = null]
: 
    decreasing_keyword result=expression
;


decreasing_keyword 
: 
        DECREASING
    |   DECREASING_REDUNDANTLY
    |   DECREASES
    |   DECREASES_REDUNDANTLY
;



//-----------------------------------------------------------------------------
//unsupported methodlevel stuff
//-----------------------------------------------------------------------------

assert_statement[ImmutableList<String> mods] 
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException
{
    PositionedString ps;
}
:
    assert_keyword ps=expression
    {
        raiseNotSupported("JML assert statements");
    	result = ImmutableSLList.<TextualJMLConstruct>nil();        
    } 
;


assert_keyword
:
	ASSERT
    |	ASSERT_REDUNDANTLY
;


assume_statement[ImmutableList<String> mods] 
	returns [ImmutableList<TextualJMLConstruct> result = null] 
	throws SLTranslationException
{
    PositionedString ps;
}
:
    assume_keyword ps=expression
    {
        raiseNotSupported("assume statements");
    	result = ImmutableSLList.<TextualJMLConstruct>nil();        
    } 
;


assume_keyword
:
	ASSUME
    |	ASSUME_REDUNDANTLY
;



//-----------------------------------------------------------------------------
//expressions
//-----------------------------------------------------------------------------


expression returns [PositionedString result = null]
{
    lexer.setExpressionMode(true);
    LT(1);
    lexer.setExpressionMode(false);
}
:
    t:EXPRESSION
    { 
    	result = createPositionedString(t.getText(), t);
    }
;
