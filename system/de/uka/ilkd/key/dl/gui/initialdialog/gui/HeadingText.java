/**
 * 
 */
package de.uka.ilkd.key.dl.gui.initialdialog.gui;

import java.awt.Color;
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
    private JLabel headerText;
    private JLabel text1;
    private Dimension d;
    private Color col;

    /**
     * Initialises the GUI desciption text.
     */
    HeadingText(String hText, String str1) {
        panel = new JPanel();
        d = new Dimension(620, 18);
        col = new java.awt.Color(255, 255, 255);
        panel.setBackground(col);
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        this.headerText = new JLabel(hText);
        this.text1 = new JLabel(str1);
        headerText.setPreferredSize(d);
        headerText.setFont(new java.awt.Font("Dialog", Font.BOLD, 14));
        text1.setFont(new java.awt.Font("Dialog", 0, 12));
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.gridy = 0;
        c.insets = new Insets(10, 3, 0, 10);
        panel.add(headerText, c);
        c.insets = new Insets(10, 10, 20, 10);
        c.gridy = 1;
        panel.add(text1, c);

    }
    /**
     * @param d the d to set
     */
    public void setD(Dimension d) {
        this.d = d;
        headerText.setPreferredSize(d);
    }
    /**
     * @param col the col to set
     */
    public void setCol(Color col) {
        this.col = col;
        panel.setBackground(col);
    }
    /**
     * @return the panel containing the GUI heading
     */
    public JPanel getDescriptionText() {
        return this.panel;

    }

}