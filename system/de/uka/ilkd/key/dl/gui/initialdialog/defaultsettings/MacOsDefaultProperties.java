/**
 * 
 */
package de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings;

import java.io.File;
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
            initHOLLight();
            initcsdpPathDefault();
            initCheckBoxDefault();
        }
        return props;
    }

    /**
     * Initialise jlink default path
     */
    public void initJlinkDefault() {
	
	String jlinkDir = "/Applications/Mathematica.app/SystemFiles/Links/JLink/SystemFiles/Libraries/MacOSX-x86-64";
        props.put("com.wolfram.jlink.libdir", jlinkDir);
    }

    /**
     * Initialise mathkernel default value
     */

    public void initMathKernelDefault() {
        props.put("[MathematicaOptions]mathKernel",
                "/Applications/Mathematica.app/Contents/MacOS/MathKernel");
    }

    /**
     * Initialise quepcad default path
     */

    public void initQepcadDefault() {
	String qpath = System.getenv("qe");
        if (qpath == null) {
            qpath = System.getProperty("user.home");
            if (qpath == null)
                qpath = "/";
        }
        props.put("[QepcadOptions]qepcadPath", qpath);

    }

    /**
     * Initialise saclib default path
     */

    public void initSaclibDefault() {

	 String spath = System.getenv("saclib");
	        if (spath == null) {
	            spath = System.getProperty("user.home");
	            if (spath == null)
	                spath = "/";
	        }
	 props.put("[QepcadOptions]saclibPath", spath);
    }

    /**
     * Initialise reduce binary default value
     */
    public void initReduceBinaryDefault() {

	 String rpath = System.getProperty("user.home");
	        if (rpath == null)
	            rpath = "/";
	        props.put("[ReduceOptions]reduceBinary", rpath);
    }
    /**
     * Initialise HOL light paths default values
     */
    public void  initHOLLight(){
	String hol = System.getProperty("user.home");
        if (hol == null)
            hol = "/";
        props.put("[HOLLightOptions]harrisonqePath", hol);
        props.put("[HOLLightOptions]hollightPath", hol);
        props.put("[HOLLightOptions]ocamlPath", hol);
    }
    /**
     * Initialise csdp default value
     */
    public void  initcsdpPathDefault(){
	
	File csdp = new File(System.getProperty("user.home"));
        if (!csdp.exists()) 
            csdp = new File("/");
        props.put("[DLOptions]csdpPath", csdp.getAbsolutePath());
    }

    /**
     * Initialise checkBox default value
     */
    public void initCheckBoxDefault() {
        props.put("[checkBox]flag", "false"); 
    }
}
