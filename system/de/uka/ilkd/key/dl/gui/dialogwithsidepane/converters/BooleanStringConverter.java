package de.uka.ilkd.key.dl.gui.dialogwithsidepane.converters;

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
    public Boolean toPropertyEditorValue(String string) {      
       bool = Boolean.valueOf(string);
        return bool;
    }

    /**
     * @return the string value given a Bolean value
     */
    public String toStringValue(Object inbool) {
        str = String.valueOf(inbool);
        return str;
    }

}
