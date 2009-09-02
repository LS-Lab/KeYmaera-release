package de.uka.ilkd.key.dl.gui.initialdialog.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;

import de.uka.ilkd.key.dl.gui.initialdialog.converters.FileStringConverter;
import de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OSInfosDefault;
import de.uka.ilkd.key.dl.gui.initialdialog.propertyconfigurations.EConfigurationFiles;
import de.uka.ilkd.key.dl.gui.initialdialog.propertyconfigurations.EPropertyConfigurations;
import de.uka.ilkd.key.gui.Main;

/**
 * @author zacho
 * 
 *         The ConfigurationMainFrame Class represents the main frame of the
 *         configuration GUI
 */
public class InitialDialogBeans implements ActionListener {

    private JFrame pathFrame;

    private JButton buttonOK;

    private JButton buttonApply;

    private JPanel MathematicaHiddenPanel;

    PropertyConfigurationBeans MathematicaEditor;

    String currentMathematicaPath;

    private JButton buttonExit;

    private LinkedHashMap<String, List<PropertyConfigurationBeans>> groupMap;

    private String[] args;

    public InitialDialogBeans(String[] argsForTheMainClass) {
	args = argsForTheMainClass;
	pathFrame = new JFrame(" - KeYmaera Settings -");
	groupMap = new LinkedHashMap<String, List<PropertyConfigurationBeans>>();
	for (EPropertyConfigurations k : EPropertyConfigurations.values()) {
	    PropertyConfigurationBeans editor = new PropertyConfigurationBeans();
	    editor.setPathPane(k.getLabel(), k.getEditorClass(), k
		    .getConverterClass(), k.getConfigFile(), k.getKey());
	    List<PropertyConfigurationBeans> editorsInGroup = groupMap.get(k
		    .getGroup());
	    if (editorsInGroup == null) {
		editorsInGroup = new LinkedList<PropertyConfigurationBeans>();
		groupMap.put(k.getGroup(), editorsInGroup);
	    }
	    editorsInGroup.add(editor);
	}

	// loop over groupMap and actually create the groups

	GridBagConstraints c = new GridBagConstraints();
	c.anchor = GridBagConstraints.LINE_START;
	c.fill = GridBagConstraints.NONE;
	c.insets = new Insets(5, 5, 5, 5);
	JPanel propertiesPanel = new JPanel();
	propertiesPanel.setLayout(new GridBagLayout());
	int y = 0;

	for (String groupIdentifier : groupMap.keySet()) {
	    c.gridy = y++;
	    propertiesPanel.add(getGroupPaths(groupMap.get(groupIdentifier),
		    groupIdentifier), c);

	}

	pathFrame.setLayout(new GridBagLayout());
	c.insets = new Insets(10, 0, 5, 0);
	c.gridy = 0;
	pathFrame
		.add(
			new HeadingText(
				"Select Solvers Properties and File Locations:",
				"KeYmaera stores the corresponding  paths and properties for the each solver")
				.getDescriptionText(), c);
	c.insets = new Insets(5, 5, 5, 5);
	c.gridy = 1;
	pathFrame.add(propertiesPanel, c);
	c.gridy = 2;
	c.insets = new Insets(10, 5, 20, 20);
	c.anchor = GridBagConstraints.LAST_LINE_END;
	pathFrame.add(decisionPanel(), c);
	pathFrame.setResizable(false);
	Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

	pathFrame.setLocation((int) (screen.getWidth() * 3 / 8), (int) (screen
		.getHeight() * 2 / 8));
	pathFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	pathFrame.pack();
    }

