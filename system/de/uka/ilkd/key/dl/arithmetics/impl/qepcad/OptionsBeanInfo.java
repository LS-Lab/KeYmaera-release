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
package de.uka.ilkd.key.dl.arithmetics.impl.qepcad;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import de.uka.ilkd.key.dl.options.DirectoryPropertyEditor;

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
		d.setDisplayName("Qepcad Options");
		d.setShortDescription("Adjusts values for the Qepcad interface");
		return d;
	}

	/*@Override*/
	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			// PropertyDescriptor _usage = new PropertyDescriptor("usage",
			// beanClass, "getUsage", "setUsage");
			// _usage.setDisplayName("usage");
			// _usage.setShortDescription("primary usage descriptor");
			// _usage.setPropertyEditorClass(UsagePropertyEditor.class);

			PropertyDescriptor[] pds = new PropertyDescriptor[] {
//					createDescriptor(
//							"qepcadBinary",
//							"Qepcad Executable",
//							"The Qepcad executable including the complete path name if it is not located in the system PATH",
//							true, false, FilePropertyEditor.class),
					createDescriptor(
							"qepcadPath",
							"Qepcad Path",
							"The path to the qepcad installation needed to setup the correct environment for the tool (it must contain bin/qepcad binary)",
							true, false, DirectoryPropertyEditor.class),
					createDescriptor(
							"saclibPath",
							"Saclib Path",
							"The path to the saclib installation needed to setup the correct environment for Qepcad",
							true, false, DirectoryPropertyEditor.class),

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
}
