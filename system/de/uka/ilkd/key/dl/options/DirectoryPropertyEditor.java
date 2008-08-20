/**
 * 
 */
package de.uka.ilkd.key.dl.options;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

/**
 * @author jdq
 *
 */
public class DirectoryPropertyEditor extends FilePropertyEditor {

	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.dl.options.FilePropertyEditor#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser(((File) getValue()).getPath());
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.showDialog(null, "Select");
		setValue(chooser.getSelectedFile());
	}
}
