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
import de.uka.ilkd.key.logic.EqualityConstraint
import de.uka.ilkd.key.logic.op.Modality
import de.uka.ilkd.key.logic.op.SubstOp

/**
 * @author jdq
 *
 */
object ApplyEquationTactic {

  def isApplicable(pos: PosInOccurrence) : Boolean = {
    if(pos != null && pos.isInAntec() && pos.posInTerm() == PosInTerm.TOP_LEVEL && pos.subTerm().op() == Op.EQUALS) 
      true
    else
      false
  }
  
  def apply(g: Goal, pos: PosInOccurrence, defLeft: Boolean, s: Services): Unit = {
    val equation = pos.subTerm();
    val defSymbol = if(defLeft) pos.subTerm.sub(0) else pos.subTerm.sub(1)
    val applyEqRule = if(defLeft) "applyEq" else "applyEq_sym"
    var skip = -1
    // we need to ignore formula skip as that is the equation we want to apply
    var r: PosInOccurrence = null
    do {
      skip = -1;
      for (i <- 0 until g.sequent().antecedent().size())
        if(g.sequent().antecedent().get(i).formula() == equation) 
            skip = i
      assert(skip != -1)
      
      r = null
      for (i <- 0 until g.sequent.antecedent.size) {
        if (i != skip && r == null) {
            r = findSymbol(new PosInOccurrence(g.sequent.antecedent.get(i), PosInTerm.TOP_LEVEL, true), defSymbol, s)
        }
      }
      if (r == null) {
        for (i <- 0 until g.sequent.succedent.size) {
          if(r == null) {
              r = findSymbol(new PosInOccurrence(g.sequent.succedent.get(i), PosInTerm.TOP_LEVEL, false), defSymbol, s)
          }
        }
      }
      if (r != null) {
        var apply_eq = g.ruleAppIndex.tacletIndex.lookup(applyEqRule)
        apply_eq = apply_eq.matchFind(r, Constraint.BOTTOM, s, Constraint.BOTTOM)
        val papp = apply_eq.setPosInOccurrence(r)
        var ra = papp.addInstantiation(papp.uninstantiatedVars().iterator().next(), g.sequent().antecedent().get(skip).formula().sub(if(defLeft) 1 else 0), true)
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
  }
  
  // finds the first occurrence of a term
  def findSymbol(p: PosInOccurrence, t: Term, s: Services): PosInOccurrence = {
    if(!p.subTerm.op.isInstanceOf[Modality] && !p.subTerm.op.isInstanceOf[SubstOp]) {
        if(new EqualityConstraint().unify(p.subTerm(), t, s).isSatisfiable()) {
          return p
        }
        for (i <- 0 until p.subTerm.arity) {
          val sym = findSymbol(p.down(i), t, s)
          if(sym != null)
            return sym
        }
    }
    return null
  }

}
