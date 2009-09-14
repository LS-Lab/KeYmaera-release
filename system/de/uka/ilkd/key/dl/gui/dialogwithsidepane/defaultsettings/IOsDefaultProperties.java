/**
 * 
 */
package de.uka.ilkd.key.dl.gui.dialogwithsidepane.defaultsettings;

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
    public String getMathKernelSuffixed(String actualPath);
    public String getJLinkSuffixed(String actualPath);
    public Boolean isMathematicaPathSetdefault();
  
}