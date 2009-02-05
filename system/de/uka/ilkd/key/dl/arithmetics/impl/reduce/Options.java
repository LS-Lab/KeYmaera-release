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
package de.uka.ilkd.key.dl.arithmetics.impl.reduce;

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

	private static final String OPTIONS_REDUCE_BINARY = "[ReduceOptions]reduceBinary";

	private File reduceBinary;

	private List<SettingsListener> listeners;

	private Options() {
		listeners = new LinkedList<SettingsListener>();
		reduceBinary = new File("/");
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
		String property = props.getProperty(OPTIONS_REDUCE_BINARY);
		if (property != null) {
			reduceBinary = new File(property);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.gui.Settings#writeSettings(java.util.Properties)
	 */
	public void writeSettings(Properties props) {
		props.setProperty(OPTIONS_REDUCE_BINARY, reduceBinary.getAbsolutePath());
	}

	/**
	 * @return the qepcadBinary
	 */
	public File getReduceBinary() {
		return reduceBinary;
	}

	/**
	 * @param qepcadPath
	 *            the qepcadPath to set
	 */
	public void setReduceBinary(File qepcadPath) {
		if (!this.reduceBinary.equals(qepcadPath)) {
			System.out.println("Setting path to " + qepcadPath);//XXX
			this.reduceBinary = qepcadPath;
			firePropertyChanged();
		}
	}

}
