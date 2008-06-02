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
		JOptionPane
				.showMessageDialog(
						null,
						"This is a crippled version of the KeYmaera prover. For the full version of KeYmaera (including quantifier elimination support),\n Mathematica is necessary and for that reason you have to install KeYmaera to the harddrive\n instead of using webstart.\n Sorry for the inconvinience.\n  Instead groebner basis are calculated.\n This does only work for equations.");
		InputStream resourceAsStream = MainFrame.class
				.getResourceAsStream("/examples/hybrid/groebner_basis/accel-simple.key");
		try {
			File tempFile = File.createTempFile("keymaera", ".key");
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

}
