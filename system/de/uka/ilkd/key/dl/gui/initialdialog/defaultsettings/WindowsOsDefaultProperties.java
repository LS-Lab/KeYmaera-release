/**
 * 
 */
package de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings;

import java.io.File;
import java.util.Properties;
/**
 *         The MacOsDefaultProperties class creates and instance of a Property Object containing all
 *         possible default properties for linux platform.
 *         
 *         @author zacho
 */
public class WindowsOsDefaultProperties implements IOsDefaultProperties {

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
            initHOLLight();
            initcsdpPathDefault();
            initReduceBinaryDefault();
            initCheckBoxDefault();
        }
        return props;
    }

    /**
     * Initialise jlink default path
     */
    public void initJlinkDefault() {

        props.put("com.wolfram.jlink.libdir", "c:\\");
    }

    /**
     * Initialise mathkernel default value
     */

    public void initMathKernelDefault() {
	props.put("[MathematicaOptions]mathematicaPath", "C:\\Program Files\\Wolfram\\Mathematica\\7.0");
        props.put("[MathematicaOptions]mathKernel",
                "C:\\Program Files\\Wolfram\\Mathematica\\7.0");
    }

    /**
     * Initialise quepcad default path
     */

    public void initQepcadDefault() {

        props.put("[QepcadOptions]qepcadPath", "C:\\Program Files\\QEPCAD");
    }

    /**
     * Initialise saclib default path
     */

    public void initSaclibDefault() {

        props.put("[QepcadOptions]saclibPath", "C:\\Program Files\\Saclib");
    }

    /**
     * Initialise reduce binary default value
     */
    public void initReduceBinaryDefault() {

        props.put("[ReduceOptions]reduceBinary", "C:\\");
    }


    /**
     * Initialise HOL light paths default values
     */
    public void  initHOLLight(){
	String hol = System.getProperty("user.home");
        if (hol == null)
            hol = "C:\\";
        props.put("[HOLLightOptions]harrisonqePath", hol);
        props.put("[HOLLightOptions]hollightPath", hol);
        
        File olcam = new File("C:\\Program Files\\flyspeck\\hol_light");
	if(!olcam.exists())
	    olcam = new File("C:\\"); 
        props.put("[HOLLightOptions]ocamlPath", olcam.getAbsolutePath());
    }
    /**
     * Initialise csdp default value
     */
    public void  initcsdpPathDefault(){
	
	File csdp = new File("C:\\Program Files\\csdp\\bin\\csdp");	
        if (!csdp.exists())
            csdp = new File("C:\\");
        props.put("[DLOptions]csdpPath", csdp.getAbsolutePath());
    }
    /**
     * Initialise checkBox default value
     */
    public void initCheckBoxDefault() {
        props.put("[checkBox]flag", "false"); 
    }
 
 
}