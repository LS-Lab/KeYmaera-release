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

import java.util.regex.*;
import java.io.File;
import java.io.FileFilter;

/**
 * 
 * This Object implements ISuffixes for the Linux Operating systems platform.
 * 
 * @author zacho
 * 
 */
public class LinuxSuffixes implements ISuffixes {

	public static final LinuxSuffixes INSTANCE = new LinuxSuffixes();

	@Override
	public Boolean isPossibleMathematicaPath(String mathematicaPath) {
		// TODO Auto-generated method stub
		return Pattern.matches(".*[Mm]athematica+.?[1-9]?+.?[0-9]?+.?[0-9]?",
				mathematicaPath);
	}

	@Override
	public Boolean containsMathematicaPathPrefix(String mathematicaPath) {
		// TODO Auto-generated method stub
		return Pattern.matches(".*[Mm]athematica+.?[1-9]?+.?[0-9]?+.?[0-9]?.*",
				mathematicaPath);
	}

	@Override
	public String getMathematicaPath(String mathematicaPath) {
		// TODO Auto-generated method stub
		Pattern p = Pattern
				.compile(".*[Mm]athematica+.?[1-9]?+.?[0-9]?+.?[0-9]?");
		Matcher m = p.matcher(mathematicaPath); // get a matcher object
		String prefix = null;
		while (m.find()) {
			prefix = m.group();
		}
		if (prefix != null)
			prefix = getMathematicaCompletePath(prefix);
		else {
			if (mathematicaPath.contains("Wolfram")
					|| mathematicaPath.contains("wolfram")
					|| mathematicaPath.contains("Wolfram Research")) {
				prefix = getMathematicaCompletePath(mathematicaPath
						+ File.separator + "Mathematica");
			}
		}
		return prefix;
	}

	public File[] getsubDirList(File dir) {

		// This filter only returns directories
		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
		if (dir.exists())
			return dir.listFiles(fileFilter);
		else
			return null;

	}

	private String getMathematicaCompletePath(String currentPath) {

	    
		File[] file = getsubDirList(new File(currentPath));

		
		if (file != null) {
		    java.util.Arrays.sort(file);
		    String tempPath = null;
			for (int i = 0; i < file.length; i++) {
				Pattern p = Pattern
						.compile(".*[Mm]athematica+.?[1-9]+.?[0-9]?+.?[0-9]?");
				Matcher m = p.matcher(file[i].toString()); // get a matcher
															// object
				while (m.find()) {
					tempPath = m.group();
				}
			}
			return tempPath;
		} else
			return null;
	}

	@Override
	public String getJLinkDefaultSuffix() {
		// TODO Auto-generated method stub
		return "SystemFiles" + File.separator + "Links" + File.separator
				+ "JLink" + File.separator + "SystemFiles" + File.separator
				+ "Libraries" + File.separator + "Linux-x86-64";
	}

	@Override
	public String getJLinkSuffix(String mathematicaPath) {
		// TODO Auto-generated method stub

		Pattern p = Pattern.compile(File.separator);
		String[] values = p.split(mathematicaPath);

		if (values[values.length - 1].equals("Linux-x86-64")
				|| values[values.length - 1].equals("Linux-x86")) //XXX Still to
																	// verify
																	// for 32
																	// bit
																	// computers
			return "";

		Pattern p2 = Pattern.compile(values[values.length - 1] + ".*");
		String suffix = "";
		Matcher m = p2.matcher("SystemFiles" + File.separator + "Links"
				+ File.separator + "JLink" + File.separator + "SystemFiles"
				+ File.separator + "Libraries" + File.separator
				+ "Linux-x86-64");
		while (m.find()) {
			suffix = m.group();
		}
		if (suffix != null) {
			values = p.split(suffix);
			suffix = "";
			for (int i = 1; i < values.length; i++) {
				if (i == 1)
					suffix = values[i];
				else
					suffix = suffix + File.separator + values[i];
			}
		}
		System.out.println(suffix);
		return suffix;
	}

	@Override
	public String getMathKernelSuffix(String mathematicaPath) {
		// TODO Auto-generated method stub
		return "Executables" + File.separator + "MathKernel";
	}

	@Override
	public String getMathkernelDefaultSuffix() {
		// TODO Auto-generated method stub
		return "Executables" + File.separator + "MathKernel";
	}
}
