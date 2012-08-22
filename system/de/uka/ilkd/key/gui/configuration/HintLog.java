/*******************************************************************************
 * Copyright (c) 2012 Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jan-David Quesel - initial API and implementation
 ******************************************************************************/
package de.uka.ilkd.key.gui.configuration;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Properties;

import de.uka.ilkd.key.gui.GUIEvent;

/**
 * @author jdq
 *
 */
public class HintLog implements Settings {

    private static final String USED_HINTS = "[hints]used";
    
    private LinkedHashSet<String> usedHints = new LinkedHashSet<String>();

    private HashSet<SettingsListener> listeners = new LinkedHashSet<SettingsListener>();
    
    /* (non-Javadoc)
     * @see de.uka.ilkd.key.gui.configuration.Settings#readSettings(java.util.Properties)
     */
    @Override
    public void readSettings(Properties props) {
        String property = props.getProperty(USED_HINTS);
        if(property != null) {
            for(String h: property.split(",")) {
                usedHints.add(h);
            }
        }
    }

    /* (non-Javadoc)
     * @see de.uka.ilkd.key.gui.configuration.Settings#writeSettings(java.util.Properties)
     */
    @Override
    public void writeSettings(Properties props) {
        String s = "";
        String c = "";
        for(String h: usedHints) {
            s += c + h;
            c = ",";
        }
        props.setProperty(USED_HINTS, s);
    }
    
    public void addUsedHint(String s) {
        if(!usedHints.contains(s)) {
            usedHints.add(s);
    		for (SettingsListener l : listeners) {
    			l.settingsChanged(new GUIEvent(this));
            }
        }
    }

    /* (non-Javadoc)
     * @see de.uka.ilkd.key.gui.configuration.Settings#addSettingsListener(de.uka.ilkd.key.gui.configuration.SettingsListener)
     */
    @Override
    public void addSettingsListener(SettingsListener l) {
		listeners.add(l);
    }

    /* (non-Javadoc)
     * @see de.uka.ilkd.key.gui.configuration.Settings#reset()
     */
    @Override
    public void reset() {
        // TODO Auto-generated method stub
        
    }

}
