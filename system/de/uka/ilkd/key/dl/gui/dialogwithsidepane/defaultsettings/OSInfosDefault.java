/**
 * 
 */
package de.uka.ilkd.key.dl.gui.dialogwithsidepane.defaultsettings;

import java.util.Locale;
import java.util.Properties;

/**
 *         The OSInfosDefault Class represents default properties. The default
 *         properties are loaded in a Properties object with repect to which
 *         operating system we are curently running on.
 *         
 *         @author zacho
 */
public class OSInfosDefault {

    public static final OSInfosDefault INSTANCE = new OSInfosDefault();

    private String OsName;
    private Properties props;
    private Class<? extends IOsDefaultProperties> defaultPropertiesClass;
    private IOsDefaultProperties defaultProperties;

    /**
     * Constructor initialises the defaultPropertyclass depending on the current
     * operating system.
     */
    private OSInfosDefault() {

        String str;
        str = System.getProperty("os.name");
        str = str.toLowerCase(Locale.ENGLISH);
        if (str.contains("linux")) {
            OsName = "linux";
            defaultPropertiesClass = LinuxOsDefaultProperties.class;
        } else if (str.contains("windows")) {
            OsName = "windows";
            defaultPropertiesClass = WindowsOsDefaultProperties.class;
        } else if (str.contains("mac")) {
            OsName = "mac";
            defaultPropertiesClass = MacOsDefaultProperties.class;
        }

    }

    /**
     * @return the Operating systems name as a string. i.e "linux", or "windows"
     *         or "mac"
     */
    public String getOsName() {
        System.out.println("You are working on : " + OsName  + " Operating System."); // XXX
        return OsName;
    }
    public void setDefaultProperty(Properties props){
	
	setDefaultPropertiesClass();
	props = defaultProperties.getDefaultPropertyList();
	this.props = props;
    }

    public void setDefaultPropertiesClass() {

        try {
           defaultProperties = defaultPropertiesClass
                    .newInstance();

        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     * @return the default property list as a Properties Object.
     */
    public Properties getDefaultProperty() {
        setDefaultProperty(new Properties());
        return props;
    }
    public String getDefaultProperty(String key){
	 setDefaultProperty(new Properties());
	return props.getProperty(key);
    }
    
    
}