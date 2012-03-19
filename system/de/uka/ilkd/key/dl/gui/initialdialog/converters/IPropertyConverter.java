/*******************************************************************************
 * Copyright (c) 2010 Zacharias Mokom.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Zacharias Mokom - initial API and implementation
 ******************************************************************************/
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
     *         the corresponding Property Editor.
     * @param string : The string to be converted.
     */
    public Object toPropertyEditorValue(String string);
    
    /**
     * @return to corresponding String object suitable to store
     *         in property file
     *@param obj : The Object to be converted to string value.
     */
    public String toStringValue(Object obj);

}
