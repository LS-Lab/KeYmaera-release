/**
 * 
 */
package de.uka.ilkd.key.pp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uka.ilkd.key.gui.syntaxhighlighting.ConvertPlain2Html;
import de.uka.ilkd.key.gui.syntaxhighlighting.HighlightSyntax;
import de.uka.ilkd.key.util.pp.Backend;
import de.uka.ilkd.key.util.pp.LayouterHtml;
import de.uka.ilkd.key.util.pp.StringBackend;

/**
 * @author mokom
 *
 */
public class StringBackendHTML extends StringBackend implements Backend{

    protected LayouterHtml layouter;
    private boolean HasSuperScript = false;
    private int NumOfSuperscript;
	public StringBackendHTML(StringBuffer sb, int lineWidth) {
		super(sb, lineWidth);
		NumOfSuperscript = 0;
		// TODO Auto-generated constructor stub
	}

	public StringBackendHTML(int lineWidth) {
		super(lineWidth);
		NumOfSuperscript = 0;
		// TODO Auto-generated constructor stub
	}
	
    /** Append a String <code>s</code> to the output.  <code>s</code> 
     * contains no newlines. */
    public void print(String s) throws java.io.IOException {
    	
    	if (s.contains("^"))
    		s = removeSpaceForSuperscript(s);
    	out.append(s);
    	if (s.equals("^")){
    		//out.deleteCharAt(out.length()-2);
    		NumOfSuperscript++;
    		HasSuperScript = true;
    	}
    	if(HasSuperScript){
    		if (s.equals("^"))
    			HasSuperScript = true;
    		else{
    			out.deleteCharAt(out.length()-1);
    		HasSuperScript = false;
    		}
    	}
    }

    /** Start a new line. */
    public void newLine() throws java.io.IOException {
	out.append('\n');
    }
    /** Returns the number of characters written through this backend.*/
    public int count() {
	return out.length()-initOutLength-NumOfSuperscript;
    }
    
    /** Returns the accumulated output */
    public String getString() {
    	
       String s = out.toString();
      s= ConvertPlain2Html.convert2html(s);
      s = HighlightSyntax.Highlight(s);
	return s;
    }
    
   
private String removeSpaceForSuperscript(String s){
    	
    	StringBuffer myStringBuffer = new StringBuffer();
    	Pattern pattern = Pattern.compile("(\\s)?\\^(\\s)?(\\d+)");
    	
    	Matcher matcher = pattern.matcher(s);
    	while (matcher.find()) {
    		NumOfSuperscript++;
    	    	Pattern p1 = Pattern.compile("\\d+");
                Matcher m1 = p1.matcher(matcher.group());
                if (m1.find())
            	matcher.appendReplacement(myStringBuffer," ^"+m1.group());	
    	}
    	
    	myStringBuffer = matcher.appendTail(myStringBuffer);
    	
    	return myStringBuffer.toString();	
    }

}
