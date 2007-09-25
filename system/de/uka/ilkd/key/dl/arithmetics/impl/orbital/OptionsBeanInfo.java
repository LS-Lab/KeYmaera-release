package de.uka.ilkd.key.dl.arithmetics.impl.orbital;

import java.beans.*;
import orbital.awt.TaggedPropertyEditorSupport;

public class OptionsBeanInfo extends SimpleBeanInfo {
    Class beanClass = Options.class;

    public OptionsBeanInfo() {}

    public BeanDescriptor getBeanDescriptor() {
	BeanDescriptor d = new BeanDescriptor(beanClass);
	d.setDisplayName("Orbital Options");
	d.setShortDescription("Adjusts values for the computer algebra parts of the Orbital engine");
	return d;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor _representation = new PropertyDescriptor("representation", beanClass);
            _representation.setDisplayName("number representation");
            _representation.setShortDescription("the internal representation of numbers. 0 means unlimited");
            _representation.setPropertyEditorClass(RepresentationPropertyEditor.class);
            PropertyDescriptor _precision = new PropertyDescriptor("precision", beanClass);
            _precision.setDisplayName("precision");
            _precision.setShortDescription("the number of digits to be used for an operations with results being rounded to this precision");
            PropertyDescriptor[] pds = new PropertyDescriptor[] {
                _representation, _precision
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
