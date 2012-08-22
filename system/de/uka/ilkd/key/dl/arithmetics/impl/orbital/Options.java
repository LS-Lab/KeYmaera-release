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
package de.uka.ilkd.key.dl.arithmetics.impl.orbital;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import orbital.math.Values;
import orbital.moon.math.ArithmeticValuesImpl;
import de.uka.ilkd.key.dl.options.EPropertyConstant;
import de.uka.ilkd.key.gui.GUIEvent;
import de.uka.ilkd.key.gui.configuration.Settings;
import de.uka.ilkd.key.gui.configuration.SettingsListener;

/**
 * 
 * @author ap
 * 
 */
public class Options implements Settings {

    public static final Options INSTANCE = new Options();
    
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
        getDeferred().setSparsePolynomials(true);
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
        for(SettingsListener l: listeners) {
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
                .getProperty(EPropertyConstant.ORBITAL_OPTIONS_REPRESENTATION.getKey());
        if (property != null) {
            getDeferred().setRepresentation(property);
        }
        property = props.getProperty(EPropertyConstant.ORBITAL_OPTIONS_PRECISION.getKey());
        if (property != null) {
             getDeferred().setPrecision(Integer.valueOf(property));
        }
        property = props.getProperty(EPropertyConstant.ORBITAL_OPTIONS_SPARSEPOLYNOMIALS.getKey());
        if (property != null) {
             getDeferred().setSparsePolynomials(Boolean.valueOf(property));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.gui.Settings#writeSettings(java.util.Properties)
     */
    public void writeSettings(Properties props) {
        props.setProperty(EPropertyConstant.ORBITAL_OPTIONS_REPRESENTATION.getKey(),
                getRepresentation());
        props.setProperty(EPropertyConstant.ORBITAL_OPTIONS_PRECISION.getKey(), "" + getPrecision());
        props.setProperty(EPropertyConstant.ORBITAL_OPTIONS_SPARSEPOLYNOMIALS.getKey(), "" + isSparsePolynomials());
    }

    public String getRepresentation() {
        return getDeferred().getRepresentation();
    }

    public void setRepresentation(String repr) {
        getDeferred().setRepresentation(repr);
        firePropertyChanged();
    }

    public int getPrecision() {
        return getDeferred().getPrecision();
    }

    public void setPrecision(int precision) {
        getDeferred().setPrecision(precision);
        firePropertyChanged();
    }

    public boolean isSparsePolynomials() {
        return getDeferred().isSparsePolynomials();
    }

    public void setSparsePolynomials(boolean sparse) {
        getDeferred().setSparsePolynomials(sparse);
        firePropertyChanged();
    }

    
    private final ArithmeticValuesImpl getDeferred() {
	return (ArithmeticValuesImpl)Values.getDefault();
    }
}
