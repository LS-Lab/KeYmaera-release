/*******************************************************************************
 * Copyright (c) 2010 Zacharias Mokom.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Zacharias Mokom - initial API and implementation
 ******************************************************************************/
package de.uka.ilkd.key.dl.gui.initialdialog.gui.options;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import de.uka.ilkd.key.dl.gui.initialdialog.gui.PropertiesCard;
import de.uka.ilkd.key.dl.gui.initialdialog.gui.PropertyConfigurationBeans;
import de.uka.ilkd.key.dl.gui.initialdialog.gui.PropertyGroupCardBeans;

/**
 * 
 * This Class write changes done on property files.
 * 
 * @author zacho
 * 
 */
public class WriteProperties {

    WriteProperties() {

    }

    /**
     * This method writes the changes done on the input paramter
     * <em> PropertyGroupCardBeans <em>
     * 
     * @param group
     *            <em> PropertyGroupCardBeans <em>
     */

    public static void write(List<PropertyConfigurationBeans> group) {
        ListIterator<PropertyConfigurationBeans> iter = group.listIterator();
        while (iter.hasNext())
            iter.next().writeSettings(new Properties());
    }

    /**
     * 
     * @param cardMap
     *            <em> PropertiesCard <em>
     * @return boolean; <b>true</b> if operation completed with success else
     *         <b>false</b>
     */
    public static Boolean write(PropertiesCard cardMap) {
        List<PropertyConfigurationBeans> list = new ArrayList<PropertyConfigurationBeans>();

        for (PropertyGroupCardBeans card : cardMap.getGroupCardMap().values()) {
            list.addAll(card.getGroup());
        }
        
        if (FileExistenceVerification.verifyDirectories(list, new JPanel()) == JOptionPane.YES_OPTION) {
            for (PropertyGroupCardBeans card : cardMap.getGroupCardMap()
                    .values()) {
                card.writePropertyChanges();
            }
            cardMap.getCheckBoxEditor().writeSettings(new Properties());
            return true;
        } else
            return false;

    }
}
