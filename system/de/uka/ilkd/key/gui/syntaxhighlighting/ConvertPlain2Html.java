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
	inputString = inputString.replace("<", "&lt;");
	
	

	/*FIXME: Commented out the following part because there is an issue
	 * with the cursor position and thus the manual application of rules*/
	 
	StringBuffer myStringBuffer = new StringBuffer();
	Pattern pattern = Pattern.compile("(\\s)?\\^(\\s)?(\\d+)");
	
	Matcher matcher = pattern.matcher(inputString);
	while (matcher.find()) {
	    Pattern p1 = Pattern.compile("\\d+");
            Matcher m1 = p1.matcher(matcher.group());
            if (m1.find())
        	matcher.appendReplacement(myStringBuffer,"&nbsp;<sup>&nbsp;"+m1.group()+"&nbsp;</sup>");	
	}
	
	myStringBuffer = matcher.appendTail(myStringBuffer);
	
	inputString = myStringBuffer.toString();
	inputString = inputString.replace(" ", "&nbsp;");
		
	return addPre(inputString);	
    }

    private static String addPre(String htmlText){
	
	return "<pre>"+htmlText+"</pre>";
    }
    public static String changeFont(String text, Font font){
	
	if (font != null){
	    text = "<font face=\""+font.getFamily()+"\"size=\""+(font.getSize()/2 - 4)+"\">" + text +"</font>";
	    
	}
	
	return "<pre>"+text+"</pre>";
    }
     	
}
