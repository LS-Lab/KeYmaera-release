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
	        // allow selecting directories too. Does no harm on non macs
	        System.setProperty("apple.awt.fileDialogForDirectories","true"); 
	    InitialDialogBeans dialog = new InitialDialogBeans(args);
		if (!dialog.getCheckboxState()) {
			final JFrame frame = dialog.getInitialDialogFrame();
			frame.setVisible(true);
		} else {
			final JFrame frame = dialog.getInitialDialogFrame();
			frame.dispose();
			Main.main(args);
		}
	}

}
