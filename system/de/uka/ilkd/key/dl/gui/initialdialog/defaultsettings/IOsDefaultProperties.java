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

import java.util.Properties;

/**
 *         The IOsDefaultProperties Interface class for default properties of different Operating systems.
 *         
 *         @author zacho
 * 
 */
public interface IOsDefaultProperties {

    /**
     * @return the initial properties list as a Property Object
     */
    public Properties getDefaultPropertyList();

  
}
