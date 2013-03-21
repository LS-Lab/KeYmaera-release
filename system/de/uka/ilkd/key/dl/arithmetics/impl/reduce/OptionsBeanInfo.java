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
import de.uka.ilkd.key.dl.options.EPropertyConstant;
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
							"reduceBinary",EPropertyConstant.OPTIONS_REDUCE_BINARY,
							true, true, true, FilePropertyEditor.class),
					createDescriptor(
							"qeMethod",EPropertyConstant.OPTIONS_REDUCE_QUANTIFIER_ELIMINATION_METHOD,
							false, true, QeMethodPropertyEditor.class),
//					createDescriptor("eliminateFractions", "Eliminate Fractions",
//							"Choose if KeYmaera should eliminate all fractions before calling reduce.",
//							false, false),
					createDescriptor(
							"rlall",EPropertyConstant.OPTIONS_REDUCE_RLALL,
							false, false),
                    createDescriptor("groebnerBasisSimplification",
                            EPropertyConstant.OPTIONS_REDUCE_GROEBNER_BASIS,
                            false, false),
                    createDescriptor("qepcadFallback",
                            EPropertyConstant.OPTIONS_REDUCE_QEPCAD_FALLBACK,
                            false, false),
					createDescriptor(
							"rlsimpl", EPropertyConstant.OPTIONS_REDUCE_RLSIMPL,
							false, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlnzden", EPropertyConstant.OPTIONS_REDUCE_RLNZDEN,
							false, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlposden", EPropertyConstant.OPTIONS_REDUCE_RLPOSDEN,
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlqeqsc", EPropertyConstant.OPTIONS_REDUCE_rlqeqsc,
							true, false,ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlqesqsc", EPropertyConstant.OPTIONS_REDUCE_rlqesqsc,
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlqedfs", EPropertyConstant.OPTIONS_REDUCE_rlqedfs,
							 true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlqeheu", EPropertyConstant.OPTIONS_REDUCE_rlqeheu,
							true,false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlqepnf",EPropertyConstant.OPTIONS_REDUCE_rlqepnf,
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlcadfac", EPropertyConstant.OPTIONS_REDUCE_rlcadfac,
							true,false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlcadbaseonly", EPropertyConstant.OPTIONS_REDUCE_rlcadbaseonly,
							true,false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlcadprojonly", EPropertyConstant.OPTIONS_REDUCE_rlcadprojonly,
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlcadextonly", EPropertyConstant.OPTIONS_REDUCE_rlcadextonly,
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlcadpartial", EPropertyConstant.OPTIONS_REDUCE_rlcadpartial,
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor("rlcadte", EPropertyConstant.OPTIONS_REDUCE_rlcadte,
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlcadpbfvs", EPropertyConstant.OPTIONS_REDUCE_rlcadpbfvs,
							
							
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlcadfulldimonly",EPropertyConstant.OPTIONS_REDUCE_rlcadfulldimonly,
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlcadtrimtree", EPropertyConstant.OPTIONS_REDUCE_rlcadtrimtree,
							
							
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlcadrawformula", EPropertyConstant.OPTIONS_REDUCE_rlcadrawformula,
							true, false,ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlcadisoallroots", EPropertyConstant.OPTIONS_REDUCE_rlcadisoallroots,
							 true, false,
							ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlcadaproj", EPropertyConstant.OPTIONS_REDUCE_rlcadaproj,
							
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlcadaprojalways", EPropertyConstant.OPTIONS_REDUCE_rlcadaprojalways,
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlcadhongproj", EPropertyConstant.OPTIONS_REDUCE_rlcadhongproj,
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlanuexpsremseq", EPropertyConstant.OPTIONS_REDUCE_rlanuexpsremseq,
							 true, false,ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlanuexgcdnormalize", EPropertyConstant.OPTIONS_REDUCE_rlanuexgcdnormalize,
							true, false, ReduceSwitchPropertyEditor.class),
					createDescriptor(
							"rlanuexsgnopt", EPropertyConstant.OPTIONS_REDUCE_rlanuexsgnopt,
							true, false, ReduceSwitchPropertyEditor.class),

			};
			return pds;
		} catch (IntrospectionException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private static PropertyDescriptor createDescriptor(String propertyName,EPropertyConstant propertyConstants)
			throws IntrospectionException {
		return createDescriptor(propertyName, propertyConstants,false);
	}

	private static PropertyDescriptor createDescriptor(String propertyName,EPropertyConstant propertyConstants, boolean expert)
			throws IntrospectionException {
		return createDescriptor(propertyName,propertyConstants, expert, false);
	}

	private static PropertyDescriptor createDescriptor(String propertyName,EPropertyConstant propertyConstants, boolean expert,
			boolean preferred) throws IntrospectionException {
		return createDescriptor(propertyName, propertyConstants,expert, preferred, null);
	}

	private static PropertyDescriptor createDescriptor(String propertyName,EPropertyConstant propertyConstants, boolean expert,
			boolean preferred, Class<?> propertyEditor)
			throws IntrospectionException {
		PropertyDescriptor result = new PropertyDescriptor(propertyName,
				beanClass);
		result.setDisplayName(propertyConstants.getLabel());
		result.setShortDescription(propertyConstants.getToolTip());
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
	private static PropertyDescriptor createDescriptor(String propertyName,EPropertyConstant propertyConstants, boolean expert,
		boolean preferred, boolean hidden) throws IntrospectionException {
	return createDescriptor(propertyName, propertyConstants, expert, preferred, hidden, null);
        }

	private static PropertyDescriptor createDescriptor(String propertyName,EPropertyConstant propertyConstants, boolean expert,
		boolean preferred, boolean hidden, Class<?> propertyEditor)
		throws IntrospectionException {
	PropertyDescriptor result = new PropertyDescriptor(propertyName,
			beanClass);
	result.setDisplayName(propertyConstants.getLabel());
	result.setShortDescription(propertyConstants.getToolTip());
	result.setExpert(expert);
	result.setHidden(hidden);
	result.setPreferred(preferred);
	if (propertyEditor != null) {
		result.setPropertyEditorClass(propertyEditor);
	}
	return result;
}}
