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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OSInfosDefault;
import de.uka.ilkd.key.dl.gui.initialdialog.gui.options.FileExistenceVerification;
import de.uka.ilkd.key.dl.gui.initialdialog.gui.options.WriteProperties;

/**
 * The PropertyGroupCardBeans class provides a Gui Object containing the properties of the same group.
 * @author Zacho
 *
 */

public class PropertyGroupCardBeans implements ActionListener {

    class DownloadAction extends AbstractAction {

        private ToolInstaller installer;
        
        /**
         * 
         */
        public DownloadAction() {
            super("Download");
        }
        
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            installer.install(null, null);
        }


        /* (non-Javadoc)
         * @see javax.swing.Action#isEnabled()
         */
        @Override
        public boolean isEnabled() {
            return installer != null;
        }
        
        public void setPropertyConfigurations(List<PropertyConfigurationBeans> beans) {
            for(PropertyConfigurationBeans bean: beans) {
                if(bean.getInstaller() != null) {
                    installer = bean.getInstaller();
                    installer.setPropertyEditor(bean.getPropertyEditor());
                    return;
                }
            }
        }
        
    }
    
    private List<PropertyConfigurationBeans> group;
    private JPanel cardPane;
    private JButton buttonDefault;
    private JButton buttonApply;
    private JButton buttonNext;
    private JButton buttonPrevious;
    private JButton download;
    private int index;

    private String groupName;
    
    private DownloadAction downloadAction;

    PropertyGroupCardBeans() {
	cardPane = new JPanel();
	downloadAction = new DownloadAction();
    }

