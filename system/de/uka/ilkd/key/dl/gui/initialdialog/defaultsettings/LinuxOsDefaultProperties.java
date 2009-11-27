package de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The LinuxOsDefaultProperties class creates and instance of a Property Object
 * containing all possible default properties for linux platform.
 * 
 * @author zacho
 * 
 */
public class LinuxOsDefaultProperties extends OsDefaultProperties implements
		IOsDefaultProperties {

	public LinuxOsDefaultProperties() {
		setMathematicaDefaultPath(sp + "usr" + sp + "local" + sp + "Wolfram"
				+ sp + "Mathematica");
	}

	public String getMathematicaCompletePath(String currentPath) {

		File[] file = getsubDirList(new File(currentPath));

		String tempPath = null;
		if (file != null) {
			java.util.Arrays.sort(file);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OsDefaultProperties
	 * #getCSDPPathDefault()
	 */
	@Override
	protected String getCSDPPathDefault() {
		File csdp = new File(sp + "user" + sp + "bin" + sp + "csdp");
		if (!csdp.exists())
			csdp = new File(System.getProperty("user.home"));
		if (!csdp.exists())
			csdp = new File("/");
		return csdp.getAbsolutePath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OsDefaultProperties
	 * #getHOLLightPath()
	 */
	@Override
	public String getHOLLightPath() {
		String hol = System.getProperty("user.home");
		if (hol == null)
			hol = "/";
		return hol;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OsDefaultProperties
	 * #getHarrisionQEPath()
	 */
	@Override
	public String getHarrisionQEPath() {
		String hol = System.getProperty("user.home");
		if (hol == null)
			hol = "/";
		return hol;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OsDefaultProperties
	 * #getJLinkDefault()
	 */
	@Override
	public String getJLinkDefault() {
		return getMathematicaDefaultPath() + sp + "SystemFiles" + sp + "Links"
				+ sp + "JLink" + sp + "SystemFiles" + sp + "Libraries" + sp
				+ "Linux-x86-64";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OsDefaultProperties
	 * #getMathKernelDefault()
	 */
	@Override
	public String getMathKernelDefault() {
		return getMathematicaDefaultPath() + sp + "Executables" + sp
				+ "MathKernel";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OsDefaultProperties
	 * #getOCAMLPath()
	 */
	@Override
	public String getOCAMLPath() {
		File ocaml = new File(sp + "usr" + sp + "bin" + sp + "ocaml");
		if (!ocaml.exists()) {
			ocaml = new File(System.getProperty("user.home") + sp + "bin" + sp
					+ "ocaml");
		}
		if (!ocaml.exists()) {
			ocaml = new File("/usr/bin/ocaml");
		}
		return ocaml.getAbsolutePath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OsDefaultProperties
	 * #getQepCadDefault()
	 */
	@Override
	public String getQepCadDefault() {
		String qpath = System.getenv("qe");
		if (qpath == null) {
			qpath = System.getProperty("user.home");
			if (qpath == null)
				qpath = "/";
		}
		return qpath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OsDefaultProperties
	 * #getReduceBinaryDefault()
	 */
	@Override
	public String getReduceBinaryDefault() {
		String rpath = System.getProperty("user.home");
		if (rpath == null) {
			rpath = "/usr/bin/reduce";
		} else {
			rpath += sp + "bin" + sp + "reduce";
		}
		return rpath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OsDefaultProperties
	 * #getSaclibDefault()
	 */
	@Override
	public String getSaclibDefault() {
		String spath = System.getenv("saclib");
		if (spath == null) {
			spath = System.getProperty("user.home");
			if (spath == null)
				spath = "/";
		}
		return spath;
	}

}