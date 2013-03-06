/*******************************************************************************
 * Copyright (c) 2009 Timo Michelsen, Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Timo Michelsen   - initial API and implementation
 *     Jan-David Quesel - implementation 
 ******************************************************************************/
package de.uka.ilkd.key.dl.arithmetics.impl.qepcad;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uka.ilkd.key.dl.arithmetics.exceptions.IncompleteEvaluationException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnableToConvertInputException;
import de.uka.ilkd.key.dl.gui.MessageWindow;

/**
 * Class to communicate with the external program Qepcad. Uses consoleinput and
 * -output.
 * 
 * @author Timo Michelsen
 */
public class ProgramCommunicator {
	private static boolean debug = true;
	
	public static class Stopper {
		private Process p;
		
		public boolean stop() {
			if(p != null) {
				p.destroy();
				p = null;
				return true;
			}
			return false;
		}

		/**
		 * @param p the p to set
		 */
		public void setP(Process p) {
			this.p = p;
		}
	}

	public static String start(QepCadInput input, Stopper stopper)
			throws UnableToConvertInputException, IncompleteEvaluationException {
		try {
			List<String> query = new ArrayList<String>();
			query.add(Options.INSTANCE.getQepcadBinary().getAbsolutePath());
			int qepcadMemoryLimit = Options.INSTANCE
					.getQepcadMemoryLimit();
			if (qepcadMemoryLimit != -1) {
				query.add("+N" + qepcadMemoryLimit);
			}
			ProcessBuilder pb = new ProcessBuilder(query
					.toArray(new String[query.size()]));

			Map<String, String> environment = pb.environment();
			environment.put("qe", Options.INSTANCE.getQepcadPath()
					.getAbsolutePath());
			environment.put("saclib", Options.INSTANCE.getSaclibPath()
					.getAbsolutePath());
			environment.put("singular", Options.INSTANCE.getSingularPath()
			        .getAbsolutePath());
			environment.put("SINGULAR", Options.INSTANCE.getSingularPath()
			        .getAbsolutePath());

			Process process = pb.start();
			stopper.setP(process);
			BufferedReader stdout = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			BufferedWriter stdin = new BufferedWriter(new OutputStreamWriter(
					process.getOutputStream()));

			readUntil(stdout,
					"Enter an informal description  between '[' and ']':", null);
			writeText(stdin, input.getDescription());

			readUntil(stdout,
					input.getDescription() + "Enter a variable list:",
					"Enter an informal description  between '[' and ']':");
			writeText(stdin, input.getVariableList());

			readUntil(stdout, input.getVariableList()
					+ "Enter the number of free variables:",
					"Enter a variable list:");
			writeText(stdin, String.valueOf(input.getFreeVariableNum()));

			readUntil(stdout, "Enter a prenex formula:",
					"Enter the number of free variables:");
			writeText(stdin, input.getFormula());

			readUntil(stdout, "Before Normalization >",
					"Enter a prenex formula:");
			writeText(stdin, "finish");

			// Ergebnis extrahieren
			readUntil(stdout, "An equivalent quantifier-free formula:",
					"Before Normalization >");

			String res = getResult(stdout);
			stdout.close();
			stdin.close();

			return res;

		} catch (IOException e) {
			if(stopper.p == null) {
				throw new IncompleteEvaluationException("The computation was aborted by the user.");
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
			while (!running) {
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
}
