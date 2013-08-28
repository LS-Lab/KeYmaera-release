
package edu.cmu.cs.ls.lyusimul

import com.wolfram.jlink._

object ExprToPlottableString {
  // deboArreglar: the diff eq is hardcoded now. Fix it.
  def exprToPlottableString(safetyCond: Expr): String = {
    val plottableString = "safety`sol = NDSolve[{x'[t] == v[t], y'[t] == v[t], v[0] == 3, x[0] == 0, y[0] == 5, v'[t] == 0}, {x, y}, {t, 0, 10}]\nsafety`plot = Plot[Evaluate[{" + safetyCond.toString() + "} /. safety`sol], {t, 0, 10}, AxesOrigin -> {0, 0}]"
    return plottableString
  }
  
}


