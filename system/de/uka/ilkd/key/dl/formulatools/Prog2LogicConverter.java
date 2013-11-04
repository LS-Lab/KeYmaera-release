/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel                                *
 *   quesel@informatik.uni-oldenburg.de                                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
/*
 * Prog2LogicConverter.java 1.00 Tue Jan 23 16:48:01 CET 2007
 */
package de.uka.ilkd.key.dl.formulatools;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.And;
import de.uka.ilkd.key.dl.model.Biimplies;
import de.uka.ilkd.key.dl.model.Box;
import de.uka.ilkd.key.dl.model.Constant;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.DLStatementBlock;
import de.uka.ilkd.key.dl.model.Diamond;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.Exists;
import de.uka.ilkd.key.dl.model.Forall;
import de.uka.ilkd.key.dl.model.FunctionTerm;
import de.uka.ilkd.key.dl.model.IfExpr;
import de.uka.ilkd.key.dl.model.Implies;
import de.uka.ilkd.key.dl.model.LogicalVariable;
import de.uka.ilkd.key.dl.model.MetaVariable;
import de.uka.ilkd.key.dl.model.NamedElement;
import de.uka.ilkd.key.dl.model.NonRigidFunction;
import de.uka.ilkd.key.dl.model.Not;
import de.uka.ilkd.key.dl.model.Or;
import de.uka.ilkd.key.dl.model.PredicateTerm;
import de.uka.ilkd.key.dl.model.Variable;
import de.uka.ilkd.key.dl.model.VariableDeclaration;
import de.uka.ilkd.key.dl.parser.NumberCache;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.ProgramElementName;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.op.*;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * The Prog2LogicConverter is used when a program element is dragged to the
 * logic level, thus it converts the DL data structures into Term objects.
 * 
 * @version 1.00
 * @author jdq
 * @author ap
 */
public class Prog2LogicConverter extends AbstractMetaOperator {
	public static final Name NAME = new Name("#prog2logic");

	public Prog2LogicConverter() {
		super(NAME, 1);
	}

	/**
	 * Just returns the first sub term of the given term. The real conversion is
	 * done by the Typeconverter using the static methods of this class.
	 * 
	 * @param arg0
	 *            the prog2logic term
	 * @param arg1
	 *            unused
	 * @param services
	 *            unused
	 * @return the first subterm of arg0
	 * 
	 * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#calculate(de.uka.ilkd.key.logic.Term,
	 *      de.uka.ilkd.key.rule.inst.SVInstantiations,
	 *      de.uka.ilkd.key.java.Services) calculate
	 */
	/*@Override*/
	public Term calculate(Term arg0, SVInstantiations arg1, Services services) {
		return arg0.sub(0);
	}

	/**
	 * Converts the given program element into a logic term
	 * 
	 * @param pe
	 *            the program element to convert
	 * @return the logic representation of the program element
	 */
	public static Term convert(DLProgramElement pe, Services services) {
		if (pe instanceof DiffSystem) {
			System.out.println("Converting: " + pe);// XXX
			Term result = TermBuilder.DF.tt();
			for (ProgramElement p : (DiffSystem) pe) {
				result = TermBuilder.DF.and(result, convertRecursivly(p,
						services, new HashMap<String, Term>()));
			}
			System.out.println("Result is: " + result);// XXX
			return result;
		} else {
			return convertRecursivly(pe, services, new HashMap<String, Term>());
		}
	}

	/**
	 * Obtain a view of a DLProgramElement iterator as a converted term
	 * iterator.
	 */
	public static Iterator<Term> convert(
			final java.util.Iterator<? extends DLProgramElement> iterator,
			final Services services) {
		return new java.util.Iterator<Term>() {
			public boolean hasNext() {
				return iterator.hasNext();
			}

			public Term next() {
				return convert(iterator.next(), services);
			}

			public void remove() {
				iterator.remove();
			}
		};
	}

