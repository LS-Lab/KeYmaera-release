// $ANTLR 3.0.1 /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g 2008-07-24 13:39:16

	package de.uka.ilkd.key.dl.arithmetics.impl.qepcad;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class QepcadLexer extends Lexer {
    public static final int GE=15;
    public static final int LT=17;
    public static final int RB=8;
    public static final int SUB=6;
    public static final int AND=13;
    public static final int Tokens=24;
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
    public static final int NE=19;
    public static final int ADD=5;
    public QepcadLexer() {;} 
    public QepcadLexer(CharStream input) {
        super(input);
    }
    public String getGrammarFileName() { return "/home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g"; }

    // $ANTLR start TRUE
    public final void mTRUE() throws RecognitionException {
        try {
            int _type = TRUE;
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:70:6: ( 'TRUE' )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:70:8: 'TRUE'
            {
            match("TRUE"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end TRUE

    // $ANTLR start FALSE
    public final void mFALSE() throws RecognitionException {
        try {
            int _type = FALSE;
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:71:6: ( 'FALSE' )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:71:8: 'FALSE'
            {
            match("FALSE"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end FALSE

    // $ANTLR start AND
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:73:5: ( '/\\\\' )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:73:7: '/\\\\'
            {
            match("/\\"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end AND

    // $ANTLR start OR
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:74:4: ( '\\\\/' )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:74:6: '\\\\/'
            {
            match("\\/"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end OR

    // $ANTLR start GE
    public final void mGE() throws RecognitionException {
        try {
            int _type = GE;
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:76:4: ( '>=' )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:76:6: '>='
            {
            match(">="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end GE

    // $ANTLR start GT
    public final void mGT() throws RecognitionException {
        try {
            int _type = GT;
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:77:4: ( '>' )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:77:6: '>'
            {
            match('>'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end GT

    // $ANTLR start LT
    public final void mLT() throws RecognitionException {
        try {
            int _type = LT;
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:78:4: ( '<' )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:78:6: '<'
            {
            match('<'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LT

    // $ANTLR start LE
    public final void mLE() throws RecognitionException {
        try {
            int _type = LE;
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:79:4: ( '<=' )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:79:6: '<='
            {
            match("<="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LE

    // $ANTLR start EQ
    public final void mEQ() throws RecognitionException {
        try {
            int _type = EQ;
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:80:4: ( '=' )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:80:6: '='
            {
            match('='); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end EQ

    // $ANTLR start NE
    public final void mNE() throws RecognitionException {
        try {
            int _type = NE;
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:81:4: ( '/=' )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:81:6: '/='
            {
            match("/="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end NE

    // $ANTLR start ADD
    public final void mADD() throws RecognitionException {
        try {
            int _type = ADD;
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:83:5: ( '+' )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:83:7: '+'
            {
            match('+'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ADD

    // $ANTLR start SUB
    public final void mSUB() throws RecognitionException {
        try {
            int _type = SUB;
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:84:5: ( '-' )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:84:7: '-'
            {
            match('-'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SUB

    // $ANTLR start MUL
    public final void mMUL() throws RecognitionException {
        try {
            int _type = MUL;
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:85:5: ( '*' )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:85:7: '*'
            {
            match('*'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end MUL

    // $ANTLR start DIV
    public final void mDIV() throws RecognitionException {
        try {
            int _type = DIV;
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:86:5: ( '/' )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:86:7: '/'
            {
            match('/'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end DIV

    // $ANTLR start LB
    public final void mLB() throws RecognitionException {
        try {
            int _type = LB;
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:88:5: ( '[' )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:88:8: '['
            {
            match('['); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LB

    // $ANTLR start RB
    public final void mRB() throws RecognitionException {
        try {
            int _type = RB;
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:89:4: ( ']' )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:89:6: ']'
            {
            match(']'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RB

    // $ANTLR start POW
    public final void mPOW() throws RecognitionException {
        try {
            int _type = POW;
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:91:5: ( '^' )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:91:7: '^'
            {
            match('^'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end POW

    // $ANTLR start WS
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:93:5: ( ( '\\t' | ' ' | '\\r' | '\\n' | '\\u000C' )+ )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:93:9: ( '\\t' | ' ' | '\\r' | '\\n' | '\\u000C' )+
            {
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:93:9: ( '\\t' | ' ' | '\\r' | '\\n' | '\\u000C' )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='\t' && LA1_0<='\n')||(LA1_0>='\f' && LA1_0<='\r')||LA1_0==' ') ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt1 >= 1 ) break loop1;
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);

             channel = HIDDEN; 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end WS

    // $ANTLR start NUM
    public final void mNUM() throws RecognitionException {
        try {
            int _type = NUM;
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:94:6: ( ( '0' .. '9' )* ( '0' .. '9' | ( '.' ( '0' .. '9' )+ ) ) )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:94:11: ( '0' .. '9' )* ( '0' .. '9' | ( '.' ( '0' .. '9' )+ ) )
            {
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:94:11: ( '0' .. '9' )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>='0' && LA2_0<='9')) ) {
                    int LA2_1 = input.LA(2);

                    if ( (LA2_1=='.'||(LA2_1>='0' && LA2_1<='9')) ) {
                        alt2=1;
                    }


                }


                switch (alt2) {
            	case 1 :
            	    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:94:11: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:94:21: ( '0' .. '9' | ( '.' ( '0' .. '9' )+ ) )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( ((LA4_0>='0' && LA4_0<='9')) ) {
                alt4=1;
            }
            else if ( (LA4_0=='.') ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("94:21: ( '0' .. '9' | ( '.' ( '0' .. '9' )+ ) )", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:94:22: '0' .. '9'
                    {
                    matchRange('0','9'); 

                    }
                    break;
                case 2 :
                    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:94:32: ( '.' ( '0' .. '9' )+ )
                    {
                    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:94:32: ( '.' ( '0' .. '9' )+ )
                    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:94:33: '.' ( '0' .. '9' )+
                    {
                    match('.'); 
                    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:94:37: ( '0' .. '9' )+
                    int cnt3=0;
                    loop3:
                    do {
                        int alt3=2;
                        int LA3_0 = input.LA(1);

                        if ( ((LA3_0>='0' && LA3_0<='9')) ) {
                            alt3=1;
                        }


                        switch (alt3) {
                    	case 1 :
                    	    // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:94:37: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt3 >= 1 ) break loop3;
                                EarlyExitException eee =
                                    new EarlyExitException(3, input);
                                throw eee;
                        }
                        cnt3++;
                    } while (true);


                    }


                    }
                    break;

            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end NUM

    // $ANTLR start VAR
    public final void mVAR() throws RecognitionException {
        try {
            int _type = VAR;
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:95:5: ( ( 'a' .. 'z' | 'A' .. 'Z' ) )
            // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:95:7: ( 'a' .. 'z' | 'A' .. 'Z' )
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end VAR

    public void mTokens() throws RecognitionException {
        // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:1:8: ( TRUE | FALSE | AND | OR | GE | GT | LT | LE | EQ | NE | ADD | SUB | MUL | DIV | LB | RB | POW | WS | NUM | VAR )
        int alt5=20;
        switch ( input.LA(1) ) {
        case 'T':
            {
            int LA5_1 = input.LA(2);

            if ( (LA5_1=='R') ) {
                alt5=1;
            }
            else {
                alt5=20;}
            }
            break;
        case 'F':
            {
            int LA5_2 = input.LA(2);

            if ( (LA5_2=='A') ) {
                alt5=2;
            }
            else {
                alt5=20;}
            }
            break;
        case '/':
            {
            switch ( input.LA(2) ) {
            case '\\':
                {
                alt5=3;
                }
                break;
            case '=':
                {
                alt5=10;
                }
                break;
            default:
                alt5=14;}

            }
            break;
        case '\\':
            {
            alt5=4;
            }
            break;
        case '>':
            {
            int LA5_5 = input.LA(2);

            if ( (LA5_5=='=') ) {
                alt5=5;
            }
            else {
                alt5=6;}
            }
            break;
        case '<':
            {
            int LA5_6 = input.LA(2);

            if ( (LA5_6=='=') ) {
                alt5=8;
            }
            else {
                alt5=7;}
            }
            break;
        case '=':
            {
            alt5=9;
            }
            break;
        case '+':
            {
            alt5=11;
            }
            break;
        case '-':
            {
            alt5=12;
            }
            break;
        case '*':
            {
            alt5=13;
            }
            break;
        case '[':
            {
            alt5=15;
            }
            break;
        case ']':
            {
            alt5=16;
            }
            break;
        case '^':
            {
            alt5=17;
            }
            break;
        case '\t':
        case '\n':
        case '\f':
        case '\r':
        case ' ':
            {
            alt5=18;
            }
            break;
        case '.':
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            {
            alt5=19;
            }
            break;
        case 'A':
        case 'B':
        case 'C':
        case 'D':
        case 'E':
        case 'G':
        case 'H':
        case 'I':
        case 'J':
        case 'K':
        case 'L':
        case 'M':
        case 'N':
        case 'O':
        case 'P':
        case 'Q':
        case 'R':
        case 'S':
        case 'U':
        case 'V':
        case 'W':
        case 'X':
        case 'Y':
        case 'Z':
        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':
        case 'g':
        case 'h':
        case 'i':
        case 'j':
        case 'k':
        case 'l':
        case 'm':
        case 'n':
        case 'o':
        case 'p':
        case 'q':
        case 'r':
        case 's':
        case 't':
        case 'u':
        case 'v':
        case 'w':
        case 'x':
        case 'y':
        case 'z':
            {
            alt5=20;
            }
            break;
        default:
            NoViableAltException nvae =
                new NoViableAltException("1:1: Tokens : ( TRUE | FALSE | AND | OR | GE | GT | LT | LE | EQ | NE | ADD | SUB | MUL | DIV | LB | RB | POW | WS | NUM | VAR );", 5, 0, input);

            throw nvae;
        }

        switch (alt5) {
            case 1 :
                // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:1:10: TRUE
                {
                mTRUE(); 

                }
                break;
            case 2 :
                // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:1:15: FALSE
                {
                mFALSE(); 

                }
                break;
            case 3 :
                // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:1:21: AND
                {
                mAND(); 

                }
                break;
            case 4 :
                // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:1:25: OR
                {
                mOR(); 

                }
                break;
            case 5 :
                // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:1:28: GE
                {
                mGE(); 

                }
                break;
            case 6 :
                // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:1:31: GT
                {
                mGT(); 

                }
                break;
            case 7 :
                // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:1:34: LT
                {
                mLT(); 

                }
                break;
            case 8 :
                // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:1:37: LE
                {
                mLE(); 

                }
                break;
            case 9 :
                // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:1:40: EQ
                {
                mEQ(); 

                }
                break;
            case 10 :
                // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:1:43: NE
                {
                mNE(); 

                }
                break;
            case 11 :
                // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:1:46: ADD
                {
                mADD(); 

                }
                break;
            case 12 :
                // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:1:50: SUB
                {
                mSUB(); 

                }
                break;
            case 13 :
                // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:1:54: MUL
                {
                mMUL(); 

                }
                break;
            case 14 :
                // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:1:58: DIV
                {
                mDIV(); 

                }
                break;
            case 15 :
                // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:1:62: LB
                {
                mLB(); 

                }
                break;
            case 16 :
                // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:1:65: RB
                {
                mRB(); 

                }
                break;
            case 17 :
                // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:1:68: POW
                {
                mPOW(); 

                }
                break;
            case 18 :
                // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:1:72: WS
                {
                mWS(); 

                }
                break;
            case 19 :
                // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:1:75: NUM
                {
                mNUM(); 

                }
                break;
            case 20 :
                // /home/boomer/keymaera/system/de/uka/ilkd/key/dl/arithmetics/impl/qepcad/Qepcad.g:1:79: VAR
                {
                mVAR(); 

                }
                break;

        }

    }


 

}