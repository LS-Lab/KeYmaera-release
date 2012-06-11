/**
 * 
 */
package de.uka.ilkd.key.dl.options;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OSInfosDefault;

public class FilePropertyEditor extends PropertyEditorSupport implements
		PropertyEditor, ActionListener {
	private JPanel panel;
	private File file;
	private JButton button;
	private JTextField comp;

	public FilePropertyEditor() {
		file = new File("/");
		button = new JButton("Browse...");
		button.addActionListener(this);
		panel = new JPanel(new BorderLayout(5, 0));
		comp = new JTextField();
		comp.setText(file.getPath());
//		comp.addPropertyChangeListener(new PropertyChangeListener() {
//
//			/*@Override*/
//			public void propertyChange(PropertyChangeEvent evt) {
//				setValue(new File(comp.getText()));
//			}
//
//		});
		comp.addFocusListener(new FocusListener() {

			/*@Override*/
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}

			/*@Override*/
			public void focusLost(FocusEvent e) {
				setValue(new File(comp.getText()));
			}
			
		});
		panel.add(comp, BorderLayout.CENTER);
		panel.add(button, BorderLayout.EAST);
	}

	/*
	 * (non-Javadoc) @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
        switch(OSInfosDefault.INSTANCE.getOs()) {
        case OSX:
            System.setProperty("apple.awt.fileDialogForDirectories", "false");
            FileDialog d = new FileDialog(Frame.getFrames()[0], "Choose a file.", FileDialog.LOAD);
            d.setDirectory(file.getPath());
            d.setVisible(true);
            if(d.getFile() != null) {
                setValue(new File(d.getDirectory(), d.getFile()));
            }
            break;
        default:
            JFileChooser chooser = file == null ? new JFileChooser() : new JFileChooser(file.getPath());
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Choose a file");
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            int result = chooser.showDialog(null, "Select");
            if(result == JFileChooser.APPROVE_OPTION) {
                setValue(chooser.getSelectedFile());
            }
        }
		
	}

	/** * @return custom editor panel */

	public Component getCustomEditor() {
		return panel;
	}

	/** * @return the value */

	public Object getValue() {
		return file;
	}

	/** * @return true if the editor is paintable */
	public boolean isPaintable() {
		return true;
	}

	public void setValue(Object value) {
		file = (File) value;
		comp.setText(file == null ? "" : file.getAbsolutePath());
		firePropertyChange();
	}

	public boolean supportsCustomEditor() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
	    setValue(new File(text));
	}

}