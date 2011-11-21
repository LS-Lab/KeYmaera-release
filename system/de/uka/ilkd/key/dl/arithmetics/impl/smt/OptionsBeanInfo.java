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
package de.uka.ilkd.key.dl.arithmetics.impl.smt;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

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
		d.setDisplayName("Z3 Options");
		d.setShortDescription("Adjusts values for the Z3 interface");
		return d;
	}

	/* @Override */
	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			PropertyDescriptor[] pds = new PropertyDescriptor[] {
					createDescriptor("z3Binary",
							EPropertyConstant.Z3_OPTIONS_Z3_BINARY, false,
							false, true, FilePropertyEditor.class),
					createDescriptor("prenexForm",
							EPropertyConstant.Z3_OPTIONS_PRENEX_FORM),
					createDescriptor(
							"elimExistentialQuantifierPrefix",
							EPropertyConstant.Z3_OPTIONS_ELIMINATE_EXISTENTIAL_PREFIX),

			};
			return pds;
		} catch (IntrospectionException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private static PropertyDescriptor createDescriptor(String propertyName,
			EPropertyConstant propertyConstants) throws IntrospectionException {
		return createDescriptor(propertyName, propertyConstants, false);
	}

	private static PropertyDescriptor createDescriptor(String propertyName,
			EPropertyConstant propertyConstants, boolean expert)
			throws IntrospectionException {
		return createDescriptor(propertyName, propertyConstants, expert, false);
	}

	private static PropertyDescriptor createDescriptor(String propertyName,
			EPropertyConstant propertyConstants, boolean expert,
			boolean preferred) throws IntrospectionException {
		return createDescriptor(propertyName, propertyConstants, expert,
				preferred, null);
	}

	private static PropertyDescriptor createDescriptor(String propertyName,
			EPropertyConstant propertyConstants, boolean expert,
			boolean preferred, Class<?> propertyEditor)
			throws IntrospectionException {
		return createDescriptor(propertyName, propertyConstants, expert,
				preferred, false, null);
	}

	private static PropertyDescriptor createDescriptor(String propertyName,
			EPropertyConstant propertyConstants, boolean expert,
			boolean preferred, boolean hidden) throws IntrospectionException {
		return createDescriptor(propertyName, propertyConstants, expert,
				preferred, hidden, null);
	}

	private static PropertyDescriptor createDescriptor(String propertyName,
			EPropertyConstant propertyConstants, boolean expert,
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
	}
}
