package de.uka.ilkd.key.dl.gui.dialogwithsidepane.gui.options;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.ListIterator;

import de.uka.ilkd.key.dl.gui.dialogwithsidepane.defaultsettings.OSInfosDefault;
import de.uka.ilkd.key.dl.gui.dialogwithsidepane.gui.PropertyConfigurationBeans;
import de.uka.ilkd.key.dl.gui.dialogwithsidepane.gui.PropertyGroupCard;

/**
 * @author zacho
 *
 */
public class MathematicaSuffixFinder {

    String MathematicaPath;
    String currentMathematicaPath;
    String mathematicaKey;
    String mathKernelKey;
    String JLinkKey;
    HashMap<String, PropertyConfigurationBeans> MathematicaGroup;

    public MathematicaSuffixFinder() {
	mathematicaKey = "[MathematicaOptions]mathematicaPath";
	mathKernelKey = "[MathematicaOptions]mathKernel";
	JLinkKey = "com.wolfram.jlink.libdir";
	MathematicaGroup = new HashMap <String,PropertyConfigurationBeans>();

    }

    public void setMathematicaPrefixFinder(PropertyGroupCard group) {

	final PropertyGroupCard mathematicaGroup = group;
	ListIterator<PropertyConfigurationBeans> iter = mathematicaGroup.getGroup().listIterator();

	PropertyConfigurationBeans beans;
	MathematicaGroup = new HashMap <String,PropertyConfigurationBeans>();
	while (iter.hasNext()) {
	    beans = new PropertyConfigurationBeans();
	    beans = iter.next();
	    if(!beans.getPropertyIdentifier().equals(mathematicaKey))
		beans.getPathPane().setVisible(false);
		
	    MathematicaGroup.put(beans.getPropertyIdentifier(), beans);
	}

	MathematicaGroup.get(mathematicaKey).getPropertyEditor().addPropertyChangeListener( 
	        new PropertyChangeListener() {
		    public void propertyChange(PropertyChangeEvent evt) {
		        currentMathematicaPath = MathematicaGroup.get(mathematicaKey).getCurrentPropertyObject().toString();

		        MathematicaGroup.get(mathKernelKey).getPathPane().setVisible(true);
		        MathematicaGroup.get(JLinkKey).getPathPane().setVisible(true);
		        mathematicaGroup.setPropertyChanges(mathKernelKey,
		                OSInfosDefault.INSTANCE.getSuffixed(
		                        mathKernelKey, currentMathematicaPath));
		        mathematicaGroup.setPropertyChanges(JLinkKey, OSInfosDefault.INSTANCE
		                .getSuffixed(JLinkKey, currentMathematicaPath));

		    }
	        });
    }
}
