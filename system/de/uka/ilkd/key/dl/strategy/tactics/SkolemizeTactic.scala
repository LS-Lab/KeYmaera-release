/**
 * *****************************************************************************
 * Copyright (c) 2012 Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 *     Jan-David Quesel - initial API and implementation
 * ****************************************************************************
 */
package de.uka.ilkd.key.dl.strategy.tactics
import de.uka.ilkd.key.proof.Goal
import de.uka.ilkd.key.java.Services
import de.uka.ilkd.key.logic.Sequent
import de.uka.ilkd.key.logic.Term
import de.uka.ilkd.key.dl.formulatools.NonRigidFunction
import de.uka.ilkd.key.logic.Name
import de.uka.ilkd.key.dl.logic.ldt.RealLDT
import de.uka.ilkd.key.logic.PosInOccurrence
import de.uka.ilkd.key.logic.PosInTerm
import de.uka.ilkd.key.logic.Constraint
import de.uka.ilkd.key.logic.op.RigidFunction.FunctionType
import scala.collection.mutable.LinkedHashMap
import de.uka.ilkd.key.logic.Named
import de.uka.ilkd.key.logic.op.Op
import de.uka.ilkd.key.rule.IfFormulaInstantiation
import de.uka.ilkd.key.rule.IfFormulaInstSeq
import de.uka.ilkd.key.collection.ImmutableList
import de.uka.ilkd.key.collection.ImmutableSLList

/**
 * @author jdq
 *
 */
object SkolemizeTactic {

  def apply(g: Goal, s: Services): Unit = {
    val positions = findNonRigidFunction(g.sequent)
    val (str, p) = positions.head
    var skolemize = g.ruleAppIndex.tacletIndex.lookup("skolemize")
    // add the new function symbol to the namespaces
    // choose the position where we want to start
    skolemize = skolemize.matchFind(p, Constraint.BOTTOM, s, Constraint.BOTTOM)
    val papp = skolemize.setPosInOccurrence(p)
    // there should only be one schema variable left that needs instantiation
    val sv = papp.uninstantiatedVars().iterator().next()
    val indices = (x: Term) => if (x.arity > 0) "_" + (for (i <- 0 until x.arity) yield x.sub(i).op.name.toString).reduce((a, b) => a + "_" + b) else ""
    // important: the instantiation has to marked as interesting to get saved with the proofs
    val tacomplete = papp.createSkolemConstant(s.getNamespaces.getUniqueName(p.subTerm.op.name.toString + indices(p.subTerm)), sv, true, s)
    var ta = tacomplete.instantiateWithMV(g)
    ta = ta.createSkolemFunctions(s.getNamespaces().functions(), s)
    val skC = ta.instantiations().lookupValue(new Name("sk"))
    val trm = ta.instantiations().lookupValue(new Name("trm"))
    assert(s.getNamespaces.lookup(skC.asInstanceOf[Term].op.name) != null)
    // s.getNamespaces().functions().addSafely(skC.asInstanceOf[Term].op())
    // apply the skolemize rule
    ta.execute(g, s)
    var skip = -1
    for (i <- 0 until g.sequent().antecedent().size())
      if (g.sequent().antecedent().get(i).formula().op() == Op.EQUALS) {
        if (g.sequent().antecedent().get(i).formula().sub(0).op() == skC.asInstanceOf[Term].op()
          && g.sequent().antecedent().get(i).formula().sub(1).op() == trm.asInstanceOf[Term].op()) {
          skip = i
        }
      }
    assert(skip != -1)
    // Apply the resulting equality on everywhere
    ApplyEquationTactic.apply(g, new PosInOccurrence(g.sequent().antecedent().get(skip), PosInTerm.TOP_LEVEL, true), false, s)
    // if there are still non rigid functions around, go on
    if (!findNonRigidFunction(g.sequent()).isEmpty)
      apply(g, s)
  }

  def findNonRigidFunction(s: Sequent): LinkedHashMap[String, PosInOccurrence] = {
    var it = s.antecedent.iterator
    var result = new LinkedHashMap[String, PosInOccurrence]
    while (it.hasNext) {
      val form = it.next
      result ++= findNonRigidFunction(new PosInOccurrence(form, PosInTerm.TOP_LEVEL, true))
    }
    it = s.succedent.iterator
    while (it.hasNext) {
      val form = it.next
      result ++= findNonRigidFunction(new PosInOccurrence(form, PosInTerm.TOP_LEVEL, false))
    }
    result
  }

  def findNonRigidFunction(p: PosInOccurrence): LinkedHashMap[String, PosInOccurrence] = {
    var result = new LinkedHashMap[String, PosInOccurrence]
    p.subTerm() match {
      case NonRigidFunction(_, _) => result += ((p.subTerm.toString, p))
      case _ =>
    }
    for (i <- 0 until p.subTerm.arity) {
      result ++= findNonRigidFunction(p.down(i))
    }
    result
  }

}
