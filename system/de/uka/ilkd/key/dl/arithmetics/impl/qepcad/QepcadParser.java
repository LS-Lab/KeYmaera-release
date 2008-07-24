// $ANTLR 3.0.1 /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g 2008-07-24 13:39:16

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


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class QepcadParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "EQ", "ADD", "SUB", "LB", "RB", "VAR", "NUM", "TRUE", "FALSE", "AND", "OR", "GE", "GT", "LT", "LE", "NE", "MUL", "DIV", "POW", "WS"
    };
    public static final int GE=15;
    public static final int LT=17;
    public static final int RB=8;
    public static final int SUB=6;
    public static final int AND=13;
    public static final int EOF=-1;
    public static final int MUL=20;
    public static final int TRUE=11;
    public static final int NUM=10;
    public static final int WS=23;
    public static final int POW=22;
    public static final int OR=14;
    public static final int GT=16;
    public static final int LB=7;
    public static final int VAR=9;
    public static final int DIV=21;
    public static final int EQ=4;
    public static final int FALSE=12;
    public static final int LE=18;
    public static final int ADD=5;
    public static final int NE=19;

        public QepcadParser(TokenStream input) {
            super(input);
        }
        

    public String[] getTokenNames() { return tokenNames; }
    public String getGrammarFileName() { return "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g"; }


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



    // $ANTLR start formula
    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:53:1: formula returns [Term t ] : st= expression EOF ;
    public final Term formula() throws RecognitionException {
        Term t = null;

        Term st = null;


        try {
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:53:26: (st= expression EOF )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:53:28: st= expression EOF
            {
            pushFollow(FOLLOW_expression_in_formula40);
            st=expression();
            _fsp--;

             t = st; 
            match(input,EOF,FOLLOW_EOF_in_formula44); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return t;
    }
    // $ANTLR end formula


    // $ANTLR start expression
    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:55:1: expression returns [ Term t ] : e= expr_add ( EQ f= expression )? ;
    public final Term expression() throws RecognitionException {
        Term t = null;

        Term e = null;

        Term f = null;


        try {
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:55:31: (e= expr_add ( EQ f= expression )? )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:55:34: e= expr_add ( EQ f= expression )?
            {
            pushFollow(FOLLOW_expr_add_in_expression61);
            e=expr_add();
            _fsp--;

            t = e; 
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:55:57: ( EQ f= expression )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==EQ) ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:55:59: EQ f= expression
                    {
                    match(input,EQ,FOLLOW_EQ_in_expression67); 
                    pushFollow(FOLLOW_expression_in_expression73);
                    f=expression();
                    _fsp--;

                    t = tb.equals(e,f);  

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return t;
    }
    // $ANTLR end expression


    // $ANTLR start expr_add
    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:57:1: expr_add returns [Term t ] : e= expr_sub ( ADD f= expr_add )? ;
    public final Term expr_add() throws RecognitionException {
        Term t = null;

        Term e = null;

        Term f = null;


        try {
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:57:29: (e= expr_sub ( ADD f= expr_add )? )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:57:31: e= expr_sub ( ADD f= expr_add )?
            {
            pushFollow(FOLLOW_expr_sub_in_expr_add96);
            e=expr_sub();
            _fsp--;

             t = e; 
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:57:55: ( ADD f= expr_add )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==ADD) ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:57:57: ADD f= expr_add
                    {
                    match(input,ADD,FOLLOW_ADD_in_expr_add102); 
                    pushFollow(FOLLOW_expr_add_in_expr_add108);
                    f=expr_add();
                    _fsp--;

                     t = tb.func((Function)nss.functions().lookup( new Name("add")),e,f); 

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return t;
    }
    // $ANTLR end expr_add


    // $ANTLR start expr_sub
    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:58:1: expr_sub returns [ Term t ] : e= atom ( SUB f= expr_add )? ;
    public final Term expr_sub() throws RecognitionException {
        Term t = null;

        Term e = null;

        Term f = null;


        try {
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:58:30: (e= atom ( SUB f= expr_add )? )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:58:32: e= atom ( SUB f= expr_add )?
            {
            pushFollow(FOLLOW_atom_in_expr_sub130);
            e=atom();
            _fsp--;

             t = e; 
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:58:52: ( SUB f= expr_add )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==SUB) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:58:54: SUB f= expr_add
                    {
                    match(input,SUB,FOLLOW_SUB_in_expr_sub136); 
                    pushFollow(FOLLOW_expr_add_in_expr_sub142);
                    f=expr_add();
                    _fsp--;

                      t = tb.func((Function)nss.functions().lookup( new Name("sub")),e,f);

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return t;
    }
    // $ANTLR end expr_sub


    // $ANTLR start atom
    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:59:1: atom returns [Term t ] : ( ( LB st= expression RB ) | (st= varOrNum ) );
    public final Term atom() throws RecognitionException {
        Term t = null;

        Term st = null;


        try {
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:59:25: ( ( LB st= expression RB ) | (st= varOrNum ) )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==LB) ) {
                alt4=1;
            }
            else if ( ((LA4_0>=VAR && LA4_0<=NUM)) ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("59:1: atom returns [Term t ] : ( ( LB st= expression RB ) | (st= varOrNum ) );", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:59:27: ( LB st= expression RB )
                    {
                    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:59:27: ( LB st= expression RB )
                    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:59:29: LB st= expression RB
                    {
                    match(input,LB,FOLLOW_LB_in_atom162); 
                    pushFollow(FOLLOW_expression_in_atom168);
                    st=expression();
                    _fsp--;

                    match(input,RB,FOLLOW_RB_in_atom170); 
                     t = st; 

                    }


                    }
                    break;
                case 2 :
                    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:60:8: (st= varOrNum )
                    {
                    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:60:8: (st= varOrNum )
                    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:60:10: st= varOrNum
                    {
                    pushFollow(FOLLOW_varOrNum_in_atom191);
                    st=varOrNum();
                    _fsp--;

                     t = st; 

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return t;
    }
    // $ANTLR end atom


    // $ANTLR start varOrNum
    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:62:1: varOrNum returns [ Term t ] : ( (st= variable ) | (st= number ) );
    public final Term varOrNum() throws RecognitionException {
        Term t = null;

        Term st = null;


        try {
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:62:29: ( (st= variable ) | (st= number ) )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==VAR) ) {
                alt5=1;
            }
            else if ( (LA5_0==NUM) ) {
                alt5=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("62:1: varOrNum returns [ Term t ] : ( (st= variable ) | (st= number ) );", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:62:32: (st= variable )
                    {
                    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:62:32: (st= variable )
                    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:62:33: st= variable
                    {
                    pushFollow(FOLLOW_variable_in_varOrNum214);
                    st=variable();
                    _fsp--;

                     t = st;

                    }


                    }
                    break;
                case 2 :
                    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:63:8: (st= number )
                    {
                    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:63:8: (st= number )
                    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:63:10: st= number
                    {
                    pushFollow(FOLLOW_number_in_varOrNum236);
                    st=number();
                    _fsp--;

                     t = st; 

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return t;
    }
    // $ANTLR end varOrNum


    // $ANTLR start variable
    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:65:1: variable returns [ Term t ] : s= VAR ;
    public final Term variable() throws RecognitionException {
        Term t = null;

        Token s=null;

        try {
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:65:31: (s= VAR )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:65:33: s= VAR
            {
            s=(Token)input.LT(1);
            match(input,VAR,FOLLOW_VAR_in_variable265); 
             t=getVariable(s.getText()); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return t;
    }
    // $ANTLR end variable


    // $ANTLR start number
    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:66:1: number returns [ Term t] : n= NUM ;
    public final Term number() throws RecognitionException {
        Term t = null;

        Token n=null;

        try {
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:66:27: (n= NUM )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:66:29: n= NUM
            {
            n=(Token)input.LT(1);
            match(input,NUM,FOLLOW_NUM_in_number283); 
             t = tb.func(NumberCache.getNumber(BigDecimal.valueOf(Integer.valueOf(n.getText())),RealLDT.getRealSort())); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            
        }
        return t;
    }
    // $ANTLR end number


 

    public static final BitSet FOLLOW_expression_in_formula40 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_formula44 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_add_in_expression61 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_EQ_in_expression67 = new BitSet(new long[]{0x0000000000000680L});
    public static final BitSet FOLLOW_expression_in_expression73 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_sub_in_expr_add96 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_ADD_in_expr_add102 = new BitSet(new long[]{0x0000000000000680L});
    public static final BitSet FOLLOW_expr_add_in_expr_add108 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_atom_in_expr_sub130 = new BitSet(new long[]{0x0000000000000042L});
    public static final BitSet FOLLOW_SUB_in_expr_sub136 = new BitSet(new long[]{0x0000000000000680L});
    public static final BitSet FOLLOW_expr_add_in_expr_sub142 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LB_in_atom162 = new BitSet(new long[]{0x0000000000000680L});
    public static final BitSet FOLLOW_expression_in_atom168 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_RB_in_atom170 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varOrNum_in_atom191 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_varOrNum214 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_number_in_varOrNum236 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VAR_in_variable265 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUM_in_number283 = new BitSet(new long[]{0x0000000000000002L});

}