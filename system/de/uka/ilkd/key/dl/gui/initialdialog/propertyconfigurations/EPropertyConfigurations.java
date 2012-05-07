/*******************************************************************************
 * Copyright (c) 2010 Zacharias Mokom.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Zacharias Mokom - initial API and implementation
 *     Jan-David Quesel - ToolPathes
 ******************************************************************************/
package de.uka.ilkd.key.dl.gui.initialdialog.propertyconfigurations;

import java.beans.PropertyEditor;

import de.uka.ilkd.key.dl.gui.initialdialog.converters.*;
import de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.EToolPath;
import de.uka.ilkd.key.dl.gui.initialdialog.propertyeditors.CheckBoxEditor;
import de.uka.ilkd.key.dl.options.DirectoryPropertyEditor;
import de.uka.ilkd.key.dl.options.FilePropertyEditor;
import  de.uka.ilkd.key.dl.options.EPropertyConstant;
/**
 * The EPropertyConfigurations Enumeration describes properties that are to be
 * configured and shown in the configuration frame.
 * 
 * <br>
 * <b>NB</b> To add a new property Option to be configured, all the parameters
 * must be given and the default value must be added to the all defaults
 * property classes, that is Linux, Mac and
 * WindowsOsDefaultProperties.class</br>
 * 
 * @author zacho
 */
public enum EPropertyConfigurations {

	OPTIONS_MATHEMATICA_PATH(EPropertyConstant.MATHEMATICA_OPTIONS_MATHEMATICA_PATH,
			DirectoryPropertyEditor.class, FileStringConverter.class),
	OPTIONS_MATHKERNEL(EPropertyConstant.MATHEMATICA_OPTIONS_MATHKERNEL, FilePropertyEditor.class,
			FileStringConverter.class),
	OPTIONS_JLINK_LIBDIR(EPropertyConstant.MATHEMATICA_OPTIONS_JLINK_LIBDIR,
			EConfigurationFiles.WEBSTART_FILE, DirectoryPropertyEditor.class,
			FileStringConverter.class),

    OPTIONS_REDUCE_BINARY_PATH(EPropertyConstant.OPTIONS_REDUCE_BINARY,
            FilePropertyEditor.class, FileStringConverter.class,
            EToolPath.REDLOG),
            
    OPTIONS_Z3_BINARY_PATH(EPropertyConstant.Z3_OPTIONS_Z3_BINARY, FilePropertyEditor.class,
            FileStringConverter.class, EToolPath.Z3),
            
	OPTIONS_QEPCAD_PATH(EPropertyConstant.QEPCAD_OPTIONS_QEPCAD_PATH,
			DirectoryPropertyEditor.class, FileStringConverter.class),
	OPTIONS_SACLIB_PATH(EPropertyConstant.QEPCAD_OPTIONS_SACLIB_PATH,
			DirectoryPropertyEditor.class, FileStringConverter.class),

	OPTIONS_CSDP_BINARY(EPropertyConstant.DLOPTIONS_CSDP_PATH,
			FilePropertyEditor.class, FileStringConverter.class, EToolPath.CSDP),

	OPTIONS_OCAML_PATH(EPropertyConstant.HOL_OPTIONS_OCAML_PATH,
			FilePropertyEditor.class, FileStringConverter.class),

	OPTIONS_HOL_LIGHT_PATH(EPropertyConstant.HOL_OPTIONS_HOLLIGHT_PATH,
			DirectoryPropertyEditor.class, FileStringConverter.class),

	OPTIONS_H_QE_PATH(EPropertyConstant.HOL_OPTIONS_HARRISON_QE_PATH,
			DirectoryPropertyEditor.class, FileStringConverter.class),
			
	CHECKBOX_PROPERTY(EPropertyConstant.INITIAL_DIALOG_CHECKBOX, EConfigurationFiles.STARTUP_PROPERTY_FILE,CheckBoxEditor.class,
			BooleanStringConverter.class);


	private String group;
	private String key;
	private String label;
	private String toolTip;
	private EConfigurationFiles configFile;
	private Class<? extends PropertyEditor> editorClass;
	private Class<? extends IPropertyConverter> converterClass;
	private EToolPath toolPath;

	EPropertyConfigurations(EPropertyConstant propsConstant,
			Class<? extends PropertyEditor> propertyEditorClass,
			Class<? extends IPropertyConverter> converterClass) {

	    this(propsConstant,
			EConfigurationFiles.KEY_PROPERTY_FILE, propertyEditorClass,
			converterClass);
	}
	
	EPropertyConfigurations(EPropertyConstant propsConstant,
	        Class<? extends PropertyEditor> propertyEditorClass,
	        Class<? extends IPropertyConverter> converterClass, EToolPath toolPath) {
	    
	    this(propsConstant,
	            EConfigurationFiles.KEY_PROPERTY_FILE, propertyEditorClass,
	            converterClass, toolPath);
	}

	/**
	 * 
	 * @param propsConstant
	 *            This enum type contains: <em>String</em> Property key, <em>String</em> property label, <em>String</em> property tooltip  <em>String</em> property group Name
	 * @param propertyConfigFile
	 *            EConfigurationFiles, Where to find and store property.
	 * @param propertyEditorClass
	 *            Class<? extends PropertyEditor>, Property editor class to be
	 *            used.
	 * @param converterClass
	 *            Class<? extends IPropertyConverter>, Converter to be used.
	 */
	EPropertyConfigurations(EPropertyConstant propsConstant,
			EConfigurationFiles propertyConfigFile,
			Class<? extends PropertyEditor> propertyEditorClass,
			Class<? extends IPropertyConverter> converterClass) {


	        this.group = propsConstant.getGroupName();
		this.label = propsConstant.getLabel();
		this.key = propsConstant.getKey();
		this.toolTip = propsConstant.getToolTip();
		this.configFile = propertyConfigFile;
		this.editorClass = propertyEditorClass;
		this.converterClass = converterClass;
	}

	/**
	 * 
	 * @param propsConstant
	 *            This enum type contains: <em>String</em> Property key, <em>String</em> property label, <em>String</em> property tooltip  <em>String</em> property group Name
	 * @param propertyConfigFile
	 *            EConfigurationFiles, Where to find and store property.
	 * @param propertyEditorClass
	 *            Class<? extends PropertyEditor>, Property editor class to be
	 *            used.
	 * @param converterClass
	 *            Class<? extends IPropertyConverter>, Converter to be used.
	 */
	EPropertyConfigurations(EPropertyConstant propsConstant,
	        EConfigurationFiles propertyConfigFile,
	        Class<? extends PropertyEditor> propertyEditorClass,
	        Class<? extends IPropertyConverter> converterClass, EToolPath toolPath) {
	    this(propsConstant, propertyConfigFile, propertyEditorClass, converterClass);
	    this.toolPath = toolPath;
	}

	
	/**
     * @return the toolPath
     */
    public EToolPath getToolPath() {
        return toolPath;
    }
    
	/**
	 * @return the group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * 
	 * @return configuration file (Property file) name (as a string)that
	 *         contains the needed property
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return the configFile
	 */
	public EConfigurationFiles getConfigFile() {
		return configFile;
	}

	/**
	 * @return the editorClass
	 */
	public Class<? extends PropertyEditor> getEditorClass() {
		return editorClass;
	}

	/**
	 * @return the converterClass
	 */
	public Class<? extends IPropertyConverter> getConverterClass() {
		return converterClass;
	}

	/**
	 * @return the toolTip
	 */
	public String getToolTip() {
		return toolTip;
	}

}
