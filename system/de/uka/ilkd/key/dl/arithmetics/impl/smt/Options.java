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
package de.uka.ilkd.key.dl.arithmetics.impl.smt;

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
 * This class serves options specific for the Z3 interface
 * 
 * @author jdq
 * @since Nov 21, 2011
 */
public class Options implements Settings {

	public static final Options INSTANCE = new Options();

	private File z3Binary;

	private boolean usePrenexForm;

	private boolean elimExistentialQuantifierPrefix;

	private List<SettingsListener> listeners;

	private Options() {
		listeners = new LinkedList<SettingsListener>();
		z3Binary = new File("");
		usePrenexForm = true;
		elimExistentialQuantifierPrefix = true;
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
		String property = props
				.getProperty(EPropertyConstant.Z3_OPTIONS_Z3_BINARY.getKey());
		if (property != null) {
			z3Binary = new File(property);
		}
		property = props.getProperty(EPropertyConstant.Z3_OPTIONS_PRENEX_FORM
				.getKey());
		if (property != null) {
			usePrenexForm = Boolean.parseBoolean(property);
		}
		property = props
				.getProperty(EPropertyConstant.Z3_OPTIONS_ELIMINATE_EXISTENTIAL_PREFIX
						.getKey());
		if (property != null) {
			elimExistentialQuantifierPrefix = Boolean.parseBoolean(property);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.gui.Settings#writeSettings(java.util.Properties)
	 */
	public void writeSettings(Properties props) {
		if (!ProofSaver.isInSavingMode()) {
			// we don't want to save user specific pathes when saving proofs
			props.setProperty(EPropertyConstant.Z3_OPTIONS_Z3_BINARY.getKey(),
					z3Binary.getAbsolutePath());
		}
		props.setProperty(EPropertyConstant.Z3_OPTIONS_PRENEX_FORM.getKey(), ""
				+ usePrenexForm);
		props.setProperty(
				EPropertyConstant.Z3_OPTIONS_ELIMINATE_EXISTENTIAL_PREFIX
						.getKey(), "" + elimExistentialQuantifierPrefix);
	}

	/**
	 * @return the qepcadBinary
	 */
	public File getZ3Binary() {
		return z3Binary;
	}

	/**
	 * @param qepcadPath
	 *            the qepcadPath to set
	 */
	public void setZ3Binary(File z3Binary) {
		if (!this.z3Binary.equals(z3Binary)) {
			this.z3Binary = z3Binary;
			firePropertyChanged();
		}
	}

	
	
	/**
	 * @return the usePrenexForm
	 */
	protected boolean isUsePrenexForm() {
		return usePrenexForm;
	}

	/**
	 * @param usePrenexForm
	 *            the usePrenexForm to set
	 */
	protected void setUsePrenexForm(boolean usePrenexForm) {
		if(this.usePrenexForm != usePrenexForm) {
			this.usePrenexForm = usePrenexForm;
			firePropertyChanged();
		}
	}

	/**
	 * @return the elimExistentialQuantifierPrefix
	 */
	protected boolean isElimExistentialQuantifierPrefix() {
		return elimExistentialQuantifierPrefix;
	}

	/**
	 * @param elimExistentialQuantifierPrefix
	 *            the elimExistentialQuantifierPrefix to set
	 */
	protected void setElimExistentialQuantifierPrefix(
			boolean elimExistentialQuantifierPrefix) {
		if (this.elimExistentialQuantifierPrefix != elimExistentialQuantifierPrefix) {
			this.elimExistentialQuantifierPrefix = elimExistentialQuantifierPrefix;
			firePropertyChanged();
		}
	}

}
