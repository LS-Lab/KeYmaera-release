/*******************************************************************************
 * Copyright (c) 2012 Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jan-David Quesel - initial API and implementation
 ******************************************************************************/
package de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings;

/**
 * @author jdq
 * 
 */
public enum EToolPath {
    REDLOG(
            "http://downloads.sourceforge.net/project/reduce-algebra/reduce-windows32-20110414.zip?r=http%3A%2F%2Fsourceforge.net%2Fprojects%2Freduce-algebra%2Ffiles%2F&ts=1335266884&use_mirror=dfn",
            "http://csd.informatik.uni-oldenburg.de/keymaera/tools/redlog-linux.zip",
            "http://csd.informatik.uni-oldenburg.de/keymaera/tools/redlog-mac.zip"),

    Z3(
            "http://csd.informatik.uni-oldenburg.de/keymaera/tools/z3-windows.zip",
            "http://csd.informatik.uni-oldenburg.de/keymaera/tools/z3-linux.zip",
            "http://csd.informatik.uni-oldenburg.de/keymaera/tools/z3-osx.zip"),

    CSDP(
            "http://csd.informatik.uni-oldenburg.de/keymaera/tools/csdp-windows.zip",
            "http://csd.informatik.uni-oldenburg.de/keymaera/tools/csdp-linux.zip",
            "http://csd.informatik.uni-oldenburg.de/keymaera/tools/csdp-mac.zip"), ;

    final private String windowsUrl;

    final private String linuxUrl;

    final private String macUrl;

    /**
     * 
     */
    private EToolPath(String windowsUrl, String linuxUrl, String macUrl) {
        this.windowsUrl = windowsUrl;
        this.linuxUrl = linuxUrl;
        this.macUrl = macUrl;
    }

    public String getUrl(OperatingSystem os) {
        switch (os) {
        case LINUX:
            return linuxUrl;
        case WINDOWS:
            return windowsUrl;
        case OSX:
            return macUrl;
        default:
            throw new IllegalArgumentException("No URL available for operating system: " + os);
        }
    }

    /**
     * @return the windowsUrl
     */
    public String getWindowsUrl() {
        return windowsUrl;
    }

    /**
     * @return the linuxUrl
     */
    public String getLinuxUrl() {
        return linuxUrl;
    }

    /**
     * @return the macUrl
     */
    public String getMacUrl() {
        return macUrl;
    }

}
