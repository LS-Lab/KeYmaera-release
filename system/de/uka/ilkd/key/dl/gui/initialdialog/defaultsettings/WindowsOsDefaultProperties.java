/**
 * 
 */
package de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The MacOsDefaultProperties class creates and instance of a Property Object
 * containing all possible default properties for linux platform.
 * 
 * @author zacho
 */
public class WindowsOsDefaultProperties extends OsDefaultProperties {

	/**
	 * 
	 */
	public WindowsOsDefaultProperties() {
		setMathematicaDefaultPath("C:" + sp + "Program Files" + sp
			+ "Wolfram" + sp + "Mathematica" + sp + "7.0");
	}
	
	public String getMathematicaCompletePath(String currentPath) {

		File[] file = getsubDirList(new File(currentPath));
		System.out.println(currentPath);
		java.util.Arrays.sort(file);

		String tempPath = null;
		if (file != null) {
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
		File csdp = new File("C:" + sp + "Program Files" + sp + "csdp" + sp
				+ "bin" + sp + "csdp");
		if (!csdp.exists())
			csdp = new File("C:" + sp);
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
			hol = "C:\\";
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
			hol = "C:\\";
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
		return getMathematicaDefaultPath() + sp + "SystemFiles" + sp + "Link"
				+ sp + "JLink" + sp + "SystemFiles" + sp + "Libraries" + sp
				+ "Windows-x86-64";
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
		return getMathematicaDefaultPath() + sp + "MathKernel.exe";
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
		File ocaml = new File("C:" + sp + "Program Files" + sp + "flyspeck"
				+ sp + "hol_light");
		if (!ocaml.exists())
			ocaml = new File("C:" + sp);
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
		return "C:" + sp + "Program Files" + sp + "qepcad" + sp + "qesource";
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
		return "C:" + sp + "reduce";
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
		return "C:" + sp + "Program Files" + sp + "Qepcad" + sp + "Saclib";
	}
}