/**
 * 
 */
package de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings;

/**
 * @author jdq
 * 
 */
public enum EToolPath {
    REDLOG(
            "http://csd.informatik.uni-oldenburg.de/keymaera/tools/redlog-windows.zip",
            "http://csd.informatik.uni-oldenburg.de/keymaera/tools/redlog-linux.zip",
            "http://csd.informatik.uni-oldenburg.de/keymaera/tools/redlog-mac.zip"), Z3(
            "http://csd.informatik.uni-oldenburg.de/keymaera/tools/z3-windows.zip",
            "http://csd.informatik.uni-oldenburg.de/keymaera/tools/z3-linux.zip",
            "http://csd.informatik.uni-oldenburg.de/keymaera/tools/z3-mac.zip"), QEPCAD(
            "http://csd.informatik.uni-oldenburg.de/keymaera/tools/qepcad-windows.zip",
            "http://csd.informatik.uni-oldenburg.de/keymaera/tools/qepcad-linux.zip",
            "http://csd.informatik.uni-oldenburg.de/keymaera/tools/qepcad-mac.zip"), CSDP(
            "http://csd.informatik.uni-oldenburg.de/keymaera/tools/csdp-windows.zip",
            "http://csd.informatik.uni-oldenburg.de/keymaera/tools/csdp-linux.zip",
            "http://csd.informatik.uni-oldenburg.de/keymaera/tools/csdp-mac.zip"),
    ;

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
