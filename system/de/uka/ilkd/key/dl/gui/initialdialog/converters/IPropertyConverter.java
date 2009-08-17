/**
 * 
 */
package de.uka.ilkd.key.dl.gui.initialdialog.converters;

/** 
 *         Interface of converters :
 *        - The call of toPropertyEditorValue(String arg)
 *         converts argument to corresponding object that can be accepted by
 *         the corresponding Property Editor.
 *         -The call of toStringValue(Object obj)
 *         converts argument to corresponding String object suitable to store
 *         in property file.
 *         
 *          @author zacho
 */
public interface IPropertyConverter {
    
    /**
     * @return the corresponding object that can be accepted by
     *         the corresponding Property Editor
     */
    public Object toPropertyEditorValue(String str);
    
    /**
     * @return to corresponding String object suitable to store
     *         in property file
     */
    public String toStringValue(Object obj);

}
