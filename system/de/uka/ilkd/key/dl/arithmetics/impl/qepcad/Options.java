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

import de.uka.ilkd.key.gui.GUIEvent;
import de.uka.ilkd.key.gui.configuration.Settings;
import de.uka.ilkd.key.gui.configuration.SettingsListener;

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

	private static final String OPTIONS_QEPCAD_BINARY = "[QepcadOptions]qepcadBinary";
	private static final String OPTIONS_QEPCAD_PATH = "[QepcadOptions]qepcadPath";
	private static final String OPTIONS_SACLIB_PATH = "[QepcadOptions]saclibPath";

	private File qepcadBinary;
	private File qepcadPath;
	private File saclibPath;

	private List<SettingsListener> listeners;

	private Options() {
		listeners = new LinkedList<SettingsListener>();
		qepcadBinary = new File("qepcad");
		String qpath = System.getenv("qe");
		if(qpath == null) {
			qepcadPath = new File("");
		} else {
			qepcadPath = new File(qpath);
		}
		String spath = System.getenv("saclib");
		if(spath == null) {
			saclibPath = new File("/");
		} else {
			saclibPath = new File(spath);
		}
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
		String property = props.getProperty(OPTIONS_QEPCAD_BINARY);
		if (property != null) {
			qepcadBinary = new File(property);
		}
		property = props.getProperty(OPTIONS_QEPCAD_PATH);
		if (property != null) {
			qepcadPath = new File(property);
		}
		property = props.getProperty(OPTIONS_SACLIB_PATH);
		if (property != null) {
			saclibPath = new File(property);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.gui.Settings#writeSettings(java.util.Properties)
	 */
	public void writeSettings(Properties props) {
		props.setProperty(OPTIONS_QEPCAD_BINARY, qepcadBinary.getAbsolutePath());
		props.setProperty(OPTIONS_QEPCAD_PATH, qepcadPath.getAbsolutePath());
		props.setProperty(OPTIONS_SACLIB_PATH, saclibPath.getAbsolutePath());
	}

	/**
	 * @return the qepcadBinary
	 */
	public File getQepcadBinary() {
		return qepcadBinary;
	}

	/**
	 * @param qepcadBinary
	 *            the qepcadBinary to set
	 */
	public void setQepcadBinary(File qepcadBinary) {
		if (!this.qepcadBinary.equals(qepcadBinary)) {
			this.qepcadBinary = qepcadBinary;
			firePropertyChanged();
		}
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

}
