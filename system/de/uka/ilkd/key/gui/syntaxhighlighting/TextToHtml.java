/**
 * 
 */
package de.uka.ilkd.key.gui.syntaxhighlighting;

import java.awt.Font;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zacho
 *
 */
/**
 * The TextToHtml class converts the input String into an Html readable code. It
 * makes sure the special characters & < ... are replace by their corresponding
 * Entities.
 * 
 */
public class TextToHtml {
	Object obj2Highlight;

	/**
	 * @param inputString
	 *            : string to convert
	 * @return the html representation of the input string
	 */
	public static String convert2html(String inputString) {

		// Superscript replacement
		StringBuffer myStringBuffer = new StringBuffer();
		Pattern pattern = Pattern
				.compile("(\\s)?\\^(\\s)?(([-|+]*?\\d+)|([-|+]*?\\w+)|([-|+]*?\\([^(]+?\\)))?");
		// Pattern pattern =
		// Pattern.compile("(\\s)?\\^(\\s)?((\\d+)|(\\w+)|(\\([^(]+\\)))?");
		// Pattern pattern = Pattern.compile("(\\s)?\\^(\\s)??(\\d+)?(\\s)?");
		Matcher matcher = pattern
				.matcher(changeHtmlSpecialCharacters(inputString));
		while (matcher.find()) {
			Pattern p1 = Pattern
					.compile("([-|+]*?\\d+)|([-|+]*?\\w+)|([-|+]*?\\([^(]+?\\))");
			Matcher m1 = p1.matcher(matcher.group());
			if (m1.find())
				matcher.appendReplacement(myStringBuffer, " <sup>" + m1.group()
						+ "</sup>");
		}
		myStringBuffer = matcher.appendTail(myStringBuffer);

		// Subscript replacement

		pattern = Pattern.compile("(_)(\\s)?((\\d|\\w)+)");

		matcher = pattern.matcher(myStringBuffer.toString());
		myStringBuffer = new StringBuffer();

		while (matcher.find()) {
			matcher.appendReplacement(myStringBuffer, "<sub>"
					+ matcher.group().substring(1) + "</sub>");
		}

		myStringBuffer = matcher.appendTail(myStringBuffer);
		
		String result = replaceQunatifiers(myStringBuffer.toString()); 

		result = result.replaceAll("\\+\\+", "&cup;");
		result = result.replaceAll("!", "&not;");

		return addPre(result);
	}
	
	private static String replaceQunatifiers(String input) {
		Pattern pattern = Pattern.compile("(\\\\forall) (\\w)+");
		input = replaceAllWithPrefixedSub(input, pattern, "\u2200");
		pattern = Pattern.compile("(\\\\exists) (\\w)+");
		input = replaceAllWithPrefixedSub(input, pattern, "\u2203");
		return input;
	}
	
	private static String replaceAllWithPrefixedSub(String input, Pattern pattern, String prefix) {
		Matcher matcher = pattern.matcher(input);
		StringBuffer myStringBuffer = new StringBuffer();

		while (matcher.find()) {
			matcher.appendReplacement(myStringBuffer, prefix + "<sub>"
					+ matcher.group(2) + "</sub>");
		}
		
		myStringBuffer = matcher.appendTail(myStringBuffer);
		return myStringBuffer.toString();
	}

	private static String addPre(String htmlText) {

		return "<pre>" + htmlText + "</pre>";
	}

	public static String changeFont(String text, Font font) {

		if (font != null) {
			text = "<font face=\"" + font.getFamily() + "\"size=\""
					+ (font.getSize() / 2 - 4) + "\">" + text + "</font>";

		}

		return "<pre>" + text + "</pre>";
	}

	public static String changeHtmlSpecialCharacters(String s) {

		s = s.replace("&", "&and;");
		s = s.replaceAll(" \\| ", " &or; ");
		s = s.replace("<", "&lt;");
		s = s.replace(">", "&gt;");
		s = s.replace("\"", "&quot;");
		s = s.replace("<", "&lt;");
		// s = s.replace(" ", "&nbsp;");
		return s;

	}

}
