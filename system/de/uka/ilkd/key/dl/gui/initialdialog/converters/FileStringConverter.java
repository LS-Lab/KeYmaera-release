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

import java.io.File;

/**
 *         Converts a given string object to a File object and also converts a
 *         given File Object to corresponding String object suitable to store
 *         in property file.
 *      
 *         @author zacho
 */
public class FileStringConverter implements IPropertyConverter {

    public FileStringConverter() {

    }

    /**
     * @return the file Object
     * @param string : String to be converted 
     */
    
    public File toPropertyEditorValue(String string) {
        return string == null ? null : new File(string);
    }

    /**
     * @return the string given a File Object
     * @param inputFile : the file to be converted
     */
    public String toStringValue(Object inputFile) {
        return ((File) inputFile).getAbsolutePath();
    }

}
