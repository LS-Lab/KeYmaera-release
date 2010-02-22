/**
 * 
 */
package de.uka.ilkd.key.pp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uka.ilkd.key.gui.syntaxhighlighting.TextToHtml;
import de.uka.ilkd.key.gui.syntaxhighlighting.HighlightSyntax;
import de.uka.ilkd.key.util.pp.Backend;
import de.uka.ilkd.key.util.pp.StringBackend;

/**
 * @author mokom
 *
 */
/** A {@link StringBackend} for Html
 */
public class StringBackendHTML extends StringBackend implements Backend{

    private boolean hasSuperScript = false;
    // Keeps records of number of superscripts used
    private int numOfSuperscript;

	public StringBackendHTML(StringBuffer sb, int lineWidth) {
		super(sb, lineWidth);
		numOfSuperscript = 0;
		// TODO Auto-generated constructor stub
	}
	/** Create a new StringBackend. And initialise the numOfSuperscript.*/
	public StringBackendHTML(int lineWidth) {
		super(lineWidth);
		numOfSuperscript = 0;
		// TODO Auto-generated constructor stub
	}
	
    /** Append a String <code>s</code> to the output.  <code>s</code> 
     * contains no newlines. */
    public void print(String s) throws java.io.IOException {
    	
    	if (s.contains("^"))
    		s = insertSpaceForSuperscript(s);
    	out.append(s);
    	if (s.equals("^")){
    		//out.deleteCharAt(out.length()-2);
    		numOfSuperscript++;
    		hasSuperScript = true;
    	}
    	if(hasSuperScript){
    		if (s.equals("^"))
    			hasSuperScript = true;
    		else{
    			out.deleteCharAt(out.length()-1);
    		hasSuperScript = false;
    		}
    	}
    }

    /** Returns the number of characters written through this backend.*/
    public int count() {
	return out.length()-initOutLength-numOfSuperscript;
    }
    
    /** Returns the accumulated output */
    public String getString() {
    	
       String s = out.toString();
      s= TextToHtml.convert2html(s);
      s = HighlightSyntax.highlight(s);
	return s;
    }
    
   /** Find supperscript character in String s and insert Space before it*/
    private String insertSpaceForSuperscript(String s){
    	
    	StringBuffer myStringBuffer = new StringBuffer();
    	Pattern pattern = Pattern.compile("(\\s)?\\^(\\s)?(\\d+)");
    	
    	Matcher matcher = pattern.matcher(s);
    	while (matcher.find()) {
    		numOfSuperscript++;
    	    	Pattern p1 = Pattern.compile("\\d+");
                Matcher m1 = p1.matcher(matcher.group());
                if (m1.find())
            	matcher.appendReplacement(myStringBuffer," ^"+m1.group());	
    	}
    	
    	myStringBuffer = matcher.appendTail(myStringBuffer);
    	
    	return myStringBuffer.toString();	
    }

}
