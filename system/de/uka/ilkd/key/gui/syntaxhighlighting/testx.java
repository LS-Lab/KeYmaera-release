/**
 * 
 */
package de.uka.ilkd.key.gui.syntaxhighlighting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.text.html.HTML;

/**
 * @author zacho
 *
 */
public class testx {

    /**
     * @param args
     */
    public static void main(String[] args) {
	// TODO Auto-generated method stub

	JEditorPane pane = new JEditorPane();
	JFrame frame = new JFrame("Test");

//	String aa = TextToHtml.convert2html(" if 1 = 2  sum = man  < + woman \n  else \n exit(1);\n\n return null;"); TextToHtml.convert2html(
	String aa ="<pre> \\problem1 {  A^(8 +(2+3)) + (56 *89) \n"+
							"\\[ R h_2a3,v,t; R c,g,H \\] ( \n"+
							"     g > 0 & h> = 0 & t >= 0 & 0 <= c & c < 1 & v ^ 2 <= 2 * g * (H-h) & H >= 0 \n"+
							"       -> \n"+
							" 	\\[  \n" +
							"              ( \n"+
							"                  {h'=v,v'=-g,t'= 1, h> = 0};       /* falling/jumping */ \n"+
						        "                     if (t > 0 & h = 0)   \n"+
							"                     then   v := -c * v;            /* bounce back */\n"+
							"                            t := 0 \n"+
							"                            fi. \n"+
							"                     elseh   \n" +
							"                            v := 2006668^-+-52 4\n" +
							"                  TEST SPECIAL CHARACTERS \n" +
							"                     BRACES [] [[[[ ]]]] [][], \n" +
							"                     PARANTHESIS () \n" +
							"                     AND & && &&&&&& \n" +
							"                     LESS THAN < << <<< <<<<<< \n" +
							"                     GREATER THAN > >> >>> >>>>>> \n" +
							"                     HTML TAG <html> This is my doc </html> \n" +
							"                     OTHERS % %% %%% ! !!! !!!! ° °° °°° ^ ^^ ^^^ ? ?? ??? etc...\n" +
							"                     fi.\n"+
							"                 )*                               /* repeat these transitions */ \n"+
							"           \\] ( 0 <=h & h <= H)                      /* safety / postcondition */ \n"+
						        "	) \n"+
							" } \n</pre>";

	//aa = HighlightSyntax.highlight(aa);
	aa = insertSubscript(aa);
	pane.setContentType( "text/html" );
	String text = aa;
	pane.setText(text);
	
	frame.add(pane);
	frame.pack();
	frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
 private static String insertSubscript(String s){
    	
    	StringBuffer myStringBuffer = new StringBuffer();
    	Pattern pattern = Pattern.compile("(_)(\\s)?((\\d|\\w)+)");
    	
    	Matcher matcher = pattern.matcher(s);
    	while (matcher.find()) {
    	    System.out.println(matcher.group());
            	matcher.appendReplacement(myStringBuffer,"<sub>"+matcher.group().substring(1)+"</sub>");	
    	}
    	
    	myStringBuffer = matcher.appendTail(myStringBuffer);
    	
    	return myStringBuffer.toString();	
    }


}
