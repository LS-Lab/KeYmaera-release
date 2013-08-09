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
package de.uka.ilkd.key.dl.formulatools;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.Equals;
import de.uka.ilkd.key.dl.model.PredicateTerm;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Term;

public class DerivativeCreator {

	/**
	 * The function calculates the derivative (induction) of a term based on the
	 * derivivates given by @sys
	 * 
	 * note that the @sys must not contain any evolution domain
	 */
	public static final Term diffInd(DiffSystem sys, Term post, Services s) {
		// neue Methode
		HashMap<String, Term> replacements = new HashMap<String, Term>();
		collectDiffReplacements(sys, replacements, s);
		System.out.println("Replacements are: " + replacements);
		Term createNFF = NegationNormalForm.apply(post);
		System.out.println("Formula: " + post);
		System.out.println("NFF: " + createNFF);
		return Derive.apply(createNFF, replacements, null, s);
	}

	/**
	 * @author s0805753@sms.ed.ac.uk
	 * The function calculates variables in the state vector from by @sys
	 * 
	 */
	public static final ArrayList<String> stateVector(DiffSystem sys, Services s) {

		ArrayList<String> stateVec = new ArrayList<String>();
		
		HashMap<String, Term> replacements = new HashMap<String, Term>();
		collectDiffReplacements(sys, replacements, s);
		
		for(String var: replacements.keySet()){
			stateVec.add(var);
		}	
		
		return stateVec;
	}
	/**
	 * The function calculates the derivative (diffFin) of a term based on the
	 * derivivates given by @sys
	 * 
	 * note that the @sys must not contain any evolution domain
	 */
	public static final Term diffFin(DiffSystem sys, Term post, Term epsilon, Services s) {
		HashMap<String, Term> replacements = new HashMap<String, Term>();
		collectDiffReplacements(sys, replacements, s);
		System.out.println("Replacements are: " + replacements);
		Term createNFF = NegationNormalForm.apply(post);
		System.out.println("Formula: " + post);
		System.out.println("NFF: " + createNFF);
		return Derive.apply(createNFF, replacements, epsilon, s);
	}

	/**
	 * Collect all program variables which are children of a Dot.
	 * 
	 * @param form
	 *            the current root element.
	 * 
	 * @param map
	 *            the Map used for storing the result
	 */
	public static final void collectDiffReplacements(ProgramElement form, Map<String, Term> map, Services services) {
		if (form instanceof PredicateTerm) {
			PredicateTerm pred = (PredicateTerm) form;
			// we can only handle differential equations of the form x'=f(x,y)
			// here
			if (pred.getChildAt(0) instanceof Equals && pred.getChildAt(1) instanceof Dot) {
				de.uka.ilkd.key.dl.model.ProgramVariable pv = (de.uka.ilkd.key.dl.model.ProgramVariable) ((Dot) pred.getChildAt(1)).getChildAt(0);
				String pvName = pv.getElementName().toString();
				map.put(pvName, Prog2LogicConverter.convert((DLProgramElement) pred.getChildAt(2), services));
			} else {
				if (containsDots(pred)) {
					// could "a'>=b+5" be an equation in the diff-free
					throw new IllegalArgumentException("Don't know how to handle predicate " + pred.getChildAt(0) + " in " + pred);
				} else {
					// ignore evolution domain constraint
					System.out.println("Ignoring " + pred);
				}
			}
		}
		if (form instanceof DLNonTerminalProgramElement) {
			DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) form;
			for (ProgramElement p : dlnpe) {
				collectDiffReplacements(p, map, services);
			}
		}
	}

	/**
	 * @param pred
	 * @return
	 */
	private static boolean containsDots(DLProgramElement pred) {
		if (pred instanceof Dot) {
			return true;
		} else if (pred instanceof DLNonTerminalProgramElement) {
			DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) pred;
			for (ProgramElement p : dlnpe) {
				if (containsDots((DLProgramElement) p)) {
					return true;
				}
			}
			return false;
		}
		return false;
	}
}
