/**
 * 
 */
package de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings;

import java.util.Properties;

/** 
 *         The MacOsDefaultProperties class creates and instance of a Property Object containing all
 *         possible default properties for linux platform
 *         
 *         @author zacho
 */
public class MacOsDefaultProperties implements IOsDefaultProperties {

    private Properties props;

    /**
     * @return the default Properties for linux Operating system
     */
    public Properties getDefaultPropertyList() {

        if (props == null) {
            props = new Properties();
            initJlinkDefault();
            initMathKernelDefault();
            initQepcadDefault();
            initSaclibDefault();
            initReduceBinaryDefault();
            initCheckBoxDefault();
        }
        return props;
    }

    /**
     * Initialise jlink default path
     */
    public void initJlinkDefault() {// Change name

        String jlinkDir = System.getProperty("user.home");
        if (jlinkDir == null)
            jlinkDir = "/";
        props.put("com.wolfram.jlink.libdir", jlinkDir);
    }

    /**
     * Initialise mathkernel default value
     */

    public void initMathKernelDefault() {
        props.put("[MathematicaOptions]mathKernel",
                "/Applications/Mathematica.app");
    }

    /**
     * Initialise quepcad default path
     */

    public void initQepcadDefault() {

        props.put("[QepcadOptions]qepcadPath", "/Applications/QEPCAD");
    }

    /**
     * Initialise saclib default path
     */

    public void initSaclibDefault() {

        props.put("[QepcadOptions]saclibPath", "/Applications/Saclib");
    }

    /**
     * Initialise reduce binary default value
     */
    public void initReduceBinaryDefault() {

        props.put("[ReduceOptions]reduceBinary", "/Applications");
    }

    /**
     * Initialise checkBox default value
     */
    public void initCheckBoxDefault() {
        props.put("[checkBox]flag", "false"); 
    }
}
