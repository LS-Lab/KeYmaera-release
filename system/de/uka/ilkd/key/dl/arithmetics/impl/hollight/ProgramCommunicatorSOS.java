/*******************************************************************************
 * Copyright (c) 2012 Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jan-David Quesel - initial API and implementation
 ******************************************************************************/
package de.uka.ilkd.key.dl.arithmetics.impl.hollight;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import de.uka.ilkd.key.dl.arithmetics.exceptions.IncompleteEvaluationException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnableToConvertInputException;
import de.uka.ilkd.key.dl.arithmetics.impl.hollight.ProgramCommunicator.Stopper;
import de.uka.ilkd.key.dl.gui.MessageWindow;

/**
 * Class to communicate with the external program HOL Light. Uses consoleinput
 * and -output.
 * 
 * @author Jan-David Quesel
 */
public class ProgramCommunicatorSOS {
	private static boolean debug = true;
	private static Process process = null;
	private static BufferedReader stdout;
	private static BufferedWriter stdin;

	public static String start(String input, final Stopper stopper)
			throws UnableToConvertInputException, IncompleteEvaluationException {
		File tmpFile = null;
		try {
			if (process == null) {
				tmpFile = File.createTempFile("keymaera-ocaml", ".sh");
				tmpFile.createNewFile();
				tmpFile.setExecutable(true);
				FileWriter writer = new FileWriter(tmpFile);
				String program = Options.INSTANCE.getOcamlPath()
                        .getAbsolutePath();
				if(Options.INSTANCE.isUseSnapshots()) {
				    program = Options.INSTANCE.getHollightPath() + File.separator + "hol_sos";
				}
				writer
						.write("#!/bin/bash\n"
								+ "FIFO=/tmp/keymara-ocaml-$$.fifo\n"
								+ "OUTPUT=/tmp/keymara-ocaml-output-$$.fifo\n"
								+ "mkfifo $FIFO\n"
								+ "mkfifo $OUTPUT\n"
								+ program
								+ " < $FIFO > $OUTPUT & pid=$!\n"
								+ "cat $OUTPUT &\n"
								+ "trap \"rm -f $FIFO $OUTPUT; kill -9 $pid\" 0\n"
								+ "(while read BLUB\n"
								+ "do\n"
								+ "if [ x\"${BLUB:0:13}\" = x\"##INTERRUPT##\" ]; then\n"
								+ "kill -2 $pid\n" + "else\n"
								+ "echo \"$BLUB\"\n" + "fi\n" + "done) > $FIFO");
				writer.flush();
				writer.close();
				ProcessBuilder pb = new ProcessBuilder(new String[] { tmpFile
						.getAbsolutePath() });

				pb.directory(Options.INSTANCE.getHollightPath());
				process = pb.start();
				stdout = new BufferedReader(new InputStreamReader(process
						.getInputStream()));
				stdin = new BufferedWriter(new OutputStreamWriter(process
						.getOutputStream()));
				if(!Options.INSTANCE.isUseSnapshots()) {
    				readUntil(stdout, "#", null);
    				writeText(stdin, "#use \"hol.ml\";;");
    
    				readUntil(stdout, "#", null);
    				writeText(stdin, "#use \"Examples/sos.ml\";;");
    				readUntil(stdout, "#", null);
				}
			}
			stopper.setP(process);

			writeText(stdin, "time REAL_SOS `" + input.replaceAll("\\\\", "\\\\\\\\") + "`;;");

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

			// stdout.close();
			// stdin.close();
			// process.destroy();

			return res;

		} catch (IOException e) {
				throw new IncompleteEvaluationException(
						"The computation was aborted by the user.");
//			// Fehler...
//			e.printStackTrace();
//			return "";
		} finally {
			if (tmpFile != null) {
				tmpFile.delete();
			}
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
			// writer.write("##COMMIT##");
			// writer.newLine();
			// writer.flush();
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
