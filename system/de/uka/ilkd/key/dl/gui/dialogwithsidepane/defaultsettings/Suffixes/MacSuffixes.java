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
public class MacSuffixes  {
    public static final MacSuffixes INSTANCE = new MacSuffixes();
    HashMap<String, String> jLinkSuffixesList; 
    HashMap<String, String> mathKernelSuffixesList; 
    
    public MacSuffixes(){
	setjLinkSuffixesList();
	setMathKernelSuffixesList();
	
    }
    private void setjLinkSuffixesList(){

     String JVMBit = "-64";
     if (!System.getProperty("java.vm.name").contains("64-Bit")) 
	    	JVMBit = "";
     String sp = File.separator;
     jLinkSuffixesList = new HashMap<String, String>();	

     jLinkSuffixesList.put("SystemFiles"+sp+"Links"+sp+"JLink"+sp+"SystemFiles"+sp+"Libraries",
	     		"MacOSX-x86-"+sp+JVMBit);
     
     jLinkSuffixesList.put("SystemFiles"+sp+"Links"+sp+"JLink"+sp+"SystemFiles",
	     		"Libraries"+sp+"MacOSX-x86-"+sp+JVMBit);
     
     jLinkSuffixesList.put("SystemFiles"+sp+"Links"+sp+"JLink",
	     		"SystemFiles"+sp+"Libraries"+sp+"MacOSX-x86-"+sp+JVMBit);
     
     jLinkSuffixesList.put("Mathematica.app"+sp+"SystemFiles"+sp+"Links",
	      		"JLink"+sp+"SystemFiles"+sp+"Libraries"+sp+"MacOSX-x86-"+sp+JVMBit);
     jLinkSuffixesList.put("Mathematica.app"+sp+"SystemFiles",
	     		"Links"+sp+"JLink"+sp+"SystemFiles"+sp+"Libraries"+sp+"MacOSX-x86-"+sp+JVMBit);
     jLinkSuffixesList.put("Mathematica.app",
	     		"SystemFiles"+sp+"Links"+sp+"JLink"+sp+"SystemFiles"+sp+"Libraries"+sp+"MacOSX-x86-"+sp+JVMBit);
     jLinkSuffixesList.put("Applications",
	     		"Mathematica.app"+sp+"SystemFiles"+sp+"Links"+sp+"JLink"+sp+"SystemFiles"+sp+"Libraries"+sp+"MacOSX-x86-"+sp+JVMBit);

     jLinkSuffixesList.put("[#DEFAULT#]",
	     "SystemFiles"+sp+"Links"+sp+"JLink"+sp+"SystemFiles"+sp+"Libraries"+sp+"MacOSX-x86-"+sp+JVMBit);	
    }	
    
    public HashMap<String, String> getJLinkSuffixesList(){
	
	return jLinkSuffixesList;
    }
    private void setMathKernelSuffixesList(){

	 String sp = File.separator;
	mathKernelSuffixesList = new HashMap<String, String>();	

	mathKernelSuffixesList.put("Mathematica.app"+sp+"Contents"+sp+"MacOS",
				"MathKernel");
	mathKernelSuffixesList.put("Mathematica.app"+sp+"Contents",
				"MacOS"+sp+"MathKernel");
	
	mathKernelSuffixesList.put("Mathematica.app",
				"Contents"+sp+"MacOS"+sp+"MathKernel");
	mathKernelSuffixesList.put("Applications",
				"Mathematica.app"+sp+"Contents"+sp+"MacOS"+sp+"MathKernel");
	mathKernelSuffixesList.put("[#DEFAULT#]",
				"Contents"+sp+"MacOS"+sp+"MathKernel");	
	
    }
    public HashMap<String, String> getMathKernelSuffixesList(){
	
	return mathKernelSuffixesList;
    }
}