	public static de.uka.ilkd.key.logic.op.Function getFunction(Name name,
			NamespaceSet namespaces, int arity, Sort sort, Sort[] sorts, boolean rigid) {
		de.uka.ilkd.key.logic.op.Function result = (de.uka.ilkd.key.logic.op.Function) namespaces
				.functions().lookup(name);
		if(!rigid) {
		    assert result == null || result instanceof de.uka.ilkd.key.logic.op.NonRigidFunctionLocation :
		        "The function " + name + " should be a non-rigid function!";
		}

		if (name.toString().startsWith("$")) {
			throw new IllegalArgumentException(
					"Dollar ($) prefix is reserved for logic variables!");
		}

		if (result == null) {
			Sort sortR = RealLDT.getRealSort();
			if (arity == 0) {
				try {
					BigDecimal b = new BigDecimal(name.toString());
					result = NumberCache.getNumber(b, sortR);
				} catch (Exception e) {
					// not a number
				}
			}
			if (result == null) {
			    if (rigid) {
			        result = new de.uka.ilkd.key.logic.op.RigidFunction(name, sort,
			                sorts);
			    } else {
			        result = new de.uka.ilkd.key.logic.op.NonRigidFunctionLocation(name, sort,
			                sorts, true);
			        
			    }
			}
			namespaces.functions().add(result);
		}

		return result;
	}

