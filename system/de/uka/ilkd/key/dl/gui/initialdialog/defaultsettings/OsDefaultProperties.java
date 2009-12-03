/***************************************************************************
 *   Copyright (C) 2008 by Jan David Quesel                                *
 *   quesel@informatik.uni-oldenburg.de                                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
package de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings;

import java.io.File;
import java.io.FileFilter;
import java.util.Properties;

import de.uka.ilkd.key.dl.options.PropertyConstants;

/**
 * @author jdq TODO Documentation since Nov 27, 2009
 */
public abstract class OsDefaultProperties implements IOsDefaultProperties,
		PropertyConstants {

	private Properties props;
	protected String sp = File.separator;
	private String mathematicaDefaultPath;

	/**
	 * @return the default Properties
	 */
	public Properties getDefaultPropertyList() {

		if (props == null) {
			props = new Properties();
			String temp = getMathematicaCompletePath(getMathematicaDefaultPath());
			if (temp != null)
				setMathematicaDefaultPath(temp);
			props.put("[MathematicaOptions]mathematicaPath",
					getMathematicaDefaultPath());
			props.put(MATHEMATICA_OPTIONS_MATHKERNEL, getMathKernelDefault());
			props.put(MATHEMATICA_OPTIONS_JLINK_LIBDIR, getJLinkDefault());
			props.put(QEPCAD_OPTIONS_QEPCAD_PATH, getQepCadDefault());
			props.put(QEPCAD_OPTIONS_SACLIB_PATH, getSaclibDefault());
			props.put(OPTIONS_REDUCE_BINARY, getReduceBinaryDefault());
			props.put(DLOPTIONS_CSDP_PATH, getCSDPPathDefault());
			props.put(HOL_OPTIONS_HARRISON_QE_PATH, getHarrisionQEPath());
			props.put(HOL_OPTIONS_HOLLIGHT_PATH, getHOLLightPath());
			props.put(HOL_OPTIONS_OCAML_PATH, getOCAMLPath());
			props.put("[checkBox]flag", "false");
		}
		return props;
	}

	protected abstract String getMathematicaCompletePath(
			String mathematicaDefaultPath2);

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

	/**
	 * Initialise jlink default path
	 */
	public abstract String getJLinkDefault();

	/**
	 * Initialise mathkernel default value
	 */

	public abstract String getMathKernelDefault();

	/**
	 * Initialise quepcad default path
	 */

	public abstract String getQepCadDefault();

	/**
	 * Initialise saclib default path
	 */

	public abstract String getSaclibDefault();

	/**
	 * Initialise reduce binary default value
	 */
	public abstract String getReduceBinaryDefault();

	/**
	 * Initialise HOL light paths default values
	 */
	public abstract String getHOLLightPath();

	/**
	 * Initialise HOL light paths default values
	 */
	public abstract String getHarrisionQEPath();

	/**
	 * Initialise HOL light paths default values
	 */
	public abstract String getOCAMLPath();

	/**
	 * Initialise csdp default value
	 */
	protected abstract String getCSDPPathDefault();

	/**
	 * @return the mathematicaDefaultPath
	 */
	protected String getMathematicaDefaultPath() {
		return mathematicaDefaultPath;
	}

	/**
	 * @param mathematicaDefaultPath
	 *            the mathematicaDefaultPath to set
	 */
	protected void setMathematicaDefaultPath(String mathematicaDefaultPath) {
		this.mathematicaDefaultPath = mathematicaDefaultPath;
	}

}