/**
 * 
 */
package de.uka.ilkd.key.dl.gui.initialdialog.gui.options;

import java.awt.GridBagConstraints;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import de.uka.ilkd.key.dl.gui.initialdialog.gui.PropertyConfigurationBeans;

/**
 * This Class implements an object of file existence Verification for the a list PropertyConfigurationBeans objects.
 * @author zacho
 *
 */
public class FileExistenceVerification {
    
    FileExistenceVerification(){
	
    }
    
    /**
     * This method verifies if the directories or Files in each of the given PropertyConfigurationBeans objects in the list
     * exists. It returns JOptionPane.YES_OPTION if all exist else shows a confirm dialog with a warning the returns the choosen option.
     * @param group <em> List &lt;PropertyConfigurationBeans&gt; </em> Input list.
     * @param parent <em> Component </em> Parent of the PropertyConfigurationBeans objects.
     * @return an int indicating the option selected by the user.
     */
    public static int verifyDirectories(List<PropertyConfigurationBeans> group, JComponent parent) {

	HashMap<String, File> directoriesAndFilesMap = new HashMap<String, File>();
	ListIterator<PropertyConfigurationBeans> iter = group.listIterator();
	while (iter.hasNext()) {
	    if (iter.next().isPropertyObjectAFile()) {
		directoriesAndFilesMap.put(iter.previous().getPropsName(),
		        (File) iter.next().getCurrentPropertyObject());
	    }

	}
	JPanel messagePane = new JPanel();
	JTextPane message = new JTextPane();
	messagePane.setLayout(new java.awt.GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	c.anchor = GridBagConstraints.FIRST_LINE_START;
	c.fill = GridBagConstraints.NONE;
	c.gridy = 0;
	boolean ALLEXIST = true;
	messagePane
	        .add(
	                new JLabel(
	                        "<html>The specified Directories Or Files do not exist : <br><br></html>"),
	                c);
	message.setEditable(false);
	message.setBorder(javax.swing.BorderFactory.createEtchedBorder());

	for (String name : directoriesAndFilesMap.keySet()) {
	    if (!directoriesAndFilesMap.get(name).exists()) {
		message.setText(message.getText() + name + "  [ "
		        + directoriesAndFilesMap.get(name) + "]\n");
		ALLEXIST = false;
	    }
	}
	c.gridy = 1;
	messagePane.add(message, c);
	c.gridy = 2;
	messagePane.add(new JLabel("<html><br>"
	        + "This may cause an error in KeYmaera <br>"
	        + "<br>Do  you want to continue ?</html>"), c);

	if (ALLEXIST)
	    return JOptionPane.YES_OPTION;
	else
	    return JOptionPane.showConfirmDialog(parent, messagePane,
		    "Warning", JOptionPane.YES_NO_OPTION);

    }


    

}
