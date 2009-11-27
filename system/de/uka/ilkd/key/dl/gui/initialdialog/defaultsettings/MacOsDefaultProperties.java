/**
 * 
 */
package de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The MacOsDefaultProperties class creates and instance of a Property Object
 * containing all possible default properties for linux platform
 * 
 * @author zacho
 */
public class MacOsDefaultProperties extends LinuxOsDefaultProperties implements
		IOsDefaultProperties {

	/**
	 * 
	 */
	public MacOsDefaultProperties() {
		setMathematicaDefaultPath(sp + "Applications" + sp + "Mathematica.app");
	}

	public String getMathematicaCompletePath(String currentPath) {

		File[] file = getsubDirList(new File(currentPath));

		java.util.Arrays.sort(file);

		String tempPath = null;
		if (file != null) {
			for (int i = 0; i < file.length; i++) {
				Pattern p = Pattern
						.compile(".*[Mm]athematica+.?[1-9]+.?[0-9]?+.?[0-9]?(.app)?");
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.LinuxOsDefaultProperties
	 * #getJLinkDefault()
	 */
	@Override
	public String getJLinkDefault() {
		return getMathematicaDefaultPath() + sp + "SystemFiles" + sp + "Links"
				+ sp + "JLink" + sp + "SystemFiles" + sp + "Libraries" + sp
				+ "MacOSX-x86-64";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.LinuxOsDefaultProperties
	 * #getMathKernelDefault()
	 */
	@Override
	public String getMathKernelDefault() {
		return getMathematicaDefaultPath() + sp + "Contents" + sp + "MacOS"
				+ sp + "MathKernel";
	}
	
	/**
	 * Initialise quepcad default path
	 */

	public String getQepCadDefault() {
		String qpath = System.getenv("qe");
		if (qpath == null) {
			qpath = System.getProperty("user.home");
			if (qpath == null)
				qpath = "/";
			else
				qpath = qpath + sp + "Workspace" + sp + "qepcad/qesource";

		}
		return qpath;

	}

	/**
	 * Initialise saclib default path
	 */

	public String getSaclibDefault() {

		String spath = System.getenv("saclib");
		if (spath == null) {
			spath = System.getProperty("user.home");
			if (spath == null)
				spath = "/";
			else
				spath = spath + File.separator + "Workspace" + File.separator
						+ "qepcad/saclib";
		}
		return spath;
	}

	/**
	 * Initialise reduce binary default value
	 */
	public String getReduceBinaryDefault() {
		String rpath = System.getProperty("user.home");

		if (rpath == null) {
			rpath = "/usr/bin/reduce";
		} else {
			rpath = rpath + sp + "Workspace" + sp + "reduce-algebra" + sp
					+ "bin/reduce";
		}
		return rpath;
	}

}