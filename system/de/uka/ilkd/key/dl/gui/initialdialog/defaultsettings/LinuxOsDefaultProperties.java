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
package de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings;

import java.io.File;

import de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.Suffixes.LinuxSuffixes;

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
			csdp = new File(System.getProperty("user.home") + sp + "bin" + "csdp");
		if (!csdp.exists())
			csdp = new File(sp + "usr" + sp + "bin" + sp + "csdp");
		return csdp.getAbsolutePath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OsDefaultProperties
	 * #getZ3BinaryPath()
	 */
	@Override
	protected String getZ3BinaryPath() {
		File z3 = new File(sp + "user" + sp + "bin" + sp + "z3");
		if (!z3.exists())
			z3 = new File(System.getProperty("user.home") + sp + "bin" + "z3");
		if (!z3.exists())
			z3 = new File(sp + "usr" + sp + "bin" + sp + "z3");
		return z3.getAbsolutePath();
	}

	  /*
    * (non-Javadoc)
    * 
    * @see
    * de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OsDefaultProperties
    * #getMetitBinaryPath()
    */
   @Override
   protected String getMetitBinaryPath() {
      File metit = new File(  "sp" + "usr" + sp + "local" + sp + "bin" + sp + "metit-2.0" +sp+ "metit");
      if (!metit.exists())
         metit = new File(System.getProperty("user.home") + sp + "metit-2.0" +sp+ "metit");
      if (!metit.exists())
         metit = new File(sp+ "opt" + sp + "metit-2.0" +sp+ "metit");
      
      return metit.getAbsolutePath();
      
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OsDefaultProperties
    * #getMetitAxiomPath()
    */
   @Override
   protected String getMetitAxiomPath() {
      File metitAxioms = new File(sp + "usr" + sp + "local" + "metit-2.0" + sp + "tptp" +sp);
      if (!metitAxioms.exists())
         metitAxioms = new File(System.getProperty("user.home") + sp + "metit-2.0"+ sp + "tptp" +sp);    
     
      if (!metitAxioms.exists())
         metitAxioms = new File(sp + "opt" + sp + "metit-2.0"+ sp + "tptp" +sp);    
        
      return metitAxioms.getAbsolutePath();
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
				+ "Linux" + (System.getProperty("os.arch").contains("64")?"-x86-64":"");
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
			rpath = sp + "usr" + sp + "bin" + sp + "reduce";
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OsDefaultProperties
	 * #getSaclibDefault()
	 */
	@Override
	public String getSingularDefault() {
	    String spath = System.getenv("saclib");
	    if (spath == null) {
	        spath = "/usr/bin";
	    }
	    return spath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OsDefaultProperties
	 * #getMathematicaCompletePath(java.lang.String)
	 */
	@Override
	protected String getMathematicaCompletePath(String mathematicaDefaultPath2) {
		return LinuxSuffixes.INSTANCE
				.getMathematicaPath(mathematicaDefaultPath2);
	}
}
