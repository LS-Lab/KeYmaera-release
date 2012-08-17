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
            FileType.ZIP,
            "http://downloads.sourceforge.net/project/reduce-algebra/reduce-x86_64-unknown-ubuntu10.10-20110414.tar.bz2?r=http%3A%2F%2Fsourceforge.net%2Fprojects%2Freduce-algebra%2Ffiles%2F&ts=1336473098&use_mirror=netcologne",
            FileType.TARBZ2,
            "http://downloads.sourceforge.net/project/reduce-algebra/reduce-x86_64-unknown-ubuntu10.10-20110414.tar.bz2?r=http%3A%2F%2Fsourceforge.net%2Fprojects%2Freduce-algebra%2Ffiles%2F&ts=1336473098&use_mirror=netcologne",
            FileType.TARBZ2,
            "http://downloads.sourceforge.net/project/reduce-algebra/reduce-x86_64-mac_10.6_snowleopard-darwin10.7.0-20110414.tar.bz2?r=http%3A%2F%2Fsourceforge.net%2Fprojects%2Freduce-algebra%2Ffiles%2F&ts=1336473164&use_mirror=dfn",
            FileType.TARBZ2),

    Z3(
            "http://csd.informatik.uni-oldenburg.de/keymaera/tools/z3-windows.zip",
            FileType.ZIP,
            "http://research.microsoft.com/projects/z3/z3-x64-4.1.tar.gz",
            FileType.TARGZ,
            "http://research.microsoft.com/projects/z3/z3-4.1.tar.gz",
            FileType.TARGZ,
            "http://research.microsoft.com/en-us/um/redmond/projects/z3/z3-osx-4.1-x64.tar.gz",
            FileType.TARGZ),

    CSDP(
            "http://www.coin-or.org/download/binary/Csdp/csdp6.1.0winp4.tgz",
            FileType.TARGZ,
            "http://www.coin-or.org/download/binary/Csdp/csdp6.1.0linuxp4.tgz",
            FileType.TARGZ,
            "http://www.coin-or.org/download/binary/Csdp/csdp6.1.0linuxp4.tgz",
            FileType.TARGZ,
            "http://www.coin-or.org/download/binary/Csdp/csdp6.0.1maccore.tgz",
            FileType.TARGZ), ;

    public enum FileType {
        ZIP, TARGZ, TARBZ2;
    }

    final private String windowsUrl;

    final private String linuxUrl;

    final private String macUrl;

    private String linux32Url;

    private FileType wft;

    private FileType lft;

    private FileType l32ft;

    private FileType mft;

    /**
     * 
     */
    private EToolPath(String windowsUrl, FileType wft, String linuxUrl,
            FileType lft, String linux32Url, FileType l32ft, String macUrl,
            FileType mft) {
        this.windowsUrl = windowsUrl;
        this.linuxUrl = linuxUrl;
        this.linux32Url = linux32Url;
        this.macUrl = macUrl;
        this.wft = wft;
        this.lft = lft;
        this.l32ft = l32ft;
        this.mft = mft;
    }

    public String getUrl(OperatingSystem os) {
        switch (os) {
        case LINUX:
            if (!System.getProperty("os.arch").equals("amd64")) {
                return linux32Url;
            }
            return linuxUrl;
        case WINDOWS:
            return windowsUrl;
        case OSX:
            return macUrl;
        default:
            throw new IllegalArgumentException(
                    "No URL available for operating system: " + os);
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
        if (!System.getProperty("os.arch").equals("amd64")) {
            return linux32Url;
        }
        return linuxUrl;
    }

    /**
     * @return the macUrl
     */
    public String getMacUrl() {
        return macUrl;
    }

    /**
     * @return the windowsFileType
     */
    public FileType getWindowsFileType() {
        return wft;
    }

    /**
     * @return the linuxFileType
     */
    public FileType getLinuxFileType() {
        if (!System.getProperty("os.arch").equals("amd64")) {
            return l32ft;
        }
        return lft;
    }

    /**
     * @return the macFileType
     */
    public FileType getMacFileType() {
        return mft;
    }

    /**
     * @param os
     * @return
     */
    public FileType getFileType(OperatingSystem os) {
        switch (os) {
        case LINUX:
            if (!System.getProperty("os.arch").equals("amd64")) {
                return l32ft;
            }
            return lft;
        case WINDOWS:
            return wft;
        case OSX:
            return mft;
        default:
            throw new IllegalArgumentException(
                    "No URL available for operating system: " + os);
        }
    }

}
