/**
 * 
 */
package de.uka.ilkd.key.dl.gui.dialogwithsidepane.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import de.uka.ilkd.key.dl.gui.dialogwithsidepane.gui.options.*;
import de.uka.ilkd.key.gui.Main;
/**
 * @author zacho
 *
 */
public class InitialDialogBeans implements ActionListener {
    private JFrame InitialDialogFrame;
    private DecisionPane decisionPanel;
    private PropertiesCard propsCards;
    private String[] args;
    
    private Dimension screen = java.awt.Toolkit.getDefaultToolkit()
	    .getScreenSize();
     
    public InitialDialogBeans(String[] argsForTheMainClass) {

	args = argsForTheMainClass;
	propsCards = new PropertiesCard();
	decisionPanel = new DecisionPane();
	decisionPanel.addActionListener(this);	
	paintDialog();
    }

    public void paintDialog(){

    	InitialDialogFrame = new JFrame(" - KeYmaera Settings - ");	  	
    	InitialDialogFrame.setLayout(new BorderLayout(5,5));
    	
    	InitialDialogFrame.add(
    			new HeadingText(
    				"Select Solvers Properties and File Locations:",
    				"KeYmaera stores the corresponding  paths and properties for the each solver")
    				.getDescriptionText(),BorderLayout.NORTH);
    	
    	InitialDialogFrame.add(propsCards.getPropertiesCardPane(),BorderLayout.CENTER ); 
    	
    	JPanel decisionPane = new JPanel();
    	
    	decisionPane.setLayout(new BorderLayout());
    	decisionPane.add(propsCards.getCheckBoxEditor().getPathPane(),BorderLayout.LINE_START);
	decisionPane.add(decisionPanel.getPane(),BorderLayout.EAST);
    	InitialDialogFrame.add(decisionPane, BorderLayout.SOUTH);
    	
        //InitialDialogFrame.setPreferredSize(new Dimension(720,350));
    	InitialDialogFrame.setResizable(false);
    	InitialDialogFrame.setLocation((int) (screen.getWidth() * 2 / 8), (int) (screen.getHeight() * 1 / 30));
    	InitialDialogFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	InitialDialogFrame.pack();
    }
  
    /**
     * @return the pathPanel
     */
    public JFrame getPathPanel() {
	return InitialDialogFrame;
    }

    public void actionPerformed(ActionEvent e) {

	if (e.getSource().equals(decisionPanel.getButtonOK())) {

	    if (WriteProperties.write(propsCards)) {
		InitialDialogFrame.dispose();
		final String[] args = this.args;
		new Thread() {

		    @Override
		    public void run() {
			Main.main(args);
		    }
		}.start();
	    }
	}

	if (e.getSource().equals(decisionPanel.getButtonApply())) {
	    if (WriteProperties.write(propsCards)) {
		decisionPanel.getButtonApply().setEnabled(false);
	    }
	}

	if (e.getSource().equals(decisionPanel.getButtonExit())) {
	    final int option = JOptionPane.showConfirmDialog(
		    InitialDialogFrame,
		    "Settings will be ignored \nReally exit KeYmaera?",
		    "Warning", JOptionPane.YES_NO_OPTION);
	    if (option == JOptionPane.YES_OPTION) {
		InitialDialogFrame.dispose();
	    }
	}

    }
    
    
	
}
