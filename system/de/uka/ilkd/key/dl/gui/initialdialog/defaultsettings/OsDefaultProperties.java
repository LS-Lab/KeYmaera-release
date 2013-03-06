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
import  de.uka.ilkd.key.dl.options.EPropertyConstant;
/**
 * @author jdq TODO Documentation since Nov 27, 2009
 */
public abstract class OsDefaultProperties implements IOsDefaultProperties {

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
			props.put(EPropertyConstant.MATHEMATICA_OPTIONS_MATHKERNEL.getKey(), getMathKernelDefault());
			props.put(EPropertyConstant.MATHEMATICA_OPTIONS_JLINK_LIBDIR.getKey(), getJLinkDefault());
			props.put(EPropertyConstant.QEPCAD_OPTIONS_QEPCAD_PATH.getKey(), getQepCadDefault());
			props.put(EPropertyConstant.QEPCAD_OPTIONS_SACLIB_PATH.getKey(), getSaclibDefault());
			props.put(EPropertyConstant.QEPCAD_OPTIONS_SINGULAR_PATH.getKey(), getSingularDefault());
			props.put(EPropertyConstant.OPTIONS_REDUCE_BINARY.getKey(), getReduceBinaryDefault());
			props.put(EPropertyConstant.DLOPTIONS_CSDP_PATH.getKey(), getCSDPPathDefault());
			props.put(EPropertyConstant.HOL_OPTIONS_HARRISON_QE_PATH.getKey(), getHarrisionQEPath());
			props.put(EPropertyConstant.HOL_OPTIONS_HOLLIGHT_PATH.getKey(), getHOLLightPath());
			props.put(EPropertyConstant.HOL_OPTIONS_OCAML_PATH.getKey(), getOCAMLPath());
			props.put(EPropertyConstant.Z3_OPTIONS_Z3_BINARY.getKey(), getZ3BinaryPath());
         props.put(EPropertyConstant.METIT_OPTIONS_BINARY.getKey(), getMetitBinaryPath());
         props.put(EPropertyConstant.METIT_OPTIONS_AXIOMS.getKey(), getMetitAxiomPath());
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
	 * Initialise singular default path
	 */
	
	public abstract String getSingularDefault();

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
	 * Initialise z3 default value
	 */
	protected abstract String getZ3BinaryPath();
	
	  /**
    * Initialise MetiTarski default value
    */
   protected abstract String getMetitBinaryPath();
   
   /**
    * Initialise MetiTarski tptp axiom folder
    */
   protected abstract String getMetitAxiomPath();

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
