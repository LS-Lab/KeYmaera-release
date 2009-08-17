/**
 * 
 */
package de.uka.ilkd.key.dl.gui.initialdialog;

import javax.swing.*;

import de.uka.ilkd.key.dl.gui.initialdialog.gui.ConfigurationMainFrame;

/**
 * 
 * The PropertyConfiguratorSample class create and instance of property
 * confirugration Frame, whereby the properties can be created or modified.
 * 
 * @author zacho
 */
public class initialDialogSample {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

        if (!ConfigurationMainFrame.INSTANCE.getCheckboxState()) {
            final JFrame frame = ConfigurationMainFrame.INSTANCE.getPathPanel();
            frame.setVisible(true);
        }
    }

}
