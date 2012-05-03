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
package de.uka.ilkd.key.dl.arithmetics.impl.ch;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.ArrayList;

import orbital.awt.TaggedPropertyEditorSupport;
import de.uka.ilkd.key.dl.arithmetics.impl.ch.Options.CHMode;
import de.uka.ilkd.key.dl.arithmetics.impl.reduce.OptionsBeanInfo.ReduceSwitchPropertyEditor;
import de.uka.ilkd.key.dl.options.DirectoryPropertyEditor;
import de.uka.ilkd.key.dl.options.EPropertyConstant;
import de.uka.ilkd.key.dl.options.FilePropertyEditor;

public class OptionsBeanInfo extends SimpleBeanInfo {
	private static final Class<Options> beanClass = Options.class;

	public OptionsBeanInfo() {
	}

	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor d = new BeanDescriptor(beanClass);
		d.setDisplayName("Cohen-Hormander Options");
		d.setShortDescription("Adjusts values for the Cohen-Hormander interface");
		return d;
	}
	
	/*@Override*/
	public PropertyDescriptor[] getPropertyDescriptors() {
		try{
			PropertyDescriptor[] pds = new PropertyDescriptor[] {
					createDescriptor(
							"eliminatorMode",EPropertyConstant.COHENHORMANDER_OPTIONS_MODE,
							false, false, CHModePropertyEditor.class)
					
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

	public static class CHModePropertyEditor extends
	TaggedPropertyEditorSupport {
		public CHModePropertyEditor() {
			super(getNames(CHMode.values()), CHMode.values());
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
	}

	
}
