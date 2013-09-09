/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel                                *
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
/**
 * 
 */
package de.uka.ilkd.key.dl.arithmetics.impl.mathematica;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import  de.uka.ilkd.key.dl.options.EPropertyConstant;
import de.uka.ilkd.key.gui.GUIEvent;
import de.uka.ilkd.key.gui.configuration.PathConfig;
import de.uka.ilkd.key.gui.configuration.Settings;
import de.uka.ilkd.key.gui.configuration.SettingsListener;
import de.uka.ilkd.key.proof.ProofSaver;

/**
 * This class serves options specific for the Mathematica interface
 * 
 * @author jdq
 * @since Aug 31, 2007
 * @TODO somehow, the values are written from default even before they are read from disk.
 */
public class Options implements Settings{

	public static enum QuantifierEliminationMethod {
		REDUCE("Reduce", true), RESOLVE("Resolve", false);
		private String command;
		private boolean supportsList;

		private QuantifierEliminationMethod(String command, boolean supportsList) {
			this.command = command;
			this.supportsList = supportsList;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		/*@Override*/
		public String toString() {
			return command;
		}

		/**
		 * @return the supportsList
		 */
		public boolean isSupportsList() {
			return supportsList;
		}
	}

	public static final Options INSTANCE = new Options();

	private QuantifierEliminationMethod quantifierEliminationMethod;

	private boolean useEliminateList;

	private boolean convertDecimalsToRationals;

	private List<SettingsListener> listeners;
	
	private File mathKernel;

	private File jLinkLibDir;

	private int memoryConstraint;
	
	private boolean eliminateFractions;

    private boolean useParallelize;

