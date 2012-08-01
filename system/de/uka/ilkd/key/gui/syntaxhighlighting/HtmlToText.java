package de.uka.ilkd.key.gui.syntaxhighlighting;
import java.io.*;
import java.util.*;

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

	private static final Map<String,String> replacements = new LinkedHashMap<String,String>();
	static {
		replacements.put("\\u2200_", "\\\\forall ");
		replacements.put("\\u2203_", "\\\\exists ");
		replacements.put("\\u2227", "&");
		replacements.put("\\u2228", "|");
		replacements.put("\\u222A", "++");
		replacements.put("\\u00AC", "!");
		replacements.put("\\u2264", "<=");
		replacements.put("\\u2265", ">=");
		replacements.put("\\u22C5", "*");
		replacements.put("\\u2212", "-");
		replacements.put("\\u2022", "."); // was used for quantifier separation
		
	}

    public HtmlToText() {	
    }
    /**
     * Construction for input String
     * @param s
     */
    public void parse(String s){
	input=s;
	
	replaceSymbol("^", "<sup>");
	replaceSymbol("_", "<sub>");
	
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
	return applyReplacements(replaceSpace(buffer.toString()));
    }

	public static String applyReplacements(String str) {
		for(String k: replacements.keySet()) {
			str = str.replaceAll(k, replacements.get(k));
		}
		return str;
	}

    /**
     * Restore keymaera superscript and subscript representation by adding the said symbol(pattern)
     * @param s
     * @param pattern
     */
    public void replaceSymbol(String s, String pattern){
	if(input!=null){
	    input = input.replaceAll(pattern, pattern+s);
	}
    }
    /**
     * Check whether some space exists before the "^" character in the input string s, if true removes it.
     * @param s
     */
    public String replaceSpace(String s){
	
	char c =0x20; // Space character
	StringBuffer out = new StringBuffer(s);
	
	for(int i= 1; i <out.length(); i++)
	    if ((out.charAt(i)== '^') && out.charAt(i-1)==c)
		out.deleteCharAt(i-1);	

	return out.toString();
    }

}
