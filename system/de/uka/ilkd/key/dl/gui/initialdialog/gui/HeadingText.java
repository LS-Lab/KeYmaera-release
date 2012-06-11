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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author zacho
 * 
 *         The HeadingText Class represents a heading headingPane. The headingPane will be
 *         placed above the GUI to indicate the gui's functions
 */
public class HeadingText {

    private JPanel headingPane;
    private JLabel headerText1;
    private JLabel headerText2;
    private Dimension HeadingDimension;

    /**
     * Initialises the GUI desciption text.
     */
    HeadingText(String hText, String str1) {
        headingPane = new JPanel();
        HeadingDimension = new Dimension(600,35);
        headingPane.setBackground(new java.awt.Color(255, 255, 255));
        headingPane.setLayout(new BorderLayout());
        this.headerText1 = new JLabel(" ".concat(hText));
        this.headerText2 = new JLabel("          ".concat(str1));
        headerText1.setPreferredSize(HeadingDimension);
        headerText1.setFont(new java.awt.Font("Dialog", Font.BOLD, 14));
        headerText2.setFont(new java.awt.Font("Dialog", 0, 12));
        headingPane.add(headerText1, BorderLayout.NORTH);
        headingPane.add(headerText2, BorderLayout.CENTER);
        headingPane.add(new JLabel(" "), BorderLayout.SOUTH);
        
    }
    /**
     * @param HeadingDimension the HeadingDimension to set
     */
    public void setD(Dimension HeadingDimension) {
        this.HeadingDimension = HeadingDimension;
    }
    /**
     * @return the headingPane containing the GUI heading
     */
    public JPanel getDescriptionText() {
        return this.headingPane;

    }

}
