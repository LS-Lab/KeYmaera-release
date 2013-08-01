package edu.cmu.cs.ls.lyusimul

import edu.cmu.cs.ls._
import edu.cmu.cs.ls.lyusimul._
import edu.cmu.cs.ls.HP._
import scala.math.BigInt.int2bigInt
import com.wolfram.jlink.Expr


object Main {
  def math_sym(s: String): Expr =
    new Expr(Expr.SYMBOL, s)

  def math_int(s: String): Expr =
    new Expr(Expr.INTEGER, s)
  
  def math_str(s: String): Expr =
    new Expr(Expr.STRING, s)

  def un_fun(f: String, arg: Expr): Expr = {
    new Expr(math_sym(f),
             List(arg).toArray)
  }

  def bin_fun(f: String, arg1: Expr, arg2: Expr): Expr = {
    new Expr(math_sym(f),
             List(arg1,arg2).toArray)
  }
  
  def mul_arg_fun(f: String, lst: List[Expr]) : Expr = {
    new Expr(math_sym(f),
             lst.toArray)
  }
  
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
    
    
    
//    val hp = (Var("x") :=*) seq
//        (Var("y") :=*) seq
//        (Var("a") :=*) seq
//        (? ((Var("x") * Var("x") + Var("y") * Var("y") + Var("z") * Var("z") <= Num(Exact.Integer(1))))) seq
//        (?( Num(Exact.Integer(9)) * (Var("z") * (Var("z") * Var("z"))) eq Num(Exact.Integer(2)) * Var("x") - Num(Exact.Integer(5)) * Var("y") - Num(Exact.Integer(7)) ))
        
    
//    val hp = (Var("y") := Num(Exact.Integer(0))) seq
//    	(Var("v") := Num(Exact.Integer(0))) seq
//    	(Var("ac") := Num(Exact.Integer(0))) seq
//    	loop (
//    	    ? (Var("v") < Num(Exact.Integer(200))) seq
//    	    (
//    	        (
//    	            (Var("ac") := Num(Exact.Integer(1))) seq 
//    	            (Var("dummT") := Num(Exact.Integer(0))) seq
//    	            (Evolve(Var("dummT") <= Num(Exact.Integer(5)), (Var("y"), Var("v")), (Var("v"), Var("ac")), (Var("ac"), Num(Exact.Integer(0))),  (Var("dummT"), Num(Exact.Integer(1)))))
//    	        ) ++
//    	        (
//    	            (Var("ac") :=* ) seq 
//    	            (? ((Var("ac") > Num(Exact.Integer(5))) & (Var("ac") < Num(Exact.Integer(10))))) seq
//    	            (Var("dummT") := Num(Exact.Integer(0))) seq
//    	            (Evolve(Var("dummT") <= Num(Exact.Integer(5)), (Var("y"), Var("v")), (Var("v"), Var("ac")), (Var("ac"), Num(Exact.Integer(0))), (Var("dummT"), Num(Exact.Integer(1)))))
//    	    	)
//    	    )
//    	)
    
//    var mdlt = Modality(Box, hp, (Var("v") < Num(Exact.Integer(50))) & (Var("y") < Num(Exact.Integer(3000))))
//    val execStri = hpToExpr.modaToExpr(mdlt, List("glob`safety", "v", "y", "ac"), 3, 10000.0, 10, -10.0, 10.0).toString()
       
    
    var mdlt = hpToExpr.extractModality(OP.parseFormula(OP.openFile("/MyDocus/CMU 2013 Spring/Research - Andre Platzer/hps/2drobotcircular_passivesafety.key")))
    val execStri = hpToExpr.modaToExpr(mdlt, List("glob`safety", "xr", "yr", "vr", "y", "d", "dx", "dy", "v", "Ohm"), 3, 10000.0, 10, -10.0, 10.0).toString()

    
    println(execStri)
    val a = new MathematicaPlot(execStri)


    
//    val hp = (Var("y") := Num(Exact.Integer(0))) seq
//    	(Var("v") := Num(Exact.Integer(0))) seq
//    	(Var("ac") := Num(Exact.Integer(0))) seq
//    	loop (
//    	    ? (Var("v") < Num(Exact.Integer(200))) seq
//    	    (
//    	        (
//    	            (Var("ac") := Num(Exact.Integer(1))) seq 
//    	            (Var("dummT") := Num(Exact.Integer(0))) seq
//    	            (Evolve(Var("dummT") <= Num(Exact.Integer(5)), (Var("y"), Var("v")), (Var("v"), Var("ac")), (Var("ac"), Num(Exact.Integer(0))),  (Var("dummT"), Num(Exact.Integer(1)))))
//    	        ) ++
//    	        (
//    	            (Var("ac") :=* ) seq 
//    	            (? ((Var("ac") > Num(Exact.Integer(5))) & (Var("ac") < Num(Exact.Integer(10))))) seq
//    	            (Var("dummT") := Num(Exact.Integer(0))) seq
//    	            (Evolve(Var("dummT") <= Num(Exact.Integer(5)), (Var("y"), Var("v")), (Var("v"), Var("ac")), (Var("ac"), Num(Exact.Integer(0))), (Var("dummT"), Num(Exact.Integer(1)))))
//    	    	)
//    	    )
//    	)
    
    


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
    
    
    
