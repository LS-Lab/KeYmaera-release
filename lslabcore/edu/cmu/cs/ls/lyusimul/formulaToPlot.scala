package edu.cmu.cs.ls.lyusimul

import edu.cmu.cs.ls._
import scala.math.BigInt.int2bigInt
import com.wolfram.jlink.Expr
// import ExprToPlottableString
// import scala.math.BigInt.int2bigInt

object FormulaToPlot {

  val dummyStringPart1_1 = "global`tend=10; \n" +
  "ObjDynamics[obj_, tend_] := \n" + 
  " NDSolve[{"
  
  val dummyStringPart1_2 = "x'[t] == v[t]*dx[t], y'[t] == v[t]*dy[t], v'[t] == ac[t], \n" + 
"   ac'[t] == 0, r'[t] == 0, om'[t] == ac[t]/r[t], \n" + 
"   dx'[t] == -om[t]* dy[t], dy'[t] == om[t]*dx[t], end'[t] == 0 \n"

  val dummyStringPart1_3 = ",s'[t]==1, s[0]==0, end[0] == tend,\n" + 
"   x[0] == x0 /. obj, y[0] == y0 /. obj, v[0] == v0 /. obj, \n" + 
"   r[0] == r0 /. obj,\n" + 
"   om[0] == v[0]/r[0], dx[0] == dx0 /. obj, dy[0] == dy0 /. obj, \n" + 
"   ac[0] == a0 /. obj, WhenEvent @@@ Evaluate[actions /. obj],\n" + 
"   WhenEvent[v[t] == 0, end[t] -> t], \n" + 
"   WhenEvent[v[t] > 0.01, end[t] -> tend],"
		
  val dummyStringPart1_4 = "}, {s, x, y, v, ac, om, dx, dy, \n" + 
"   r, end},\n" + 
"  {t, 0, tend}]\n" + 
"\n" + 
"PlotDynamics::\"usage\" = \"plot the dynamics of a list of objects\"\n" + 
"PlotDynamics[objs_List, plotargs__ : {}] :=\n" + 
" Module[{T = global`tend, sol},\n" + 
"  sol = Table[ObjDynamics[objs[[i]], T], {i, Length[objs]}];\n" + 
"  Table[\n" + 
"   {Plot[{x[t], y[t]} /. sol, {t, 0, T}, \n" + 
"     AxesLabel -> {Style[t, 28], Style[p, 28]}, plotargs,\n" + 
"     Epilog -> {\n" + 
"       Inset[\n" + 
"        Framed[DisplayForm[Text[Style[\"p_x\", FontSize -> 20]]], \n" + 
"         RoundingRadius -> 5, Background -> White], {5, \n" + 
"         x[5] /. sol[[i]][[1]]}],\n" + 
"       Inset[\n" + 
"        Framed[DisplayForm[Text[Style[\"p_y\", FontSize -> 20]]], \n" + 
"         RoundingRadius -> 5, Background -> White], {5, \n" + 
"         y[5] /. sol[[i]][[1]]}]\n" + 
"       },\n" + 
"     AxesOrigin -> {0, 0}\n" + 
"     ],\n" + 
"    Plot[v[t] /. sol[[i]], {t, 0, T}, \n" + 
"     AxesLabel -> {Style[t, 28], Style[v, 28]}, plotargs, \n" + 
"     AxesOrigin -> {0, 0}\n" + 
"     , PlotRange -> {{0, global`tend}, {-3, 6}}],\n" + 
"    Plot[ac[t] /. sol[[i]], {t, 0, T}, \n" + 
"     AxesLabel -> {Style[t, 28], Style[a, 28]}, plotargs, \n" + 
"     AxesOrigin -> {0, 0}\n" + 
"     ],\n" + 
"    Plot[{dx[t], dy[t]} /. sol, {t, 0, T}, \n" + 
"     AxesLabel -> {Style[t, 28], Style[d, 28]}, plotargs,\n" + 
"     Epilog -> {\n" + 
"       Inset[\n" + 
"        Framed[DisplayForm[Text[Style[\"d_x\", FontSize -> 20]]], \n" + 
"         RoundingRadius -> 5, Background -> White], {1, \n" + 
"         dx[1] - 0.1 /. sol[[i]][[1]]}],\n" + 
"       Inset[\n" + 
"        Framed[DisplayForm[Text[Style[\"d_y\", FontSize -> 20]]], \n" + 
"         RoundingRadius -> 5, Background -> White], {5, \n" + 
"         dy[5] /. sol[[i]][[1]]}]\n" + 
"       },\n" + 
"     AxesOrigin -> {0, 0}\n" + 
"     ],\n" + 
"    Plot[om[t] /. sol[[i]], {t, 0, T}, \n" + 
"     AxesLabel -> {Style[t, 28], Style[\"omega\", 28]}, plotargs, \n" + 
"     AxesOrigin -> {0, 0}]\n" + 
"    },\n" + 
"   {i, Length[sol]}\n" + 
"   ]\n" + 
"  ]\n" + 
"\n" + 
"statobs1 = {x0 -> 3, y0 -> 1, v0 -> 0, dx0 -> 0, dy0 -> 0, r0 -> 1, \n" + 
"   a0 -> 0, actions -> {{v[t] == 0, ac[t] -> 0}}, obsr -> 0.25};\n" + 
"\n" + 
"robot2 = {x0 -> 0, y0 -> 1, v0 -> 1, dx0 -> 1, dy0 -> 0, r0 -> 10, \n" + 
"   a0 -> 0, b -> 0.7,\n" + 
"   actions -> {\n" + 
"     (* start clockwise arc to avoid static obstacle *)\n" + 
"     {Sqrt[(x[t] - Evaluate[x0 /. statobs1])^2 + (y[t] - \n" + 
"            Evaluate[y0 /. statobs1])^2] <= \n" + 
"       Evaluate[obsr /. statobs1] + \n" + 
"        v[t]^2/(2*0.7), {r[t] -> -v[t]^2/(2*0.7), om[t] -> v[t]/r[t]}},\n" + 
"     (* start counter clockwise arc to follow obstacle shape *)\n" + 
"     {y[t] <= \n" + 
"       Evaluate[y0 /. statobs1] - \n" + 
"        Evaluate[0.6*obsr /. statobs1], {r[t] -> \n" + 
"        Sqrt[(x[t] - Evaluate[x0 /. statobs1] - \n" + 
"             Evaluate[2*obsr /. statobs1])^2 + (y[t] - \n" + 
"             Evaluate[y0 /. statobs1] - \n" + 
"             Evaluate[2*obsr /. statobs1])^2], om[t] -> v[t]/r[t], \n" + 
"       \"RemoveEvent\"}},\n" + 
"     (* start clockwise arc towards original trajectory *)\n" + 
"     {x[t] >= \n" + 
"       Evaluate[x0 /. statobs1] + Evaluate[2*obsr /. statobs1] + \n" + 
"        0.1, {r[t] -> -2.5, om[t] -> v[t]/r[t], \"RemoveEvent\"}},\n" + 
"     (* hack: stop to avoid obstacle *)\n" + 
"     {Sqrt[(x[t] - 3.5)^2 + (y[t] - 1)^2] <= \n" + 
"       v[t]^2/(2*0.7), {ac[t] -> -0.7}},\n" + 
"     (* stay stopped once stopped *)\n" + 
"     {v[t] == 0, {ac[t] -> 0}},\n" + 
"     {t >= 0.2, {r[t] -> -4, om[t] -> v[t]/r[t], \"RemoveEvent\"}},\n" + 
"     {t >= 0.8, {r[t] -> 3, om[t] -> v[t]/r[t], \"RemoveEvent\"}},\n" + 
"     {t >= 1.5, {r[t] -> -5, om[t] -> v[t]/r[t], \"RemoveEvent\"}},\n" + 
"     {t >= 6, {ac[t] -> 0.5, \"RemoveEvent\"}},\n" + 
"     {x[t] > 5, {r[t] -> 100, om[t] -> v[t]/r[t], ac[t] -> 0}}\n" + 
"     }\n" + 
"   };\n" + 
"\n" + 
"robotsafeplots = \n" + 
" PlotDynamics[{robot2}, Filling -> Axis, \n" + 
"  PlotStyle -> Thickness[0.01`], \n" + 
"  LabelStyle -> {FontFamily -> \"Times\", FontSlant -> \"Italic\", \n" + 
"    FontSize -> 14}];\n" + 
"\n" 


