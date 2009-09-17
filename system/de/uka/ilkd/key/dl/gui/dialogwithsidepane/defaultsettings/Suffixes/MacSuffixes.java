package de.uka.ilkd.key.dl.gui.dialogwithsidepane.defaultsettings.Suffixes;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 *   
 *    This Object implements ISuffixes in Mac Operating systems.
 *    
 * @author zacho
 *
 */
public class MacSuffixes implements ISuffixes {
   
    String JVMBit = "-64";
    @Override
    public Boolean isPossibleMathematicaPath(String mathematicaPath) {
	// TODO Auto-generated method stub
	return Pattern.matches(".*[Mm]athematica+.?[1-9]?+.?[0-9]?+.?[0-9]?(.app)?",mathematicaPath);
    }
    @Override
    public Boolean containsMathematicaPathPrefix(String mathematicaPath) {
	// TODO Auto-generated method stub
	return Pattern.matches(".*[Mm]athematica+.?[1-9]?+.?[0-9]?+.?[0-9]?(.app)?.*",mathematicaPath);
    }
    @Override
    public String getMathematicaPathPrefix(String mathematicaPath) {
	// TODO Auto-generated method stub
    	Pattern p = Pattern.compile(".*[Mm]athematica+.?[1-9]?+.?[0-9]?+.?[0-9]?(.app)?");
    	Matcher m = p.matcher(mathematicaPath); // get a matcher object
    	String prefix = null;
    	while (m.find()) {
    	    prefix = m.group();
    	}
    	return prefix;
    }
    
    @Override
    public String getJLinkDefaultSuffix() {
	// TODO Auto-generated method stub
	String sp = File.separator;
	if (!System.getProperty("java.vm.name").contains("64-Bit")) 
	    	JVMBit = "";
	return  "SystemFiles"+sp+"Links"+sp+"JLink"+sp+"SystemFiles"+sp+"Libraries"+sp+"MacOSX-x86-"+sp+JVMBit;
    }

    @Override
    public String getJLinkSuffix(String mathematicaPath) {
	// TODO Auto-generated method stub

	String sp = File.separator;
	if (!System.getProperty("java.vm.name").contains("64-Bit")) 
	    	JVMBit = "";
	Pattern p = Pattern.compile(File.separator);
	String[] values = p.split(mathematicaPath);
	
	if(values[values.length - 1].equals("JLink"))
	    return "";
	
	Pattern p2 = Pattern.compile(values[values.length - 1] + ".*");
	String suffix = null;
	Matcher m = p2.matcher("SystemFiles"+sp+"Links"+sp+"JLink"+sp+"SystemFiles"+sp+"Libraries"+sp+"MacOSX-x86-"+sp+JVMBit);
	while (m.find()) {
	    suffix = m.group();
	}
	if(suffix != null){
	    values = p.split(suffix);
	    suffix = "";
	    for (int i =1; i <  values.length; i++){
		if(i==1)
		    suffix = values[i];
		else
		    suffix = suffix + File.separator+values[i];
	    }
	}
	
	return suffix;
    }

    
    
    
    @Override
    public String getMathKernelSuffix(String mathematicaPath) {
	String sp = File.separator;
	if (!System.getProperty("java.vm.name").contains("64-Bit")) 
	    	JVMBit = "";
	Pattern p = Pattern.compile(File.separator);
	String[] values = p.split(mathematicaPath);
	
	if(values[values.length - 1].equals("JLink"))
	    return "";
	
	Pattern p2 = Pattern.compile(values[values.length - 1] + ".*");
	String suffix = null;
	Matcher m = p2.matcher("Contents"+sp+"MacOS"+sp+"MathKernel");
	while (m.find()) {
	    suffix = m.group();
	}
	if(suffix != null){
	    values = p.split(suffix);
	    suffix = "";
	    for (int i =1; i <  values.length; i++){
		if(i==1)
		    suffix = values[i];
		else
		    suffix = suffix + File.separator+values[i];
	    }
	}
	
	return suffix;
    }



    @Override
    public String getMathkernelDefaultSuffix() {
	// TODO Auto-generated method stub
	return "Contents"+File.separator+"MacOS"+File.separator+"MathKernel";
    }

}