    // val a = new MathematicaPlot("Module[List[y, v, ac, dummT], CompoundExpression[ Set[glob`tendLimi, 100], Set[glob`tends, {0}], Set[glob`sol, {}], Set[curr`y, 0], Set[curr`v, 0], Set[curr`ac, 0], Set[curr`ac, 1], Set[curr`dummT, 0], Set[ glob`sol, Append[ glob`sol, NDSolve[ { Equal[Derivative[1][y][loca`t], v[loca`t]], Equal[Derivative[1][v][loca`t], ac[loca`t]], Equal[Derivative[1][ac][loca`t], 0], Equal[Derivative[1][dummT][loca`t], 1], y[Evaluate[Last[glob`tends]]] == curr`y, v[Evaluate[Last[glob`tends]]] == curr`v, ac[Evaluate[Last[glob`tends]]] == curr`ac, dummT[Evaluate[Last[glob`tends]]] == curr`dummT, WhenEvent[ GreaterEqual[dummT[loca`t], 5], CompoundExpression[ Set[glob`tends, Append[glob`tends, loca`t]], \"StopIntegration\" ] ] }, {y, v, ac, dummT}, {loca`t, Evaluate[Last[glob`tends]], glob`tendLimi} ] ] ], Set[ curr`y, First[ Evaluate[ (* y[Evaluate[Last[glob`tends]]] /. Last[glob`sol] *) ReplaceAll[ y[ Evaluate[Last[glob`tends]] ], Last[glob`sol] ] ] ] ], Set[ curr`v, Evaluate[v[Evaluate[Last[glob`tends]]] /. Last[glob`sol]][[1]] ], Set[ curr`ac, Evaluate[ac[Evaluate[Last[glob`tends]]] /. Last[glob`sol]][[1]] ], Set[ curr`dummT, Evaluate[dummT[Evaluate[Last[glob`tends]]] /. Last[glob`sol]][[1]] ], Set[ curr`ac, RandomReal[ List[6, 11] ] ], If[ ((5 < curr`ac) && (curr`ac < 10)), CompoundExpression[ Set[curr`dummT, 0], Set[glob`sol, Append[glob`sol, NDSolve[ { Equal[Derivative[1][y][loca`t], v[loca`t]], Equal[Derivative[1][v][loca`t], ac[loca`t]], Equal[Derivative[1][ac][loca`t], 0], Equal[Derivative[1][dummT][loca`t], 1], y[Evaluate[Last[glob`tends]]] == curr`y, v[Evaluate[Last[glob`tends]]] == curr`v, ac[Evaluate[Last[glob`tends]]] == curr`ac, dummT[Evaluate[Last[glob`tends]]] == curr`dummT, WhenEvent[ GreaterEqual[dummT[loca`t], 5], CompoundExpression[ Set[glob`tends, Append[glob`tends, loca`t]], \"StopIntegration\" ] ] }, {y, v, ac, dummT}, {loca`t, Evaluate[Last[glob`tends]], glob`tendLimi} ] ] ] ], Set[local`nop, 0] ], Set[glob`safety[loca`t_] , 100 - v[loca`t]], GraphicsGrid[ List[ List[ Plot[ Piecewise[ Table[ { Evaluate[ ReplaceAll[glob`safety[loca`t], Part[glob`sol, loca`i]] ], And[ GreaterEqual[loca`t, Part[glob`tends, loca`i]], Less[loca`t, Part[glob`tends, Plus[loca`i, 1]]] ] }, { loca`i, 1, Plus[Length[glob`tends], -1] } ] ], { loca`t, Evaluate[First[glob`tends]], Evaluate[Last[glob`tends]] }, Rule[AxesLabel, List[Style[t, 20], Style[\"safety\", 20]]] ], Plot[ Piecewise[ Table[ { Evaluate[ ReplaceAll[y[loca`t], Part[glob`sol, loca`i]] ], And[ GreaterEqual[loca`t, Part[glob`tends, loca`i]], Less[loca`t, Part[glob`tends, Plus[loca`i, 1]]] ] }, { loca`i, 1, Plus[Length[glob`tends], -1] } ] ], { loca`t, Evaluate[First[glob`tends]], Evaluate[Last[glob`tends]] }, Rule[AxesLabel, List[Style[t, 20], Style[\"y\", 20]]] ], Plot[ Piecewise[ Table[ { Evaluate[ ReplaceAll[v[loca`t], Part[glob`sol, loca`i]] ], And[ GreaterEqual[loca`t, Part[glob`tends, loca`i]], Less[loca`t, Part[glob`tends, Plus[loca`i, 1]]] ] }, { loca`i, 1, Plus[Length[glob`tends], -1] } ] ], { loca`t, Evaluate[First[glob`tends]], Evaluate[Last[glob`tends]] }, Rule[AxesLabel, List[Style[t, 20], Style[\"v\", 20]]] ], Plot[ Piecewise[ Table[ { Evaluate[ ReplaceAll[ac[loca`t], Part[glob`sol, loca`i]] ], And[ GreaterEqual[loca`t, Part[glob`tends, loca`i]], Less[loca`t, Part[glob`tends, Plus[loca`i, 1]]] ] }, { loca`i, 1, Plus[Length[glob`tends], -1] } ] ], { loca`t, Evaluate[First[glob`tends]], Evaluate[Last[glob`tends]] }, Rule[AxesLabel, List[Style[t, 20], Style[\"ac\", 20]]] ] ] ] ] ] ]")
    
//    val myTemp : Expr = bin_fun("hey", math_str("yo"), math_sym("dude"))
//    println(myTemp.toString())
    
//    val myList : List[String] = List("aa", "bb", "cc", "aa", "dd", "ee", "bb", "aa")
//    println(myList.distinct) 
  }
}
