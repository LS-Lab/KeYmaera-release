/**
 * 
 */
package de.uka.ilkd.key.dl.gui.initialdialog;

import javax.swing.JFrame;

import de.uka.ilkd.key.dl.gui.initialdialog.gui.InitialDialogBeans;
import de.uka.ilkd.key.gui.Main;

/**
 * 
 * The PropertyConfiguratorSample class create and instance of property
 * confirugration Frame, whereby the properties can be created or modified.
 * 
 * @author zacho
 */
public class MainWithInitialDialog {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		InitialDialogBeans dialog = new InitialDialogBeans(args);
		if (!dialog.getCheckboxState()) {
			final JFrame frame = dialog.getPathPanel();
			frame.setVisible(true);
		} else {
			final JFrame frame = dialog.getPathPanel();
			frame.dispose();
			Main.main(args);
		}
	}

}
