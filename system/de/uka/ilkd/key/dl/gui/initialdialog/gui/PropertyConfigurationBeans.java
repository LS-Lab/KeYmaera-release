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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uka.ilkd.key.dl.gui.initialdialog.converters.IPropertyConverter;
import de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OSInfosDefault;
import de.uka.ilkd.key.dl.gui.initialdialog.propertyconfigurations.EConfigurationFiles;

/**
 * The PropsSettingsBeans class provides a Gui Object containing the property of
 * a specified property key. The properties of this Gui objects are set using
 * the method setPathPane and the method getPathPane() to get the Gui.
 * 
 * @author zacho
 */
public class PropertyConfigurationBeans implements PropertyChangeListener {

	public static PropertyConfigurationBeans INSTANCE = new PropertyConfigurationBeans();
	private JPanel propertyPane;
	private Class<? extends PropertyEditor> propertyEditorClass; // Property
	private Class<? extends IPropertyConverter> converterClass;
	private Boolean USING_DEFAULT_PROPERTY = false;
	private PropertyEditor propertyEditor;
	private IPropertyConverter converter;
	private ToolInstaller installer;
	private String propsName;
	private String propsFileName; // Property file name where to get the
									// properties
	private String propertyIdentifier; // Property key
	private String propertyPathName; // Property path read into browser using
	// given property key
	private String oldPropsSetting; // Contain string value of the old property
	// object if changed
	private Object currentPropertyObject; // Current displayed property value,
	// will be saved if method
	private JPanel editorPane; // Editor pane, contains property editor object

	/**
	 * Initialises the property editor parameters to default values NB: Must Use
	 * setPathPane(String labelName, Class<? extends PropertyEditor>
	 * propertyEditorClass, Class<? extends IPropertyConverter> converterClass,
	 * EConfigurationFiles configurationFiles, String key) To set parameters.
	 */
	public PropertyConfigurationBeans() {
		propertyPane = new JPanel();
		propertyPane.setLayout(new GridBagLayout());
		editorPane = new JPanel();
		propertyPathName = "/";
	}

	/**
	 * @param labelName
	 *            : The property label to be displayed
	 * @param toolTipText
	 *            The text to display as tool tip
	 * @param propertyEditorClass
	 *            : The property editor class to be used
	 * @param converterClass
	 *            : The conveter class to use with the property editor
	 * @param configurationFiles
	 *            : The confguration file to be used
	 * @param key
	 *            : The property key
	 */
	public void setPathPane(String labelName, String toolTipText,
			Class<? extends PropertyEditor> propertyEditorClass,
			Class<? extends IPropertyConverter> converterClass,
			EConfigurationFiles configurationFiles, String key, ToolInstaller installer) {

		this.propsFileName = configurationFiles.getFileName();
		this.propertyIdentifier = key;
		this.propertyEditorClass = propertyEditorClass;
		this.converterClass = converterClass;
		this.propsName = labelName;
		this.installer = installer;

		readSettings(new Properties());

		if (this.propertyEditorClass != null) {
			setEditorPane();
		}
		addEditorPane(editorPane);
		if (labelName != null)
			addLabel(labelName, toolTipText);
	}

