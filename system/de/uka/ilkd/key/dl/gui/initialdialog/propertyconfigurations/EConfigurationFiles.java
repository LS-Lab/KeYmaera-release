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
package de.uka.ilkd.key.dl.gui.initialdialog.propertyconfigurations;

import java.io.File;

import de.uka.ilkd.key.gui.configuration.PathConfig;

/**
 * The EConfigurationFiles Enumeration describes the files where the properties
 * are stored
 * 
 * @author zacho
 */
public enum EConfigurationFiles {

    KEY_PROPERTY_FILE(PathConfig.KEY_CONFIG_DIR + File.separator
                                            + "proof-settings.props"),
    WEBSTART_FILE(PathConfig.KEY_CONFIG_DIR
                                            + File.separator + "webstart-math.props"),
    STARTUP_PROPERTY_FILE(PathConfig.KEY_CONFIG_DIR + File.separator
            + "startup.props");
    private String fileName;

    EConfigurationFiles(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 
     * @return configuration file (Property file) name (as a string)that
     *         contains the needed property
     */
    public String getFileName() {
        return fileName;
    }
}
