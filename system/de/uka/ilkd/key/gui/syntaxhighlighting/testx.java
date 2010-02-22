/**
 * 
 */
package de.uka.ilkd.key.gui.syntaxhighlighting;

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

//	String aa = TextToHtml.convert2html(" if 1 = 2  sum = man  < + woman \n  else \n exit(1);\n\n return null;");
	String aa = TextToHtml.convert2html(" \\problem1 {  A^(8 +(2+3)) + (56 *89) \n"+
							"\\[ R h,v,t; R c,g,H \\] ( \n"+
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
							" } \n");

	aa = HighlightSyntax.highlight(aa);
	pane.setContentType( "text/html" );
	String text = aa;
	pane.setText(text);
	
	frame.add(pane);
	frame.pack();
	frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

}
