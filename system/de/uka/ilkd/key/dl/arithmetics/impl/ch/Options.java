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
package de.uka.ilkd.key.dl.arithmetics.impl.ch;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import de.uka.ilkd.key.dl.arithmetics.impl.reduce.Options.ReduceSwitch;
import de.uka.ilkd.key.dl.options.EPropertyConstant;
import de.uka.ilkd.key.gui.GUIEvent;
import de.uka.ilkd.key.gui.configuration.Settings;
import de.uka.ilkd.key.gui.configuration.SettingsListener;

/**
 * This class serves options specific for the CohenHormander interface
 * 
 * @author jdq
 * @since Aug 20, 2008
 * @TODO somehow, the values are written from default even before they are read
 *       from disk.
 */
public class Options implements Settings {

	public static final Options INSTANCE = new Options();

	
	public static enum CHMode {NO_DNF, DNF};
	
	private CHMode eliminatorMode;
	
	private List<SettingsListener> listeners;

	private Options() {
		listeners = new LinkedList<SettingsListener>();
		reset();
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.gui.configuration.Settings#reset()
	 */
	@Override
	public void reset() {
		eliminatorMode = CHMode.DNF;
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
		String property;
		property = props.getProperty(EPropertyConstant.COHENHORMANDER_OPTIONS_MODE.getKey());
		if (property != null) {
			eliminatorMode = CHMode.valueOf(property);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.gui.Settings#writeSettings(java.util.Properties)
	 */
	public void writeSettings(Properties props) {
		props.setProperty(EPropertyConstant.COHENHORMANDER_OPTIONS_MODE.getKey(), eliminatorMode.name());
		//props.setProperty(OPTIONS_QEPCAD_PATH, qepcadPath.getAbsolutePath());
		//props.setProperty(OPTIONS_SACLIB_PATH, saclibPath.getAbsolutePath());
		//props.setProperty(OPTIONS_QEPCAD_MEMORYLIMIT, "" + qepcadMemoryLimit);
	}
	
	
	public void setEliminatorMode(CHMode m){
		if(this.eliminatorMode != m){
			this.eliminatorMode = m;
			firePropertyChanged();
		}
	}
	
	public CHMode getEliminatorMode(){
		
		return eliminatorMode;
	}

}
