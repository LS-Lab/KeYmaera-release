/************************************************************************
 *  KeYmaera-MetiTarski interface.
 *  Copyright (C) 2012  s0805753@sms.ed.ac.uk University of Edinburgh.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 *  
 ************************************************************************/

package de.uka.ilkd.key.dl.arithmetics.impl.preprocessor;

/**
 * Enum type for translating KeYmaera operators to back-end solver syntax.
 * <p>
 * N.B. Here we set the Enum constant names to match those of KeYmaera
 * operators, this allows us to perform translation in constant time.
 * </p>
 * <p>
 * N.B. The special function operators for KeY are in fact those returned by
 * <i>Mathematica™</i>.</p>
 * 
 * @author s0805753@sms.ed.ac.uk
 * @since 12/02/2012
 */
public enum Operators {

   not      (  "not"    ,  " ¬ "    ,  "not"    ,  "~"      )  ,
   and      (  "and"    ,  " ∧ "    ,  "/\\"    ,  "&"      )  ,
   or       (  "or"     ,  " ∨ "    ,  "\\/"    ,  "|"      )  ,
   imp      (  "imp"    ,  " → "    ,  "imp"    ,  "=>"     )  ,
   equiv    (  "equiv"  ,  " ↔ "    ,  "equiv"  ,  "equiv"  )  ,
   all      (  "all"    ,  "∀ "     ,  "all"    ,  "!"      )  ,
   exist    (  "exist"  ,  "∃ "     ,  "exist"  ,  "?"      )  ,

   neg      (  "neg"    ,  "-"      ,  "-"      ,  "-"      )  ,
   exp      (  "exp"    ,  "^"      ,  "^"      ,  "^"      )  ,
   mul      (  "mul"    ,  "·"      ,  "*"      ,  "*"      )  ,
   div      (  "div"    ,  "÷"      ,  "/"      ,  "/"      )  ,
   add      (  "add"    ,  "+"      ,  "+"      ,  "+"      )  ,
   sub      (  "sub"    ,  "-"      ,  "-"      ,  "-"      )  ,
      
   equals   (  "equals" ,  "="      ,  "="      ,  "="      )  ,  
   geq      (  "geq"    ,  "≥"      ,  ">="     ,  ">="     )  ,
   neq      (  "neq"    ,  "≠"      ,  "!="     ,  "!="     )  ,
   leq      (  "leq"    ,  "≤"      ,  "<="     ,  "<="     )  ,
   lt       (  "lt"     ,  "<"      ,  "<"      ,  "<"      )  ,
   gt       (  "gt"     ,  ">"      ,  ">"      ,  ">"      )  ,
         
   Log      (  "Log"    ,  "ln"                             )  ,
   Exp      (  "Exp"    ,  "exp"                            )  ,

   Sin      (  "Sin"    ,  "sin"                            )  ,
   Cos      (  "Cos"    ,  "cos"                            )  ,
   Tan      (  "Tan"    ,  "tan"                            )  ,
   
   Sinh     (  "Sinh"   ,  "sinh"                           )  ,
   Cosh     (  "Cosh"   ,  "cosh"                           )  ,
   Tanh     (  "Tanh"   ,  "tanh"                           )  ,

   ArcSin   (  "ArcSin" ,  "asin"                           )  ,
   ArcCos   (  "ArcCos" ,  "acos"                           )  ,
   ArcTan   (  "ArcTan" ,  "atan"                           )  ,
   
   VERUM    (  "true"   ,  "⊤"      ,  "VERUM"  ,  "$true"  )  ,
   FALSUM   (  "false"  ,  "⊥"      ,  "FALSUM" ,  "$false" )  ,
   
   Sqrt     (  "Sqrt"   ,  "√"      ,  "sqrt"   ,  "sqrt"   )  ,
   Cbrt     (  "Cbrt"   ,  "∛"      ,  "cbrt"   ,  "cbrt"   )  ;

   public String KeY, utf, Rahd, Tptp;

   Operators(String KeY, String utf, String Rahd, String Tptp) {
      this. KeY   =  KeY;
      this. utf   =  utf;
      this. Rahd  =  Rahd;
      this. Tptp  =  Tptp;       
   }

   Operators(String KeY, String Tptp) {
      this. KeY   =  KeY;
      this. utf   =  KeY;
      this. Rahd  =  KeY;
      this. Tptp  =  Tptp;
   } 

   public Operators negatePredicate(){
      return Operators.negatePredicate(this);
   }

   private static Operators negatePredicate(Operators operator) {
      switch(operator)
      {
         case geq    :  return lt;
         case leq    :  return gt;
         case lt     :  return geq;
         case gt     :  return leq;
         case equals :  return neq;
         case neq    :  return equals;
         default     :  return operator;
      }
   }         
}