	/**
	 * Sets up the property editor class and gets the customEditor.
	 */
	private void setEditorPane() {
		editorPane = new JPanel(new BorderLayout(5, 0));
		editorPane.setPreferredSize(new Dimension(400, 25));

		try {
			propertyEditor = propertyEditorClass.newInstance();
			converter = converterClass.newInstance();
			editorPane.add(propertyEditor.getCustomEditor());
			propertyEditor.setValue(converter
					.toPropertyEditorValue(propertyPathName));
			currentPropertyObject = propertyEditor.getValue();
			propertyEditor.addPropertyChangeListener(this);
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * @return the propertyEditor
	 */
	public PropertyEditor getPropertyEditor() {
		return propertyEditor;
	}

	/**
	 * @return the currentPropertyObject
	 */
	public Object getCurrentPropertyObject() {
		return currentPropertyObject;
	}

	/**
	 * @return the propsName
	 */
	public String getPropsName() {
		return propsName;
	}

	/**
	 * @param propsName
	 *            the propsName to set
	 */
	public void setPropsName(String propsName) {
		this.propsName = propsName;
	}

	/**
	 * @return the currentPropertyObject
	 */
	public Boolean isPropertyObjectAFile() {
		if (converter.getClass().getCanonicalName().endsWith(
				"FileStringConverter")) {
			return true;
		} else
			return false;
	}

	/**
	 * @param propertyPathObject
	 *            the propertyPathObject to set
	 */
	public void setPropertyPathObject(String propertyPathObject) {
		this.propertyPathName = propertyPathObject;
		propertyEditor.setValue(converter
				.toPropertyEditorValue(propertyPathObject));
		currentPropertyObject = propertyEditor.getValue();
	}

	/**
	 * @return the propertyIdentifier
	 */
	public String getPropertyIdentifier() {
		return propertyIdentifier;
	}

	/**
	 * @return the pathPane
	 */
	public JPanel getPathPane() {
		return propertyPane;
	}

	public void addEditorPane(JPanel pane) {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.gridx = 1;
		propertyPane.add(pane, c);
	}

	/**
	 * Adds the property Label into the editor pane
	 * 
	 * @param LabelName
	 */
	public void addLabel(String LabelName, String toolTipText) {
		JLabel propsLabel = new JLabel(LabelName.concat(" :"));
		if (toolTipText != null) {
			propsLabel.setToolTipText(toolTipText);
		}
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		propsLabel.setPreferredSize(new Dimension(120, 25));
		propertyPane.add(propsLabel, c);

	}

	/**
	 * reads property using propertyIdentifier (key) and configuration file
	 */
	public void readSettings(Properties props) {

		File file = new File(propsFileName);

		if (file.exists()) {
			try {
				props.load(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		propertyPathName = props.getProperty(propertyIdentifier);
		if (propertyPathName == null) {

			props = OSInfosDefault.INSTANCE.getDefaultProperty();
			propertyPathName = props.getProperty(propertyIdentifier);
			System.out.println("Initialized " + propertyIdentifier + " with "
					+ propertyPathName); // XXX
		}
	}

	/**
	 * Given an ConfigurationFile Class a property key and a Converter class,
	 * this method fetches for the corresponding key value
	 * 
	 * @return keyValue
	 */
	public Object getValueOfKey(EConfigurationFiles configurationFiles,
			String key, Class<? extends IPropertyConverter> LconverterClass) {
		Object keyValue = null;
		try {
			final IPropertyConverter converterl = LconverterClass.newInstance();

			this.propsFileName = configurationFiles.getFileName();
			this.propertyIdentifier = key;
			readSettings(new Properties());

			keyValue = converterl.toPropertyEditorValue(propertyPathName);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return keyValue;
	}

	/**
	 * write the given currentProperty on the old on, if old one is changed
	 */
	public void writeSettings(Properties props) {

		File file = new File(propsFileName);
		if (file.exists()) {
			try {
				props.load(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		oldPropsSetting = props.getProperty(propertyIdentifier);
		if (oldPropsSetting == null) {
			Properties defaultProps = new Properties();
			defaultProps = OSInfosDefault.INSTANCE.getDefaultProperty();
			oldPropsSetting = defaultProps.getProperty(propertyIdentifier);
			USING_DEFAULT_PROPERTY = true;
		}
		String currentProperty = converter.toStringValue(currentPropertyObject);
		if (oldPropsSetting == null || !oldPropsSetting.equals(currentProperty)
				|| USING_DEFAULT_PROPERTY) {
			props.setProperty(propertyIdentifier, currentProperty);

			try {
				if (!file.exists()) {
					file.getParentFile().mkdirs();
					file.createNewFile();
				}
				props.store(new FileOutputStream(file), null);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (!USING_DEFAULT_PROPERTY)
				System.out.println("Property  " + propsName
						+ " changed from : " + oldPropsSetting + " to "
						+ props.getProperty(propertyIdentifier));// XXX
			
			else
				System.out.println("Property  " + propsName
						+ " created with value :"
						+ props.getProperty(propertyIdentifier));// XXX
		}

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		currentPropertyObject = propertyEditor.getValue();
	}

    /**
     * @return the installer
     */
    public ToolInstaller getInstaller() {
        return installer;
    }

    /**
     * @param installer the installer to set
     */
    public void setInstaller(ToolInstaller installer) {
        this.installer = installer;
    }

}
