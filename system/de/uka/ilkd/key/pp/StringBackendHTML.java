/**
 * 
 */
package de.uka.ilkd.key.pp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.gui.syntaxhighlighting.TextToHtml;
import de.uka.ilkd.key.gui.syntaxhighlighting.HighlightSyntax;
import de.uka.ilkd.key.util.pp.Backend;
import de.uka.ilkd.key.util.pp.StringBackend;

/**
 * @author mokom
 *
 */
/**
 * A {@link StringBackend} for Html
 */
public class StringBackendHTML extends StringBackend implements Backend {

	private boolean hasSuperScript = false;
	// Keeps records of number of superscripts used
	private int numOfSuperSub_script;

	private int quantifiers;

	public StringBackendHTML(StringBuffer sb, int lineWidth) {
		super(sb, lineWidth);
		numOfSuperSub_script = 0;
		quantifiers = 0;
	}

	/** Create a new StringBackend. And initialise the numOfSuperSub_script. */
	public StringBackendHTML(int lineWidth) {
		super(lineWidth);
		numOfSuperSub_script = 0;
		quantifiers = 0;
	}

	/**
	 * Append a String <code>s</code> to the output. <code>s</code> contains no
	 * newlines.
	 */
	public void print(String s) throws java.io.IOException {
		if (s.equals("^")) {
			// out.append(' ');
			numOfSuperSub_script++;
			hasSuperScript = true;
		} else if (s.contains("^")) {
			s = insertSpaceForSuperscript(s);
		}
		out.append(s);

		Matcher matcher = Pattern.compile("(_)(\\s)?((\\d|\\w)+)").matcher(s);
		while (matcher.find()) {
			numOfSuperSub_script++;
		}
		matcher = Pattern.compile("(\\\\forall)|(\\\\exists)").matcher(s);
		while (matcher.find()) {
			quantifiers++;
		}
		matcher = Pattern.compile("(\\+\\+)|(>=)|(<=)|(!=)").matcher(s);
		while (matcher.find()) {
			numOfSuperSub_script++;
		}
		if (hasSuperScript) {
			if (s.equals("^"))
				hasSuperScript = true;
			else {
				out.deleteCharAt(out.length() - 1);
				hasSuperScript = false;
			}
		}
	}

	/** Returns the number of characters written through this backend. */
	public int count() {
	    if(Main.isUnicodeView()) {
	        return out.length() - initOutLength - numOfSuperSub_script - 7*quantifiers;
	    } else {
	        return out.length() - initOutLength - numOfSuperSub_script;
	    }
	}

	/** Returns the accumulated output */
	public String getString() {

		String s = out.toString();
		s = TextToHtml.convert2html(s);
		s = HighlightSyntax.highlight(s);

		return s;
	}

	/** Find supperscript character in String s and insert Space before it */
	private String insertSpaceForSuperscript(String s) {

		StringBuffer myStringBuffer = new StringBuffer();
		Pattern pattern = Pattern.compile("(\\s)?\\^(\\s)?(\\d+)");

		Matcher matcher = pattern.matcher(s);
		while (matcher.find()) {
			numOfSuperSub_script++;
			Pattern p1 = Pattern.compile("\\d+");
			Matcher m1 = p1.matcher(matcher.group());
			if (m1.find())
				matcher.appendReplacement(myStringBuffer, " ^" + m1.group());
		}

		myStringBuffer = matcher.appendTail(myStringBuffer);

		return myStringBuffer.toString();
	}
}
