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
import de.uka.ilkd.key.dl.gui.initialdialog.propertyconfigurations.EConfigurationFiles;



/**
 * The PropsSettingsBeans class provides a Gui Object (JPanel) containing the property of a
 * specified property key. The properties of this Gui objects are set using the
 * method setPathPane and the method getPathPane() to get the Gui.
 * 
 *  @author zacho 
 */
public class PropertyConfigurationBeans {

    public static PropertyConfigurationBeans INSTANCE = new PropertyConfigurationBeans();
    private JPanel PropertyPane;
    private Class<? extends PropertyEditor> propertyEditorClass; // Property
    private Class<? extends IPropertyConverter> converterClass;
    private static Boolean USING_DEFAULT_PROPERTY = false;
    private String propsName;
    private String propsFileName; // Property file name where to get the properties
    private String propertyIdentifier; // Property key
    private String propertyPathObject; // Property Object read into browser using given property key
    private String oldPropsSetting; // Contain string value of the old property object if changed
    private Object currentPropertyObject; // Current displayed property value, will be saved if method
                                          // writeSettings() is called
    private JPanel editorPane;                  // Editor pane, contains property editor object

    /**
     * Initialises the property editor parameters to default values NB: Must Use
     * setPathPane(String labelName, Class<? extends PropertyEditor>
     * propertyEditorClass, Class<? extends IPropertyConverter> converterClass,
     * EConfigurationFiles configurationFiles, String key) To set parameters.
     */
    PropertyConfigurationBeans() {
        PropertyPane = new JPanel();
        PropertyPane.setLayout(new GridBagLayout());
        editorPane = new JPanel();
        propertyPathObject = "/";
    }
    
    /**
     * @return the pathPane
     */
    public JPanel getPathPane() {
        return this.PropertyPane;
    }

    /**
     * @param labelName : The property label to be displayed
     * @param propertyEditorClass : The property editor class to be used
     * @param converterClass : The conveter class to use with the property editor
     * @param configurationFiles : The confguration file to be used
     * @param key : The property key
     */
    public void setPathPane(String labelName, Class<? extends PropertyEditor> propertyEditorClass,
                             Class<? extends IPropertyConverter> converterClass,EConfigurationFiles configurationFiles, String key) {

        this.propsFileName = configurationFiles.getFileName();
        this.propertyIdentifier = key;
        this.propertyEditorClass = propertyEditorClass;
        this.converterClass = converterClass;
        this.propsName = labelName;

        readSettings(new Properties());

        if (this.propertyEditorClass != null) {
            setEditorPane();
        }
        addEditorPane(editorPane);
        if (labelName != null)
            addLabel(labelName);

    }

    /**
     * Sets up the property editor class and gets the customEditor.
     */
    private void setEditorPane() {

        this.editorPane = new JPanel(new BorderLayout(5, 0));
        try {
            final PropertyEditor propertyEditor = propertyEditorClass .newInstance();
            final IPropertyConverter converter = converterClass.newInstance();
            this.editorPane.setPreferredSize(new Dimension(470, 25));
            propertyEditor.setValue(converter.toPropertyEditorValue(propertyPathObject));
            this.editorPane.add(propertyEditor.getCustomEditor());
            currentPropertyObject = propertyEditor.getValue();
            propertyEditor.addPropertyChangeListener(new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            currentPropertyObject = propertyEditor.getValue();
                        }
                    });

        } catch (InstantiationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    /**
     *  Adds the customEditor pane into the editor pane
     * @param editorPane
     */
    public void addEditorPane(JPanel editorPane) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        this.PropertyPane.add(editorPane, c);
    }

    /**
     *  Adds the property Label into the editor pane
     * @param LabelName
     */
    public void addLabel(String LabelName) {
        JLabel propsLabel = new JLabel(LabelName);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        propsLabel.setPreferredSize(new Dimension(120, 25));
        this.PropertyPane.add(propsLabel, c);

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
        propertyPathObject = props.getProperty(propertyIdentifier);
        if (propertyPathObject == null) {
            
            props = de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OSInfosDefault.INSTANCE
                                                                        .getDefaultProperty();
            propertyPathObject = props.getProperty(propertyIdentifier);
            System.out.println("Initialized " + propertyIdentifier + " with "  + propertyPathObject);   // XXX
        }
    }
    
    /**
     * Given an ConfigurationFile Class a property key and a Converter class,
     * this method fetches for the corresponding key value
     * 
     * @return keyValue
     */
    public Object getValueOfKey(EConfigurationFiles configurationFiles,
            String key, Class<? extends IPropertyConverter> converterClass) {
        Object keyValue = null;
        try {
            final IPropertyConverter converter = converterClass.newInstance();

            this.propsFileName = configurationFiles.getFileName();
            this.propertyIdentifier = key;
            readSettings(new Properties());

            keyValue = converter.toPropertyEditorValue(propertyPathObject);
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
            defaultProps = de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OSInfosDefault.INSTANCE
                    .getDefaultProperty();
            oldPropsSetting = defaultProps.getProperty(propertyIdentifier);
            USING_DEFAULT_PROPERTY = true;
        }
        try {
            final IPropertyConverter converter = converterClass.newInstance();
            
            String currentProperty = converter.toStringValue(currentPropertyObject);
            
            if (oldPropsSetting == null || !oldPropsSetting.equals(currentProperty)|| USING_DEFAULT_PROPERTY) {
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
                    System.out.println("Property  " + propsName + " changed from : " + oldPropsSetting + " to "
                                                                + props.getProperty(propertyIdentifier));// XXX
                else
                    System.out.println("Property  " + propsName  + " created with value :"
                                                                + props.getProperty(propertyIdentifier));// XXX
            }
        } catch (InstantiationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}