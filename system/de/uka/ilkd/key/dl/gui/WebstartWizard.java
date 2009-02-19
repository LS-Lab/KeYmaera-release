/**
 * 
 */
package de.uka.ilkd.key.dl.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import de.uka.ilkd.key.gui.Main;

/**
 * @author jdq
 * 
 */
public class WebstartWizard {

	public static class MainFrame extends JFrame {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public MainFrame() {
			add(new JLabel("Supertext"));
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// JFrame mainFrame = new MainFrame();
		// mainFrame.setVisible(true);
		showRestrictionWarning(null);
		InputStream resourceAsStream = MainFrame.class
				.getResourceAsStream("/examples/hybrid/groebner_basis/accel-simple.key");
		try {
			File tempFile = File.createTempFile("keymaera", ".key");
			tempFile.deleteOnExit();
			System.out.println(tempFile.getCanonicalPath());
			FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
			int i;
			while ((i = resourceAsStream.read()) != -1) {
				fileOutputStream.write((char) i);
			}
			resourceAsStream.close();
			fileOutputStream.close();
			String[] newArgs = new String[args.length + 2];
			System.arraycopy(args, 0, newArgs, 0, args.length);
			newArgs[args.length] = "dL";
			newArgs[args.length + 1] = tempFile.getCanonicalPath();
			Main.main(newArgs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 
	 */
	public static void showRestrictionWarning(JFrame parent) {
		JOptionPane
				.showMessageDialog(
						parent,
			"Please note that this is only a RESTRICTED WEBSTARTABLE VERSION of the KeYmaera prover for hybrid systems!\n\nThe full version of KeYmaera needs to be installed on your computer to work properly.\nYou can download the full version of KeYmaera for installation on your computer from:\n\n    http://symbolaris.com/info/KeYmaera-download.html\n\nThe full installation has much more features, including the handling of hybrid systems or specifications with inequalities.\nBut you need to have Mathematica installed.\n\nUnfortunately, Mathematica does not allow webstartable versions,\neven if you have already installed Mathematica on your machine.\nPlease install KeYmaera on your computer for experiencing its full functionality.\nWe apologize for the inconvenience.",
                   "Webstart Restriction Notification",
                   JOptionPane.INFORMATION_MESSAGE);
	}

}
