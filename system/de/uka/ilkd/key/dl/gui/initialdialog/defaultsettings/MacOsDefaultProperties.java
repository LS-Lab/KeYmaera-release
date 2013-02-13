/**
 * 
 */
package de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings;

import java.io.File;

import de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.Suffixes.MacSuffixes;

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
						+ "qepcad";
		}
		return spath;
	}

	/**
	 * Initialise reduce binary default value
	 */
	public String getReduceBinaryDefault() {
		String rpath = System.getProperty("user.home");

		if (rpath == null) {
			rpath = sp + "usr" + sp + "bin" + sp + "reduce";
		} else {
			rpath = rpath + sp + "Workspace" + sp + "reduce-algebra" + sp
					+ "bin/reduce";
		}
		return rpath;
	}

	@Override
	public String getHOLLightPath() {
		String rpath = System.getProperty("user.home");

		if (rpath == null) {
			rpath = sp + "usr" + sp + "bin" + sp + "reduce";
		} else {
			rpath = rpath + sp + "Workspace" + sp + "hol_light";
		}
		return rpath;
	}

	@Override
	public String getOCAMLPath() {
		File ocaml = new File(sp + "opt" + sp + "local" + sp + "bin" + sp + "ocaml");
		if (!ocaml.exists()) {
			ocaml = new File(sp + "usr" + sp + "bin" + sp + "ocaml");
		}
		if (!ocaml.exists()) {
			ocaml = new File(System.getProperty("user.home") + sp + "bin" + sp
					+ "ocaml");
		}
		if (!ocaml.exists()) {
			ocaml = new File("/usr/bin/ocaml");
		}
		return ocaml.getAbsolutePath();
	}

	@Override
	protected String getCSDPPathDefault() {
		File csdp = new File(sp + "usr" + sp + "bin" + sp + "csdp");
		if (!csdp.exists())
			csdp = new File(System.getProperty("user.home") + sp + "bin" + "csdp");
		if (!csdp.exists())
			csdp = new File(sp + "usr" + sp + "local" + sp + "bin" + sp + "csdp");
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
        metit = new File(System.getProperty("user.home") + sp + "Workspace" + sp + "metit-2.0" +sp+ "metit");
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
     File metitAxioms = new File(sp + "usr" + sp + "local" + "metit-2.0" + sp + "tptp");
     if (!metitAxioms.exists())
        metitAxioms = new File(System.getProperty("user.home") + sp + "Workspace" + sp + "metit-2.0"+ sp + "tptp");    
     return metitAxioms.getAbsolutePath();
  }
  
  
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.LinuxOsDefaultProperties
	 * #getMathematicaCompletePath(java.lang.String)
	 */
	@Override
	protected String getMathematicaCompletePath(String mathematicaDefaultPath2) {
		return MacSuffixes.INSTANCE.getMathematicaPath(mathematicaDefaultPath2);
	}

}
