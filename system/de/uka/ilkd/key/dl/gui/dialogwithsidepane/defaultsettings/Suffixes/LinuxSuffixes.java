/**
 * 
 */
package de.uka.ilkd.key.dl.gui.dialogwithsidepane.defaultsettings.Suffixes;

import java.io.File;
import java.util.HashMap;
/**
 * @author zacho
 *
 */
public class LinuxSuffixes{

    public static final LinuxSuffixes INSTANCE = new LinuxSuffixes();
    HashMap<String, String> jLinkSuffixesList; 
    HashMap<String, String> mathKernelSuffixesList; 
    
    public LinuxSuffixes(){
	setjLinkSuffixesList();
	setMathKernelSuffixesList();
	
    }
    private void setjLinkSuffixesList(){
	
	jLinkSuffixesList = new HashMap<String, String>();	
	String sp = File.separator;
	
	jLinkSuffixesList.put("SystemFiles"+sp+"Links",
				"JLink");
	jLinkSuffixesList.put("SystemFiles",
				"Links"+sp+"JLink");	
	jLinkSuffixesList.put("Mathematica",
				"SystemFiles"+sp+"Links"+sp + "JLink");
	jLinkSuffixesList.put("Wolfram",
				"Mathematica"+sp+"SystemFiles"+sp+"Links"+sp + "JLink");
	jLinkSuffixesList.put("[#DEFAULT#]",
				"SystemFiles"+sp+"Links"+sp + "JLink");	
	//Suffixes for differents mathematica versions
	for (MathematicaVersions version: MathematicaVersions.values()){
	    
	    jLinkSuffixesList.put("Mathematica" + sp + version.getVersionEndString(),
		    "SystemFiles"+ sp + "Links" + sp + "JLink");	    
	}
	
	
    }
    public HashMap<String, String> getJLinkSuffixesList(){
	return jLinkSuffixesList;
    }
    private void setMathKernelSuffixesList(){
	
	String sp = File.separator;
	mathKernelSuffixesList = new HashMap<String, String>();	
	mathKernelSuffixesList.put("Mathematica",
		               "Executables");
	mathKernelSuffixesList.put("Wolfram",
		    		"Mathematica"+sp+"Executables");
	mathKernelSuffixesList.put("[#DEFAULT#]",
				"Executables");
	//Suffixes for differents mathematica versions
	for (MathematicaVersions version: MathematicaVersions.values()){
	    
	    mathKernelSuffixesList.put("Mathematica" + sp + version.getVersionEndString(),
		    "Executables");	    
	}

    }
    public String getMathkernelDefaultSuffix(){
	return "Executables";
	
    }
    public String getJLinkDefaultSuffix(){
	return "SystemFiles"+File.separator+"Links"+File.separator + "JLink";
	
    }
    public HashMap<String, String> getMathKernelSuffixesList(){
	
	return mathKernelSuffixesList;
    }
}
