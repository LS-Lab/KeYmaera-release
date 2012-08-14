// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.gui.configuration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class ColorSelector extends JDialog {

    private static final String ADDITIONAL = "Program Highlight Color";

    private static final String UPDATE = "Update Highlight Color";

    private static final String DEFAULT = "Default Highlight Color";

    private Color defaultColor;

    private Color additionalColor;

    private Color updateColor;

    /**
     * creates a new ViewSelector
     * 
     * @param parent
     *            The parent widget of this ViewSelector
     */
    public ColorSelector(JFrame parent) {
        super(parent, "Color Selection", true);
        defaultColor = ProofSettings.DEFAULT_SETTINGS.getViewSettings()
                .getDefaultHighlightColor();
        additionalColor = ProofSettings.DEFAULT_SETTINGS.getViewSettings()
                .getAdditionalHighlightColor();
        updateColor = ProofSettings.DEFAULT_SETTINGS.getViewSettings()
                .getUpdateHighlightColor();
        layoutViewSelector();
        pack();
        setLocation(70, 70);
    }

    private void updateButtons() {

    }

    /** lays out the selector */
    protected void layoutViewSelector() {
        getContentPane().setLayout(new BorderLayout());

        final JComboBox/*<String>*/ comboBox = new JComboBox/*<String>*/();

        comboBox.addItem(DEFAULT);

        comboBox.addItem(UPDATE);

        comboBox.addItem(ADDITIONAL);

        final JColorChooser chooser = new JColorChooser(
                ProofSettings.DEFAULT_SETTINGS.getViewSettings()
                        .getDefaultHighlightColor());
        chooser.getSelectionModel().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if (comboBox.getSelectedItem().equals(DEFAULT)) {
                    defaultColor = chooser.getColor();
                } else if (comboBox.getSelectedItem().equals(UPDATE)) {
                    updateColor = chooser.getColor();
                } else if (comboBox.getSelectedItem().equals(ADDITIONAL)) {
                    additionalColor = chooser.getColor();
                }
            }
        });
        comboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (comboBox.getSelectedItem().equals(DEFAULT)) {
                    chooser.setColor(defaultColor);
                } else if (comboBox.getSelectedItem().equals(UPDATE)) {
                    chooser.setColor(updateColor);
                } else if (comboBox.getSelectedItem().equals(ADDITIONAL)) {
                    chooser.setColor(additionalColor);
                }

            }
        });

        JButton okButton = new JButton("OK");
        okButton.setMnemonic(KeyEvent.VK_ENTER);

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (comboBox.getSelectedItem().equals(DEFAULT)) {
                    ProofSettings.DEFAULT_SETTINGS.getViewSettings()
                            .setDefaultHighlightColor(defaultColor);
                } else if (comboBox.getSelectedItem().equals(UPDATE)) {
                    ProofSettings.DEFAULT_SETTINGS.getViewSettings()
                            .setUpdateHighlightColor(updateColor);
                } else if (comboBox.getSelectedItem().equals(ADDITIONAL)) {
                    ProofSettings.DEFAULT_SETTINGS.getViewSettings()
                            .setAdditionalHighlightColor(additionalColor);
                }

                ProofSettings.DEFAULT_SETTINGS.saveSettings();
                setVisible(false);
                dispose();
            }
        });
        JButton reset = new JButton("Reset to Default");

        reset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                defaultColor = ViewSettings.DEFAULT_DEFAULT_HIGHTLIGHT_COLOR;
                updateColor = ViewSettings.DEFAULT_UPDATE_HIGHTLIGHT_COLOR;
                additionalColor = ViewSettings.DEFAULT_ADDITIONAL_HIGHLIGHT_COLOR;
                if (comboBox.getSelectedItem().equals(DEFAULT)) {
                    chooser.setColor(defaultColor);
                } else if (comboBox.getSelectedItem().equals(UPDATE)) {
                    chooser.setColor(updateColor);
                } else if (comboBox.getSelectedItem().equals(ADDITIONAL)) {
                    chooser.setColor(additionalColor);
                }

            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setMnemonic(KeyEvent.VK_ESCAPE);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(reset);
        buttonPanel.add(cancelButton);

        getContentPane().add(comboBox);
        getContentPane().add(chooser);

        getContentPane().setLayout(
                new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(buttonPanel);

        updateButtons();

    }

    // INNER CLASS TO READ ONLY NUMBERS FOR MAX APPs
    static class NumberDocument extends PlainDocument {

        public void insertString(int offs, String str, AttributeSet a)
                throws BadLocationException {
            if (str == null) {
                return;
            }
            char[] upper = str.toCharArray();
            for (char anUpper : upper) {
                if (anUpper < '0' || anUpper > '9') {
                    return;
                }
            }
            super.insertString(offs, new String(upper), a);
        }

    }

    static class NumberInputField extends JTextField {
        public NumberInputField(int number, int cols) {
            super(cols);
            setText("" + number);
        }

        protected Document createDefaultModel() {
            return new NumberDocument();
        }
    }

}
