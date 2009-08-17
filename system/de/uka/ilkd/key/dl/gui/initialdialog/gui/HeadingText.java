/**
 * 
 */
package de.uka.ilkd.key.dl.gui.initialdialog.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author zacho
 * 
 *         The HeadingText Class represents a heading panel. The panel will be
 *         placed above the GUI to indicate the gui's functions
 */
public class HeadingText {

    private JPanel panel;

    /**
     * Initialises the GUI desciption text.
     */
    HeadingText() {
        panel = new JPanel();
        panel.setBackground(new java.awt.Color(255, 255, 255));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JLabel label1 = new JLabel(
                "Select Solvers Properties and File Locations:");
        JLabel label2 = new JLabel(
                "KeYmaera stores the corresponding  paths and properties for the each solver");
        label1.setPreferredSize(new Dimension(620, 18));
        label1.setFont(new java.awt.Font("Dialog", Font.BOLD, 14));
        label2.setFont(new java.awt.Font("Dialog", 0, 12));
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.gridy = 0;
        c.insets = new Insets(10, 3, 0, 10);
        panel.add(label1, c);
        c.insets = new Insets(10, 10, 20, 10);
        c.gridy = 1;
        panel.add(label2, c);

    }
    /**
     * @return the panel containing the GUI heading
     */
    public JPanel getDescriptionText() {
        return this.panel;

    }

}
