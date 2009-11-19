/**
 * 
 */
package de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings;

import java.io.File;
import java.io.FileFilter;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 *         The MacOsDefaultProperties class creates and instance of a Property Object containing all
 *         possible default properties for linux platform.
 *         
 *         @author zacho
 */
public class WindowsOsDefaultProperties implements IOsDefaultProperties {

    private Properties props;
    private String sp = File.separator;
    private String mathematicaDefaultPath = "C:"+sp+"Program Files"+sp+"Wolfram"+sp+"Mathematica"+sp+"7.0";
    /**
     * @return the default Properties for linux Operating system
     */
    public Properties getDefaultPropertyList() {

        if (props == null) {
            props = new Properties();
            String temp = getMathematicaCompletePath(mathematicaDefaultPath);
            if(temp != null)
           	mathematicaDefaultPath = temp;
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
    public File[] getsubDirList(File dir){
	 
	    // This filter only returns directories
	    FileFilter fileFilter = new FileFilter() {
	        public boolean accept(File file) {
	            return file.isDirectory();
	        }
	    };
	    if(dir.exists())
		return  dir.listFiles(fileFilter);
	    else
		return null;
	
    }
    public String getMathematicaCompletePath(String currentPath){
	
	File[] file = getsubDirList(new File(currentPath));
	System.out.println(currentPath);
	java.util.Arrays.sort(file);
	
	String tempPath = null;
	if (file != null){
	    for (int i= 0; i < file.length; i++){
		Pattern p = Pattern.compile(".*[Mm]athematica+.?[1-9]+.?[0-9]?+.?[0-9]?");
		Matcher m = p.matcher(file[i].toString()); // get a matcher object
		while (m.find()) {
		    tempPath = m.group();
		}
	    }
	   return  tempPath;
	}
	else
	    return null;
    }

    /**
     * Initialise jlink default path
     */
    public void initJlinkDefault() {

	String jlinkDir = mathematicaDefaultPath+sp+"SystemFiles"+sp+"Link"+sp+"JLink"+sp+"SystemFiles"+sp+"Libraries"+sp+"Windows-x86-64";
        props.put("com.wolfram.jlink.libdir", jlinkDir);
    }

    /**
     * Initialise mathkernel default value
     */

    public void initMathKernelDefault() {
	props.put("[MathematicaOptions]mathematicaPath", mathematicaDefaultPath);
        props.put("[MathematicaOptions]mathKernel",mathematicaDefaultPath+sp+"Executables"+sp+"MathKernel");
    }

    /**
     * Initialise quepcad default path
     */

    public void initQepcadDefault() {

        props.put("[QepcadOptions]qepcadPath", "C:"+sp+"Program Files"+sp+"QEPCAD");
    }

    /**
     * Initialise saclib default path
     */

    public void initSaclibDefault() {

        props.put("[QepcadOptions]saclibPath", "C:"+sp+"Program Files"+sp+"Saclib");
    }

    /**
     * Initialise reduce binary default value
     */
    public void initReduceBinaryDefault() {

        props.put("[ReduceOptions]reduceBinary", "C:"+sp);
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
        
        File olcam = new File("C:"+sp+"Program Files"+sp+"flyspeck"+sp+"hol_light");
	if(!olcam.exists())
	    olcam = new File("C:"+sp); 
        props.put("[HOLLightOptions]ocamlPath", olcam.getAbsolutePath());
    }
    /**
     * Initialise csdp default value
     */
    public void  initcsdpPathDefault(){
	
	File csdp = new File("C:"+sp+"Program Files"+sp+"csdp"+sp+"bin"+sp+"csdp");	
        if (!csdp.exists())
            csdp = new File("C:"+sp);
        props.put("[DLOptions]csdpPath", csdp.getAbsolutePath());
    }
    /**
     * Initialise checkBox default value
     */
    public void initCheckBoxDefault() {
        props.put("[checkBox]flag", "false"); 
    }
 
 
}