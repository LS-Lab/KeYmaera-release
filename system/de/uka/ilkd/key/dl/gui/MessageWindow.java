/**
 * 
 */
package de.uka.ilkd.key.dl.gui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import de.uka.ilkd.key.gui.Main;

/**
 * @author jdq
 *
 */
public class MessageWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8818738477491044882L;
	public static final MessageWindow INSTNACE = new MessageWindow();

	private JTextArea area;
	
	private MessageWindow() {
		area = new JTextArea();
		JScrollPane comp = new JScrollPane(area);
		comp.setMinimumSize(new Dimension(200, 100));
		add(comp);
		setTitle("Messages from Background Solvers");
		setMinimumSize(new Dimension(500, 300));
		pack();
	}
	
	public void addMessage(String message) {
		if(!message.equals("")) {
			area.append(message + "\n");
			if(!Main.batchMode) {
				setVisible(true);
			}
		}
	}
	
}
