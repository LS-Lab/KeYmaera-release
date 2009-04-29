package de.uka.ilkd.key.dl.arithmetics.impl.hollight;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import de.uka.ilkd.key.dl.arithmetics.exceptions.IncompleteEvaluationException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnableToConvertInputException;
import de.uka.ilkd.key.dl.gui.MessageWindow;

/**
 * Class to communicate with the external program HOL Light. Uses consoleinput
 * and -output.
 * 
 * @author Jan-David Quesel
 */
public class ProgramCommunicator {
	private static boolean debug = true;

	public static class Stopper {
		private Process p;

		public boolean stop() {
			if (p != null) {
				p.destroy();
				p = null;
				return true;
			}
			return false;
		}

		/**
		 * @param p
		 *            the p to set
		 */
		public void setP(Process p) {
			this.p = p;
		}
	}

	public static String start(String input, Stopper stopper)
			throws UnableToConvertInputException, IncompleteEvaluationException {
		try {
			ProcessBuilder pb = new ProcessBuilder(new String[] { Options.INSTANCE.getOcamlPath().getAbsolutePath() });

			pb.directory(Options.INSTANCE.getHollightPath());
			Process process = pb.start();
			stopper.setP(process);
			BufferedReader stdout = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			BufferedWriter stdin = new BufferedWriter(new OutputStreamWriter(
					process.getOutputStream()));

			// readUntil(stdout, "#", null);
			// writeText(stdin,
			// "hol_dir := \"/home/user/jdq/space/local/hol_light\";;");
			//			
			// readUntil(stdout, "#", null);
			// writeText(stdin, "load_path := [\".\"; !hol_dir];;");

			readUntil(stdout, "#", null);
			writeText(stdin, "#use \"hol.ml\";;");

			readUntil(stdout, "#", null);
			writeText(stdin, "#use \"Rqe/make.ml\";;");
			readUntil(stdout, "#", null);
			writeText(stdin, "time REAL_QELIM_CONV `" + input + "`;;");

			String s = "";
			String res = "";
			while (true) {
				char read = (char) stdout.read();
				System.out.print(read);
				res += read;
				if (read == '\n') {
					s = "";
				} else {
					s += read;
					if (s.equals("#")) {
						System.out.println("");
						break;
					}
				}
			}

			stdout.close();
			stdin.close();

			return res;

		} catch (IOException e) {
			if (stopper.p == null) {
				throw new IncompleteEvaluationException(
						"The computation was aborted by the user.");
			}
			// Fehler...
			e.printStackTrace();
			return "";
		}

	}

	private static void readUntil(BufferedReader reader, String text,
			String error) throws UnableToConvertInputException {
		try {
			String s = null;
			boolean running = false;
			boolean outputMessage = false;
			StringBuilder errorMessage = new StringBuilder();
			boolean errorOccurred = false;
			s = "";
			while (!running) {
				while (!running) {
					char read = (char) reader.read();
					System.out.print(read);
					if (read == '\n') {
						s = "";
					} else {
						s += read;
						if (s.equals(text)) {
							System.out.println("");
							return;
						}
					}
				}
				s = reader.readLine();
				if (debug) {
					System.out.println(s);
				}
				if (errorOccurred || s.contains("Error")) {
					errorOccurred = true;
					errorMessage.append(s + "\n");
				}
				if (error != null && error.equals(s)) {
					throw new UnableToConvertInputException(
							"An erorr occured while communicating with qepcad. it did not understand the input for: "
									+ error
									+ "\n Message was: "
									+ errorMessage.toString());
				}
				running = s.equals(text);
				if (!running && outputMessage) {
					MessageWindow.INSTNACE.addMessage(s);
				}
				outputMessage |= s.equals("finish");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getResult(BufferedReader reader) {
		try {
			StringBuilder builder = new StringBuilder();
			String s = null;

			while ((s = reader.readLine()) != null
					&& !s
							.equals("=====================  The End  =======================")) {
				builder.append(s);
			}

			return builder.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	private static void writeText(BufferedWriter writer, String text) {
		System.out.println("Want to write: " + text);// XXX
		try {
			writer.write(text);
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * TODO documentation since Jan 28, 2009
	 */
	public static void stop() {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
		try {
			start("!x. ?y. y > x", new Stopper());
		} catch (UnableToConvertInputException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IncompleteEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
