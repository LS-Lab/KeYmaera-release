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
package de.uka.ilkd.key.dl.formulatools

import de.uka.ilkd.key.logic.op.Op
import de.uka.ilkd.key.logic.Term

/**
 * @author jdq
 *
 */
object NegationNormalForm {

  implicit def term2richterm(t: Term): RichTerm = new RichTerm(t)

  def apply(t: Term): Term = apply(t, false)

  def apply(t: Term, negated: Boolean): Term = {
    if (negated) negative(t) else positive(t)
  }

  def negative(t: Term): Term = t match {
    case All(a, v) => Ex(negative(a), v)
    case Ex(a, v) => All(negative(a), v)
    case Not(a) => positive(a)
    case Or(a, b) => negative(a) & negative(b)
    case And(a, b) => negative(a) | negative(b)
    case Imp(a, b) => positive(a) | negative(b)
    case Eqv(a, b) => (negative(a) & positive(b)) | (positive(a) & negative(b))
    case Equals(a, b) => a unequal b
    case Greater(a, b) => a leq b
    case GreaterEquals(a, b) => a lt b
    case LessEquals(a, b) => a gt b
    case Less(a, b) => a geq b
    case UnEquals(a, b) => a equal b
    case _ => throw new IllegalArgumentException("Don't known how to convert " + t.op)
  }

  def positive(t: Term): Term = t match {
    case All(a, v) => All(positive(a), v)
    case Ex(a, v) => Ex(positive(a), v)
    case Not(a) => negative(a)
    case Or(a, b) => positive(a) | positive(b)
    case And(a, b) => positive(a) & positive(b)
    case Imp(a, b) => negative(a) | positive(b)
    case Eqv(a, b) => (positive(a) <-> positive(b))
    case Equals(a, b) => a equal b
    case Greater(a, b) => a gt b
    case GreaterEquals(a, b) => a geq b
    case LessEquals(a, b) => a leq b
    case Less(a, b) => a lt b
    case UnEquals(a, b) => a unequal b
    case _ => throw new IllegalArgumentException("Don't known how to convert " + t.op)
  }
}





