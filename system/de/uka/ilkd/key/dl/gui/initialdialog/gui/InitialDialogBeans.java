package de.uka.ilkd.key.dl.gui.initialdialog.gui;



import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import javax.swing.border.TitledBorder;

import de.uka.ilkd.key.dl.gui.initialdialog.propertyconfigurations.EPropertyConfigurations;


/**
 * @author zacho
 * 
 *         The ConfigurationMainFrame Class represents the main frame of the configuration GUI
 */
public class InitialDialogBeans implements ActionListener {

    public static final InitialDialogBeans INSTANCE = new InitialDialogBeans();

    JFrame pathFrame;
    private JButton buttonOK;
    private JButton buttonApply;

    private JButton buttonExit;
    private LinkedHashMap<String, List<PropertyConfigurationBeans>> groupMap;

    InitialDialogBeans() {
        pathFrame = new JFrame("KeYmaera Settings");
        groupMap = new LinkedHashMap<String, List<PropertyConfigurationBeans>>();

        for (EPropertyConfigurations k : EPropertyConfigurations.values()) {
            PropertyConfigurationBeans editor = new PropertyConfigurationBeans();
            editor.setPathPane(k.getLabel(), k.getEditorClass(), k
                    .getConverterClass(), k.getConfigFile(), k.getKey());
            List<PropertyConfigurationBeans> editorsInGroup = groupMap
                    .get(k.getGroup());
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
        c.insets = new Insets(5, 5, 20, 5);
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
        pathFrame.add(new HeadingText().getDescriptionText(), c);
        c.insets = new Insets(5, 5, 5, 5);
        c.gridy = 1;
        pathFrame.add(propertiesPanel, c);
        c.gridy = 2;
        c.insets = new Insets(10, 5, 20, 20);
        c.anchor = GridBagConstraints.LAST_LINE_END;
        pathFrame.add(decisionPanel(), c);
        pathFrame.setResizable(false);
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screen = tk.getScreenSize();

        pathFrame.setLocation((int) (screen.getWidth() * 3 / 8), (int) (screen   .getHeight() * 2 / 8));
        pathFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pathFrame.pack();
    }

    private JPanel getGroupPaths(List<PropertyConfigurationBeans> group, String title) {

        if (title.equals("checkBox")) // Ensures that the checkbox does not have a border.
            return group.listIterator().next().getPathPane();

        JPanel panel = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);

        TitledBorder border = new TitledBorder(title);
        border.setTitleColor(Color.gray);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(border);
        
        ListIterator<PropertyConfigurationBeans> iter = group.listIterator();
        int y = 0;
        while (iter.hasNext()) {
            c.gridy = y++;
            panel.add(iter.next().getPathPane(), c);
        }
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

    private void writePropertyChanges() {
        for (List<PropertyConfigurationBeans> property : groupMap.values()) {
            ListIterator<PropertyConfigurationBeans> iter = property.listIterator();
            while (iter.hasNext()) {
                iter.next().writeSettings(new Properties());
            }
        }
    }

    /**
     * @return the pathPanel
     */
    public JFrame getPathPanel() {
        return pathFrame;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(buttonOK)) {
            writePropertyChanges();
            pathFrame.dispose();
        }
        if (e.getSource().equals(buttonApply))
            writePropertyChanges();
        if (e.getSource().equals(buttonExit)) {
            final int option = JOptionPane
                    .showConfirmDialog(
                            pathFrame,
                            "Settings will be ignored \nReally exit KeyMaera path setter?",
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
        checkboxState = (Boolean) PropertyConfigurationBeans.INSTANCE.getValueOfKey(EPropertyConfigurations.CHECKBOX_PROPERTY.getConfigFile(),
                                                                                EPropertyConfigurations.CHECKBOX_PROPERTY.getKey(), 
                                                                                EPropertyConfigurations.CHECKBOX_PROPERTY.getConverterClass());
        return checkboxState;
    }

}
