package edu.cmu.cs.ls.lyusimul

import edu.cmu.cs.ls._
import edu.cmu.cs.ls.lyusimul._
import edu.cmu.cs.ls.HP._
import scala.math.BigInt.int2bigInt
import com.wolfram.jlink.Expr


object Main {
  def main(args: Array[String]) {
    
//y := 0;
//v := 0;
//ac := 0;
//(
//	?v < 50;
//	(
//		(ac := 1; dummT := 0; {y' = v, v' = ac, ac' = 0, dummT' = 1, dummT <= 5} )
//		++ (ac := *; ?5 < ac < 10; dummT := 0; {y' = v, v' = ac, ac' = 0, dummT' = 1, dummT <= 5})
//	)
//)*
    
    
    val hp = (Var("y") := Num(Exact.Integer(0))) seq
    	(Var("v") := Num(Exact.Integer(0))) seq
    	(Var("ac") := Num(Exact.Integer(0))) seq
    	loop (
    	    ? (Var("v") < Num(Exact.Integer(200))) seq
    	    (
    	        (
    	            (Var("ac") := Num(Exact.Integer(1))) seq 
    	            (Var("dummT") := Num(Exact.Integer(0))) seq
    	            (Evolve(Var("dummT") <= Num(Exact.Integer(5)), (Var("y"), Var("v")), (Var("v"), Var("ac")), (Var("ac"), Num(Exact.Integer(0))),  (Var("dummT"), Num(Exact.Integer(1)))))
    	        ) ++
    	        (
    	            (Var("ac") :=* ) seq 
    	            (? ((Var("ac") > Num(Exact.Integer(5))) & (Var("ac") < Num(Exact.Integer(10))))) seq
    	            (Var("dummT") := Num(Exact.Integer(0))) seq
    	            (Evolve(Var("dummT") <= Num(Exact.Integer(5)), (Var("y"), Var("v")), (Var("v"), Var("ac")), (Var("ac"), Num(Exact.Integer(0))), (Var("dummT"), Num(Exact.Integer(1)))))
    	    	)
    	    )
    	)
    
//     val hp = (Var("y") := Num(Exact.Integer(0))) seq
//    	(Var("v") := Num(Exact.Integer(0))) seq
//    	(Var("ac") := Num(Exact.Integer(0))) seq
//    	loop (
//    	    (
//    	        (
//    	            (Var("ac") := Num(Exact.Integer(1))) seq 
//    	            (Var("dummT") := Num(Exact.Integer(0))) seq
//    	            (Evolve(Var("dummT") <= Num(Exact.Integer(5)), (Var("y"), Var("v")), (Var("v"), Var("ac")), (Var("ac"), Num(Exact.Integer(0))), (Var("dummT"), Num(Exact.Integer(1)))))
//    	        ) ++
//    	        (
//    	            (Var("ac") :=* ) seq 
//    	            (Var("dummT") := Num(Exact.Integer(0))) seq
//    	            (Evolve(Var("dummT") <= Num(Exact.Integer(5)), (Var("y"), Var("v")), (Var("v"), Var("ac")), (Var("ac"), Num(Exact.Integer(0))), (Var("dummT"), Num(Exact.Integer(1)))))
//    	    	)
//    	    )
//    	)

    
    val mdlt = Modality(BoxModality, hp, (Var("v") < Num(Exact.Integer(50))))
    
    
    // println(hpToExpr.varisToDispToPlotComms(List("v")).toString())
    
    // hayqueHacer: plot it
        
//    val plotStri1 = vaniEvolToPlotStri(evolve1, 100);
//    println(plotStri1)    
//    val ddd = new MathematicaPlot(plotStri1)

    
    // HP.loop((Var("a"):=*) seq (Var("s") := Num(Exact.Integer(0))) seq evolve1)    
    
    
//        val myFrml = ((Var("y")>Var("x")) & (Var("x") > Num(Exact.Integer(8))))
//    val myMmt = MmtManipulation.formulaToMmt(myFrml)
//    val myExpr = MmtManipulation.mmtToExpr(myMmt)
//    val safeCondStri = ExprToPlottableString.exprToPlottableString(myExpr)
//    
//    val evolve1 = Evolve(Var("dummT") < Num(Exact.Integer(11)), (Var("x"), Var("v") * Var("dx")), (Var("y"), Var("v") * Var("dy")), (Var("v"), Var("ac")),
//        (Var("ac"), Num(Exact.Integer(0))), (Var("r"), Num(Exact.Integer(0))), (Var("om"), Var("ac") / Var("r")),
//        (Var("dx"), (Arithmetic(Negate, Var("om"))) * Var("dy")), (Var("dy"), Var("om") * Var("dx")), (Var("dummT"), Num(Exact.Integer(1))))
//
//    
//    val evolve2 = Evolve(Var("dummT") < Num(Exact.Integer(7)), (Var("x"), Var("v") * Var("dx")), (Var("y"), Var("v") * Var("dy")), (Var("v"), Var("ac")),
//        (Var("ac"), Num(Exact.Integer(3))), (Var("r"), Num(Exact.Integer(0))), (Var("om"), Var("ac") / Var("r")),
//        (Var("dx"), (Arithmetic(Negate, Var("om"))) * Var("dy")), (Var("dy"), Var("om") * Var("dx")), (Var("dummT"), Num(Exact.Integer(1))))
//
//  
//        
//    val plotStri1 = seqEvolRootToPlotStri(evolve1 seq evolve2 seq evolve1, 100);
//    println(plotStri1)    
//    val ddd = new MathematicaPlot(plotStri1)
    
    val execStri = hpToExpr.modaToExpr(mdlt, List("v"), 1000.0, 90, 4.0, 11.0).toString() 
    println(execStri)
    val a = new MathematicaPlot(execStri)
    
  }
}
