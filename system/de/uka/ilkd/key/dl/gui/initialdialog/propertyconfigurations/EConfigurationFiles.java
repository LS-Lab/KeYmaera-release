package de.uka.ilkd.key.dl.gui.initialdialog.propertyconfigurations;

import java.io.File;

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
                                            + File.separator + "webstart-math.props");
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
