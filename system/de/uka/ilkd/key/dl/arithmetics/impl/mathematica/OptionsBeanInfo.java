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
    
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            // PropertyDescriptor _usage = new PropertyDescriptor("usage",
            // beanClass, "getUsage", "setUsage");
            // _usage.setDisplayName("usage");
            // _usage.setShortDescription("primary usage descriptor");
            // _usage.setPropertyEditorClass(UsagePropertyEditor.class);

            PropertyDescriptor[] pds = new PropertyDescriptor[] {
                    createDescriptor(
                            "useEliminateList",
                            "use elimination list",
                            "choose if the list of variables should be passed to Mathematica",
                            true, false),
                    createDescriptor(
                            "quantifierEliminationMethod",
                            "method for quantifier elimination",
                            "the mathematica method that is used to perform quantifier elimination",
                            true, false, QuantifierEliminationMethodPropertyEditor.class)

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
