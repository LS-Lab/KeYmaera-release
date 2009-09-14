/**
 * 
 */
package de.uka.ilkd.key.dl.gui.dialogwithsidepane.defaultsettings.Suffixes;

import java.util.List;
import java.util.Map;

/**
 * @author zacho
 *
 */
public enum MathematicaVersions {
    
    Ver1_0("1.0"),Ver1_1("1.1"),Ver1_2("1.2"),
    Ver2_0("2.0"),Ver2_1("2.1"),Ver2_2("2.2"),
    Ver3_0("3.0"),Ver3_1("3.1"),Ver3_2("3.2"),
    Ver4_0("4.0"),Ver4_1("4.1"),Ver4_2("4.2"),
    Ver5_0("5.0"),Ver5_1("5.1"),Ver5_2("5.2"),
    Ver6_0("6.0"),Ver6_0_1("6.0.1"),Ver6_0_2("6.0.2"),Ver6_0_3("6.0.3"),
    Ver7_0("7.0"),Ver7_0_1("7.0.1"),Ver7_0_2("7.0.2"),Ver7_0_3("7.0.3"),
    Ver8_0("8.0"),Ver8_0_1("8.0.1"),Ver8_0_2("8.0.2"),Ver8_0_3("8.0.3");
    
    private String versionEndString;
    
    MathematicaVersions(String versionEndString){
	this.versionEndString = versionEndString;
    }

    /**
     * @return the versionEndString
     */
    public String getVersionEndString(){
        return versionEndString;
    }


}
