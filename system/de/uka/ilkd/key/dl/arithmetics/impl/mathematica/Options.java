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

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import de.uka.ilkd.key.gui.GUIEvent;
import de.uka.ilkd.key.gui.configuration.Settings;
import de.uka.ilkd.key.gui.configuration.SettingsListener;

/**
 * This class serves options specific for the Mathematica interface
 * 
 * @author jdq
 * @since Aug 31, 2007
 * @TODO somehow, the values are written from default even before they are read from disk.
 */
public class Options implements Settings {

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

	private static final String OPTIONS_QUANTIFIER_ELIMINATION_METHOD = "[MathematicaOptions]quantifierEliminationMethod";

	private static final String OPTIONS_USE_ELIMINATE_LIST = "[MathematicaOptions]useEliminateList";

	private static final String OPTIONS_MEMORYCONSTRAINT = "[MathematicaOptions]memoryConstraint";

	private static final String OPTIONS_CONVERT_DECIMAL_FRACTIONS_TO_RATIONALS = "[MathematicaOptions]convertDecimalFractionsToRationals";

	private QuantifierEliminationMethod quantifierEliminationMethod;

	private boolean useEliminateList;

	private boolean convertDecimalsToRationals;

	private List<SettingsListener> listeners;

	private int memoryConstraint;

	private Options() {
		listeners = new LinkedList<SettingsListener>();
		quantifierEliminationMethod = QuantifierEliminationMethod.REDUCE;
		useEliminateList = true;
		convertDecimalsToRationals = true;
		memoryConstraint = -1;
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
		String property = props
				.getProperty(OPTIONS_QUANTIFIER_ELIMINATION_METHOD);
		if (property != null) {
			quantifierEliminationMethod = QuantifierEliminationMethod
					.valueOf(property);
		}
		property = props.getProperty(OPTIONS_USE_ELIMINATE_LIST);
		if (property != null) {
			useEliminateList = Boolean.valueOf(property);
		}
		property = props.getProperty(OPTIONS_CONVERT_DECIMAL_FRACTIONS_TO_RATIONALS);
		if (property != null) {
			convertDecimalsToRationals = Boolean.valueOf(property);
		}
		property = props.getProperty(OPTIONS_MEMORYCONSTRAINT);
		if (property != null) {
			memoryConstraint = Integer.valueOf(property);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.gui.Settings#writeSettings(java.util.Properties)
	 */
	public void writeSettings(Properties props) {
		props.setProperty(OPTIONS_QUANTIFIER_ELIMINATION_METHOD,
				quantifierEliminationMethod.name());
		props.setProperty(OPTIONS_USE_ELIMINATE_LIST, Boolean
				.toString(useEliminateList));
		props.setProperty(OPTIONS_MEMORYCONSTRAINT, "" + memoryConstraint);
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
	
	

}