/**
 * This class sets the propertie fields of the group.
 * @param group
 * @param title
 * @param index
 */
    void setPropertiesFields(List<PropertyConfigurationBeans> group,
	    String title, int index) {

	groupName = title;
	this.group = group;

	this.index = index;
	JPanel propsPane = new JPanel();
	propsPane.setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	c.insets = new Insets(3, 0, 10, 0);
	propsPane.add(new JLabel(), c);
	c.insets = new Insets(3, 0, 3, 0);
	int y = 1;
	PropertyConfigurationBeans beans;
	ListIterator<PropertyConfigurationBeans> iter = this.group
	        .listIterator();
	while (iter.hasNext()) {
	    c.gridy = y++;
	    beans = iter.next();
	    beans.getPropertyEditor().addPropertyChangeListener(
		    new PropertyChangeListener() {
		        public void propertyChange(PropertyChangeEvent evt) {
			    buttonApply.setEnabled(true);
			    buttonDefault.setEnabled(true);
		        }
		    });
	    propsPane.add(beans.getPathPane(), c);
	} 
	c.gridy = y;
	c.insets = new Insets(20, 0, 3, 0);
	propsPane.add(new JLabel(), c);
	cardPane.setLayout(new BorderLayout());
	cardPane.add(propsPane, BorderLayout.NORTH);
	cardPane.add(decisionPanel(), BorderLayout.SOUTH);
	TitledBorder border = new TitledBorder(BorderFactory
	        .createEtchedBorder(), title, 2, 0);
	border.setTitleColor(Color.gray);
	propsPane.setBorder(border);

	
	downloadAction.setPropertyConfigurations(group);
	download.setEnabled(downloadAction.isEnabled());
    }

    /**
     * @return the group.
     */
    public List<PropertyConfigurationBeans> getGroup() {
	return group;
    }

    /**
     * @return the cardPane
     */
    public JPanel getCardPane() {
	return cardPane;
    }

    public void setPropertyChanges(String PropertyKey, String value) {

	ListIterator<PropertyConfigurationBeans> iter = group.listIterator();
	while (iter.hasNext()) {
	    if (iter.next().getPropertyIdentifier().equals(PropertyKey)) {
		iter.previous().setPropertyPathObject(value);
		iter.next();
	    }
	}
    }
    /**
     * Adds button listeners
     * @param l <em> ActionListener </em>
     */
    public void addButtonListeners(ActionListener l) {

	buttonNext.addActionListener(l);
	buttonPrevious.addActionListener(l);

    }
    /**
     * 
     * @return a Decision panel for Apply Restore Defaults Next and previous buttons.
     */
    public JPanel decisionPanel() {

	buttonApply = new JButton(" Apply ");
	buttonApply.setToolTipText("Apply only changes that were done here in " + groupName+".");
	buttonDefault = new JButton(" Restore Defaults ");
	buttonDefault.setToolTipText("Restore default " + groupName +".");
	buttonNext = new JButton(" Next ");
	buttonNext.setToolTipText("Go to next property Group. Note that this will not apply recent changes.");
	buttonPrevious = new JButton(" Previous ");
	buttonPrevious.setToolTipText("Go to previous property Group. Note that this will not apply recent changes.");

	buttonApply.addActionListener(this);
	buttonDefault.addActionListener(this);
	buttonNext.addActionListener(this);
	buttonPrevious.addActionListener(this);
	buttonPrevious.setEnabled(false);

	download = new JButton("Download");
	download.setToolTipText("Download this tool for your operating system.");
	download.setAction(downloadAction);
	
	JPanel pane1 = new JPanel();
	pane1.setLayout(new FlowLayout());
	pane1.add(buttonPrevious, FlowLayout.LEFT);
	pane1.add(buttonNext);

	JPanel pane2 = new JPanel();
	pane2.setLayout(new FlowLayout());
	pane2.add(download);
	pane2.add(buttonDefault);
	pane2.add(buttonApply);

	JPanel decisionPane = new JPanel();
	decisionPane.setLayout(new BorderLayout());
	decisionPane.add(pane2, BorderLayout.WEST);
	decisionPane.add(pane1, BorderLayout.EAST);
	return decisionPane;
    }

    /**
     * @return the buttonNext
     */
    public JButton getButtonNext() {
	return buttonNext;
    }

    /**
     * @return the buttonPrevious
     */
    public JButton getButtonPrevious() {
	return buttonPrevious;
    }
    /**
     * @return the GroupName
     */
    public String getGroupName() {
	return groupName;
    }
    /**
     * @return the index
     */
    public int getIndex() {
	return index;
    }
    /**
     * @param CardsIndex
     * @return true if available or false if otherwise
     */
    public Boolean isNextAvailable(int CardsIndex) {

	if (index < (CardsIndex - 1) && index >= -1)
	    return true;
	else
	    return false;

    }

    /**
     * @param CardsIndex
     * @return true if available or false if otherwise
     */
    public Boolean isPreviousAvailable(int CardsIndex) {

	if (index > 0 && index < CardsIndex)
	    return true;
	else
	    return false;

    }
    /**
     * @return the NextCardIndex
     */
    public int getNextCardIndex() {
	return index + 1;
    }
    /**
     * @return the PreviousCardIndex
     */
    public int getPreviousCardIndex() {
	return index - 1;
    }
    /**
     * Writes the properties changes
     */
    public void writePropertyChanges() {
	WriteProperties.write(group);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

	if (e.getSource().equals(buttonApply)) {
	    if (FileExistenceVerification.verifyDirectories(group, cardPane) == JOptionPane.YES_OPTION) {
		writePropertyChanges();
		buttonApply.setEnabled(false);
	    }
	}
	if (e.getSource().equals(buttonDefault)) {
	    Properties props = OSInfosDefault.INSTANCE.getDefaultProperty();

	    ListIterator<PropertyConfigurationBeans> iter = group
		    .listIterator();
	    String key;
	    while (iter.hasNext()) {	 
		key = iter.next().getPropertyIdentifier();
		setPropertyChanges(key, props.getProperty(key));
	    }
	    buttonDefault.setEnabled(false);
	}
    }
}
