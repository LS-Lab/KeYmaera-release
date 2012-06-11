/*******************************************************************************
 * Copyright (c) 2010 Zacharias Mokom.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Zacharias Mokom - initial API and implementation
 ******************************************************************************/
package de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.Suffixes;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 *   
 *    This Object implements ISuffixes for Windows Operating systems platform.
 *    
 * @author zacho
 *
 */

public class WindowsSuffixes extends LinuxSuffixes implements ISuffixes{

	public static final WindowsSuffixes INSTANCE = new WindowsSuffixes();
	
    /**
     * Initialise jlink default path
     * 
     */
      
    @Override
    public String getJLinkDefaultSuffix() {
	// TODO Auto-generated method stub
	  return "SystemFiles"+File.separator+"Links"+File.separator + "JLink"+ File.separator + "SystemFiles" + File.separator + "Libraries"+ File.separator +"Windows-x86-64"; 
    }

    @Override
    public String getJLinkSuffix(String mathematicaPath) {
	// TODO Auto-generated method stub

	Pattern p = Pattern.compile(File.separator);
	String[] values = p.split(mathematicaPath);
	
	if(values[values.length - 1].equals("Windows-x86-64")||values[values.length - 1].equals("Windows-x86")) //XXX Still to verify for 32 bit computers
	    return "";
	
	Pattern p2 = Pattern.compile(values[values.length - 1] + ".*");
	String suffix = "";
	Matcher m = p2.matcher("SystemFiles" + File.separator + "Links" + File.separator + "JLink"+ File.separator + "SystemFiles" + File.separator + "Libraries"+ File.separator +"Windows-x86-64");
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
		return "MathKernel.exe";
	}

	@Override
	public String getMathkernelDefaultSuffix() {
		// TODO Auto-generated method stub
		return "MathKernel.exe";
	}
}