	/**
	 * Converts the given DLFormula recursivly into a logic formula
	 * 
	 * @param form
	 *            the formula to convert
	 * @return the converted formula
	 */
	public static Term convertRecursivly(ProgramElement form,
			Services services, Map<String, Term> dotReplacementmap) {
		Sort sortR = RealLDT.getRealSort();

		TermBuilder termBuilder = services.getTypeConverter();
		if (form instanceof PredicateTerm) {
			PredicateTerm p = (PredicateTerm) form;
			Term[] subTerms = new Term[p.getChildCount() - 1];
			Sort[] sorts = new Sort[p.getChildCount() - 1];

			for (int i = 1; i < p.getChildCount(); i++) {
				subTerms[i - 1] = convertRecursivly(p.getChildAt(i), services,
						dotReplacementmap);
				sorts[i - 1] = subTerms[i - 1].sort();
			}
			Name elementName = ((NamedElement) p.getChildAt(0))
					.getElementName();
			if (elementName.equals(new Name("equals"))) {
				assert subTerms.length == 2 : "equals has arity 2";
				return termBuilder.equals(subTerms[0], subTerms[1]);
				// return termBuilder.tf().createEqualityTerm(subTerms);
			} else {
				if (elementName.toString().startsWith("$")) {
					LogicVariable var = (LogicVariable) services
							.getNamespaces().variables().lookup(
									((NamedElement) p.getChildAt(0))
											.getElementName());
					if (var == null) { // XXX
						// TODO: find a way to locate bound variable objects
						System.out.println("Prog2LogicConverter: Could not find logic variable "
								+ ((NamedElement) p.getChildAt(0))
										.getElementName());// XXX
						var = new LogicVariable(
								((NamedElement) p.getChildAt(0))
										.getElementName(), sortR);
						services.getNamespaces().variables().add(var);
					}
					return termBuilder.var(var);
				}
				return termBuilder.func(getFunction(elementName, services
						.getNamespaces(), subTerms.length, Sort.FORMULA, sorts, true),
						subTerms);
			}

		} else if (form instanceof FunctionTerm) {
			FunctionTerm p = (FunctionTerm) form;
			if (p.getChildAt(0) instanceof Variable) {
				return convertRecursivly(p.getChildAt(0), services,
						dotReplacementmap);
			}
			if (((NamedElement) p.getChildAt(0)).getElementName().toString()
					.startsWith("$")) {
				LogicVariable var = (LogicVariable) services.getNamespaces()
						.variables().lookup(
								((NamedElement) p.getChildAt(0))
										.getElementName());
				if (var == null) { // XXX
					// TODO: find a way to locate bound variable objects
					System.out
							.println("Prog2LogicConverter: Could not find logic variable "
									+ ((NamedElement) p.getChildAt(0))
											.getElementName());// XXX
					var = new LogicVariable(((NamedElement) p.getChildAt(0))
							.getElementName(), sortR);
					services.getNamespaces().variables().add(var);
				}
				return termBuilder.var(var);
			}
			Term[] subTerms = new Term[p.getChildCount() - 1];
			Sort[] sorts = new Sort[p.getChildCount() -1];

			for (int i = 1; i < p.getChildCount(); i++) {
				subTerms[i - 1] = convertRecursivly(p.getChildAt(i), services,
						dotReplacementmap);
				sorts[i-1] = subTerms[i - 1].sort();
			}

			return termBuilder
					.func(getFunction(((NamedElement) p.getChildAt(0))
							.getElementName(), services.getNamespaces(),
							subTerms.length, sortR, sorts, !(p.getChildAt(0) instanceof NonRigidFunctionLocation)), subTerms);
		} else if (form instanceof Forall) {
			Forall f = (Forall) form;
			VariableDeclaration decl = (VariableDeclaration) f.getChildAt(0);

			LogicVariable[] vars = new LogicVariable[decl.getChildCount() - 1];
			Sort sort = (Sort) services.getNamespaces().sorts().lookup(decl.getType().getElementName());
			for (int i = 1; i < decl.getChildCount(); i++) {
				vars[i - 1] = new LogicVariable(((NamedElement) decl.getChildAt(i)).getElementName(), sort);
				services.getNamespaces().variables().add(vars[i - 1]);
			}
			// do not convert the formula before adding the vars
			Term formula = convertRecursivly(f.getChildAt(1), services,
					dotReplacementmap);
            // WARNING: do not use the TermBuilder here because it fails in case \forall R xn. [x:=xn] x > 0!
            // return TermBuilder.DF.all(vars, formula);
            return TermFactory.DEFAULT.createQuantifierTerm(Op.ALL, vars, formula);
		} else if (form instanceof Exists) {
			Exists f = (Exists) form;
			VariableDeclaration decl = (VariableDeclaration) f.getChildAt(0);
			LogicVariable[] vars = new LogicVariable[decl.getChildCount() - 1];
			Sort sort = (Sort) services.getNamespaces().sorts().lookup(decl.getType().getElementName());
			for (int i = 1; i < decl.getChildCount(); i++) {
				vars[i - 1] = new LogicVariable(((NamedElement) decl.getChildAt(i)).getElementName(), sort);
				services.getNamespaces().variables().add(vars[i - 1]);
			}
			// do not convert the formula before adding the vars
			Term formula = convertRecursivly(f.getChildAt(1), services,
					dotReplacementmap);
            // WARNING: do not use the TermBuilder here because it fails in case \forall R xn. [x:=xn] x > 0!
            // return TermBuilder.DF.ex(vars, formula);
            return TermFactory.DEFAULT.createQuantifierTerm(Op.EX, vars, formula);
		} else if (form instanceof de.uka.ilkd.key.dl.model.Box) {
		    Box b = (Box) form;
		    return TermBuilder.DF.box(JavaBlock.createJavaBlock(new DLStatementBlock((DLProgram)b.getChildAt(0))), convertRecursivly(b.getChildAt(1), services, dotReplacementmap));
		} else if (form instanceof de.uka.ilkd.key.dl.model.Diamond) {
		    Diamond b = (Diamond) form;
		    return TermBuilder.DF.dia(JavaBlock.createJavaBlock(new DLStatementBlock((DLProgram)b.getChildAt(0))), convertRecursivly(b.getChildAt(1), services, dotReplacementmap));
		} else if (form instanceof IfExpr) {
		    IfExpr i = (IfExpr) form;
            return termBuilder.ife(
                    convertRecursivly(i.getChildAt(0), services,
                            dotReplacementmap),
                    convertRecursivly(i.getChildAt(1), services,
                            dotReplacementmap),
                    convertRecursivly(i.getChildAt(2), services,
                            dotReplacementmap));
		} else if (form instanceof DLNonTerminalProgramElement) {
			DLNonTerminalProgramElement p = (DLNonTerminalProgramElement) form;
			Term[] subTerms = new Term[p.getChildCount()];

			for (int i = 0; i < subTerms.length; i++) {
				subTerms[i] = convertRecursivly(p.getChildAt(i), services,
						dotReplacementmap);
			}

			if (p instanceof And) {
				return termBuilder.and(subTerms);
			} else if (p instanceof Or) {
				return termBuilder.or(subTerms);
			} else if (p instanceof Implies) {
				Term result = termBuilder.tt();

				for (Term element : subTerms) {
					result = termBuilder.imp(result, element);
				}

				return result;
			} else if (p instanceof Biimplies) {
				Term result = termBuilder.tt();

				for (Term element : subTerms) {
					result = termBuilder.equiv(result, element);
				}

				return result;
			} else if (p instanceof Not) {
				return termBuilder.not(subTerms[0]);
			} else if (p instanceof Dot) {
				final String name = ((NamedElement) p.getChildAt(0))
						.getElementName().toString();
				assert name != null && !name.equals("") : "variable has to have a name";
				if (dotReplacementmap.containsKey(name + ((Dot) p).getOrder())) {
					return dotReplacementmap.get(name + ((Dot) p).getOrder());
				}
				// just use the previous name and append the order
				String newName = name;
				newName += ((Dot) p).getOrder();
				String n = services.getNamespaces().getUniqueName(newName);
				de.uka.ilkd.key.logic.op.ProgramVariable var = new LocationVariable(
						new ProgramElementName(n), RealLDT.getRealSort());
				services.getNamespaces().programVariables().add(var);
				Term var2 = TermBuilder.DF.var(var);
				dotReplacementmap.put(name + ((Dot) p).getOrder(), var2);
				return var2;
			}
		} else if (form instanceof Variable) {
			Variable vform = (Variable) form;
			Name elementName = vform.getElementName();
			if (form instanceof de.uka.ilkd.key.logic.op.ProgramVariable) {
			    return termBuilder.var((de.uka.ilkd.key.logic.op.ProgramVariable)form);
			} else if (form instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
				de.uka.ilkd.key.logic.op.ProgramVariable var = getCorresponding(
						(de.uka.ilkd.key.dl.model.ProgramVariable) vform,
						services);
				return termBuilder.var(var);
			} else if (form instanceof LogicalVariable) {
				LogicVariable var = (LogicVariable) services.getNamespaces()
						.variables().lookup(elementName);
				if (var == null) { // XXX
					// TODO: find a way to locate bound variable objects
					System.out.println("Prog2LogicConverter: Could not find logic variable "
							+ elementName);// XXX
					var = new LogicVariable(elementName, sortR);
					services.getNamespaces().variables().add(var);
				}
				return termBuilder.var(var);
			} else if (form instanceof MetaVariable) {
				Metavariable var = (Metavariable) services.getNamespaces()
						.variables().lookup(elementName);
				if (var == null) { // XXX
					throw new IllegalStateException(
							"Could not find meta variable " + form);
				}
				return TermFactory.DEFAULT
						.createFunctionTerm((Metavariable) var);
			}
		} else if (form instanceof Constant) {
			return termBuilder.func(getFunction(new Name(""
					+ ((Constant) form).getValue()), services.getNamespaces(),
					0, sortR, new Sort[0], true));
		}

		throw new IllegalArgumentException("Cannot convert " + form);
	}

