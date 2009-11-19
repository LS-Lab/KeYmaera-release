/**
 * 
 */
package de.uka.ilkd.key.gui.syntaxhighlighting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zacho
 *
 */
/**
 * The ConvertPlain2Html class converts the input String into an Html readable code.
 * It makes sure the special characters & < ... are replace by their corresponding Entities.
 * 
 */
public class ConvertPlain2Html {
    Object obj2Highlight;

    /**
     * @param inputString : string to convert
     * @return the html representation of the input string
     */
    public static String convert2html(String inputString){

	inputString = inputString.replace("&", "&amp;");
	inputString = inputString.replace("<", "&lt;");
	inputString = inputString.replace(">", "&gt;");
	inputString = inputString.replace("\"", "&quot;");
	inputString = inputString.replace("<", "&lt");
	
//	StringBuffer myStringBuffer = new StringBuffer();
//	Pattern pattern = Pattern.compile("(\\s)?\\^(\\s)?(\\d+)");
//	
//	Matcher matcher = pattern.matcher(inputString);
//	
//	while (matcher.find()) {
//	    Pattern p1 = Pattern.compile("\\d+");
//            Matcher m1 = p1.matcher(matcher.group());
//            if (m1.find())
//        	matcher.appendReplacement(myStringBuffer,"<sup>"+m1.group()+"</sup>");	 
//	    System.out.println(matcher.group());
//	}
//	
//	myStringBuffer = matcher.appendTail(myStringBuffer);
//	inputString = myStringBuffer.toString();
//	
//	StringBuffer myStringBuffer = new StringBuffer();
//	Pattern pattern = Pattern.compile(".*[\n|\r]");
//	
//	Matcher matcher = pattern.matcher(inputStream);
//	
//	while (matcher.find()) {
//	    matcher.appendReplacement(myStringBuffer,"<br>"+matcher.group()+"</br>");	  
//	}
//	
//	matcher.appendTail(myStringBuffer);
		

	return "<pre>"+inputString+"</pre>";	
    }

   

}
