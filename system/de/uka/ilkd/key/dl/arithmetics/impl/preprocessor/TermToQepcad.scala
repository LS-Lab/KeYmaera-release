package de.uka.ilkd.key.dl.arithmetics.impl.preprocessor

import java.math.BigInteger
import de.uka.ilkd.key.dl.arithmetics.impl.qepcad.QepCadInput
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.PolynomTool
import de.uka.ilkd.key.logic.NamespaceSet
import de.uka.ilkd.key.logic.Term
import de.uka.ilkd.key.logic.op.Function
import de.uka.ilkd.key.logic.op.Junctor
import de.uka.ilkd.key.logic.op.LogicVariable
import de.uka.ilkd.key.logic.op.Metavariable
import de.uka.ilkd.key.logic.op.Op
import de.uka.ilkd.key.logic.op.QuantifiableVariable
import de.uka.ilkd.key.logic.op.Quantifier
import java.util.ArrayList
import de.uka.ilkd.key.dl.arithmetics.impl.metitarski.OperatorMap

object TermToQepcad{
  
  def opMap(x:String) = OperatorMap.mapOps.get(x) match {
    case None => x
    case Some(s:List[String]) => s(OperatorMap.qepcad)
  }
  
  def convertToString(
      form	: Term, 
      nss	: NamespaceSet, 
      eliminateFractions: Boolean 
      ): String = form match {   
    
    
       /* Logical connectives */
      case t if(OperatorMap.isLogicalConnective(t.op().toString())) =>
      t.arity() match{
		  		    
        case 1 => { "[" +
          opMap(form.op().toString())+convertToString(form.sub(0),nss,true) + 
          "]" }
          
        case 2 => { 
          "[" + 
          convertToString(form.sub(0),nss,eliminateFractions) + 
          opMap(form.op().toString())  		+ 
          convertToString(form.sub(1),nss,eliminateFractions) +
          "]" }
        
      }   
      
      /* Quantifiers */
      case t if(OperatorMap.isQuantifier(t.op().toString())) =>
      {   
        val varsNum = t.varsBoundHere(0).size()
        var vars:Array[String] = Array();
        for(i <- 0 to varsNum){ 
          var name = t.varsBoundHere(0).get(i).name().toString()
				if (name.contains("_")) {
					name = name.replaceAll("_", "uscore");
				}
				if (name.contains("$")) {
					name = name.replaceAll("\\$", "dollar");
				}
				vars(i)=name
        }
        
        "[" +
          opMap(form.op().toString())+convertToString(form.sub(0),nss,true) + 
          "]"            
      }   
      
    /* Relational symbols {<,<=,=,/=,>=,>} */
    case t if(OperatorMap.isRelationalSymbol(t.op().toString())) => 
      
      t.arity() match{
        
        case n if(eliminateFractions) => { convertToString(
		  		    PolynomTool.eliminateFractionsFromInequality(form, nss),
		  		    nss,
		  		    false) }
		  		    
        case 1 => { opMap(form.op().toString())  + 
  		       convertToString(form.sub(0),nss,true)}
          
        case 2 => {
        	convertToString(form.sub(0),nss,true) + 
  		       opMap(form.op().toString())  + 
  		       convertToString(form.sub(1),nss,true)
  		 }                
      }   
      
      /* Arithmetic operators {+,-,^,/,*} */
      case t if(OperatorMap.isArithmeticOperator(t.op().toString())) =>
      t.arity() match{
		  		    
        case 1 => { "(" +
          opMap(form.op().toString())+convertToString(form.sub(0),nss,true) + 
          ")" }
          
        case 2 => { "(" + 
          convertToString(form.sub(0),nss,true) + 
          opMap(form.op().toString())  		+ 
          convertToString(form.sub(1),nss,true) +
          ")" }               
      }             
  }  
}