	/**
	 * Lookup the respective logic.op.ProgramVariable belonging to
	 * dl.model.ProgramVariable
	 * 
	 * @param services
	 * @return
	 */
	public static Set<de.uka.ilkd.key.logic.op.ProgramVariable> getCorresponding(
			Set<de.uka.ilkd.key.dl.model.ProgramVariable> form,
			Services services) {
		Set<de.uka.ilkd.key.logic.op.ProgramVariable> set2 = new LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable>(
				form.size() + 1);
		for (de.uka.ilkd.key.dl.model.ProgramVariable x : form) {
			set2.add(getCorresponding(x, services));
		}
		return set2;
	}

	/**
	 * Lookup the logic.op.ProgramVariable belonging to a
	 * dl.model.ProgramVariable
	 * 
	 * @param services
	 * @return
	 */
	public static de.uka.ilkd.key.logic.op.ProgramVariable getCorresponding(
			de.uka.ilkd.key.dl.model.ProgramVariable form, Services services) {
	    if(form instanceof de.uka.ilkd.key.logic.op.ProgramVariable) {
	        return (ProgramVariable) form;
	    }
		// @todo assert namespaces.unique because of dangerous name equality
		de.uka.ilkd.key.logic.op.ProgramVariable var = (de.uka.ilkd.key.logic.op.ProgramVariable) services
				.getNamespaces().programVariables().lookup(
						form.getElementName());
		if (var == null) {
			if (form.getElementName().toString().startsWith("dollar$")) {
				var = new LocationVariable(new ProgramElementName(form
						.getElementName().toString()), RealLDT.getRealSort());
				services.getNamespaces().programVariables().add(var);
				return var;
			}
			throw new IllegalStateException("ProgramVariable " + form
					+ " is not declared");
		}
		return var;
	}

	/**
	 * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#sort(de.uka.ilkd.key.logic.Term[])
	 *      sort
	 */
	/*@Override*/
	public Sort sort(de.uka.ilkd.key.logic.Term[] arg0) {
		return Sort.FORMULA;
	}

	/**
	 * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#validTopLevel(de.uka.ilkd.key.logic.Term)
	 *      validTopLevel
	 */
	/*@Override*/
	public boolean validTopLevel(Term arg0) {
		return true;
	}

}
