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
/**
 * 
 */
package de.uka.ilkd.key.dl.arithmetics.impl.mathematica;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.ArrayList;

import orbital.awt.TaggedPropertyEditorSupport;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.Options.QuantifierEliminationMethod;
import de.uka.ilkd.key.dl.options.DirectoryPropertyEditor;
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
		d.setDisplayName("Mathematica Options");
		d.setShortDescription("Adjusts values for the Mathematica interface");
		return d;
	}

	/* @Override */
	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			// PropertyDescriptor _usage = new PropertyDescriptor("usage",
			// beanClass, "getUsage", "setUsage");
			// _usage.setDisplayName("usage");
			// _usage.setShortDescription("primary usage descriptor");
			// _usage.setPropertyEditorClass(UsagePropertyEditor.class);

			PropertyDescriptor[] pds = new PropertyDescriptor[] {
					createDescriptor("mathKernel", "MathKernel path",
							"the path to the MathKernel binary", true, false,
							FilePropertyEditor.class),
					createDescriptor(
							"jLinkLibDir",
							"J/Link native dir",
							"the path where the J/Link natives are located. Restart is required when this setting is changed.",
							true, false, DirectoryPropertyEditor.class),
					createDescriptor(
							"useEliminateList",
							"elimination list",
							"choose if the list of elimination variables should be passed to Mathematica's Reduce",
							true, false),
					createDescriptor(
							"convertDecimalsToRationals",
							"convert decimals",
							"choose if decimal fraction entered by the user should be converted into a rational representation (q/r)",
							true, false),
					createDescriptor(
							"memoryConstraint",
							"memory limit",
							"the maximum memory used by the Mathematica server [in bytes], -1 means no limit",
							true, false),
					createDescriptor(
							"quantifierEliminationMethod",
							"quantifier elimination",
							"the Mathematica method that is used to perform quantifier elimination",
							true, false,
							QuantifierEliminationMethodPropertyEditor.class), };
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

	public static class QuantifierEliminationMethodPropertyEditor extends
			TaggedPropertyEditorSupport {

		private static String[] getNames() {
			java.util.List<String> names = new ArrayList<String>();
			for (QuantifierEliminationMethod r : QuantifierEliminationMethod
					.values()) {
				names.add(r.toString());
			}
			return names.toArray(new String[0]);
		}

		public QuantifierEliminationMethodPropertyEditor() {
			super(getNames(), QuantifierEliminationMethod.values());
		}
	}
}
