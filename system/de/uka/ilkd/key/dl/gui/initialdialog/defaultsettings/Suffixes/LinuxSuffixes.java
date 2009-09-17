/**
 * 
 */
package de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.Suffixes;

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
	
	jLinkSuffixesList.put("SystemFiles"+sp+"Links"+sp + "JLink",
				null);
	jLinkSuffixesList.put("SystemFiles"+sp+"Links",
				"JLink");
	jLinkSuffixesList.put("SystemFiles",
				"Links"+sp+"JLink");	
	jLinkSuffixesList.put("Mathematica"+sp+"7.0",
				"SystemFiles"+sp+"Links"+sp + "JLink");	
	jLinkSuffixesList.put("Mathematica"+File.separator+"6.0",
				"SystemFiles"+sp+"Links"+sp + "JLink");	
	jLinkSuffixesList.put("Mathematica",
				"SystemFiles"+sp+"Links"+sp + "JLink");
	jLinkSuffixesList.put("Wolfram",
				"Mathematica"+sp+"7.0"+sp+"SystemFiles"+sp+"Links"+sp + "JLink");
	jLinkSuffixesList.put("[#DEFAULT#]",
				"SystemFiles"+sp+"Links"+sp + "JLink");	
	
    }
    public HashMap<String, String> getJLinkSuffixesList(){
	return jLinkSuffixesList;
    }
    private void setMathKernelSuffixesList(){
	
	String sp = File.separator;
	mathKernelSuffixesList = new HashMap<String, String>();	
	mathKernelSuffixesList.put("Executables",
				null);
	mathKernelSuffixesList.put("Mathematica",
		               "Executables");
	mathKernelSuffixesList.put("Mathematica"+sp+"7.0",
				"Executables");
	mathKernelSuffixesList.put("Mathematica"+sp+"6.0",
				"Executables");
	mathKernelSuffixesList.put("Wolfram",
		    		"Mathematica"+sp+"7.0"+sp+"Executables");
	mathKernelSuffixesList.put("[#DEFAULT#]",
				"Executables");
	
    }
    public HashMap<String, String> getMathKernelSuffixesList(){
	
	return mathKernelSuffixesList;
    }
}
