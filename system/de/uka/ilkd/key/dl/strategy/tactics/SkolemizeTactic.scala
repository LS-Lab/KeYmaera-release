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
class SkolemizeTactic {

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
    val tacomplete = papp.createSkolemConstant(s.getNamespaces.getUniqueName("sk"), sv, false, s)
    var ta = tacomplete.instantiateWithMV(g)
    ta = ta.createSkolemFunctions(s.getNamespaces().functions(), s)
    val skC = ta.instantiations().lookupValue(new Name("sk"))
    val trm = ta.instantiations().lookupValue(new Name("trm"))
    s.getNamespaces().functions().add(skC.asInstanceOf[Term].op())
    // apply the skolemize rule
    ta.execute(g, s);
    var skip = -1;
    for (i <- 0 until g.sequent().antecedent().size())
      if (g.sequent().antecedent().get(i).formula().op() == Op.EQUALS) {
        if (g.sequent().antecedent().get(i).formula().sub(0).op() == skC.asInstanceOf[Term].op()) {
          skip = i
        }
      }
    assert(skip != -1)
    // we need to ignore formula skip as that is the equation we want to apply
    var r: PosInOccurrence = null
    do {
      var skip = -1;
      for (i <- 0 until g.sequent().antecedent().size())
        if (g.sequent().antecedent().get(i).formula().op() == Op.EQUALS) {
          if (g.sequent().antecedent().get(i).formula().sub(0).op() == skC.asInstanceOf[Term].op()
              && g.sequent().antecedent().get(i).formula().sub(1).op() == trm.asInstanceOf[Term].op()) {
            skip = i
          }
        }
      assert(skip != -1)

      r = null
      for (i <- 0 until g.sequent.antecedent.size) {
        if (i != skip) {
          val res = findNonRigidFunction(new PosInOccurrence(g.sequent.antecedent.get(i), PosInTerm.TOP_LEVEL, true))
          res get p.subTerm().toString() match {
            case Some(l) => r = l
            case _ =>
          }
        }
      }
      if (r == null) {
        for (i <- 0 until g.sequent.succedent.size) {
          val res = findNonRigidFunction(new PosInOccurrence(g.sequent.succedent.get(i), PosInTerm.TOP_LEVEL, false))
          res get p.subTerm().toString() match {
            case Some(l) => r = l
            case _ =>
          }
        }
      }
      if (r != null) {
        var apply_eq = g.ruleAppIndex.tacletIndex.lookup("applyEq_sym")
        apply_eq = apply_eq.matchFind(r, Constraint.BOTTOM, s, Constraint.BOTTOM)
        val papp = apply_eq.setPosInOccurrence(r)
        var ra = papp.addInstantiation(papp.uninstantiatedVars().iterator().next(), g.sequent().antecedent().get(skip).formula().sub(0), false)
        val lst: ImmutableList[IfFormulaInstantiation] = ImmutableSLList.nil.asInstanceOf[ImmutableList[IfFormulaInstantiation]]
        val ifInstList = lst.append(new IfFormulaInstSeq(g.sequent, true, g.sequent.antecedent.get(skip)))
        ra = ra.setIfFormulaInstantiations(ifInstList, s, Constraint.BOTTOM)
        ra = ra.instantiateWithMV(g)
        ra = ra.createSkolemFunctions(s.getNamespaces().functions(), s)
        // apply rule
        ra.execute(g, s);
      }
    } while (r != null)
    // afterwards hide the introduced equality
    var hide = g.ruleAppIndex().tacletIndex().lookup("hide_left")
    val pos0 = new PosInOccurrence(g.sequent().antecedent().get(skip), PosInTerm.TOP_LEVEL, true)
    hide = hide.matchFind(pos0, Constraint.BOTTOM, s, Constraint.BOTTOM)
    val pHide = hide.setPosInOccurrence(pos0)
    var ra = pHide.instantiateWithMV(g)
    ra = ra.createSkolemFunctions(s.getNamespaces().functions(), s)
    // apply rule
    ra.execute(g, s);
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