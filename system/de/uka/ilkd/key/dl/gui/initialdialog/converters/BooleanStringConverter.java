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
