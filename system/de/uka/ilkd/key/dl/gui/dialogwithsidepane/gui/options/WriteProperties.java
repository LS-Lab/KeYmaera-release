/**
 * 
 */
package de.uka.ilkd.key.dl.gui.dialogwithsidepane.gui.options;

import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import de.uka.ilkd.key.dl.gui.dialogwithsidepane.gui.PropertiesCard;
import de.uka.ilkd.key.dl.gui.dialogwithsidepane.gui.PropertyConfigurationBeans;
import de.uka.ilkd.key.dl.gui.dialogwithsidepane.gui.PropertyGroupCard;

/**
 * @author zacho
 * 
 */
public class WriteProperties {

    static List<PropertyConfigurationBeans> list;

    WriteProperties() {

    }

    public static void write(List<PropertyConfigurationBeans> group) {
	ListIterator<PropertyConfigurationBeans> iter = group.listIterator();
	while (iter.hasNext())
	    iter.next().writeSettings(new Properties());
    }

    public static Boolean  write(PropertiesCard cardMap) {

	for (PropertyGroupCard card : cardMap.getGroupCardMap().values()) {
	    if(list == null)
		list  = card.getGroup();
	    else
		list.addAll(card.getGroup());
	}
	if (FileExistenceVerification.verifyDirectories(list, new JPanel()) == JOptionPane.YES_OPTION) {
	    for (PropertyGroupCard card : cardMap.getGroupCardMap().values()) {
		card.writePropertyChanges();
	    }
	    cardMap.getCheckBoxEditor().writeSettings(new Properties());
	    return true;
	}
	else 
	    return false;

    }
}
