/**
 * 
 */
package de.uka.ilkd.key.dl.gui;

import javax.swing.JFrame;

import de.uka.ilkd.key.dl.gui.initialdialog.gui.InitialDialogBeans;
import de.uka.ilkd.key.gui.Main;

/**
 * 
 * This class serves as the Main class for KeYmaera. It shows an initial dialog
 * that allows the configuration of several solvers. It is run before starting
 * the original Main class to allow for example to set the location of the
 * natives for some solvers like Mathematica and J/Link.
 * 
 * @author zacho
 * @author jdq
 */
public class KeYmaera {

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
