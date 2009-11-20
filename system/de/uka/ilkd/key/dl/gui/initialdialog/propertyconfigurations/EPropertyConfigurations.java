package de.uka.ilkd.key.dl.gui.initialdialog.propertyconfigurations;

import java.beans.PropertyEditor;

import de.uka.ilkd.key.dl.gui.initialdialog.converters.*;
import de.uka.ilkd.key.dl.gui.initialdialog.propertyeditors.CheckBoxEditor;
import de.uka.ilkd.key.dl.options.DirectoryPropertyEditor;
import de.uka.ilkd.key.dl.options.FilePropertyEditor;
import de.uka.ilkd.key.dl.options.PropertyConstants;

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
public enum EPropertyConfigurations implements PropertyConstants {

	OPTIONS_MATHEMATICA_PATH(Cat.MATHEMATICA, "Mathematica Path",
			"The path where Mathematica is installed.",
			"[MathematicaOptions]mathematicaPath",
			DirectoryPropertyEditor.class, FileStringConverter.class),

	OPTIONS_MATHKERNEL(Cat.MATHEMATICA, MATHEMATICA_OPTIONS_MATHKERNEL_LABEL,
			MATHEMATICA_OPTIONS_MATHKERNEL_TOOLTIP,
			MATHEMATICA_OPTIONS_MATHKERNEL, FilePropertyEditor.class,
			FileStringConverter.class),

	OPTIONS_JLINK_LIBDIR(Cat.MATHEMATICA,
			MATHEMATICA_OPTIONS_JLINK_LIBDIR_LABEL,
			MATHEMATICA_OPTIONS_JLINK_LIBDIR_TOOLTIP,
			MATHEMATICA_OPTIONS_JLINK_LIBDIR,
			EConfigurationFiles.WEBSTART_FILE, DirectoryPropertyEditor.class,
			FileStringConverter.class),

	OPTIONS_QEPCAD_PATH(Cat.QEPCAD, QEPCAD_OPTIONS_QEPCAD_PATH_LABEL,
			QEPCAD_OPTIONS_QEPCAD_PATH_TOOLTIP, QEPCAD_OPTIONS_QEPCAD_PATH,
			DirectoryPropertyEditor.class, FileStringConverter.class),

	OPTIONS_SACLIB_PATH(Cat.QEPCAD, QEPCAD_OPTIONS_SACLIB_PATH_LABEL,
			QEPCAD_OPTIONS_SACLIB_PATH_TOOLTIP, QEPCAD_OPTIONS_SACLIB_PATH,
			DirectoryPropertyEditor.class, FileStringConverter.class),

	OPTIONS_CSDP_BINARY(Cat.DL, DLOPTIONS_CSDP_PATH_LABEL,
			DLOPTIONS_CSDP_PATH_TOOLTIP, DLOPTIONS_CSDP_PATH,
			FilePropertyEditor.class, FileStringConverter.class),

	OPTIONS_OCAML_PATH(Cat.HOL, HOL_OPTIONS_OCAML_PATH_LABEL,
			HOL_OPTIONS_OCAML_PATH_TOOLTIP, HOL_OPTIONS_OCAML_PATH,
			FilePropertyEditor.class, FileStringConverter.class),

	OPTIONS_HOL_LIGHT_PATH(Cat.HOL, HOL_OPTIONS_HOLLIGHT_PATH_LABEL,
			HOL_OPTIONS_HOLLIGHT_PATH_TOOLTIP, HOL_OPTIONS_HOLLIGHT_PATH,
			DirectoryPropertyEditor.class, FileStringConverter.class),

	OPTIONS_H_QE_PATH(Cat.HOL, HOL_OPTIONS_HARRISON_QE_PATH_LABEL,
			HOL_OPTIONS_HARRISON_QE_PATH_TOOLTIP, HOL_OPTIONS_HARRISON_QE_PATH,
			DirectoryPropertyEditor.class, FileStringConverter.class),

	OPTIONS_REDUCE_BINARY_PATH(Cat.REDLOG, OPTIONS_REDUCE_BINARY_LABEL,
			OPTIONS_REDUCE_BINARY_TOOLTIP, OPTIONS_REDUCE_BINARY,
			FilePropertyEditor.class, FileStringConverter.class),

	// null label is used by PropertiesCard.setCardProperties() to decide that
	// this is a hidden option
	CHECKBOX_PROPERTY(Cat.HIDDEN, null,
			"Check to skip this dialog in the future.",
			"[DLOptions]skipInitialDialog", CheckBoxEditor.class,
			BooleanStringConverter.class);

	/**
	 * Categories used for grouping the different options
	 * 
	 * @author jdq
	 */
	private static enum Cat {
		MATHEMATICA("Mathematica Properties"), QEPCAD("Qepcad Properties"), DL(
				"DL Properties"), HOL("HOL Light Properties"), REDLOG(
				"Redlog Properties"), HIDDEN("hidden");

		String label;

		Cat(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}
	}

	private Cat group;
	private String key;
	private String label;
	private String toolTip;
	private EConfigurationFiles configFile;
	private Class<? extends PropertyEditor> editorClass;
	private Class<? extends IPropertyConverter> converterClass;

	EPropertyConfigurations(Cat propertyGroup, String propertyLabel,
			String toolTip, String propertyKey,
			Class<? extends PropertyEditor> propertyEditorClass,
			Class<? extends IPropertyConverter> converterClass) {
		this(propertyGroup, propertyLabel, toolTip, propertyKey,
				EConfigurationFiles.KEY_PROPERTY_FILE, propertyEditorClass,
				converterClass);
	}

	/**
	 * 
	 * @param propertyGroup
	 *            String, property group name
	 * @param propertyLabel
	 *            String, Property label name
	 * @param propertyKey
	 *            String, property key
	 * @param propertyConfigFile
	 *            EConfigurationFiles, Where to find and store property.
	 * @param propertyEditorClass
	 *            Class<? extends PropertyEditor>, Property editor class to be
	 *            used.
	 * @param converterClass
	 *            Class<? extends IPropertyConverter>, Converter to be used.
	 */
	EPropertyConfigurations(Cat propertyGroup, String propertyLabel,
			String toolTip, String propertyKey,
			EConfigurationFiles propertyConfigFile,
			Class<? extends PropertyEditor> propertyEditorClass,
			Class<? extends IPropertyConverter> converterClass) {

		this.group = propertyGroup;
		this.label = propertyLabel;
		this.key = propertyKey;
		this.toolTip = toolTip;
		this.configFile = propertyConfigFile;
		this.editorClass = propertyEditorClass;
		this.converterClass = converterClass;
	}

	/**
	 * @return the group
	 */
	public String getGroup() {
		return group.getLabel();
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