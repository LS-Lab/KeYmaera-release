/**
 * 
 */
package de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings;

import java.util.Properties;

/**
 *         The IOsDefaultProperties Interface class for default properties for
 *         particular Operating system.
 *         
 *         @author zacho
 * 
 */
public interface IOsDefaultProperties {

    /**
     * @return the initial properties list as a Property Object
     */
    public Properties getDefaultPropertyList();
    public String getMathKernelSuffix(String actualPath);
    public String getJLinkSuffix(String actualPath);
  
}