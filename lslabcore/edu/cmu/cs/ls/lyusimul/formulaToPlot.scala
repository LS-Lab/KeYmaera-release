package edu.cmu.cs.ls.lyusimul

import edu.cmu.cs.ls._
import edu.cmu.cs.ls.lyusimul._
import edu.cmu.cs.ls.HP._
import scala.math.BigInt.int2bigInt
import com.wolfram.jlink.Expr
// import ExprToPlottableString
// import scala.math.BigInt.int2bigInt

object FormulaToPlot {



  def math_sym(s: String): Expr =
    new Expr(Expr.SYMBOL, s)

  def math_int(s: String): Expr =
    new Expr(Expr.INTEGER, s)

  def un_fun(f: String, arg: Expr): Expr = {
    new Expr(math_sym(f),
             List(arg).toArray)
  }

  def bin_fun(f: String, arg1: Expr, arg2: Expr): Expr = {
    new Expr(math_sym(f),
             List(arg1,arg2).toArray)
  }

  // Deprecated for use of string
//  def vaniEvolToPlotStri(evolve: Evolve, tendLimi: Int) : String = {
//	val dummStri1 = "Set[s, NDSolve[{"
//    val dummStri2 = ", x[0] == 0, y[0] == 0, v[0] == 0, r[0] == 1, om[0] == v[0]/r[0], dx[0] == 1, dy[0] == 0, ac[0] == 1, dummT[0] == 0,"
//    val dummStri3 = "}, {x, y, v, ac, om, dx, dy, r, dummT}, {t, 0, global`tendLimi}]]; \n Plot[Evaluate[x[t] /. s], {t, 0, global`tend}]"
//      
//    val tendLimiMathStri = EvolveToExpr.tendLimiToSetExpr(tendLimi).toString() + ";\n"
//        
//    val odesListFromEvolve = EvolveToExpr.hpToOdesList(evolve)
//    val whenEventsFromEvolve = EvolveToExpr.hpToWhenEvents(evolve, 9.0)
//    
//    val odesListStri = EvolveToExpr.exprsListToCsvStri(odesListFromEvolve)
//    val whenEvensStri = EvolveToExpr.exprsListToCsvStri(whenEventsFromEvolve)
//    
//    val plotStri = tendLimiMathStri + dummStri1 + odesListStri + dummStri2 + whenEvensStri + dummStri3 
//    return plotStri
//  } 
//  
//  def seqEvolRootToPlotStri(evolve: HP, tendLimi: Int) : String = {
//    val dummStri1 = "Set[global`tends, {0}]; \n Set[global`sol, {}]; \n"
//    val dummStri2_1 = "Set[global`sol, Append[global`sol, NDSolve[{"
//    // hayQuePreguntar: ok to use t > 0.00001?
//    val dummStri2_2 = ", x[Evaluate[Last[global`tends]]] == 0, y[Evaluate[Last[global`tends]]] == 0, v[Evaluate[Last[global`tends]]] == 1, r[Evaluate[Last[global`tends]]] == 1, om[Evaluate[Last[global`tends]]] == v[Evaluate[Last[global`tends]]]/r[Evaluate[Last[global`tends]]], dx[Evaluate[Last[global`tends]]] == 1, dy[Evaluate[Last[global`tends]]] == 0, ac[Evaluate[Last[global`tends]]] == 0, dummT[0] == 0, WhenEvent[GreaterEqual[t, 0.00001], CompoundExpression[Set[global`tends, Append[global`tends, t]], \"StopIntegration\"]]}, {x, y, v, ac, om, dx, dy, r, dummT}, {t, 0, 1}]]];\n"
//    val dummStri3 = "Plot[Piecewise[Table[{Evaluate[x[t] /. global`sol[[i]]],  And[t >= global`tends[[i]], t < global`tends[[i + 1]]]}, {i, 1, Length[global`tends] - 1}]], {t, Evaluate[First[global`tends]], Evaluate[Last[global`tends]]}]"
//    
//    val tendLimiMathStri = EvolveToExpr.tendLimiToSetExpr(tendLimi).toString() + ";\n"
//    
//    // hayQueHacer: gotta do something like (evolToOdesList(getFirstEvolve)) instead of hardcoding
//    val odesListStriForFirsEvol = "Equal[Derivative[1][x][t], Times[v[t], dx[t]]], Equal[ Derivative[1][y][t], Times[v[t], dy[t]]], Equal[Derivative[1][v][t], ac[t]], Equal[Derivative[1][ac][t], 0], Equal[ Derivative[1][r][t], 0], Equal[Derivative[1][om][t], Divide[ac[t], r[t]]], Equal[Derivative[1][dx][t], Times[Minus[om[t]], dy[t]]], Equal[Derivative[1][dy][t], Times[om[t], dx[t]]], Equal[Derivative[1][dummT][t], 1]"
//    
//    val evolsList = EvolveToExpr.seqToEvolsList(evolve)
//    val setsStriList = EvolveToExpr.evolsListToSetsStriList(evolsList)
//    val setsStri = EvolveToExpr.setsStriListToNlsvStri(setsStriList)
//      
//    return tendLimiMathStri + dummStri1 + dummStri2_1 + odesListStriForFirsEvol + dummStri2_2 + setsStri + dummStri3
//  }
  
//  def loopToPlotStri(loop: HP.loop, tendLimi: Int) : String = {
//    val dummStri1 = "s4 = NDSolve[{"
//    val dummStri2 = ", dummT[0] == 0, x[0] == 0, y[0] == 0, v[0] == 0, r[0] == 1, om[0] == v[0]/r[0], dx[0] == 1, dy[0] == 0, ac[0] == 1,"
//    val dummStri3 = "{x, y, v, ac, om, dx, dy, r, s}, {t, 0, global`tendLimi}]; Plot[Evaluate[v[t] /. s4], {t, 0, global`tend4}]"
//    
//    val tendLimiMathStri = EvolveToExpr.tendLimiToSetExpr(tendLimi).toString() + ";\n"
//    
//    val odesList = EvolveToExpr.loopToOdesList(loop)
//    val whenEvents = EvolveToExpr.loopToWhenEvents(loop, 9.0)
//    
//    val odesListStri = EvolveToExpr.exprsListToCsvStri(odesList)
//    val whenEvensStri = EvolveToExpr.exprsListToCsvStri(whenEvents)
//    
//      
//    return tendLimiMathStri + dummStri1
//  }
  
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
    
    
//    val hp = (Var("y") := Num(Exact.Integer(0))) seq
//    	(Var("v") := Num(Exact.Integer(0))) seq
//    	(Var("ac") := Num(Exact.Integer(0))) seq
//    	loop (
//    	    ? (Var("v") < Num(Exact.Integer(50))) seq
//    	    (
//    	        (
//    	            (Var("ac") := Num(Exact.Integer(1))) seq 
//    	            (Var("dummT") := Num(Exact.Integer(0))) seq
//    	            (Evolve(Var("dummT") <= Num(Exact.Integer(5)), (Var("y"), Var("v")), (Var("v"), Var("ac")), (Var("dummT"), Num(Exact.Integer(1)))))
//    	        ) ++
//    	        (
//    	            (Var("ac") :=* ) seq 
//    	            (? ((Var("ac") > Num(Exact.Integer(5))) & (Var("ac") < Num(Exact.Integer(10))))) seq
//    	            (Var("dummT") := Num(Exact.Integer(0))) seq
//    	            (Evolve(Var("dummT") <= Num(Exact.Integer(5)), (Var("y"), Var("v")), (Var("v"), Var("ac")), (Var("dummT"), Num(Exact.Integer(1)))))
//    	    	)
//    	    )
//    	)
    
