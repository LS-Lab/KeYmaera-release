/**
 * 
 */
package de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings;

import java.io.File;

import de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.Suffixes.WindowsSuffixes;

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
			+ "Wolfram Research" + sp + "Mathematica" + sp + "7.0");
	}
	
	public String getMathematicaCompletePath(String currentPath) {
		return WindowsSuffixes.INSTANCE.getMathematicaPath(currentPath);
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
				+ "bin" + sp + "csdp.exe");
		if (!csdp.exists())
			csdp = new File("C:" + sp + "csdp.exe");
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
		File z3 = new File("C:" + sp + "Program Files" + sp + "z3" + sp
				+ "bin" + sp + "z3.exe");
		if (!z3.exists())
			z3 = new File("C:" + sp + "z3.exe");
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
      File metit = new File("C:" + sp + "Program Files" + sp + "metit-2.0" + sp + "metit.exe");
      
      if (!metit.exists())
         metit = new File("C:" +sp + "metit-2.0" + sp + "metit.exe");
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
      File metitAxioms = new File("C:" + sp + "Program Files" + sp + "metit-2.0" + sp + "tptp");
      if (!metitAxioms.exists())
         metitAxioms = new File("C:"+ sp + "metit-2.0" + "tptp");
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
		return "C:" + sp + "reduce.exe";
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
		return "C:" + sp + "Program Files" + sp + "qepcad" + sp + "saclib";
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
	    return "C:" + sp + "Program Files" + sp + "singular";
	}
}
