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
package de.uka.ilkd.key.dl.gui.initialdialog.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * This class represents a decision JPanel Object for validating or canceling etc... an operation.
 * @author Zacho
 * 
 */
public class DecisionPane {

    JPanel pane;

    private JButton buttonExit;
    private JButton buttonOK;
    private JButton buttonApply;

    public DecisionPane() {

	
	pane = new JPanel();
	buttonOK = new JButton("    OK   ");
	buttonOK.setDefaultCapable(true);
	buttonApply = new JButton(" Apply ");
	buttonExit = new JButton(" Cancel ");
	pane.setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	c.insets = new Insets(70,3,20,3);
	pane.add(buttonOK,c);
	pane.add(buttonApply,c);
	c.insets = new Insets(70,3,20,15);
	pane.add(buttonExit, c);
    }

    public JPanel getPane() {
	return pane;
    }

    public JButton getButtonExit() {
	return buttonExit;
    }

    public JButton getButtonOK() {
	return buttonOK;
    }

    public JButton getButtonApply() {
	return buttonApply;
    }
    public void addActionListener(ActionListener l) {
	buttonOK.addActionListener(l);
	buttonApply.addActionListener(l);
	buttonExit.addActionListener(l);
    }


}