    private JPanel getGroupPaths(List<PropertyConfigurationBeans> group,
	    String title) {

	if (title.equals("checkBox")) // Ensures that the checkbox does not have
	    // a border.
	    return group.listIterator().next().getPathPane();

	JPanel panel = new JPanel();
	GridBagConstraints c = new GridBagConstraints();
	c.insets = new Insets(5, 5, 5, 5);
	panel.setLayout(new GridBagLayout());
	int y = 1;
	ListIterator<PropertyConfigurationBeans> iter = group.listIterator();

	while (iter.hasNext()) {
	    c.gridy = y++;
	    panel.add(iter.next().getPathPane(), c);
	}

	if (title.equals("Mathematica Properties")) {

	    MathematicaHiddenPanel = panel;
	    panel = new JPanel();
	    panel.setLayout(new GridBagLayout());
	    MathematicaEditor = new PropertyConfigurationBeans();
	    MathematicaEditor.setPathPane("Mathematica Path :",
		    de.uka.ilkd.key.dl.options.DirectoryPropertyEditor.class,
		    FileStringConverter.class,
		    EConfigurationFiles.KEY_PROPERTY_FILE,
		    "[MathematicaOptions]mathematicaPath");
	    currentMathematicaPath = MathematicaEditor
		    .getCurrentPropertyObject().toString();
	    final String defaultMathematicaPath = currentMathematicaPath;
	    final String mathKernelKey = "[MathematicaOptions]mathKernel";
	    final String JLinkKey = "com.wolfram.jlink.libdir";
	    MathematicaEditor.getPropertyEditor().addPropertyChangeListener(
		    new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {

			    currentMathematicaPath = MathematicaEditor
				    .getCurrentPropertyObject().toString();
			    if (!MathematicaEditor.getCurrentPropertyObject()
				    .toString().equals(defaultMathematicaPath)) {
				setPropertyChanges(mathKernelKey,
					OSInfosDefault.INSTANCE.getSuffixed(
						mathKernelKey,
						currentMathematicaPath));

				setPropertyChanges(JLinkKey,
					OSInfosDefault.INSTANCE.getSuffixed(
						JLinkKey,
						currentMathematicaPath));
				MathematicaHiddenPanel.setVisible(true);
				pathFrame.pack();
			    }
			}
		    });
	    c.gridy = 1;
	    panel.add(MathematicaEditor.getPathPane(), c);
	    MathematicaHiddenPanel.setVisible(false);
	    c.gridy = 2;
	    panel.add(MathematicaHiddenPanel, c);
	}
	TitledBorder border = new TitledBorder(title);
	border.setTitleColor(java.awt.Color.gray);
	panel.setBorder(border);
	return panel;
    }

    public JPanel decisionPanel() {

	JPanel panel = new JPanel();
	buttonOK = new JButton("    Ok   ");
	buttonApply = new JButton(" Apply ");
	buttonExit = new JButton(" Cancel ");

	buttonOK.addActionListener(this);
	buttonApply.addActionListener(this);
	buttonExit.addActionListener(this);

	panel.setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	c.insets = new Insets(0, 3, 3, 3);

	panel.add(buttonOK, c);
	panel.add(buttonApply, c);
	panel.add(buttonExit, c);
	return panel;
    }

    private void setPropertyChanges(String PropertyKey, String value) {
	for (List<PropertyConfigurationBeans> group : groupMap.values()) {
	    ListIterator<PropertyConfigurationBeans> iter = group
		    .listIterator();
	    while (iter.hasNext()) {
		if (iter.next().getPropertyIdentifier().equals(PropertyKey)) {
		    iter.previous().setPropertyPathObject(value);
		    iter.next();
		}
	    }
	}

    }

    private void writePropertyChanges() {
	for (List<PropertyConfigurationBeans> property : groupMap.values()) {
	    ListIterator<PropertyConfigurationBeans> iter = property
		    .listIterator();
	    while (iter.hasNext()) {
		iter.next().writeSettings(new Properties());
	    }
	}
	MathematicaEditor.writeSettings(new Properties());
    }

    private int verifyDirectories() {

	HashMap<String, File> directoriesAndFilesMap = new HashMap<String, File>();

	for (List<PropertyConfigurationBeans> property : groupMap.values()) {

	    ListIterator<PropertyConfigurationBeans> iter = property
		    .listIterator();
	    while (iter.hasNext()) {
		if (iter.next().isPropertyObjectAFile()) {
		    directoriesAndFilesMap.put(iter.previous().getPropsName(),
			    (File) iter.next().getCurrentPropertyObject());
		}

	    }
	}

	JPanel messagePane = new JPanel();
	JTextPane message = new JTextPane();
	messagePane.setLayout(new java.awt.GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	c.anchor = GridBagConstraints.FIRST_LINE_START;
	c.fill = GridBagConstraints.NONE;
	c.gridy = 0;
	boolean AllExit = true; 
	messagePane
		.add(new JLabel("<html>The specified Directories Or Files do not exist : <br><br></html>"),
			c);
	message.setEditable(false);
	message.setBorder(javax.swing.BorderFactory.createEtchedBorder());

	for (String name : directoriesAndFilesMap.keySet()) {

	    if (!directoriesAndFilesMap.get(name).exists()) {
		message.setText(message.getText() + name + "  [ "
			+ directoriesAndFilesMap.get(name) + "]\n");
		AllExit = false;
	    }
	}
	c.gridy = 1;
	messagePane.add(message, c);
	c.gridy = 2;
	messagePane.add(new JLabel(
		"<html><br>Do  you want to continue ?</html>"), c);

	if (AllExit)
	    return JOptionPane.YES_OPTION;
	else
	    return JOptionPane.showConfirmDialog(pathFrame, messagePane,
		    "Warning", JOptionPane.YES_NO_OPTION);

    }

    /**
     * @return the pathPanel
     */
    public JFrame getPathPanel() {
	return pathFrame;
    }

    public void actionPerformed(ActionEvent e) {

	if (e.getSource().equals(buttonOK)) {
	    if (verifyDirectories() == JOptionPane.YES_OPTION) {
		writePropertyChanges();
		pathFrame.dispose();
		final String[] args = this.args;
		new Thread() {

		    @Override
		    public void run() {
			Main.main(args);
		    }
		}.start();
	    }
	}
	if (e.getSource().equals(buttonApply)) {
	    if (verifyDirectories() == JOptionPane.YES_OPTION)
		writePropertyChanges();

	}
	if (e.getSource().equals(buttonExit)) {
	    final int option = JOptionPane.showConfirmDialog(pathFrame,
		    "Settings will be ignored \nReally exit KeYmaera?",
		    "Warning", JOptionPane.YES_NO_OPTION);
	    if (option == JOptionPane.YES_OPTION) {
		pathFrame.dispose();
	    }
	}
    }

    /**
     * @return the checkboxState
     */
    public Boolean getCheckboxState() {
	Boolean checkboxState;
	checkboxState = (Boolean) PropertyConfigurationBeans.INSTANCE
		.getValueOfKey(EPropertyConfigurations.CHECKBOX_PROPERTY
			.getConfigFile(),
			EPropertyConfigurations.CHECKBOX_PROPERTY.getKey(),
			EPropertyConfigurations.CHECKBOX_PROPERTY
				.getConverterClass());
	return checkboxState;
    }

}