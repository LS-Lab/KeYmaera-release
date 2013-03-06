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
/**
 * 
 */
package de.uka.ilkd.key.dl.arithmetics.impl.qepcad;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import de.uka.ilkd.key.dl.options.EPropertyConstant;
import de.uka.ilkd.key.gui.GUIEvent;
import de.uka.ilkd.key.gui.configuration.Settings;
import de.uka.ilkd.key.gui.configuration.SettingsListener;
import de.uka.ilkd.key.proof.ProofSaver;

/**
 * This class serves options specific for the Qepcad interface
 * 
 * @author jdq
 * @since Aug 20, 2008
 * @TODO somehow, the values are written from default even before they are read
 *       from disk.
 */
public class Options implements Settings {

	public static final Options INSTANCE = new Options();

	private File qepcadPath;
	private File saclibPath;	
	private File singularPath;	
	private int qepcadMemoryLimit;

	private List<SettingsListener> listeners;

	private Options() {
		listeners = new LinkedList<SettingsListener>();
		String qpath = System.getenv("qe");
		if (qpath == null) {
			qpath = System.getProperty("user.home");
			if(qpath == null) {
				qepcadPath = new File("/");
			} else {
				qepcadPath = new File(qpath);
			}
		} else {
			qepcadPath = new File(qpath);
		}
		String spath = System.getenv("saclib");
		if (spath == null) {
			spath = System.getProperty("user.home");
			if(spath == null) {
				saclibPath = new File("/");
			} else {
				saclibPath = new File(qpath);
			}
		} else {
			saclibPath = new File(spath);
		}
		spath = System.getenv("SINGULAR");
        if (spath == null) {
            spath = System.getProperty("user.home");
            if(spath == null) {
                singularPath = new File("/");
            } else {
                singularPath = new File(qpath);
            }
        } else {
            singularPath = new File(spath);
        }
		reset();
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.gui.configuration.Settings#reset()
	 */
	@Override
	public void reset() {
		qepcadMemoryLimit = 2000000;
		firePropertyChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.gui.Settings#addSettingsListener(de.uka.ilkd.key.gui.
	 * SettingsListener)
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
		String property = props.getProperty(EPropertyConstant.QEPCAD_OPTIONS_QEPCAD_PATH.getKey());
		if (property != null) {
			qepcadPath = new File(property);
		}
		property = props.getProperty(EPropertyConstant.QEPCAD_OPTIONS_SACLIB_PATH.getKey());
		if (property != null) {
			saclibPath = new File(property);
		}
		property = props.getProperty(EPropertyConstant.QEPCAD_OPTIONS_SINGULAR_PATH.getKey());
		if (property != null) {
		    singularPath = new File(property);
		}
		property = props.getProperty(EPropertyConstant.QEPCAD_OPTIONS_QEPCAD_MEMORYLIMIT.getKey());
		if (property != null) {
			qepcadMemoryLimit = Integer.parseInt(property);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.gui.Settings#writeSettings(java.util.Properties)
	 */
	public void writeSettings(Properties props) {
		if(!ProofSaver.isInSavingMode()) {
			// we don't want to save user specific pathes when saving proofs
			props.setProperty(EPropertyConstant.QEPCAD_OPTIONS_QEPCAD_PATH.getKey(), qepcadPath.getAbsolutePath());
			props.setProperty(EPropertyConstant.QEPCAD_OPTIONS_SACLIB_PATH.getKey(), saclibPath.getAbsolutePath());
			props.setProperty(EPropertyConstant.QEPCAD_OPTIONS_SINGULAR_PATH.getKey(), singularPath.getAbsolutePath());
		}
		props.setProperty(EPropertyConstant.QEPCAD_OPTIONS_QEPCAD_MEMORYLIMIT.getKey(), "" + qepcadMemoryLimit);
	}

	/**
	 * @return the qepcadBinary
	 */
	public File getQepcadBinary() {
		return new File(qepcadPath + File.separator + "bin" + File.separator
				+ "qepcad");
	}

	/**
	 * @return the qepcadPath
	 */
	public File getQepcadPath() {
		return qepcadPath;
	}

	/**
	 * @param qepcadPath
	 *            the qepcadPath to set
	 */
	public void setQepcadPath(File qepcadPath) {
		if (!this.qepcadPath.equals(qepcadPath)) {
			System.out.println("Setting path to " + qepcadPath);//XXX
			this.qepcadPath = qepcadPath;
			firePropertyChanged();
		}
	}

	/**
	 * @return the saclibPath
	 */
	public File getSaclibPath() {
		return saclibPath;
	}

	/**
	 * @param saclibPath
	 *            the saclibPath to set
	 */
	public void setSaclibPath(File saclibPath) {
		if (!this.saclibPath.equals(saclibPath)) {
			this.saclibPath = saclibPath;
			firePropertyChanged();
		}
	}
	

	/**
	 * @return the qepcadMemoryLimit
	 */
	public int getQepcadMemoryLimit() {
		return qepcadMemoryLimit;
	}

	/**
	 * @param qepcadMemoryLimit the qepcadMemoryLimit to set
	 */
	public void setQepcadMemoryLimit(int qepcadMemoryLimit) {
		if(this.qepcadMemoryLimit != qepcadMemoryLimit) {
			this.qepcadMemoryLimit = qepcadMemoryLimit;
			firePropertyChanged();
		}
	}

    public File getSingularPath() {
        return singularPath;
    }

    public void setSingularPath(File singularPath) {
        if(this.singularPath != singularPath) {
            this.singularPath = singularPath;
            firePropertyChanged();
        }
    }

}
