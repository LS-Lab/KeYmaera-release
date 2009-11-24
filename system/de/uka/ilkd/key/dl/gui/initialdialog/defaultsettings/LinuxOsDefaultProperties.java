package de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings;

import java.io.File;
import java.io.FileFilter;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 *         The LinuxOsDefaultProperties class creates and instance of a Property
 *         Object containing all possible default properties for linux platform.
 *         
 *          @author zacho
 * 
 */
public class LinuxOsDefaultProperties implements IOsDefaultProperties {

    private Properties props;
    private String sp = File.separator;
    private String mathematicaDefaultPath = sp+"usr"+sp+"local"+sp+"Wolfram"+sp+"Mathematica";
    
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
            initReduceBinaryDefault();
            initcsdpPathDefault();
            initHOLLight();
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
		
	String tempPath = null;
	if (file != null){
	    java.util.Arrays.sort(file);
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

       String jlinkDir = mathematicaDefaultPath+sp+"SystemFiles"+sp+"Links"+sp + "JLink" +sp+ "SystemFiles" + sp+ "Libraries"+sp+"Linux-x86-64";
        
        props.put("com.wolfram.jlink.libdir", jlinkDir);
    }

    /**
     * Initialise mathkernel default value
     */

    public void initMathKernelDefault() {
	
	props.put("[MathematicaOptions]mathematicaPath", mathematicaDefaultPath);
        props.put("[MathematicaOptions]mathKernel", mathematicaDefaultPath+sp+"Executables"+sp+"MathKernel");
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
        
        File olcam = new File(sp+"usr"+sp+"bin"+sp+"ocaml");
	if(!olcam.exists())
	    olcam = new File(System.getProperty("user.home")); 
	if(!olcam.exists())
	    olcam = new File("/"); 
        props.put("[HOLLightOptions]ocamlPath", olcam.getAbsolutePath());
    }
    /**
     * Initialise csdp default value
     */
    public void  initcsdpPathDefault(){
	
	File csdp = new File(sp+"user"+sp+"bin"+sp+"csdp");	
        if (!csdp.exists())
            csdp = new File(System.getProperty("user.home"));
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