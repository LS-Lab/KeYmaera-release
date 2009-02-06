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

	public static enum QuantifierEliminationMethod {
		RLQE("rlqe", "Virtual Substitution"), RLCAD("rlcad",
				"Cylindrical algebraic decomposition"), RLHQE("rlhqe",
				"Hermitian Quantifier Elimination");

		private final String method;
		private final String display;

		private QuantifierEliminationMethod(String str, String display) {
			this.method = str;
			this.display = display;
		}

		/**
		 * @return the method
		 */
		public String getMethod() {
			return method;
		}

		/**
		 * @return the display
		 */
		public String getDisplay() {
			return display;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return display;
		}
	}

	public static enum ReduceSwitch {
		ON, OFF, DEFAULT;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
	}

	private static final String OPTIONS_REDUCE_BINARY = "[ReduceOptions]reduceBinary";

	private static final String OPTIONS_REDUCE_QUANTIFIER_ELIMINATION_METHOD = "[ReduceOptions]quantifierEliminationMethod";

	private static final String OPTIONS_REDUCE_RLALL = "[ReduceOptions]rlall";

	private static final String OPTIONS_REDUCE_rlanuexsgnopt = "[ReduceOptions]rlanuexsgnopt";
	private static final String OPTIONS_REDUCE_rlanuexgcdnormalize = "[ReduceOptions]ReduceSwitch rlanuexgcdnormalize";
	private static final String OPTIONS_REDUCE_rlanuexpsremseq = "[ReduceOptions]rlanuexpsremseq";
	private static final String OPTIONS_REDUCE_rlcadhongproj = "[ReduceOptions]rlcadhongproj";
	private static final String OPTIONS_REDUCE_rlcadaprojalways = "[ReduceOptions]rlcadaprojalways";
	private static final String OPTIONS_REDUCE_rlcadaproj = "[ReduceOptions]rlcadaproj";
	private static final String OPTIONS_REDUCE_rlcadisoallroots = "[ReduceOptions]rlcadisoallroots";
	private static final String OPTIONS_REDUCE_rlcadrawformula = "[ReduceOptions]rlcadrawformula";
	private static final String OPTIONS_REDUCE_rlcadtrimtree = "[ReduceOptions]rlcadtrimtree";
	private static final String OPTIONS_REDUCE_rlcadfulldimonly = "[ReduceOptions]rlcadfulldimonly";
	private static final String OPTIONS_REDUCE_rlcadpbfvs = "[ReduceOptions]rlcadpbfvs";
	private static final String OPTIONS_REDUCE_rlcadte = "[ReduceOptions]rlcadte";
	private static final String OPTIONS_REDUCE_rlcadpartial = "[ReduceOptions]rlcadpartial";
	private static final String OPTIONS_REDUCE_rlcadextonly = "[ReduceOptions]rlcadextonly";
	private static final String OPTIONS_REDUCE_rlcadprojonly = "[ReduceOptions]rlcadprojonly";
	private static final String OPTIONS_REDUCE_rlcadbaseonly = "[ReduceOptions]rlcadbaseonly";
	private static final String OPTIONS_REDUCE_rlcadfac = "[ReduceOptions]rlcadfac";
	private static final String OPTIONS_REDUCE_rlqepnf = "[ReduceOptions]rlqepnf";
	private static final String OPTIONS_REDUCE_rlqeheu = "[ReduceOptions]rlqeheu";
	private static final String OPTIONS_REDUCE_rlqedfs = "[ReduceOptions]rlqedfs";
	private static final String OPTIONS_REDUCE_rlqesqsc = "[ReduceOptions]rlqesqsc";
	private static final String OPTIONS_REDUCE_rlqeqsc = "[ReduceOptions]rlqeqsc";

	private static final String OPTIONS_REDUCE_RLSIMPL = "[ReduceOptions]rlsimpl";
	private File reduceBinary;

	private QuantifierEliminationMethod qeMethod;

	private boolean rlall;

	private ReduceSwitch rlsimpl;

	private ReduceSwitch rlqeqsc;

	private ReduceSwitch rlqesqsc;

	private ReduceSwitch rlqedfs;

	private ReduceSwitch rlqeheu;

	private ReduceSwitch rlqepnf;

	private ReduceSwitch rlcadfac;

	private ReduceSwitch rlcadbaseonly;

	private ReduceSwitch rlcadprojonly;

	private ReduceSwitch rlcadextonly;

	private ReduceSwitch rlcadpartial;

	private ReduceSwitch rlcadte;

	private ReduceSwitch rlcadpbfvs;

	private ReduceSwitch rlcadfulldimonly;

	private ReduceSwitch rlcadtrimtree;

	private ReduceSwitch rlcadrawformula;

	private ReduceSwitch rlcadisoallroots;

	private ReduceSwitch rlcadaproj;

	private ReduceSwitch rlcadaprojalways;

	private ReduceSwitch rlcadhongproj;

	private ReduceSwitch rlanuexpsremseq;

	private ReduceSwitch rlanuexgcdnormalize;

	private ReduceSwitch rlanuexsgnopt;

	private List<SettingsListener> listeners;

	private Options() {
		listeners = new LinkedList<SettingsListener>();
		reduceBinary = new File("/");
		qeMethod = QuantifierEliminationMethod.RLQE;

		rlall = false;

		rlsimpl = ReduceSwitch.ON;

		rlqeqsc = ReduceSwitch.DEFAULT;

		rlqesqsc = ReduceSwitch.DEFAULT;

		rlqedfs = ReduceSwitch.DEFAULT;

		rlqeheu = ReduceSwitch.DEFAULT;

		rlqepnf = ReduceSwitch.DEFAULT;

		rlcadfac = ReduceSwitch.DEFAULT;

		rlcadbaseonly = ReduceSwitch.DEFAULT;

		rlcadprojonly = ReduceSwitch.DEFAULT;

		rlcadextonly = ReduceSwitch.DEFAULT;

		rlcadpartial = ReduceSwitch.DEFAULT;

		rlcadte = ReduceSwitch.DEFAULT;

		rlcadpbfvs = ReduceSwitch.DEFAULT;

		rlcadfulldimonly = ReduceSwitch.DEFAULT;

		rlcadtrimtree = ReduceSwitch.DEFAULT;

		rlcadrawformula = ReduceSwitch.DEFAULT;

		rlcadisoallroots = ReduceSwitch.DEFAULT;

		rlcadaproj = ReduceSwitch.DEFAULT;

		rlcadaprojalways = ReduceSwitch.DEFAULT;

		rlcadhongproj = ReduceSwitch.DEFAULT;

		rlanuexpsremseq = ReduceSwitch.DEFAULT;

		rlanuexgcdnormalize = ReduceSwitch.DEFAULT;

		rlanuexsgnopt = ReduceSwitch.DEFAULT;
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
		property = props
				.getProperty(OPTIONS_REDUCE_QUANTIFIER_ELIMINATION_METHOD);
		if (property != null) {
			qeMethod = QuantifierEliminationMethod.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_RLALL);
		if (property != null) {
			rlall = Boolean.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_RLSIMPL);
		if (property != null) {
			rlsimpl = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlanuexsgnopt);
		if (property != null) {
			rlanuexsgnopt = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlanuexgcdnormalize);
		if (property != null) {
			rlanuexgcdnormalize = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlanuexpsremseq);
		if (property != null) {
			rlanuexpsremseq = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlcadhongproj);
		if (property != null) {
			rlcadhongproj = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlcadaprojalways);
		if (property != null) {
			rlcadaprojalways = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlcadaproj);
		if (property != null) {
			rlcadaproj = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlcadisoallroots);
		if (property != null) {
			rlcadisoallroots = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlcadrawformula);
		if (property != null) {
			rlcadrawformula = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlcadtrimtree);
		if (property != null) {
			rlcadtrimtree = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlcadfulldimonly);
		if (property != null) {
			rlcadfulldimonly = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlcadpbfvs);
		if (property != null) {
			rlcadpbfvs = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlcadte);
		if (property != null) {
			rlcadte = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlcadpartial);
		if (property != null) {
			rlcadpartial = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlcadextonly);
		if (property != null) {
			rlcadextonly = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlcadprojonly);
		if (property != null) {
			rlcadprojonly = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlcadbaseonly);
		if (property != null) {
			rlcadbaseonly = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlcadfac);
		if (property != null) {
			rlcadfac = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlqepnf);
		if (property != null) {
			rlqepnf = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlqeheu);
		if (property != null) {
			rlqeheu = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlqedfs);
		if (property != null) {
			rlqedfs = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlqesqsc);
		if (property != null) {
			rlqesqsc = ReduceSwitch.valueOf(property);
		}
		property = props.getProperty(OPTIONS_REDUCE_rlqeqsc);
		if (property != null) {
			rlqeqsc = ReduceSwitch.valueOf(property);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.gui.Settings#writeSettings(java.util.Properties)
	 */
	public void writeSettings(Properties props) {
		props
				.setProperty(OPTIONS_REDUCE_BINARY, reduceBinary
						.getAbsolutePath());
		props.setProperty(OPTIONS_REDUCE_QUANTIFIER_ELIMINATION_METHOD,
				qeMethod.name());
		props.setProperty(OPTIONS_REDUCE_RLALL, Boolean.toString(rlall));
		props.setProperty(OPTIONS_REDUCE_rlanuexsgnopt, rlanuexsgnopt.name());
		props.setProperty(OPTIONS_REDUCE_rlanuexgcdnormalize,
				rlanuexgcdnormalize.name());
		props.setProperty(OPTIONS_REDUCE_rlanuexpsremseq, rlanuexpsremseq
				.name());
		props.setProperty(OPTIONS_REDUCE_rlcadhongproj, rlcadhongproj.name());
		props.setProperty(OPTIONS_REDUCE_rlcadaprojalways, rlcadaprojalways
				.name());
		props.setProperty(OPTIONS_REDUCE_rlcadaproj, rlcadaproj.name());
		props.setProperty(OPTIONS_REDUCE_rlcadisoallroots, rlcadisoallroots
				.name());
		props.setProperty(OPTIONS_REDUCE_rlcadrawformula, rlcadrawformula
				.name());
		props.setProperty(OPTIONS_REDUCE_rlcadtrimtree, rlcadtrimtree.name());
		props.setProperty(OPTIONS_REDUCE_rlcadfulldimonly, rlcadfulldimonly
				.name());
		props.setProperty(OPTIONS_REDUCE_rlcadpbfvs, rlcadpbfvs.name());
		props.setProperty(OPTIONS_REDUCE_rlcadte, rlcadte.name());
		props.setProperty(OPTIONS_REDUCE_rlcadpartial, rlcadpartial.name());
		props.setProperty(OPTIONS_REDUCE_rlcadextonly, rlcadextonly.name());
		props.setProperty(OPTIONS_REDUCE_rlcadprojonly, rlcadprojonly.name());
		props.setProperty(OPTIONS_REDUCE_rlcadbaseonly, rlcadbaseonly.name());
		props.setProperty(OPTIONS_REDUCE_rlcadfac, rlcadfac.name());
		props.setProperty(OPTIONS_REDUCE_rlqepnf, rlqepnf.name());
		props.setProperty(OPTIONS_REDUCE_rlqeheu, rlqeheu.name());
		props.setProperty(OPTIONS_REDUCE_rlqedfs, rlqedfs.name());
		props.setProperty(OPTIONS_REDUCE_rlqesqsc, rlqesqsc.name());
		props.setProperty(OPTIONS_REDUCE_rlqeqsc, rlqeqsc.name());
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
			System.out.println("Setting path to " + qepcadPath);// XXX
			this.reduceBinary = qepcadPath;
			firePropertyChanged();
		}
	}

	/**
	 * @return the qeMethod
	 */
	public QuantifierEliminationMethod getQeMethod() {
		return qeMethod;
	}

	/**
	 * @param qeMethod
	 *            the qeMethod to set
	 */
	public void setQeMethod(QuantifierEliminationMethod qeMethod) {
		if (qeMethod != this.qeMethod) {
			this.qeMethod = qeMethod;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlqeqsc
	 */
	public ReduceSwitch getRlqeqsc() {
		return rlqeqsc;
	}

	/**
	 * @param rlqeqsc
	 *            the rlqeqsc to set
	 */
	public void setRlqeqsc(ReduceSwitch rlqeqsc) {
		if (this.rlqeqsc != rlqeqsc) {
			this.rlqeqsc = rlqeqsc;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlqesqsc
	 */
	public ReduceSwitch getRlqesqsc() {
		return rlqesqsc;
	}

	/**
	 * @param rlqesqsc
	 *            the rlqesqsc to set
	 */
	public void setRlqesqsc(ReduceSwitch rlqesqsc) {
		if (this.rlqesqsc != rlqesqsc) {
			this.rlqesqsc = rlqesqsc;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlqedfs
	 */
	public ReduceSwitch getRlqedfs() {
		return rlqedfs;
	}

	/**
	 * @param rlqedfs
	 *            the rlqedfs to set
	 */
	public void setRlqedfs(ReduceSwitch rlqedfs) {
		if (this.rlqedfs != rlqedfs) {
			this.rlqedfs = rlqedfs;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlqeheu
	 */
	public ReduceSwitch getRlqeheu() {
		return rlqeheu;
	}

	/**
	 * @param rlqeheu
	 *            the rlqeheu to set
	 */
	public void setRlqeheu(ReduceSwitch rlqeheu) {
		if (this.rlqeheu != rlqeheu) {
			this.rlqeheu = rlqeheu;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlqepnf
	 */
	public ReduceSwitch getRlqepnf() {
		return rlqepnf;
	}

	/**
	 * @param rlqepnf
	 *            the rlqepnf to set
	 */
	public void setRlqepnf(ReduceSwitch rlqepnf) {
		if (this.rlqepnf != rlqepnf) {
			this.rlqepnf = rlqepnf;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlcadfac
	 */
	public ReduceSwitch getRlcadfac() {
		return rlcadfac;
	}

	/**
	 * @param rlcadfac
	 *            the rlcadfac to set
	 */
	public void setRlcadfac(ReduceSwitch rlcadfac) {
		if (this.rlcadfac != rlcadfac) {
			this.rlcadfac = rlcadfac;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlcadbaseonly
	 */
	public ReduceSwitch getRlcadbaseonly() {
		return rlcadbaseonly;
	}

	/**
	 * @param rlcadbaseonly
	 *            the rlcadbaseonly to set
	 */
	public void setRlcadbaseonly(ReduceSwitch rlcadbaseonly) {
		if (this.rlcadbaseonly != rlcadbaseonly) {
			this.rlcadbaseonly = rlcadbaseonly;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlcadprojonly
	 */
	public ReduceSwitch getRlcadprojonly() {
		return rlcadprojonly;
	}

	/**
	 * @param rlcadprojonly
	 *            the rlcadprojonly to set
	 */
	public void setRlcadprojonly(ReduceSwitch rlcadprojonly) {
		if (this.rlcadprojonly != rlcadprojonly) {
			this.rlcadprojonly = rlcadprojonly;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlcadextonly
	 */
	public ReduceSwitch getRlcadextonly() {
		return rlcadextonly;
	}

	/**
	 * @param rlcadextonly
	 *            the rlcadextonly to set
	 */
	public void setRlcadextonly(ReduceSwitch rlcadextonly) {
		if (this.rlcadextonly != rlcadextonly) {
			this.rlcadextonly = rlcadextonly;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlcadpartial
	 */
	public ReduceSwitch getRlcadpartial() {
		return rlcadpartial;
	}

	/**
	 * @param rlcadpartial
	 *            the rlcadpartial to set
	 */
	public void setRlcadpartial(ReduceSwitch rlcadpartial) {
		if (this.rlcadpartial != rlcadpartial) {
			this.rlcadpartial = rlcadpartial;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlcadte
	 */
	public ReduceSwitch getRlcadte() {
		return rlcadte;
	}

	/**
	 * @param rlcadte
	 *            the rlcadte to set
	 */
	public void setRlcadte(ReduceSwitch rlcadte) {
		if (this.rlcadte != rlcadte) {
			this.rlcadte = rlcadte;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlcadpbfvs
	 */
	public ReduceSwitch getRlcadpbfvs() {
		return rlcadpbfvs;
	}

	/**
	 * @param rlcadpbfvs
	 *            the rlcadpbfvs to set
	 */
	public void setRlcadpbfvs(ReduceSwitch rlcadpbfvs) {
		if (this.rlcadpbfvs != rlcadpbfvs) {
			this.rlcadpbfvs = rlcadpbfvs;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlcadfulldimonly
	 */
	public ReduceSwitch getRlcadfulldimonly() {
		return rlcadfulldimonly;
	}

	/**
	 * @param rlcadfulldimonly
	 *            the rlcadfulldimonly to set
	 */
	public void setRlcadfulldimonly(ReduceSwitch rlcadfulldimonly) {
		if (this.rlcadfulldimonly != rlcadfulldimonly) {
			this.rlcadfulldimonly = rlcadfulldimonly;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlcadtrimtree
	 */
	public ReduceSwitch getRlcadtrimtree() {
		return rlcadtrimtree;
	}

	/**
	 * @param rlcadtrimtree
	 *            the rlcadtrimtree to set
	 */
	public void setRlcadtrimtree(ReduceSwitch rlcadtrimtree) {
		if (this.rlcadtrimtree != rlcadtrimtree) {
			this.rlcadtrimtree = rlcadtrimtree;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlcadrawformula
	 */
	public ReduceSwitch getRlcadrawformula() {
		return rlcadrawformula;
	}

	/**
	 * @param rlcadrawformula
	 *            the rlcadrawformula to set
	 */
	public void setRlcadrawformula(ReduceSwitch rlcadrawformula) {
		if (this.rlcadrawformula != rlcadrawformula) {
			this.rlcadrawformula = rlcadrawformula;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlcadisoallroots
	 */
	public ReduceSwitch getRlcadisoallroots() {
		return rlcadisoallroots;
	}

	/**
	 * @param rlcadisoallroots
	 *            the rlcadisoallroots to set
	 */
	public void setRlcadisoallroots(ReduceSwitch rlcadisoallroots) {
		if (this.rlcadisoallroots != rlcadisoallroots) {
			this.rlcadisoallroots = rlcadisoallroots;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlcadaproj
	 */
	public ReduceSwitch getRlcadaproj() {
		return rlcadaproj;
	}

	/**
	 * @param rlcadaproj
	 *            the rlcadaproj to set
	 */
	public void setRlcadaproj(ReduceSwitch rlcadaproj) {
		if (this.rlcadaproj != rlcadaproj) {
			this.rlcadaproj = rlcadaproj;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlcadaprojalways
	 */
	public ReduceSwitch getRlcadaprojalways() {
		return rlcadaprojalways;
	}

	/**
	 * @param rlcadaprojalways
	 *            the rlcadaprojalways to set
	 */
	public void setRlcadaprojalways(ReduceSwitch rlcadaprojalways) {
		if (this.rlcadaprojalways != rlcadaprojalways) {
			this.rlcadaprojalways = rlcadaprojalways;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlcadhongproj
	 */
	public ReduceSwitch getRlcadhongproj() {
		return rlcadhongproj;
	}

	/**
	 * @param rlcadhongproj
	 *            the rlcadhongproj to set
	 */
	public void setRlcadhongproj(ReduceSwitch rlcadhongproj) {
		if (this.rlcadhongproj != rlcadhongproj) {
			this.rlcadhongproj = rlcadhongproj;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlanuexpsremseq
	 */
	public ReduceSwitch getRlanuexpsremseq() {
		return rlanuexpsremseq;
	}

	/**
	 * @param rlanuexpsremseq
	 *            the rlanuexpsremseq to set
	 */
	public void setRlanuexpsremseq(ReduceSwitch rlanuexpsremseq) {
		if (this.rlanuexpsremseq != rlanuexpsremseq) {
			this.rlanuexpsremseq = rlanuexpsremseq;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlanuexgcdnormalize
	 */
	public ReduceSwitch getRlanuexgcdnormalize() {
		return rlanuexgcdnormalize;
	}

	/**
	 * @param rlanuexgcdnormalize
	 *            the rlanuexgcdnormalize to set
	 */
	public void setRlanuexgcdnormalize(ReduceSwitch rlanuexgcdnormalize) {
		if (this.rlanuexgcdnormalize != rlanuexgcdnormalize) {
			this.rlanuexgcdnormalize = rlanuexgcdnormalize;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlanuexsgnopt
	 */
	public ReduceSwitch getRlanuexsgnopt() {
		return rlanuexsgnopt;
	}

	/**
	 * @param rlanuexsgnopt
	 *            the rlanuexsgnopt to set
	 */
	public void setRlanuexsgnopt(ReduceSwitch rlanuexsgnopt) {
		if (this.rlanuexsgnopt != rlanuexsgnopt) {
			this.rlanuexsgnopt = rlanuexsgnopt;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlall
	 */
	public boolean isRlall() {
		return rlall;
	}

	/**
	 * @param rlall
	 *            the rlall to set
	 */
	public void setRlall(boolean rlall) {
		if (rlall != this.rlall) {
			this.rlall = rlall;
			firePropertyChanged();
		}
	}

	/**
	 * @return the rlsimpl
	 */
	public ReduceSwitch getRlsimpl() {
		return rlsimpl;
	}

	/**
	 * @param rlsimpl
	 *            the rlsimpl to set
	 */
	public void setRlsimpl(ReduceSwitch rlsimpl) {
		if (this.rlsimpl != rlsimpl) {
			this.rlsimpl = rlsimpl;
			firePropertyChanged();
		}
	}

}
