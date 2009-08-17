/**
 * 
 */
package de.uka.ilkd.key.dl.gui.initialdialog;

import javax.swing.JFrame;

import de.uka.ilkd.key.dl.gui.initialdialog.gui.InitialDialogBeans;

/**
 * 
 * The PropertyConfiguratorSample class create and instance of property
 * confirugration Frame, whereby the properties can be created or modified.
 * 
 * @author zacho
 */
public class InitialDialogSample {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
	if (!InitialDialogBeans.INSTANCE.getCheckboxState()) {
	            final JFrame frame = InitialDialogBeans.INSTANCE.getPathPanel();
            frame.setVisible(true);
        }
    }

}
