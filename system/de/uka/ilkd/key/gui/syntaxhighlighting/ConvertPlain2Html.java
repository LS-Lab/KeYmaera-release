/**
 * 
 */
package de.uka.ilkd.key.gui.syntaxhighlighting;
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
