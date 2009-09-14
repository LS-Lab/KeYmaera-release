package de.uka.ilkd.key.dl.gui.dialogwithsidepane.propertyconfigurations;

import java.beans.PropertyEditor;

import de.uka.ilkd.key.dl.gui.dialogwithsidepane.converters.*;
import de.uka.ilkd.key.dl.gui.dialogwithsidepane.propertyeditors.*;


/**
 * The EPropertyConfigurations Enumeration describes properties that are to be
 * configured and shown in the configuration frame. 
 * 
 *@NB To add a new property Option to be configured,
 * all the parameters must be given and the default value must be added to the
 * all defaults property classes, that is Linux, Mac and WindowsOsDefaultProperties.class
 * 
 * @author zacho
 */
public enum EPropertyConfigurations {

    OPTIONS_MATHEMATICA_PATH ("Mathematica Properties", "Mathematica Path","[MathematicaOptions]mathematicaPath", EConfigurationFiles.KEY_PROPERTY_FILE, 
            										DirectoryPropertyEditor.class, FileStringConverter.class),
    OPTIONS_MATHKERNEL ("Mathematica Properties", "MathKernel Path", "[MathematicaOptions]mathKernel", EConfigurationFiles.KEY_PROPERTY_FILE, 
                                                            FilePropertyEditor.class, FileStringConverter.class),
    OPTIONS_JLINK_LIBDIR ("Mathematica Properties","J/Link Native Dir" ,"com.wolfram.jlink.libdir", EConfigurationFiles.WEBSTART_FILE, 
                                                            DirectoryPropertyEditor.class, FileStringConverter.class),
    OPTIONS_SACLIB_PATH ("Qepcad Properties","Saclib Path", "[QepcadOptions]saclibPath",EConfigurationFiles.KEY_PROPERTY_FILE, 
                                                            DirectoryPropertyEditor.class, FileStringConverter.class),
    OPTIONS_QEPCAD_PATH ("Qepcad Properties","Qepcad Path","[QepcadOptions]qepcadPath", EConfigurationFiles.KEY_PROPERTY_FILE, 
                                                            DirectoryPropertyEditor.class, FileStringConverter.class),
    OPTIONS_CSDP_BINARY ("DL Properties","CSDP Binary","[DLOptions]csdpPath", EConfigurationFiles.KEY_PROPERTY_FILE, 
                                                            FilePropertyEditor.class, FileStringConverter.class),
    OPTIONS_OlCAM_PATH ("HOL Light Properties","Olcam Path","[HOLLightOptions]ocamlPath", EConfigurationFiles.KEY_PROPERTY_FILE, 
                                                            FilePropertyEditor.class, FileStringConverter.class),
    OPTIONS_HOL_LIGHT_PATH ("HOL Light Properties","HOL Light Path","[HOLLightOptions]hollightPath", EConfigurationFiles.KEY_PROPERTY_FILE, 
                                                            DirectoryPropertyEditor.class, FileStringConverter.class),
    OPTIONS_H_QE_PATH ("HOL Light Properties","Harrison QE Path","[HOLLightOptions]harrisonqePath", EConfigurationFiles.KEY_PROPERTY_FILE, 
                                                            DirectoryPropertyEditor.class, FileStringConverter.class),                                                       
    OPTIONS_REDUCE_BINARY("Redlog Properties","Reduce Binary","[ReduceOptions]reduceBinary", EConfigurationFiles.KEY_PROPERTY_FILE, 
                                                            FilePropertyEditor.class, FileStringConverter.class),  
    CHECKBOX_PROPERTY("checkBox" ,null,"[DLOptions]showInitialDialog", EConfigurationFiles.KEY_PROPERTY_FILE, 
                                                            CheckBoxEditor.class, BooleanStringConverter.class);
    private String group;
    private String key;
    private String label;
    private EConfigurationFiles configFile;
    private Class<? extends PropertyEditor> editorClass;
    private Class<? extends IPropertyConverter> converterClass;

    /**
     * 
     * @param propertyGroup String, property group name
     * @param propertyLabel String, Property label name
     * @param propertyKey String, property key
     * @param propertyConfigFile EConfigurationFiles, Where to find and store property.
     * @param propertyEditorClass Class<? extends PropertyEditor>, Property editor class to be used.
     * @param converterClass Class<? extends IPropertyConverter>, Converter to be used.
     */
    EPropertyConfigurations(String propertyGroup, String propertyLabel,
            String propertyKey, EConfigurationFiles propertyConfigFile,
            Class<? extends PropertyEditor> propertyEditorClass,
            Class<? extends IPropertyConverter> converterClass) {

        this.group = propertyGroup;
        this.label = propertyLabel;
        this.key = propertyKey;
        this.configFile = propertyConfigFile;
        this.editorClass = propertyEditorClass;
        this.converterClass = converterClass;
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

}