     val hp = (Var("y") := Num(Exact.Integer(0))) seq
    	(Var("v") := Num(Exact.Integer(0))) seq
    	(Var("ac") := Num(Exact.Integer(0))) seq
    	loop (
    	    (
    	        (
    	            (Var("ac") := Num(Exact.Integer(1))) seq 
    	            (Var("dummT") := Num(Exact.Integer(0))) seq
    	            (Evolve(Var("dummT") <= Num(Exact.Integer(5)), (Var("y"), Var("v")), (Var("v"), Var("ac")), (Var("ac"), Num(Exact.Integer(0))), (Var("dummT"), Num(Exact.Integer(1)))))
    	        ) ++
    	        (
    	            (Var("ac") :=* ) seq 
    	            (Var("dummT") := Num(Exact.Integer(0))) seq
    	            (Evolve(Var("dummT") <= Num(Exact.Integer(5)), (Var("y"), Var("v")), (Var("v"), Var("ac")), (Var("ac"), Num(Exact.Integer(0))), (Var("dummT"), Num(Exact.Integer(1)))))
    	    	)
    	    )
    	)

    
    val mdlt = Modality(Box, hp, (Var("v") < Num(Exact.Integer(50))))
    
    
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
    
//    val execStri = hpToExpr.modaToExpr(mdlt, List("y"), 100.0, 20, 4.0, 11.0).toString() 
//    println(execStri)
//    val a = new MathematicaPlot(execStri)
//    
  }
}
