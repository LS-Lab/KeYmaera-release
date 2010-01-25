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
package de.uka.ilkd.key.dl.arithmetics.impl.orbital;

import java.beans.*;

import orbital.awt.TaggedPropertyEditorSupport;

public class OptionsBeanInfo extends SimpleBeanInfo {
    Class beanClass = Options.class;

    public OptionsBeanInfo() {}

    public BeanDescriptor getBeanDescriptor() {
	BeanDescriptor d = new BeanDescriptor(beanClass);
	d.setDisplayName("Orbital Options");
	d.setShortDescription("Adjusts options for the computer algebra system engine of the Orbital library");
	return d;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor _representation = new PropertyDescriptor("representation", beanClass);
            _representation.setDisplayName("number representation");
            _representation.setShortDescription("the internal representation of numbers.");
            _representation.setPropertyEditorClass(RepresentationPropertyEditor.class);
            _representation.setHidden(true);
            PropertyDescriptor _precision = new PropertyDescriptor("precision", beanClass);
            _precision.setDisplayName("precision");
            _precision.setShortDescription("the number of digits to be used for a operations with results being rounded to this precision. 0 means unlimited");
            PropertyDescriptor _sparse = new PropertyDescriptor("sparsePolynomials", beanClass);
            _sparse.setDisplayName("sparse polynomials");
            _sparse.setShortDescription("whether to use sparse or dense polynomial representations");
            PropertyDescriptor[] pds = new PropertyDescriptor[] {
                _representation, _precision, _sparse
            };
            return pds;
        } catch (IntrospectionException ex) {
            ex.printStackTrace();
            return null;
        } 
    } 

    public static class RepresentationPropertyEditor extends TaggedPropertyEditorSupport {
        public RepresentationPropertyEditor() {
            super(new String[] {
                "big", "machine", "dynamic"
            }, new String[] {
                "big", "machine", "dynamic"
            }, new String[] {
                "\"big\"", "\"machine\"", "\"dynamic\""
            });
        }
    }

}
