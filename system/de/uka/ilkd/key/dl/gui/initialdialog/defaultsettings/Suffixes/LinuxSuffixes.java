package de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.Suffixes;
import java.util.regex.*;
import java.io.File;
/**
 * 
 *    This Object implements ISuffixes for the Linux Operating systems platform.
 *    
 * @author zacho
 *
 */
public class LinuxSuffixes implements ISuffixes{
    

    @Override
    public Boolean isPossibleMathematicaPath(String mathematicaPath) {
	// TODO Auto-generated method stub
	return Pattern.matches(".*[Mm]athematica+.?[1-9]?+.?[0-9]?+.?[0-9]?",mathematicaPath);
    }
    @Override
    public Boolean containsMathematicaPathPrefix(String mathematicaPath) {
	// TODO Auto-generated method stub
	return Pattern.matches(".*[Mm]athematica+.?[1-9]?+.?[0-9]?+.?[0-9]?.*",mathematicaPath);
    }
    @Override
    public String getMathematicaPath(String mathematicaPath) {
	// TODO Auto-generated method stub
    	Pattern p = Pattern.compile(".*[Mm]athematica+.?[1-9]?+.?[0-9]?+.?[0-9]?");
    	Matcher m = p.matcher(mathematicaPath); // get a matcher object
    	String prefix = "";
    	while (m.find()) {
    	    prefix = m.group();
    	}
    	return prefix;
    }
    
    @Override
    public String getJLinkDefaultSuffix() {
	// TODO Auto-generated method stub
	  return "SystemFiles"+File.separator+"Links"+File.separator + "JLink"+ File.separator + "SystemFiles" + File.separator + "Libraries"+ File.separator +"Linux-x86-64"; 
    }

    @Override
    public String getJLinkSuffix(String mathematicaPath) {
	// TODO Auto-generated method stub

	Pattern p = Pattern.compile(File.separator);
	String[] values = p.split(mathematicaPath);
	
	if(values[values.length - 1].equals("Linux-x86-64")||values[values.length - 1].equals("Linux-x86")) // Still to verify for 32 bit computers
	    return "";
	
	Pattern p2 = Pattern.compile(values[values.length - 1] + ".*");
	String suffix = "";
	Matcher m = p2.matcher("SystemFiles" + File.separator + "Links" + File.separator + "JLink"+ File.separator + "SystemFiles" + File.separator + "Libraries"+ File.separator +"Linux-x86-64");
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
	// TODO Auto-generated method stub
	return "Executables"+File.separator+"MathKernel";
    }



    @Override
    public String getMathkernelDefaultSuffix() {
	// TODO Auto-generated method stub
	return "Executables"+File.separator+"MathKernel";
    }
}
