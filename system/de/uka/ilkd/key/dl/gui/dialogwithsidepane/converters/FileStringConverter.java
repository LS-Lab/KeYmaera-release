package de.uka.ilkd.key.dl.gui.dialogwithsidepane.converters;

import java.io.File;

/**
 *         Converts a given string object to a File object and also converts a
 *         given File Object to corresponding String object suitable to store
 *         in property file.
 *      
 *         @author zacho
 */
public class FileStringConverter implements IPropertyConverter {

    private File file;
    private String str;

    public FileStringConverter() {

    }

    /**
     * @return the file Object given a String agurment
     */
    
    public File toPropertyEditorValue(String string) {
        this.file = new File(string);
        return file;
    }

    /**
     * @return the string given a File Object
     */
    public String toStringValue(Object file) {
        this.str = ((File) file).getAbsolutePath();
        return str;
    }

}
