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
package de.uka.ilkd.key.dl.gui.initialdialog.propertyeditors;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * 
 * The CheckBoxEditor class creates a checkbox property editor which supports Bollean values.
 * @author zacho
 */
public class CheckBoxEditor extends PropertyEditorSupport implements
                                                            PropertyEditor, ActionListener {
    private JPanel panel;
    private JCheckBox checkBox;
    private Boolean checkBoxState;

    /**
     * This method initialises the checkbox editor.
     */
    public CheckBoxEditor() {
        checkBoxState = false;
        checkBox = new JCheckBox(" Always use these settings as default.",
                checkBoxState);
        checkBox.setFont(new java.awt.Font("Dialog", 0, 12));
        setTooltip("Check to skip this dialog in the future.");
        checkBox.addActionListener(this);
        panel = new JPanel(new BorderLayout());
        panel.add(checkBox, BorderLayout.LINE_START);

    }

    /** Set tooltip for the checkbox */
    public void setTooltip(String tooltip){	
	checkBox.setToolTipText(tooltip);
    }
    /*
     * (non-Javadoc) @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        setValue(checkBox.isSelected());
    }

    /** * @return custom editor panel */

    public Component getCustomEditor() {
        return panel;
    }

    /**  @return the panel */

    public JPanel getCustomEditorPanel() {
        return panel;
    }

    /**  @return the value */

    public Object getValue() {
        return this.checkBoxState;
    }

    /** @return true if the editor is paintable */
    public boolean isPaintable() {
        return true;
    }

    public void setValue(Object val) {
        boolean value;
        if (val instanceof String) {
            value = Boolean.parseBoolean((String) val);
        } else {
            value = (Boolean) val;
        }
        if (!checkBoxState.equals(value)) {
            checkBoxState = (Boolean) value;
            checkBox.setSelected(checkBoxState);
            firePropertyChange();
        }
    }

    public boolean supportsCustomEditor() {
        return true;
    }

}
