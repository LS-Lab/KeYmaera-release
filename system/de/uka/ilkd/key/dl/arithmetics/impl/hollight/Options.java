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
package de.uka.ilkd.key.dl.arithmetics.impl.hollight;

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
 * This class serves options specific for the HOL Light interface
 * 
 * @author jdq
 * @since Aug 20, 2008
 * @TODO somehow, the values are written from default even before they are read
 *       from disk.
 */
public class Options implements Settings {

	public static final Options INSTANCE = new Options();

	public static enum QuantifierEliminationMethod {
		ProofProducing, Harrison;
	}

	private File hollightPath;
	private File ocamlPath;

	private File harrisonqePath;

	private QuantifierEliminationMethod method;
	
	private boolean useSnapshots;

	private List<SettingsListener> listeners;

	private Options() {
		listeners = new LinkedList<SettingsListener>();
		hollightPath = new File("/");
		harrisonqePath = new File("/");
		ocamlPath = new File("/usr/bin/ocaml");
		reset();
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.gui.configuration.Settings#reset()
	 */
	@Override
	public void reset() {
		method = QuantifierEliminationMethod.ProofProducing;
		useSnapshots = false;
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
		String property = props.getProperty(EPropertyConstant.HOL_OPTIONS_HOLLIGHT_PATH.getKey());
		if (property != null) {
			hollightPath = new File(property);
		}
		property = props.getProperty(EPropertyConstant.HOL_OPTIONS_OCAML_PATH.getKey());
		if (property != null) {
			ocamlPath = new File(property);
		}
		property = props.getProperty(EPropertyConstant.HOL_OPTIONS_HARRISON_QE_PATH.getKey());
		if (property != null) {
			harrisonqePath = new File(property);
		}
		property = props.getProperty(EPropertyConstant.HOL_OPTIONS_QUANTIFIER_ELIMINATION_METHOD.getKey());
		if (property != null) {
			method = QuantifierEliminationMethod.valueOf(property);
		}
		property = props.getProperty(EPropertyConstant.HOL_OPTIONS_USE_SNAPSHOTS.getKey());
		if (property != null) {
		    useSnapshots = Boolean.valueOf(property);
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
			props
				.setProperty(EPropertyConstant.HOL_OPTIONS_HOLLIGHT_PATH.getKey(), hollightPath
						.getAbsolutePath());
			props.setProperty(EPropertyConstant.HOL_OPTIONS_OCAML_PATH.getKey(), ocamlPath.getAbsolutePath());
			props.setProperty(EPropertyConstant.HOL_OPTIONS_HARRISON_QE_PATH.getKey(), harrisonqePath
					.getAbsolutePath());
		}
		props.setProperty(EPropertyConstant.HOL_OPTIONS_QUANTIFIER_ELIMINATION_METHOD.getKey(), method.name());
		props.setProperty(EPropertyConstant.HOL_OPTIONS_USE_SNAPSHOTS.getKey(), "" + useSnapshots);
	}

	/**
	 * @return the qepcadPath
	 */
	public File getHollightPath() {
		return hollightPath;
	}

	/**
	 * @param qepcadPath
	 *            the qepcadPath to set
	 */
	public void setHollightPath(File qepcadPath) {
		if (!this.hollightPath.equals(qepcadPath)) {
			System.out.println("Setting path to " + qepcadPath);// XXX
			new Exception().printStackTrace();
			this.hollightPath = qepcadPath;
			firePropertyChanged();
		}
	}

	/**
	 * @return the saclibPath
	 */
	public File getOcamlPath() {
		return ocamlPath;
	}

	/**
	 * @param ocamlPath
	 *            the saclibPath to set
	 */
	public void setOcamlPath(File ocamlPath) {
		if (!this.ocamlPath.equals(ocamlPath)) {
			this.ocamlPath = ocamlPath;
			firePropertyChanged();
		}
	}

	/**
	 * @return the harrisonqePath
	 */
	public File getHarrisonqePath() {
		return harrisonqePath;
	}

	/**
	 * @param harrisonqePath
	 *            the harrisonqePath to set
	 */
	public void setHarrisonqePath(File harrisonqePath) {
		if (!this.harrisonqePath.equals(harrisonqePath)) {
			this.harrisonqePath = harrisonqePath;
			firePropertyChanged();
		}
	}

	/**
	 * @return the method
	 */
	public QuantifierEliminationMethod getMethod() {
		return method;
	}

	/**
	 * @param method
	 *            the method to set
	 */
	public void setMethod(QuantifierEliminationMethod method) {
		if (this.method != method) {
			this.method = method;
			firePropertyChanged();
		}
	}

    public boolean isUseSnapshots() {
        return useSnapshots;
    }

    public void setUseSnapshots(boolean useSnapshots) {
        if(this.useSnapshots != useSnapshots) {
            this.useSnapshots = useSnapshots;
            firePropertyChanged();
        }
    }

}
