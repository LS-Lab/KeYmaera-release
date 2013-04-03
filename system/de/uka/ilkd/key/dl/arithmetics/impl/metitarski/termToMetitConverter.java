/************************************************************************
 *  Formula converter from KeYmaera syntax to infix TPTP (part of the 
 *  MetiTarski-KeYmera interface).
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

package de.uka.ilkd.key.dl.arithmetics.impl.metitarski;

import java.util.HashSet;
import org.apache.log4j.Logger;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.dl.arithmetics.impl.preprocessor.FormulaTree;

/**
 * This class provides functionality to transform a KeYmaera Term into a mutable binary tree
 * representation, and provides tree restructuring methods needed to create a valid infix
 * TPTP problem for MetiTarski. Some of the methods implemented in this class are, in fact, 
 * optional (e.g. {@link #collapseQuantifiers(Tree)}, but result in a cleaner TPTP problem
 * statement.
 * 
 * @author s0805753@sms.ed.ac.uk.
 * @since 12/02/2012
 */

public class termToMetitConverter {

   private static Logger logger = Logger.getLogger("MetiTarski Converter Logger");
   
   public static String termToMetit(Term form, String name, String comments) {

      /* Convert immutable formula into a mutable tree representation      */
      FormulaTree formulaTree = new FormulaTree(form);
      
      
      String[] problem = treeToMetit(formulaTree);

      return formatProblem(comments, name, problem);
   }
   
   /**
    * Compiles an infix TPTP problem given a KeY Term formula and a String label
    * @return String formula in infix TPTP.
    */
   public  String termToMetit(Term form, String name){
      return termToMetit(form,name,"Auto-generated MetiTarski problem");
   }
   
   public static String termToMetit(Term form, boolean findInstance){
      return termToMetit(  form,
                           "Problem" + System.currentTimeMillis(), 
                           "Auto-generated MetiTarski problem");
   }

   public static String[] treeToMetit(FormulaTree formulaTree){
      
      return new String[]{ 
    		  formulaTree.metitSyntax(), 
    		  Integer.toString(formulaTree.numberOfVars())
    		  };
   }

   
   static String formatProblem(String comments, String name, String[] problem){
      StringBuilder output = new StringBuilder();
      
      for(String line: comments.trim().split("\n")){
         output.append("% "+ line + "\n");
      }

      output.append( "% Number of variables: " 
                     + problem[1] 
                     + "\n"  
                     + "fof(" 
                     + name 
                     + ",conjecture, " 
                     + problem[0] 
                     + ").\n" );

      return output.toString();
   }
}