	private Options() {
		listeners = new LinkedList<SettingsListener>();
		mathKernel = new File("MathKernel");
		String libDirProp = System.getProperty(EPropertyConstant.MATHEMATICA_OPTIONS_JLINK_LIBDIR.getKey());
		if(libDirProp != null) {
			jLinkLibDir = new File(libDirProp);
		} else {
			String home = System.getProperty("user.home");
			if(home != null) {
				jLinkLibDir = new File(home);
			} else {
				jLinkLibDir = new File("/");
			}
		}
		reset();
	}

	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.gui.configuration.Settings#reset()
	 */
	@Override
	public void reset() {
		quantifierEliminationMethod = QuantifierEliminationMethod.REDUCE;
		useEliminateList = true;
		convertDecimalsToRationals = true;
		eliminateFractions = false;
        useParallelize = false;
		memoryConstraint = -1;
		firePropertyChanged();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.gui.Settings#addSettingsListener(de.uka.ilkd.key.gui.SettingsListener)
	 */
	public void addSettingsListener(SettingsListener l) {
		listeners.add(l);
	}

	private void firePropertyChanged() {
		for (SettingsListener l : listeners) {
			l.settingsChanged(new GUIEvent(this));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.gui.Settings#readSettings(java.util.Properties)
	 */
	public void readSettings(Properties props) {
		String property = props.getProperty(EPropertyConstant.MATHEMATICA_OPTIONS_QUANTIFIER_ELIMINATION_METHOD.getKey());
		if (property != null) {
			quantifierEliminationMethod = QuantifierEliminationMethod
					.valueOf(property);
		}
		property = props.getProperty(EPropertyConstant.MATHEMATICA_OPTIONS_USE_ELIMINATE_LIST.getKey());
		if (property != null) {
			useEliminateList = Boolean.valueOf(property);
		}
		property = props.getProperty(EPropertyConstant.MATHEMATICA_OPTIONS_CONVERT_DECIMAL_FRACTIONS_TO_RATIONALS.getKey());
		if (property != null) {
			convertDecimalsToRationals = Boolean.valueOf(property);
		}
		property = props.getProperty(EPropertyConstant.MATHEMATICA_OPTIONS_MEMORYCONSTRAINT.getKey());
		if (property != null) {
			memoryConstraint = Integer.valueOf(property);
		}
		property = props.getProperty(EPropertyConstant.MATHEMATICA_OPTIONS_MATHKERNEL.getKey());
		if (property != null) {
			mathKernel = new File(property);
		}
		property = props.getProperty(EPropertyConstant.MATHEMATICA_OPTIONS_ELIMINATE_FRACTIONS.getKey());
		if (property != null) {
		    eliminateFractions = Boolean.valueOf(property);
		}
        property = props.getProperty(EPropertyConstant.MATHEMATICA_OPTIONS_PARALLELIZE.getKey());
        if (property != null) {
            useParallelize = Boolean.valueOf(property);
        }
		File file = new File(PathConfig.KEY_CONFIG_DIR + File.separator
				+ "webstart-math.props");
		if (file.exists()) {
			try {
				System.getProperties().load(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		jLinkLibDir = new File(System.getProperty(EPropertyConstant.MATHEMATICA_OPTIONS_JLINK_LIBDIR.getKey()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.gui.Settings#writeSettings(java.util.Properties)
	 */
	public void writeSettings(Properties props) {
		props.setProperty(EPropertyConstant.MATHEMATICA_OPTIONS_QUANTIFIER_ELIMINATION_METHOD.getKey(),
				quantifierEliminationMethod.name());
		props.setProperty(EPropertyConstant.MATHEMATICA_OPTIONS_USE_ELIMINATE_LIST.getKey(), Boolean
				.toString(useEliminateList));
		props.setProperty(EPropertyConstant.MATHEMATICA_OPTIONS_MEMORYCONSTRAINT.getKey(), "" + memoryConstraint);
		props.setProperty(EPropertyConstant.MATHEMATICA_OPTIONS_ELIMINATE_FRACTIONS.getKey(), Boolean.toString(eliminateFractions));
        props.setProperty(EPropertyConstant.MATHEMATICA_OPTIONS_PARALLELIZE.getKey(), Boolean.toString(useParallelize));

		if(!ProofSaver.isInSavingMode()) {
			// we don't want to save user specific pathes when saving proofs
			props.setProperty(EPropertyConstant.MATHEMATICA_OPTIONS_MATHKERNEL.getKey(), mathKernel.getAbsolutePath());			
		
			File file = new File(PathConfig.KEY_CONFIG_DIR + File.separator
					+ "webstart-math.props");
		
			Properties properties = new Properties();
			properties.setProperty(EPropertyConstant.MATHEMATICA_OPTIONS_JLINK_LIBDIR.getKey(), jLinkLibDir.getAbsolutePath());
			try {
				if (!file.exists()) {
					new File(PathConfig.KEY_CONFIG_DIR + File.separator)
							.mkdirs();
					file.createNewFile();
				}
				properties.store(new FileOutputStream(file), null);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the quantifierEliminationMethod
	 */
	public QuantifierEliminationMethod getQuantifierEliminationMethod() {
		return quantifierEliminationMethod;
	}

	/**
	 * @param quantifierEliminationMethod
	 *            the quantifierEliminationMethod to set
	 */
	public void setQuantifierEliminationMethod(
			QuantifierEliminationMethod quantifierEliminationMethod) {
		if (this.quantifierEliminationMethod != quantifierEliminationMethod) {
			this.quantifierEliminationMethod = quantifierEliminationMethod;
			firePropertyChanged();
		}
	}

	/**
	 * @return the useEliminateList
	 */
	public boolean isUseEliminateList() {
		return useEliminateList;
	}

	/**
	 * @param useEliminateList
	 *            the useEliminateList to set
	 */
	public void setUseEliminateList(boolean useEliminateList) {
		if (this.useEliminateList != useEliminateList) {
			this.useEliminateList = useEliminateList;
			firePropertyChanged();
		}
	}

	/**
	 * @return the memoryConstraint
	 */
	public int getMemoryConstraint() {
		return memoryConstraint;
	}

	/**
	 * @param memoryConstraint
	 *            the memoryConstraint to set
	 */
	public void setMemoryConstraint(int memoryConstraint) {
		if (this.memoryConstraint != memoryConstraint) {
			this.memoryConstraint = memoryConstraint;
			firePropertyChanged();
		}
	}

	/**
	 * @return the convertDecimalsToRationals
	 */
	public boolean isConvertDecimalsToRationals() {
		return convertDecimalsToRationals;
	}

	/**
	 * @param convertDecimalsToRationals the convertDecimalsToRationals to set
	 */
	public void setConvertDecimalsToRationals(boolean convertDecimalsToRationals) {
		if(convertDecimalsToRationals != this.convertDecimalsToRationals) {
			this.convertDecimalsToRationals = convertDecimalsToRationals;
			firePropertyChanged();
		}
	}

	/**
	 * @return the mathKernel
	 */
	public File getMathKernel() {
		return mathKernel;
	}

	/**
	 * @param mathKernel the mathKernel to set
	 */
	public void setMathKernel(File mathKernel) {
		if(!this.mathKernel.equals(mathKernel)) {
			this.mathKernel = mathKernel;
			firePropertyChanged();
		}
	}

	/**
	 * @return the jLinkLibDir
	 */
	public File getJLinkLibDir() {
		return jLinkLibDir;
	}

	/**
	 * @param linkLibDir the jLinkLibDir to set
	 */
	public void setJLinkLibDir(File linkLibDir) {
		if(!this.jLinkLibDir.equals(linkLibDir)) {
			jLinkLibDir = linkLibDir;
			System.setProperty(EPropertyConstant.MATHEMATICA_OPTIONS_JLINK_LIBDIR.getKey(), linkLibDir.getAbsolutePath());
			firePropertyChanged();
		}
	}
	
	/**
     * @return the eliminateFractions
     */
    public boolean isEliminateFractions() {
        return eliminateFractions;
    }
    
    /**
     * @param eliminateFractions the eliminateFractions to set
     */
    public void setEliminateFractions(boolean eliminateFractions) {
        if(this.eliminateFractions != eliminateFractions) {
            this.eliminateFractions = eliminateFractions;
            firePropertyChanged();
        }
    }

    public boolean isUseParallelize() {
        return useParallelize;
    }

    public void setUseParallelize(boolean useParallelize) {
        if(this.useParallelize != useParallelize) {
            this.useParallelize = useParallelize;
            firePropertyChanged();
        }
    }
}
