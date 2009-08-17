/**
 * 
 */
package de.uka.ilkd.key.dl.gui.initialdialog.converters;

/**
 *         Converts a given string object to a Boolean object and also converts a
 *         given Bolean Object to corresponding String object suitable to store
 *         in property file.
 *         
 *         @author zacho
 */
public class BooleanStringConverter implements IPropertyConverter {

    private Boolean bool;
    private String str;

    public BooleanStringConverter() {

    }

    /**
     * @return the Boolean Object given a String agurment
     */
    public Boolean toPropertyEditorValue(String str) {
       
        this.bool = Boolean.valueOf(str);
        return bool;
    }

    /**
     * @return the string value given a Bolean value
     */
    public String toStringValue(Object bool) {
        this.str = String.valueOf(bool);
        return str;
    }

}