  val dummyStringPart3 = 
"\n" + 
"robotsafeplots[[1]][[2]] = \n" + 
"  Show[robotsafeplots[[1]][[2]], safety`plot];\n" + 
"\n" +
"GraphicsGrid[robotsafeplots]\n" 

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
  
  
  def main(args: Array[String]) {
    
    //val myFrml = (Var("y")>Var("x"))
    val myFrml = ((Var("y")>Var("x")) & (Var("x") > Num(Exact.Integer(8))))
    val myMmt = MmtManipulation.formulaToMmt(myFrml)
    val myExpr = MmtManipulation.mmtToExpr(myMmt)
    val plottableString = ExprToPlottableString.exprToPlottableString(myExpr)
    // println(plottableString)
    // val dummy = new MathematicaPlot(plottableString)
    
//    x'[t] == v[t]*dx[t], y'[t] == v[t]*dy[t], v'[t] == ac[t], ac'[t] == 0,
// r'[t] == 0, om'[t] == ac[t]/r[t], dx'[t] == -om[t]*dy[t], 
//dy'[t] == om[t]*dx[t]
    
    // deboArreglar: should not use "true" for h.
    val evolve = Evolve(Var("s") < Num(Exact.Integer(7)), (Var("x"), Var("v") * Var("dx")), (Var("y"), Var("v") * Var("dy")), (Var("v"), Var("ac")),
        (Var("ac"), Num(Exact.Integer(0))), (Var("r"), Num(Exact.Integer(0))), (Var("om"), Var("ac") / Var("r")),
        (Var("dx"), (Arithmetic(Negate, Var("om"))) * Var("dy")), (Var("dy"), Var("om") * Var("dx")),
        (Var("end"), Num(Exact.Integer(0))))
        
    val odesListFromEvolve = EvolveToExpr.hpToOdesList(evolve)
    val whenEventsFromEvolve = EvolveToExpr.hpToWhenEvents(evolve, 9.0)
    
    val odesListStri = EvolveToExpr.odesListToCsvStri(odesListFromEvolve)
    val whenEventsStri = EvolveToExpr.odesListToCsvStri(whenEventsFromEvolve)
    
    val dummyString = dummyStringPart1_1 + odesListStri + dummyStringPart1_3 + whenEventsStri + dummyStringPart1_4 + plottableString + dummyStringPart3
    println(dummyString);
    val ddd = new MathematicaPlot(dummyString)
    

//    println(new Expr("StopIntegration").toString())
//    println(new Expr(Expr.SYMBOL, "StopIntegration").toString())
    
     // println(new Expr(math_sym("Pl"), List(new Expr(2L), new Expr(3L)).toArray, List(new Expr(2L), new Expr(3L)).toArray))
    
//    
//    val a = new Expr(Expr.SYMBOL, "Plus")
//    
//    
//    
//    val e = new Expr(new Expr(Expr.SYMBOL, "Plus"), Array(new Expr(2L), new Expr(3L)))
//    println(e.toString())
    

  }
}
