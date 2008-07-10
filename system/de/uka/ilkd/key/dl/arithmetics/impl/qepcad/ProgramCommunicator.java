package de.uka.ilkd.key.dl.arithmetics.impl.qepcad;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Class to communicate with an external program. Uses consoleinput and -output.
 * 
 * @author Timo Michelsen
 */
public class ProgramCommunicator {

	public static String start(QepCadInput input) {
		try {
			Process process = new ProcessBuilder(new String[] { "/home/boomer/Arbeit/qesource/bin/qepcad" }).start();

			BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedWriter stdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

			readUntil(stdout, "Enter an informal description  between '[' and ']':");
			writeText(stdin, input.getDescription());

			readUntil(stdout, input.getDescription() + "Enter a variable list:");
			writeText(stdin, input.getVariableList());

			readUntil(stdout, input.getVariableList() + "Enter the number of free variables:");
			writeText(stdin, String.valueOf(input.getFreeVariableNum()));

			readUntil(stdout, "Enter a prenex formula:");
			writeText(stdin, input.getFormula());

			readUntil(stdout, "Before Normalization >");
			writeText(stdin, "finish");

			// Ergebnis extrahieren
			readUntil(stdout, "An equivalent quantifier-free formula:");

			String res = getResult(stdout);
			stdout.close();
			stdin.close();
			
			return res;

		} catch (IOException e) {
			// Fehler...
			e.printStackTrace();
			return "";
		}

	}
	
	private static void readUntil(BufferedReader reader, String text) {
		try {
			String s = null;
			boolean running = false;
			while (!running ) {
				s = reader.readLine();
				//System.out.println(s);
				running = s.equals(text);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String getResult( BufferedReader reader ) {
		try {
			StringBuilder builder = new StringBuilder();
			String s = null;
			
			while( (s = reader.readLine()) != null && !s.equals("=====================  The End  =======================") ) {
				builder.append(s);
			}
			
			return builder.toString();
		} catch (IOException e) {		
			e.printStackTrace();
			return "";
		}		
	}
	
	private static void writeText(BufferedWriter writer, String text) {
		try {
			writer.write(text);
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
