package de.uka.ilkd.key.gui.syntaxhighlighting;
import java.io.*;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

/**
 * This class parses and Html formatted text into a plain text. It takes care of the
 * representation by replacing it by the '^' character. This class is need particulartly for the 
 * SequentView {@link de.uka.ilkd.key.gui.nodeviews.SequentView.java} which is set to "html/text".
 * @author zacho
 */
public class HtmlToText extends HTMLEditorKit.ParserCallback {
    
    private StringBuffer buffer;
    private String input;

    public HtmlToText() {
    }
    /**
     * Construction for input String
     * @param s
     */
    public void parse(String s){
	input=s;	
	replaceSuperscript("^");
	StringReader in = new StringReader(input);
	buffer = new StringBuffer();
	ParserDelegator delegator = new ParserDelegator();
	try {
	    delegator.parse(in, this, Boolean.TRUE); // parse ingnoring charset directive(true for third parameter)
        } catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
        } 
    }
   /**
    * Append Text
    */
    public void handleText(char[] text, int pos) {
	buffer.append(text);
    }

    /**
     * Return plain text.
     */
    public String getText() {	
	return replaceSpace(buffer.toString());
    }
    /**
     * Insert '^' For superscript represenntation and remove space before it in oder
     * to keep the position table correct.
     * @param s
     */
    public void replaceSuperscript(String s){	
	
	if(input!=null){
	    input = input.replaceAll("<sup>", "<sup>"+s);
	    input = input.replaceAll("&#160;<sup>", "<sup>");
	    input = input.replaceAll("&nbsp;<sup>", "<sup>");
	    input = input.replaceAll(" <sup>", "<sup>");
	}
    }
    /**
     * Check whether some space exists before the "^" character in the input string s, if true removes it.
     * @param s
     */
    public String replaceSpace(String s){	
	
	if(s!=null){
	    s = s.replaceAll("&#160;^", "^");
	    s = s.replaceAll("&nbsp;^", "^");
	    s = s.replaceAll(" ^", "^");
	}
	return s;
    }
}
