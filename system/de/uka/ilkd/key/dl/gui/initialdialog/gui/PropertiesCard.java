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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OSInfosDefault;
import de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OperatingSystem;
import de.uka.ilkd.key.dl.gui.initialdialog.gui.options.FileExistenceVerification;
import de.uka.ilkd.key.dl.gui.initialdialog.gui.options.MathematicaSuffixFinder;
import de.uka.ilkd.key.dl.gui.initialdialog.propertyconfigurations.EPropertyConfigurations;

/**
 * The PropertiesCard class provides a Gui Object containing the
 * the TabbedPane for all the properties. It contains all group properties according to
 * EPropertyConfigurations Enum class.
 * @author zacho
 *
 */
public class PropertiesCard implements ActionListener, ChangeListener { 
    
    private LinkedHashMap<String, List<PropertyConfigurationBeans>> groupMap;
    private HashMap<String, PropertyGroupCardBeans> groupCardMap;
    private PropertyGroupCardBeans currentCard;
    private JTabbedPane propertiesCardPane;
    private static PropertyConfigurationBeans checkBoxEditor;
    
    PropertiesCard(){
	
	    groupMap = new LinkedHashMap<String, List<PropertyConfigurationBeans>>();
	    groupCardMap = new HashMap<String, PropertyGroupCardBeans>();
	    currentCard = new PropertyGroupCardBeans();
	    propertiesCardPane = new JTabbedPane();
	    propertiesCardPane.setTabPlacement(SwingConstants.LEFT);
	    setCardProperties();
		
	
    }
    /**
     * This method sets the CardProperties by grouping each property in its corresponding group.
     * The properties used are those written in EPropertyConfigurations class.
     */
    public void setCardProperties() {
        for (EPropertyConfigurations k : EPropertyConfigurations.values()) {
            if (k.getKey() == "[StartUpOptions]skipInitialDialog") {
                checkBoxEditor = new PropertyConfigurationBeans();
                checkBoxEditor.setPathPane(k.getLabel(), k.getToolTip(),
                        k.getEditorClass(), k.getConverterClass(),
                        k.getConfigFile(), k.getKey(), null);
                continue;
            }
            final PropertyConfigurationBeans editor = new PropertyConfigurationBeans();
            if (k.getToolPath() != null) {
                OperatingSystem os = OSInfosDefault.INSTANCE.getOs();
                editor.setPathPane(
                        k.getLabel(),
                        k.getToolTip(),
                        k.getEditorClass(),
                        k.getConverterClass(),
                        k.getConfigFile(),
                        k.getKey(),
                        new ToolInstaller(k.getToolPath().name(), k
                                .getToolPath().getUrl(
                                        os), k.getToolPath().getFileType(os),k
                                .getPropertySetter(os)));
            } else {
                editor.setPathPane(k.getLabel(), k.getToolTip(),
                        k.getEditorClass(), k.getConverterClass(),
                        k.getConfigFile(), k.getKey(), null);
            }
            List<PropertyConfigurationBeans> editorsInGroup = groupMap.get(k
                    .getGroup());
            if (editorsInGroup == null) {
                editorsInGroup = new LinkedList<PropertyConfigurationBeans>();
                groupMap.put(k.getGroup(), editorsInGroup);
            }
            editorsInGroup.add(editor);
        }
        int index = 0;
        for (String groupIdentifier : groupMap.keySet()) {
            PropertyGroupCardBeans card = new PropertyGroupCardBeans();
            card.setPropertiesFields(groupMap.get(groupIdentifier),
                    groupIdentifier, index++);
            card.addButtonListeners(this);
            groupCardMap.put(groupIdentifier, card);
            propertiesCardPane.add(groupIdentifier, card.getCardPane());
        }
        propertiesCardPane.addChangeListener(this);
        currentCard = groupCardMap.get("Mathematica Properties");
        if (!(currentCard == null))
            propertiesCardPane.setSelectedIndex(currentCard.getIndex());
        MathematicaSuffixFinder m = new MathematicaSuffixFinder();
        m.setMathematicaSuffixFinder(groupCardMap.get("Mathematica Properties"));
    }
   
    @Override
    public void actionPerformed(ActionEvent e) {
	// TODO Auto-generated method stub
	if(e.getSource().equals(currentCard.getButtonNext())){
	    if(currentCard.isNextAvailable(propertiesCardPane.getTabCount())) {
		if (FileExistenceVerification.verifyDirectories(currentCard.getGroup(),currentCard.getCardPane()) == JOptionPane.YES_OPTION){
		    propertiesCardPane.setSelectedIndex(currentCard.getNextCardIndex());
		    currentCard = groupCardMap.get(propertiesCardPane.getTitleAt(propertiesCardPane.getSelectedIndex()));	
		}
	    }	
	}
	if(e.getSource().equals(currentCard.getButtonPrevious())){
	    if(currentCard.isPreviousAvailable(propertiesCardPane.getTabCount())) { 
		propertiesCardPane.setSelectedIndex(currentCard.getPreviousCardIndex());
		currentCard = groupCardMap.get(propertiesCardPane.getTitleAt(propertiesCardPane.getSelectedIndex()));		
	    }	
	}

    }
    @Override
    public void stateChanged(ChangeEvent arg0) {
	// TODO Auto-generated method stub
	currentCard = groupCardMap.get(propertiesCardPane.getTitleAt(propertiesCardPane.getSelectedIndex()));
	if(currentCard.isNextAvailable(propertiesCardPane.getTabCount()))
	    currentCard.getButtonNext().setEnabled(true);
	else
	    currentCard.getButtonNext().setEnabled(false);  
	   
	    
	if(currentCard.isPreviousAvailable(propertiesCardPane.getTabCount())) 
	    currentCard.getButtonPrevious().setEnabled(true);  
	else 
	    currentCard.getButtonPrevious().setEnabled(false);
	
    }

    /**
     * @return the propertiesCardPane
     */
    public JTabbedPane getPropertiesCardPane() { 
        return propertiesCardPane;
    }
    /**
     * @return the groupCardMap
     */
    public HashMap<String, PropertyGroupCardBeans> getGroupCardMap() {
        return groupCardMap;
    }
    /**
     * @return the currentCard
     */
    public PropertyGroupCardBeans getCurrentCard() {
        return currentCard;
    }
    public static Boolean getCheckboxState() {

	return 	(Boolean) checkBoxEditor.getCurrentPropertyObject();
    }

    /**
     * @return the checkBoxEditor
     */
    public PropertyConfigurationBeans getCheckBoxEditor() {
        return checkBoxEditor;
    }
   
}
