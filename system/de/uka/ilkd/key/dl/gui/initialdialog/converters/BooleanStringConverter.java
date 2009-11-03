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
     * @param string to be converted
     */
    public Boolean toPropertyEditorValue(String string) {      
       bool = Boolean.valueOf(string);
        return bool;
    }

    /**
     * @return the corresponding string value 
     * @param bool : Boolean to be converted
     */
    public String toStringValue(Object bool) {
        str = String.valueOf(bool);
        return str;
    }

}
