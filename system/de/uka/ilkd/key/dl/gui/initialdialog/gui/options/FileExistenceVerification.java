/**
 * 
 */
package de.uka.ilkd.key.dl.gui.initialdialog.gui.options;

import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import de.uka.ilkd.key.dl.gui.initialdialog.gui.PropertyConfigurationBeans;
import de.uka.ilkd.key.dl.gui.initialdialog.gui.ToolInstaller;

/**
 * This Class implements an object of file existence Verification for the a list
 * PropertyConfigurationBeans objects.
 * 
 * @author zacho
 * 
 */
public class FileExistenceVerification {

    FileExistenceVerification() {

    }

    /**
     * This method verifies if the directories or Files in each of the given
     * PropertyConfigurationBeans objects in the list exists. It returns
     * JOptionPane.YES_OPTION if all exist else shows a confirm dialog with a
     * warning the returns the choosen option.
     * 
     * @param group
     *            <em> List &lt;PropertyConfigurationBeans&gt; </em> Input list.
     * @param parent
     *            <em> Component </em> Parent of the PropertyConfigurationBeans
     *            objects.
     * @return an int indicating the option selected by the user.
     */
    public static int verifyDirectories(List<PropertyConfigurationBeans> group,
            final JComponent parent) {

        final JPanel messagePane = new JPanel();
        JPanel buttonPanel = new JPanel();
        JTextPane message = new JTextPane();
        messagePane.setLayout(new java.awt.GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.NONE;
        c.gridy = 0;
        boolean ALLEXIST = true;
        messagePane
                .add(new JLabel(
                        "<html>The specified directories or files do not exist : <br><br></html>"),
                        c);
        message.setEditable(false);
        message.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        HashSet<String> toolButtons = new HashSet<String>(); 
        for (PropertyConfigurationBeans pcb : group) {
            if (pcb.isPropertyObjectAFile()) {
                File currentPropertyObject = (File) pcb
                        .getCurrentPropertyObject();
                if (!currentPropertyObject.exists()) {
                    message.setText(message.getText() + pcb.getPropsName()
                            + "  [ " + currentPropertyObject + "]\n");
                    ALLEXIST = false;
                    final ToolInstaller installer = pcb.getInstaller();
                    if(installer != null && !toolButtons.contains(installer.getToolName())) {
                        toolButtons.add(installer.getToolName());
                        // check that for each tool there is only one button
                        JButton jButton = new JButton(installer.getToolName());
                        jButton.addActionListener(new ActionListener() {
                            
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                Window w = SwingUtilities.getWindowAncestor(messagePane);
                                installer.install(parent, w);
                            }
                        });
                        buttonPanel.add(jButton);
                    }
                }
            }
        }
        c.gridy = 1;
        messagePane.add(message, c);
        c.gridy = 2;
        buttonPanel.add(new JLabel("Download: "), 0);
        messagePane.add(buttonPanel, c);
        c.gridy = 3;
        messagePane.add(new JLabel("<html><br>"
                + "This may cause an error in KeYmaera <br>"
                + "<br>Do  you want to continue?</html>"), c);

        if (ALLEXIST)
            return JOptionPane.YES_OPTION;
        else
            return JOptionPane.showConfirmDialog(parent, messagePane,
                    "Configuration Warning", JOptionPane.YES_NO_OPTION);

    }

}
