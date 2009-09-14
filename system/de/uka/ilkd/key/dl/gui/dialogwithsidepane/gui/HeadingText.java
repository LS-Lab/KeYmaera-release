package de.uka.ilkd.key.dl.gui.dialogwithsidepane.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

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

    /**
     * Initialises the GUI desciption text.
     */
    HeadingText(String hText, String str1) {
        panel = new JPanel();
        d = new Dimension(600,35);
        panel.setBackground(new java.awt.Color(255, 255, 255));
        panel.setLayout(new BorderLayout());
        this.headerText = new JLabel(" ".concat(hText));
        this.text1 = new JLabel("          ".concat(str1));
        headerText.setPreferredSize(d);
        headerText.setFont(new java.awt.Font("Dialog", Font.BOLD, 14));
        text1.setFont(new java.awt.Font("Dialog", 0, 12));
        panel.add(headerText, BorderLayout.NORTH);
        panel.add(text1, BorderLayout.CENTER);
        panel.add(new JLabel(" "), BorderLayout.SOUTH);
        
    }
    /**
     * @param d the d to set
     */
    public void setD(Dimension d) {
        this.d = d;
        headerText.setPreferredSize(d);
    }
    /**
     * @return the panel containing the GUI heading
     */
    public JPanel getDescriptionText() {
        return this.panel;

    }

}