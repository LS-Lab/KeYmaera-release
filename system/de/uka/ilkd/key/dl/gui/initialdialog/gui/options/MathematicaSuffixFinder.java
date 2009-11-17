package de.uka.ilkd.key.dl.gui.initialdialog.gui.options;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.ListIterator;
import javax.swing.JOptionPane;

import de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OSInfosDefault;
import de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.Suffixes.*;
import de.uka.ilkd.key.dl.gui.initialdialog.gui.PropertyConfigurationBeans;
import de.uka.ilkd.key.dl.gui.initialdialog.gui.PropertyGroupCard;

/**
 * 
 * This Class represents an Object for Mathematica suffix completion. It is essentially used in the mathematica propertycard group 
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

    /**
     * This method sets the property group to be evaluated by the suffixFinder.
     * @param group <em> PropertyGroupCard <em>
     */
    public void setMathematicaSuffixFinder(PropertyGroupCard group) {

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

    /**
     * Set Auto-searched paths for various PropertyConfigurationBeans if possible according to current mathematica Path.
     */
    public void setPathChanges() {

	if (suffixesClassInstance.isPossibleMathematicaPath(currentMathematicaPath)) {
	    
	    mathematica = suffixesClassInstance.getMathematicaPath(currentMathematicaPath);
	    mathkernel = mathematica + File.separator + suffixesClassInstance.getMathkernelDefaultSuffix();
	    Jlink = mathematica + File.separator + suffixesClassInstance.getJLinkDefaultSuffix();
	    
	} else if (showConfirnPathSearch()) {
	    
	    	mathematica = suffixesClassInstance.getMathematicaPath(currentMathematicaPath);
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

    /**
     * Set suffixes Class according to current OS.
     */
    public void setSuffixesClass() {

	String osName = OSInfosDefault.INSTANCE.getOsName();
	if (osName.equals("linux"))
	    suffixesClass = LinuxSuffixes.class;
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
