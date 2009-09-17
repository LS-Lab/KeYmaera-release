package de.uka.ilkd.key.dl.gui.dialogwithsidepane.gui.options;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.ListIterator;
import javax.swing.JOptionPane;

import de.uka.ilkd.key.dl.gui.dialogwithsidepane.defaultsettings.OSInfosDefault;
import de.uka.ilkd.key.dl.gui.dialogwithsidepane.defaultsettings.Suffixes.*;
import de.uka.ilkd.key.dl.gui.dialogwithsidepane.gui.PropertyConfigurationBeans;
import de.uka.ilkd.key.dl.gui.dialogwithsidepane.gui.PropertyGroupCard;

/**
 * @author zacho
 * 
 */
public class MathematicaSuffixFinder  implements PropertyChangeListener {

    PropertyGroupCard mathematicaGroup;
    String MathematicaPath;
    String currentMathematicaPath;
    String mathematicaKey;
    String mathKernelKey;
    String JLinkKey;
    
    String mathkernel;
    String Jlink;
    String mathematica;
    private ISuffixes suffixesClassInstance;

    private Class<? extends ISuffixes> suffixesClass;

    HashMap<String, PropertyConfigurationBeans> MathematicaGroup;

    public MathematicaSuffixFinder(){
	mathematicaKey = "[MathematicaOptions]mathematicaPath";
	mathKernelKey = "[MathematicaOptions]mathKernel";
	JLinkKey = "com.wolfram.jlink.libdir";
	MathematicaGroup = new HashMap<String, PropertyConfigurationBeans>();
	setSuffixesClass();

    }

    public void setMathematicaPrefixFinder(PropertyGroupCard group) {

	mathematicaGroup = group;
	ListIterator<PropertyConfigurationBeans> iter = mathematicaGroup
	        .getGroup().listIterator();

	PropertyConfigurationBeans beans;
	MathematicaGroup = new HashMap<String, PropertyConfigurationBeans>();
	while (iter.hasNext()) {
	    beans = new PropertyConfigurationBeans();
	    beans = iter.next();
	    if (!beans.getPropertyIdentifier().equals(mathematicaKey))
		beans.getPathPane().setVisible(false);

	    MathematicaGroup.put(beans.getPropertyIdentifier(), beans);
	}
	
	MathematicaGroup.get(mathematicaKey).getPropertyEditor().addPropertyChangeListener(this);
	
	
    }

    public void setPathChanges() {

	if (suffixesClassInstance.isPossibleMathematicaPath(currentMathematicaPath)) {
	    mathematica = currentMathematicaPath;
	    mathkernel = currentMathematicaPath + File.separator + suffixesClassInstance.getMathkernelDefaultSuffix();
	    Jlink = currentMathematicaPath + File.separator + suffixesClassInstance.getJLinkDefaultSuffix();
	} else if (showConfirnPathSearch()) {
	    mathematica = suffixesClassInstance.getMathematicaPathPrefix(currentMathematicaPath);
	    if((mathematica=="")||(mathematica == null)){
		mathkernel = currentMathematicaPath + File.separator + suffixesClassInstance.getMathkernelDefaultSuffix();
		Jlink = currentMathematicaPath + File.separator + suffixesClassInstance.getJLinkDefaultSuffix();
	    }
	    else{
		mathkernel = mathematica + File.separator + suffixesClassInstance.getMathkernelDefaultSuffix();
		Jlink = mathematica + File.separator + suffixesClassInstance.getJLinkDefaultSuffix();
	    }
	} else {
	    mathematica = currentMathematicaPath;
	    mathkernel = currentMathematicaPath + File.separator+ suffixesClassInstance.getMathkernelDefaultSuffix();
	    Jlink = currentMathematicaPath + File.separator + suffixesClassInstance.getJLinkDefaultSuffix();
	}
    }

    public void setSuffixesClass() {

	String osName = OSInfosDefault.INSTANCE.getOsName();
	if (osName.equals("linux"))
	    suffixesClass = WindowsSuffixes.class;
	if (osName.equals("mac"))
	    suffixesClass = MacSuffixes.class;
	if (osName.equals("Windows"))
	    suffixesClass = WindowsSuffixes.class;

	try {
	    suffixesClassInstance = suffixesClass.newInstance();

	} catch (InstantiationException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IllegalAccessException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }
    public Boolean showConfirnPathSearch(){
		

	   int flag = JOptionPane.showConfirmDialog(null,"<html><br>The specified Directory was not recognised by KeYmaera<br>"+
		    					" as Possible Mathematica path. <br>"+
		    					"<br>Do you want KeYmaera to ignore the selected Mathematica path<br>"+
		    					"and independently search for available Mathkernel and Jlink Paths?<br></html>",
		    "Warning", JOptionPane.YES_NO_OPTION);
	    
	    if(flag == JOptionPane.YES_OPTION)
		return true;
	    else
		return false;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
	// TODO Auto-generated method stub
	
        currentMathematicaPath = MathematicaGroup.get(mathematicaKey).getCurrentPropertyObject().toString();		        
        MathematicaGroup.get(mathKernelKey).getPathPane().setVisible(true);
        MathematicaGroup.get(JLinkKey).getPathPane().setVisible(true);
        setPathChanges();
        mathematicaGroup.setPropertyChanges(mathKernelKey, mathkernel);
        mathematicaGroup.setPropertyChanges(JLinkKey,Jlink);

    }


 
}
