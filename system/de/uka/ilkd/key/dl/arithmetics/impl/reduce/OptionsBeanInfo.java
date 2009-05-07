/***************************************************************************
 *   Copyright (C) 2008 by Jan David Quesel                                *
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
/**
 * 
 */
package de.uka.ilkd.key.dl.arithmetics.impl.reduce;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.ArrayList;

import orbital.awt.TaggedPropertyEditorSupport;
import de.uka.ilkd.key.dl.arithmetics.impl.reduce.Options.QuantifierEliminationMethod;
import de.uka.ilkd.key.dl.arithmetics.impl.reduce.Options.ReduceSwitch;
import de.uka.ilkd.key.dl.options.FilePropertyEditor;

/**
 * @author jdq
 * 
 */
public class OptionsBeanInfo extends SimpleBeanInfo {
	private static final Class<Options> beanClass = Options.class;

	public OptionsBeanInfo() {
	}

	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor d = new BeanDescriptor(beanClass);
		d.setDisplayName("Reduce Options");
		d.setShortDescription("Adjusts values for the Reduce/Redlog interface");
		return d;
	}

	/* @Override */
	public PropertyDescriptor[] getPropertyDescriptors() {
		try {

			PropertyDescriptor[] pds = new PropertyDescriptor[] {
					createDescriptor(
							"reduceBinary",
							"Reduce Binary",
							"<html>The path to the reduce binary installation needed<br>"
									+ "to setup the correct environment for the tool</html>",
							false, true, FilePropertyEditor.class),
					createDescriptor(
							"qeMethod",
							"Quantifier Elimination",
							"<html>The method to use for quantifier elimination<br>"
									+ "(virtual substitution (rlqe), Cylindrical algebraic<br>"
									+ "decomposition (rlcad)...)</html>",
							false, true, QeMethodPropertyEditor.class),
					createDescriptor("eliminateFractions", "Eliminate Fractions",
							"Choose if KeYmaera should eliminate all fractions before calling reduce.",
							false, false),
				    createDescriptor("rlall", "Use Universal Closure",
							"If true the formula will be universally closed.",
							false, false),
					createDescriptor("rlsimpl", "Simplify formulas",
									"<html>Simplify. By default this switch is off.<br>" +
									"With this switch on, the function rlsimpl is applied at the expression<br>" +
									"evaluation stage. See rlsimpl.<br>" +
									"Automatically performing formula simplification at the evaluation stage<br>" +
									"is very similar to the treatment of polynomials or rational functions,<br>" +
									"which are converted to some normal form. For formulas, however, the<br>" +
									"simplified equivalent is by no means canonical.</html>",
									false, false,
									ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlqeqsc",
							"rlqeqsc",
							"<html>Quantifier elimination (super) quadratic special case.<br>"
									+ "By default these switches are off. They are relevant only in ofsf.<br>"
									+ "If turned on, alternative elimination sets are used for certain<br>"
									+ "special cases by rlqe/rlqea and rlgqe/rlgqea. (see Generic Quantifier<br>"
									+ "Elimination). They will possibly avoid violations of the degree<br>"
									+ "restrictions, but lead to larger results in general. Former versions<br>"
									+ "of redlog without these switches behaved as if rlqeqsc was on and<br>"
									+ "rlqesqsc was off.</html>", true, false,
							ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlqesqsc",
							"rlqesqsc",
							"<html>Quantifier elimination (super) quadratic special case. By default<br>"
									+ "these switches are off. They are relevant only in ofsf. If turned on,<br>"
									+ "alternative elimination sets are used for certain special cases by<br>"
									+ "rlqe/rlqea and rlgqe/rlgqea. (see Generic Quantifier Elimination).<br>"
									+ "They will possibly avoid violations of the degree restrictions, but lead<br>"
									+ "to larger results in general. Former versions of redlog without these<br>"
									+ "switches behaved as if rlqeqsc was on and rlqesqsc was off.</html>",
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlqedfs",
							"rlqedfs",
							"<html>Quantifier elimination depth first search. By default this switch is off.<br>"
									+ "It is also ignored in the acfsf context. It is ignored with the switch<br>"
									+ "rlqeheu on, which is the default for ofsf. Turning rlqedfs on makes<br>"
									+ "rlqe/rlqea and rlgqe/rlgqea (see Generic Quantifier Elimination) work<br>"
									+ "in a depth first search manner instead of breadth first search. This saves<br>"
									+ "space, and with decision problems, where variable-free atomic formulas can<br>"
									+ "be evaluated to truth values, it might save time. In general, it leads to<br>"
									+ "larger results.</html>", true, false,
							ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlqeheu",
							"rlqeheu",
							"<html>Quantifier elimination search heuristic. By default this switch is on<br>"
									+ "in ofsf and off in dvfsf. It is ignored in acfsf. Turning rlqeheu on causes<br>"
									+ "the switch rlqedfs to be ignored. rlqe/rlqea and rlgqe/rlgqea (see Generic<br>"
									+ "Quantifier Elimination) will then decide between breadth first search and depth<br>"
									+ "first search for each quantifier block, where dfs is chosen when the problem<br>"
									+ "is a decision problem.</html>", true,
							false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlqepnf",
							"rlqepnf",
							"<html>Quantifier elimination compute prenex normal form. By default this switch<br>"
									+ "is on, which causes that rlpnf (see Miscellaneous Normal Forms) is applied to formula<br>"
									+ "before starting the elimination process. If the argument formula to rlqe/rlqea or<br>"
									+ "rlgqe/rlgqea (see Generic Quantifier Elimination) is already prenex, this switch can<br>"
									+ "be turned off. This may be useful with formulas containing equiv since rlpnf applies rlnnf,<br>"
									+ "(see Miscellaneous Normal Forms), and resolving equivalences can double the size of a formula.<br>"
									+ "rlqepnf is ignored in acfsf, since nnf is necessary for elimination there.</html>",
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor("rlcadfac", "rlcadfac",
							"Factorisation. This is on by default. ", true,
							false, ReduceSwitchPropertyEditor.class),
					createDescriptor("rlcadbaseonly", "rlcadbaseonly",
							"Base phase only. Turned off by default. ", true,
							false, ReduceSwitchPropertyEditor.class),
					createDescriptor("rlcadprojonly", "rlcadprojonly",
							"Projection phase only. Turned off by default. ",
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor("rlcadextonly", "rlcadextonly",
							"Extension phase only. Turned off by default. ",
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor("rlcadpartial", "rlcadpartial",
							"Partial CAD. This is turned on by default. ",
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor("rlcadte", "rlcadte",
							"<html>Trial evaluation, the first improvement to partial CAD.<br>"
									+ " This is turned on by default.</html>",
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlcadpbfvs",
							"rlcadpbfvs",
							"<html>Propagation below free variable space, the second improvement to partial CAD.<br>"
									+ "This is turned on by default.</html>",
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlcadfulldimonly",
							"rlcadfulldimonly",
							"<html>Full dimensional cells only. This is turned off by default.<br>"
									+ "Only stacks over full dimensional cells are built. Such cells have<br>"
									+ "rational sample points. To do this ist sound only in special cases<br>"
									+ "as consistency problems (existenially quantified, strict inequalities).</html>",
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlcadtrimtree",
							"rlcadtrimtree",
							"<html>Trim tree. This is turned on by default.<br>"
									+ "Frees unused part of the constructed partial CAD-tree, and hence<br>"
									+ "saves space. However, afterwards it is not possible anymore to find<br>"
									+ "out how many cells were calculated beyond free variable space.</html>",
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlcadrawformula",
							"rlcadrawformula",
							"<html>Raw formula. Turned off by default.<br>"
									+ "If turned on, a variable-free DNF is returned (if simple solution<br>"
									+ "formula construction succeeds). Otherwise, the raw result is simplified<br>"
									+ "with rldnf.</html>", true, false,
							ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlcadisoallroots",
							"rlcadisoallroots",
							"<html>Isolate all roots. This is off by default. Turning this switch on allows<br>"
									+ "to find out, how much time is consumed more without incremental root<br>"
									+ "isolation.</html>", true, false,
							ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlcadaproj",
							"rlcadaproj",
							"<html>Augmented projection (always). By default, rlcadaproj is turned on and<br>"
									+ "rlcadaprojalways is turned off. If rlcadaproj is turned off, no augmented<br>"
									+ "projection is performed. Otherwerwise, if turned on, augmented projection<br>"
									+ "is performed always (if rlcadaprojalways is turned on) or just for the<br>"
									+ "free variable space (rlcadaprojalways turned off).</html>",
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlcadaprojalways",
							"rlcadaprojalways",
							"<html>Augmented projection (always). By default, rlcadaproj is turned on and<br>"
									+ "rlcadaprojalways is turned off. If rlcadaproj is turned off, no augmented<br>"
									+ "projection is performed. Otherwerwise, if turned on, augmented projection<br>"
									+ "is performed always (if rlcadaprojalways is turned on) or just for the<br>"
									+ "free variable space (rlcadaprojalways turned off).</html>",
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlcadhongproj",
							"rlcadhongproj",
							"<html>Hong projection. This is on by default.<br>"
									+ "If turned on, Hong's improvement for the projection operator is used.</html>",
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlanuexpsremseq",
							"rlanuexpsremseq",
							"<html>Pseudo remainder sequences. This is turned off by default.<br>"
									+ "This switch decides, whether division or pseudo division is used for<br>"
									+ "sturm chains.</html>", true, false,
							ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlanuexgcdnormalize",
							"rlanuexgcdnormalize",
							"<html>GCD normalize. This is turned on by default.<br>"
									+ "If turned on, the GCD is normalized to 1, if it is a constant polynomial.</html>",
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlanuexsgnopt",
							"rlanuexsgnopt",
							"<html>Sign optimization. This is turned off by default.<br>"
									+ "If turned on, it is tried to determine the sign of a constant polynomial<br>"
									+ "by calculating a containment.</html>",
							true, false, ReduceSwitchPropertyEditor.class),

			};
			return pds;
		} catch (IntrospectionException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private static PropertyDescriptor createDescriptor(String propertyName,
			String displayName, String shortDescription)
			throws IntrospectionException {
		return createDescriptor(propertyName, displayName, shortDescription,
				false);
	}

	private static PropertyDescriptor createDescriptor(String propertyName,
			String displayName, String shortDescription, boolean expert)
			throws IntrospectionException {
		return createDescriptor(propertyName, displayName, shortDescription,
				expert, false);
	}

	private static PropertyDescriptor createDescriptor(String propertyName,
			String displayName, String shortDescription, boolean expert,
			boolean preferred) throws IntrospectionException {
		return createDescriptor(propertyName, displayName, shortDescription,
				expert, preferred, null);
	}

	private static PropertyDescriptor createDescriptor(String propertyName,
			String displayName, String shortDescription, boolean expert,
			boolean preferred, Class<?> propertyEditor)
			throws IntrospectionException {
		PropertyDescriptor result = new PropertyDescriptor(propertyName,
				beanClass);
		result.setDisplayName(displayName);
		result.setShortDescription(shortDescription);
		result.setExpert(expert);
		result.setPreferred(preferred);
		if (propertyEditor != null) {
			result.setPropertyEditorClass(propertyEditor);
		}
		return result;
	}

	public static class QeMethodPropertyEditor extends
			TaggedPropertyEditorSupport {
		public QeMethodPropertyEditor() {
			super(getNames(QuantifierEliminationMethod.values()),
					QuantifierEliminationMethod.values());
		}
	}

	public static class ReduceSwitchPropertyEditor extends
			TaggedPropertyEditorSupport {
		public ReduceSwitchPropertyEditor() {
			super(getNames(ReduceSwitch.values()), ReduceSwitch.values());
		}
	}

	private static <E extends Enum<E>> String[] getNames(Enum<E> vals[]) {
		java.util.List<String> names = new ArrayList<String>();
		for (Enum<E> r : vals) {
			names.add(r.toString());
		}
		return names.toArray(new String[0]);
	}
}
