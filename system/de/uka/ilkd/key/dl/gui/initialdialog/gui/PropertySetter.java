/*******************************************************************************
 * Copyright (c) 2012 Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jan-David Quesel - initial API and implementation
 ******************************************************************************/
package de.uka.ilkd.key.dl.gui.initialdialog.gui;

import java.beans.PropertyEditor;
import java.io.File;

/**
 * @author jdq
 *
 */
public class PropertySetter {
    
    private PropertyEditor propertyEditor;
    private String fileName;

    /**
     * 
     */
    public PropertySetter(String fileName) {
        this.fileName = fileName;
    }
    
    public void setProperty(String prop) {
        System.out.println("Setting property to " + prop );
        propertyEditor.setAsText(prop);
    }
    
    public boolean filterFilename(File file) {
        if(file.getName().equals(fileName)) {
            file.setExecutable(true);
            return true;
        }
        return false;
    }

    /**
     * @param propertyEditor2
     */
    public void setPropertyEditor(PropertyEditor propertyEditor) {
        this.propertyEditor = propertyEditor;
    }
    
}
