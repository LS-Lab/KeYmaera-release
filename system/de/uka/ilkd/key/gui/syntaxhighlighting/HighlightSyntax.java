
package de.uka.ilkd.key.gui.syntaxhighlighting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zacho
 *
 */

public class HighlightSyntax {

    /**
     * @param inputString : string to highlight preferrable in html form
     * @return the Highlighted String in html form.
     */
    public static String highlight(String inputString){
	
	String output = highLighNumericals(inputString);
	Pattern pattern = Pattern.compile(getKeymaeraSpecificTypes());

	StringBuffer myStringBuffer = new StringBuffer();
	Matcher matcher = pattern.matcher(output);
	
	while (matcher.find()) {
	    matcher.appendReplacement(myStringBuffer, "<b><font color=\"blue\">"+ matcher.group()+"</font></b>");	  
	}
	matcher.appendTail(myStringBuffer);
	
	output = myStringBuffer.toString();

	
	return output;	
    }
    /**
     * @return the regex for KeYmaera specific types
     */
    static String getKeymaeraSpecificTypes(){
	return "\\b((if)|(then)|(fi)|(else)|(while)|(end)|(exists)|R|(forall)|(problem))\\b";  
    }

    /**
     * @param inputString : string to highlight preferrable in html form
     * @return the Highlighted String in html form. NB: perform only numerical highligh, for complete highlight use 
     * the static method String Highlight(String inputString)
     */
    static String highLighNumericals(String inputString) {

	Pattern pattern = Pattern.compile("\\b\\d+\\b");

	StringBuffer myStringBuffer = new StringBuffer();
	Matcher matcher = pattern.matcher(inputString); 
	
	while (matcher.find()) {
	    matcher.appendReplacement(myStringBuffer, "<font color=\"red\">"+ matcher.group()+"</font>");	  
	}
	matcher.appendTail(myStringBuffer);
	return myStringBuffer.toString();	
    }

}
