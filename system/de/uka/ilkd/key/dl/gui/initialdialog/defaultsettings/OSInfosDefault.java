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
package de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings;

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

    private OperatingSystem os;
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
            os = OperatingSystem.LINUX;
            defaultPropertiesClass = LinuxOsDefaultProperties.class;
        } else if (str.contains("windows")) {
            os = OperatingSystem.WINDOWS;
            defaultPropertiesClass = WindowsOsDefaultProperties.class;
        } else if (str.contains("mac")) {
            os = OperatingSystem.OSX;
            defaultPropertiesClass = MacOsDefaultProperties.class;
        }

    }

    /**
     * @return the Operating systems name as a string. i.e "linux", or "windows"
     *         or "mac"
     */
    public OperatingSystem getOs() {
        return os;
    }
    public void setDefaultProperty(){
	
	setDefaultPropertiesClass();
	this.props = defaultProperties.getDefaultPropertyList();
    }
    /**
     *  Sets the default properties
     */
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
     * @return <em> Properties </em> the default property list as a Properties Object.
     */
    public Properties getDefaultProperty() {
        setDefaultProperty();
        return props;
    }
    /**
     * Gets the default property of a particular given Key
     *  @param key <em> string </em>
     *  @return <em> string </em> the default property value .
     */
    public String getDefaultProperty(String key){
	 setDefaultProperty();
	return props.getProperty(key);
    }
    
    
}
