/*******************************************************************************
 * Copyright (c) 2010 Zacharias Mokom.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Zacharias Mokom - initial API and implementation
 ******************************************************************************/
package de.uka.ilkd.key.dl.gui.initialdialog.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import de.uka.ilkd.key.dl.gui.initialdialog.gui.options.*;
import de.uka.ilkd.key.gui.Main;
/**
 * @author zacho
  * 
 *         The ConfigurationMainFrame Class represents the main frame of the
 *         initialdialog GUI
 */
public class InitialDialogBeans implements ActionListener {
    private JFrame InitialDialogFrame;
    private DecisionPane decisionPanel;
    private PropertiesCard propsCards;
    private String[] args;
    private boolean reinit;
    
    
    private Dimension screen = java.awt.Toolkit.getDefaultToolkit()
	    .getScreenSize();
     
    public InitialDialogBeans(String[] argsForTheMainClass, boolean reinit) {

	args = argsForTheMainClass;
	propsCards = new PropertiesCard();
	decisionPanel = new DecisionPane();
	decisionPanel.addActionListener(this);
	this.reinit = reinit;
	paintDialog();
    }
    public InitialDialogBeans(String[] argsForTheMainClass) {
	this(argsForTheMainClass, false);
    }
   /**
    * This method paints the initial dialog frame.
    */
    public void paintDialog(){

    	InitialDialogFrame = new JFrame(" - KeYmaera Settings - ");	  	
    	InitialDialogFrame.setLayout(new BorderLayout(5,5));
    	
    	InitialDialogFrame.add(
    			new HeadingText(
    				"Select Solvers Properties and File Locations:",
    				"KeYmaera stores the corresponding paths and properties for the each solver")
    				.getDescriptionText(),BorderLayout.NORTH);
    	
    	InitialDialogFrame.add(propsCards.getPropertiesCardPane(),BorderLayout.CENTER ); 
    	
    	JPanel decisionPane = new JPanel();
    	
    	decisionPane.setLayout(new BorderLayout());
    	decisionPane.add(propsCards.getCheckBoxEditor().getPathPane(),BorderLayout.LINE_START);
	decisionPane.add(decisionPanel.getPane(),BorderLayout.EAST);
    	InitialDialogFrame.add(decisionPane, BorderLayout.SOUTH);
    	InitialDialogFrame.getRootPane().setDefaultButton(decisionPanel.getButtonOK());
    	InitialDialogFrame.getRootPane().registerKeyboardAction(new ActionListener() {
    	    public void actionPerformed(ActionEvent e) {
    	        doExit();
    	    }
    	}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    	
        //InitialDialogFrame.setPreferredSize(new Dimension(720,350));
    	InitialDialogFrame.setResizable(false);
    	InitialDialogFrame.setLocation((int) (screen.getWidth() * 2 / 8), (int) (screen.getHeight() * 1 / 30));
    	InitialDialogFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	InitialDialogFrame.pack();
    }
  
    /**
     * @return the InitialDialogFrame
     */
    public JFrame getInitialDialogFrame() {
	return InitialDialogFrame;
    }

    public void actionPerformed(ActionEvent e) {

	if (e.getSource().equals(decisionPanel.getButtonOK())) {

	    if (WriteProperties.write(propsCards)) {
		InitialDialogFrame.dispose();
		final String[] args = this.args;
		if (reinit) {
		        int result = JOptionPane.showConfirmDialog(null, "Note that you have to restart KeYmaera now for options to take effect.\n"
		        	+ "In particular, KeYmaera needs to be restarted after changing the Mathematica JLink path.\n\n"
		        	+ "Otherwise option settings may change in KeYmaera.\n"
		        	+ "Do you want to exit KeYmaera without saving?", "Restart KeYmaera", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		        if (result == JOptionPane.YES_OPTION) {
		            System.exit(5);
		        }
		} else {
		new Thread() {

		    @Override
		    public void run() {
			Main.main(args);
		    }
		}.start();
		}
	    }
	}

	if (e.getSource().equals(decisionPanel.getButtonApply())) {
	    if (WriteProperties.write(propsCards)) {
		decisionPanel.getButtonApply().setEnabled(false);
	    }
	}

	if (e.getSource().equals(decisionPanel.getButtonExit())) {
	    doExit();
	}

    }
    private void doExit() {
	final int option = JOptionPane.showConfirmDialog(
	    InitialDialogFrame,
	    "Settings will be ignored.\nReally exit KeYmaera tool settings dialog?",
	    "Warning", JOptionPane.YES_NO_OPTION);
	if (option == JOptionPane.YES_OPTION) {
	InitialDialogFrame.dispose();
	}
    }
    
    /**
     * Gets the current state of the checkboxState 
     * @return <em> boolean </em> 
     */
    public boolean getCheckboxState(){
	
	return PropertiesCard.getCheckboxState();
    }
    
    
	
}
