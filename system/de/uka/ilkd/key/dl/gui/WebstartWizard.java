/**
 * 
 */
package de.uka.ilkd.key.dl.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.gui.configuration.PathConfig;

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
		File file = new File(PathConfig.KEY_CONFIG_DIR + File.separator
				+ "webstart-math.props");
		if (file.exists()) {
			try {
				System.getProperties().load(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (checkForJLinkNativesFails()) {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int state = chooser.showDialog(null,
					"Select JLink Library Location");
			if (state == JFileChooser.APPROVE_OPTION) {
				System.setProperty("com.wolfram.jlink.libdir", chooser
						.getSelectedFile().getAbsolutePath());
				Properties properties = new Properties();
				properties.setProperty("com.wolfram.jlink.libdir", chooser
						.getSelectedFile().getAbsolutePath());
				try {
					if (!file.exists()) {
						new File(PathConfig.KEY_CONFIG_DIR + File.separator)
								.mkdirs();
						file.createNewFile();
					}

					properties.store(new FileOutputStream(file), null);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		// String[] newArgs = new String[args.length + 1];
		// System.arraycopy(args, 0, newArgs, 0, args.length);
		// newArgs[args.length] = "dL";
		// Main.main(newArgs);
		InputStream resourceAsStream = MainFrame.class
				.getResourceAsStream("/examples/hybrid/groebner_basis/magnetic_field.key");
		if (resourceAsStream == null) {
			String[] newArgs = new String[args.length + 1];
			System.arraycopy(args, 0, newArgs, 0, args.length);
			newArgs[args.length] = "dL";
			Main.main(newArgs);
		} else
		try {
			File tempFile = File.createTempFile("magnetic_field", ".key");
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
	 * @return TODO documentation since 18.03.2009
	 */
	private static boolean checkForJLinkNativesFails() {
		if (System.getProperty("com.wolfram.jlink.libdir") == null) {
			return true;
		}
		// TODO: check if the directory really contains the native files
		return false;
	}

	/**
	 * 
	 */
	public static void showRestrictionWarning(JFrame parent) {
		JOptionPane
				.showMessageDialog(
						parent,
						"In the following the KeYmaera startup will present you some dialogs for the configuration of the different backends.\n"
								+ "First, you can enter the location of the J/Link natives.\n"
								+ "In the following dialogs you can configure the locations of \n"
								+ "1. the MathKernel (Mathematica),\n"
								+ "2. QepCad and/or\n"
								+ "3. Redlog.\n\n"
								+ "Not all of these tools are necessary. You can even start using KeYmaera for simple examples without any of those solvers.\n"
								+ "We recommend configuring at least one of those solvers for improved verification power.\n"
								+ "Note that you can alter these options in the \"Hybrid Strategy\" options later on too.\n\n"
								+ "Also note that the support for CSDP is currently not included in this Webstart version.\n"
								+ "We are still working on a method for deploying the support for semi-definite programming tools.",
						"KeYmaera Webstart Notice",
						JOptionPane.INFORMATION_MESSAGE);
	}

}
