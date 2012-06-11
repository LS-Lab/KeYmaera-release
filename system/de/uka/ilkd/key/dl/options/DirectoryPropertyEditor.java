/**
 * 
 */
package de.uka.ilkd.key.dl.options;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

import de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OSInfosDefault;

/**
 * @author jdq
 * 
 */
public class DirectoryPropertyEditor extends FilePropertyEditor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.options.FilePropertyEditor#actionPerformed(java.awt
	 * .event.ActionEvent)
	 */
	/*@Override*/
	public void actionPerformed(ActionEvent e) {
        switch(OSInfosDefault.INSTANCE.getOs()) {
        case OSX:
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
            FileDialog d = new FileDialog(Frame.getFrames()[0], "Choose a directory.", FileDialog.LOAD);
            d.setDirectory(((File) getValue()).getPath());
            d.setVisible(true);
            System.setProperty("apple.awt.fileDialogForDirectories", "false");
            if(d.getFile() != null) {
                setValue(new File(d.getDirectory(), d.getFile()));
            }
            break;
        default:
            final JFileChooser chooser = new JFileChooser(((File) getValue()).getPath());
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Choose a directory");
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            int result = chooser.showDialog(null, "Select");
            if(result == JFileChooser.APPROVE_OPTION) {
                setValue(chooser.getSelectedFile());
            }
        }
	}
}
